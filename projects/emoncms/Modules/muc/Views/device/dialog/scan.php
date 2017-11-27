<?php
	global $path;
?>

<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/device/dialog/scan.js"></script>

<style>
	#device-scan-results-table td:nth-of-type(1) { width:14px; text-align: center; }
	#device-scan-results-table td:nth-of-type(2) { width:25%; }
</style>

<div id="device-modal-scan" class="modal hide keyboard modal-adjust" tabindex="-1" role="dialog" aria-labelledby="device-scan-label" aria-hidden="true" data-backdrop="static">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		<h3 id="device-scan-label"><?php echo _('Scan Devices'); ?></h3>
	</div>
	<div id="device-scan-body" class="modal-body">
		<div>
			<label id="device-scan-driver-header"><?php echo _('Driver to search devices for: '); ?></label>
			<select id="device-scan-driver-select" class="input-large"></select>
		</div>
		<p id="device-scan-driver-description"></p>
		
		<div id="device-scan-container"></div>
		
		<table id="device-scan-results-table" class="table table-hover" style="display:none">
			<tr id="device-scan-results-header">
				<th colspan="1"></th>
				<th><?php echo _('Description'); ?></th>
				<th><?php echo _('Address'); ?></th>
				<th><?php echo _('Settings'); ?></th>
			</tr>
			<tbody id="device-scan-results"></tbody>
		</table>
		<div id="device-scan-results-none" class="alert" style="display:none"><?php echo _('No devices found'); ?></div>
	</div>
	<div class="modal-footer">
		<button class="btn" data-dismiss="modal" aria-hidden="true"><?php echo _('Cancel'); ?></button>
		<button id="device-scan-start" class="btn btn-primary" style="border-radius: 4px;"><?php echo _('Scan'); ?></button>
	</div>
	<div id="device-scan-loader" class="ajax-loader" style="display:none"></div>
</div>

<script>
	$('#device-scan-container').load('<?php echo $path; ?>Modules/muc/Lib/config/config_list.php');

	$(window).resize(function() {
		deviceScanDialog.adjustModal();
	});
</script>