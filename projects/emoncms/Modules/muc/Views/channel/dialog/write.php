<?php
	global $path;
?>

<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/channel/dialog/write.js"></script>

<div id="channel-modal-write" class="modal hide" tabindex="-1" role="dialog" aria-labelledby="channel-write-label" aria-hidden="true" data-backdrop="static">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		<h3 id="channel-write-label"><?php echo _('Write to Channel'); ?></h3>
	</div>
	<div id="channel-write-body" class="modal-body">
		<p style="color:#888"><em><?php echo _("Value to write to the channel, depending on its' defined Value Type"); ?></em></p>
		
		<div id="channel-write-value">
			<span id="value-float">
				<div class="input-prepend">
					<span class="add-on value-select-label">Value</span>
					<input type="text" id="value-input" class="input-medium" placeholder="Type value..." />
				</div>
			</span>
			
			<span id="value-boolean" style="display:none">
				<div class="input-prepend">
					<span class="add-on value-select-label">Enabled</span>
					<button type="button" id="boolean-input" class="btn" style="border-radius: 4px;"><?php echo _('False'); ?></button>
				</div>
			</span>
			
			<span id="value-text" style="display:none">
				<div class="input-prepend">
					<span class="add-on text-select-label">Text</span>
					<input type="text" id="text-input" class="input-large" placeholder="Type text..." />
				</div>
			</span>
		</div>
		<div id="channel-write-loader" class="ajax-loader" style="display:none;"></div>
	</div>
	<div class="modal-footer">
		<button class="btn" data-dismiss="modal" aria-hidden="true"><?php echo _('Cancel'); ?></button>
		<button id="channel-write-confirm" class="btn btn-primary"><?php echo _('Write'); ?></button>
	</div>
</div>
