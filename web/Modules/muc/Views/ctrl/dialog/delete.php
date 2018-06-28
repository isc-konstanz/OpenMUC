<?php
	global $path;
?>

<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/ctrl/dialog/delete.js"></script>

<div id="ctrl-modal-delete" class="modal hide" tabindex="-1" role="dialog" aria-labelledby="ctrl-delete-label" aria-hidden="true" data-backdrop="static">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		<h3 id="ctrl-modal-label"><?php echo _('Delete Controller'); ?></h3>
	</div>
	<div id="ctrl-delete-body" class="modal-body">
		<p><?php echo _('Deleting a Multi Utility Communication controller is permanent.'); ?>
			<br><br>
			<?php echo _('If this MUC controller is active and is registered, it will no longer be able to retrieve the configuration. '); ?>
			<?php echo _('All corresponding drivers and configurations will be removed, while feeds and all historic data is kept. '); ?>
			<?php echo _('To remove it, delete them manually afterwards.'); ?>
			<br><br>
			<?php echo _('Are you sure you want to proceed?'); ?>
		</p>
		<div id="ctrl-delete-loader" class="ajax-loader" style="display:none;"></div>
	</div>
	<div class="modal-footer">
		<button class="btn" data-dismiss="modal" aria-hidden="true"><?php echo _('Cancel'); ?></button>
		<button id="ctrl-delete-confirm" class="btn btn-primary"><?php echo _('Delete permanently'); ?></button>
	</div>
</div>