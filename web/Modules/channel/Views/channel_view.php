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
        <a style="text-decoration: none;" href="<?php echo $path; ?>muc/view">&nbsp;<button class="btn btn-mini"><span class="icon-cog"></span>&nbsp;<?php echo _('Controller'); ?></button></a>
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
    <div id="channel-header" class="channel-header">
        <h2><?php echo _('Channels'); ?></h2>
        
        <input id="channel-select-all" class="select" type="checkbox"></input>
        <span id="channel-select-header"><i><?php echo _('Select all'); ?></i></span>
        <div class="channel-select-action">
            <button id="channel-delete" class="channel-delete btn hide"><span class="icon-trash"></span></button>
        </div>
    </div>
    
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
    device.list(function(result) { devices = associateById('device', result); });
    channel.load(function(result) {
        if (result.length > 0) {
            var wait = function() {
                if (Object.keys(devices).length == 0) {
                    setTimeout(wait, 100);
                    return;
                }
                channels = associateById('channel', result);
                draw(channels);
            }
            wait();
        }
    });
}

function updateRecords() {
    var time = new Date().getTime();
    if (time - updateTime >= INTERVAL) {
        updateTime = time;
        
        channel.records(drawRecords);
    }
    for (var id in channels) {
        $('#'+id+'-time').html(drawRecordTime(channels[id].time, time));
    }
}

function updaterStart() {
    if (updater != null) {
        clearInterval(updater);
    }
    updater = setInterval(updateRecords, 1000);
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

    var count = 0;
    var list = $("#channel-list").empty();
    for (var id in devices) {
        drawDevice(id, devices[id], list);
    }
    for (var id in channels) {
        var channel = channels[id];
        
        var deviceid = 'device-muc'+channel.ctrlid+'-'+channel.deviceid.toLowerCase().replace(/[._]/g, '-');
        var device = drawDevice(deviceid, { 'id': channel.deviceid }, list);
        
        var checked = "";
        if (selected[id]) {
            checked = "checked";
            count++;
        }
        drawChannel(id, checked, channel, device);
    }
    drawSelected(count);
    
    registerEvents();
}

