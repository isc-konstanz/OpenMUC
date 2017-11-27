var deviceAddDialog =
{
	'ctrlId': null,
	'driverId': null,
	'deviceConfig': null,

	'load':function(driver) {

		if (driver != null) {
			this.ctrlId = driver.ctrlid;
			this.driverId = driver.id;
			this.deviceConfig = {
					'ctrlid' : driver.ctrlid, 
					'driverid' : driver.id, 
					'driver' : driver.name
			};
		}
		else {
			this.ctrlId = null;
			this.driverId = null;
			this.deviceConfig = {};
		}
		
		this.draw();
		
		// Initialize callbacks
		this.registerEvents();
	},

	'loadDevice':function(device) {

		this.ctrlId = device.ctrlid;
		this.driverId = device.driverid;
		this.deviceConfig = device;
		
		this.draw();
		
		// Initialize callbacks
		this.registerEvents();
	},

	'draw':function() {
		
		$("#device-modal-add").modal('show');
		this.adjustModal();
		this.clearModal();

		var groups = {
			address: "Device address",
			settings: "Device settings",
			configs: "Configuration"
		};
		config.init($('#device-add-container'), groups);
		
		if (this.driverId != null) {
			$("#device-add-container").show();
			$('#device-add-driver').html('<b>'+this.deviceConfig.driver+'</b>').show();
			
			if (!this.drawParameters()) {
				$("#deviceConfigModal").modal('hide');
				return false;
			}
		}
		else {
			$("#device-add-container").show();
			
			$.ajax({ url: path+"muc/controller/list.json", dataType: 'json', async: true, success: function(data, textStatus, xhr) {
				if (data.length > 0) {
					$("#device-add-driver").hide();
					
					// Append drivers from database to select
					var driverSelect = $("#device-add-driver-select").show();
					driverSelect.append("<option selected hidden='true' value=''>Select a driver</option>");
								
					$.each(data, function() {
						var ctrl = this;
						$.ajax({ url: path+"muc/driver/available.json", data: "ctrlid="+ctrl.id, dataType: 'json', async: false, success: function(result, textStatus, xhr) {
							if (result.length > 0) {
								if (deviceAddDialog.ctrlId <= 0) {
									driverSelect.append('<optgroup label="'+ctrl.description+'">');
								}

								$.each(result, function() {
									var driverName;
									if (typeof this.name !== 'undefined') {
										driverName = this.name;
									}
									else {
										driverName = this.id;
									}
									driverSelect.append('<option value="'+this.id+'" ctrlid="'+ctrl.id+'">'+driverName+'</option>');
								});
							}
						}});
					});
				}
			}});
		}
	},

	'drawParameters':function() {

		$('#device-add-loader').show();

		var info = device.info(deviceAddDialog.ctrlId, deviceAddDialog.driverId);
		if (typeof info.success !== 'undefined' && !info.success) {
			alert('Driver info could not be retrieved:\n'+info.message);

			$('#device-add-info').text('').hide();
			$('#device-add-loader').hide();
			
			return false;
		}
		else {
			if (typeof info.description !== 'undefined') {
				$('#device-add-info').html('<span style="color:#888">'+info.description+'</span>').show();
			}
			else {
				$('#device-add-info').text('').hide();
			}
			
			config.load(deviceAddDialog.deviceConfig, info);
		}
		$('#device-add-loader').hide();
		return true;
	},

	'clearModal':function() {
		$('#device-add-driver').html('<span style="color:#888"><em>loading...</em></span>').show();
		$("#device-add-driver-select").empty().hide();
		$('#device-add-info').text('').hide();

		$('#device-add-name').val('');
		$('#device-add-description').val('');
	},

	'adjustModal':function() {
		
		if ($("#device-modal-add").length) {
			var h = $(window).height() - $("#device-modal-add").position().top - 180;
			$("#device-add-body").height(h);
		}
	},

	'registerEvents':function() {
		
		// Event to scroll to parameter panel at the bottom of the page when editing
		$('#config', '#device-add-container').on('click', '.edit-parameter', function() {
			
			var container = $('#deviceConfigBody');
			container.animate({
				scrollTop: container.scrollTop() + container.height()
			});
		});
		
		$('#device-add-driver-select').off('change').on('change', function() {
			
			deviceAddDialog.ctrlId = $('option:selected', this).attr('ctrlid');
			deviceAddDialog.driverId = this.value;
			deviceAddDialog.deviceConfig = {};
			deviceAddDialog.deviceType = null;

			if (!deviceAddDialog.drawParameters()) {
				$("#device-modal-add").modal('hide');
				return false;
			}
		});

		$("#device-add-save").off('click').on('click', function () {

			var id = $('#device-add-name').val();
			
			if (id && deviceAddDialog.driverId != null) {
				if (config.valid()) {
					$('#device-add-loader').show();
					
					var description = $('#device-add-description').val();
					
					var deviceConfig = {
							'id': id,
							'description': description
					};
					if (deviceAddDialog.deviceConfig['disabled'] != null) {
						deviceConfig['disabled'] = deviceAddDialog.deviceConfig['disabled'];
					}
					
					deviceConfig['address'] = config.parseOptions('address');
					deviceConfig['settings'] = config.parseOptions('settings');
					
					// Make sure JSON.stringify gets passed the right object type
					deviceConfig['configs'] = $.extend({}, config.getOptions('configs'));
					
					var result = device.create(deviceAddDialog.ctrlId, deviceAddDialog.driverId, deviceConfig);
					if (typeof result.success !== 'undefined' && !result.success) {
						$('#device-add-loader').hide();
						alert('Device could not be created:\n'+result.message);
						return false;
					}
					update();
					$('#device-add-loader').hide();
					$('#device-modal-add').modal('hide');
				}
				else {
					alert('Required parameters need to be configured first.');
					return false;
				}
			}
			else {
				alert('Device needs to be configured first.');
				return false;
			}
		});
	}
}
