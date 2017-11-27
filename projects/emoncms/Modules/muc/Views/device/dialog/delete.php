<?php
	global $path;
?>

<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/device/dialog/delete.js"></script>

<div id="device-modal-delete" class="modal hide" tabindex="-1" role="dialog" aria-labelledby="device-delete-label" aria-hidden="true" data-backdrop="static">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		<h3 id="device-delete-label"></h3>
	</div>
	<div id="device-delete-body" class="modal-body">
		<p><?php echo _('Deleting a device is permanent.'); ?>
			<br><br>
			<?php echo _('If the represented device is active and is using a device key, it will no longer be able to post data. '); ?>
			<?php echo _('All corresponding channels and configurations will be removed, while inputs, feeds and all historic data is kept. '); ?>
			<?php echo _('To remove it, delete them manually afterwards.'); ?>
			<br><br>
			<?php echo _('Are you sure you want to proceed?'); ?>
		</p>
		<div id="device-delete-loader" class="ajax-loader" style="display:none;"></div>
	</div>
	<div class="modal-footer">
		<button class="btn" data-dismiss="modal" aria-hidden="true"><?php echo _('Cancel'); ?></button>
		<button id="device-delete-confirm" class="btn btn-primary"><?php echo _('Delete permanently'); ?></button>
	</div>
</div>