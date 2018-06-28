<?php
	global $path;
?>

<link href="<?php echo $path; ?>Modules/muc/Lib/style.css" rel="stylesheet">
<script type="text/javascript" src="<?php echo $path; ?>Lib/tablejs/table.js"></script>
<script type="text/javascript" src="<?php echo $path; ?>Lib/tablejs/custom-table-fields.js"></script>
<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Lib/muc-table-fields.js"></script>
<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Lib/config/config.js"></script>
<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/driver/driver.js"></script>
<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/device/device.js"></script>

<style>
	#table input[type="text"] {
	    width: 88%;
	}
	#table td:nth-of-type(1) { width:14px; text-align: center; }
	#table td:nth-of-type(2) { width:10%;}
	#table td:nth-of-type(3) { width:10%;}
	#table th:nth-of-type(4), td:nth-of-type(4) { font-weight:normal; }
	#table th[fieldg="devices"] { font-weight:normal; }
	#table td:nth-of-type(5) { width:14px; text-align: center; }
	#table td:nth-of-type(6) { width:14px; text-align: center; }
	#table td:nth-of-type(7) { width:14px; text-align: center; }
	#table td:nth-of-type(8) { width:14px; text-align: center; }
</style>

<div>
	<div id="api-help-header" style="float:right;"><a href="api"><?php echo _('Driver API Help'); ?></a></div>
	<div id="local-header"><h2><?php echo _('Drivers'); ?></h2></div>

	<div id="table"><div align='center'></div></div>

	<div id="driver-none" class="alert alert-block hide">
		<h4 class="alert-heading"><?php echo _('No drivers created'); ?></h4>
		<p>
			<?php echo _('Drivers are used to configure the basic communication with different devices.'); ?>
			<br><br>
			<?php echo _('A driver implements for example the necessary communication protocol, to read several configured energy metering devices (see the devices tab).'); ?>
			<br>
			<?php echo _('You may want the next link as a guide for generating your request: '); ?><a href="api"><?php echo _('Driver API helper'); ?></a>
		</p>
	</div>

	<div id="toolbar-bottom"><hr>
		<a id="ctrl-return" class="btn btn-small" href="<?php echo $path; ?>muc/controller/view"><i class="icon-chevron-left" ></i>&nbsp;<?php echo _('Controllers'); ?></a>
		<a id="device-show" class="btn btn-small" href="<?php echo $path; ?>muc/device/view"><i class="icon-chevron-right" ></i>&nbsp;<?php echo _('Devices'); ?></a>
		<button id="driver-new" class="btn btn-primary btn-small"><i class="icon-plus-sign icon-white" ></i>&nbsp;<?php echo _('New driver'); ?></button>
	</div>

	<div id="driver-loader" class="ajax-loader"></div>
</div>

<?php require "Modules/muc/Views/driver/dialog/add.php"; ?>
<?php require "Modules/muc/Views/driver/dialog/config.php"; ?>
<?php require "Modules/muc/Views/driver/dialog/delete.php"; ?>

<?php require "Modules/muc/Views/device/dialog/add.php"; ?>
<?php require "Modules/muc/Views/device/dialog/scan.php"; ?>
<?php require "Modules/muc/Views/device/dialog/config.php"; ?>
<?php require "Modules/muc/Views/device/dialog/delete.php"; ?>

<script>
	var path = "<?php echo $path; ?>";
	
	// Extend table library field types
	for (z in muctablefields) table.fieldtypes[z] = muctablefields[z];
	for (z in customtablefields) table.fieldtypes[z] = customtablefields[z];
	table.element = "#table";
	table.groupprefix = "Controller ";
	table.groupby = 'controller';

	table.deletedata = false;
	table.fields = {
		'disabled':{'title':'', 'type':"disable"},
		'name':{'title':'<?php echo _("Name"); ?>','type':"fixed"},
		'controller':{'title':'<?php echo _("Controller"); ?>','type':"fixed"},
		'devices':{'title':'<?php echo _("Devices"); ?>','type':"devicelist"},
		// Actions
		'add-action':{'title':'', 'type':"icon-enabled", 'icon':'icon-plus-sign'},
		'scan-action':{'title':'', 'type':"icon-enabled", 'icon':'icon-search'},
		'delete-action':{'title':'', 'type':"delete"},
		'config-action':{'title':'', 'type':"iconconfig", 'icon':'icon-wrench'}
	}

	update();

	device.states = null;
	function update() {
		
		var requestTime = (new Date()).getTime();
		$.ajax({ url: path+"muc/ctrl/list.json", dataType: 'json', async: true, success: function(data, textStatus, xhr) {
			if (data.length == 0) {
				$("#driver-new").prop('disabled', true);
			} else {
				$("#driver-new").prop('disabled', false);
			}
		}});
		
		$.ajax({ url: path+"muc/driver/list.json", dataType: 'json', async: true, success: function(data, textStatus, xhr) {
			table.timeServerLocalOffset = requestTime-(new Date(xhr.getResponseHeader('Date'))).getTime(); // Offset in ms from local to server time
			table.data = data;
			
			table.draw();
			if (table.data.length == 0) {
				$("#driver-none").show();
				$("#local-header").hide();
			} else {
				$("#driver-none").hide();
				$("#local-header").show();
			}
			$('#driver-loader').hide();
		}});
		
		$.ajax({ url: path+"muc/device/states.json", dataType: 'json', async: true, success: function(data, textStatus, xhr) {
			if (device.states == null) {
				device.states = data;
				table.draw();
			}
			else device.states = data;
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
		
		$('#driver-loader').show();

		// Get driver of clicked row
		var driverSelection = table.data[row];
		var result = driver.update(driverSelection['ctrlid'], driverSelection['id'], driverSelection);

		if (!result.success) {
			alert('Unable to update driver:\n'+result.message);
			return false;
		}
		update();
	});

	$("#table").bind("onResume", function(e) {
		
		updaterStart(update, 5000);
	});

	$("#table").bind("onDelete", function(e,id,row) {
		
		// Get driver of clicked row
		var driverSelection = table.data[row];

		driverDeleteDialog.load(driverSelection, row);
	});

	$("#table").on('click', '.icon-wrench', function() {
		
		// Get driver of clicked row
		var driverSelection = table.data[$(this).attr('row')];

		driverConfigDialog.load(driverSelection, false);
	});

	$("#table").on('click', '.icon-search', function() {
		
		// Get driver of clicked row
		var driverSelection = table.data[$(this).attr('row')];

		deviceScanDialog.load(driverSelection);
	});

	$("#table").on('click', '.icon-plus-sign', function() {
		
		// Do not open dialog if the icon-plus-sign is used on a group header
		if(!$(this).attr('group')) {
			// Get driver of clicked row
			var driverSelection = table.data[$(this).attr('row')];

			deviceAddDialog.load(driverSelection);
		}
	});

	$("#table").on('click', '.device-label', function() {
		
		$('#driver-loader').show();
		
		// Get the ids of the clicked lable
		var ctrlId = $(this).attr('ctrlid');
		var deviceId = $(this).attr('deviceid');
		
		var deviceSelection = device.get(ctrlId, deviceId);
		deviceConfigDialog.load(deviceSelection, true);
		
		$('#driver-loader').hide();
	});

	$("#driver-new").on('click', function () {
		
		driverAddDialog.load(null);
	});
</script>
