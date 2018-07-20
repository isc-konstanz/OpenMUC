<?php
    global $path;
?>

<link href="<?php echo $path; ?>Modules/channel/Views/channel.css" rel="stylesheet">
<link href="<?php echo $path; ?>Modules/muc/Views/muc.css" rel="stylesheet">
<link href="<?php echo $path; ?>Modules/muc/Lib/tablejs/titatoggle-dist-min.css" rel="stylesheet">
<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Lib/configjs/config.js"></script>
<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/device/device.js"></script>
<script type="text/javascript" src="<?php echo $path; ?>Modules/channel/Views/channel.js"></script>

<div class="view-container">
    <div style="float:right">
        <span id="api-help-header"><a href="api"><?php echo _('Channel API Help'); ?></a></span>
        <a href="<?php echo $path; ?>muc/view">&nbsp;<button class="btn btn-mini"><span class="icon-cog"></span>&nbsp;<?php echo _('Controller'); ?></button></a>
    </div>
    <div id="channel-none" class="alert alert-block hide" style="margin-top:40px">
        <h4 class="alert-heading"><?php echo _('No Channels configured'); ?></h4>
        <p>
            <?php echo _('Channels represents single data points, representing e.g. the metered active power of a smart meter, the temperature of a temperature sensor, '); ?>
            <?php echo _('any value of digital or analog I/O modules or the manufacture data of the device.'); ?>
            <?php echo _('If configured to log sampled data, values will be written into inputs for the same key, to allow further processing.'); ?>
            <?php echo _('You may want the next link as a guide for generating your request: '); ?><a href="api"><?php echo _('Channels API helper'); ?></a>
        </p>
       </div>
    <div id="channel-header"><h2><?php echo _('Channels'); ?></h2></div>

    <div id="channel-list"></div>
    
    <div id="channel-loader" class="ajax-loader"></div>
    
    <button id="device-new" class="btn btn-small" >&nbsp;<i class="icon-plus-sign" ></i>&nbsp;<?php echo _('New device connection'); ?></button>
</div>

<?php require "Modules/muc/Views/device/device_dialog.php"; ?>
<?php require "Modules/muc/Views/channel/channel_dialog.php"; ?>

<script>

const INTERVAL = 5000;
var updateTime = new Date().getTime();
var updater;
var timeout;
var path = "<?php echo $path; ?>";

var devices = {};
var channels = {};
var selected = {};

channel.list(function(result) {
    draw(result);
    
    update();
    updaterStart();
});

function update() {
    device.list(function(result) {
        devices = associateById(result);
        
        channel.load(function(result) { channels = associateById(result); draw(channels); });
    });
}

function updateRecords() {
    channel.records(drawRecords);
}

function updaterStart() {
    clearInterval(updater);
    updater = null;
    if (INTERVAL > 0) updater = setInterval(updateRecords, INTERVAL);
}

function updaterStop() {
    clearInterval(updater);
    updater = null;
}

