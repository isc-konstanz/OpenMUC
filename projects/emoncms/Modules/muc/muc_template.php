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

require_once "Modules/device/device_template.php";

class MucTemplate extends DeviceTemplate
{
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

    public function prepare_template($device) {
        $userid = intval($device['userid']);
        
        $result = $this->get_template($device['type']);
        if (!is_object($result)) {
            return $result;
        }
        $prefix = $this->parse_prefix($device['nodeid'], $device['name'], $result->prefix);
        
        if (isset($result->feeds)) {
            $feeds = $result->feeds;
            $this->prepare_feeds($userid, $device['nodeid'], $prefix, $feeds);
        }
        else {
            $feeds = [];
        }
        
        if (isset($result->channels)) {
            $channels = $result->channels;
            $this->prepare_inputs($userid, $device['nodeid'], $prefix, $channels);
        }
        else {
            $channels = [];
        }
        
        if (!empty($feeds)) {
            $this->prepare_input_processes($userid, $prefix, $feeds, $channels);
        }
        if (!empty($channels)) {
            $this->prepare_feed_processes($userid, $prefix, $feeds, $channels);
        }
        
        return array('success'=>true, 'feeds'=>$feeds, 'inputs'=>$channels);
    }

    public function init_template($device, $template) {
        $userid = intval($device['userid']);
        
        $result = $this->get_template($device['type']);
        if (!is_object($result)) {
            return $result;
        }
        parent::init_template($device, $template);
        
        $options = $device['options'];
        if (isset($options->ctrlid)) {
            $ctrlid = intval($options->ctrlid);
            
            // Create device connection
            $result = $this->create_device($ctrlid, $device['name'], $result->device);
            if (isset($result["success"]) && !$result["success"]) {
                return array('success'=>false, 'message'=>'Error while creating device connection.');
            }
            
            if (isset($template->inputs)) {
                $channels = $template->inputs;
                
                // Create channels
                $result = $this->create_channels($userid, $ctrlid, $device['name'], $options, $channels);
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
    protected function create_device($ctrlid, $name, $configs) {
        if (empty($configs)) {
            return false;
        }
        
        require_once "Modules/muc/Models/ctrl.php";
        $ctrl = new Controller($this->mysqli, $this->redis);
        
        require_once "Modules/muc/Models/device.php";
        $device = new DeviceConnection($ctrl, $this->mysqli, $this->redis);
        
        $configs->id = $name;
        unset($configs->name);
        
        return $device->create($ctrlid, $configs->driver, json_encode($configs));
    }

    // Create the channels
    protected function create_channels($userid, $ctrlid, $deviceid, $options, $channels) {
        require_once "Modules/muc/Models/ctrl.php";
        $ctrl = new Controller($this->mysqli, $this->redis);
        
        require_once "Modules/muc/Models/channel.php";
        $channel = new Channel($ctrl, $this->mysqli, $this->redis);
        
    	foreach($channels as $c) {
    		// Create each channel
    	    $c->id = $c->name;
    	    unset($c->name);
    	    
    		$c->nodeid = $c->node;
    		unset($c->node);
    		
    		$channel->create($userid, $ctrlid, $deviceid, json_encode($c));
    	}
    	return true;
    }
}
