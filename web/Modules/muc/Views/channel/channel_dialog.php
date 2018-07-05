<?php
    global $path;
?>

<script type="text/javascript" src="<?php echo $path; ?>Modules/muc/Views/channel/channel_dialog.js"></script>

<style>
    #channel-config-header th {
        text-align: left;
        font-weight: normal;
        color: #888;
    }
    #channel-config-header td:nth-of-type(1) { width:10%; padding-right:8px; }
    #channel-config-header td:nth-of-type(2) { width:10%; padding-right:8px; }
    
    #channel-scan-results td { cursor: pointer; }
</style>

<div id="channel-config-modal" class="modal hide keyboard modal-adjust" tabindex="-1" role="dialog" aria-labelledby="channel-config-modal" aria-hidden="true" data-backdrop="static">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="channel-config-label"></h3>
    </div>
    <div id="channel-config-body" class="modal-body">
        <table id="channel-config-header">
            <tr>
                <th><?php echo _('Device'); ?></th>
                <th><?php echo _('Key'); ?></th>
                <th><?php echo _('Name'); ?></th>
            </tr>
            <tr>
                <td>
                    <label id="channel-config-device" style="padding: 4px 6px; margin-bottom: 10px;"><span style="color:#888"><em><?php echo _('loading...'); ?></em></span></label>
                    <select id="channel-config-device-select" class="input-large" style="display:none;"></select>
                </td>
                <td><input id="channel-config-name" class="input-medium" type="text"></td>
                <td><input id="channel-config-description" class="input-large" type="text"></td>
            </tr>
        </table>
        <p id="channel-config-info" style="display:none;"></p>
        
        <div class="modal-container">
            <div id="channel-config-container"></div>
            
            <div id="channel-config-overlay" class="modal-overlay"></div>
        </div>
    </div>
    <div class="modal-footer">
        <button id="channel-config-back" class="btn" style="display:none; float:left"><?php echo _('Back'); ?></button>
        <button id="channel-config-cancel" class="btn" data-dismiss="modal" aria-hidden="true"><?php echo _('Cancel'); ?></button>
        <button id="channel-config-delete" class="btn btn-danger" style="display:none; cursor:pointer;"><i class="icon-trash icon-white"></i> <?php echo _('Delete'); ?></button>
        <button id="channel-config-scan" class="btn btn-info" style="display:none; cursor:pointer;"><i class="icon-search icon-white"></i> <?php echo _('Scan'); ?></button>
        <button id="channel-config-save" class="btn btn-primary"><?php echo _('Save'); ?></button>
    </div>
    <div id="channel-config-loader" class="ajax-loader" style="display:none"></div>
</div>

<div id="channel-scan-modal" class="modal hide keyboard modal-adjust" tabindex="-1" role="dialog" aria-labelledby="channel-scan-label" aria-hidden="true" data-backdrop="static">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="channel-scan-label"><?php echo _('Scan Channels'); ?></h3>
    </div>
    <div id="channel-scan-body" class="modal-body">
        <div>
            <label id="channel-scan-device"><?php echo _('Device to search channels for: '); ?></label>
            <select id="channel-scan-device-select" class="input-large"></select>
        </div>
        <p id="channel-scan-info"></p>
        
        <div class="modal-container">
            <table id="channel-scan-results" class="table table-hover" style="display:none"></table>
            <div id="channel-scan-results-none" class="alert" style="display:none"><?php echo _('No channels found'); ?></div>
            
            <div id="channel-scan-container"></div>
            
            <div id="channel-scan-overlay" class="modal-overlay"></div>
        </div>
    </div>
    <div class="modal-footer">
        <button id="channel-scan-cancel" class="btn" data-dismiss="modal" aria-hidden="true"><?php echo _('Cancel'); ?></button>
        <button id="channel-scan-start" class="btn btn-primary" style="border-radius: 4px;"><?php echo _('Scan'); ?></button>
    </div>
    <div id="channel-scan-loader" class="ajax-loader" style="display:none"></div>
</div>

<div id="channel-delete-modal" class="modal hide" tabindex="-1" role="dialog" aria-labelledby="channel-delete-label" aria-hidden="true" data-backdrop="static">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="channel-delete-label"></h3>
    </div>
    <div id="channel-delete-body" class="modal-body">
        <p><?php echo _('Deleting a channel is permanent.'); ?>
            <br><br>
            <?php echo _('If the representing channel is active and data gets written to an input, it will no longer be able to post data. '); ?>
            <?php echo _('The corresponding input and configurations will be removed, while feeds and all historic data is kept. '); ?>
            <?php echo _('To remove it, delete them manually afterwards.'); ?>
            <br><br>
            <?php echo _('Are you sure you want to proceed?'); ?>
        </p>
        <div id="channel-delete-loader" class="ajax-loader" style="display:none;"></div>
    </div>
    <div class="modal-footer">
        <button id="channel-delete-cancel" class="btn" data-dismiss="modal" aria-hidden="true"><?php echo _('Cancel'); ?></button>
        <button id="channel-delete-confirm" class="btn btn-primary"><?php echo _('Delete permanently'); ?></button>
    </div>
</div>

<script>
    $('#channel-config-container').load('<?php echo $path; ?>Modules/muc/Lib/configjs/config.php');
	$('#channel-scan-container').load('<?php echo $path; ?>Modules/muc/Lib/configjs/config.php');

    $(window).resize(function() {
        channel_dialog.adjustConfigModal();
        channel_dialog.adjustScanModal();
    });
</script>