//---------------------------------------------------------------------------------------------
// Draw channels
//---------------------------------------------------------------------------------------------
function draw(channels) {
    $('#channel-loader').hide();
    if (Object.keys(channels).length == 0) {
        $("#channel-none").show();
        $("#channel-header").hide();
        $("#api-help-header").hide();

        return;
    }
    $("#channel-none").hide();
    $("#channel-header").show();
    $("#api-help-header").show();
    
    var list = $("#channel-list").empty();
    var device = new Object();
    for (var key in channels) {
        var channel = channels[key];
        
        var id = 'device-muc'+channel.ctrlid+'-'+channel.deviceid.replace(/[._]/g, '-');
        if (!device.length || device.attr('id') !== id) {
            var description = "";
            var devicekey = channel.ctrlid+':'+channel.deviceid;
            if (typeof devices[devicekey] !== 'undefined' && typeof devices[devicekey].description !== 'undefined') {
                description = devices[devicekey].description;
            }
            
            list.append(
                "<div class='device'>" +
                    "<div id='"+id+"-header' class='device-header' data-toggle='collapse' data-target='#"+id+"-body'>" +
                        "<table>" +
                            "<tr data-id='"+id+"' data-key='"+devicekey+"'>" +
                                "<td>" +
                                    "<span class='device-name'>"+channel.deviceid+(description.length>0 ? ":" : "")+"</span>" +
                                    "<span class='device-description'>"+description+"</span>" +
                                "</td>" +
                                "<td><span class='device-action icon-search icon-white' title='Scan'></span></td>" +
                                "<td><span class='device-action icon-plus-sign icon-white' title='Add'></span></td>" +
                                "<td><span class='device-action icon-wrench icon-white' title='Configure'></span></td>" +
                            "</tr>" +
                        "</table>" +
                    "</div>" +
                    "<div id='"+id+"-body' class='collapse'>" +
                        "<div class='channels'>" +
                            "<table><tbody id='"+id+"'></tbody></table>" +
                        "</div>" +
                    "</div>" +
                "</div>"
            );
            device = $('#'+id);
        }
        drawChannel(key, channel, device);
    }
    registerEvents();
}

function drawChannel(key, channel, device) {
    var id = 'channel-muc'+channel.ctrlid+'-'+channel.id.replace(/[._]/g, '-');
    
    var checked = "";
    if (typeof selected[key] !== 'undefined' && selected[key]) {
        checked = "checked";
    }
    
    var description = "";
    if (typeof channel.description !== 'undefined') {
        description = channel.description;
    }

    var unit = "";
    if (typeof channel.configs !== 'undefined' && typeof channel.configs.unit !== 'undefined') {
        unit = channel.configs.unit;
    }
    
    var type = 'DOUBLE';
    if (typeof channel.configs !== 'undefined' && typeof channel.configs.valueType !== 'undefined') {
        type = channel.configs.valueType;
    }
    
    device.append(
        "<tr data-id='"+id+"' data-key='"+key+"'>" +
            "<td><input class='channel-select select' type='checkbox' "+checked+"></input></td>" +
            "<td>" +
                "<span class='channel-name'>"+channel.id+"</span>" +
                (description.length>0 ? "<span style='margin:0px 5px 0px 1px'>:</span>" : "") +
                "<span class='channel-description'>"+description+"</span>" +
            "</td>" +
            "<td id='"+id+"-flag' class='channel-flag'>"+drawRecordFlag(channel.flag)+"</td>" +
            "<td id='"+id+"-time' class='channel-time'>"+drawRecordTime(channel.time)+"</td>" +
            "<td id='"+id+"-sample' class='channel-value'>"+drawRecordValue(id, type, channel.value)+"</td>" +
            "<td id='"+id+"-unit' class='channel-unit'><span>"+unit+"</span></td>" +
            "<td id='"+id+"-write' class='channel-action'><span class='icon-pencil' title='Add'></span></td>" +
            "<td id='"+id+"-config' class='channel-action'><span class='icon-wrench' title='Configure'></span></td>" +
        "</tr>"
    );
}

function drawRecords(records) {

}

function drawRecordFlag(flag) {
    var color;
    if (flag === 'VALID' || flag === 'CONNECTED' || flag === 'SAMPLING' || flag === 'LISTENING') {
        color = "rgb(50,200,50)";
    }
    else if (flag === 'READING' || flag === 'WRITING' || flag === 'STARTING_TO_LISTEN' || 
            flag === 'SCANNING_FOR_CHANNELS' || flag === 'NO_VALUE_RECEIVED_YET') {
        color = "rgb(240,180,20)";
    }
    else if (flag === 'CONNECTING' || flag === 'WAITING_FOR_CONNECTION_RETRY' || 
            flag === 'DISCONNECTING') {
        color = "rgb(255,125,20)";
    }
    else if (flag === 'LOADING' || flag === 'SAMPLING_AND_LISTENING_DISABLED') {
        color = "rgb(135,135,135)";
    }
    else {
        color = "rgb(255,0,0)";
    }
    return "<span style='color:"+color+"'>"+flag.toLowerCase().replace(/[_]/g, ' ')+"</span><span style='margin-left:1px'>:</span>";
}

