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

function device_controller($format, $action, $method)
{
	global $mysqli, $redis, $user, $session;

	$result = false;
	
	require_once "Modules/muc/Models/ctrl.php";
	$ctrl = new Controller($mysqli, $redis);
	
	require_once "Modules/muc/Models/device.php";
	$device = new Device($ctrl, $mysqli, $redis);

	if ($format == 'html')
	{
		if ($action == "view" && $session['write']) $result = view("Modules/muc/Views/device/view.php",array());
		elseif ($action == 'api') $result = view("Modules/muc/Views/device/api.php", array());
	}
	elseif ($format== 'json')
	{
		if ($action == 'list') {
			if ($session['userid']>0 && $session['write']) $result = $device->get_list($session['userid']);
		}
		elseif ($action == 'states') {
			if ($session['userid']>0 && $session['write']) $result = $device->get_states($session['userid']);
		}
		else {
			$ctrlid = (int) get('ctrlid');
			if ($ctrl->exists($ctrlid)) // if the controller exists
			{
				$ctrlget = $ctrl->get($ctrlid);
				if (isset($session['write']) && $session['write'] && $session['userid']>0 && $ctrlget['userid']==$session['userid'])
				{
					if ($action == "create") $result = $device->create($ctrlid, get('driverid'), get('configs'));
					elseif ($action == 'info') $result = $device->info($ctrlid, get('driverid'));
					elseif ($action == 'scan') $result = $device->scan($ctrlid, get('driverid'), get('settings'));
					elseif ($action == "scanprogress") $result = $device->scan_progress($ctrlid, get('driverid'));
					elseif ($action == "scancancel") $result = $device->scan_cancel($ctrlid, get('driverid'));
					elseif ($action == "get") $result = $device->get($ctrlid, get('id'));
					elseif ($action == 'update') $result = $device->update($ctrlid, get('id'), get('configs'));
					elseif ($action == "delete") $result = $device->delete($ctrlid, get('id'));
				}
			}
			else
			{
				$result = array('success'=>false, 'message'=>'Controller does not exist');
			}
		}
	}
	return $result;
}