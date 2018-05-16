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

function muc_controller()
{
    global $route;

    $result = false;

    if ($route->action == "controller") {
        require_once "Modules/muc/Controllers/ctrl.php";
        $result = ctrl_controller($route->format, $route->subaction, $route->method);
    }
    elseif ($route->action == "driver") {
    	require_once "Modules/muc/Controllers/driver.php";
    	$result = driver_controller($route->format, $route->subaction, $route->method);
    }
    elseif ($route->action == "device") {
    	require_once "Modules/muc/Controllers/device.php";
    	$result = device_controller($route->format, $route->subaction, $route->method);
    }
    elseif ($route->action == "channel") {
    	require_once "Modules/muc/Controllers/channel.php";
    	$result = channel_controller($route->format, $route->subaction, $route->method);
    }
    return array('content'=>$result);
}