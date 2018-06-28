var channelScanDialog =
{
	'ctrlId': null,
	'deviceId': null,
	'driverId': null,
	'scanSettings': null,
	
	'scanChannels': [],

	'load':function(device) {

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
		
		this.draw();
		
		// Initialize callbacks
		this.registerEvents();
	},

	'draw':function() {
		
		$("#channel-modal-scan").modal('show');
		this.adjustModal();
		this.clearModal();

		var groups = {
			scanSettings: "Scan settings"
		};
		config.init($('#channel-scan-container'), groups);
		
		if (this.deviceId != null) {
			if (!this.drawParameters()) {
				$("#channel-modal-scan").modal('hide');
				return false;
			}
		}
		else {
			// Append devices from database to select
			var deviceselect = $("#channel-scan-device-select").show();
			deviceselect.append("<option selected hidden='true' value=''>Select a device</option>");
						
			$.ajax({ url: path+"muc/device/list.json", dataType: 'json', async: true, success: function(data, textStatus, xhr) {
				$.each(data, function() {
					deviceselect.append('<option value="'+this.id+'" ctrlid="'+this.ctrlid+'" driverid="'+this.driverid+'">'+this.id+'</option>');
				});
			}});
		}
	},
	
	'drawParameters':function() {
		
		$('#channel-scan-loader').show();

		var info = channel.info(channelScanDialog.ctrlId, channelScanDialog.driverId);
		if (typeof info.success !== 'undefined' && !info.success) {
			alert('Channel info could not be retrieved:\n'+info.message);
			
			$('#channel-scan-loader').hide();
			return false;
		}
		else {
			if (typeof info.description !== 'undefined') {
				$('#channel-scan-device-description').html('<span style="color:#888">'+info.description+'</span>').show();
			}
			else {
				$('#channel-scan-device-description').text('').hide();
			}
			
			config.load(channelScanDialog.scanSettings, info);
		}
		$('#channel-scan-loader').hide();
		return true;
	},
	
	'drawChannels':function() {
		
		if (channelScanDialog.scanChannels.length > 0) {
			$('#channel-scan-results-table').show();
			$('#channel-scan-results-none').hide();
			
			var table = '';
			for (var i = 0; i < channelScanDialog.scanChannels.length; i++) {
				table += '<tr>'+
						'<td><a class="add-channel" title="Add" row='+i+'><i class="icon-plus-sign" style="cursor:pointer"></i></a></td>'+
						'<td>'+channelScanDialog.scanChannels[i]['description']+'</td>'+
						'<td>'+channelScanDialog.scanChannels[i]['address']+'</td>'+
						'<td>'+channelScanDialog.scanChannels[i]['settings']+'</td>'+
						'<td>'+channelScanDialog.scanChannels[i]['configs']['valueType']+'</td>'+
						'<td>'+channelScanDialog.scanChannels[i]['metadata']+'</td>'+
						'</tr>';
			}
			$('#channel-scan-results').html(table);
		}
		else {
			$('#channel-scan-results-table').hide();
			$('#channel-scan-results-none').show();
		}
	},

	'clearModal':function(){

		if (this.deviceId != null) {
			$('#channel-scan-device-header').html('Device <b>'+this.deviceId+'</b>');
			$("#channel-scan-device-select").empty().hide();
		}
		else {
			$('#channel-scan-device-header').text('Device to search channels for: ');
			$("#channel-scan-device-select").empty().show();
		}
		$('#channel-scan-device-description').text('').hide();
		
		$('#channel-scan-results-table').hide();
		$('#channel-scan-results-none').hide();
	},

	'adjustModal':function() {
		
		if ($("#channel-modal-scan").length) {
			var h = $(window).height() - $("#channel-modal-scan").position().top - 180;
			$("#channel-scan-body").height(h);
		}
	},

	'registerEvents':function() {

		// Event to scroll to parameter panel at the bottom of the page when editing
		$('#config', '#channel-scan-container').on('click', '.edit-parameter', function() {
			
			var container = $('#channel-scan-body');
			container.animate({
		    	scrollTop: container.scrollTop() + container.height()
		    });
		});
		
		$('#channel-scan-device-select').off('change').on('change', function(){

			channelScanDialog.ctrlId = $('option:selected', this).attr('ctrlid');
			channelScanDialog.driverId = $('option:selected', this).attr('driverid');
			channelScanDialog.deviceId = this.value;
			channelScanDialog.scanSettings = {};

			$('#channel-scan-results-table').hide();
			$('#channel-scan-results-none').hide();
			
			if (!channelScanDialog.drawParameters()) {
				$("#channel-modal-scan").modal('hide');
				return false;
			}
		});

		$("#channel-scan-start").off('click').on('click', function () {
			
			if (config.valid()) {
				$('#channel-scan-loader').show();
				
				var settings = config.parseOptions('scanSettings');
				
				var result = channel.scan(channelScanDialog.ctrlId, channelScanDialog.deviceId, settings);
				if (typeof result.success !== 'undefined' && !result.success) {
					$('#channel-scan-loader').hide();
					alert('Channel scan failed:\n'+result.message);
					return false;
				}
				channelScanDialog.scanChannels = result;
				channelScanDialog.drawChannels();
				
				config.groupShow['scanSettings'] = false;
				config.draw();

				$('#channel-scan-loader').hide();
			}
			else {
				alert('Required parameters need to be configured first.');
				return false;
			}
		});

		$('#channel-scan-results').on('click', '.add-channel', function() {

			var row = $(this).attr('row');
			var localChannel = channelScanDialog.scanChannels[row];
			localChannel['driverid'] = channelScanDialog.driverId;
			localChannel['deviceid'] = channelScanDialog.deviceId;

			$("#channel-modal-scan").modal('hide');
			channelAddDialog.loadChannel(localChannel);
		});
	}
}
