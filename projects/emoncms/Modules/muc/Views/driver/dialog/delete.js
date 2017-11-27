var driverDeleteDialog =
{
	'ctrlId': null,
	'driverId': null,

	'load': function(driver, tablerow) {
		
		this.ctrlId = driver.ctrlid;
		this.driverId = driver.id;

		$('#driver-modal-delete').modal('show');
		$('#driver-delete-label').html('Delete Driver: <b>'+driver.name+'</b>');
		
		// Initialize callbacks
		this.registerEvents(tablerow);
	},

	'registerEvents':function(row) {
		
		$("#driver-delete-confirm").off('click').on('click', function() {
			$('#driver-delete-loader').show();
			var result = driver.remove(driverDeleteDialog.ctrlId, driverDeleteDialog.driverId);
			$('#driver-delete-loader').hide();

			if (typeof result.success !== 'undefined' && !result.success) {
				alert('Unable to delete driver:\n'+result.message);
				return false;
				
			} else {
				if (row != null) table.remove(row);
			    
				update();
				$('#driver-modal-delete').modal('hide');
			}
			return true;
		});
	}
}
