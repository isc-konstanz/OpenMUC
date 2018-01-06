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

function channel_controller($format, $action, $method)
{
	global $mysqli, $redis, $user, $session;

	$result = false;

	require_once "Modules/muc/Models/ctrl.php";
	$ctrl = new Controller($mysqli, $redis);

	require_once "Modules/muc/Models/channel.php";
	$channel = new Channel($ctrl, $mysqli, $redis);

	if ($format == 'html')
	{
		if ($action == "view" && $session['write']) $result = view("Modules/muc/Views/channel/view.php",array());
		elseif ($action == 'api') $result = view("Modules/muc/Views/channel/api.php", array());
	}
	elseif ($format == 'json')
	{
		if ($action == 'list') {
			if ($session['userid']>0 && $session['write']) $result = $channel->get_list($session['userid']);
		}
		elseif ($action == 'states') {
			if ($session['userid']>0 && $session['write']) $result = $channel->get_states($session['userid']);
		}
		else {
			$ctrlid = (int) get('ctrlid');
			if ($ctrl->exists($ctrlid)) // if the controller exists
			{
				$ctrlget = $ctrl->get($ctrlid);
				if (isset($session['write']) && $session['write'] && $session['userid']>0 && $ctrlget['userid']==$session['userid'])
				{
					if ($action == "create") $result = $channel->create($session['userid'], $ctrlid, get('deviceid'), get('configs'));
					elseif ($action == 'info') $result = $channel->info($session['userid'], $ctrlid, get('driverid'));
					elseif ($action == 'scan') $result = $channel->scan($ctrlid, get('deviceid'), get('settings'));
					elseif ($action == "get") $result = $channel->get($ctrlid, get('id'));
					elseif ($action == 'update') $result = $channel->update($session['userid'], $ctrlid, get('nodeid'), get('id'), get('configs'));
					elseif ($action == "write") $result = $channel->write($ctrlid, get('id'), get('value'), get('valueType'));
					elseif ($action == "delete") $result = $channel->delete($ctrlid, get('id'));
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