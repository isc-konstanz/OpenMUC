<?php
	global $path;
?>

<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/channel/dialog/add.js"></script>

<style>
	#channel-add-table td:nth-of-type(1) { width:10%; }
	#channel-add-table td:nth-of-type(2) { width:10%; }
</style>

<div id="channel-modal-add" class="modal hide keyboard modal-adjust" tabindex="-1" role="dialog" aria-labelledby="channel-add-label" aria-hidden="true" data-backdrop="static">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		<h3 id="channel-add-label"><?php echo _('New Channel'); ?></h3>
	</div>
	<div id="channel-add-body" class="modal-body">
		<table id="channel-add-table" class="table">
			<tr>
				<th><?php echo _('Device'); ?></th>
				<th><?php echo _('Key'); ?></th>
				<th><?php echo _('Name'); ?></th>
			</tr>
			<tr>
				<td>
					<label id="channel-add-device-label" style="padding: 4px 6px; margin-bottom: 10px;"><span style="color:#888"><em><?php echo _('loading...'); ?></em></span></label>
					<select id="channel-add-device-select" class="input-large" style="display:none;"></select>
				</td>
				<td><input id="channel-add-name" class="input-medium" type="text"></td>
				<td><input id="channel-add-description" class="input-large" type="text" style="width:97%;"></td>
			</tr>
		</table>
		
		<p id="channel-add-info" style="display:none;"></p>
		
		<div id="channel-add-container"></div>
	</div>
	<div class="modal-footer">
		<button class="btn" data-dismiss="modal" aria-hidden="true"><?php echo _('Cancel'); ?></button>
		<button id="channel-add-save" class="btn btn-primary"><?php echo _('Save'); ?></button>
	</div>
	<div id="channel-add-loader" class="ajax-loader" style="display:none"></div>
</div>

<script>
	$('#channel-add-container').load('<?php echo $path; ?>Modules/muc/Lib/config/config_list.php');

	$(window).resize(function() {
		channelAddDialog.adjustModal();
	});
</script>