function drawRecordTime(time) {
    var now = (new Date()).getTime();
    var update = (new Date(time)).getTime();
    
    var delta = (now - update);
    var secs = Math.abs(delta)/1000;
    var mins = secs/60;
    var hour = secs/3600;
    var day = hour/24;
    
    var updated = secs.toFixed(0) + "s";
    if ((update == 0) || (!$.isNumeric(secs))) updated = "n/a";
    else if (secs.toFixed(0) == 0) updated = "now";
    else if (day>7 && delta>0) updated = "inactive";
    else if (day>2) updated = day.toFixed(1)+" days";
    else if (hour>2) updated = hour.toFixed(0)+" hrs";
    else if (secs>180) updated = mins.toFixed(0)+" mins";
    
    secs = Math.abs(secs);
    var color = "rgb(255,0,0)";
    if (delta<0) color = "rgb(60,135,170)"
    else if (secs<25) color = "rgb(50,200,50)"
    else if (secs<60) color = "rgb(240,180,20)"; 
    else if (secs<(3600*2)) color = "rgb(255,125,20)"
    
    return "<span style='color:"+color+";'>"+updated+"</span>";
}

function drawRecordValue(id, type, value) {
    var html = "";
    
    if (typeof value === 'undefined') {
        value = "";
    }
    if (type == 'BOOLEAN') {
        var checked = "";
        if (typeof value === 'string' || value instanceof String) {
            value = (value == 'true');
        }
        if (value) {
            checked = "checked";
        }
        html = "<div id='"+id+"-value' class='checkbox checkbox-slider--b-flat'>" +
                    "<label>" +
                        "<input type='checkbox' onclick='return false;' "+checked+"><span></span></input>" +
                    "</label>" +
                "</div>" +
                "<div id='"+id+"-input' class='checkbox checkbox-slider--b-flat checkbox-slider-info hide'>" +
                    "<label>" +
                        "<input id='"+id+"-slider' class='channel-slider' type='checkbox' "+checked+"><span></span></input>" +
                    "</label>" +
                "</div>";
    }
    else if (type == 'DOUBLE' || type == 'FLOAT') {
        if (!isNaN(value)) {
            value = parseFloat(value);
            if (Math.abs(value) >= 1000) value = value.toFixed(0);
            else if (Math.abs(value) >= 100) value = value.toFixed(1);
            else if (Math.abs(value) >= 10) value = value.toFixed(2);
        }
        html = "<span id='"+id+"-value'>"+value+"</span>" +
                "<input id='"+id+"-input' type='number' step='any' class='channel-input input-small' style='display:none'></input>";
    }
    else if (type == 'LONG' || type == 'INTEGER' || type == 'SHORT') {
        if (!isNaN(value)) {
            value = parseInt(value).toFixed(0);
        }
        html = "<span id='"+id+"-value'>"+value+"</span>" +
                "<input id='"+id+"-input' type='number' step='1' class='channel-input input-small' style='display:none'></input>";
    }
    else {
        html = "<span id='"+id+"-value'>"+value+"</span>" +
                "<input id='"+id+"-input' type='text' class='channel-input input-small hide'></input>";
    }
    return html;
}

