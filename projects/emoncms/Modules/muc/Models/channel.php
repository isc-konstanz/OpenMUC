<?php
/*
 Released under the GNU Affero General Public License.
 See COPYRIGHT.txt and LICENSE.txt.
 
 MUC module contributed by Adrian Minde Adrian_Minde(at)live.de 2017
 ---------------------------------------------------------------------
 Sponsored by http://isc-konstanz.de/
 */

// no direct access
defined('EMONCMS_EXEC') or die('Restricted access');

class Channel
{
	private $input;
	private $ctrl;
	private $mysqli;
	private $redis;
	private $log;

	public function __construct($ctrl, $mysqli, $redis)
	{
		require_once "Modules/input/input_model.php";
		$this->input = new Input($mysqli,$redis,null);

		$this->ctrl = $ctrl;
		$this->mysqli = $mysqli;
		$this->redis = $redis;
		$this->log = new EmonLogger(__FILE__);
	}

	public function create($userid, $ctrlid, $deviceid, $configs)
	{
		$userid = (int) $userid;
		$ctrlid = (int) $ctrlid;

		$configs = (array) json_decode($configs);

		$nodeid = preg_replace('/[^\p{N}\p{L}_\s-.]/u','',$configs['nodeid']);
		$id = preg_replace('/[^\p{N}\p{L}_\s-:]/u','',$configs['id']);
		if (isset($configs['description'])) {
			$description = preg_replace('/[^\p{N}\p{L}_\s-:]/u','',$configs['description']);
		}
		else $description = '';
		
		$authorization = isset($configs['authorization']) ? $configs['authorization'] : null;
		$settings = $this->create_log_settings($userid, $nodeid, $id, $description, $authorization, null);
		if ($description !== '') {
			$this->input->set_fields($settings['inputid'], '{"description":"'.$description.'"}');
		}
		
		$data = array(
				'device' => $deviceid,
				'configs' => $this->parse_channel($id, $description, $settings, $configs)
		);
		$response = $this->ctrl->request($ctrlid, 'channels/'.$id, 'POST', $data);
		if (isset($response["success"]) && !$response["success"]) {
			return $response;
		}
		return array('success'=>true, 'message'=>'Channel successfully added');
	}

	public function get_list($userid)
	{
		$userid = (int) $userid;
		
		$channels = array();
		foreach($this->ctrl->get_list($userid) as $ctrl) {
			// Get drivers of all registered MUCs and add identifying location description and parse their configuration
			$response = $this->ctrl->request($ctrl['id'], 'channels/details', 'GET', null);
			if (isset($response["details"])) {
				foreach($response['details'] as $details) {
					$channels[] = $this->get_channel($ctrl, $details);
				}
			}
		}
		return $channels;
	}

	public function get_states($userid)
	{
		$userid = (int) $userid;
		
		$states = array();
		foreach($this->ctrl->get_list($userid) as $ctrl) {
			// Get drivers of all registered MUCs and add identifying location description
			$response = $this->ctrl->request($ctrl['id'], 'channels/states', 'GET', null);
			if (isset($response["states"])) {
				foreach($response['states'] as $state) {
					$states[] = array(
							'userid'=>$ctrl['userid'],
							'ctrlid'=>$ctrl['id'],
							'id'=>$state['id'],
							'state'=>$state['state']
					);
				}
			}
		}
		return $states;
	}

	public function info($ctrlid, $driverid)
	{
		$ctrlid = (int) $ctrlid;
		
		$response = $this->ctrl->request($ctrlid, 'drivers/'.$driverid.'/infos/details/channel', 'GET', null);
		if (isset($response["success"]) && !$response["success"]) {
			return $response;
		}
		
		return $response['infos'];
	}

	public function get($ctrlid, $id)
	{
		$ctrlid = (int) $ctrlid;
		
		$ctrl = $this->ctrl->get($ctrlid);
		$response = $this->ctrl->request($ctrlid, 'channels/'.$id.'/details', 'GET', null);
		if (isset($response["success"]) && !$response["success"]) {
			return $response;
		}
		$details = (array) $response['details'];
		return $this->get_channel($ctrl, $details);
	}

