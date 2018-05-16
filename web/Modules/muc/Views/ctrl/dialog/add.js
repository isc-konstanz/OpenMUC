var ctrlAddDialog =
{
	'load': function() {
		
		$("#ctrl-modal-add").modal('show');
		
		// Initialize callbacks
		this.registerEvents();
	},

	'registerEvents':function() {
		
		$("#ctrl-add-save").off('click').on('click', function () {
			
			$('#ctrl-add-loader').show();

			var type = $('#ctrl-add-type').val();
			var address = $('#ctrl-add-address').val();
			var description = $('#ctrl-add-description').val();
			
			var result = muc.create(type, address, description);
			$('#ctrl-add-loader').hide();

			if (typeof result.success !== 'undefined' && (!result.success || result.id < 1)) {
				alert('MUC could not be created:\n'+result.message);
				return false;
			}
			else {
				update();
				$('#ctrl-modal-add').modal('hide');
			}
		});
	}
}
