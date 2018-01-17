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
    protected function load_template_list($userid) {
        $list = array();
        
        $it = new RecursiveDirectoryIterator("Modules/muc/Data");
        foreach (new RecursiveIteratorIterator($it) as $file) {
            if ($file->getExtension() == "json") {
                $type = pathinfo(substr(strstr($file, "Modules/muc/Data"), 17), PATHINFO_DIRNAME).'/'.pathinfo($file, PATHINFO_FILENAME);
                $list[$type] = $this->get_template($userid, $type);
            }
        }
        return $list;
    }

    public function get_template($userid, $type) {
        if (file_exists("Modules/muc/Data/$type.json")) {
            $template = json_decode(file_get_contents("Modules/muc/Data/$type.json"));
            if (empty($template->options)) {
                $template->options = array();
            }
            return $template;
        }
    }

    public function get_template_options($userid, $type) {
        $result = $this->get_template($userid, $type);
        if (!is_object($result)) {
            return $result;
        }
        $options = array();
        
        require_once "Modules/muc/Models/ctrl.php";
        $ctrl = new Controller($this->mysqli, $this->redis);
        
        $ctrls = $ctrl->get_list($userid);
        $select = array();
        foreach ($ctrls as $ctrl) {
            $select[] = array('name'=>$ctrl['description'], 'value'=>$ctrl['id']);
        }
        $options[] = array('id'=>'ctrlid',
            'name'=>'Controller',
            'description'=>'The communication controller this device should be registered for.',
            'type'=>'selection',
            'select'=>$select,
            'mandatory'=>true,
        );
        
        if (isset($result->options)) {
            $options = array_merge($options, (array) $result->options);
        }
        return $options;
    }

    public function prepare_template($device) {
        $userid = intval($device['userid']);
        
        $result = $this->get_template($userid, $device['type']);
        if (!is_object($result)) {
            return $result;
        }
        $prefix = $this->parse_prefix($device['nodeid'], $device['name'], $result);
        
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
        
        if (empty($template)) {
            $result = $this->prepare_template($device);
            if (isset($result["success"]) && !$result["success"]) {
                return $result;
            }
            $template = $result;
        }
        if (!is_object($template)) $template = (object) $template;
        
        $result = $this->get_template($userid, $device['type']);
        if (!is_object($result)) {
            return $result;
        }
        parent::init_template($device, $template);
        
        $options = $device['options'];
        if (isset($options['ctrlid'])) {
            $ctrlid = intval($options['ctrlid']);
            
            $options = $this->parse_options($options, $result);
            if (isset($options["success"])) {
                return $options;
            }
            
            // Create device connection
            $result = $this->create_device($ctrlid, $device['name'], $options, $result->devices);
            if (isset($result["success"]) && !$result["success"]) {
                return $result;
            }
            
            if (isset($template->inputs)) {
                $channels = $template->inputs;
                
                // Create channels
                $result = $this->create_channels($userid, $ctrlid, $device['name'], $options, $channels);
                if (isset($result["success"]) && !$result["success"]) {
                    return $result;
                }
            }
        }
        else {
            return array('success'=>false, 'message'=>'Controller ID not found. Only inputs, feeds and their processes were initialized.');
        }
        return array('success'=>true, 'message'=>'Device initialized');
    }

    // Create the channels
    protected function create_device($ctrlid, $name, $options, $devices) {
        if (empty($devices)) {
            return array('success'=>false, 'message'=>'Bad device template. Undefined devices');
        }
        
        require_once "Modules/muc/Models/ctrl.php";
        $ctrl = new Controller($this->mysqli, $this->redis);
        
        require_once "Modules/muc/Models/device.php";
        $device = new DeviceConnection($ctrl, $this->mysqli, $this->redis);
        
        foreach ($devices as $d) {
            $configs = (array) $d;
            
            if (isset($configs['name'])) {
                $configs['id'] = $configs['name'];
                unset($configs['name']);
            }
            else {
                $configs['id'] = $name;
            }
            if (isset($options['deviceAddress'])) $configs['address'] = $options['deviceAddress'];
            if (isset($options['deviceSettings'])) $configs['settings'] = $options['deviceSettings'];
            
            $result = $device->create($ctrlid, $configs['driver'], json_encode($configs));
            if (isset($result["success"]) && !$result["success"]) {
                return $result;
            }
        }
        return array('success'=>true, 'message'=>'Devices successfully created');
    }

    // Create the channels
    protected function create_channels($userid, $ctrlid, $deviceid, $options, $channels) {
        require_once "Modules/muc/Models/ctrl.php";
        $ctrl = new Controller($this->mysqli, $this->redis);
        
        require_once "Modules/muc/Models/channel.php";
        $channel = new Channel($ctrl, $this->mysqli, $this->redis);
        
        foreach($channels as $c) {
            // Create each channel
            $configs = (array) $c;
            $configs['id'] = $configs['name'];
            unset($configs['name']);
            
            $configs['nodeid'] = $configs['node'];
            unset($configs['node']);
            
            if (isset($options['channelAddress'])) $configs['address'] = $options['channelAddress'];
            if (isset($options['channelSettings'])) $configs['settings'] = $options['channelSettings'];
            if (isset($configs['logging'])) {
                $logging = (array) $configs['logging'];
                $logging['nodeid'] = $configs['nodeid'];
                $configs['logging'] = $logging;
            }
            
            if (isset($configs['device'])) {
                $device = $configs['device'];
                unset($configs['device']);
            }
            else {
                $device = $deviceid;
            }
            $result = $channel->create($userid, $ctrlid, $device, json_encode($configs));
            if (isset($result["success"]) && !$result["success"]) {
                return $result;
            }
        }
        return array('success'=>true, 'message'=>'Channels successfully created');
    }

    protected function parse_options($options, $template) {
        $result = array();
        
        // Iterate all options as configured in the template and parse them accordingly, 
        // if they exist in the passed key value options array
        foreach ($template->options as $option) {
            $id = $option->id;
            if (isset($options[$id])) {
                $value = $options[$id];
                
                if (isset($option->syntax)) {
                    $syntaxkey = $option->syntax;
                    if (isset($template->syntax) && isset($template->syntax->$syntaxkey)) {
                        $syntax = $template->syntax->$syntaxkey;
                        
                        // Default syntax is <key1>:<value1>,<key2>:<value2>,...
                        if (empty($result[$syntaxkey])) {
                            $result[$syntaxkey] = "";
                        }
                        else {
                            $result[$syntaxkey] .= isset($syntax->separator) ? $syntax->separator : ',';
                        }
                        
                        if (isset($syntax->keyValue) && !$syntax->keyValue) {
                            $result[$syntaxkey] .= $value;
                        }
                        else {
                            $assignment = isset($syntax->assignment) ? $syntax->assignment : ':';
                            $result[$syntaxkey] .= $id.$assignment.$value;
                        }
                    }
                }
            }
            else if ($option->mandatory) {
                return array('success'=>false, 'message'=>'Mandatory option for device '.$template->device->name.' not configured: '.$id);
            }
            return $result;
        }
    }
}
