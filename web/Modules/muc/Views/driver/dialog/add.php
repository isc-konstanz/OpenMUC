<?php
	global $path;
?>

<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/driver/dialog/add.js"></script>

<div id="driver-modal-add" class="modal hide keyboard modal-adjust" tabindex="-1" role="dialog" aria-labelledby="driver-add-label" aria-hidden="true" data-backdrop="static">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		<h3 id="driver-add-label"><?php echo _('New Driver'); ?></h3>
	</div>
	<div id="driver-add-body" class="modal-body">
		<div id="driver-add-ctrl" style="display:none">
			<label><?php echo _('Controller to register driver for: '); ?></label>
			<select id="driver-add-ctrl-select" class="input-large"></select>
		</div>
		
		<div class="modal-container">
			<h4 id="driver-add-header"><?php echo _('Driver'); ?></h4>
			
			<select id="driver-add-select" class="input-large" style="display:none" disabled></select>
			<p id="driver-add-description"></p>
			
			<div id="driver-add-container"></div>
			
			<div id="driver-add-overlay" class="modal-overlay"></div>
		</div>
	</div>
	<div class="modal-footer">
		<button class="btn" data-dismiss="modal" aria-hidden="true"><?php echo _('Cancel'); ?></button>
		<button id="driver-add-save" class="btn btn-primary"><?php echo _('Save'); ?></button>
	</div>
	<div id="driver-add-loader" class="ajax-loader" style="display:none"></div>
</div>

<script>
	$('#driver-add-container').load('<?php echo $path; ?>Modules/muc/Lib/config/config_list.php');

	$(window).resize(function() {
		driverAddDialog.adjustModal();
	});
</script>