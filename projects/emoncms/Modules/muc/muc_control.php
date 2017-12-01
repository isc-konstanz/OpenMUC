<?php
/*
     Released under the GNU Affero General Public License.
     See COPYRIGHT.txt and LICENSE.txt.

     Device module contributed by Nuno Chaveiro nchaveiro(at)gmail.com 2015
     ---------------------------------------------------------------------
     Sponsored by http://archimetrics.co.uk/
*/

// no direct access
defined('EMONCMS_EXEC') or die('Restricted access');

require_once "Modules/device/device_control.php";

class MucControl extends DeviceControl
{
	protected $ctrl;
	protected $channel;
	
	// Module required constructor, receives parent as reference
	public function __construct(&$parent) {
		parent::__construct($parent);
		
		require_once "Modules/muc/Models/ctrl.php";
		$this->ctrl = new Controller($this->mysqli, $this->redis);
		
		require_once "Modules/muc/Models/channel.php";
		$this->channel = new Channel($this->ctrl, $this->mysqli, $this->redis);
	}

    protected function load_template_list() {
    	$list = array();
    	
    	$it = new RecursiveDirectoryIterator("Modules/muc/Data");
    	foreach (new RecursiveIteratorIterator($it) as $file) {
    		if ($file->getExtension() == "json") {
    			$type = pathinfo(substr(strstr($file, "Modules/muc/Data"), 17), PATHINFO_DIRNAME).'/'.pathinfo($file, PATHINFO_FILENAME);
    			$list[$type] = json_decode(file_get_contents($file));
	    	}
    	}
        return $list;
    }

    public function get_template($type) {
    	if (file_exists("Modules/muc/Data/$type.json")) {
    		return json_decode(file_get_contents("Modules/muc/Data/$type.json"));
        }
    }

    public function get_control($userid, $nodeid, $name, $type, $options) {
        $file = "Modules/muc/Data/$type.json";
        if (file_exists($file)) {
            $template = json_decode(file_get_contents($file));
        } else {
            return array('success'=>false, 'message'=>"Template file not found '".$file."'");
        }
        
        $ctrlid = null;
        if (isset($options->ctrlid)) {
            $ctrlid = (int) $options->ctrlid;
        }
        else {
            $ctrlid = $this->get_ctrl_id($userid, $name, $template->device->driver);
        }
        
        if (isset($template->prefix)) {
            $prefix = $this->parse_prefix($nodeid, $name, $template->prefix);
        }
        else $prefix = "";
        
        $controls = array();
        for ($i=0; $i<count($template->control); $i++) {
            $control = (array) $template->control[$i];
            
            if (isset($ctrlid)) {
                if (isset($control['mapping'])) {
                    foreach($control['mapping'] as &$entry) {
                        if (isset($entry->channel)) {
                            $channelid = $prefix.$entry->channel;
                            
                            $configs = [];
                            foreach($template->channels as $c) {
                                if ($c->name == $entry->channel) {
                                    if (isset($c->configs->valueType)) {
                                        $configs['valueType'] = $c->configs->valueType;
                                    }
                                }
                            }
                            unset($entry->channel);
                            
                            $entry = array_merge(array('ctrlid'=>$ctrlid, 'channelid'=>$channelid), $configs, (array) $entry);
                        }
                    }
                }
                if (isset($control['input'])) {
                    if (isset($template->channels)) {
                        $inputs = $template->channels;
                    }
                    else if (isset($template->inputs)) {
                        $inputs = $template->inputs;
                    }
                    else $inputs = [];
                    $inputid = $this->get_input_id($userid, $nodeid, $prefix, $control['input'], $inputs);
                    if ($inputid == false) {
                        continue;
                    }
                    
                    unset($control['input']);
                    $control = array_merge($control, array('inputid'=>$inputid));
                }
                if (isset($control['feed'])) {
                    $feedid = $this->get_feed_id($userid, $prefix, $control['feed']);
                    if ($feedid == false) {
                        continue;
                    }
                    unset($control['feed']);
                    $control = array_merge($control, array('feedid'=>$feedid));
                }
                
                $controls[] = $control;
            }
        }
        return $controls;
    }

