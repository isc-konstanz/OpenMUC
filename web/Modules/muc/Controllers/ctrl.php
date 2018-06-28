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

function ctrl_controller($format, $action, $method)
{
    global $mysqli, $redis, $user, $session;

	$result = false;

	require_once "Modules/muc/Models/ctrl.php";
	$ctrl = new Controller($mysqli, $redis);

	if ($format == 'html')
	{
		if ($action == "view" && $session['write']) $result = view("Modules/muc/Views/ctrl/view.php",array());
		elseif ($action == 'api') $result = view("Modules/muc/Views/ctrl/api.php", array());
	}
	elseif ($format == 'json')
	{
		if ($action == 'list') {
			if ($session['userid']>0 && $session['write']) $result = $ctrl->get_list($session['userid']);
		}
		elseif ($action == "create") {
			if ($session['userid']>0 && $session['write']) $result = $ctrl->create($session['userid'], get('type'), get('address'), get('description'));
		}
		elseif ($action == "config") {
			// Configuration may be retrieved with read key
			if ($session['userid']>0 && $session['write']) $result = $ctrl->get_config($session['userid'], get('id'));
		}
		else {
			$ctrlid = (int) get('id');
			if ($ctrl->exists($ctrlid)) // if the muc exists
			{
				$ctrlget = $ctrl->get($ctrlid);
				if (isset($session['write']) && $session['write'] && $session['userid']>0 && $ctrlget['userid']==$session['userid']) {
					if ($action == "get") $result = $ctrlget;
					elseif ($action == 'set') $result = $ctrl->set_fields($session['userid'], $ctrlid, get('fields'));
					elseif ($action == "delete") $result = $ctrl->delete($session['userid'], $ctrlid);
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