var deviceConfigDialog =
{
	'deviceConfig': null,

	'load': function(device, showDelete) {
		
		this.deviceConfig = device;
		this.deviceType = device.type;
		
		this.draw(showDelete);
		
		// Initialize callbacks
		this.registerEvents();
	},
	
	'draw':function(showDelete) {
		
		$("#device-modal-config").modal('show');
		$('#device-config-label').html('Configure Device: <b>'+this.deviceConfig.id+'</b>');
		if (showDelete) {
			$('#device-config-delete').show();
		}
		else {
			$('#device-config-delete').hide();
		}
		
		this.adjustModal();
		this.clearModal();

		var groups = {
			address: "Device address",
			settings: "Device settings",
			configs: "Configuration"
		};
		config.init($('#device-config-container'), groups);

		$('#device-config-driver').html('<b>'+this.deviceConfig.driver+'</b>').show();
		$('#device-config-name').val(this.deviceConfig.id);
		$('#device-config-description').val(this.deviceConfig.description);
		
		if (!this.drawParameters()) {
			$("#device-modal-config").modal('hide');
		}
	},
	
	'drawParameters':function() {
		
		$('#device-config-loader').show();

		var info = device.info(deviceConfigDialog.deviceConfig.ctrlid, deviceConfigDialog.deviceConfig.driverid);
		if (typeof info.success !== 'undefined' && !info.success) {
			alert('Device info could not be retrieved:\n'+info.message);

			$('#device-config-info').text('').hide();
			$('#device-config-loader').hide();
			
			return false;
		}
		else {
			if (typeof info.description !== 'undefined') {
				$('#device-config-info').html('<span style="color:#888">'+info.description+'</span>').show();
			}
			else {
				$('#device-config-info').text('').hide();
			}
			
			config.load(deviceConfigDialog.deviceConfig, info);
		}
		$('#device-config-loader').hide();
		return true;
	},

	'clearModal':function() {
		$('#device-config-driver').html('<span style="color:#888"><em>loading...</em></span>');
		$('#device-config-info').text('').hide();

		$('#device-config-name').text('');
		$('#device-config-description').text('');

		$("#device-config-template-select").empty();
		$("#device-config-template-description").text('').hide();
		$('#device-config-template-overlay').show();
	},

	'adjustModal':function() {
		
		if ($("#device-modal-config").length) {
			var h = $(window).height() - $("#device-modal-config").position().top - 180;
			$("#device-config-body").height(h);
		}
	},

	'registerEvents':function() {
		
		// Event to scroll to parameter panel at the bottom of the page when editing
		$('#config', '#device-config-container').on('click', '.edit-parameter', function() {
			
			var container = $('#deviceConfigBody');
			container.animate({
				scrollTop: container.scrollTop() + container.height()
			});
		});

		$("#device-config-save").off('click').on('click', function () {
			
			if (config.valid()) {
				$('#device-config-loader').show();
				
				var deviceConfig = {
						'id': $('#device-config-name').val(),
						'description': $('#device-config-description').val()
				};
				if (deviceConfigDialog.deviceConfig['disabled'] != null) {
					deviceConfig['disabled'] = deviceConfigDialog.deviceConfig['disabled'];
				}
				
				// Make sure JSON.stringify gets passed the right object type
				deviceConfig['channels'] = $.extend([], deviceConfigDialog.deviceConfig.channels);
				
				deviceConfig['address'] = config.parseOptions('address');
				deviceConfig['settings'] = config.parseOptions('settings');
				
				deviceConfig['configs'] = $.extend({}, config.getOptions('configs'));
				
				var result = device.update(deviceConfigDialog.deviceConfig.ctrlid, deviceConfigDialog.deviceConfig.id, deviceConfig);
				if (typeof result.success !== 'undefined' && !result.success) {
					$('#device-config-loader').hide();
					alert('Device could not be configured:\n'+result.message);
					return false;
				}
				
				update();
				$('#device-config-loader').hide();
				$('#device-modal-config').modal('hide');
			}
			else {
				alert('Required parameters need to be configured first.');
				return false;
			}
		});

		$("#device-config-delete").off('click').on('click', function () {
			
			$('#device-modal-config').modal('hide');
			
			deviceDeleteDialog.load(deviceConfigDialog.deviceConfig);
		});
	}
}
