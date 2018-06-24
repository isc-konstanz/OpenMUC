var channel = {

    'list':function(callback) {
    	return $.ajax({
	        url: path+"channel/list.json",
	        dataType: 'json',
	        async: true,
	        success: callback
	    });
    }

}
