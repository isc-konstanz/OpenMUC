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

require_once "Modules/device/device_thing.php";

class MucThing extends DeviceThing
{
    protected $ctrl;
    protected $channel;

	// Module required constructor, receives parent as reference
	public function __construct(&$parent) {
		parent::__construct($parent);
		
		require_once "Modules/muc/Models/ctrl.php";
		$this->ctrl = new Controller($this->mysqli, $this->redis);
		
		require_once "Modules/muc/Models/channel.php";
		$this->channel = new Channel($ctrl, $this->mysqli, $this->redis);
	}

	public function get_item($userid, $nodeid, $name, $type, $options) {
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

    public function set_item($itemid, $mapping) {
        if (isset($mapping['ctrlid']) && isset($mapping['channelid']) && isset($mapping['value'])) {
            $ctrlid = intval($mapping['ctrlid']);
    	    
            if (isset($mapping['valueType'])) {
                $valueType = $mapping['valueType'];
    	    }
    	    else $valueType = null;
    	    
    	    $result = $this->channel->write($ctrlid, $mapping['channelid'], $mapping['value'], $valueType);
    		if (isset($result['success']) && !$result['success']) {
    			return $result;
    		}
    		return array('success'=>true, 'message'=>"Item value set");
        }
        return array('success'=>false, 'message'=>"Error while seting item value");
    }

    protected function get_ctrl_id($userid, $name, $driver) {
        require_once "Modules/muc/Models/device.php";
        $device = new DeviceConnection($this->ctrl, $this->mysqli, $this->redis);
        
        $devices = $device->get_list($userid);
        foreach($devices as $d) {
            if ($d['id'] == $name && $d['driverid'] == $driver) {
                return intval($d['ctrlid']);
            }
        }
        return null;
    }
}
