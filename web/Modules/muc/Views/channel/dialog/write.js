var channelWriteDialog =
{
	'ctrlId': null,
	'channelId': null,
	'valueType': null,
	'value': null,

	'load': function(channel) {
		
		this.ctrlId = channel.ctrlid;
		this.channelId = channel.id;
		
		if (typeof channel.configs !== 'undefined' && 
				typeof channel.configs.valueType !== 'undefined') {
			
			this.valueType = channel.configs.valueType;
		}
		else {
			this.valueType = 'DOUBLE';
		}
		if (typeof channel.value !== 'undefined') {
			this.value = channel.value
		}
		else {
			this.value = '';
		}
		
		this.draw();
		
		// Initialize callbacks
		this.registerEvents();
	},

	'draw':function() {

		$('#channel-modal-write').modal('show');
		this.clearModal();
		
	},

	'clearModal':function() {
		
		$('#channel-write-label').html('Write to Channel: <b>'+this.channelId+'</b>');
		
		// Check Value Type
		switch(this.valueType.toLowerCase()) {
		case 'string': // STRING
			$('#value-float', '#channel-write-value').hide();
			$('#value-boolean', '#channel-write-value').hide();
			$('#value-text', '#channel-write-value').show();
			$('#text-input', '#channel-write-value').val(this.value);
			break;
		case 'boolean': // BOOLEAN
			$('#value-float', '#channel-write-value').hide();
			$('#value-text', '#channel-write-value').hide();
			$('#value-boolean', '#channel-write-value').show();
			var toggle = $('#boolean-input', '#channel-write-value');
			if (typeof this.value !== 'undefined' && String(this.value).toLowerCase() == 'true') {
				toggle.addClass('btn-success');
				toggle.text('True');
			}
			else {
				toggle.removeClass('btn-success');
				toggle.text('False');
			}
			break;
		default: // DOUBLE
			$('#value-boolean', '#channel-write-value').hide();
			$('#value-text', '#channel-write-value').hide();
			$('#value-float', '#channel-write-value').show();
			$('#value-input', '#channel-write-value').val(this.value);
			break;
		}
		
		$('#channel-write-loader').hide();
	},

	'registerEvents':function() {
		
		$('#boolean-input', '#channel-write-value').off('click').on('click', function() {
			if ($(this).text() == 'False') {
				$(this).addClass('btn-success');
				$(this).text('True');
			}
			else {
				$(this).removeClass('btn-success');
				$(this).text('False');
			}
		});
		
		$("#channel-write-confirm").off('click').on('click', function() {
			$('#channel-write-loader').show();
			
			var valueType = channelWriteDialog.valueType.toLowerCase();
			var value = '';
			// Check Value Type
			switch(valueType) {
			case 'string': // STRING
				value = $("#text-input", '#channel-write-value').val();
				break;
			case 'boolean': // BOOLEAN
				value = $("#boolean-input", '#channel-write-value').text().toLowerCase();
				break;
			default: // DOUBLE
				value = $("#value-input", '#channel-write-value').val();
				value = parseFloat(value.replace(",", "."));
				if (isNaN(value)) {
					$('#channel-write-loader').hide();
					alert('Value must be a valid number');
					return false;
				}
				break;
			}
			var result = channel.write(channelWriteDialog.ctrlId, channelWriteDialog.channelId, value, valueType);
			$('#channel-write-loader').hide();

			if (typeof result.success !== 'undefined' && !result.success) {
				alert('Unable to write to channel:\n'+result.message);
				return false;
			}

			$('#channel-modal-write').modal('hide');
		});
	}
}
