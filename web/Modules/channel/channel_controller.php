<?php
/*
 Released under the GNU Affero General Public License.
 See COPYRIGHT.txt and LICENSE.txt.
 
 Channel module contributed by Adrian Minde Adrian_Minde(at)live.de 2018
 ---------------------------------------------------------------------
 Sponsored by https://isc-konstanz.de/
 */

// no direct access
defined('EMONCMS_EXEC') or die('Restricted access');

function channel_controller()
{
    global $mysqli, $redis, $session, $route;

    $result = false;

    require_once "Modules/muc/muc_model.php";
    $ctrl = new Controller($mysqli, $redis);

    require_once "Modules/muc/Models/channel_model.php";
    $channel = new Channel($ctrl, $mysqli, $redis);

    require_once "Modules/channel/channel_model.php";
    $cache = new ChannelCache($ctrl, $channel, $redis);

    if ($route->format == 'html') {
        if ($route->action == "view" && $session['write']) $result = view("Modules/channel/Views/channel_view.php", array());
        else if ($route->action == 'api') $result = view("Modules/channel/Views/channel_api.php", array());
    }

    if ($route->format == 'json') {
        if ($route->action == 'list') {
            if ($session['userid']>0 && $session['write']) $result = $cache->get_list($session['userid']);
        }
        else if ($route->action == 'load') {
            if ($session['userid']>0 && $session['write']) $result = $cache->load($session['userid']);
        }
        else {
            $ctrlid = (int) get('ctrlid');
            if ($ctrl->exists($ctrlid)) {
                $ctrlget = $ctrl->get($ctrlid);
                if (isset($session['write']) && $session['write'] && $session['userid'] > 0
                    && $session['userid'] == $ctrlget['userid']) {
                        
                        if ($route->action == "create") $result = $cache->create($session['userid'], $ctrlid, get('driverid'), get('deviceid'), get('configs'));
                        elseif ($route->action == "get") $result = $cache->get($ctrlid, get('id'));
                        elseif ($route->action == 'update') $result = $cache->update($session['userid'], $ctrlid, get('nodeid'), get('id'), get('configs'));
                        elseif ($route->action == "delete") $result = $cache->delete($ctrlid, get('id'));
                    }
            }
            else {
                $result = array('success'=>false, 'message'=>'Controller does not exist');
            }
        }
    }
    return array('content'=>$result);
}
