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

class ChannelCache
{
    private $ctrl;
    private $channel;
    private $redis;
    private $log;

    public function __construct($ctrl, $channel, $redis) {
        $this->ctrl = $ctrl;
        $this->channel = $channel;
        $this->redis = $redis;
        $this->log = new EmonLogger(__FILE__);
    }

    public function create($userid, $ctrlid, $deviceid, $configs) {
        $result = $this->channel->create($userid, $ctrlid, $deviceid, $configs);
        if ($this->redis && isset($result["channel"])) {
            $channel = $result['channel'];
            $id = $channel['id'];
            
            $this->redis->sAdd("user:muc:$ctrlid", $id);
            $this->redis->hMSet("channel:muc$ctrlid:$id", $channel);
        }
        return $result;
    }

    public function exist($ctrlid, $id) {
        static $channel_exists_cache = array(); // Array to hold the cache
        if (isset($channel_exists_cache[$id])) {
            $channel_exist = $channel_exists_cache[$id]; // Retrieve from static cache
        }
        else {
            $channel_exist = false;
            if ($this->redis) {
                $channel_exist = $this->redis->exists("channel:muc$ctrlid:$id");
            }
            else {
                // Always return true if redis is not enabled
                return true;
            }
            $channel_exists_cache[$id] = $channel_exist; // Cache it
        }
        return $channel_exist;
    }

    public function load($userid) {
        $channels = $this->channel->get_list($userid);
        
        if ($this->redis) {
            foreach ($channels as $channel) {
                $ctrlid = $channel['ctrlid'];
                $id = $channel['id'];
                
                $this->redis->sAdd("user:muc:$ctrlid", $id);
                $this->redis->hMSet("channel:muc$ctrlid:$id", array(
                    'id'=>$id,
                    'userid'=>$userid,
                    'ctrlid'=>$ctrlid,
                    'deviceid'=>$channel['driverid'],
                    'deviceid'=>$channel['deviceid'],
                    'nodeid'=>$channel['nodeid'],
                    'description'=>$channel['description']
                ));
            }
        }
        usort($channels, function($c1, $c2) {
            if($c1['deviceid'] == $c2['deviceid'])
                return strcmp($c1['id'], $c2['id']);
            return strcmp($c1['deviceid'], $c2['deviceid']);
        });
        return $channels;
    }

    public function get_list($userid) {
        if (!$this->redis) {
            return $this->channel->get_list($userid);
        }
        $channels = array();
        
        foreach($this->ctrl->get_list($userid) as $ctrl) {
            $ctrlid = $ctrl['id'];
            
            $channelids = $this->redis->sMembers("user:muc:$ctrlid");
            foreach ($channelids as $id) {
                $channel = (array) $this->redis->hGetAll("channel:muc$ctrlid:$id");
                $channel['time'] = null;
                $channel['value'] = null;
                $channel['flag'] = 'LOADING';
                
                $channels[] = $channel;
            }
        }
        usort($channels, function($c1, $c2) {
            if($c1['deviceid'] == $c2['deviceid'])
                return strcmp($c1['id'], $c2['id']);
                return strcmp($c1['deviceid'], $c2['deviceid']);
        });
        return $channels;
    }

    public function get($ctrlid, $id) {
        if (!$this->redis) {
            return $this->channel->get_list($ctrlid, $id);
        }
        $channel = (array) $this->redis->hGetAll("channel:muc$ctrlid:$id");
        $channel['time'] = null;
        $channel['value'] = null;
        $channel['flag'] = 'LOADING';
        
        return $channel;
    }

    public function update($userid, $ctrlid, $nodeid, $id, $configs) {
        $result = $this->channel->update($userid, $ctrlid, $nodeid, $id, $configs);
        if ($this->redis && isset($result["success"]) && $result["success"]) {
            $configs = (array) json_decode($configs);
            $logging = (array) $configs['logging'];
            
            if (isset($configs['description'])) {
                $description = $configs['description'];
            }
            else $description = '';
            
            $newid = $configs['id'];
            if ($id != $newid) {
                $this->redis->del("channel:muc$ctrlid:$id");
                $this->redis->srem("user:muc:$ctrlid", $id);
                
                $this->redis->sAdd("user:muc:$ctrlid", $newid);
            }
            $this->redis->hMSet("channel:muc$ctrlid:$newid", array(
                'id'=>$newid,
                'userid'=>$userid,
                'ctrlid'=>$ctrlid,
                'deviceid'=>$configs['driverid'],
                'deviceid'=>$configs['deviceid'],
                'nodeid'=>$logging['nodeid'],
                'description'=>$description
            ));
        }
        return $result;
    }

    public function delete($ctrlid, $id) {
        $result = $this->channel->delete($ctrlid, $id);
        if ($this->redis && isset($result["success"]) && $result["success"]) {
            $this->redis->del("channel:muc$ctrlid:$id");
            $this->redis->srem("user:muc:$ctrlid", $id);
        }
        return $result;
    }

}
