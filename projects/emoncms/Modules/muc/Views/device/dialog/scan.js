var deviceScanDialog =
{
	'ctrlId': null,
	'driverId': null,
	'driver': null,
	'scanSettings': null,
	
	'scanDevices': [],

	'load':function(driver) {

		if (driver != null) {
			this.ctrlId = driver.ctrlid;
			this.driverId = driver.id;
			this.driver = driver.name;
		}
		else {
			this.ctrlId = null;
			this.driverId = null;
			this.driver = null;
		}

		this.draw();
		
		// Initialize callbacks
		this.registerEvents();
	},

	'draw':function() {
		
		$("#device-modal-scan").modal('show');
		this.adjustModal();
		this.clearModal();

		var groups = {
			scanSettings: "Scan settings"
		};
		config.init($('#device-scan-container'), groups);
		
		if (this.driverId != null) {
			if (!this.drawParameters()) {
				$("#device-modal-scan").modal('hide');
				return false;
			}
		}
		else {
			$.ajax({ url: path+"muc/controller/list.json", dataType: 'json', async: true, success: function(data, textStatus, xhr) {
				if (data.length > 0) {
					// Append drivers from database to select
					var driverSelect = $("#device-scan-driver-select").show();
					driverSelect.append("<option selected hidden='true' value=''>Select a driver</option>");
					
					$.each(data, function() {
						var ctrl = this;
						$.ajax({ url: path+"muc/driver/available.json", data: "ctrlid="+ctrl.id, dataType: 'json', async: false, success: function(result, textStatus, xhr) {
							if (result.length > 0) {
								if (deviceScanDialog.ctrlId <= 0) {
									driverSelect.append('<optgroup label="'+ctrl.description+'">');
								}

								$.each(result, function() {
									var driver;
									if (typeof this.name !== 'undefined') {
										driver = this.name;
									}
									else {
										driver = this.id;
									}
									driverSelect.append('<option value="'+this.id+'" ctrlid="'+ctrl.id+'">'+driver+'</option>');
								});
							}
						}});
					});
				}
				else {
					$("#device-modal-scan").modal('hide');
					
					alert('No controller registered');
					return false;
				}
			}});
		}
	},
	
	'drawParameters':function() {
		
		$('#device-scan-loader').show();

		var info = device.info(deviceScanDialog.ctrlId, deviceScanDialog.driverId);
		if (typeof info.success !== 'undefined' && !info.success) {
			alert('Device info could not be retrieved:\n'+info.message);
			
			$('#device-scan-loader').hide();
			
			return false;
		}
		else {
			if (typeof info.description !== 'undefined') {
				$('#device-scan-driver-description').html('<span style="color:#888">'+info.description+'</span>').show();
			}
			else {
				$('#device-scan-driver-description').text('').hide();
			}
			
			config.load(deviceScanDialog.scanSettings, info);
		}
		$('#device-scan-loader').hide();
		return true;
	},
	
	'drawDevices':function() {
		
		if (deviceScanDialog.scanDevices.length > 0) {
			$('#device-scan-results-table').show();
			$('#device-scan-results-none').hide();
			
			var table = '';
			for (var i = 0; i < deviceScanDialog.scanDevices.length; i++) {
				table += '<tr>'+
						'<td><a class="add-device" title="Add" row='+i+'><i class="icon-plus-sign" style="cursor:pointer"></i></a></td>'+
						'<td>'+deviceScanDialog.scanDevices[i]['description']+'</td>'+
						'<td>'+deviceScanDialog.scanDevices[i]['address']+'</td>'+
						'<td>'+deviceScanDialog.scanDevices[i]['settings']+'</td>'+
						'</tr>';
			}
			$('#device-scan-results').html(table);
		}
		else {
			$('#device-scan-results-table').hide();
			$('#device-scan-results-none').show();
		}
	},

	'clearModal':function(){

		if (this.driverId != null) {
			$('#device-scan-driver-header').html('Driver <b>'+this.driver+'</b>');
			$("#device-scan-driver-select").empty().hide();
		}
		else {
			$('#device-scan-driver-header').text('Driver to search devices for: ');
			$("#device-scan-driver-select").empty().show();
		}
		$('#device-scan-driver-description').text('').hide();
		
		$('#device-scan-results-table').hide();
		$('#device-scan-results-none').hide();
	},

	'adjustModal':function() {
		
		if ($("#device-modal-scan").length) {
			var h = $(window).height() - $("#device-modal-scan").position().top - 180;
			$("#device-scan-body").height(h);
		}
	},

	'registerEvents':function() {

		// Event to scroll to parameter panel at the bottom of the page when editing
		$('#config', '#device-scan-container').on('click', '.edit-parameter', function() {
			
			var container = $('#device-scan-body');
			container.animate({
		    	scrollTop: container.scrollTop() + container.height()
		    });
		});
		
		$('#device-scan-driver-select').off('change').on('change', function(){

			deviceScanDialog.driverId = this.value;
			deviceScanDialog.driver = $('option:selected', this).text();
			deviceScanDialog.ctrlId = $('option:selected', this).attr('ctrlid');
			deviceScanDialog.scanSettings = {};

			$('#device-scan-results-table').hide();
			$('#device-scan-results-none').hide();
			
			if (!deviceScanDialog.drawParameters()) {
				$("#device-modal-scan").modal('hide');
				return false;
			}
		});

		$("#device-scan-start").off('click').on('click', function () {
			
			if (config.valid()) {
				$('#device-scan-loader').show();
				
				var settings = config.parseOptions('scanSettings');
				
				var result = device.scan(deviceScanDialog.ctrlId, deviceScanDialog.driverId, settings);
				if (typeof result.success !== 'undefined' && !result.success) {
					$('#device-scan-loader').hide();
					alert('Device scan failed:\n'+result.message);
					return false;
				}
				deviceScanDialog.scanDevices = result;
				deviceScanDialog.drawDevices();
				
				config.groupShow['scanSettings'] = false;
				config.draw();

				$('#device-scan-loader').hide();
			}
			else {
				alert('Required parameters need to be configured first.');
				return false;
			}
		});

		$('#device-scan-results').on('click', '.add-device', function() {

			var row = $(this).attr('row');
			var localDevice = deviceScanDialog.scanDevices[row];
			localDevice['driverid'] = deviceScanDialog.driverId;
			localDevice['driver'] = deviceScanDialog.driver;

			$("#device-modal-scan").modal('hide');
			deviceAddDialog.loadDevice(localDevice);
		});
	}
}
