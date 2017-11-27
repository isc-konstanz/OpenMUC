<?php
	global $path;
?>

<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/channel/dialog/delete.js"></script>

<div id="channel-modal-delete" class="modal hide" tabindex="-1" role="dialog" aria-labelledby="channel-delete-label" aria-hidden="true" data-backdrop="static">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		<h3 id="channel-delete-label"></h3>
	</div>
	<div id="channel-delete-body" class="modal-body">
		<p><?php echo _('Deleting a channel is permanent.'); ?>
			<br><br>
			<?php echo _('If the representing channel is active and data gets written to an input, it will no longer be able to post data. '); ?>
			<?php echo _('The corresponding input and configurations will be removed, while feeds and all historic data is kept. '); ?>
			<?php echo _('To remove it, delete them manually afterwards.'); ?>
			<br><br>
			<?php echo _('Are you sure you want to proceed?'); ?>
		</p>
		<div id="channel-delete-loader" class="ajax-loader" style="display:none;"></div>
	</div>
	<div class="modal-footer">
		<button class="btn" data-dismiss="modal" aria-hidden="true"><?php echo _('Cancel'); ?></button>
		<button id="channel-delete-confirm" class="btn btn-primary"><?php echo _('Delete permanently'); ?></button>
	</div>
</div>
