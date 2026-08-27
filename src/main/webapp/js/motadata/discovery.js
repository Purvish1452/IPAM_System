var discovery =
{
    checkScanStatus: undefined,

    popupContent : '',

    init : function ()
    {
        loaderUtil.showModalLoader();

        loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

        $("#settingMenuGrid").html('<div class="container-fluid"><div class="content-section-panel"><div class="row"><div class="col-xs-12 col-md-12 col-lg-12"><div class="datatable-grid-panel"><div class="widget-main-box"><div class="widget-header-box"><div class="widget-header-title">Gateways</div></div><div class="search-left-panel"><div class="search-box"><i class="icon-magnifier icons"></i><input class="input-box searchFilter" id="searchFilter" placeholder="Search" type="text"></div></div><div class="widget-content-box" id="gatewayTable"></div></div></div></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-12"><div class="datatable-grid-panel"><div class="widget-main-box"><div class="widget-header-box"><div class="widget-header-title">Discovered Subnets</div></div><div class="search-left-panel"><div class="search-box"><i class="icon-magnifier icons"></i><input class="input-box searchFilter" id="searchFilterDiscoveredSubnet" placeholder="Search" type="text"></div></div><div class="widget-content-box" id="discoveredSubnetTable"></div></div></div></div></div></div></div>');

        discovery.renderGateways();

        discovery.renderDiscoveredSubnets();
    },

    renderGateways : function ()
    {
        var gatewayTable = $("#gatewayTable");

        var callbackContext = [];

        callbackContext['id'] = gatewayTable;

        callbackContext['name'] = "Gateway";

        callbackContext['dataSource'] =
            {
                pageSize: 10,
                transport:
                    {
                        read: function (options)
                        {
                            appManager.executeGETRequest({url: '/gateways/',container:options,callback:discovery.renderGatewayGrid});
                        },
                        destroy:function (options)
                        {
                            var id = options.data.models[0].id;

                            appManager.executeDELETERequest({ url: '/gateway/'+id, callback: discovery.afterGatewayDeleted, gridId:gatewayTable});
                        },

                        parameterMap: function(options, operation)
                        {
                            if (operation != "read" && options.models)
                            {
                                return {models: kendo.stringify(options.models)};
                            }
                        }
                    },

                batch: true,
                schema: {
                    model: {
                        id: "id",
                        fields: {
                            name: { type: "string"},
                            gateway: { type: "string" },
                            previousScan: { type: "string"},
                            status: { type: "string" },
                        }
                    }
                }
            };

        callbackContext['toolbar'] = [{name: "create", text: "<span class='fa fa-plus'> Add Gateway</span>"}];

        callbackContext['columns'] = [
            {field:"id", hidden:true},
            { field:"name", title: "Name",template:'# if (name) { # <span title="#:name#">#: name # </span># } else { #<span></span># } #'},
            { field: "gateway", title:"IP",width:'20%',template:'# if (gateway) { # <span title="#:gateway#">#: gateway # </span># } else { #<span></span># } #'},
            { field: "previousScan", title:"Previous Scan",width:'25%',template:'# if (previousScan) { # <span title="#:previousScan#">#: previousScan # </span># } else { #<span></span># } #'},
            { field: "status", title:"Status",width:'15%',template:'# if (status) { # <span title="#:status#">#: status # </span># } else { #<span></span># } #'},
            {
                width:'20%',
                command: [
                    {name: "edit", text: {edit: "", update: "Save", cancel: "Cancel"}, iconClass: "fa fa-pencil",title:"Edit"},
                    {name: "Delete", text: "", iconClass: "fa fa-trash",
                        click:function (e)
                        {
                            e.preventDefault();

                            var tr = $(e.target).closest("tr");

                            var data = this.dataItem(tr);

                            var context = {};

                            context.data = data;

                            context.windowId = 'deleteModal';

                            context.title = 'Do you really want to delete the selected Gateway?';

                            context.yesButtonEvent = discovery.deleteConfirmationPrompt;

                            context.noButtonEvent = table.closeDeleteWindow;

                            context.grid = "Gateway";

                            flux.deleteWindowExecute(context);
                        }},
                    {name: "Scan", text: "", title:"Scan", iconClass: "fa fa-play",
                        click:function (e)
                        {
                            e.preventDefault();

                            var tr = $(e.target).closest("tr");

                            var data = this.dataItem(tr);

                            appManager.executePOSTRequest({url: '/scanGateway/' + data.id, callback: discovery.afterScanGateway, params: {id : data.id}});
                        }}
                ]
            }
        ];

        callbackContext['edit'] = function (e)
        {
            var gatewayModal = e.container.data("kendoWindow");

            gatewayModal.content('<div class="common-box-grid-panel padding-t-20"><form id="gatewayForm"><div class="content-box-grid-panel margin-t-20" id="importType"><div class="row"><div class="col-xs-6 col-md-6 col-lg-6"><label for="name">Name</label><input id="name" name="name" type="text" class="k-textbox" required="" validationmessage="Name required" maxlength="45"></div><div class="col-xs-6 col-md-6 col-lg-6"><label for="gateway">IP</label><input id="gateway" name="gateway" type="text" class="k-textbox" required="" validationmessage="Gateway required" maxlength="45"></div></div><div class="row"><div class="col-xs-6 col-md-6 col-lg-6 select-drop-menu"><label for="version">Version</label><select id="version2" name="version" class="k-dropdown" required="">Version</select></div><div class="col-xs-6 col-md-6 col-lg-6"><label for="community">Community</label><input id="community" name="community" type="password" class="k-textbox"></div></div><div class="row"><div class="col-xs-6 col-md-6 col-lg-6 select-drop-menu"><label for="securityLevel">Security Level</label><select id="securityLevel2" name="securityLevel" class="k-dropdown" required="">securityLevel</select></div><div class="col-xs-6 col-md-6 col-lg-6 select-drop-menu"><label for="authenticationProtocol">Authentication Protocol</label><select id="authenticationProtocol2" name="authenticationProtocol" class="k-dropdown">Authentication Protocol</select></div></div><div class="row"><div class="col-xs-6 col-md-6 col-lg-6 select-drop-menu"><label for="privacyProtocol">Privacy Protocol</label><select id="privacyProtocol2" name="privacyProtocol" class="k-dropdown">Privacy Protocol</select></div><div class="col-xs-6 col-md-6 col-lg-6"><label for="securityUserName">Security User Name</label><input id="securityUserName" name="securityUserName" type="text" class="k-textbox"></div></div><div class="row"><div class="col-xs-6 col-md-6 col-lg-6"><label for="authenticationPassword">Authentication Password</label><input id="authenticationPassword" name="authenticationPassword" type="password" class="k-textbox"></div><div class="col-xs-6 col-md-6 col-lg-6"><label for="privatePassword">Private Password</label><input id="privatePassword" name="privatePassword" type="password" class="k-textbox"></div></div></div><div class="footer-box-grid-boxs margin-t-10 align-right"><div class="bottom-form-panel"><button class="k-button k-button-icontext k-primary k-grid-update float-r" id="addGateway">Add Gateway</button><button class="k-button k-button-icontext k-grid-cancel float-l" id="cancelGateway">Cancel</button></div></div></form></div>');

            var formContext = $("#gatewayForm");

            discovery.loadGatewayDropDowns();


            flux.bindKendoButtonClickEvent({element : 'cancelGateway',kendoModal:gatewayModal},discovery.onCloseButtonClick);

            if (!e.model.isNew())
            {
                var id = e.model.id;

                var gateway = $("#addGateway");

                table.kendoWindowSetOption({title : 'Edit Gateway',width:'750px', kendoWindow : gatewayModal});

                gateway.text("Update");

                gateway.data('id', id);

                loaderUtil.showModalLoader();

                appManager.executeGETRequest({url: '/gateway/' + id, container: formContext, callback: formManager.renderForm, params: {id : id}});

                loaderUtil.hideModalLoader();

                flux.bindKendoButtonClickEvent({element:'addGateway',kendoModal:gatewayModal},discovery.editEntity);

            }
            else
            {
                table.kendoWindowSetOption({title : 'Add Gateway', width:'750px', kendoWindow : gatewayModal});

                flux.bindKendoButtonClickEvent({element:'addGateway',kendoModal:gatewayModal},discovery.addGatewayEntity);

            }
        };

        gatewayTable.kendoTooltip({ filter: ".k-grid-toolbar a.k-grid-add", content: "Add", showAfter: 1000, position: "bottom"});

        gatewayTable.kendoTooltip({ filter: ".k-grid-edit", content: "Edit", showAfter: 1000, position: "bottom"});

        gatewayTable.kendoTooltip({ filter: ".k-grid-Delete", content: "Delete", showAfter: 1000, position: "bottom"});

        gatewayTable.kendoTooltip({ filter: ".k-grid-Scan", content: "Scan", showAfter: 1000, position: "bottom"});

        $("#popupContent").kendoTooltip({ filter: "li[title]",showAfter: 1000, position: "bottom" });

        table.renderTable(callbackContext);

        var filter = $("#searchFilter");

        table.searchFilter(gatewayTable, filter);

        loaderUtil.hideModalLoader();
    },

    loadGatewayDropDowns : function ()
    {
        var version = $("#version2");

        var securityLevel = $("#securityLevel2");

        var authenticationProtocol = $("#authenticationProtocol2");

        var privacyProtocol = $("#privacyProtocol2");

        var versions = [{text: "V1", value: "v1" },{text: "V2c", value: "v2c" },{text: "V3", value: "v3" }];

        var securityLevels = [{text: "No Authentication No Privacy", value: "noAuthNoPriv" },{text: "Authentication No Privacy", value: "authNoPriv" },{text: "Authentication Privacy", value: "authPriv" }];

        var privacyProtocols = [{text: "DES", value: "DES" },{text: "3DES", value: "3DES" },{text: "AES", value: "AES" },{text: "AES128", value: "AES128" },{text: "AES192", value: "AES192" },{text: "AES256", value: "AES256" }];

        var authenticationProtocols = [{text: "MD5", value: "MD5" },{text: "SHA", value: "SHA" },{text: "SHA224", value: "SHA224" }, {text: "SHA256", value: "SHA256" }, {text: "SHA384", value: "SHA384" }, {text: "SHA512", value: "SHA512" }];

        flux.getKendoDropDownList({dropDownId:version, dataTextField: "text", dataValueField: "value", data:versions});

        flux.getKendoDropDownList({dropDownId:securityLevel, dataTextField: "text", dataValueField: "value", data:securityLevels});

        flux.getKendoDropDownList({dropDownId:authenticationProtocol, dataTextField: "text", dataValueField: "value", data:authenticationProtocols});

        flux.getKendoDropDownList({dropDownId:privacyProtocol, dataTextField: "text", dataValueField: "value", data:privacyProtocols});
    },

    renderDiscoveredSubnets : function ()
    {
        var discoveredSubnetTable = $("#discoveredSubnetTable");

        var callbackContext = [];

        callbackContext['id'] = discoveredSubnetTable;

        callbackContext['name'] = "DiscoveredSubnet";

        callbackContext['dataSource'] =
            {
                pageSize: 10,
                transport:
                    {
                        read: function (options)
                        {
                            appManager.executeGETRequest({url: '/discoveredSubnet/',container:options,callback:discovery.renderGatewayGrid});
                        },
                        destroy:function (options)
                        {
                            var id = options.data.models[0].id;

                            appManager.executeDELETERequest({ url: '/discoveredSubnet/'+id, callback: discovery.afterGatewayDeleted, gridId:discoveredSubnetTable});
                        },

                        parameterMap: function(options, operation)
                        {
                            if (operation != "read" && options.models)
                            {
                                return {models: kendo.stringify(options.models)};
                            }
                        }
                    },

                batch: true,
                schema: {
                    model: {
                        id: "id",
                        fields: {
                            subnet: { type: "string"},
                            subnetMask: { type: "string" },
                            gateway: { type: "string"},
                            status: { type: "string" },
                        }
                    }
                }
            };


        callbackContext['columns'] = [
            {field:"id", hidden:true},
            { field:"subnet", title: "Subnet",template:'# if (subnet) { # <span title="#:subnet#">#: subnet # </span># } else { #<span></span># } #'},
            { field: "subnetMask", title:"Subnet Mask",width:'25%',template:'# if (subnetMask) { # <span title="#:subnetMask#">#: subnetMask # </span># } else { #<span></span># } #'},
            { field: "gateway", title:"Gateway",width:'20%',template:'# if (gateway) { # <span title="#:gateway#">#: gateway # </span># } else { #<span></span># } #'},
            {
                width:'20%',
                command: [
                    {name: "Delete", text: "", iconClass: "fa fa-trash",
                        click:function (e)
                        {
                            e.preventDefault();

                            var tr = $(e.target).closest("tr");

                            var data = this.dataItem(tr);

                            var context = {};

                            context.data = data;

                            context.windowId = 'deleteModal';

                            context.title = 'Do you really want to delete the selected Subnet?';

                            context.yesButtonEvent = discovery.deleteSubnetConfirmationPrompt;

                            context.noButtonEvent = table.closeDeleteWindow;

                            context.grid = "DiscoveredSubnet";

                            flux.deleteWindowExecute(context);
                        }},
                    {name: "Add", text: "", title:"Add", iconClass: "fa fa-plus",
                        click:function (e)
                        {
                            e.preventDefault();

                            var tr = $(e.target).closest("tr");

                            var data = this.dataItem(tr);

                            var modal = $("#addModal");

                            modal.width('650px');

                            flux.kendoWindowSetOption({kendoWindow:modal});

                            $('#addModal_wnd_title').html('Add Subnet');

                            modal.data("kendoWindow").content('<form id="subnetManuallyForm"> <div class="fixed-height-body-popup-panel margin-t-20 widthFullSpacer"> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <label for="categoryId">Select Category</label> <select id="categoryId" name="categoryId" class="k-dropdown" required validationMessage="Category Name is required"/></div><div class="col-xs-12 col-md-12 col-lg-6"> <button id="categoryButton" class="primery-btn margin-t-15" title="Add Category"> <i class="fa fa-plus"></i> Add Category </button> <div id="categoryModal"></div></div></div><div id="subnettype" class="row"><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"><label for="gatewayId">Select Gateway</label><select id="gatewayId" name="gatewayId" class="k-dropdown"/></div><div class="col-xs-12 col-md-12 col-lg-6"><button id="gatewayButton" class="primery-btn margin-t-15" title="Add Gateway"> <i class="fa fa-plus"></i> Add Gateway </button><div id="gatewayModal"></div></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-6"> <label for="subnetAddress">Subnet/Network Address</label> <input id="subnetAddress" name="subnetAddress" type="text" class="k-textbox" maxlength="40" required validationMessage="Subnet Address is required" readonly/></div><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <label for="subnetMask">Subnet Mask / Prefix </label> <input id="subnetMask" name="subnetMask" class="k-textbox" maxlength="25" type="text" required validationMessage="Subnet Mask is required" readonly/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-6"> <label for="subnetName">Subnet Name</label> <input id="subnetName" name="subnetName" type="text" class="k-textbox" maxlength="50" required validationMessage="Subnet Name is required"/></div><div class="col-xs-12 col-md-12 col-lg-6"> <label for="dnsAddress">DNS Address</label> <input id="dnsAddress" name="dnsAddress" type="text" class="k-textbox" maxlength="25"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-6"> <label for="vlanName">VLAN Name</label> <input id="vlanName" name="vlanName" type="text" class="k-textbox" maxlength="50"/></div><div class="col-xs-12 col-md-12 col-lg-6"> <label for="location">Location</label> <input id="location" name="location" type="text" class="k-textbox" maxlength="50"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="description">Description</label> <input id="description" name="description" type="text" class="k-textbox" maxlength="100"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12 select-drop-menu"> <input type="checkbox" id="scheduleStatus" name="scheduleStatus" class="k-checkbox" value="true"/> <label for="automaticScanning">Automatic Scanning</label> <div id="autoScheduler"></div></div><div class="col-xs-12 col-md-12 col-lg-12"> <input type="checkbox" id="allowIcmp" name="allowIcmp" class="k-checkbox" value="true"/> <label class="k-checkbox-label" for="allowIcmp">To check the IP Availability using ICMP</label> </div><div class="col-xs-12 col-md-12 col-lg-12"> <input type="checkbox" id="allowDns" name="allowDns" class="k-checkbox" value="true"/> <label class="k-checkbox-label" for="allowDns">To perform DNS forward and reverse lookup</label> </div></div></div><div class="footer-box-grid-box margin-t-10 align-right">  <div class="bottom-form-panel"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="addSubnet">Add</button> <button class="defualt-btn k-button k-grid-cancel" id="addCancelSubnet">Cancel</button></div></div></form>');

                            var formContext = $('#subnetManuallyForm');

                            var categoryList = $("#categoryId");

                            flux.getKendoDropDownListURL({dropDownId:categoryList,url:"/category/",dataTextField: "categoryName",dataValueField: "id"});

                            var gatewayList = $("#gatewayId");

                            flux.getKendoDropDownListURL({dropDownId:gatewayList,url:"/gateway/",dataTextField: "gateway",dataValueField: "id"});

                            flux.bindKendoButtonClickEvent({element: 'gatewayButton'}, homeManager.onAddGatewayButtonClick);

                            flux.bindElementEvent({event:'change',element:'addModal',selector:'#gatewayId' ,isEditable:true},homeManager.onGatewayChange);

                            appManager.initCustomScrollbar({container:$(".fixed-height-body-popup-panel")});

                            flux.bindKendoButtonClickEvent({element: 'categoryButton'}, homeManager.onAddCategoryButtonClick);

                            appManager.executeGETRequest({url: '/discoveredSubnet/'+data.id,container:formContext,callback: formManager.renderForm,params:data.id,postCallback:homeManager.afterEditSubnetFormRender});

                            flux.bindElementEvent({event:'change',element:'addModal',selector:'#scheduleStatus' ,isEditable:true},homeManager.renderAutoSchedulerDropDown);

                            flux.bindKendoButtonClickEvent({element: 'addSubnet',kendoModal:modal},homeManager.addSubnetEntity);

                            flux.bindKendoButtonClickEvent({element : 'addCancelSubnet',kendoModal:modal},flux.closeKendoModal);

                        }}
                ]
            }
        ];

        discoveredSubnetTable.kendoTooltip({ filter: ".k-grid-Delete", content: "Delete", showAfter: 1000, position: "bottom"});

        $("#popupContent").kendoTooltip({ filter: "li[title]",showAfter: 1000, position: "bottom" });

        table.renderTable(callbackContext);

        var filter = $("#searchFilterDiscoveredSubnet");

        table.searchFilter(discoveredSubnetTable, filter);

        loaderUtil.hideModalLoader();
    },

    renderGatewayGrid : function (context)
    {
        if(context.json.data != null && context.json.success == true)
        {
            var data = context.json.data;

            context.container.success(data);

        }
        else
        {
            context.container.success("");

            $(".k-grid-content").html(appConstant.NoDataSpan);
        }
        loaderUtil.hideCentralModalLoader();
    },


    addGatewayEntity :function (event)
    {
        if(event)
        {
            event.preventDefault();

            var kendoEvent = event.sender.options.prefix.kendoModal;

            var form = $("#gatewayForm");

            var param = formManager.serializeForm(form);

            var validator = form.kendoValidator().data("kendoValidator");

            if(validator.validate())
            {
                loaderUtil.showModalLoader();

                appManager.executePOSTRequest({ url: '/gateway/', container: kendoEvent, isInit:true, callback: discovery.afterGatewayAddedOrUpdated, params: param});
            }
        }
    },


    afterGatewayAddedOrUpdated : function (context)
    {
        if(context)
        {
            var gridId = $("#gatewayTable");

            var modal = context.container;

            if(context.json.success == true)
            {
                modal.close();

                flux.refreshKendoGrid({gridId : gridId});

                notification.showNotification({notificationTitle:context.json.message,notificationType:"success"});
            }
            else
            {
                notification.showNotification({notificationTitle:context.json.message,notificationType:"error"});
            }

            loaderUtil.hideModalLoader();
        }
    },

    afterScanGateway : function (context)
    {
        if(context)
        {
            var gatewayTable = $("#gatewayTable");

            var discoveredSubnetTable = $("#discoveredSubnetTable");

            if(context.json.success === true)
            {
                notification.showNotification({notificationTitle:context.json.message,notificationType:"success"});

                flux.refreshKendoGrid({gridId : gatewayTable});

                flux.refreshKendoGrid({gridId : discoveredSubnetTable});

                appManager.executeGETRequest({url: '/statusScanGateway/', callback: discovery.handleScanStatusResponse});
            }
            else
            {
                notification.showNotification({notificationTitle:context.json.message,notificationType:"error"});
            }

            loaderUtil.hideModalLoader();
        }
    },

    afterGatewayDeleted : function (context)
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
            flux.refreshKendoGrid({gridId : gridId});
        }
    },

    deleteConfirmationPrompt : function (event)
    {
        if(event)
        {
            event.preventDefault();

            loaderUtil.showModalLoader();

            var modal =  event.data.kendoModal;

            var context =  event.data.data;

            var grid = $("#gatewayTable").data('kendoGrid');

            grid.dataSource.remove(context);

            grid.dataSource.sync();

            modal.close();

            loaderUtil.hideModalLoader();
        }
    },

    deleteSubnetConfirmationPrompt : function (event)
    {
        if(event)
        {
            event.preventDefault();

            loaderUtil.showModalLoader();

            var modal =  event.data.kendoModal;

            var context =  event.data.data;

            var grid = $("#discoveredSubnetTable").data('kendoGrid');

            grid.dataSource.remove(context);

            grid.dataSource.sync();

            modal.close();

            loaderUtil.hideModalLoader();
        }
    },

    onCloseButtonClick : function (event)
    {
        if(event)
        {
            event.event.preventDefault();

            var modal = event.sender.options.prefix.kendoModal;

            modal.close();

            var gridId = $("#gatewayTable");

            flux.refreshKendoGrid({gridId : gridId});

        }
    },

    editEntity : function (event)
    {
        if(event)
        {
            event.event.preventDefault();

            var kendoEvent = event.sender.options.prefix.kendoModal;

            var id = $(event.event.currentTarget).data('id');

            var form = $("#gatewayForm");

            var param =formManager.serializeForm(form);

            var validator = form.kendoValidator().data("kendoValidator");

            if(validator.validate())
            {
                loaderUtil.showModalLoader();

                appManager.executePUTRequest({url: '/gateway/'+id, container: kendoEvent,callback: discovery.afterGatewayAddedOrUpdated, params: param});
            }
          }
    },

    handleScanStatusResponse: function (callbackContext)
    {
        if (callbackContext.json.success === true)
        {
            if (callbackContext.intervalFunctionCall === undefined)
            {
                discovery.checkScanStatus = setInterval(function ()
                {
                    appManager.executeGETRequest({
                        url: '/statusScanGateway/',
                        intervalFunctionCall: true,
                        callback: discovery.handleScanStatusResponse
                    });
                }, 2000);
            }
        }
        else
        {
            clearInterval(discovery.checkScanStatus);

            var gatewayTable = $("#gatewayTable");

            var discoveredSubnetTable = $("#discoveredSubnetTable");

            flux.refreshKendoGrid({gridId : gatewayTable});

            flux.refreshKendoGrid({gridId : discoveredSubnetTable});
        }
    },

};
