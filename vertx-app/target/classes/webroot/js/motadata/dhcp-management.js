var dhcpManagement =
    {
        credentialType : {WINDOWS:0 ,CISCO:1},

        // ----------------------------------------------------------------------Dhcp Management init----------------------------------------------------------------------------------------------------//
        
        init: function ()
        {
            loaderUtil.showModalLoader();

            loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

            var header = $("#header_panel");

            header.empty();

            header.html('<div class="title-inner-box"> Settings </div>');

            $("#settingMenuGrid").html('<div class="container-fluid"><div class="content-section-panel"><div class="row"><div class="col-xs-12 col-md-12 col-lg-12"><div class="datatable-grid-panel"><div class="widget-main-box"><div class="widget-header-box"><div class="widget-header-title">DHCP Management</div><div class="widget-header-action" id="gridAdd"><button id="addDHCPModal" class="primery-btn" title="Add"> <i class="fa fa-plus"></i> Add New DHCP Server </button></div></div><div id="notification"></div><div class="widget-content-box" id="dhcpManagementTable"></div></div></div></div></div></div></div>');

            navigationManager.addHistory("navigation=settings~~settingsTab=dhcpManagement");

            var dhcpManagementTable = $("#dhcpManagementTable");

            var callbackContext = [];

            callbackContext['id'] = dhcpManagementTable;

            callbackContext['name'] = "DHCP";

            callbackContext['dataSource'] =
                {
                    pageSize: 10,
                    transport: {
                        read: function (options)
                        {
                            appManager.executeGETRequest({url: '/dhcpCredential/',container:options,callback:dhcpManagement.renderDHCPUtilzationGrid});
                        },
                        update: {
                            url: "/editDHCP",
                            dataType: "json"
                        },
                        create: {
                            url: "/addDHCP",
                            dataType: "json"
                        },
                        destroy:function (options)
                        {
                            var id = options.data.models[0].id;

                            appManager.executeDELETERequest({ url: '/dhcpCredential/'+id,container:options, callback: dhcpManagement.afterDhcpServerDeleted, gridId:dhcpManagementTable});
                        },
                        parameterMap: function (options, operation) {
                            if (operation !== "read" && options.models) {
                                return {models: kendo.stringify(options.models)};
                            }
                        }
                    },
                    batch: true,
                    schema: {
                        model: {
                            id: "id",
                            fields: {
                                type:{type:"string"},
                                credentialName: {type: "string"},
                                hostAddress: {type: "string"},
                                port: {type:"string"},
                                lastScanTime: {type:"string"},
                                scheduleHour: {type: "integer"},
                                duration:{type:"string"},
                                createdBy:{type:"string"}
                            }
                        }
                    }
                };

            callbackContext['columns'] = [
                {field:"id",hidden:true},
                {field: "hostAddress", template : "<a data-uid='#: id #' data-link='hostAddress' data-value='#: credentialName #' title='#:hostAddress#'>#: hostAddress #</a>",title: "Server Address",width:'13%'},
                {field: "port", title: "Port",width:'10%',template:'# if (port) { # <span title="#:port#">#: port # </span># } else { #<span></span># } #'},
                {field:"type",title:"Type",width:'10%',template:'# if (type) { # <span title="#:type#">#: type # </span># } else { #<span></span># } #'},
                {field: "credentialName", title: "Credential Name",width:"15%",template:'# if (credentialName) { # <span title="#:credentialName#">#: credentialName # </span># } else { #<span></span># } #'},
                {field: "lastScanTime", title: "Last Scan Time",template:'# if (lastScanTime) { # <span title="#:lastScanTime#">#: lastScanTime # </span># } else { #<span></span># } #'},
                {field: "scheduleHour", title: "Schedule Time",template:"#if(duration==null){#<span>N/A</span>#}else{#<span title='#:scheduleHour # #:duration#'>#:scheduleHour # #:duration#</span>#}#",width:'15%'},
                {field: "createdBy", title: "Created By",width:'15%',template:'# if (createdBy) { # <span title="#:createdBy#">#: createdBy # </span># } else { #<span></span># } #'},
                {
                    width:'7%',
                    command: [
                        {name: "Edit", text: "", iconClass: "fa fa-pencil",title:"Edit",click:function (e) {

                            e.preventDefault();

                            if(appManager.validatePermission() == true)
                            {
                                loaderUtil.showModalLoader();

                                loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

                                var tr = $(e.target).closest("tr");

                                var data = this.dataItem(tr);

                                var editId = data.id;

                                appManager.executeGETRequest({url: '/dhcpCredential/'+editId, editId:editId, callback:dhcpManagement.renderEditModal});
                            }
                        }},
                        {name: "Delete", text: "", iconClass: "fa fa-trash",title:"Delete",click:function (e)
                        {
                            e.preventDefault();

                            if(appManager.validatePermission() == true)
                            {
                                var tr = $(e.target).closest("tr");

                                var data = this.dataItem(tr);

                                var context = {};

                                context.data = data;

                                context.windowId = 'deleteModal';

                                context.title = 'Do you really want to delete the selected DHCP Server?';

                                context.yesButtonEvent = dhcpManagement.deleteConfirmationPrompt;

                                context.noButtonEvent = table.closeDeleteWindow;

                                context.grid = "DHCP";

                                context.coSequences = 'Warning : Related all subnet addresses will be deleted with this DHCP server !';

                                flux.deleteWindowExecute(context);
                            }
                        }}
                    ]}];

            dhcpManagementTable.kendoTooltip({ filter: ".k-grid-Edit", content: "Edit", showAfter: 1000, position: "bottom"});

            $("#gridAdd").kendoTooltip({ filter: "button[title]" , showAfter: 1000, position: "bottom"});

            dhcpManagementTable.kendoTooltip({ filter: ".k-grid-Delete", content: "Delete", showAfter: 1000, position: "bottom"});

            table.renderTable(callbackContext);

            flux.bindEvent({element: 'dhcpManagementTable', selector:'a[data-link=hostAddress]',breadCrumbMenu:"DHCP Management"},dhcpServerStatistics.init);

            flux.bindEvent({element:'addDHCPModal'},dhcpManagement.onAddDHCPButtonClick);

            loaderUtil.hideModalLoader();
        },

        // ---------------------------------------------------------------------------Render DHCP Grid-----------------------------------------------------------------------------------------------//
        
        renderDHCPUtilzationGrid : function (context)
        {
            if(context!=null && context!=undefined)
            {
                var result = context.json.data;

                if(result!=null && result !=undefined && context.json.success == true)
                {
                    context.container.success(result);
                }
                else
                {
                    context.container.success("");

                    $(".k-grid-content").html(appConstant.NoDataSpan);
                }
            }
            loaderUtil.hideCentralModalLoader();
        },
        
        // -------------------------------------------------------------------------------Add dhcp modal event-------------------------------------------------------------------------------------------//
        
        onAddDHCPButtonClick : function (event)
        {
            if(event)
            {
                event.preventDefault();

                if(appManager.validatePermission() == true)
                {
                    var windowId = $("#addModal");

                    windowId.width('750px');

                    flux.kendoWindowSetOption({ kendoWindow: windowId});

                    $('#addModal_wnd_title').html('Add New DHCP Server');

                    var dhcpModal = windowId.data('kendoWindow');

                    dhcpModal.content('<div class="content-box-grid-panel margin-t-20" id="credentialType"><div class="row"></div> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-6"> <input type="radio" name="type" id="type" class="k-radio" value="0"  checked/> <label class="k-radio-label label-2" for="type">Windows Credential</label> </div><div class="col-xs-12 col-md-12 col-lg-6"> <input type="radio" name="type" id="type" class="k-radio" value="1"/> <label class="k-radio-label label-2" for="type ">Cisco Credential</label> </div></div></div><div class="common-box-grid-panel" id="addCredentialDetails"><form id="dhcpCredentialForm"></form></div>');

                    flux.bindElementEvent({event:'click',element: 'credentialType', selector:'#type', kendoModal:dhcpModal}, dhcpManagement.onAddDhcpServerClick);

                    $("#type").trigger('click');
                }
            }
        },

        onAddDhcpServerClick : function (event)
        {
            if(event)
            {
                var elementValue = parseInt($(event.currentTarget).val());

                var dhcpModal =  event.data.kendoModal;

                var credentialForm = $("#dhcpCredentialForm");

                if(elementValue == dhcpManagement.credentialType.WINDOWS)
                {
                    credentialForm.html('<div id="commonCredentialDetails" class="nav-panel"><div class="fixed-height-body-popup-panel widthFullSpacer"><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-6"> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <select id="windowsCredentialDropdown" name="id" class="k-dropdown" required/></div></div><div id="testCredentialForm"> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="hostAddress">Host Address</label> <input id="hostAddress" name="hostAddress" type="text" class="k-textbox" validationMessage="Host Address is required" required maxlength="40"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="port">Port</label> <input id="port" name="port" type="text" class="k-textbox" maxlength="5" required validationMessage="Port is required" min="1"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="credentialName">Credential Name</label> <input id="credentialName" name="credentialName" type="text" class="k-textbox" validationMessage="Credential Name is required" required maxlength="50"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="userName">User Name</label> <input id="ciscoUserName" name="userName" type="text" class="k-textbox" validationMessage="User Name is required" required maxlength="50"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="password">Password</label> <input id="password" name="password" type="password" class="k-textbox" required validationMessage="Password is required" maxlength="50"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12 select-drop-menu padding-r-0 margin-0"> <input type="checkbox" id="scheduleStatus" name="scheduleStatus" class="k-checkbox" value="true"/> <label for="dhcpAutoScanning">Automatic Scanning</label></div></div></div></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12" id="dhcpAutoScanDiv"></div></div></div></div>');

                    flux.getKendoDropDownListURL({dropDownId:$("#windowsCredentialDropdown"),url:"/windowsDhcpCredential/",dataTextField: "credentialName",dataValueField: "id"});

                    flux.bindElementEvent({event:'change',element:'commonCredentialDetails',selector:'#windowsCredentialDropdown',deviceType:0},dhcpManagement.onWindowsCredentialChange);
                }
                else if(elementValue == dhcpManagement.credentialType.CISCO)
                {
                    credentialForm.html('<div id="commonCredentialDetails" class="nav-panel"><div class="fixed-height-body-popup-panel widthFullSpacer"><div class="row"><div class="col-xs-12 col-md-12 col-lg-12"> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-6"></div><div class="col-xs-12 col-md-12 col-lg-6"> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <select id="ciscoCredentialDropdown" name="id" class="k-dropdown" required/></div></div><div id="testCredentialForm"> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="hostAddress">Host Address</label> <input id="hostAddress" name="hostAddress" type="text" class="k-textbox" validationMessage="Host Address is required" required maxlength="40"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="port">Port</label> <input id="port" name="port" type="text" class="k-textbox" required validationMessage="Port is required" maxlength="5"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="credentialName">Credential Name</label> <input id="credentialName" name="credentialName" type="text" class="k-textbox" validationMessage="Credential Name is required" required maxlength="50"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="userName">User Name</label> <input id="ciscoUserName" name="userName" type="text" class="k-textbox" validationMessage="User Name is required" required maxlength="50"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="password">Password</label> <input id="password" name="password" type="password" class="k-textbox" required validationMessage="Password is required" maxlength="50"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12 select-drop-menu padding-r-0 margin-0"> <input type="checkbox" id="scheduleStatus" name="scheduleStatus" class="k-checkbox" value="true"/> <label for="dhcpAutoScanning">Automatic Scanning</label></div></div></div></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12" id="dhcpAutoScanDiv"></div></div></div>');

                    flux.getKendoDropDownListURL({dropDownId:$("#ciscoCredentialDropdown"),url:"/ciscoDhcpCredential/",dataTextField: "credentialName",dataValueField: "id"});

                    flux.bindElementEvent({event:'change',element:'commonCredentialDetails',selector:'#ciscoCredentialDropdown',deviceType:1},dhcpManagement.onCiscoCredentialChange);
                }
                flux.bindElementEvent({event:'keyup',element:'addModal',selector:'#port'},flux.numberValidation);

                flux.bindElementEvent({event:'keyup',element:'addModal',selector:'#hostAddress'},flux.hostAddressValidation);
            }

            appManager.initCustomScrollbar({container:$(".fixed-height-body-popup-panel")});

            var commonCredential = $("#commonCredentialDetails");

            commonCredential.append('<div class="footer-box-grid-box margin-t-10 align-right"> <div class="bottom-form-panel"> <button class="k-button k-button-icontext k-primary k-grid-cancel float-l margin-0" id="testCredenatial">Test</button> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="addDHCP"> Save </button> <button class="k-button k-button-icontext k-grid-cancel" id="cancelDhcpModal">Cancel</button></div></div>');

            flux.bindElementEvent({event:'change',element:'addModal',selector:'#scheduleStatus'},dhcpManagement.renderDhcpScheduler);

            flux.bindEvent({element: 'addDHCP',elementValue:elementValue,kendoModal:dhcpModal},dhcpManagement.addDHCPEntity);

            flux.bindKendoButtonClickEvent({element:'cancelDhcpModal',kendoModal:dhcpModal,gridId:'dhcpManagementTable'},dhcpManagement.closeGridCancelModal);

            flux.bindEvent({element: 'testCredenatial',elementValue:elementValue,kendoModal:dhcpModal},dhcpManagement.testDeviceCredential);

        },

        onWindowsCredentialChange : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var credentialId = $("#windowsCredentialDropdown").val();

                var container = $("#dhcpCredentialForm");

                if(credentialId != 'null')
                {
                    appManager.executeGETRequest({url: '/dhcpCredential/'+credentialId,container:container, callback: formManager.renderForm});
                }
                else
                {
                    container.trigger('reset');

                    var dropdownlist = $("#windowsCredentialDropdown").data('kendoDropDownList');

                    dropdownlist.value(credentialId);

                    dropdownlist.refresh();
                }
            }
        },

        onCiscoCredentialChange : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var credentialId = $("#ciscoCredentialDropdown").val();

                var container = $("#dhcpCredentialForm");

                if(credentialId != 'null')
                {
                    appManager.executeGETRequest({url: '/dhcpCredential/'+credentialId,container:container, callback: formManager.renderForm});
                }
                else
                {
                    container.trigger('reset');

                    var dropdownlist = $("#ciscoCredentialDropdown").data('kendoDropDownList');

                    dropdownlist.value(credentialId);

                    dropdownlist.refresh();
                }
            }
        },

        addDHCPEntity: function (event)
        {
            if(event)
            {
                event.preventDefault();

                var kendoEvent = event.data.kendoModal;

                var form = $("#dhcpCredentialForm");

                var type = event.data.elementValue;

                var param = formManager.serializeForm(form);

                param['id'] = null;

                var credentialType;

                if(type == 0)
                {
                    credentialType = "windows";
                }

                if(type == 1)
                {
                    credentialType = "cisco";
                }

                param['type'] = credentialType;

                var validator = form.kendoValidator().data("kendoValidator");

                if(validator.validate())
                {
                    loaderUtil.showModalLoader();

                    appManager.executePOSTRequest({url: '/dhcpCredential/',container:kendoEvent, callback: dhcpManagement.afterDhcpServerAddedOrUpdated, params: param});
                }
            }
        },

        // -----------------------------------------------------------------------Render DHCP Edit modal---------------------------------------------------------------------------------------------------//
        
        renderEditModal : function (context)
        {
            var windowId = $("#addModal");

            windowId.width('750px');

            flux.kendoWindowSetOption({kendoWindow: windowId});

            $('#addModal_wnd_title').html('Edit DHCP Server');

            var container = windowId.data('kendoWindow');

            container.content('<div class="content-box-grid-panel margin-t-20" id="credentialType"><div class="row"></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"> <input type="radio" name="type" id="windowsType" class="k-radio" value="0"/> <label class="k-radio-label label-2" for="type">Windows Credential</label></div><div class="col-xs-12 col-md-12 col-lg-6"> <input type="radio" name="type" id="ciscoType" class="k-radio" value="1"/> <label class="k-radio-label label-2" for="type ">Cisco Credential</label></div></div></div><div class="common-box-grid-panel" id="addCredentialDetails"><form id="dhcpCredentialForm"></form></div>');

            if(context.json.data != null && context.json.success)
            {
                var data = context.json.data;

                var elementValue;

                var credentialForm = $("#dhcpCredentialForm");

                if(data.type == 'Cisco')
                {
                    dhcpManagement.radionButtonSelectedValueSet('type',1,'windowsType');

                    elementValue = 1;

                    credentialForm.html('<div id="commonCredentialDetails"><div class="fixed-height-body-popup-panel widthFullSpacer"><div class="row"><div class="col-xs-12 col-md-12 col-lg-12"> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-6"></div><div class="col-xs-12 col-md-12 col-lg-6"> <div class="row"></div><div id="testCredentialForm"> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="hostAddress">Host Address</label> <input id="hostAddress" name="hostAddress" type="text" class="k-textbox" validationMessage="Host Address is required" required maxlength="50" readonly/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="port">Port</label> <input id="port" name="port" type="text" class="k-textbox" required validationMessage="Valid Port is required" maxlength="5"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="credentialName">Credential Name</label> <input id="credentialName" name="credentialName" type="text" class="k-textbox" validationMessage="Credential Name is required" required maxlength="50"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="userName">User Name</label> <input id="ciscoUserName" name="userName" type="text" class="k-textbox" validationMessage="User Name is required" required maxlength="50"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="password">Password</label> <input id="password" name="password" type="password" class="k-textbox" required validationMessage="Password is required" maxlength="50"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12 select-drop-menu padding-r-0 margin-0"> <input type="checkbox" id="scheduleStatus" name="scheduleStatus" class="k-checkbox" value="true"/> <label for="dhcpAutoScanning">Automatic Scanning</label></div></div></div></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12" id="dhcpAutoScanDiv"></div></div></div>');

                    //flux.bindElementEvent({event:'change',element:'commonCredentialDetails',selector:'#ciscoCredentialDropdown',deviceType:1},dhcpManagement.onCiscoCredentialChange);
                }

                if(data.type == 'Windows')
                {
                    dhcpManagement.radionButtonSelectedValueSet('type',0,'ciscoType');

                    elementValue = 0;

                    credentialForm.html('<div id="commonCredentialDetails"><div class="fixed-height-body-popup-panel widthFullSpacer"><div class="row"><div class="col-xs-12 col-md-12 col-lg-12"> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-6"> <div class="row"></div><div id="testCredentialForm"> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="hostAddress">Host Address</label> <input id="hostAddress" name="hostAddress" type="text" class="k-textbox" validationMessage="Host Address is required" required maxlength="50" readonly/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="port">Port</label> <input id="port" name="port" type="text" class="k-textbox" maxlength="5" required validationMessage="Valid Port is required"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="credentialName">Credential Name</label> <input id="credentialName" name="credentialName" type="text" class="k-textbox" validationMessage="Credential Name is required" required maxlength="50"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="userName">User Name</label> <input id="ciscoUserName" name="userName" type="text" class="k-textbox" validationMessage="User Name is required" required maxlength="50"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="password">Password</label> <input id="password" name="password" type="password" class="k-textbox" required validationMessage="Password is required" maxlength="50"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12 select-drop-menu padding-r-0 margin-0"> <input type="checkbox" id="scheduleStatus" name="scheduleStatus" class="k-checkbox" value="true"/> <label for="dhcpAutoScanning">Automatic Scanning</label></div></div></div></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12" id="dhcpAutoScanDiv"></div></div></div>');

                    //flux.bindElementEvent({event:'change',element:'commonCredentialDetails',selector:'#windowsCredentialDropdown',deviceType:0},dhcpManagement.onWindowsCredentialChange);
                }

                var commonCredential = $("#commonCredentialDetails");

                commonCredential.append('<div class="footer-box-grid-box margin-t-10 align-right"> <div class="bottom-form-panel"><button class="k-button k-button-icontext k-primary k-grid-cancel float-l margin-0" id="testCredenatial">Test</button> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="editDHCP">Update</button> <button class="k-button k-button-icontext k-grid-cancel" id="cancelDhcpEditModal">Cancel</button></div></div>');

                var dataValue = {};

                dataValue['scheduleHour'] = data.scheduleHour;
                dataValue['duration'] = data.duration;
                dataValue['subnetScheduleHour'] = data.subnetScheduleHour;
                dataValue['subnetDuration'] = data.subnetDuration;

                flux.bindElementEvent({event:'change',element:'addModal',selector:'#scheduleStatus',isEditable:true,dataValue:dataValue},dhcpManagement.renderDhcpScheduler);

                context['container'] = credentialForm;

                formManager.renderForm(context);

                appManager.initCustomScrollbar({container:$(".fixed-height-body-popup-panel")});

                flux.bindElementEvent({event:'keyup',element:'addModal',selector:'#port'},flux.numberValidation);

                flux.bindEvent({element: 'editDHCP',elementValue:elementValue,kendoModal:container, editId:context.editId},dhcpManagement.editDHCPServer);

                flux.bindEvent({element: 'testCredenatial',elementValue:elementValue,kendoModal:windowId},dhcpManagement.testDeviceCredential);

                flux.bindKendoButtonClickEvent({element:'cancelDhcpEditModal',kendoModal:container,gridId:'dhcpManagementTable'},dhcpManagement.closeGridCancelModal);

                if(data.scheduleStatus)
                {
                    $('#scheduleStatus').trigger('change');
                }

                loaderUtil.hideModalLoader();

                loaderUtil.hideCentralModalLoader();
            }
        },

        radionButtonSelectedValueSet: function(name, SelectedValue,disableElement)
        {
            $('input[name="' + name+ '"][value="' + SelectedValue + '"]').prop('checked', true);

            $("#"+disableElement).attr('disabled', true);
        },
        
        renderDhcpScheduler : function (event)
        {
            if(event)
            {
                //$('#scheduleStatus').trigger('click');

                var dataContext = event.data.dataValue;

                var repeatContent = $("#dhcpAutoScanDiv");

                var checkedValue = $(event.currentTarget).context.checked;

                if(checkedValue)
                {
                    repeatContent.html('<div class="row"><div class="col-xs-12 col-md-12 col-lg-12"><div class="line-separator"></div></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"><div class="small-title-h margin-b-10">DHCP Server Scan Setting</div> <label>Scan DHCP Server for new scopes and leases every</label><div class="row"><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <select id="duration" name="duration" class="k-dropdown"/></div><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <select id="scheduleHour" name="scheduleHour" class="k-dropdown" /></div></div></div><div class="col-xs-12 col-md-12 col-lg-6"><div class="small-title-h margin-b-10">New Scope and Subnet Setting</div> <label>These Setting will be applied upon creation.They can be changed once a subnet or scope has been added to IPAM.</label><div class="row"><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <select id="subnetDuration" name="subnetDuration" class="k-dropdown"/></div><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <select id="subnetScheduleHour" name="subnetScheduleHour" class="k-dropdown" /></div></div></div></div>');

                    var scheduleHour = $('#scheduleHour');

                    var duration = $("#duration");

                    var subnetScheduleHour = $("#subnetScheduleHour");

                    var subnetDuration = $("#subnetDuration");

                    var durationDropdown = duration.kendoDropDownList({
                        dataSource: [{name: "Hour", id: "Hours"}, {name: "Day", id: "Days"}, { name: "Month", id: "Month"}],
                        dataTextField: "name",
                        dataValueField: "id"
                    }).data("kendoDropDownList");

                    var scheduleHourDropdown = scheduleHour.kendoDropDownList({
                        dataSource: [{text: '1', value: '1', id: 'Hours' }, {text: '2', value: '2', id: 'Hours'}, {text: '3', value: '3', id: 'Hours'},
                            {text: '4', value: '4', id: 'Hours'}, {text: '5', value: '5', id: 'Hours'}, {text: '6', value: '6', id: 'Hours' }, {text: '7', value: '7', id: 'Hours'},
                            {text: '8', value: '8', id: 'Hours'}, {text: '9', value: '9', id: 'Hours'}, {text: '10', value: '10', id: 'Hours'}, {text: '11', value: '11', id: 'Hours' },
                            {text: '12', value: '12', id: 'Hours'}, {text: '13', value: '13', id: 'Hours'}, {text: '14', value: '14', id: 'Hours'}, {text: '15', value: '15', id: 'Hours' },
                            {text: '16', value: '16', id: 'Hours'}, {text: '17', value: '17', id: 'Hours'}, {text: '18', value: '18', id: 'Hours'}, {text: '19', value: '19', id: 'Hours'},
                            { text: '20', value: '20', id: 'Hours'}, { text: '21', value: '21', id: 'Hours' }, {text: '22', value: '22', id: 'Hours'}, {text: '23', value: '23', id: 'Hours'},
                            { text: '1', value: '1', id: 'Month'}, { text: '2', value: '2',id: 'Month'},{text: '3', value: '3', id: 'Month' }, {text: '4', value: '4', id: 'Month'},
                            { text: '5', value: '5', id: 'Month'}, { text: '6', value: '6', id: 'Month'}, { text: '7', value: '7', id: 'Month'}, { text: '8', value: '8', id: 'Month'},
                            {text: '9', value: '9', id: 'Month'}, {text: '10', value: '10', id: 'Month'}, {text: '11', value: '11', id: 'Month' }, {text: '12', value: '12', id: 'Month'},
                            { text: '1', value: '1', id: 'Days'}, { text: '2', value: '2', id: 'Days'}, { text: '3', value: '3', id: 'Days'}, {text: '4', value: '4', id: 'Days'},
                            {text: '5', value: '5', id: 'Days'}, {text: '6', value: '6', id: 'Days'}, { text: '7', value: '7', id: 'Days'}, { text: '8', value: '8', id: 'Days'},
                            { text: '9', value: '9', id: 'Days'}, {text: '10', value: '10', id: 'Days'}, {text: '11', value: '11', id: 'Days' }, {text: '12', value: '12', id: 'Days'},
                            {text: '13', value: '13', id: 'Days'}, {text: '14', value: '14', id: 'Days'}, { text: '15', value: '15', id: 'Days'}, { text: '16', value: '16', id: 'Days'},
                            {text: '17', value: '17', id: 'Days'}, {text: '18', value: '18', id: 'Days'}, { text: '19', value: '19', id: 'Days'}, { text: '20', value: '20', id: 'Days'},
                            {text: '21', value: '21', id: 'Days'}, {text: '22', value: '22', id: 'Days'}, {text: '23', value: '23', id: 'Days'}, {text: '24', value: '24', id: 'Days'},
                            {text: '25', value: '25', id: 'Days'}, {text: '26', value: '26', id: 'Days'}, {text: '27', value: '27', id: 'Days' }, {text: '28', value: '28', id: 'Days'},
                            {text: '29', value: '29', id: 'Days'}, {text: '30', value: '30', id: 'Days'}, { text: '31', value: '31', id: 'Days'}],
                        cascadeFrom: "duration",
                        dataTextField: "text",
                        dataValueField: "value"
                    }).data("kendoDropDownList");

                    var subnetDurationDropdown = subnetDuration.kendoDropDownList({
                        dataSource: [{name: "Hour", id: "Hours"}, {name: "Day", id: "Days"}, {
                            name: "Month",
                            id: "Month"
                        }],
                        dataTextField: "name",
                        dataValueField: "id"
                    }).data("kendoDropDownList");

                    var subnetScheduleHourDropdown = subnetScheduleHour.kendoDropDownList({

                        dataSource: [{text: '1', value: '1', id: 'Hours' }, {text: '2', value: '2', id: 'Hours'}, {text: '3', value: '3', id: 'Hours'},
                            {text: '4', value: '4', id: 'Hours'}, {text: '5', value: '5', id: 'Hours'}, {text: '6', value: '6', id: 'Hours' }, {text: '7', value: '7', id: 'Hours'},
                            {text: '8', value: '8', id: 'Hours'}, {text: '9', value: '9', id: 'Hours'}, {text: '10', value: '10', id: 'Hours'}, {text: '11', value: '11', id: 'Hours' },
                            {text: '12', value: '12', id: 'Hours'}, {text: '13', value: '13', id: 'Hours'}, {text: '14', value: '14', id: 'Hours'}, {text: '15', value: '15', id: 'Hours' },
                            {text: '16', value: '16', id: 'Hours'}, {text: '17', value: '17', id: 'Hours'}, {text: '18', value: '18', id: 'Hours'}, {text: '19', value: '19', id: 'Hours'},
                            { text: '20', value: '20', id: 'Hours'}, { text: '21', value: '21', id: 'Hours' }, {text: '22', value: '22', id: 'Hours'}, {text: '23', value: '23', id: 'Hours'},
                            { text: '1', value: '1', id: 'Month'}, { text: '2', value: '2',id: 'Month'},{text: '3', value: '3', id: 'Month' }, {text: '4', value: '4', id: 'Month'},
                            { text: '5', value: '5', id: 'Month'}, { text: '6', value: '6', id: 'Month'}, { text: '7', value: '7', id: 'Month'}, { text: '8', value: '8', id: 'Month'},
                            {text: '9', value: '9', id: 'Month'}, {text: '10', value: '10', id: 'Month'}, {text: '11', value: '11', id: 'Month' }, {text: '12', value: '12', id: 'Month'},
                            { text: '1', value: '1', id: 'Days'}, { text: '2', value: '2', id: 'Days'}, { text: '3', value: '3', id: 'Days'}, {text: '4', value: '4', id: 'Days'},
                            {text: '5', value: '5', id: 'Days'}, {text: '6', value: '6', id: 'Days'}, { text: '7', value: '7', id: 'Days'}, { text: '8', value: '8', id: 'Days'},
                            { text: '9', value: '9', id: 'Days'}, {text: '10', value: '10', id: 'Days'}, {text: '11', value: '11', id: 'Days' }, {text: '12', value: '12', id: 'Days'},
                            {text: '13', value: '13', id: 'Days'}, {text: '14', value: '14', id: 'Days'}, { text: '15', value: '15', id: 'Days'}, { text: '16', value: '16', id: 'Days'},
                            {text: '17', value: '17', id: 'Days'}, {text: '18', value: '18', id: 'Days'}, { text: '19', value: '19', id: 'Days'}, { text: '20', value: '20', id: 'Days'},
                            {text: '21', value: '21', id: 'Days'}, {text: '22', value: '22', id: 'Days'}, {text: '23', value: '23', id: 'Days'}, {text: '24', value: '24', id: 'Days'},
                            {text: '25', value: '25', id: 'Days'}, {text: '26', value: '26', id: 'Days'}, {text: '27', value: '27', id: 'Days' }, {text: '28', value: '28', id: 'Days'},
                            {text: '29', value: '29', id: 'Days'}, {text: '30', value: '30', id: 'Days'}, { text: '31', value: '31', id: 'Days'}],
                        cascadeFrom: "subnetDuration",
                        dataTextField: "text",
                        dataValueField: "value"
                    }).data("kendoDropDownList");


                    if(dataContext != undefined)
                    {
                        if(dataContext.duration != null && dataContext.scheduleHour != null && dataContext.subnetDuration != null && dataContext.subnetScheduleHour != null )
                        {
                            if(durationDropdown)
                            {
                                durationDropdown.value(dataContext.duration);
                            }
                            if(scheduleHourDropdown)
                            {
                                scheduleHourDropdown.value(dataContext.scheduleHour);
                            }
                            if(subnetDurationDropdown)
                            {
                                subnetDurationDropdown.value(dataContext.subnetDuration);
                            }
                            if(subnetScheduleHourDropdown)
                            {
                                subnetScheduleHourDropdown.value(dataContext.subnetScheduleHour);
                            }
                        }
                    }
                }
                else
                {
                    repeatContent.html('<div id="dhcpAutoScheduler"></div>');
                }
            }
        },

        editDHCPServer : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var kendoEvent = event.data.kendoModal;

                var id = event.data.editId;

                var form = $("#dhcpCredentialForm");

                var type = event.data.elementValue;

                var param = formManager.serializeForm(form);

                var credentialType;

                if(type == 0)
                {
                    credentialType = "windows";
                }

                if(type == 1)
                {
                    credentialType = "cisco";
                }

                param['type'] = credentialType;

                var validator = form.kendoValidator().data("kendoValidator");

                if(validator.validate())
                {
                    loaderUtil.showModalLoader();

                    appManager.executePUTRequest({url: '/dhcpCredential/'+id, container: kendoEvent,callback: dhcpManagement.afterDhcpServerAddedOrUpdated, params: param});
                }
            }
        },

        // ------------------------------------------------------------------------------------After DHCP Add & Edit Operation--------------------------------------------------------------------------------------//
        
        afterDhcpServerAddedOrUpdated : function (context)
        {
            if(context)
            {
                var modal = context.container;

                loaderUtil.hideModalLoader();

                loaderUtil.hideCentralModalLoader();

                if(context.json.success == true)
                {
                    modal.close();

                    dhcpManagement.init();

                    notification.showNotification({ notificationTitle: context.json.message, notificationType: "success"});
                }
                else
                {
                    notification.showNotification({ notificationTitle: context.json.message, notificationType: "error"});
                }

            }
        },

        // --------------------------------------------------------------------------Close DHCP Cancel modal event------------------------------------------------------------------------------------------------//
        
        closeGridCancelModal : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var context = event.sender.options.prefix;

                var modal = context.kendoModal;

                modal.close();

                var gridId = $("#"+context.gridId);

                flux.refreshKendoGrid({gridId : gridId});
            }
        },

        // -------------------------------------------------------------------------Delete DHCP-------------------------------------------------------------------------------------------------//

        deleteConfirmationPrompt : function (event)
        {
            if(event)
            {
                event.preventDefault();

                loaderUtil.showModalLoader();

                loaderUtil.showCentralModalLoader(appConstant.DHCPServerDeleteMessage);

                var modal =  event.data.kendoModal;

                var context =  event.data.data;

                var grid = $("#dhcpManagementTable").data('kendoGrid');

                grid.dataSource.remove(context);

                grid.dataSource.sync();

                modal.close();

                loaderUtil.hideModalLoader();
            }
        },

        afterDhcpServerDeleted : function (context)
        {
            if(context)
            {
                var gridId = context.gridId;

                if(context.json.success == true)
                {
                    notification.showNotification({ notificationTitle: context.json.message, notificationType: "success"});
                }
                else
                {
                    notification.showNotification({ notificationTitle: context.json.message, notificationType: "error"});
                }

                flux.refreshKendoGrid({gridId : gridId,container:context.container});

                loaderUtil.hideCentralModalLoader();
            }
        },
        
        // -------------------------------------------------------------------------------DHCP Server Test Method-------------------------------------------------------------------------------------------//
        
        testDeviceCredential : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var kendoEvent = event.data.kendoModal;

                var form = $("#testCredentialForm");

                var type = event.data.elementValue;

                var param = formManager.serializeForm(form);

                var credentialType;

                if(type == 0)
                {
                    credentialType = "windows";
                }

                if(type == 1)
                {
                    credentialType = "cisco";
                }

                param['type'] = credentialType;

                var validator = form.kendoValidator().data("kendoValidator");

                if(validator.validate())
                {
                    loaderUtil.showModalLoader();

                    appManager.executePOSTRequest({url: '/checkDhcpCredential/',container:kendoEvent, callback: dhcpManagement.afterDHCPServerTested, params: param});
                }
            }
        },

        afterDHCPServerTested : function (context)
        {
            if(context)
            {
                loaderUtil.hideModalLoader();

                if(context.json.success == true)
                {
                    notification.showNotification({ notificationTitle: context.json.message, notificationType: "success"});
                }
                else
                {
                    notification.showNotification({ notificationTitle: context.json.message, notificationType: "error"});
                }
            }

        }
    };

