var device = 
{
	states: null,

	'create':function(ctrlid, driverid, configs)
	{
		var result = {};
		$.ajax({ url: path+"muc/device/create.json", data: "ctrlid="+ctrlid+"&driverid="+driverid+"&configs="+JSON.stringify(configs), dataType: 'json', async: false, success: function(data){result = data;} });
		return result;
	},

	'list':function()
	{
		var result = {};
		$.ajax({ url: path+"muc/device/list.json", dataType: 'json', async: false, success: function(data){result = data;} });
		return result;
	},

	'states':function()
	{
		var result = {};
		$.ajax({ url: path+"muc/device/states.json", dataType: 'json', async: false, success: function(data){result = data;} });
		return result;
	},

	'info':function(ctrlid, driverid)
	{
		var result = {};
		$.ajax({ url: path+"muc/device/info.json", data: "ctrlid="+ctrlid+"&driverid="+driverid, dataType: 'json', async: false, success: function(data){result = data;} });
		return result;
	},

	'get':function(ctrlid, id)
	{
		var result = {};
		$.ajax({ url: path+"muc/device/get.json", data: "ctrlid="+ctrlid+"&id="+id, dataType: 'json', async: false, success: function(data){result = data;} });
		return result;
	},

	'update':function(ctrlid, id, configs)
	{
		var result = {};
		$.ajax({ url: path+"muc/device/update.json", data: "ctrlid="+ctrlid+"&id="+id+"&configs="+JSON.stringify(configs), dataType: 'json', async: false, success: function(data){result = data;} });
		return result;
	},

	'remove':function(ctrlid, id)
	{
		var result = {};
		$.ajax({ url: path+"muc/device/delete.json", data: "ctrlid="+ctrlid+"&id="+id, dataType: 'json', async: false, success: function(data){result = data;} });
		return result;
	},

	'scan':function(ctrlid, driverid, settings)
	{
		var result = {};
		$.ajax({ url: path+"muc/device/scan.json", data: "ctrlid="+ctrlid+"&driverid="+driverid+"&settings="+settings, dataType: 'json', async: false, success: function(data){result = data;} });
		return result;
	},

	'scanProgress':function(ctrlid, driverid)
	{
		var result = {};
		$.ajax({ url: path+"muc/device/scanprogress.json", data: "ctrlid="+ctrlid+"&driverid="+driverid, dataType: 'json', async: false, success: function(data){result = data;} });
		return result;
	},

	'scanCancel':function(ctrlid, driverid)
	{
		var result = {};
		$.ajax({ url: path+"muc/device/scancancel.json", data: "ctrlid="+ctrlid+"&driverid="+driverid, dataType: 'json', async: false, success: function(data){result = data;} });
		return result;
	}
}
