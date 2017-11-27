var channelDeleteDialog =
{
	'ctrlId': null,
	'channelId': null,

	'load': function(channel, tablerow) {
		
		this.ctrlId = channel.ctrlid;
		this.channelId = channel.id;

		$('#channel-modal-delete').modal('show');
		$('#channel-delete-label').html('Delete Channel: <b>'+this.channelId+'</b>');
		
		// Initialize callbacks
		this.registerEvents(tablerow);
	},

	'registerEvents':function(row) {
		
		$("#channel-delete-confirm").off('click').on('click', function() {
			$('#channel-delete-loader').show();
			var result = channel.remove(channelDeleteDialog.ctrlId, channelDeleteDialog.channelId);
			$('#channel-delete-loader').hide();

			if (typeof result.success !== 'undefined' && !result.success) {
				alert('Unable to delete channel:\n'+result.message);
				return false;
				
			} else {
				if (row != null) table.remove(row);
			    
				update();
				$('#channel-modal-delete').modal('hide');
			}
			return true;
		});
	}
}
