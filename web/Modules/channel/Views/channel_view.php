<?php
    global $path;
?>

<link href="<?php echo $path; ?>Modules/channel/Views/channel.css" rel="stylesheet">
<link href="<?php echo $path; ?>Modules/muc/Views/muc.css" rel="stylesheet">
<link href="<?php echo $path; ?>Modules/muc/Lib/tablejs/titatoggle-dist-min.css" rel="stylesheet">
<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Lib/configjs/config.js"></script>
<script type="text/javascript" src="<?php echo $path; ?>Modules/channel/Views/device.js"></script>
<script type="text/javascript" src="<?php echo $path; ?>Modules/channel/Views/channel.js"></script>

<div class="view-container">
	<div id="api-help-header" style="float:right;"><a href="api"><?php echo _('Channel API Help'); ?></a></div>
    <div id="channel-header" class="channel-header">
        <h2><?php echo _('Channels'); ?></h2>
        
        <input id="channel-select-all" class="select" type="checkbox"></input>
        <span id="channel-select-header"><i><?php echo _('Select all'); ?></i></span>
        <div class="channel-select-action">
            <button id="channel-delete" class="channel-delete btn hide"><span class="icon-trash"></span></button>
        </div>
    </div>
    <div id="channel-none" class="alert alert-block hide" style="margin-top:12px">
        <h4 class="alert-heading"><?php echo _('No Channels configured'); ?></h4>
        <p>
            <?php echo _('Channels represents single data points, representing e.g. the metered active power of a smart meter, the temperature of a temperature sensor, '); ?>
            <?php echo _('any value of digital or analog I/O modules or the manufacture data of the device.'); ?>
            <?php echo _('If configured to log sampled data, values will be written into inputs for the same key, to allow further processing.'); ?>
            <?php echo _('You may want the next link as a guide for generating your request: '); ?><a href="api"><?php echo _('Channels API helper'); ?></a>
        </p>
    </div>
    
    <div id="channel-list"></div>
    
    <div id="channel-loader" class="ajax-loader"></div>
    
    <button id="device-new" class="btn btn-small" >&nbsp;<i class="icon-plus-sign" ></i>&nbsp;<?php echo _('New device connection'); ?></button>
</div>

<div id="channels-delete-modal" class="modal hide" tabindex="-1" role="dialog" aria-labelledby="channels-delete-modal" aria-hidden="true" data-backdrop="static">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="channels-delete-modal"><?php echo _('Delete Channels'); ?></h3>
    </div>
    <div class="modal-body">
        <p><?php echo _('The following channels will be deleted permanently:'); ?>
        </p>
        <div id="channels-delete-list"></div>
        <p style="color:#999">
            <?php echo _('Corresponding configurations will be removed, while inputs, feeds and all historic data will be kept. '); ?>
            <?php echo _('To remove those, delete them manually afterwards.'); ?>
        </p>
        <p>
            <?php echo _('Are you sure you want to proceed?'); ?>
        </p>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true"><?php echo _('Cancel'); ?></button>
        <button id="channels-delete-confirm" class="btn btn-primary"><?php echo _('Delete'); ?></button>
    </div>
</div>

<?php require "Modules/muc/Views/device/device_dialog.php"; ?>
<?php require "Modules/muc/Views/channel/channel_dialog.php"; ?>

<script>

const INTERVAL_RECORDS = 5000;
const INTERVAL_REDRAW = 15000;
var redrawTime = new Date().getTime();
var redraw = false;
var updater;
var timeout;
var path = "<?php echo $path; ?>";

var devices = {};
var channels = {};
var records = {};

var collapsed = {};
var selected = {};

device.list(function(result) {
    draw(result);
    device.load();
    channel.load();
    channel.records(drawRecords);
    
    updaterStart();
});

function update() {
    device.list(draw);
}

function updateView() {
    var time = new Date().getTime();
    if (time - redrawTime >= INTERVAL_REDRAW) {
        redrawTime = time;
        redraw = true;
        
        device.list(function(result) {
            draw(result);
            redraw = false;
            
            channel.records(drawRecords);
        });
    }
    else if (!redraw) {
        if ((time - redrawTime) % INTERVAL_RECORDS == 0) {
            
            channel.records(drawRecords);
        }
        else if (Object.keys(records).length > 0) {
            for (var id in records) {
                $('#'+id+'-time').html(drawRecordTime(id, time));
            }
        }
    }
}

function updaterStart() {
    if (updater != null) {
        clearInterval(updater);
    }
    updater = setInterval(updateView, 1000);
}

