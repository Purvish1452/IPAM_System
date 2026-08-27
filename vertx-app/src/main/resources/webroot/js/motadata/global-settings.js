/**
 * Created by hardik on 6/6/18.
 */
var globalSettings =
    {
        // ------------------------------------------------------------------------Init & Render Global settings--------------------------------------------------------------------------------------------------//

        init : function ()
        {
            loaderUtil.showModalLoader();

            $("#settingMenuGrid").html('<div class="container-fluid"><div class="content-section-panel"><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"><div class="datatable-grid-panel"><div class="widget-main-box"><div class="widget-header-box"><div class="widget-header-title">Global Settings</div></div><form id="globalSettingsForm"><div class="content-box-grid-panel"><div class="row"><div class="col-xs-12 col-md-12 col-lg-12"> <label>Logging Level</label><div class="col-xs-12 col-md-12 col-lg-12 padding-0 margin-t-5"> <input type="radio" name="loggingLevel" class="k-radio" value="0" checked/> <label class="k-radio-label label-2" for="loggingLevel">Trace</label> <input type="radio" name="loggingLevel" class="k-radio" value="1"/> <label class="k-radio-label label-2" for="loggingLevel ">Debug</label> <input type="radio" name="loggingLevel" class="k-radio" value="2"/> <label class="k-radio-label label-2" for="loggingLevel ">Info</label> <input type="radio" name="loggingLevel" class="k-radio" value="3"/> <label class="k-radio-label label-2" for="loggingLevel ">Warning</label> <input type="radio" name="loggingLevel" class="k-radio" value="4"/> <label class="k-radio-label label-2" for="loggingLevel ">Error</label></div></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-12"> <label>Mode</label><div class="col-xs-12 col-md-12 col-lg-12 padding-0 margin-t-5"> <input type="radio" name="cssMode" class="k-radio" value="1" checked/> <label class="k-radio-label label-2" for="cssMode">Light</label> <input type="radio" name="cssMode" class="k-radio" value="2" checked/> <label class="k-radio-label label-2" for="cssMode">Dark</label></div></div></div></div><div class="footer-box-grid-panel margin-t-10 align-right"><div class="bottom-form-panel"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="globalAddButton">Update</button></div></div></form></div></div></div></div></div></div>');

            var formContext = $("#globalSettingsForm");

            appManager.executeGETRequest({url:'/globalSetting/',callback:formManager.renderForm,container: formContext});

            flux.bindKendoButtonClickEvent({element: 'globalAddButton'},globalSettings.onGlobalSettingAddButtonClick);

            loaderUtil.hideModalLoader();
        },

        // ---------------------------------------------------------------------------Save Global settings-----------------------------------------------------------------------------------------------//

        onGlobalSettingAddButtonClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var form = $('#globalSettingsForm');

                var param = formManager.serializeForm(form);

                appManager.executePUTRequest({url:'/globalSetting/1',container:form,callback:globalSettings.afterGlobalSettingApplied,params:param});
            }
        },

        afterGlobalSettingApplied : function (context)
        {
            if(context.json.success == true)
            {
                location.reload();

                notification.showNotification({notificationTitle:context.json.message,notificationType:"success"});
            }
            else
            {
                notification.showNotification({notificationTitle:context.json.message,notificationType:"error"});
            }
        }
    };