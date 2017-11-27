var driverConfigDialog =
{
	'driverConfig': null,

	'load': function(driver, showDelete) {
		
		this.driverConfig = driver;

		this.draw(showDelete);
		
		// Initialize callbacks
		this.registerEvents();
	},
	
	'draw':function(showDelete) {
		
		$("#driver-modal-config").modal('show');
		$('#driver-config-label').html('Configure Driver: <b>'+this.driverConfig.name+'</b>');
		if (showDelete) {
			$('#driver-config-delete').show();
		}
		else {
			$('#driver-config-delete').hide();
		}
		
		this.adjustModal();
		this.clearModal();

		var groups = {
			configs: "Configuration"
		};
		config.init($('#driver-config-container'), groups);
		
		if (!this.drawParameters()) {
			$("#driver-modal-config").modal('hide');
		}
	},
	
	'drawParameters':function() {
		
		$('#driver-config-loader').show();
		
		var info = driver.info(driverConfigDialog.driverConfig.ctrlid, driverConfigDialog.driverConfig.id);
		if (typeof info.success !== 'undefined' && !info.success) {
			alert('Driver info could not be retrieved:\n'+info.message);
			
			$('#driver-config-loader').hide();
			return false;
		}
		else {
			if (typeof info.description !== 'undefined') {
				$('#driver-config-description').html('<span style="color:#888">'+info.description+'</span>');
			}
			
			config.load(driverConfigDialog.driverConfig, info);
		}
		$('#driver-config-loader').hide();
		return true;
	},

	'clearModal':function() {
		
		$('#driver-config-description').text('');
	},

	'adjustModal':function() {
		
		if ($("#driver-modal-config").length) {
			var h = $(window).height() - $("#driver-modal-config").position().top - 180;
			$("#driver-config-body").height(h);
		}
	},

	'registerEvents':function() {

		// Event to scroll to parameter panel at the bottom of the page when editing
		$('#config', '#driver-config-container').on('click', '.edit-parameter', function() {
			
			var container = $('#driver-config-body');
			container.animate({
		    	scrollTop: container.scrollTop() + container.height()
		    });
		});

		$("#driver-config-save").off('click').on('click', function () {
			
			$('#driver-config-loader').show();
			
			if (config.valid()) {
				var driverConfig = { 'id': driverConfigDialog.driverConfig.id };
				
				// Make sure JSON.stringify gets passed the right object type
				driverConfig['devices'] = $.extend([], driverConfigDialog.driverConfig.devices);
				driverConfig['configs'] = $.extend({}, config.getOptions('configs'));
				
				var result = driver.update(driverConfigDialog.driverConfig.ctrlid, driverConfigDialog.driverConfig.id, driverConfig);
				if (typeof result.success !== 'undefined' && !result.success) {
					alert('Driver could not be configured:\n'+result.message);
					return false;
				}
				
				update();
				$('#driver-config-loader').hide();
				$('#driver-modal-config').modal('hide');
			}
			else {
				$('#driver-config-loader').hide();
				
				alert('Required parameters need to be configured first.');
				return false;
			}
		});

		$("#driver-config-delete").off('click').on('click', function () {
			
			$('#driver-modal-config').modal('hide');
			
			driverDeleteDialog.load(driverConfigDialog.driverConfig);
		});
	}
}
