<?php
	global $path;
?>

<link href="<?php echo $path; ?>Modules/muc/Lib/style.css" rel="stylesheet">
<script type="text/javascript" src="<?php echo $path; ?>Lib/tablejs/table.js"></script>
<script type="text/javascript" src="<?php echo $path; ?>Lib/tablejs/custom-table-fields.js"></script>
<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Lib/muc-table-fields.js"></script>
<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Lib/config/config.js"></script>
<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/device/device.js"></script>
<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/channel/channel.js"></script>

<style>
	#table input[type="text"] {
		width: 88%;
	}
	#table td:nth-of-type(1) { width:14px; text-align: center; }
	#table td:nth-of-type(2) { width:10%;}
	#table td:nth-of-type(3) { width:10%;}
	#table td:nth-of-type(4) { width:10%;}
	#table th:nth-of-type(5) { font-weight:normal; }
	#table td:nth-of-type(6), th:nth-of-type(6) { text-align: right; }
	#table th[fieldg="channels"] { font-weight:normal; }
	#table th[fieldg="state"] { font-weight:normal; text-align: right; }
	#table td:nth-of-type(7) { width:14px; text-align: center; }
	#table td:nth-of-type(8) { width:14px; text-align: center; }
	#table td:nth-of-type(9) { width:14px; text-align: center; }
	#table td:nth-of-type(10) { width:14px; text-align: center; }
	#table th[fieldg="dummy-7"] { width:14px; text-align: center; }
	#table th[fieldg="dummy-8"] { width:14px; text-align: center; }
	#table th[fieldg="dummy-9"] { width:14px; text-align: center; }
	#table th[fieldg="dummy-10"] { width:14px; text-align: center; }
</style>

<div>
	<div id="api-help-header" style="float:right;"><a href="api"><?php echo _('Device connections API Help'); ?></a></div>
	<div id="local-header"><h2><?php echo _('Device connections'); ?></h2></div>

	<div id="table"><div align='center'></div></div>

	<div id="device-none" class="hide">
		<div class="alert alert-block">
			<h4 class="alert-heading"><?php echo _('No device connections'); ?></h4><br>
			<p>
				<?php echo _('Device connections are used to configure and prepare the communication with different metering units.'); ?>
				<br><br>
				<?php echo _('A device configures and prepares inputs, feeds possible device channels, representing e.g. different registers of defined metering units (see the channels tab).'); ?>
				<br>
				<?php echo _('You may want the next link as a guide for generating your request: '); ?><a href="api"><?php echo _('Device API helper'); ?></a>
			</p>
		</div>
	</div>

	<div id="toolbar_bottom"><hr>
		<a id="channel-show" class="btn btn-small" href="<?php echo $path; ?>muc/channel/view"><i class="icon-chevron-right" ></i>&nbsp;<?php echo _('Channels'); ?></a>
		<a id="ctrl-return" class="btn btn-small" href="<?php echo $path; ?>muc/controller/view"><i class="icon-cog" ></i>&nbsp;<?php echo _('Controllers'); ?></a>
		<button id="device-new" class="btn btn-primary btn-small" >&nbsp;<i class="icon-plus-sign icon-white" ></i>&nbsp;<?php echo _('New device'); ?></button>
		<button id="device-scan" class="btn btn-info btn-small" >&nbsp;<i class="icon-search icon-white" ></i>&nbsp;<?php echo _('Scan devices'); ?></button>
	</div>
	
	<div id="device-loader" class="ajax-loader"></div>
</div>

<?php require "Modules/muc/Views/device/dialog/add.php"; ?>
<?php require "Modules/muc/Views/device/dialog/scan.php"; ?>
<?php require "Modules/muc/Views/device/dialog/config.php"; ?>
<?php require "Modules/muc/Views/device/dialog/delete.php"; ?>

<?php require "Modules/muc/Views/channel/dialog/add.php"; ?>
<?php require "Modules/muc/Views/channel/dialog/scan.php"; ?>
<?php require "Modules/muc/Views/channel/dialog/config.php"; ?>
<?php require "Modules/muc/Views/channel/dialog/delete.php"; ?>
<?php require "Modules/muc/Views/channel/dialog/write.php"; ?>

