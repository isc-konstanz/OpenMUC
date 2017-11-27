<?php
	global $path;
?>

<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/channel/dialog/config.js"></script>

<style>
	#channel-config-table td:nth-of-type(1) { width:10%; }
	#channel-config-table td:nth-of-type(2) { width:5%; }
	#channel-config-table td:nth-of-type(3) { width:5%; }
	#channel-config-table td:nth-of-type(4) { width:10%; }
</style>

<div id="channel-modal-config" class="modal hide keyboard modal-adjust" tabindex="-1" role="dialog" aria-labelledby="channel-config-modal" aria-hidden="true" data-backdrop="static">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		<h3 id="channel-config-label"></h3>
	</div>
	<div id="channel-config-body" class="modal-body">
		<table id="channel-config-table" class="table">
			<tr>
				<th><?php echo _('Device'); ?></th>
				<th><?php echo _('Authorization'); ?></th>
				<th><?php echo _('Node'); ?></th>
				<th><?php echo _('Key'); ?></th>
				<th><?php echo _('Name'); ?></th>
			</tr>
			<tr>
				<td><label id="channel-config-device" style="padding: 4px 6px; margin-bottom: 10px;"></label></td>
            	<td>
                	<select id="channel-config-auth" class="input-medium" style="width: 105px;">
                        <option value=DEFAULT><?php echo _('Default'); ?></option>
                        <option value=DEVICE><?php echo _('Device'); ?></option>
                        <option value=WRITE><?php echo _('Write'); ?></option>
                    	<option value=READ><?php echo _('Read'); ?></option>
                        <option value=NONE><?php echo _('None'); ?></option>
                	</select>
                </td>
				<td>
					<label id="channel-config-node-label" style="padding: 4px 6px; margin-bottom: 10px; display:none;"></label>
					<input id="channel-config-node" class="input-medium" type="text">
				</td>
				<td><input id="channel-config-name" class="input-medium" type="text"></td>
				<td><input id="channel-config-description" class="input-large" type="text"></td>
			</tr>
		</table>
		
		<p id="channel-config-info" style="display:none;"></p>
		
		<div id="channel-config-container"></div>
	</div>
	<div class="modal-footer">
		<button class="btn" data-dismiss="modal" aria-hidden="true"><?php echo _('Cancel'); ?></button>
		<button id="channel-config-write" class="btn btn-warning" style="display:none;"><?php echo _('Write'); ?></button>
		<button id="channel-config-delete" class="btn btn-info" style="display:none;"><?php echo _('Delete'); ?></button>
		<button id="channel-config-save" class="btn btn-primary"><?php echo _('Save'); ?></button>
	</div>
	<div id="channel-config-loader" class="ajax-loader" style="display:none"></div>
</div>

<script>
	$('#channel-config-container').load('<?php echo $path; ?>Modules/muc/Lib/config/config_list.php');

	$(window).resize(function(){
		channelConfigDialog.adjustModal();
	});
</script>