	private function get_channel($ctrl, $details)
	{
		$time = isset($details['timestamp']) ? $details['timestamp'] : null;
		$value = isset($details['value']) ? $details['value'] : null;
		
		$address = isset($details['channelAddress']) ? $details['channelAddress'] : '';
		$settings = isset($details['channelSettings']) ? $details['channelSettings'] : '';
		$logsettings = $this->get_log_settings($ctrl['userid'], $ctrl['id'], $details);
		
		if (isset($logsettings['inputid'])) {
			$input = $this->get_input($ctrl['userid'], $logsettings['inputid'], $logsettings['nodeid'], $details['id']);
			$description = $input['description'];
		}
		elseif (isset($details['description'])) {
			$description = $details['description'];
		}
		else $description = '';
		
		$channel = array(
				'userid'=>$ctrl['userid'],
				'ctrlid'=>$ctrl['id'],
				'driverid'=>$details['driver'],
				'deviceid'=>$details['device'],
				'nodeid'=>$logsettings['nodeid'],
				'id'=>$details['id'],
				'description'=>$description,
				'time'=>$time,
				'value'=>$value,
				'flag'=>$details['flag'],
				'state'=>$details['state'],
				'address'=>$address,
				'settings'=>$settings
		);
		
		$configs = $this->get_configs($details);
		if (count($configs) > 0) $channel['configs'] = $configs;
		
		if (isset($logsettings['authorization'])) $channel['authorization'] = $logsettings['authorization'];
		
		if (isset($details['disabled'])) {
			$channel['disabled'] = $details['disabled'];
		}
		else $channel['disabled'] = false;
		
		return $channel;
	}

	private function parse_channel($id, $description, $settings, $configs)
	{
		$channel = array(
				'id' => $id
		);
		if ($description !== '') $channel['description'] = $description;
		
		if (isset($configs['address'])) $channel['channelAddress'] = $configs['address'];
		if (isset($configs['settings'])) $channel['channelSettings'] = $configs['settings'];
		if (isset($logsettings)) $channel['loggingSettings'] = $this->parse_log_settings($logsettings);
		
		if (isset($configs['configs'])) {
			$detailconfigs = (array) $configs['configs'];
			
			if (isset($detailconfigs['samplingInterval'])) $channel['samplingInterval'] = $detailconfigs['samplingInterval'];
			if (isset($detailconfigs['samplingTimeOffset'])) $channel['samplingTimeOffset'] = $detailconfigs['samplingTimeOffset'];
			if (isset($detailconfigs['samplingGroup'])) $channel['samplingGroup'] = $detailconfigs['samplingGroup'];
			if (isset($detailconfigs['listening'])) $channel['listening'] = $detailconfigs['listening'];
			if (isset($detailconfigs['loggingInterval'])) $channel['loggingInterval'] = $detailconfigs['loggingInterval'];
			if (isset($detailconfigs['loggingTimeOffset'])) $channel['loggingTimeOffset'] = $detailconfigs['loggingTimeOffset'];
			
			if (isset($detailconfigs['valueType'])) $channel['valueType'] = $detailconfigs['valueType'];
			if (isset($detailconfigs['valueLength'])) $channel['valueLength'] = $detailconfigs['valueLength'];
			if (isset($detailconfigs['unit'])) $channel['unit'] = $detailconfigs['unit'];
			if (isset($detailconfigs['scalingFactor'])) $channel['scalingFactor'] = $detailconfigs['scalingFactor'];
			if (isset($detailconfigs['valueOffset'])) $channel['valueOffset'] = $detailconfigs['valueOffset'];
		}
		if (isset($configs['disabled'])) {
			$channel['disabled'] = $configs['disabled'];
		}
		
		return $channel;
	}
	