function updaterStop() {
    clearInterval(updater);
    updater = null;
}

//---------------------------------------------------------------------------------------------
// Draw devices and channels
//---------------------------------------------------------------------------------------------
function draw(result) {
    $('#channel-loader').hide();
    $("#channel-list").empty();
    
    if (typeof result.success !== 'undefined' && !result.success) {
        alert("Error:\n" + result.message);
        return;
    }
    else if (result.length == 0) {
        $("#channel-none").show();
        $("#channel-header").hide();
        $("#api-help-header").hide();

        return;
    }
    devices = {};
    channels = {};
    
    $("#channel-none").hide();
    $("#channel-header").show();
    $("#api-help-header").show();
    
    var count = 0;
    for (var i in result) {
        count += drawDevice(result[i]);
    }
    drawSelected(count);
    
    registerEvents();
}

function drawDevice(device) {
    var deviceid = 'device-muc'+device.ctrlid+'-'+device.id.toLowerCase().replace(/[._]/g, '-');
    
    var collapse = '';
    if (typeof collapsed[deviceid] === 'undefined') {
    	collapsed[deviceid] = true;
    }
    if (!collapsed[deviceid]) {
    	collapse = 'in';
    }
    
    var description;
    if (typeof device.description !== 'undefined') {
        description = device.description;
    }
    else description = "";
    
    var table = "<table><tbody id='"+deviceid+"'>";
    var count = 0;
    var checked = '';
    if (typeof device.channels !== 'undefined' && device.channels.length > 0) {
        for (var i in device.channels) {
            var channel = device.channels[i];
            var channelid = 'channel-muc'+channel.ctrlid+'-'+channel.id.toLowerCase().replace(/[._]/g, '-');
            
            channels[channelid] = channel;
            
            if (typeof selected[channelid] === 'undefined') {
                selected[channelid] = false;
            }
            if (selected[channelid]) {
                count++;
            }
            table += drawChannel(channelid, channel);
        }
        table += "</tbody></table>";
    }
    else {
        table += "</tbody></table><div id='"+deviceid+"-none' class='alert'>" +
            "No channels configured yet. <a class='device-add'>Add</a> or <a class='device-scan'>scan</a> for channels with the buttons on this connection block." +
        "</div>";
    }
    if (count > 0 && count == device.channels.length) {
        checked = 'checked';
    }
    
    $("#channel-list").append(
        "<div class='device'>" +
            "<div id='"+deviceid+"-header' class='device-header' data-toggle='collapse' data-target='#"+deviceid+"-body'>" +
                "<table>" +
                    "<tr data-id='"+deviceid+"'>" +
                        "<td><input id='"+deviceid+"-select' class='device-select select' type='checkbox' "+checked+"></input></td>" +
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
            "<div id='"+deviceid+"-body' class='collapse "+collapse+"'>" +
                "<div class='channels'>" + table + "</div>" +
            "</div>" +
        "</div>"
    );
    if (count > 0 && count < device.channels.length) {
        $('#'+deviceid+'-select').prop('indeterminate', true);
    }
    delete device['channels'];
    devices[deviceid] = device;
    
    return count;
}

function drawChannel(id, channel) {
    var time = (new Date()).getTime();
    
    var checked = "";
    if (selected[id]) {
        checked = "checked";
    }
    
    if (typeof channel.configs === 'undefined') {
        channel.configs = {};
    }
    
    var description = "";
    if (typeof channel.description !== 'undefined') {
        description = channel.description;
    }
    
    var unit = "";
    if (typeof channel.configs.unit !== 'undefined') {
        unit = channel.configs.unit;
    }
    
    var type = "DOUBLE";
    if (typeof channel.configs.valueType !== 'undefined') {
        type = channel.configs.valueType;
    }
    
    return "<tr id='"+id+"-row' data-id='"+id+"'>" +
            "<td><input id='"+id+"-select' class='channel-select select' type='checkbox' "+checked+"></input></td>" +
            "<td>" +
                "<span class='channel-name'>"+channel.id+"</span>" +
                (description.length>0 ? "<span style='margin:0px 5px 0px 1px'>:</span>" : "") +
                "<span class='channel-description'>"+description+"</span>" +
            "</td>" +
            "<td id='"+id+"-flag' class='channel-flag'>"+drawRecordFlag(id)+"</td>" +
            "<td id='"+id+"-time' class='channel-time'>"+drawRecordTime(id, time)+"</td>" +
            "<td id='"+id+"-sample' class='channel-sample'>"+drawRecordValue(id)+"</td>" +
            "<td id='"+id+"-unit' class='channel-unit'><span>"+unit+"</span></td>" +
            "<td id='"+id+"-write' class='channel-action channel-write' data-action='edit'><span class='icon-pencil' title='Add'></span></td>" +
            "<td id='"+id+"-config' class='channel-action channel-config'><span class='icon-wrench' title='Configure'></span></td>" +
        "</tr>";
}

