<?php
	global $path;
?>

<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/driver/dialog/config.js"></script>

<div id="driver-modal-config" class="modal hide keyboard modal-adjust" tabindex="-1" role="dialog" aria-labelledby="driver-config-modal" aria-hidden="true" data-backdrop="static">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		<h3 id="driver-config-label"></h3>
	</div>
	<div id="driver-config-body" class="modal-body">
		<div class="modal-container">
			<h4 id="driver-config-header"><?php echo _('Driver'); ?></h4>
			<p id="driver-config-description"></p>
			
			<div id="driver-config-container"></div>
		</div>
	</div>
	<div class="modal-footer">
		<button class="btn" data-dismiss="modal" aria-hidden="true"><?php echo _('Cancel'); ?></button>
		<button id="driver-config-delete" class="btn btn-info" style="display:none;"><?php echo _('Delete'); ?></button>
		<button id="driver-config-save" class="btn btn-primary"><?php echo _('Save'); ?></button>
	</div>
	<div id="driver-config-loader" class="ajax-loader" style="display:none"></div>
</div>

<script>
	$('#driver-config-container').load('<?php echo $path; ?>Modules/muc/Lib/config/config_list.php');

	$(window).resize(function(){
		driverConfigDialog.adjustModal();
	});
</script>