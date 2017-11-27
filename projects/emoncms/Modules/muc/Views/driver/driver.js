var driver = {

	'create':function(ctrlid, id, configs)
	{
		var result = {};
		$.ajax({ url: path+"muc/driver/create.json", data: "ctrlid="+ctrlid+"&id="+id+"&configs="+JSON.stringify(configs), dataType: 'json', async: false, success: function(data){result = data;} });
		return result;
	},

	'list':function()
	{
		var result = {};
		$.ajax({ url: path+"muc/driver/list.json", dataType: 'json', async: false, success: function(data) {result = data;} });
		return result;
	},
	
	'available':function(ctrlid)
	{
		var result = {};
		$.ajax({ url: path+"muc/driver/available.json", data: "ctrlid="+ctrlid, dataType: 'json', async: false, success: function(data) {result = data;} });
		return result;
	},
	
	'configured':function(ctrlid)
	{
		var result = {};
		$.ajax({ url: path+"muc/driver/configured.json", data: "ctrlid="+ctrlid, dataType: 'json', async: false, success: function(data) {result = data;} });
		return result;
	},

	'info':function(ctrlid, id)
	{
		var result = {};
		$.ajax({ url: path+"muc/driver/info.json", data: "ctrlid="+ctrlid+"&id="+id, dataType: 'json', async: false, success: function(data) {result = data;} });
		return result;
	},

	'get':function(ctrlid, id)
	{
		var result = {};
		$.ajax({ url: path+"muc/driver/get.json", data: "ctrlid="+ctrlid+"&id="+id, dataType: 'json', async: false, success: function(data) {result = data;} });
		return result;
	},

	'update':function(ctrlid, id, configs)
	{
		var result = {};
		$.ajax({ url: path+"muc/driver/update.json", data: "ctrlid="+ctrlid+"&id="+id+"&configs="+JSON.stringify(configs), dataType: 'json', async: false, success: function(data){result = data;} });
		return result;
	},

	'remove':function(ctrlid, id)
	{
		var result = {};
		$.ajax({ url: path+"muc/driver/delete.json", data: "ctrlid="+ctrlid+"&id="+id, dataType: 'json', async: false, success: function(data){result = data;} });
		return result;
	}
}