function drawDevice(id, device, devices) {
    var description;
    if (typeof device.description !== 'undefined') {
        description = device.description;
    }
    else description = "";
    
    var result = $('#'+id);
    if (!result.length || result.attr('id') !== id) {
        devices.append(
            "<div class='device'>" +
                "<div id='"+id+"-header' class='device-header' data-toggle='collapse' data-target='#"+id+"-body'>" +
                    "<table>" +
                        "<tr data-id='"+id+"'>" +
                            "<td><input id='"+id+"-select' class='device-select select' type='checkbox'></input></td>" +
                            "<td>" +
                                "<span class='device-name'>"+device.id+(description.length>0 ? ":" : "")+"</span>" +
                                "<span class='device-description'>"+description+"</span>" +
                            "</td>" +
                            "<td class='device-scan'><span class='icon-search icon-white' title='Scan'></span></td>" +
                            "<td class='device-add'><span class='icon-plus-sign icon-white' title='Add'></span></td>" +
                            "<td class='device-config'><span class='icon-wrench icon-white' title='Configure'></span></td>" +
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
        result = $('#'+id);
    }
    if (Object.keys(channels).length > 0) {
        var checked = true;
        var indeterminate = false;
        for (var i in channels) {
            if (channels[i].deviceid == device.id) {
                if (typeof selected[i] === 'undefined') {
                	selected[i] = false;
                }
                if (selected[i]) {
                	indeterminate = true;
                }
                else {
                	checked = false;
                }
            }
        }
        if (checked) indeterminate = false;
        
        $('#'+id+'-select').prop('checked', checked).prop('indeterminate', indeterminate);
    }
    return result;
}

function drawChannel(id, checked, channel, device) {
    var time = (new Date()).getTime();
    
    var description = "";
    if (typeof channel.description !== 'undefined') {
        description = channel.description;
    }
    
    if (typeof channel.configs === 'undefined') {
        channel.configs = {};
    }
    
    if (typeof channel.configs.unit === 'undefined') {
        channel.configs.unit = "";
    }
    var unit = channel.configs.unit;
    
    if (typeof channel.configs.valueType === 'undefined') {
        channel.configs.valueType = "DOUBLE";
    }
    var type = channel.configs.valueType;
    
    device.append(
        "<tr data-id='"+id+"'>" +
            "<td><input id='"+id+"-select' class='channel-select select' type='checkbox' "+checked+"></input></td>" +
            "<td>" +
                "<span class='channel-name'>"+channel.id+"</span>" +
                (description.length>0 ? "<span style='margin:0px 5px 0px 1px'>:</span>" : "") +
                "<span class='channel-description'>"+description+"</span>" +
            "</td>" +
            "<td id='"+id+"-flag' class='channel-flag'>"+drawRecordFlag(channel.flag)+"</td>" +
            "<td id='"+id+"-time' class='channel-time'>"+drawRecordTime(channel.time, time)+"</td>" +
            "<td id='"+id+"-sample' class='channel-value'>"+drawRecordValue(id, type, channel.value)+"</td>" +
            "<td id='"+id+"-unit' class='channel-unit'><span>"+unit+"</span></td>" +
            "<td id='"+id+"-write' class='channel-action'><span class='icon-pencil' title='Add'></span></td>" +
            "<td id='"+id+"-config' class='channel-action'><span class='icon-wrench' title='Configure'></span></td>" +
        "</tr>"
    );
}

function drawRecords(records) {
    for (var i in records) {
        var record = records[i];
        var id = 'channel-muc'+record.ctrlid+'-'+record.id.toLowerCase().replace(/[._]/g, '-');
        var type = channels[id].configs.valueType;
        
        channels[id].flag = record.flag;
        channels[id].time = record.time;
        channels[id].flag = record.value;
        
        $('#'+id+'-flag').html(drawRecordFlag(record.flag));
        $('#'+id+'-sample').html(drawRecordValue(id, type, record.value));
    }
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

function drawRecordTime(time, now) {
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

function drawSelected(count) {
    if (count == 0) {
        $('#channel-select-all').prop('checked', false).prop('indeterminate', false);
        $('#channel-delete').hide();
    }
    else if (count < Object.keys(channels).length) {
        $('#channel-select-all').prop('checked', false).prop('indeterminate', true);
        $('#channel-delete').show();
    }
    else {
        $('#channel-select-all').prop('checked', true).prop('indeterminate', false);
        $('#channel-delete').show();
    }
}

function selectAll(state) {
    if (state) {
        $('#channel-delete').show();
    }
    else {
        $('#channel-delete').hide();
    }
    for (var id in devices) {
    	if (state && !$('#'+id+'-body').hasClass('in')) {
        	$('#'+id+'-body').collapse('show');
        }
        $('#'+id+'-select').prop('checked', state).prop('indeterminate', false);
    }
    for (var id in channels) {
    	selected[id] = state;
    	
        $('#'+id+'-select').prop('checked', state);
    }
}

function selectDevice(id, state) {
	var device = devices[id];
    
	var count = 0;
    for (var i in channels) {
        if (channels[i].deviceid == device.id) {
        	selected[i] = state;

            $('#'+i+'-select').prop('checked', state);
        }
        if (selected[i]) count++;
    }
    $('#'+id+'-select').prop('indeterminate', false);
    if (!$('#'+id+'-body').hasClass('in')) {
    	$('#'+id+'-body').collapse('show');
    }
    drawSelected(count);
}

function selectChannel(id, state) {
    var checked = true;
    var indeterminate = false;
    var channel = channels[id];
    var deviceid = 'device-muc'+channel.ctrlid+'-'+channel.deviceid.toLowerCase().replace(/[._]/g, '-');
    
    selected[id] = state;
    
	var count = 0;
    for (var i in channels) {
        if (channels[i].deviceid == channel.deviceid) {
            if (selected[i]) {
            	indeterminate = true;
            }
            else {
            	checked = false;
            }
        }
        if (selected[i]) count++;
    }
    drawSelected(count);
    
    if (checked) indeterminate = false;
    $('#'+deviceid+'-select').prop('checked', checked).prop('indeterminate', indeterminate);
}

function registerEvents() {
    $(".channel-action").off();
    $(".channel-action").on("click", ".icon-pencil", function(e) {
        e.stopPropagation();

        $(this).removeClass('icon-pencil').addClass('icon-remove');
        
        var id = $(this).closest('tr').data('id');
        var ch = channels[id];
        
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

        var id = $(this).closest('tr').data('id');
        var ch = channels[id];
        
        var type = 'DOUBLE';
        if (typeof ch.configs !== 'undefined' && typeof ch.configs.valueType !== 'undefined') {
            type = ch.configs.valueType;
        }
        var value = getChannelInputValue(id, type);
        
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

        // Get channel of clicked row
        var id = $(this).closest('tr').data('id');
        var channel = channels[id];
        
        channel_dialog.loadConfig(channel);
    });

    $(".channels").off();
    $(".channels").on("click", ".channel-slider", function(e) {
        e.stopPropagation();

        var id = $(this).closest('tr').data('id');
        var value = channels[id].value;
        if (typeof value === 'string' || value instanceof String) {
            value = (value == 'true');
        }
        if (value !== $(this).is(':checked')) {
            $('#'+id+'-write span').removeClass('icon-remove').addClass('icon-share-alt');
        }
        else {
            $('#'+id+'-write span').removeClass('icon-share-alt').addClass('icon-remove');
        }
    });

    $(".channels").on("keyup", ".channel-input", function(e) {
        e.stopPropagation();
        
        var self = this;
        if (timeout != null) {
            clearTimeout(timeout);
        }
        timeout = setTimeout(function() {
            timeout = null;
            
            var id = $(this).closest('tr').data('id');
            var value = channels[id].value;
            if (!isNaN(value)) {
                value = value.toFixed(3);
            }
            var newVal = $(self).val();
            if (newVal != "" && newVal !== value) {
                $('#'+id+'-write span').removeClass('icon-remove').addClass('icon-share-alt');
            }
            else {
                $('#'+id+'-write span').removeClass('icon-share-alt').addClass('icon-remove');
            }
        }, 200);
    });

    $(".channels").on("click", ".channel-select", function(e) {
        e.stopPropagation();
        
        var id = $(this).closest('tr').data('id');
        var state = $(this).prop('checked');
        selectChannel(id, state);
    });

    $(".channels").on("click", "tr", function(e) {
        e.stopPropagation();
        
        var id = $(this).data('id');
        var select = $('#'+id+'-select');
        var state = !select.prop('checked');
        
        select.prop('checked', state);
        selectChannel(id, state);
    });

    $("#channel-select-all").off("click").on("click", function(e) {
        selectAll($(this).prop('checked'));
    });

    $(".device").off('click');
    $(".device").on("click", ".device-select", function(e) {
        e.stopPropagation();

        var id = $(this).closest('tr').data('id');
        var state = $(this).prop('checked');

        selectDevice(id, state);
    });

    $(".device").on('click', '.device-config', function(e) {
        e.stopPropagation();
        
        // Get device of clicked row
        var id = $(this).closest('tr').data('id');
        var device = devices[id];
        
        device_dialog.loadConfig(device);
    });

    $(".device").on('click', '.device-add', function(e) {
        e.stopPropagation();
        
        // Get device of clicked row
        var id = $(this).closest('tr').data('id');
        var device = devices[id];
        
        channel_dialog.loadNew(device);
    });

    $(".device").on('click', '.device-scan', function(e) {
        e.stopPropagation();
        
        // Get device of clicked row
        var id = $(this).closest('tr').data('id');
        var device = devices[id];
        
        channel_dialog.loadScan(device);
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

function associateById(prefix, list) {
    dict = {};
    for (var i in list) {
        dict[prefix+'-muc'+list[i].ctrlid+'-'+list[i].id.toLowerCase().replace(/[._]/g, '-')] = list[i];
    }
    return dict;
}

$.ajax({ url: path+"muc/driver/registered.json", dataType: 'json', async: true, success: function(result) {
    device_dialog.drivers = result;
}});

$("#device-new").on('click', function () {
    
    device_dialog.loadNew();
});

</script>
