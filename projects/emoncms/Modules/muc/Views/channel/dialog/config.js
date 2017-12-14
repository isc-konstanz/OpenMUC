var channelConfigDialog =
{
	'channelConfig': null,

	'load': function(channel, showDelete) {
		
		this.channelConfig = channel;

		this.draw(showDelete);
		
		// Initialize callbacks
		this.registerEvents();
	},

	'draw':function(showAdditional) {
		
		$("#channel-modal-config").modal('show');
		$('#channel-config-label').html('Configure Channel: <b>'+this.channelConfig.id+'</b>');
		if (showAdditional) {
			$('#channel-config-write').show();
			$('#channel-config-delete').show();
		}
		else {
			$('#channel-config-write').hide();
			$('#channel-config-delete').hide();
		}
		
		this.adjustModal();
		this.clearModal();

		var groups = {
			address: "Channel address",
			settings: "Channel settings",
			configs: "Configuration"
		};
		config.init($('#channel-config-container'), groups);
		
		if (!this.drawParameters()) {
			$("#channel-modal-config").modal('hide');
		}
	},
	
	'drawParameters':function() {
		
		$('#channel-config-loader').show();
		
		var info = channel.info(channelConfigDialog.channelConfig.ctrlid, channelConfigDialog.channelConfig.driverid);
		if (typeof info.success !== 'undefined' && !info.success) {
			alert('Channel info could not be retrieved:\n'+info.message);

			$('#channel-config-info').text('').hide();
			$('#channel-config-loader').hide();
			
			return false;
		}
		else {
			if (typeof info.description !== 'undefined') {
				$('#channel-config-info').html('<span style="color:#888">'+info.description+'</span>').show();
			}
			else {
				$('#channel-config-info').text('').hide();
			}
			
			config.load(channelConfigDialog.channelConfig, info);
		}
		$('#channel-config-loader').hide();
		return true;
	},

	'clearModal':function() {

		$('#channel-config-device').html('<b>'+this.channelConfig.deviceid+'</b>');
		$('#channel-config-auth').val(this.channelConfig.authorization);
		$('#channel-config-node-label').text('').hide();
		$('#channel-config-node').val(this.channelConfig.nodeid).show();
		$('#channel-config-name').val(this.channelConfig.id);
		$('#channel-config-description').val(this.channelConfig.description);
		$('#channel-config-info').text('').hide();
	},

	'adjustModal':function() {
		
		if ($("#channel-modal-config").length) {
			var h = $(window).height() - $("#channel-modal-config").position().top - 180;
			$("#channel-config-body").height(h);
		}
	},

	'registerEvents':function() {

		// Event to scroll to parameter panel at the bottom of the page when editing
		$('#config', '#channel-config-container').on('click', '.edit-parameter', function() {
			
			var container = $('#channel-config-body');
			container.animate({
		    	scrollTop: container.scrollTop() + container.height()
		    });
		});

		$("#channel-config-save").off('click').on('click', function () {
			
			if (config.valid()) {
				$('#channel-config-loader').show();

				var node = $('#channel-config-node').val();
				var name = $('#channel-config-name').val();
				var description = $('#channel-config-description').val();
				
				var authorization = $('#channel-config-auth').val();
				
				var channelConfig = {
						'nodeid': node,
						'id': name,
						'description': description,
						'authorization': authorization
				};
				if (channelConfigDialog.channelConfig['disabled'] != null) {
					channelConfig['disabled'] = channelConfigDialog.channelConfig['disabled'];
				}
				channelConfig['address'] = config.parseOptions('address');
				channelConfig['settings'] = config.parseOptions('settings');
				
				// Make sure JSON.stringify gets passed the right object type
				channelConfig['configs'] = $.extend({}, config.getOptions('configs'));
				
				var result = channel.update(channelConfigDialog.channelConfig.ctrlid, channelConfigDialog.channelConfig.nodeid, 
						channelConfigDialog.channelConfig.id, channelConfigDialog.channelConfig.description, channelConfig);
				if (typeof result.success !== 'undefined' && !result.success) {
					$('#channel-config-loader').hide();
					alert('Channel could not be configured:\n'+result.message);
					return false;
				}
			}
			else {
				alert('Required parameters need to be configured first.');
				return false;
			}
			
			update();
			$('#channel-config-loader').hide();
			$('#channel-modal-config').modal('hide');
		});
		
		$("#channel-config-write").off('click').on('click', function () {
			
			$('#channel-modal-config').modal('hide');
			
			channelWriteDialog.load(channelConfigDialog.channelConfig);
		});
		
		$("#channel-config-delete").off('click').on('click', function () {
			
			$('#channel-modal-config').modal('hide');
			
			channelDeleteDialog.load(channelConfigDialog.channelConfig);
		});
	}
}
