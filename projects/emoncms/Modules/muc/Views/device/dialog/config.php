<?php
	global $path;
?>

<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/device/dialog/config.js"></script>

<style>
	#device-config-table td:nth-of-type(1) { width:10%; }
	#device-config-table td:nth-of-type(2) { width:10%; }
</style>

<div id="device-modal-config" class="modal hide keyboard modal-adjust" tabindex="-1" role="dialog" aria-labelledby="device-config-modal" aria-hidden="true" data-backdrop="static">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		<h3 id="device-config-label"></h3>
	</div>
	<div id="device-config-body" class="modal-body">
		<table id="device-config-table" class="table">
			<tr>
				<th><?php echo _('Driver'); ?></th>
				<th><?php echo _('Name'); ?></th>
				<th><?php echo _('Description'); ?></th>
			</tr>
			<tr>
				<td><label id="device-config-driver" style="padding: 4px 6px; margin-bottom: 10px;"></label></td>
				<td><input id="device-config-name" class="input-medium" type="text"></td>
				<td><input id="device-config-description" class="input-large" type="text" style="width:97%;"></td>
			</tr>
		</table>
		
		<p id="device-config-info" style="display:none;"></p>
		
		<div id="device-config-container"></div>
	</div>
	<div class="modal-footer">
		<button class="btn" data-dismiss="modal" aria-hidden="true"><?php echo _('Cancel'); ?></button>
		<button id="device-config-delete" class="btn btn-info" style="display:none;"><?php echo _('Delete'); ?></button>
		<button id="device-config-save" class="btn btn-primary"><?php echo _('Save'); ?></button>
	</div>
	<div id="device-config-loader" class="ajax-loader" style="display:none"></div>
</div>

<script>
	$('#device-config-container').load('<?php echo $path; ?>Modules/muc/Lib/config/config_list.php');

	$(window).resize(function(){
		deviceConfigDialog.adjustModal();
	});
</script>