<script>
	var path = "<?php echo $path; ?>";
	
	// Extend table library field types
	for (z in muctablefields) table.fieldtypes[z] = muctablefields[z];
	for (z in customtablefields) table.fieldtypes[z] = customtablefields[z];
	table.element = "#table";
	table.groupprefix = "Driver ";
	table.groupby = 'driver';
	table.groupfields = {
		'dummy-4':{'title':'', 'type':"blank"},
		'channels':{'title':'<?php echo _("Channels"); ?>','type':"group-channellist"},
		'state':{'title':'<?php echo _('State'); ?>', 'type':"group-state"},
		'dummy-7':{'title':'', 'type':"blank"},
		'dummy-8':{'title':'', 'type':"blank"},
		'dummy-9':{'title':'', 'type':"blank"},
		'dummy-10':{'title':'', 'type':"blank"}
	}
	
	table.deletedata = false;
	table.fields = {
		'disabled':{'title':'', 'type':"disable"},
		'driver':{'title':'<?php echo _("Driver"); ?>','type':"fixed"},
		'id':{'title':'<?php echo _("Name"); ?>','type':"text"},
		'description':{'title':'<?php echo _('Description'); ?>','type':"text"},
		'channels':{'title':'<?php echo _("Channels"); ?>','type':"channellist"},
		'state':{'title':'<?php echo _("State"); ?>', 'type':"state"},
		// Actions
		'add-action':{'title':'', 'type':"icon-enabled", 'icon':'icon-plus-sign'},
		'scan-action':{'title':'', 'type':"icon-enabled", 'icon':'icon-search'},
// 	    'edit-action':{'title':'', 'type':"edit"},
		'delete-action':{'title':'', 'type':"delete"},
		'config-action':{'title':'', 'type':"iconconfig", 'icon':'icon-wrench'}
	}

	update();

	channel.states = null;
	function update() {
		
		var requestTime = (new Date()).getTime();
		$.ajax({ url: path+"muc/device/list.json", dataType: 'json', async: true, success: function(data, textStatus, xhr) {
			table.timeServerLocalOffset = requestTime-(new Date(xhr.getResponseHeader('Date'))).getTime(); // Offset in ms from local to server time
			table.data = data;

			table.draw();
			if (table.data.length != 0) {
				$("#device-none").hide();
				$("#local-header").show();
			} else {
				$("#device-none").show();
				$("#local-header").hide();
			}
			$('#device-loader').hide();
		}});

		$.ajax({ url: path+"muc/channel/states.json", dataType: 'json', async: true, success: function(data, textStatus, xhr) {
			// Set the channel states for the labels to be colored correctly
			channel.states = data;
		}});
	}

	var updater;
	function updaterStart(func, interval) {
		
		clearInterval(updater);
		updater = null;
		if (interval > 0) updater = setInterval(func, interval);
	}
	updaterStart(update, 5000);

	$("#table").bind("onEdit", function(e) {
		
		updaterStart(update, 0);
	});

	$("#table").bind("onDisable", function(e,id,row,disable) {
		
		$('#device-loader').show();

		// Get device of clicked row
		var deviceSelection = table.data[row];
		var result = device.update(deviceSelection['ctrlid'], deviceSelection['id'], deviceSelection);

		if (!result.success) {
			alert('Unable to update device:\n'+result.message);
			return false;
		}
		update();
	});

	$("#table").bind("onResume", function(e) {
		
		updaterStart(update, 5000);
	});

	$("#table").bind("onDelete", function(e,id,row) {
		
		// Get device of clicked row
		var deviceSelection = table.data[row];
		
		deviceDeleteDialog.load(deviceSelection, row);
	});

	$("#table").on('click', '.icon-wrench', function() {
		
		// Get device of clicked row
		var deviceSelection = table.data[$(this).attr('row')];
		
		deviceConfigDialog.load(deviceSelection, false);
	});

	$("#table").on('click', '.icon-search', function() {

		// Get device of clicked row
		var deviceSelection = table.data[$(this).attr('row')];

		// Do not open dialog, if no corresponding driver exists
		if (deviceSelection['ctrlid'] > 0 && deviceSelection['state'] != 'UNAVAILABLE') {
			channelScanDialog.load(deviceSelection);
		}
	});

	$("#table").on('click', '.icon-plus-sign', function() {
		
		// Do not open dialog if the icon-plus-sign is used on a group header
		if(!$(this).attr('group')) {
			// Get device of clicked row
			var deviceSelection = table.data[$(this).attr('row')];

			// Do not open dialog, if no corresponding driver exists
			if (deviceSelection['ctrlid'] > 0 && deviceSelection['state'] != 'UNAVAILABLE') {
				channelAddDialog.load(deviceSelection);
			}
		}
	});

	$("#table").on('click', '.channel-label', function() {
		$('#device-loader').show();
		
		// Get the ids of the clicked lable
		var ctrlId = $(this).attr('ctrlid');
		var channelId = $(this).attr('channelid');
		
		var channelSelection = channel.get(ctrlId, channelId);
		channelConfigDialog.load(channelSelection, true);
		
		$('#device-loader').hide();
	});

	$("#device-new").on('click', function () {
		
		deviceAddDialog.load(null);
	});

	$("#device-scan").on('click', function () {
		
		deviceScanDialog.load(null);
	});
</script>