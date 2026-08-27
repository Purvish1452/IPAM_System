var configureAlertManager =
{
    // -------------------------------------------------------------------------Configure Alert init-------------------------------------------------------------------------------------------------//

    init : function ()
    {
        loaderUtil.showModalLoader();

        $("#settingMenuGrid").html(`<div class="container-fluid"><div class="content-section-panel"><div class="row"><div class="col-lg-8 col-md-8 col-xs-8"><div class="datatable-grid-panel"><div class="widget-main-box" style="width:80%"><div class="widget-header-box"><div class="widget-header-title">Configure Alert</div></div><form id="alertConfigureForm"><div class="content-box-grid-panel" style="padding:0"><div class="k-grid k-widget k-display-block k-reorderable k-editable" id="alertConfigGrid" data-role="grid"><div class="k-grid-header" style="padding-right:6px"><div class="k-grid-header-wrap k-auto-scrollable"><table role="grid"><colgroup><col><col style="width:20%"></colgroup><thead role="rowgroup"><tr role="row"><th scope="col" role="columnheader" class="k-header">Alert Type</th><th scope="col" role="columnheader" class="k-header">Status</th></tr></thead></table></div></div><div class="k-grid-content k-auto-scrollable"><table role="grid"><colgroup><col><col style="width:20%"></colgroup><tbody role="rowgroup"><tr role="row"><td role="gridcell" style="position:relative">IP Utilization of the Subnet is lower than<span class="clickable-text" id="ipUtilizationBelowText" data-input="ipUtilizationBelow">%</span></td><td role="gridcell"><label class="toggle-label"><input id="ipUtilizationBelowFlag" name="ipUtilizationBelowFlag" type="checkbox"><span class="back"><span class="toggle"></span></span></label></td></tr><tr class="k-alt" role="row"><td role="gridcell" style="position:relative">IP Utilization of the Subnet is greater than<span class="clickable-text" id="ipUtilizationText" data-input="ipUtilization">%</span></td><td role="gridcell"><label class="toggle-label"><input id="ipUtilizationFlag" name="ipUtilizationFlag" type="checkbox"><span class="back"><span class="toggle"></span></span></label></td></tr><tr role="row"><td role="gridcell" style="position:relative">A Change in MAC-IP combination is detected<span class="clickable-text" id="macIpChangeToggleText">(Exclude MACs)</span><td role="gridcell"><label class="toggle-label"><input id="macIpChangeFlag" name="macIpChangeFlag" type="checkbox"><span class="back"><span class="toggle"></span></span></label></td></tr><tr class="k-alt" role="row"><td role="gridcell">Rogue IP is detected during scanning</td><td role="gridcell"><label class="toggle-label"><input id="rogueDetection" name="rogueDetection" type="checkbox"><span class="back"><span class="toggle"></span></span></label></td></tr><tr role="row"><td role="gridcell">State of an IP Address is changed from "Available to Used" or from "Transient to Available"</td><td role="gridcell"><label class="toggle-label"><input id="ipStateChange" name="ipStateChange" type="checkbox"><span class="back"><span class="toggle"></span></span></label></td></tr><tr class="k-alt" role="row"><td role="gridcell">Reverse Lookup Failed</td><td role="gridcell"><label class="toggle-label"><input id="reverseLookupFailed" name="reverseLookupFailed" type="checkbox"><span class="back"><span class="toggle"></span></span></label></td></tr><tr role="row"><td role="gridcell">Forward Lookup Failed</td><td role="gridcell"><label class="toggle-label"><input id="forwardLookupFailed" name="forwardLookupFailed" type="checkbox"><span class="back"><span class="toggle"></span></span></label></td></tr><tr class="k-alt" role="row"><td role="gridcell">Forward Lookup returns a different IP</td><td role="gridcell"><label class="toggle-label"><input id="forwardLookupMismatch" name="forwardLookupMismatch" type="checkbox"><span class="back"><span class="toggle"></span></span></label></td></tr><tr role="row"><td role="gridcell">Reservation status of an IP Address is changed</td><td role="gridcell"><label class="toggle-label"><input id="ipReservationChange" name="ipReservationChange" type="checkbox"><span class="back"><span class="toggle"></span></span></label></td></tr><tr class="k-alt" role="row"><td role="gridcell">IP Address conflict is detected</td><td role="gridcell"><label class="toggle-label"><input id="ipConflict" name="ipConflict" type="checkbox"><span class="back"><span class="toggle"></span></span></label></td></tr><tr role="row"><td role="gridcell">New subnets have been discovered</td><td role="gridcell"><label class="toggle-label"><input id="newSubnetsDiscovered" name="newSubnetsDiscovered" type="checkbox"><span class="back"><span class="toggle"></span></span></label></td></tr></tbody></table></div></div></div><div class="align-right footer-box-grid-panel"><div class="bottom-form-panel"><button class="k-button k-button-icontext float-r k-grid-update k-primary" id="alertSaveButton">Save</button></div></div></form></div></div></div></div></div></div>`);

        flux.bindKendoButtonClickEvent({ element: 'ipUtilizationBelowText' }, function(event) {
            configureAlertManager.onTextClick(event, 'IP Utilization',
                'Alert when IP utilization of the subnet goes below',
                'ipUtilizationBelow', 'Invalid Percentage',
                ' % ', '^(100|[1-9]?[0-9])$', false);
        });

        flux.bindKendoButtonClickEvent({ element: 'ipUtilizationText' }, function(event) {
            configureAlertManager.onTextClick(event, 'IP Utilization',
                'Alert when IP utilization of the subnet goes above',
                'ipUtilization', 'Invalid Percentage',
                ' % ', '^(100|[1-9]?[0-9])$', false);
        });

        flux.bindKendoButtonClickEvent({ element: 'macIpChangeToggleText' }, function(event) {
            configureAlertManager.onTextClick(event, 'Exclude MAC Address',
                'Specify the MAC address whose change in assigned IP address is to be ignored.',
                'macIpChange', 'MAC Address Required',
                'Use comma as separator for multiple values', '.*', true);
        });

        flux.bindKendoButtonClickEvent({element: 'alertSaveButton'},configureAlertManager.onAlertSaveButtonClick);

        flux.bindElementEvent({event:'keyup',element:'settingMenuGrid',selector:'#ipUtilization'},flux.numberValidation);

        var formContext = $("#alertConfigureForm");

        appManager.executeGETRequest({url: '/configureAlert/', container: formContext, callback: configureAlertManager.onRenderAlertConfiguration});

        loaderUtil.hideModalLoader();
    },

    onRenderAlertConfiguration : function (callbackContexts)
    {
        if(callbackContexts.json.data !== undefined)
        {
            Object.keys(callbackContexts.json.data).forEach(key =>
            {
                var value = callbackContexts.json.data[key];

                var element = $(`span[data-input='${key}']`);

                if (element.length)
                {
                    element.text(value +  "%");
                }
            });

            formManager.renderForm(callbackContexts);
        }
    },

    onTextClick: function (event, title, label, inputId, validationMessage, placeholder, pattern, isTextArea)
    {
        if (event)
        {
            event.event.preventDefault();

            var categoryModal = $("#innerModal");

            categoryModal.width('500px');

            flux.kendoWindowSetOption({ height: '155px', kendoWindow: categoryModal });

            $('#innerModal_wnd_title').html(title);

            var inputField = isTextArea
                ? `<textarea id="${inputId}" name="${inputId}" class="k-textbox k-input" required 
                         validationMessage="${validationMessage}" title="${placeholder}" 
                         placeholder="${placeholder}" rows="4"></textarea>`
                : `<input id="${inputId}" name="${inputId}" type="text" 
                      class="k-textbox k-input" required 
                      validationMessage="${validationMessage}" maxlength="3" 
                      pattern="${pattern}" title="Enter a value between 0-100" 
                      placeholder="${placeholder}" />`;

            categoryModal.data("kendoWindow").content(`<form id="saveForm"><div class="content-box-grid-panel margin-t-20"><div class="row"><div class="col-xs-12 col-md-12 col-lg-12"><label for="${inputId}" class="form-label">${label}</label>${inputField}<span class="k-invalid-msg" data-for="${inputId}"></span></div></div></div><div class="footer-box-grid-boxs margin-t-10 align-right"><div class="bottom-form-panel"><button type="submit" class="k-button k-button-icontext k-primary float-r" id="save">Save</button><button type="button" class="k-button k-button-icontext k-grid-cancel float-l" id="cancel">Cancel</button></div></div></form>`);

            if(!isTextArea)
            {
                var percentage = $("#" + inputId + "Text").text();

                $("#" + inputId).val(percentage.substring(0, percentage.length - 1));
            }
            else
            {
                var form = $("#saveForm");

                appManager.executeGETRequest({url: '/configureAlert/', container: form, callback: formManager.renderForm});
            }

            flux.bindKendoButtonClickEvent({ element: 'save', kendoModal: categoryModal }, configureAlertManager.saveEntity);

            flux.bindKendoButtonClickEvent({ element: 'cancel', kendoModal: categoryModal }, flux.closeKendoModal);
        }
    },

    saveEntity : function (event)
    {
        if (event)
        {
            event.event.preventDefault();

            var kendoEvent = event.sender.options.prefix.kendoModal;

            var form = $("#saveForm");

            var param = formManager.serializeForm(form);

            var validator = form.kendoValidator().data("kendoValidator");

            if (validator.validate())
            {
                const alertData = [];

                Object.keys(param).forEach(key =>
                {
                    alertData.push({
                        alertKey: key,
                        alertValue: param[key]});
                });

                loaderUtil.showModalLoader();

                appManager.executePUTRequest({
                    url: '/configureAlert/',
                    container: kendoEvent,
                    isInit:true,
                    callback: configureAlertManager.afterAlertSave,
                    params: alertData
                });
            }
        }
    },

    afterAlertSave : function (callbackContexts)
    {
        if(callbackContexts)
        {
            if(callbackContexts.json.success === true)
            {
                callbackContexts.params.forEach(alert =>
                {
                    var element = $(`span[data-input='${alert.alertKey}']`);

                    if (element.length)
                    {
                        element.text(alert.alertValue + "%");
                    }
                });

                notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"success"});
            }
            else
            {
                notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"error"});
            }
            var modal = callbackContexts.container;

            modal.data("kendoWindow").close();

            loaderUtil.hideModalLoader();

            loaderUtil.hideCentralModalLoader();
        }
    },


    onAlertSaveButtonClick : function (event)
    {
        if (event)
        {
            event.event.preventDefault();

            var form = $("#alertConfigureForm");

            var param = formManager.serializeForm(form);

            var checkbox = configureAlertManager.getCheckboxValues(form);

            params = { ...param, ...checkbox };

            var validator = form.kendoValidator().data("kendoValidator");

            if (validator.validate())
            {
                const alertData = [];

                Object.keys(params).forEach(key =>
                {
                    alertData.push({
                        alertKey: key,
                        alertValue: params[key]
                    });
                });

                loaderUtil.showModalLoader();

                appManager.executePUTRequest({
                    url: '/configureAlert/',
                    container: form,
                    callback: configureAlertManager.afterAlertConfigurationSave,
                    params: alertData
                });
            }
        }
    },

    // -------------------------------------------------------------------------After Save Alert Configuration-------------------------------------------------------------------------------------------------//

    afterAlertConfigurationSave : function (callbackContexts)
    {
        if(callbackContexts)
        {
            if(callbackContexts.json.success === true)
            {
                notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"success"});
            }
            else
            {
                notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"error"});
            }

            loaderUtil.hideModalLoader();

            loaderUtil.hideCentralModalLoader();
        }
    },

    getCheckboxValues: function (form)
    {
        var params = {};

        var elements = $(form).find('input[type=checkbox]');

        elements.each(function ()
        {
            var element = $(this);

            var elementName = element.attr('name');

            if (elementName !== undefined && !element.prop('disabled'))
            {
                params[elementName] = element.prop('checked') ? "true" : "false";
            }
        });

        return params;
    }
};