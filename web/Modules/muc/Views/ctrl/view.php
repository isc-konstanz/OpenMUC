<?php
	global $path;
?>

<link href="<?php echo $path; ?>Modules/muc/Lib/style.css" rel="stylesheet">
<script type="text/javascript" src="<?php echo $path; ?>Lib/tablejs/table.js"></script>
<script type="text/javascript" src="<?php echo $path; ?>Lib/tablejs/custom-table-fields.js"></script>
<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Lib/muc-table-fields.js"></script>
<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Lib/config/config.js"></script>
<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/ctrl/ctrl.js"></script>
<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/driver/driver.js"></script>

<style>
	#table input[type="text"] {
		width: 88%;
	}
	#table td:nth-of-type(1) { width:5%;}
	#table td:nth-of-type(2) { width:20%;}
	#table td:nth-of-type(3) { width:10%;}
	#table th:nth-of-type(4) { font-weight:normal; }
	#table td:nth-of-type(5), th:nth-of-type(5) { width:20%; text-align: right; }
	#table td:nth-of-type(6) { width:14px; text-align: center; }
	#table td:nth-of-type(7) { width:14px; text-align: center; }
	#table td:nth-of-type(8) { width:14px; text-align: center; }
</style>

<div>
	<div id="api-help-header" style="float:right;"><a href="api"><?php echo _('MUC API Help'); ?></a></div>
	<div id="local-header"><h2><?php echo _('Controller'); ?></h2></div>
	
	<div id="table"><div align='center'></div></div>
	
	<div id="ctrl-none" class="alert alert-block hide">
		<h4 class="alert-heading"><?php echo _('No Controllers configured'); ?></h4>
			<p>
				<?php echo _('Multi Utility Communication (MUC) controller handle the communication protocols to a variety of devices and are the main entry point to configure metering units.'); ?>
				<br><br>
				<?php echo _('A MUC controller registers several drivers (see the drivers tab) and is needed to configure the communication protocol they implement.'); ?>
				<br>
				<?php echo _('Several MUC controllers may be added, but it is recommended to use the local platform, if geographically possible.'); ?>
				<br>
				<?php echo _('You may want the next link as a guide for generating your request: '); ?><a href="api"><?php echo _('MUC API helper'); ?></a>
			</p>
	</div>
	
	<div id="toolbar-bottom"><hr>
		<a id="device-return" class="btn btn-small" href="<?php echo $path; ?>muc/device/view"><i class="icon-chevron-left" ></i>&nbsp;<?php echo _('Devices'); ?></a>
		<a id="driver-show" class="btn btn-small" href="<?php echo $path; ?>muc/driver/view"><i class="icon-chevron-right" ></i>&nbsp;<?php echo _('Drivers'); ?></a>
		<button id="ctrl-new" class="btn btn-primary btn-small"><i class="icon-plus-sign icon-white" ></i>&nbsp;<?php echo _('New controller'); ?></button>
	</div>
	
	<div id="ctrl-loader" class="ajax-loader"></div>
</div>

<?php require "Modules/muc/Views/ctrl/dialog/add.php"; ?>
<?php require "Modules/muc/Views/ctrl/dialog/delete.php"; ?>

<?php require "Modules/muc/Views/driver/dialog/add.php"; ?>
<?php require "Modules/muc/Views/driver/dialog/config.php"; ?>
<?php require "Modules/muc/Views/driver/dialog/delete.php"; ?>

<script>
	var path = "<?php echo $path; ?>";

	var types = {
		HTTP: "HTTP",
		HTTPS: "HTTPS",
		MQTT: "MQTT"
	};
	
	// Extend table library field types
	for (z in muctablefields) table.fieldtypes[z] = muctablefields[z];
	for (z in customtablefields) table.fieldtypes[z] = customtablefields[z];
	table.element = "#table";
	table.deletedata = false;
	table.fields = {
		'type':{'title':'<?php echo _("Type"); ?>','type':"select",'options':types},
		'address':{'title':'<?php echo _("Address"); ?>','type':"text"},
		'description':{'title':'<?php echo _('Location'); ?>','type':"text"},
		'drivers':{'title':'<?php echo _("Drivers"); ?>','type':"driverlist"},
		'password':{'title':'<?php echo _('Password'); ?>','type':"text"},
		// Actions
		'add-action':{'title':'', 'type':"icon-enabled", 'icon':'icon-plus-sign'},
		'edit-action':{'title':'', 'type':"edit"},
		'delete-action':{'title':'', 'type':"delete"}
	}

	update();

	function update() {
		
		var requestTime = (new Date()).getTime();
		$.ajax({ url: path+"muc/controller/list.json", dataType: 'json', async: true, success: function(data, textStatus, xhr) {
			table.timeServerLocalOffset = requestTime-(new Date(xhr.getResponseHeader('Date'))).getTime(); // Offset in ms from local to server time
			table.data = data;

			table.draw();
			if (table.data.length == 0) {
				$("#ctrl-none").show();
				$("#local-header").hide();
			} else {
				$("#ctrl-none").hide();
				$("#local-header").show();
			}
			$('#ctrl-loader').hide();
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

	$("#table").bind("onSave", function(e,id,fields_to_update) {
		
		$('#ctrl-loader').show();
		
		var result = muc.set(id,fields_to_update);
		update();
		
		$('#ctrl-loader').hide();

		if (!result.success) {
			alert('Unable to update muc:\n'+result.message);
			return false;
		}
	});

	$("#table").bind("onResume", function(e) {
		
		updaterStart(update, 5000);
	});

	$("#table").bind("onDelete", function(e,id,row) {
		
		ctrlDeleteDialog.load(id, row);
	});

	$("#table").on('click', '.icon-plus-sign', function() {
		// Get MUC of clicked row
		var ctrlSelection = table.data[$(this).attr('row')];
		
		driverAddDialog.load(ctrlSelection);
	});

	$("#table").on('click', '.driver-label', function() {
		$('#ctrl-loader').show();
		
		// Get the ids of the clicked lable
		var ctrlId = $(this).attr('ctrlid');
		var driverId = $(this).attr('driverid');
		
		var driverSelection = driver.get(ctrlId, driverId);
		driverConfigDialog.load(driverSelection, true);

		$('#ctrl-loader').hide();
	});

	$('#ctrl-new').on('click', function() {
		
		ctrlAddDialog.load();
	});
</script>