function registerEvents() {
    $(".channel-action").off();
    $(".channel-action").on("click", ".icon-pencil", function(e) {
        e.stopPropagation();

        $(this).removeClass('icon-pencil').addClass('icon-remove');

        var row = $(this).closest('tr');
        var id = row.data('id');
        var ch = channels[row.data('key')];
        
        var type = 'DOUBLE';
        if (typeof ch.configs !== 'undefined' && typeof ch.configs.valueType !== 'undefined') {
            type = ch.configs.valueType;
        }
        var value = "";
        if (typeof ch.value !== 'undefined') {
            value = ch.value;
        }
        setChannelInputValue(id, type, value);
        
        $('#'+id+'-value').hide();
        $('#'+id+'-input').fadeIn();
    });

    $(".channel-action").on("click", ".icon-share-alt", function(e) {
        e.stopPropagation();
        
        $(this).removeClass('icon-share-alt').addClass('icon-pencil');

        var row = $(this).closest('tr');
        var ch = channels[row.data('key')];
        
        var type = 'DOUBLE';
        if (typeof ch.configs !== 'undefined' && typeof ch.configs.valueType !== 'undefined') {
            type = ch.configs.valueType;
        }
        var value = getChannelInputValue(row.data('id'), type);
        
        channel.write(ch.ctrlid, ch.id, value, type, function(result) {
            if (typeof result.success !== 'undefined' && !result.success) {
                alert("Error:\n" + result.message);
            }
        });
    });

    $(".channel-action").on("click", ".icon-remove", function(e) {
        e.stopPropagation();
        
        $(this).removeClass('icon-remove').addClass('icon-pencil');
        
        var id = $(this).closest('tr').data('id');
        $('#'+id+'-input').hide();
        $('#'+id+'-value').fadeIn();
    });

    $(".channel-action").on("click", ".icon-wrench", function(e) {
        e.stopPropagation();
        
        var id = $(this).closest('tr').data('id');
    });

    $(".channels").off();
    $(".channels").on("click", ".channel-slider", function(e) {
        e.stopPropagation();
        
        var row = $(this).closest('tr');
        
        var value = channels[row.data('key')].value;
        if (typeof value === 'string' || value instanceof String) {
            value = (value == 'true');
        }
        if (value !== $(this).is(':checked')) {
            $('#'+row.data('id')+'-write span').removeClass('icon-remove').addClass('icon-share-alt');
        }
        else {
            $('#'+row.data('id')+'-write span').removeClass('icon-share-alt').addClass('icon-remove');
        }
    });

    $(".channels").on("keyup", ".channel-input", function(e) {
        var self = this;
        if (timeout != null) {
            clearTimeout(timeout);
        }
        timeout = setTimeout(function() {
            timeout = null;
            
            var row = $(self).closest('tr');
            
            var value = channels[row.data('key')].value;
            if (!isNaN(value)) {
				value = value.toFixed(3);
            }
            var newVal = $(self).val();
            if (newVal != "" && newVal !== value) {
                $('#'+row.data('id')+'-write span').removeClass('icon-remove').addClass('icon-share-alt');
            }
            else {
                $('#'+row.data('id')+'-write span').removeClass('icon-share-alt').addClass('icon-remove');
            }
        }, 200);
    });
}

function getChannelInputValue(id, type) {
    if (type == 'BOOLEAN') {
        return $('#'+id+'-slider').is(':checked');
    }
    else {
        return $('#'+id+'-input').val();
    }
}

function setChannelInputValue(id, type, value) {
    if (type == 'BOOLEAN') {
        if (typeof value === 'string' || value instanceof String) {
            value = (value == 'true');
        }
        $('#'+id+'-slider').prop('checked', value);
    }
    else {
        if (!isNaN(value) && (type == 'DOUBLE' || type == 'FLOAT')) {
            value = parseFloat(value);
        }
        else if (!isNaN(value) && (type == 'LONG' || type == 'INTEGER' || type == 'SHORT')) {
            value = parseInt(value);
        }
        $('#'+id+'-input').val(value);
    }
}

function associateById(list) {
    dict = {};
    for (var i in list) {
        dict[list[i].ctrlid+':'+list[i].id] = list[i];
    }
    return dict;
}

$.ajax({ url: path+"muc/driver/registered.json", dataType: 'json', async: true, success: function(result) {
	device_dialog.drivers = result;
	device_dialog.drawDrivers(model);
}});

$("#device-new").on('click', function () {
    
    device_dialog.loadNew();
});

</script>
