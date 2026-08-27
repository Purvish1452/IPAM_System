var mailServerConfiguration =
    {
        serverId : {PRIMARY:1 , SECONDARY:2},

        // ---------------------------------------------------------------------------Mail server init & render-----------------------------------------------------------------------------------------------//

        init : function ()
        {
            loaderUtil.showModalLoader();

           // $("#settingMenuGrid").html('<div class="container-fluid"><div class="content-section-panel"><div class="row"><div class="col-xs-12 col-md-12 col-lg-7"><div class="datatable-grid-panel"><div class="widget-main-box"><div class="widget-header-box"><div class="widget-header-title">Mail Server Configuration</div></div><form id="mailConfigurationForm"><div class="content-box-grid-panel" id="mailModal"><div class="row"><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <label for="mailType">Mail Server</label> <select id="mailType" name="mailType" class="k-dropdown" style="width: 100%;"></select></div></div><div class="hideMailForm"><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"> <label for="mailHost">SMTP Server</label> <input id="mailHost" name="mailHost" type="text" class="k-textbox" required validationMessage="Server name required" style="width: 100%;" maxlength="100" /></div><div class="col-xs-12 col-md-12 col-lg-3"> <label for="mailPort">SMTP Port</label> <input id="mailPort" name="mailPort" type="text" class="k-textbox" required validationMessage="Port is required" style="width: 100%;" maxlength="5" /></div><div class="col-xs-12 col-md-12 col-lg-3"> <label for="mailTimeout">TimeOut(Sec)</label><i class="fa fa-clock-o inputNearShow"></i><input id="mailTimeout" name="mailTimeout" type="text" class="k-textbox" style="width: 100%;" maxlength="2" pattern="[0-9]+" /> <span> * Max 60 sec</span></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"> <label for="mailFromEmail">From Email ID (optional)</label> <input id="mailFromEmail" name="mailFromEmail" type="email" class="k-textbox" required validationMessage="Email format is not valid" style="width: 100%;" /></div><div class="col-xs-12 col-md-12 col-lg-6"> <label for="mailToEmail">To Email ID</label> <input id="mailToEmail" name="mailToEmail" type="email" class="k-textbox"required validationMessage="Email format is not valid" style="width: 100%;"/></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-12"> <input type="radio" name="mailProtocol" id="connection1" class="k-radio" value="ssl"> <label class="k-radio-label" for="connection1">SSL Enabled</label> <input type="radio" name="mailProtocol" id="connection2" class="k-radio" value="tls"> <label class="k-radio-label" for="connection2">TLS Enabled</label> <input type="radio" name="mailProtocol" id="connection3" class="k-radio" checked="checked" value="none"> <label class="k-radio-label" for="connection3">None</label></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"> <label for="mailUserName">User Name</label> <input id="mailUserName" name="mailUserName" type="text" class="k-textbox" style="width: 100%;" maxlength="50" /></div><div class="col-xs-12 col-md-12 col-lg-6"> <label for="mailPassword">Password</label> <input id="mailPassword" name="mailPassword" type="password" class="k-textbox" style="width: 100%;" maxlength="50" /></div></div></div></div><div id="appendMailData"></div></form></div></div></div></div></div></div>');
            $("#settingMenuGrid").html('<div class="container-fluid"><div class="content-section-panel"><div class="row"><div class="col-xs-12 col-md-12 col-lg-7"><div class="datatable-grid-panel"><div class="widget-main-box"><div class="widget-header-box"><div class="widget-header-title">Mail Server Configuration</div></div><form id="mailConfigurationForm"><div class="content-box-grid-panel" id="mailModal"><div class="row"><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"><label for="mailType">Mail Server</label><select id="mailType" name="mailType" class="k-dropdown" style="width:100%"></select></div></div><div class="hideMailForm"><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"><label for="mailHost">SMTP Server</label><input id="mailHost" name="mailHost" type="text" class="k-textbox" required validationmessage="Server name required" style="width:100%" maxlength="100"></div><div class="col-xs-12 col-md-12 col-lg-3"><label for="mailPort">SMTP Port</label><input id="mailPort" name="mailPort" type="text" class="k-textbox" required validationmessage="Port is required" style="width:100%" maxlength="5"></div><div class="col-xs-12 col-md-12 col-lg-3"><label for="mailTimeout">TimeOut(Sec)</label><i class="fa fa-clock-o inputNearShow"></i><input id="mailTimeout" name="mailTimeout" type="text" class="k-textbox" style="width:100%" maxlength="2" pattern="[0-9]+" min="0" max="60"><span>* Max 60 sec</span></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"><label for="mailFromEmail">From Email ID </label><input id="mailFromEmail" name="mailFromEmail" type="email" class="k-textbox" required validationmessage="Email format is not valid" style="width:100%"></div><div class="col-xs-12 col-md-12 col-lg-6"><label for="mailToEmail">To Email ID</label><input id="mailToEmail" name="mailToEmail" type="email" class="k-textbox" required validationmessage="Email format is not valid" style="width:100%"></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-12"><input type="radio" name="mailProtocol" id="connection1" class="k-radio" value="ssl"><label class="k-radio-label" for="connection1">SSL Enabled</label><input type="radio" name="mailProtocol" id="connection2" class="k-radio" value="tls"><label class="k-radio-label" for="connection2">TLS Enabled</label><input type="radio" name="mailProtocol" id="connection3" class="k-radio" checked="checked" value="none"><label class="k-radio-label" for="connection3">None</label></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"><label for="mailUserName">Name</label><input id="mailUserName" name="mailUserName" type="text" class="k-textbox" required validationmessage="Invalid Name"  style="width:100%" maxlength="50"></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"><label for="mailUserId">User Name</label><input id="mailUserId" name="mailUserId" type="text" class="k-textbox" required validationmessage="Invalid Username" style="width:100%" maxlength="50"></div><div class="col-xs-12 col-md-12 col-lg-6"><label for="mailPassword">Password</label><input id="mailPassword" name="mailPassword" type="password" class="k-textbox" style="width:100%" maxlength="50"></div></div></div></div><div id="appendMailData"></div></form></div></div></div></div></div></div>');

            var selectId = $("#mailType");

            var data = [{text:'Primary Server',value:'Primary'},{text:'Secondary Server',value:'Secondary'}];

            selectId.kendoDropDownList(
                {
                    dataTextField: "text",
                    dataValueField: "value",
                    dataSource: data,
                    change: mailServerConfiguration.changeMailServer,
                    serverFiltering: true
                }
            );
            selectId.getKendoDropDownList().trigger("change");

            //selectId.data('kendoDropDownList').refresh();

            loaderUtil.hideModalLoader();
        },

        // ---------------------------------------------------------------------On mail server type change-----------------------------------------------------------------------------------------------------//

        changeMailServer : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var elementValue = $('#mailType').val();

                var formContext = $("#mailConfigurationForm");

                if(elementValue == "Primary")
                {
                    mailServerConfiguration.renderMailForm({eventId:1,container:formContext});
                }
                if (elementValue == "Secondary")
                {
                    mailServerConfiguration.renderMailForm({eventId:2,container:formContext});
                }
            }
        },

        // ------------------------------------------------------------------------Render mail form data--------------------------------------------------------------------------------------------------//

        renderMailForm : function (context)
        {
            if(context)
            {
                var id = context.eventId;

                var formContext = context.container;

                appManager.executeGETRequest({url: '/mail/'+id, container: formContext, callback: formManager.renderForm, params: {userId : id}});

                formManager.appendFormElements('appendMailData','<div class="footer-box-grid-panel margin-t-10 align-right padding-l-1"><div class="bottom-form-panel"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="mailServerAddButton">Save</button> <button class="k-button k-button-icontext k-grid-cancel float-l k-primary margin-0" id="mailServerTestButton">Test Mail</button> <button class="k-button k-button-icontext k-grid-cancel" id="mailServerCancelButton">Cancel</button></div></div>');

                flux.bindElementEvent({event:'keyup',element:'mailModal',selector:'#mailPort'},flux.numberValidation);

                flux.bindElementEvent({event:'keyup',element:'mailModal',selector:'#mailTimeout'},flux.maxNumberValidation);

                flux.bindKendoButtonClickEvent({element:'mailServerAddButton',ID:id},mailServerConfiguration.onMailSaveButtonClick);

                flux.bindKendoButtonClickEvent({element:'mailServerTestButton'},mailServerConfiguration.onMailTestButtonClick);

                flux.bindKendoButtonClickEvent({element:'mailServerCancelButton'},mailServerConfiguration.onMailServerCancelButtonClick);
            }
        },

        // -------------------------------------------------------------------------Mail test event-------------------------------------------------------------------------------------------------//

        onMailTestButtonClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var form = $("#mailConfigurationForm");

                var param = formManager.serializeForm(form);

                var validator = form.kendoValidator().data("kendoValidator");

                if (validator.validate())
                {
                    appManager.executePOSTRequest({ url: '/mail/', container: form, callback: mailServerConfiguration.afterMailServerTest, params: param});

                    loaderUtil.showCentralModalLoader(appConstant.MailServerTestMessage);
                }
            }
        },

        afterMailServerTest : function (callbackContexts)
        {
            loaderUtil.hideCentralModalLoader();

            if(callbackContexts.json.success == true)
            {
                notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"success"});
            }
            else
            {
                notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"error"});
            }
        },

        // ----------------------------------------------------------------------------Mail save event----------------------------------------------------------------------------------------------//

        onMailSaveButtonClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var id = event.sender.options.prefix.ID;

                var form = $("#mailConfigurationForm");

                var param =formManager.serializeForm(form);

                var validator = form.kendoValidator().data("kendoValidator");

                if(validator.validate())
                {
                    appManager.executePUTRequest({url: '/mail/'+id, container: form,  callback: mailServerConfiguration.afterMailServerUpdated, params: param});

                    loaderUtil.showCentralModalLoader(appConstant.MailServerAddMessage);
                }
            }
        },

        afterMailServerUpdated : function (callbackContexts)
        {
            loaderUtil.hideCentralModalLoader();

            if(callbackContexts.json.success == true)
            {
                notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"success"});
            }
            else
            {
                notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"error"});
            }

            mailServerConfiguration.init();
        },

        // -------------------------------------------------------------------------------Reinit mail server on cancel btn click-------------------------------------------------------------------------------------------//

        onMailServerCancelButtonClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                mailServerConfiguration.init();
            }
        }
    };