	private function create_log_settings($userid, $nodeid, $name, $description, $auth, $authid)
	{
		$settings = array(
				'nodeid' => $nodeid
		);
		
		$key = null;
		if ($auth !== 'NONE') {
			// TODO: check if device for authid exists and fetch devicekey
			switch ($auth)
			{
				case 'WRITE':
					global $user;
					
					$key = $user->get_apikey_write($userid);
					break;
				case 'READ':
					global $user;
					
					$key = $user->get_apikey_read($userid);
					break;
				default:
					$auth = 'DEFAULT';
					break;
			}
			
			$input = $this->get_input_by_node_name($userid, $nodeid, $name);
			if (isset($input)) {
				$inputid = $input['id'];
			}
			else {
				$inputid = $this->create_input($userid, $nodeid, $name, $description);
			}
			$settings['inputid'] = $inputid;
		}
		
		$settings['authorization'] = $auth;
		if (isset($key)) {
			$settings['key'] = $key;
		}
		return $settings;
	}

	private function get_log_settings($userid, $ctrlid, $channel)
	{
		$settings = array();
		if (isset($channel) ) {
			if(isset($channel['loggingSettings'])) {
				$str = $channel['loggingSettings'];
				if (strpos($str, ':') !== false) {
					$parameters = explode(',', $str);
					foreach ($parameters as $parameter) {
						$keyvalue = explode(':', $parameter);
						$settings[$keyvalue[0]] = $keyvalue[1];
					}
				}
			}
		}
		if (!isset($settings['authorization'])) {
			$settings['authorization'] = 'NONE';
		}
		return $settings;
	}

	private function parse_log_settings($settings)
	{
		$arr = array();
		foreach ($settings as $key=>$value) {
			$arr[] = $key.':'.$value;
		}
		return implode(",", $arr);
	}

	private function get_configs($channel)
	{
		$configs = array();
		if (isset($channel)) {
			foreach($channel as $key => $value) {
				if (strcmp($key, 'id') !== 0 &&
						strcmp($key, 'device') !== 0 &&
						strcmp($key, 'channelAddress') !== 0 &&
						strcmp($key, 'channelSettings') !== 0  &&
						strcmp($key, 'loggingSettings') !== 0 &&
						strcmp($key, 'timestamp') !== 0 &&
						strcmp($key, 'value') !== 0 &&
						strcmp($key, 'flag') !== 0 &&
						strcmp($key, 'state') !== 0 &&
						strcmp($key, 'disabled') !== 0) {
					
					$configs[$key] = $value;
				}
			}
		}
		return $configs;
	}
	
	private function create_input($userid, $nodeid, $name, $description)
	{
		$inputid = $this->input->create_input($userid, $nodeid, $name);
		if ($inputid > 0 && $description !== '') {
			$this->input->set_fields($inputid, '{"description":"'.$description.'"}');
		}
		
		return $inputid;
	}
	
	private function get_input_by_node_name($userid, $nodeid, $name)
	{
		$result = $this->mysqli->query("SELECT id, nodeid, name, description, processList FROM input WHERE nodeid = '$nodeid' AND name = '$name'");
		if ($result->num_rows == 0) {
			return null;
		}
		return (array) $result->fetch_object();
	}

	private function get_input($userid, $id)
	{
		if ($this->redis) {
			// Get from redis cache
			return $this->get_redis_input($userid, $id);
		}
		else {
			// Get from mysql db
			return $this->get_mysql_input($userid, $id);
		}
	}

	private function get_redis_input($userid, $id)
	{
		if (!$this->redis->exists("input:$id") && !$this->load_input_to_redis($id)) {
			return null;
		}
		return (array) $this->redis->hGetAll("input:$id");
	}

	private function get_mysql_input($userid, $id)
	{
		$result = $this->mysqli->query("SELECT id, nodeid, name, description, processList FROM input WHERE id = '$id'");
		if ($result->num_rows == 0) {
			return null;
		}
		return (array) $result->fetch_object();
	}

	private function load_redis_input($id)
	{
		$result = $this->mysqli->query("SELECT id, nodeid, name, description, processList FROM input WHERE id = '$id'");
		if ($result->num_rows > 0) {
			$row = (array) $result->fetch_object();
			
			$this->redis->hMSet("input:$id",array(
					'id'=>$id,
					'nodeid'=>$row['nodeid'],
					'name'=>$row['name'],
					'description'=>$row['description'],
					'processList'=>$row['processList']
			));
			
			return true;
		}
		return false;
	}

