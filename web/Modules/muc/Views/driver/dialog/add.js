var driverAddDialog =
{
	'ctrlId': null,
	'driverId': null,
	'driverConfig': null,

	'load':function(ctrl) {

		if (ctrl != null) {
			this.ctrlId = ctrl.id;
		}
		else {
			this.ctrlId = null;
		}
		this.driverId = null;
		this.driverConfig = null;

		this.draw();
		
		// Initialize callbacks
		this.registerEvents();
	},

	'draw':function() {
		
		$("#driver-modal-add").modal('show');
		this.adjustModal();
		this.clearModal();

		var groups = {
			configs: "Configuration"
		};
		config.init($('#driver-add-container'), groups);
		
		if (this.ctrlId != null) {
			$('#driver-add-ctrl').hide();
			
			if (!this.drawDrivers()) {
				$("#driver-modal-add").modal('hide');
				return false;
			}
		}
		else {
			// Append MUCs from database to select
			var ctrlSelect = $("#driver-add-ctrl-select");
			ctrlSelect.append("<option selected hidden='true' value=''>Select a controller</option>").val('');
			
			$.ajax({ url: path+"muc/controller/list.json", dataType: 'json', async: true, success: function(data, textStatus, xhr) {
				$.each(data, function() {
					ctrlSelect.append($("<option />").val(this.id).text(this.description));
				});
			}});
		}
	},
	
	'drawDrivers':function() {

		if (driverAddDialog.ctrlId > 0) {
			$('#driver-add-loader').show();
			$('#driver-add-overlay').hide();
			
			var drivers = driver.available(driverAddDialog.ctrlId);
			var configured = driver.configured(driverAddDialog.ctrlId);
			$('#driver-add-loader').hide();
			
			$('#driver-add-description').text('');
			
			var driverSelect = $("#driver-add-select");
			driverSelect.prop('disabled', false).find('option').remove().end()
				.append("<option selected hidden='true' value=''>Select a driver</option>").val('');

			if (typeof drivers.success !== 'undefined' && !drivers.success) {
				alert('Available drivers could not be retrieved:\n'+drivers.message);
				return false;
			}
			else if (typeof configured.success !== 'undefined' && !configured.success) {
				alert('Configured drivers could not be retrieved:\n'+configured.message);
				return false;
			}
			else {
				for (var i = 0; i < drivers.length; i++) {
					var driverDesc = drivers[i];
					if (configured.indexOf(driverDesc.id) < 0) {
						var driverName;
						if (typeof driverDesc.name !== 'undefined') {
							driverName = driverDesc.name;
						}
						else {
							driverName = driverDesc.id;
						}
						driverSelect.append($("<option />").val(driverDesc.id).text(driverName));
					}
				}
			}
			return true;
		}
	},
	
	'drawParameters':function() {
		
		$('#driver-add-loader').show();
		
		var info = driver.info(driverAddDialog.ctrlId, driverAddDialog.driverId);
		if (typeof info.success !== 'undefined' && !info.success) {
			alert('Driver info could not be retrieved:\n'+info.message);
			
			$('#driver-add-loader').hide();
			return false;
		}
		else {
			if (typeof info.description !== 'undefined') {
				$('#driver-add-description').html('<span style="color:#888">'+info.description+'</span>');
			}
			
			config.load(driverAddDialog.driverConfig, info);
		}
		$('#driver-add-loader').hide();
		return true;
	},

	'clearModal':function(){
		$('#driver-add-ctrl').show();
		$("#driver-add-ctrl-select").empty();
		$("#driver-add-select").empty().prop('disabled', true).show();
	
		$('#driver-add-description').text('');
		
		$('#driver-add-overlay').show();
	},

	'adjustModal':function() {
		
		if ($("#driver-modal-add").length) {
			var h = $(window).height() - $("#driver-modal-add").position().top - 180;
			$("#driver-add-body").height(h);
		}
	},

	'registerEvents':function() {

		// Event to scroll to parameter panel at the bottom of the page when editing
		$('#config', '#driver-add-container').on('click', '.edit-parameter', function() {
			
			var container = $('#driver-add-body');
			container.animate({
		    	scrollTop: container.scrollTop() + container.height()
		    });
		});
		
		$('#driver-add-ctrl-select').off('change').on('change', function(){
			var ctrlId = this.value;
			if (ctrlId.length > 0) {
				driverAddDialog.ctrlId = ctrlId;
				driverAddDialog.drawDrivers();
			}
		});
		
		$('#driver-add-select').off('change').on('change', function(){
			var driverId = this.value;
			if (driverId.length > 0) {
				driverAddDialog.driverId = driverId;
				driverAddDialog.driverConfig = {};
				driverAddDialog.drawParameters();
			}
		});

		$("#driver-add-save").off('click').on('click', function () {
			
			if (driverAddDialog.ctrlId != null && driverAddDialog.driverId != null) {
				
				$('#driver-add-loader').show();
				
				if (config.valid()) {
					var driverConfig = { 'id': driverAddDialog.driverId };
					
					// Make sure JSON.stringify gets passed the right object type
					driverConfig['configs'] = $.extend({}, config.getOptions('configs'));
					
					var result = driver.create(driverAddDialog.ctrlId, driverAddDialog.driverId, driverConfig);					
					if (typeof result.success !== 'undefined' && !result.success) {
						alert('Driver could not be created:\n'+result.message);
						return false;
					}
					
					update();
					$('#driver-add-loader').hide();
					$('#driver-modal-add').modal('hide');
				}
				else {
					$('#driver-add-loader').hide();
					
					alert('Required parameters need to be configured first.');
					return false;
				}
			}
			else {
				alert('Driver needs to be configured first.');
				return false;
			}
		});
	}
}
