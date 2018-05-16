<?php
	global $path;
?>

<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/device/dialog/add.js"></script>

<style>
	#device-add-table td:nth-of-type(1) { width:10%; }
	#device-add-table td:nth-of-type(2) { width:10%; }
</style>

<div id="device-modal-add" class="modal hide keyboard modal-adjust" tabindex="-1" role="dialog" aria-labelledby="device-add-label" aria-hidden="true" data-backdrop="static">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		<h3 id="device-add-label"><?php echo _('New Device'); ?></h3>
	</div>
	<div id="device-add-body" class="modal-body">
		<table id="device-add-table" class="table">
			<tr>
				<th><?php echo _('Driver'); ?></th>
				<th><?php echo _('Name'); ?></th>
				<th><?php echo _('Description'); ?></th>
			</tr>
			<tr>
				<td>
					<label id="device-add-driver" style="padding: 4px 6px; margin-bottom: 10px;"><span style="color:#888"><em><?php echo _('loading...'); ?></em></span></label>
					<select id="device-add-driver-select" class="input-large" style="display:none;"></select>
				</td>
				<td><input id="device-add-name" class="input-medium" type="text"></td>
				<td><input id="device-add-description" class="input-large" type="text" style="width:97%;"></td>
			</tr>
		</table>
		
		<p id="device-add-info" style="display:none;"></p>
		
		<div id="device-add-container" style="display:none;"></div>
	</div>
	<div class="modal-footer">
		<button class="btn" data-dismiss="modal" aria-hidden="true"><?php echo _('Cancel'); ?></button>
		<button id="device-add-save" class="btn btn-primary"><?php echo _('Save'); ?></button>
	</div>
	<div id="device-add-loader" class="ajax-loader" style="display:none"></div>
</div>

<script>
	$('#device-add-container').load('<?php echo $path; ?>Modules/muc/Lib/config/config_list.php');

	$(window).resize(function() {
		deviceAddDialog.adjustModal();
	});
</script>