	public function update($userid, $ctrlid, $id, $configs)
	{
		$ctrlid = (int) $ctrlid;
		
		$configs = (array) json_decode($configs);
		
		$nodeid = preg_replace('/[^\p{N}\p{L}_\s-:]/u','',$configs['nodeid']);
		$newid = preg_replace('/[^\p{N}\p{L}_\s-:]/u','',$configs['id']);
		if (isset($configs['description'])) {
			$description = preg_replace('/[^\p{N}\p{L}_\s-:]/u','',$configs['description']);
		}
		else $description = '';
		
		// TODO: check if input with name and node exists and abort if so
		
		$authorization = isset($configs['authorization']) ? $configs['authorization'] : null;
		$settings = $this->create_log_settings($userid, $nodeid, $id, $description, $authorization, null);
		$channel = $this->parse_channel($newid, $description, $settings, $configs);
		
		$response = $this->ctrl->request($ctrlid, 'channels/'.$id.'/configs', 'PUT', array('configs' => $channel));
		if (isset($response["success"]) && !$response["success"]) {
			return $response;
		}
		
		if ($id !== $newid && isset($settings) && isset($settings['inputid'])) {
			$inputid = $settings['inputid'];
			$this->mysqli->query("UPDATE input SET `name`='$newid',`description`='$description',`nodeid`='$nodeid' WHERE `id` = '$inputid'");
			if ($this->redis) $this->load_redis_input($inputid);
		}
		return array('success'=>true, 'message'=>'Channel successfully updated');
	}

	public function write($ctrlid, $id, $value, $valueType)
	{
		// Make sure to encode the value parameter in the correct format, 
		// depending on its valueType
		if (strtolower($valueType) === 'boolean') {
			switch (strtolower($value)) {
				case 'true':
					$value = True;
					break;
				case 'false':
					$value = False;
					break;
				default:
					return array('success'=>false, 'message'=>'Unknown boolean value: '.$value);
			}
		}
		else if (is_numeric($value)) {
			$value = (float) $value;
		}
		else {
			return array('success'=>false, 'message'=>'Value inconsistend with its type');
		}
		
		$record = array( 'value' => $value );
		
		$response = $this->ctrl->request($ctrlid, 'channels/'.$id, 'PUT', array('record' => $record));
		if (isset($response["success"]) && !$response["success"]) {
			return $response;
		}
		return array('success'=>true, 'message'=>'Channel successfully written to');
	}

	public function delete($ctrlid, $id)
	{
		$ctrlid = (int) $ctrlid;
		
		$response = $this->ctrl->request($ctrlid, 'channels/'.$id, 'DELETE', null);
		if (isset($response["success"]) && !$response["success"]) {
			return $response;
		}
		return array('success'=>true, 'message'=>'Channel successfully removed');
	}

	public function scan($ctrlid, $device, $settings)
	{
		$ctrlid = (int) $ctrlid;

		$response = $this->ctrl->request($ctrlid, 'devices/'.$device.'/scan', 'GET', array('settings' => $settings));
		if (isset($response["success"]) && !$response["success"]) {
			return $response;
		};
		
		$channels = array();
		foreach($response['channels'] as $scan) {
			
			$channel = array(
					'ctrlid'=>$ctrlid,
					'device'=>$device
			);
			if (isset($scan['description'])) $channel['description'] = $scan['description'];
			if (isset($scan['channelAddress'])) $channel['address'] = $scan['channelAddress'];
			if (isset($scan['channelSettings'])) $channel['settings'] = $scan['channelSettings'];
			if (isset($scan['type'])) $channel['configs'] = array('valueType' => $scan['type']);
			if (isset($scan['metadata'])) $channel['metadata'] = $scan['metadata'];
			$channel['authorization'] = 'DEFAULT';
			
			$channels[] = $channel;
		}
		return $channels;
	}
}
