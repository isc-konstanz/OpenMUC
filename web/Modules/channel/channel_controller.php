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
    global $mysqli, $redis, $user, $session, $route;

    $result = false;

    require_once "Modules/channel/channel_model.php";
    $channel = new Channel($mysqli,$redis);

    if ($route->format == 'html') {
        if ($route->action == "view" && $session['write']) $result = view("Modules/channel/Views/channel_view.php", array());
        else if ($route->action == 'api') $result = view("Modules/channel/Views/channel_api.php", array());
    }

    if ($route->format == 'json') {
        if ($route->action == 'list') {
            if ($session['userid']>0 && $session['write']) $result = $channel->get_list($session['userid']);
        }
    }

    return array('content'=>$result);
}