function drawRecords(result) {
    records = {};
    for (var i in result) {
        var record = result[i];
        var id = 'channel-muc'+record.ctrlid+'-'+record.id.toLowerCase().replace(/[._]/g, '-');
        
        records[id] = record;
        if (typeof channels[id] !== 'undefined' && !redraw) {
            $('#'+id+'-flag').html(drawRecordFlag(id));
            $('#'+id+'-sample').html(drawRecordValue(id));
        }
    }
}

function drawRecordFlag(id) {
    var flag;
    if (typeof records[id] !== 'undefined') {
        flag = records[id].flag;
    }
    else {
        flag = 'LOADING';
    }
    
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

function drawRecordValue(id) {
    var html = "";
    
	var value = "<span style='color: #999'>null</span>";
    if (typeof records[id] !== 'undefined' && typeof records[id].value !== 'undefined' && records[id].value !== null) {
        value = records[id].value;
    }
    
    var type = channels[id].configs.valueType;
    if (type === 'BOOLEAN') {
        var checked = "";
        if (typeof value === 'string' || value instanceof String) {
            value = (value == 'true');
        }
        if (value) {
            checked = "checked";
        }
        html = "<div class='channel-checkbox'>" +
                    "<div id='"+id+"-value' class='channel-value checkbox checkbox-slider--b-flat'>" +
                        "<label>" +
                            "<input type='checkbox' onclick='return false;' "+checked+"><span></span></input>" +
                        "</label>" +
                    "</div>" +
                    "<div id='"+id+"-input' class='channel-input checkbox checkbox-slider--b-flat checkbox-slider-info hide'>" +
                        "<label>" +
                            "<input id='"+id+"-slider' class='channel-slider' type='checkbox' "+checked+"><span></span></input>" +
                        "</label>" +
                    "</div>" +
                "</div>";
    }
    else if (type === 'DOUBLE' || type === 'FLOAT') {
        if (!isNaN(value)) {
            value = parseFloat(value);
            if (Math.abs(value) >= 1000) {
                value = value.toFixed(0);
            }
            else if (Math.abs(value) >= 100) {
                value = value.toFixed(1);
            }
            else {
                value = value.toFixed(2);
            }
        }
        html = "<span id='"+id+"-value' class='channel-value'>"+value+"</span>" +
                "<input id='"+id+"-input' class='channel-input input-small' type='number' step='any' style='display:none'></input>";
    }
    else if (type === 'LONG' || type === 'INTEGER' || type === 'SHORT') {
        if (!isNaN(value)) {
            value = parseInt(value).toFixed(0);
        }
        html = "<span id='"+id+"-value' class='channel-value'>"+value+"</span>" +
                "<input id='"+id+"-input' class='channel-input input-small' type='number' step='1' style='display:none'></input>";
    }
    else {
        html = "<span id='"+id+"-value' class='channel-value'>"+value+"</span>" +
                "<input id='"+id+"-input' class='channel-input input-small' type='text' style='display:none'></input>";
    }
    return html;
}

function drawRecordTime(id, time) {
    var updated = "n/a"
    var color = "rgb(255,0,0)";
    
    if (typeof records[id] !== 'undefined' && records[id].time > 0) {
        var update = (new Date(records[id].time)).getTime();
        var delta = (time - update);
        var secs = Math.abs(delta)/1000;
        var mins = secs/60;
        var hour = secs/3600;
        var day = hour/24;
        
        if ($.isNumeric(secs)) {
            updated = secs.toFixed(0) + "s";
            if (secs.toFixed(0) == 0) updated = "now";
            else if (day>7 && delta>0) updated = "inactive";
            else if (day>2) updated = day.toFixed(1)+" days";
            else if (hour>2) updated = hour.toFixed(0)+" hrs";
            else if (secs>180) updated = mins.toFixed(0)+" mins";
            
            secs = Math.abs(secs);
            if (delta<0) color = "rgb(60,135,170)"
            else if (secs<25) color = "rgb(50,200,50)"
            else if (secs<60) color = "rgb(240,180,20)"; 
            else if (secs<(3600*2)) color = "rgb(255,125,20)"
        }
    }
    return "<span style='color:"+color+";'>"+updated+"</span>";
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
    if (state) {
        $('#'+id+'-select').prop('indeterminate', false);
        if (!$('#'+id+'-body').hasClass('in')) {
            $('#'+id+'-body').collapse('show');
        }
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
    $("#channel-list .collapse").off().on("show hide", function(e) {
        // Remember if the device block is collapsed, to redraw it correctly
        var id = $(this).attr('id').replace('-body', '');
        collapsed[id] = $(this).hasClass('in');
    }),
    
    $(".channel-write").off("click").on("click", function(e) {
        e.stopPropagation();
        
        var id = $(this).closest('tr').data('id');
        var ch = channels[id];
        
        var action = $(this).data('action');
        if (action == 'edit') {
            updaterStop();
            
            $(this).data('action', 'cancel');
            $(this).find('span').removeClass('icon-pencil').addClass('icon-remove');
            
            var type = ch.configs.valueType;
            var value = "";
            if (typeof records[id].value !== 'undefined') {
                value = records[id].value;
            }
            setChannelInputValue(id, type, value);
            
            $('#'+id+'-value').hide();
            $('#'+id+'-input').fadeIn();
        }
        else if (action == 'write') {
            $(this).data('action', 'edit');
            $(this).find('span').removeClass('icon-share-alt').addClass('icon-pencil');
            
            var type = ch.configs.valueType;
            var value = getChannelInputValue(id, type);
            
            channel.write(ch.ctrlid, ch.id, value, type, function(result) {
                if (typeof result.success !== 'undefined' && !result.success) {
                    alert("Error:\n" + result.message);
                    return;
                }
                channel.records(drawRecords);
            });
            
            $(".channel-input").hide();
            $('.channel-value').fadeIn();

            updaterStart();
        }
        else {
            $(this).data('action', 'edit');
            $(this).find('span').removeClass('icon-remove').addClass('icon-pencil');
            
            $(".channel-input").hide();
            $('.channel-value').fadeIn();

            updaterStart();
        }
    });

    $(".channel-config").off("click").on("click", function(e) {
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
        
        var value = null;
        if (typeof records[id] !== 'undefined') {
            value = records[id].value;
        }
        if (typeof value === 'string' || value instanceof String) {
            value = (value == 'true');
        }
        if (value !== $(this).is(':checked')) {
            $('#'+id+'-write').data('action', 'write');
            $('#'+id+'-write span').removeClass('icon-remove').addClass('icon-share-alt');
        }
        else {
            $('#'+id+'-write').data('action', 'cancel');
            $('#'+id+'-write span').removeClass('icon-share-alt').addClass('icon-remove');
        }
    });

    $(".channels").on("click", ".channel-input", function(e) {
        e.stopPropagation();
    }),

    $(".channels").on("keyup", ".channel-input", function(e) {
        e.stopPropagation();
        
        var self = this;
        if (timeout != null) {
            clearTimeout(timeout);
        }
        timeout = setTimeout(function() {
            timeout = null;
            
            var id = $(self).closest('tr').data('id');
            
            var value = null;
            if (typeof records[id] !== 'undefined') {
                value = records[id].value;
                if (!isNaN(value)) {
                    value = value.toFixed(3);
                }
            }
            var newVal = $(self).val();
            if (newVal != "" && newVal !== value) {
                $('#'+id+'-write').data('action', 'write');
                $('#'+id+'-write span').removeClass('icon-remove').addClass('icon-share-alt');
            }
            else {
                $('#'+id+'-write').data('action', 'cancel');
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

$.ajax({ url: path+"muc/driver/registered.json", dataType: 'json', async: true, success: function(result) {
    if (typeof result.success === 'undefined' || result.success) {
        device_dialog.drivers = result;
    }
}});

$("#device-new").on('click', function () {
    
    device_dialog.loadNew();
});

$("#channel-delete").on('click', function () {
    var list = "";
    for (var id in channels) {
        if (selected[id]) {
            list += "<li>"+channels[id].id+"</li>";
        }
    }
    $('#channels-delete-list').html("<ul>"+list+"</ul>");
    $('#channels-delete-modal').modal('show');
});

$("#channels-delete-confirm").on('click', function () {
    var count = 0;
    for (var id in channels) {
        if (selected[id]) {
            delete selected[id];
            
            $('#'+id+'-row').remove();
            channel.remove(channels[id].ctrlid, channels[id].id);

            count++;
        }
    }
    update();
    
    drawSelected(count);
    $('#channels-delete-modal').modal('hide');
});

</script>
