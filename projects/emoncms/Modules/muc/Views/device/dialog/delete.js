var deviceDeleteDialog =
{
	'crtlId': null,
	'deviceId': null,

	'load': function(device, tablerow) {
		
		this.crtlId = device.ctrlid;
		this.deviceId = device.id;

		$('#device-modal-delete').modal('show');
		$('#device-delete-label').html('Delete Device: <b>'+device.id+'</b>');
		
		// Initialize callbacks
		this.registerEvents(tablerow);
	},

	'registerEvents':function(row) {
		
		$("#device-delete-confirm").off('click').on('click', function() {
			$('#device-delete-loader').show();
			var result = device.remove(deviceDeleteDialog.crtlId, deviceDeleteDialog.deviceId);
			$('#device-delete-loader').hide();

			if (typeof result.success !== 'undefined' && !result.success) {
				alert('Unable to delete device:\n'+result.message);
				return false;
				
			} else {
				if (row != null) table.remove(row);
			    
				update();
				$('#device-modal-delete').modal('hide');
			}
			return true;
		});
	}
}
