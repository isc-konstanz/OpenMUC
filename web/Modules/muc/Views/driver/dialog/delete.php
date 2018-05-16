<?php
	global $path;
?>

<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/driver/dialog/delete.js"></script>

<div id="driver-modal-delete" class="modal hide" tabindex="-1" role="dialog" aria-labelledby="driver-delete-label" aria-hidden="true" data-backdrop="static">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		<h3 id="driver-delete-label"></h3>
	</div>
	<div id="driver-delete-body" class="modal-body">
		<p><?php echo _('Deleting a driver is permanent.'); ?>
			<br><br>
			<?php echo _('If this driver is enabled and has devices configured, they will no longer be sampled or listened to. '); ?>
			<?php echo _('All corresponding devices and configurations will be removed, while feeds and all historic data is kept. '); ?>
			<?php echo _('To remove it, delete them manually afterwards.'); ?>
			<br><br>
			<?php echo _('Are you sure you want to proceed?'); ?>
		</p>
		<div id="driver-delete-loader" class="ajax-loader" style="display:none;"></div>
	</div>
	<div class="modal-footer">
		<button class="btn" data-dismiss="modal" aria-hidden="true"><?php echo _('Cancel'); ?></button>
		<button id="driver-delete-confirm" class="btn btn-primary"><?php echo _('Delete permanently'); ?></button>
	</div>
</div>