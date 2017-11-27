var muc = 
{
	'list':function()
	{
		var result = {};
		$.ajax({ url: path+"muc/controller/list.json", dataType: 'json', async: false, success: function(data) {result = data;} });
		return result;
	},

	'get':function(id)
	{
		var result = {};
		$.ajax({ url: path+"muc/controller/get.json", data: "id="+id, dataType: 'json', async: false, success: function(data) {result = data;} });
		return result;
	},

	'set':function(id, fields)
	{
		var result = {};
		$.ajax({ url: path+"muc/controller/set.json", data: "id="+id+"&fields="+JSON.stringify(fields), dataType: 'json', async: false, success: function(data) {result = data;} });
		return result;
	},

	'create':function(type, address, description)
	{
		var result = {};
		$.ajax({ url: path+"muc/controller/create.json", data: "type="+type+"&address="+address+"&description="+description, dataType: 'json', async: false, success: function(data){result = data;} });
		return result;
	},

	'remove':function(id)
	{
		var result = {};
		$.ajax({ url: path+"muc/controller/delete.json", data: "id="+id, dataType: 'json', async: false, success: function(data) {result = data;} });
		return result;
	}

}
