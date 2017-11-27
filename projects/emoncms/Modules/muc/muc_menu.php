<?php
    $domain = "messages";
    bindtextdomain($domain, "Modules/feed/locale");
    bind_textdomain_codeset($domain, 'UTF-8');

    $menu_dropdown_config[] = array(
            'name'=> dgettext($domain, "Device Connections"), 
            'icon'=>'icon-random', 
            'path'=>"muc/device/view", 
            'session'=>"write", 
            'order' => 46
    );
