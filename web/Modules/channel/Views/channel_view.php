<?php
global $path;
?>

<script type="text/javascript" src="<?php echo $path; ?>Modules/channel/Views/channel.js"></script>
<link href="<?php echo $path; ?>Modules/channel/Lib/titatoggle-dist-min.css" rel="stylesheet">

<style>
    .device {
        margin-bottom: 12px;
        border: 1px solid #aaa;
    }

    .device-info {
        background-color: #ddd;
        cursor: pointer;
    }

    .device-configure {
        float:right;
        padding:10px;
        width:30px;
        text-align:center;
        color:#666;
        border-left: 1px solid #eee;
    }

    .device-configure:hover {
        background-color:#eaeaea;
    }

    .device-list {
        padding: 0px 5px 5px 5px;
        background-color: #ddd;
    }

    .channel-list {
        background-color: #f0f0f0;
        border-bottom: 1px solid #fff;
        border-item-left: 2px solid #f0f0f0;
        height: 41px;
    }

    .channel {
        color: grey;
        padding-top: 5px;
    }

    .channel-name {
        text-align: right;
        font-weight: bold;
        width: 80%;
    }

    .channel-input {
        text-align: right;
        font-weight: bold;
        width: 1%;
    }

    .channel-input .text {
        color: dimgrey;
        margin-right: 8px;
    }

    .channel-left {
        text-align: right;
    }

    .channel-right {
        text-align: left;
        width: 5%;
    }

    input.number {
        margin-bottom: 2px;
        margin-right: 8px;
        text-align: right;
        width: 55px;
        color: grey;
        background-color: white;
    }

    input.number[disabled] {
        background-color: #eee;
    }
    
    /* Chrome */
    @supports (-webkit-appearance:none) {
        .checkbox-slider--b {
            margin-left: 8px;
            margin-right: 8px;
            height: 20px;
        }
    }
    
    /* IE */
    @media screen and (-ms-high-contrast: active), (-ms-high-contrast: none) {
        .checkbox-slider--b {
            margin-left: 8px;
            margin-right: 8px;
            height: 20px;
        }
    }
    
    /* Firefox */
    _:-moz-tree-row(hover), .checkbox-slider--b {
        margin-left: 8px;
        margin-right: 8px;
        height: 20px;
        width: 20px;
        border-radius: 25px;
        background-color: gainsboro;
    }

    *::before, *::after {
        box-sizing: border-box;
    }
</style>

<div>
    <div id="api-help-header" style="float:right;"><a href="api"><?php echo _('Channels Help'); ?></a></div>
    <div id="local-header"><h2><?php echo _('Channels'); ?></h2></div>

    <div id="channel-list"></div>

    <div id="channel-none" class="alert alert-block hide" style="margin-top: 20px">
        <h4 class="alert-heading"><?php echo _('No Channels configured'); ?></h4>
        <p>
            <?php echo _('Channels represents single data points, representing e.g. the metered active power of a smart meter, the temperature of a temperature sensor, '); ?>
            <?php echo _('any value of digital or analog I/O module or the manufacture data of the device.'); ?>
            <?php echo _('If configured to log sampled data, values will be written in inputs with the same key to allow further processing.'); ?>
            <?php echo _('You may want the next link as a guide for generating your request: '); ?><a href="api"><?php echo _('Channels API helper'); ?></a>
        </p>
       </div>
    <div id="channel-loader" class="ajax-loader"></div>
</div>

<script>

const INTERVAL = 5000;
var path = "<?php echo $path; ?>";

var channels = {};

var redraw = true;

function update() {
    channel.list(function(result) {
        if (result.length != 0) {
            $("#channel-none").hide();
            $("#local-header").show();
            $("#api-help-header").show();

            draw();
        }
        else {
            $("#channel-none").show();
            $("#local-header").hide();
            $("#api-help-header").hide();
        }
        $('#channel-loader').hide();
    });
}

update();

var updater;
function updaterStart() {
    clearInterval(updater);
    updater = null;
    if (INTERVAL > 0) updater = setInterval(update, INTERVAL);
}
function updaterStop() {
    clearInterval(updater);
    updater = null;
}
updaterStart();

//---------------------------------------------------------------------------------------------
// Draw channels
//---------------------------------------------------------------------------------------------
function draw() {
    var list = "";

    $("#channel-list").html(list);
}
</script>