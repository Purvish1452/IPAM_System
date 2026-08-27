var databaseMaintenance =
{
    // -------------------------------------------------------------------------Database maintanance init with render-------------------------------------------------------------------------------------------------//

    init : function ()
    {
        loaderUtil.showModalLoader();

        $("#settingMenuGrid").html('<div class=container-fluid><div class=content-section-panel><div class=row><div class="col-md-12 col-xs-12 col-lg-8"><div class="database-placeholder datatable-grid-panel"><div class=widget-main-box><div class=widget-header-box><div class=widget-header-title>Database Maintenance</div></div><form id=databaseMaintananceForm><div class=content-box-grid-panel id=dataRetentionModal><div class=row><div class="col-md-12 col-xs-12 col-lg-12"><label>Status</label><div class="col-md-12 col-xs-12 col-lg-20 margin-t-5 padding-0"><input class=k-radio type=radio name=status value=enable> <label for=enabled class="k-radio-label label-2">Enabled</label> <input class=k-radio type=radio name=status value=disable> <label for=status class="k-radio-label label-2">Disabled</label></div></div></div><div class=row><div class="col-xs-12 col-md-12 col-lg-12"> <label for="detailed-statistics">Detailed statistics will be maintained for the last </label> <input id="detailed-statistics" type="text" min="1" class="k-textbox" name="maintainedDays" style="width: 100%;" placeholder="day(s)" required="" validationmessage="Maintained Days is required" maxlength="3"></div>\n</div></div><div class="align-right footer-box-grid-panel margin-t-10"><div class=bottom-form-panel><button class="k-button k-button-icontext float-r k-grid-update k-primary"id=maintananceAddButton>Save</button> <button class="k-button k-button-icontext k-grid-cancel float-l margin-0"id=maintananceArchieveButton>Run Archieve</button> <button class="k-button k-button-icontext k-grid-cancel"id=maintananceCancelButton>Cancel</button></div></div></form></div></div></div></div><div class=row><div class="col-md-12 col-xs-12 col-lg-8"><div class="database-placeholder datatable-grid-panel"><div class=widget-main-box><div class=widget-header-box><div class=widget-header-title>Database Backup</div></div><form id=databaseBackupForm><div class=content-box-grid-panel id=dataBackupModal><div class=row><div class="col-md-12 col-xs-12 col-lg-12"style=margin-bottom:5px><label for=detailed-statistics>Path</label> <input type="text" class=k-textbox id=detailed-statistics id=backupPath name=backupPath required style=width:100% validationmessage="Path is required"></div></div><div class=row><div class="col-md-12 col-xs-12 col-lg-12 select-drop-menu"style=margin-bottom:5px><input class=k-checkbox id=scheduleStatus name=scheduleStatus type=checkbox value=true> <label for=automaticScanning style=margin-bottom:5px;margin-top:10px>Repeat</label><div id=autoScheduler></div></div></div></div><div class="align-right footer-box-grid-panel margin-t-10"><div class=bottom-form-panel><button class="k-button k-button-icontext float-r k-grid-update k-primary"id=backupAddButton>Save</button> <button class="k-button k-button-icontext k-grid-cancel float-l margin-0"id=backupRunButton>Run Backup</button> <button class="k-button k-button-icontext k-grid-cancel"id=backupCancelButton>Cancel</button></div></div></form></div></div></div></div>');

        var formContext = $("#databaseMaintananceForm");

        appManager.executeGETRequest({url: '/databaseMaintenance/1', container: formContext, callback: formManager.renderForm});

        flux.bindElementEvent({event:'keyup',element:'dataRetentionModal',selector:'#detailed-statistics'},flux.numberValidation);

        flux.bindKendoButtonClickEvent({element:'maintananceAddButton'},databaseMaintenance.onDatabaseMaintananceSaveButtonClick);

        flux.bindKendoButtonClickEvent({element:'maintananceArchieveButton'},databaseMaintenance.onDatabaseMaintananceArchieveButtonClick);

        flux.bindKendoButtonClickEvent({element:'maintananceCancelButton'},databaseMaintenance.onDatabaseMaintananceCancelButtonClick);

        flux.bindElementEvent({event:'change',element:'databaseBackupForm',selector:'#scheduleStatus' ,isEditable:true},databaseMaintenance.renderRepeatDropDown);

        appManager.executeGETRequest({url: '/databaseMaintenance/1', container: $("#databaseBackupForm"), callback: formManager.renderForm, postCallback: databaseMaintenance.afterBackupFormRender});

        flux.bindKendoButtonClickEvent({element:'backupAddButton'},databaseMaintenance.onBackupSaveButtonClick);

        flux.bindKendoButtonClickEvent({element:'backupRunButton'},databaseMaintenance.onBackupButtonClick);

        flux.bindKendoButtonClickEvent({element:'backupCancelButton'},databaseMaintenance.onDatabaseMaintananceCancelButtonClick);

        loaderUtil.hideModalLoader();
    },


    // ---------------------------------------------------------------------------Save Database maintanance-----------------------------------------------------------------------------------------------//

    onDatabaseMaintananceSaveButtonClick : function (event)
    {
        if(event)
        {
            event.event.preventDefault();

            var form = $("#databaseMaintananceForm");

            var param =formManager.serializeForm(form);

            var validator = form.kendoValidator().data("kendoValidator");

            if(validator.validate())
            {
                loaderUtil.showModalLoader();

                appManager.executePUTRequest({url: '/databaseMaintenance/1', container: form,  callback: databaseMaintenance.afterDatabaseMaintananceUpdated,params:param});

                loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);
            }
        }
    },

    // --------------------------------------------------------------------------Archieve database maintanance------------------------------------------------------------------------------------------------//

    onDatabaseMaintananceArchieveButtonClick : function (event)
    {
        if(event)
        {
            event.event.preventDefault();

            var form = $("#databaseMaintananceForm");

            var param = formManager.serializeForm(form);

            var validator = form.kendoValidator().data("kendoValidator");

            if (validator.validate())
            {
                loaderUtil.showModalLoader();

                appManager.executeDELETERequest({ url: '/databaseMaintenance/1', container: form, callback: databaseMaintenance.afterDatabaseMaintananceUpdated, params: param});

                loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);
            }
        }
    },

    // -----------------------------------------------------------------------------After database maintanance operations---------------------------------------------------------------------------------------------//

    afterDatabaseMaintananceUpdated : function (callbackContexts)
    {
        if(callbackContexts)
        {
            loaderUtil.hideCentralModalLoader();

            loaderUtil.hideModalLoader();

            if(callbackContexts.json.success == true)
            {
                notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"success"});
            }
            else
            {
                notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"error"});
            }

            databaseMaintenance.init();
        }
    },

    // --------------------------------------------------------------------------------Reinit database maintanance on cancel btn click------------------------------------------------------------------------------------------//

    onDatabaseMaintananceCancelButtonClick : function (event)
    {
        if(event)
        {
            event.event.preventDefault();

            databaseMaintenance.init();
        }
    },

    onBackupSaveButtonClick : function (event)
    {
        if(event)
        {
            event.event.preventDefault();

            var form = $("#databaseBackupForm");

            var param =formManager.serializeForm(form);

            var validator = form.kendoValidator().data("kendoValidator");

            if(validator.validate())
            {
                loaderUtil.showModalLoader();

                appManager.executePUTRequest({url: '/databaseBackup/1', container: form,  callback: databaseMaintenance.afterDatabaseMaintananceUpdated,params:param});

                loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);
            }
        }
    },

    onBackupButtonClick : function (event)
    {
        if(event)
        {
            event.event.preventDefault();

            var form = $("#databaseBackupForm");

            var param = formManager.serializeForm(form);

            var validator = form.kendoValidator().data("kendoValidator");

            if (validator.validate())
            {
                loaderUtil.showModalLoader();

                appManager.executePUTRequest({ url: '/runDatabaseBackup/1', container: form, callback: databaseMaintenance.afterDatabaseMaintananceUpdated, params: param});

                loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);
            }
        }
    },

    afterBackupFormRender: function (context)
    {
        var autoScheduler = $('#scheduleStatus');

        if(autoScheduler.prop('checked'))
        {
            autoScheduler.trigger('change');

            var durationValue = context.json.data.duration;

            var schedulerValue = context.json.data.scheduleHour;

            var scopeDropDown = $("#duration").data('kendoDropDownList');

            var scopeDuration = $("#scheduleHour").data('kendoDropDownList');

            scopeDropDown.value(durationValue);

            scopeDuration.value(schedulerValue);
        }
    },

    renderRepeatDropDown : function (event)
    {
        if(event)
        {
            var repeatContent = $("#autoScheduler");

            var checkedValue = $(event.currentTarget).context.checked;

            if(checkedValue)
            {
                repeatContent.html('<div class="row"><div class="col-xs-12 col-md-12 col-lg-6"><select id="duration" name="duration" class="k-dropdown" required/></div><div class="col-xs-12 col-md-12 col-lg-6"><select id="scheduleHour" name="scheduleHour" class="k-dropdown" /></div></div>');

                var scopeDropDown = $("#duration");

                var scopeDuration = $("#scheduleHour");

                scopeDropDown.kendoDropDownList({
                    dataSource:[{name: "Day", id: "Days" },{name: "Month", id: "Month" }],
                    dataTextField: "name",
                    dataValueField: "id"
                }).data("kendoDropDownList");

                scopeDuration.kendoDropDownList({
                    dataSource:[
                        {text:'1',value:'1',id: 'Days'},{text:'2',value:'2',id: 'Days'},{text:'3',value:'3',id: 'Days'}, {text:'4',value:'4',id: 'Days'},
                        {text:'5',value:'5',id: 'Days'}, {text:'6',value:'6',id: 'Days'},{text:'7',value:'7',id: 'Days'}, {text:'8',value:'8',id: 'Days'},
                        {text:'9',value:'9',id: 'Days'},{text:'10',value:'10',id: 'Days'},{text:'11',value:'11',id: 'Days'}, {text:'12',value:'12',id: 'Days'},
                        {text:'13',value:'13',id: 'Days'},{text:'14',value:'14',id: 'Days'},{text:'15',value:'15',id: 'Days'},{text:'16',value:'16',id: 'Days'},
                        {text:'17',value:'17',id: 'Days'}, {text:'18',value:'18',id: 'Days'}, {text:'19',value:'19',id: 'Days'},{text:'20',value:'20',id: 'Days'},
                        {text:'21',value:'21',id: 'Days'},{text:'22',value:'22',id: 'Days'},{text:'23',value:'23',id: 'Days'},{text:'24',value:'24',id: 'Days'},
                        {text:'25',value:'25',id: 'Days'},{text:'26',value:'26',id: 'Days'},{text:'27',value:'27',id: 'Days'},{text:'28',value:'28',id: 'Days'},
                        {text:'29',value:'29',id: 'Days'},{text:'30',value:'30',id: 'Days'},{text:'31',value:'31',id: 'Days'},
                        {text:'1',value:'1',id: 'Month'},{text:'2',value:'2',id: 'Month'},{text:'3',value:'3',id: 'Month'},{text:'4',value:'4',id: 'Month'},
                        {text:'5',value:'5',id: 'Month'},{text:'6',value:'6',id: 'Month'}, {text:'7',value:'7',id: 'Month'}, {text:'8',value:'8',id: 'Month'},
                        {text:'9',value:'9',id: 'Month'},{text:'10',value:'10',id: 'Month'},{text:'11',value:'11',id: 'Month'},{text:'12',value:'12',id: 'Month'}],
                    cascadeFrom: "duration",
                    dataTextField: "text",
                    dataValueField: "value"
                }).data("kendoDropDownList");
            }
            else
            {
                repeatContent.html('<div id="autoScheduler"></div>')
            }
        }
    },
};