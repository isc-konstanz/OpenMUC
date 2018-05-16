<?php
	global $path;
?>

<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/channel/dialog/scan.js"></script>

<style>
	#channel-scan-results-table td:nth-of-type(1) { width:14px; text-align: center; }
	#channel-scan-results-table td:nth-of-type(2) { width:25%; }
	#channel-scan-results-table td:nth-of-type(5) { width:5%; }
</style>

<div id="channel-modal-scan" class="modal hide keyboard modal-adjust" tabindex="-1" role="dialog" aria-labelledby="channel-scan-label" aria-hidden="true" data-backdrop="static">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		<h3 id="channel-scan-label"><?php echo _('Scan Channels'); ?></h3>
	</div>
	<div id="channel-scan-body" class="modal-body">
		<div>
			<label id="channel-scan-device-header"><?php echo _('Device to search channels for: '); ?></label>
			<select id="channel-scan-device-select" class="input-large"></select>
		</div>
		<p id="channel-scan-device-description"></p>
		
		<div id="channel-scan-container"></div>
		
		<table id="channel-scan-results-table" class="table table-hover" style="display:none">
			<tr id="channel-scan-results-header">
				<th colspan="1"></th>
				<th><?php echo _('Description'); ?></th>
				<th><?php echo _('Address'); ?></th>
				<th><?php echo _('Settings'); ?></th>
				<th><?php echo _('Type'); ?></th>
				<th><?php echo _('Meta'); ?></th>
			</tr>
			<tbody id="channel-scan-results"></tbody>
		</table>
		<div id="channel-scan-results-none" class="alert" style="display:none"><?php echo _('No channels found'); ?></div>
	</div>
	<div class="modal-footer">
		<button class="btn" data-dismiss="modal" aria-hidden="true"><?php echo _('Cancel'); ?></button>
		<button id="channel-scan-start" class="btn btn-primary" style="border-radius: 4px;"><?php echo _('Scan'); ?></button>
	</div>
	<div id="channel-scan-loader" class="ajax-loader" style="display:none"></div>
</div>

<script>
	$('#channel-scan-container').load('<?php echo $path; ?>Modules/muc/Lib/config/config_list.php');

	$(window).resize(function() {
		channelScanDialog.adjustModal()
	});
</script>