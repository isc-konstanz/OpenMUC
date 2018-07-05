var device_dialog =
{
    'ctrlid': null,
    'driverid': null,
    'driver': null,
    'device': null,

    'loadNew': function(driver) {
        if (driver != null) {
            this.ctrlid = driver.ctrlid;
            this.driverid = driver.id;
            this.driver = driver.name;
        }
        else {
            this.ctrlid = null;
            this.driverid = null;
            this.driver = null;
        }
        this.device = null;
        
        this.drawConfig();
    },

    'loadConfig': function(device) {
        this.ctrlid = null;
        this.driverid = null;
        this.driver = null;
        this.device = device;
        
        this.drawConfig();
    },

    'drawConfig':function() {
        $("#device-config-modal").modal('show');
        
        this.adjustConfigModal();
        
        var groups = {
            address: "Device address",
            settings: "Device settings",
            configs: "Configuration"
        };
        config.init($('#device-config-container'), groups);
        
        $('#device-config-driver').html('<span style="color:#888"><em>loading...</em></span>');
        $("#device-config-driver-select").empty().hide();
        $('#device-config-info').text('').hide();
        
        if (device_dialog.device != null) {
            $('#device-config-label').html('Configure Device: <b>'+device_dialog.device.id+'</b>');
            $('#device-config-driver').html('<b>'+device_dialog.device.driver+'</b>').show();
            $('#device-config-name').val(device_dialog.device.id);
            $('#device-config-description').val(device_dialog.device.description);
            
            $('#device-config-overlay').hide();
            
            if (typeof device_dialog.device.scanned !== 'undefined' && !device_dialog.device.scanned) {
                $('#device-config-back').hide();
                $('#device-config-scan').show();
                $('#device-config-delete').hide();
            }
            else {
                $('#device-config-back').show();
                $('#device-config-scan').hide();
                $('#device-config-delete').show();
            }
            device_dialog.drawPreferences('config');
        }
        else {
            $('#device-config-label').html('New Device');
            $('#device-config-name').val('');
            $('#device-config-description').val('');
            
            $('#device-config-back').hide();
            $('#device-config-delete').hide();
            
            if (device_dialog.driverid != null) {
                $('#device-config-driver').html('<b>'+device_dialog.driver+'</b>').show();

                $('#device-config-overlay').hide();
                $('#device-config-scan').hide();
                
                device_dialog.drawPreferences('config');
            }
            else {
                $('#device-config-overlay').show();
                $('#device-config-scan').show();
                
                device_dialog.drawDrivers('config');
            }
        }
        device_dialog.registerConfigEvents();
    },

    'drawDrivers':function(modal) {
        muc.list(function(data, textStatus, xhr) {
            // Append drivers from database to select
            var driverSelect = $('#device-'+modal+'-driver-select');
            driverSelect.append("<option selected hidden='true' value=''>Select a driver</option>");
            
            $.each(data, function() {
                var ctrl = this;
                $.ajax({ url: path+"muc/driver/registered.json", data: "ctrlid="+ctrl.id, dataType: 'json', async: false, success: function(result, textStatus, xhr) {
                    if (result.length > 0) {
                        if (device_dialog.ctrlid <= 0) {
                            driverSelect.append('<optgroup label="'+ctrl.description+'">');
                        }
                        
                        $.each(result, function() {
                            var driver;
                            if (typeof this.name !== 'undefined') {
                                driver = this.name;
                            }
                            else {
                                driver = this.id;
                            }
                            driverSelect.append('<option value="'+this.id+'" ctrlid="'+ctrl.id+'">'+driver+'</option>');
                        });
                    }
                }});
            });
            driverSelect.show();
            if (modal == 'config') {
                $('#device-config-driver').hide();
            }
        });
    },

    'drawPreferences':function(modal) {
        $('#device-'+modal+'-loader').show();
        
        var ctrlid;
        var driverid;
        if (device_dialog.device != null) {
            ctrlid = device_dialog.device.ctrlid;
            driverid = device_dialog.device.driverid;
        }
        else {
            ctrlid = device_dialog.ctrlid;
            driverid = device_dialog.driverid;
        }
        
        device.info(ctrlid, driverid, function(result) {
            if (typeof result.success !== 'undefined' && !result.success) {
                $('#device-'+modal+'-info').text('').hide();
                
                alert('Device info could not be retrieved:\n'+result.message);
            }
            else {
                if (typeof result.description !== 'undefined') {
                    $('#device-'+modal+'-info').html('<span style="color:#888">'+result.description+'</span>').show();
                }
                else {
                    $('#device-'+modal+'-info').text('').hide();
                }
                
                config.load(device_dialog.device, result);
            }
            $('#device-'+modal+'-loader').hide();
        });
    },

    'closeConfigModal':function(result) {
        $('#device-config-loader').hide();
        
        if (typeof result.success !== 'undefined' && !result.success) {
            alert('Device could not be configured:\n'+result.message);
            return false;
        }
        update();
        $('#device-config-modal').modal('hide');
    },

    'adjustConfigModal':function() {
        if ($("#device-config-modal").length) {
            var h = $(window).height() - $("#device-config-modal").position().top - 180;
            $("#device-config-body").height(h);
        }
    },

    'registerConfigEvents':function() {
        
        // Event to scroll to parameter panel at the bottom of the page when editing
        $('#config', '#device-config-container').on('click', '.edit-parameter', function() {
            
            var container = $('#deviceBody');
            container.animate({
                scrollTop: container.scrollTop() + container.height()
            });
        });

        $('#device-config-driver-select').off('change').on('change', function() {
            device_dialog.ctrlid = $('option:selected', this).attr('ctrlid');
            device_dialog.driverid = this.value;
            device_dialog.driver = $('option:selected', this).text();
            device_dialog.device = null;
            
            device_dialog.drawPreferences('config');
            
            $('#device-config-overlay').hide();
        });

        $("#device-config-save").off('click').on('click', function () {
            var id = $('#device-config-name').val();
            
            if (id == '' || (device_dialog.device == null && device_dialog.driverid == null)) {
                alert('Device needs to be configured first.');
                return false;
            }
            if (!config.valid()) {
                alert('Required parameters need to be configured first.');
                return false;
            }
            $('#device-config-loader').show();
            
            var configs = { 'id': id, 'description': $('#device-config-description').val() };
            
            configs['address'] = config.parseOptions('address');
            configs['settings'] = config.parseOptions('settings');
            
            // Make sure JSON.stringify gets passed the right object type
            configs['configs'] = $.extend({}, config.getOptions('configs'));
            
            if (device_dialog.device != null 
                    && !(typeof device_dialog.device.scanned !== 'undefined' && !device_dialog.device.scanned)) {
                
                if (device_dialog.device['disabled'] != null) {
                    configs['disabled'] = device_dialog.device['disabled'];
                }
                configs['channels'] = $.extend([], device_dialog.device.channels);
                
                result = device.update(device_dialog.device.ctrlid, device_dialog.device.id, configs, 
                        device_dialog.closeConfigModal);
            }
            else {
                result = device.create(device_dialog.ctrlid, device_dialog.driverid, configs, 
                        device_dialog.closeConfigModal);
            }
        });

        $("#device-config-back").off('click').on('click', function () {
            $('#device-config-modal').modal('hide');
            $('#device-scan-modal').modal('show');
        });

        $("#device-config-scan").off('click').on('click', function () {
            $('#device-config-modal').modal('hide');
            
            device_dialog.loadScan();
        });

        $("#device-config-delete").off('click').on('click', function () {
            $('#device-config-modal').modal('hide');
            
            device_dialog.loadDelete(device_dialog.device);
        });
    },

    'loadScan':function(driver) {
        if (driver != null) {
            this.ctrlid = driver.ctrlid;
            this.driverid = driver.id;
            this.driver = driver.name;
        }
        else {
            this.ctrlid = null;
            this.driverid = null;
            this.driver = null;
        }
        this.device = null;
        
        this.scanUpdater = null;
        this.scanDevices = [];
        this.drawScan();
    },

    'drawScan':function() {
        $("#device-scan-modal").modal('show');
        
        device_dialog.adjustScanModal();
        
        var groups = {
            scanSettings: "Scan settings"
        };
        config.init($('#device-scan-container'), groups);
        
        $('#device-scan-progress').removeClass('progress-default progress-info progress-success progress-warning progress-error').hide();
        $('#device-scan-progress-bar').css('width', '100%');
        
        $('#device-scan-results').text('');
        $('#device-scan-results-table').hide();
        
        if (device_dialog.driverid != null) {
            $('#device-scan-label').html('Scan Devices: <b>'+device_dialog.driver+'</b>');
            $("#device-scan-driver-select").hide().empty();
            $('#device-scan-driver').hide();
            $('#device-scan-overlay').hide();
            
            device_dialog.drawPreferences('scan');
        }
        else {
            $('#device-scan-label').html('Scan Devices');
            $("#device-scan-driver-select").show().empty();
            $('#device-scan-driver').show();
            $('#device-scan-overlay').show();
            
            device_dialog.drawDrivers('scan');
        }
        device_dialog.registerScanEvents();
    },

    'drawScanProgress':function(progress) {
    	device_dialog.drawScanProgressBar(progress);
    	
    	if (!progress.success) {
            alert(progress.message);
    		return;
    	}
    	
    	device_dialog.scanDevices = progress.devices;
        if (device_dialog.scanDevices.length > 0) {
        	
            $('#device-scan-results-table').show();
            $('#device-scan-results-none').hide();
            
            var table = '';
            for (var i = 0; i < device_dialog.scanDevices.length; i++) {
                table += '<tr class="device-scan-row" title="Add" row='+i+'>'+
                        '<td><i class="icon-edit"></i> '+device_dialog.scanDevices[i]['description']+'</td>'+
                        '</tr>';
            }
            $('#device-scan-results').html(table);
        }
        else {
            $('#device-scan-results-table').hide();
            $('#device-scan-results-none').show();
        }
    },

    'drawScanProgressBar':function(progress) {
    	var bar = $('#device-scan-progress');

    	var value = 100;
		var type = 'danger';
    	if (progress.success) {
        	value = progress.info.progress;
        	
        	if (progress.info.interrupted) {
        		value = 100;
        		type = 'warning';
        	}
        	else if (progress.info.finished) {
        		value = 100;
        		type = 'success';
        	}
        	else if (value > 0) {
            	// If the progress value equals zero, set it to 5%, so the user can see the bar already
            	if (value == 0) {
            		value = 5;
            	}
        		type = 'info';
        	}
        	else {
        		value = 100;
        		type = 'default';
        	}
    	}
        $('#device-scan-progress-bar').css('width', value+'%');
        
    	if (value < 100 || type == 'default') {
    		bar.addClass('active');
    	}
    	else {
    		bar.removeClass('active');
        }
    	bar.removeClass('progress-default progress-info progress-success progress-warning progress-danger');
    	bar.addClass('progress-'+type);
    	bar.show();
    },

    'scanProgress':function(progress) {
    	if (device_dialog.scanUpdater != null) {
    		clearTimeout(device_dialog.scanUpdater);
    		device_dialog.scanUpdater = null;
    	}
    	device_dialog.drawScanProgress(progress);
    	
    	// Continue to schedule scan progress requests every second until the scan info signals completion
    	if (progress.success && !progress.info.finished && !progress.info.interrupted && progress.info.progress < 100) {
    		
        	device_dialog.scanUpdater = setTimeout(device.scanProgress(device_dialog.ctrlid, device_dialog.driverid, 
        			device_dialog.scanProgress), 10000);
    	}
    },

    'adjustScanModal':function() {
        if ($("#device-scan-modal").length) {
            var h = $(window).height() - $("#device-scan-modal").position().top - 180;
            $("#device-scan-body").height(h);
        }
    },

    'registerScanEvents':function() {

        // Event to scroll to parameter panel at the bottom of the page when editing
        $('#config', '#device-scan-container').on('click', '.edit-parameter', function() {
            
            var container = $('#device-scan-body');
            container.animate({
                scrollTop: container.scrollTop() + container.height()
            });
        });

        $('#device-scan-driver-select').off('change').on('change', function(){
            device_dialog.ctrlid = $('option:selected', this).attr('ctrlid');
            device_dialog.driverid = this.value;
            device_dialog.driver = $('option:selected', this).text();
            device_dialog.device = null;
            
            device_dialog.drawPreferences('scan');
            
            $('#device-scan-results-table').hide();
            $('#device-scan-results-none').hide();
            $('#device-scan-overlay').hide();
        });

        $("#device-scan-start").off('click').on('click', function () {
            if (device_dialog.driverid == null) {
                alert('Driver needs to be configured first.');
                return false;
            }
            if (!config.valid()) {
                alert('Required parameters need to be configured first.');
                return false;
            }
            $('#device-scan-loader').show();
            
            var settings = config.parseOptions('scanSettings');
            
            device.scanStart(device_dialog.ctrlid, device_dialog.driverid, settings, function(result) {
                $('#device-scan-loader').hide();
                
                device_dialog.scanProgress(result);
                
                config.groupShow['scanSettings'] = false;
                config.draw();
            });
        });

        $('#device-scan-results').on('click', '.device-scan-row', function() {
            var row = $(this).attr('row');
            var device = device_dialog.scanDevices[row];
            device['driverid'] = device_dialog.driverid;
            device['driver'] = device_dialog.driver;
            device['scanned'] = true;
            
            $("#device-scan-modal").modal('hide');
            device_dialog.device = device;
            device_dialog.drawConfig();
        });
    },

    'loadDelete': function(device, tablerow) {
        this.ctrlid = null;
        this.driverid = null;
        this.driver = null;
        this.device = device;
        
        $('#device-delete-modal').modal('show');
        $('#device-delete-label').html('Delete Device: <b>'+device.id+'</b>');
        
        this.registerDeleteEvents(tablerow);
    },

    'closeDeleteModal':function(result) {
        $('#device-delete-loader').hide();
        
        if (typeof result.success !== 'undefined' && !result.success) {
            alert('Unable to delete device:\n'+result.message);
            return false;
        }
        
        update();
        $('#device-delete-modal').modal('hide');
    },

    'registerDeleteEvents':function(row) {
        
        $("#device-delete-confirm").off('click').on('click', function() {
            $('#device-delete-loader').show();
            device.remove(device_dialog.device.ctrlid, device_dialog.device.id,
                    device_dialog.closeDeleteModal);
            
            if (typeof table !== 'undefined' && row != null) table.remove(row);
        });
    }
}
