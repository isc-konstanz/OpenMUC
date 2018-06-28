<?php
	global $path;
?>

<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/ctrl/dialog/add.js"></script>

<div id="ctrl-modal-add" class="modal hide keyboard" tabindex="-1" role="dialog" aria-labelledby="ctrl-add-label" aria-hidden="true" data-backdrop="static">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		<h3 id="ctrl-add-label"><?php echo _('New Controller'); ?></h3>
	</div>
	<div id="ctrl-add-body" class="modal-body">
		<p style="margin-bottom: 18px;">
			<em><?php echo _('Multi Utility Communications (MUC) controller handle the communication protocols to a variety of devices and are the main entry point to configure metering units.'); ?>
			<br><br> 
			<?php echo _('A MUC controller registers several drivers (see the drivers tab) and is needed to configure their parameters.'); ?><br> 
			<?php echo _('Several MUC controllers may be added and configured, but it is recommended to use the local platform, if geographically possible.'); ?></em>
		</p>
		
		<label><?php echo _('Address: '); ?></label>
		<span>
			<select id="ctrl-add-type" class="input-small">
				<option value=HTTP><?php echo _('HTTP'); ?></option>
				<option value=HTTPS><?php echo _('HTTPS'); ?></option>
				<option value=MQTT><?php echo _('MQTT'); ?></option>
			</select>
			<input id="ctrl-add-address" type="text" value="localhost">
		</span>
		<label><?php echo _('Location description: '); ?></label>
		<input id="ctrl-add-description" type="text" value="Local">
		
		<div id="ctrl-add-loader" class="ajax-loader" style="display:none;"></div>
	</div>
	<div class="modal-footer">
		<button class="btn" data-dismiss="modal" aria-hidden="true"><?php echo _('Cancel'); ?></button>
		<button id="ctrl-add-save" class="btn btn-primary"><?php echo _('Save'); ?></button>
	</div>
</div>