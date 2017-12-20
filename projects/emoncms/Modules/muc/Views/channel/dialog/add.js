var channelAddDialog =
{
	'ctrlId': null,
	'driverId': null,
	'deviceId': null,
	'channelConfig': null,

	'load':function(device){
		
		if (device != null) {
			this.ctrlId = device.ctrlid;
			this.driverId = device.driverid;
			this.deviceId = device.id;
		}
		else {
			this.ctrlId = null;
			this.driverId = null;
			this.deviceId = null;
		}
		this.channelConfig = {};
		
		this.draw();
		
		// Initialize callbacks
		this.registerEvents();
	},

	'loadChannel':function(channel) {

		this.ctrlId = channel.ctrlid;
		this.driverId = channel.driverid;
		this.deviceId = channel.deviceid;
		this.channelConfig = channel;
		
		this.draw();
		
		// Initialize callbacks
		this.registerEvents();
	},

	'draw':function() {
		
		$("#channel-modal-add").modal('show');
		this.adjustModal();
		this.clearModal();
		
		$("#channel-add-device-label").hide();

		var groups = {
			address: "Channel address",
			settings: "Channel settings",
			configs: "Configuration"
		};
		config.init($('#channel-add-container'), groups);

		if (this.deviceId != null) {
			$('#channel-add-device-label').html('<b>'+this.deviceId+'</b>').show();
			$('#channel-add-device-select').hide();
			
			if (!this.drawParameters()) {
				$("#channel-modal-add").modal('hide');
				return false;
			}
		}
		else {
			$("#channel-add-device-label").hide();

			// Append devices from database to select
			var deviceselect = $("#channel-add-device-select").show();
			deviceselect.append("<option selected hidden='true' value=''>Select a device</option>");
						
			$.ajax({ url: path+"muc/device/list.json", dataType: 'json', async: true, success: function(data, textStatus, xhr) {
				$.each(data, function() {
					if (this.ctrlid > 0) {
						deviceselect.append('<option value="'+this.id+'" ctrlid="'+this.ctrlid+'" driverid="'+this.driverid+'">'+this.id+'</option>');
					}
				});
			}});
		}
	},
	
	'drawParameters':function() {
		
		$('#channel-add-loader').show();
		
		var info = channel.info(channelAddDialog.ctrlId, channelAddDialog.driverId);
		if (typeof info.success !== 'undefined' && !info.success) {
			alert('Channel info could not be retrieved:\n'+info.message);
			
			$('#channel-add-info').text('').hide();
			$('#channel-add-loader').hide();
			
			return false;
		}
		else {
			if (typeof info.description !== 'undefined') {
				$('#channel-add-info').html('<span style="color:#888">'+info.description+'</span>').show();
			}
			else {
				$('#channel-add-info').text('').hide();
			}
			
			config.load(channelAddDialog.channelConfig, info);
		}
		$('#channel-add-loader').hide();
		return true;
	},

	'clearModal':function() {
		
        $('#channel-add-device-label').html('<span style="color:#888"><em>loading...</em></span>').show();
		$("#channel-add-device-select").empty().hide();
        $('#channel-add-node-label').text('').hide();
        $('#channel-add-node').val('').show();
		$('#channel-add-name').val('');
		$('#channel-add-description').val('');
		$('#channel-add-info').text('').hide();
	},

	'adjustModal':function() {
		
		if ($("#channel-modal-add").length) {
			var h = $(window).height() - $("#channel-modal-add").position().top - 180;
			$("#channel-add-body").height(h);
		}
	},

	'registerEvents':function() {
		
		// Event to scroll to parameter panel at the bottom of the page when editing
		$('#config', '#channel-add-container').on('click', '.edit-parameter', function() {
			
			var container = $('#channel-add-body');
			container.animate({
		    	scrollTop: container.scrollTop() + container.height()
		    });
		});
		
		$('#channel-add-device-select').off('change').on('change', function() {

			channelAddDialog.ctrlId = $('option:selected', this).attr('ctrlid');
			channelAddDialog.driverId = $('option:selected', this).attr('driverid');
			channelAddDialog.deviceId = this.value;
			channelAddDialog.channelConfig = {};
			
			if (!channelAddDialog.drawParameters()) {
				$("#channel-modal-add").modal('hide');
				return false;
			}
		});

		$("#channel-add-save").off('click').on('click', function () {

			var node = $('#channel-add-node').val();
			var name = $('#channel-add-name').val();

			if (node && name && channelAddDialog.deviceId) {

				var description = $('#channel-add-description').val();
				var authorization = $('#channel-add-auth').val();
				
				if (config.valid()) {
					$('#channel-add-loader').show();
					
					var channelConfig = {
							'nodeid': node,
							'id': name,
							'description': description,
							'authorization': authorization
					};
					channelConfig['address'] = config.parseOptions('address');
					channelConfig['settings'] = config.parseOptions('settings');
					
					// Make sure JSON.stringify gets passed the right object type
					channelConfig['configs'] = $.extend({}, config.getOptions('configs'));
					
					var result = channel.create(channelAddDialog.ctrlId, channelAddDialog.deviceId, channelConfig);
					$('#channel-add-loader').hide();
					
					if (typeof result.success !== 'undefined' && !result.success) {
						alert('Channel could not be created:\n'+result.message);
						return false;
					}
				}
				else {
					alert('Required parameters need to be configured first.');
					return false;
				}
				
				update();
				$('#channel-modal-add').modal('hide');
			}
			else {
				alert('Channel needs to be configured first.');
				return false;
			}
		});
	}
}
