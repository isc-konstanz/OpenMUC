<style>
	.table-config td:nth-of-type(1) { width:15%;}
	.table-config td:nth-of-type(3) { text-align: right; }
	.table-config td:nth-of-type(4) { width:14px; text-align: center; }
	.table-config td:nth-of-type(5) { width:14px; text-align: center; }
</style>

<div id="config">
	<div id="config-table-container"></div>
	
	<div id="option-panel" style="display:none">
		<h4 id="option-header-add" style="display:none"><?php echo _('Add option'); ?>:</h4>
		<h4 id="option-header-edit" style="display:none"><?php echo _('Edit option'); ?>:</h4>
		
		<select id="option-select" class="input-large" disabled></select>
		
		<span id="option-value-boolean" style="display:none">
			<div class="input-prepend">
				<span class="add-on value-select-label"><?php echo _('Value'); ?></span>
				<button type="button" id="option-value-boolean-input" class="btn" style="border-radius: 4px;"><?php echo _('False'); ?></button>
			</div>
		</span>
		
		<span id="option-value-small" style="display:none">
			<div class="input-prepend">
				<span class="add-on value-select-label"><?php echo _('Value'); ?></span>
				<input type="text" id="option-value-small-input" class="input-small" placeholder="Type value..." />
			</div>
		</span>
		
		<span id="option-value-medium" style="display:none">
			<div class="input-prepend">
				<span class="add-on value-select-label"><?php echo _('Value'); ?></span>
				<input type="text" id="option-value-medium-input" class="input-medium" placeholder="Type value..." />
			</div>
		</span>
		
		<span id="option-value-large" style="display:none">
			<div class="input-prepend">
				<span class="add-on text-select-label"><?php echo _('Value'); ?></span>
				<input type="text" id="option-value-large-input" class="input-large" placeholder="Type value..." />
			</div>
		</span>
		
		<span id="option-value-select" style="display:none">
			<div class="input-prepend">
				<span class="add-on text-select-label"><?php echo _('Value'); ?></span>
				<select id="option-value-select-input" class="input-medium"></select>
			</div>
		</span>
		
		<span id="option-btn-add">
			<div class="input-prepend">
				<button id="option-add" class="btn btn-info" style="border-radius: 4px;" disabled><?php echo _('Add'); ?></button>
			</div>
		</span>
		
		<span id="option-btn-edit" style="display:none">
			<div class="input-prepend">
				<button id="option-edit" class="btn btn-info" style="border-radius: 4px;"><?php echo _('Edit'); ?></button>
				<button id="option-cancel" class="btn" style="border-radius: 4px;"><?php echo _('Cancel'); ?></button>
			</div>
		</span>
		
		<div id="option-description" class="alert alert-info" style="display:none"></div>
	</div>
</div>