    public function set_control($channelid, $options, $value) {
    	if (isset($options['ctrlid'])) {
    	    $ctrlid = (int) $options['ctrlid'];
    	    
    	    if (isset($options['valueType'])) {
    	        $valueType = $options['valueType'];
    	    }
    	    else $valueType = null;
    	    
    	    $result = $this->channel->write($ctrlid, $channelid, $value, $valueType);
    		if (isset($result['success']) && !$result['success']) {
    			return $result;
    		}
    	    return array('success'=>true, 'message'=>"Value set");
    	}
    	return array('success'=>false, 'message'=>"Options to set value incomplete");
    }

    public function init_template($userid, $nodeid, $name, $type, $options) {
    	$file = "Modules/muc/Data/$type.json";
        if (file_exists($file)) {
            $template = json_decode(file_get_contents($file));
        } else {
            return array('success'=>false, 'message'=>"Template file not found '" . $file . "'");
        }
        $prefix = $this->parse_prefix($nodeid, $name, $template->prefix);
        
        $feeds = $template->feeds;
        // Create feeds
        $result = $this->create_feeds($userid, $nodeid, $prefix, $feeds);
        if ($result["success"] !== true) {
        	return array('success'=>false, 'message'=>'Error while creating the feeds. ' . $result['message']);
        }
        
        // Create inputs
        if (isset($template->channels)) {
        	$inputs = $template->channels;
        }
        else if (isset($template->inputs)) {
        	$inputs = $template->inputs;
        }
        else $inputs = [];
        $result = $this->create_inputs($userid, $nodeid, $prefix, $inputs);
        if ($result !== true) {
        	return array('success'=>false, 'message'=>'Error while creating the inputs.');
        }
        
        // Create inputs processes
        $result = $this->create_input_processes($userid, $feeds, $inputs);
        if ($result["success"] !== true) {
        	return array('success'=>false, 'message'=>'Error while creating the inputs process list. ' . $result['message']);
        }
        
        // Create feeds processes
        $result = $this->create_feed_processes($userid, $feeds, $inputs);
        if ($result["success"] !== true) {
        	return array('success'=>false, 'message'=>'Error while creating the feeds process list. ' . $result['message']);
        }
        
        if (isset($options->ctrlid)) {
            $ctrlid = (int) $options->ctrlid;
        	
        	// Create device connection
        	$result = $this->create_device($name, $ctrlid, $template->device);
        	if (isset($result["success"]) && !$result["success"]) {
        		return array('success'=>false, 'message'=>'Error while creating device connection.');
        	}
        	
        	if (isset($template->channels)) {
        		$channels = $template->channels;
        		
        		// Create channels
        		$result = $this->create_channels($userid, $ctrlid, $name, $nodeid, $prefix, $options, $channels);
        		if ($result !== true) {
        			return array('success'=>false, 'message'=>'Error while creating device connection channels.');
        		}
        	}
        }
        else {
        	$this->log->info("Controller ID not found. Only inputs, feeds and their processes were initialized.");
        }
        
        return array('success'=>true, 'message'=>'Device initialized');
    }
    
    // Create the channels
    protected function create_device($name, $ctrlid, $configs) {
    	if (empty($configs)) {
    		return false;
    	}
    	$configs->id = $name;
    	
    	require_once "Modules/muc/Models/device.php";
    	$device = new DeviceConnection($this->ctrl, $this->mysqli, $this->redis);
    	
    	return $device->create($ctrlid, $configs->driver, json_encode($configs));
    }
    
    // Create the channels
    protected function create_channels($userid, $ctrlid, $deviceid, $nodeid, $prefix, $options, &$channels) {
    	foreach($channels as $c) {
    		// Create each channel
    		$name = $prefix.$c->name;
    		$c->id = $name;
    		
    		if(property_exists($c, "node")) {
    			$node = $c->node;
    		} else {
    			$node = $nodeid;
    		}
    		$c->nodeid = $node;
    		
    		$this->log->info("create_channels() userid=$userid nodeid=$node name=$name description=$c->description");
    		$this->channel->create($userid, $ctrlid, $deviceid, json_encode($c));
    	}
    	return true;
    }

    protected function get_ctrl_id($userid, $name, $driver) {
        require_once "Modules/muc/Models/device.php";
        $device = new DeviceConnection($this->ctrl, $this->mysqli, $this->redis);
        
        $devices = $device->get_list($userid);
        foreach($devices as $d) {
            if ($d['id'] == $name && $d['driverid'] == $driver) {
                return (int) $d['ctrlid'];
            }
        }
        return null;
    }
}
