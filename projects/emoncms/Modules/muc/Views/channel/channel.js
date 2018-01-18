var channel = 
{
    states: null,

    'create':function(ctrlid, deviceid, configs)
    {
        var result = {};
        $.ajax({ url: path+"muc/channel/create.json", data: "ctrlid="+ctrlid+"&deviceid="+deviceid+"&configs="+JSON.stringify(configs), dataType: 'json', async: false, success: function(data) {result = data;} });
        return result;
    },

    'list':function()
    {
        var result = {};
        $.ajax({ url: path+"muc/channel/list.json", dataType: 'json', async: false, success: function(data) {result = data;} });
        return result;
    },

    'states':function()
    {
        var result = {};
        $.ajax({ url: path+"muc/channel/states.json", dataType: 'json', async: false, success: function(data) {result = data;} });
        return result;
    },

    'info':function(ctrlid, driverid)
    {
        var result = {};
        $.ajax({ url: path+"muc/channel/info.json", data: "ctrlid="+ctrlid+"&driverid="+driverid, dataType: 'json', async: false, success: function(data){result = data;} });
        return result;
    },

    'get':function(ctrlid, id)
    {
        var result = {};
        $.ajax({ url: path+"muc/channel/get.json", data: "ctrlid="+ctrlid+"&id="+id, dataType: 'json', async: false, success: function(data) {result = data;} });
        return result;
    },

    'update':function(ctrlid, node, id, configs)
    {
        var result = {};
        $.ajax({ url: path+"muc/channel/update.json", data: "ctrlid="+ctrlid+"&nodeid="+node+"&id="+id+"&configs="+JSON.stringify(configs), async: false, success: function(data) {result = data;} });
        return result;
    },

    'write':function(ctrlid, id, value, valueType)
    {
        var result = {};
        $.ajax({ url: path+"muc/channel/write.json", data: "ctrlid="+ctrlid+"&id="+id+"&value="+value+"&valueType="+valueType, dataType: 'json', async: false, success: function(data){result = data;} });
        return result;
    },

    'remove':function(ctrlid, id)
    {
        var result = {};
        $.ajax({ url: path+"muc/channel/delete.json", data: "ctrlid="+ctrlid+"&id="+id, dataType: 'json', async: false, success: function(data){result = data;} });
        return result;
    },

    'scan':function(ctrlid, deviceid, settings)
    {
        var result = {};
        $.ajax({ url: path+"muc/channel/scan/list.json", data: "ctrlid="+ctrlid+"&deviceid="+deviceid+"&settings="+settings, dataType: 'json', async: false, success: function(data){result = data;} });
        return result;
    }
}
