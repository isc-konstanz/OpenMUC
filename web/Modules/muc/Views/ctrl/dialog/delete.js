var ctrlDeleteDialog =
{
	'ctrlId': null,

	'load': function(ctrlId, tablerow) {
		
		this.ctrlId = ctrlId;
		
		$("#ctrl-modal-delete").modal('show');

		// Initialize callbacks
		this.registerEvents(tablerow);
	},

	'registerEvents':function(row) {
		
		$("#ctrl-delete-confirm").off('click').on('click', function() {
			
			$('#ctrl-delete-loader').show();
			var result = muc.remove(ctrlDeleteDialog.ctrlId);
			$('#ctrl-delete-loader').hide();
			
			if (!result.success) {
				alert('Unknown error while deleting muc');
				return false;
			}
			else {
			    table.remove(row);
				update();
				$('#ctrl-modal-delete').modal('hide');
			}
		});
	}
}
