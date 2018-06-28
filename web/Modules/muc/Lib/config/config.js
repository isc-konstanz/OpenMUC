var config =
{
	'container': null,
	'groups': {},
	'groupShow': {},

	'options': null,
	'info': null,


	'init':function(parent, groups) {
		if (!parent) {
			alert('Config has to be loaded to valid container');
			return false;
		}
		this.container = parent;
		
		this.options = null;
		this.info = null;

		this.groups = groups;
		this.groupShow = {};
		
		var tables = '';
		for (var group in groups) {
			if (groups.hasOwnProperty(group)) {
				
				var header = '<tr><th id="'+group+'-header" colspan="5"></th></tr>'+
					'<tr id="'+group+'-option-header" style="display:none" colspan="5">'+
					'<th>Option</th><th>Value</th><th colspan="3"></th></tr>';
				
				var warning = '<div id="'+group+'-option-none" class="alert" style="display:none">You have no options configured</div>'
				
				var table = '<table id="'+group+'-table" class="table table-hover table-config">'+header+
					'<tbody id="'+group+'-option-table"></tbody></table>'+
					warning;
				
				tables += '<div class="modal-container">' + table + 
					'<div id="'+group+'-table-overlay" class="modal-overlay"></div></div>'
				
				this.groupShow[group] = false;
			}
		}

		$('#config-table-container', parent).html(tables);
		this.draw();
	},

	'load':function(options, info) {
		if (options == null) options = {};
		this.options = options;

		if (info == null) info = {};
		this.info = info;

		for (var group in this.groups) {
			if (this.groups.hasOwnProperty(group)) {
				
				if (typeof info[group] !== 'undefined') {
					var optionsInfo = info[group]['options']

					// When no options are configured, they were disabled and the overlay will be shown
					var optionsLength = optionsInfo.length;
					if (optionsLength > 0) {

						// If group option is a String, parse it depending on its syntax info
						if (typeof options[group] === 'string' || options[group] instanceof String) {
							this.loadOptions(group);
						}
						
						// Show group, if at least one of the groups options is defined or mandatory
						var show = false;
						for (var i = 0; i < optionsLength; i++) {
							if (optionsInfo[i]['mandatory']) {
								show = true;
								break;
							}
							else if (typeof options[group] !== 'undefined') {
								var key = optionsInfo[i]['key'];
								if (typeof options[group][key] !== 'undefined') {
									show = true;
									break;
								}
							}
						}
						this.groupShow[group] = show;
						$('#'+group+'-table-overlay', config.container).hide();
					}
					else {
						this.groupShow[group] = false;
						$('#'+group+'-table-overlay', config.container).show();
					}
				}
				else {
					this.groupShow[group] = false;
				}
			}
		}
		this.draw();
	},
	
	'loadOptions':function(group) {
		if (typeof this.options[group] !== 'undefined') {
			var options = this.info[group]['options']
			var syntax = this.info[group]['syntax']

			if (!syntax['keyValue']) {
				var optMandatoryCount = 0;
				for (var i = 0; i < options.length; i++) {
					if (options[i]['mandatory']) optMandatoryCount++;
				}
			}
			
			var optList = {};
			var optArr = this.options[group].split(syntax['separator']);
			for (var p = 0, i = 0; i < options.length && p < optArr.length; i++) {
				optInfo = options[i];

				if (syntax['keyValue']) {
					var keyValue = optArr[p].split(syntax['assignment']);
					if (optInfo.key === keyValue[0]) {
						optList[optInfo.key] = keyValue[1];
						p++;
					}
				}
				else {
					if (optInfo['mandatory'] || optArr.length > optMandatoryCount) {
						optList[optInfo.key] = optArr[p];
						p++;
					}
				}
			}
			this.options[group] = optList;
		}
	},
	
	'draw':function() {
		var optionsShow = false;
		var optionsSelect = '';
		for (var group in this.groups) {
			if (this.groups.hasOwnProperty(group)) {
				
				var groupName = this.groups[group];
				var groupSelect = config.drawGroup(group, groupName);
				if (groupSelect.length > 0) {
					optionsSelect += '<optgroup label="'+groupName+'">'+groupSelect;
				}
				if (config.info != null && typeof config.info[group] !== 'undefined'
						&& config.info[group]['options'].length > 0) {
					optionsShow = true;
				}
			}
		}
		if (optionsShow) {
			$("#option-panel", config.container).show();
			
			$("#option-header-add", config.container).show();
			$("#option-header-edit", config.container).hide();
			$('#option-btn-add', config.container).show();
			$('#option-btn-edit', config.container).hide();
			$('#option-add', config.container).prop('disabled', true);
			
			if (optionsSelect.length > 0) {
				optionsSelect += '<option hidden="true" value="">Select an option</option></optgroup>';
				
				$('#option-select', config.container).html(optionsSelect).prop('disabled', false).val('').change();
			}
			else {
				$('#option-select', config.container).find('option').remove().end().prop('disabled', true);
				$('#option-value-boolean', config.container).hide();
				$('#option-value-small', config.container).hide();
				$('#option-value-medium', config.container).hide();
				$('#option-value-large', config.container).hide();
				$('#option-value-select', config.container).hide();
				$('#option-description', config.container).html('').hide();
			}
		}
		else {
			$('#option-panel', config.container).hide();
		}
		
		// Initialize callbacks
		config.registerEvents();
	},
	
	'drawGroup':function(group, header) {
		var select = '';

		$('#'+group+'-header', config.container).html(
				'<i class="toggle-header icon-minus-sign" group="'+group+'" style="cursor:pointer"></i>'+
				'<a class="toggle-header" group="'+group+'" style="cursor:pointer"> '+header+'</a>');

		$('#'+group+'-option-header', config.container).show();
		$('#'+group+'-option-table', config.container).show();

		var table='';
		if (config.info != null && typeof config.info[group] !== 'undefined') {
			var options = config.info[group]['options'];
			
			for (var i = 0; i < options.length; i++) {
				var option = options[i];
				var key = option['key'];
				
				var name = option['name'];
				if (typeof name === 'undefined') {
					name = key;
				}
				if (typeof config.options[group] === 'undefined') {
					config.options[group] = {};
				}
				if (typeof config.options[group][key] === 'undefined' && option['mandatory'] && option['valueDefault']) {
					config.setOption(key, group, option['valueDefault']);
				}
				
				var row = config.drawOption(key, name, group, option);
				
				if (row.length > 0) {
					table += row;
				}
				else {
					select += '<option value='+key+' group='+group+'>'+name+'</option>';
				}
			}
		}

		if (config.groupShow[group]) {
			if (table.length > 0) {
				$('#'+group+'-option-header', config.container).show();
				$('#'+group+'-option-table', config.container).html(table).show();
				$('#'+group+'-option-none', config.container).hide();
			}
			else {
				$('#'+group+'-option-header', config.container).hide();
				$('#'+group+'-option-table', config.container).html('').show();
				$('#'+group+'-option-none', config.container).show();
			}
		}
		else {
			$('#'+group+'-header', config.container).html(
					'<i class="toggle-header icon-plus-sign" group="'+group+'" style="cursor:pointer"></i>'+
					'<a class="toggle-header" group="'+group+'" style="cursor:pointer"> '+header+'</a>');

			$('#'+group+'-option-header', config.container).hide();
			$('#'+group+'-option-table', config.container).hide();
			$('#'+group+'-option-none', config.container).hide();
		}
		
		return select;
	},
	
	'drawOption':function(key, name, group, option) {
		var row = '';
		if (typeof option !== 'undefined') {

			var value = '';
			if (typeof config.options[group][key] !== 'undefined') {

				if (typeof option['valueSelection'] !== 'undefined') {
					value = option.valueSelection[config.options[group][key]];
				}
				else if (option['type'].toUpperCase() == 'BOOLEAN') {
					var val = config.options[group][key];
					if (typeof(val) === 'string') val = (val == 'true');
					if (val) {
						value = '<span style="color:#5bb75b">Enabled</span>';
					}
					else {
						value = '<span style="color:#888">Disabled</span>';
					}
				}
				else {
					value += config.options[group][key];
				}
				
				if (typeof option['valueDefault'] !== 'undefined' && 
						option['valueDefault'] == config.options[group][key]) {
					value = '<span style="color:#006dcc"><i>Default: </i></span>' + value;
				}
			}

			var comment = '';
			if (option['mandatory']) {
				if (value.length === 0) {
					value = '<span style="color:#b94a48"><i>Empty</i></span>';
				}
				comment = '<span style="color:#888; font-size:12px"><em>mandatory</em></span>';
			}
			
			if (typeof value !== 'undefined' && value.length > 0) {
				row += '<tr>';
				row += '<td>'+name+'</td><td>'+value+'</td><td>'+comment+'</td>';
			 
				// Edit and delete buttons (icon)
				row += '<td><a class="edit-option" title="Edit" key='+key+' group='+group+'><i class="icon-pencil" style="cursor:pointer"></i></a></td>';
				if (option['mandatory']) {
					row += '<td><i class="icon-trash" style="cursor:pointer; opacity:0.33" disabled></i></td>';
				}
				else {
					row += '<td><a class="delete-option" title="Delete" key='+key+' group='+group+'><i class="icon-trash" style="cursor:pointer"></i></a></td>';
				}
				row += '</tr>';
			}
		}
		return row;
	},

	'registerEvents':function() {
		$('#config', config.container).off();

		// Event: minimise or maximise settings
		$('#config', config.container).on('click touchend', '.toggle-header', function(e) {
			e.stopPropagation();
			e.preventDefault();
			var $me=$(this);
			if ($me.data('clicked')) {
				$me.data('clicked', false); // reset
				if ($me.data('alreadyclickedTimeout')) clearTimeout($me.data('alreadyclickedTimeout')); // prevent this from happening

				// Do what needs to happen on double click. 
				var group = $(this).attr('group');
				var state = config.groupShow[group];
				config.groupShow[group] = !state;
				config.draw();
				
			}
			else {
				$me.data('clicked', true);
				var alreadyclickedTimeout=setTimeout(function() {
					$me.data('clicked', false); // reset when it happens
	
					// Do what needs to happen on single click. Use $me instead of $(this) because $(this) is  no longer the element
					var group = $me.attr('group');
					var state = config.groupShow[group];
					config.groupShow[group] = !state;
					config.draw();
				
				},250); // dblclick tolerance
				$me.data('alreadyclickedTimeout', alreadyclickedTimeout); // store this id to clear if necessary
			}
		});

		$('#config', config.container).on('click', '.edit-option', function() {
			var key = $(this).attr('key');
			var group = $(this).attr('group');

			$('#option-header-add', config.container).hide();
			$('#option-header-edit', config.container).show();
			$('#option-btn-add', config.container).hide();
			$('#option-btn-edit', config.container).show();

			var name = config.getInfo(key, group)['name'];
			if (!name) {
				name = key;
			}
			
			$('#option-select', config.container).append('<option hidden="true" value="'+key+'" group="'+group+'">'+name+'</option>').val(key)
			$('#option-select', config.container).change(); // Force a refresh
			$('#option-select', config.container).prop('disabled', true);
		});

		$('#config', config.container).on('click', '.delete-option', function() {
			var key = $(this).attr('key');
			var group = $(this).attr('group');
			delete config.options[group][key];

			config.draw();
		});

		$('#config #option-select', config.container).off('change').on('change', function() {
			$('#option-value-boolean', config.container).hide();
			$('#option-value-small', config.container).hide();
			$('#option-value-medium', config.container).hide();
			$('#option-value-large', config.container).hide();
			$('#option-value-select', config.container).hide();
			$('#option-description', config.container).html('').hide();
			
			var key = this.value;
			if (key.length > 0) {
				var group = $('option:selected', this).attr('group');
				
				var option = config.getInfo(key, group);
				if (option != null) {
					var value = config.getOption(key, group);
					
					if (value == null && option['valueDefault']) {
						value = option['valueDefault'];
					}
					
					if (value == null) value = '';
					
					// Check Option Type
					var type = option['type'].toUpperCase();
					if (typeof option['valueSelection'] !== 'undefined') {
						$('#option-value-select', config.container).show();
						
						var selection = '<option hidden="true" value="">Select</option>';
						for (var selectValue in option.valueSelection) {
							selection += '<option value='+selectValue+'>'+option.valueSelection[selectValue]+'</option>';
						}
						$('#option-value-select-input', config.container).html(selection).val(value).focus().select();
					}
					else if (type == 'BOOLEAN') {
						$('#option-value-boolean', config.container).show();
						
						var button = $('#option-value-boolean-input', config.container);
						if (typeof(value) === 'string') value = (value == 'true');
						if (value) {
							button.addClass('btn-success');
							button.text('True');
						}
						else {
							button.removeClass('btn-success');
							button.text('False');
						}
					}
					else if (type == 'INTEGER' || type == 'SHORT' || type == 'BYTE') {
						$('#option-value-small', config.container).show();
						$('#option-value-small-input', config.container).val(value).focus().select();
					}
					else if (type == 'DOUBLE' || type == 'FLOAT' || type == 'LONG') {
						$('#option-value-medium', config.container).show();
						$('#option-value-medium-input', config.container).val(value).focus().select();
					}
					else {
						$('#option-value-large', config.container).show();
						$('#option-value-large-input', config.container).val(value).focus().select();
					}

					if (typeof option['description'] !== 'undefined' && option['description'].length > 0) {
						$('#option-description', config.container).html(option['description']);
					}
					else {
						$('#option-description', config.container).html('<b style="color: orange">No option description available for option "'+key+'".');
					}
					$('#option-add', config.container).prop('disabled', false);
					$('#option-description', config.container).show();
				}
			}
		});

		$('#config #option-add, #config #option-edit', config.container).off('click').on('click', function() {
			var key = $('#option-select', config.container).val();
			var group = $('#option-select :selected', config.container).attr('group');
			
			var option = config.getInfo(key, group);
			if (option != null) {
				var value = '';

				// Check Option Type
				var type = option['type'].toUpperCase();
				if (typeof option['valueSelection'] !== 'undefined') {
					value = $("#option-value-select-input", config.container).val();
				}
				else if (type == 'BOOLEAN') {
					value = $("#option-value-boolean-input", config.container).text().toLowerCase();
				}
				else if (type == 'INTEGER' || type == 'SHORT' || type == 'BYTE') {
					value = $("#option-value-small-input", config.container).val();
					if (isNaN(value)) {
						alert('Value must be a valid number');
						return false;
					}
				}
				else if (type == 'DOUBLE' || type == 'FLOAT' || type == 'LONG') {
					value = $("#option-value-medium-input", config.container).val();
					value = parseFloat(value.replace(",", "."));
					if (isNaN(value)) {
						alert('Value must be a valid number');
						return false;
					}
				}
				else {
					value = $("#option-value-large-input", config.container).val();
				}
				config.setOption(key, group, ''+value);
				config.groupShow[group] = true;
				
				config.draw();
			}
		});

		$('#config #option-cancel', config.container).off('click').on('click', function() {
			$('#option-header-add', config.container).show();
			$('#option-header-edit', config.container).hide();
			
			$('#option-btn-edit', config.container).hide();
			$('#option-btn-add', config.container).show();
			$('#option-value-boolean', config.container).hide();
			$('#option-value-small', config.container).hide();
			$('#option-value-medium', config.container).hide();
			$('#option-value-large', config.container).hide();
			$('#option-value-select', config.container).hide();
			$('#option-description', config.container).text('').hide();

			config.draw();
		});
		
		$('#config #option-value-boolean-input', config.container).off('click').on('click', function() {
			if ($(this).text() == 'False') {
				$(this).addClass('btn-success');
				$(this).text('True');
			}
			else {
				$(this).removeClass('btn-success');
				$(this).text('False');
			}
		});
	},
	
	'getInfo':function(key, group) {
		if (config.info.hasOwnProperty(group)) {
			var options = config.info[group]['options'];
			
			for (var i = 0; i < options.length; i++) {
				if (options[i]['key'] === key) {
					return options[i];
				}
			}
		}
		return null;
	},
	
	'getOption':function(key, group) {
		if (typeof config.options[group] !== 'undefined' && 
				typeof config.options[group][key] !== 'undefined') {

			return config.options[group][key];
		}
		return null;
	},
	
	'setOption':function(key, group, value) {
		if (typeof config.options[group] === 'undefined' || config.options[group] === '') {
			config.options[group] = {};
		}
		config.options[group][key] = value;
	},

	'getOptions':function(group) {
		if (typeof config.options[group] !== 'undefined') {
			return config.options[group];
		}
		return null;
	},

	'parseOptions':function(group) {
		if (typeof config.options[group] !== 'undefined' && config.options[group] !== '' &&
				typeof config.info[group] !== 'undefined') {
			var optArr = [];
			// Add options in the defined order of the information
			var options = config.info[group]['options'];
			var syntax = config.info[group]['syntax'];
			
			for (var p = 0, i = 0; i < options.length; i++) {
				optInfo = options[i];
				
				if (config.options[group].hasOwnProperty(optInfo.key)) {
					var value = config.options[group][optInfo.key];
					if (syntax['keyValue']) {
						optArr.push(optInfo.key+syntax['assignment']+value);
					}
					else {
						optArr.push(value);
					}
				}
			}
			return optArr.join(syntax['separator']);
		}
		return null;
	},

	'valid':function() {
		for (var group in config.groups) {
			if (config.groups.hasOwnProperty(group)) {
				if (typeof config.info[group] !== 'undefined') {
					var options = config.info[group]['options'];
					
					for (var i in options) {
						if (options.hasOwnProperty(i)) {
							var option = options[i];
							var key = option['key'];
							if (option['mandatory']) {
								if (!(key in config.options[group]) || config.options[group][key].length === 0) {
									return false;
								}
							}
						}
					}
				}
			}
		}
		return true;
	}
}