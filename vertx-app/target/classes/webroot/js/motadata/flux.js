var flux =
{
    // ------------------------------------------------------------------Breadcrumb event bind--------------------------------------------------------------------------------------------------------//

    init: function ()
    {
        // Breadcrumb navigation event
        flux.bindBodyClickEvent({event: 'click' , element: $('body'), selector: 'a[data-link=breadCrumbNavigation]'}, navigationManager.doBreadCrumbNavigation);

        // User management grid popup
        flux.bindEvent({element:'updatePassword'},userManagement.onUpdatePasswordClick);

        // Home screen action popup content event
        flux.bindKendoButtonClickEvent({element:'exportConflictPdf',dataValue : homeManager.ConflictIP},homeManager.onDashboardActionBtnClick);

        flux.bindKendoButtonClickEvent({element:'exportDhcpPdf',dataValue : homeManager.DhcpUtilization},homeManager.onDashboardActionBtnClick);

        flux.bindKendoButtonClickEvent({element:'exportSubnetPdf',dataValue : homeManager.SubnetUtilization},homeManager.onDashboardActionBtnClick);

        flux.bindKendoButtonClickEvent({element:'exportIPAvailability',dataValue : homeManager.IpAvailability,title:'All_ IP_Availability_Chart',exportType:"png"},homeManager.onDashboardActionBtnClick);

        flux.bindKendoButtonClickEvent({element:'exportIPAvailabilityPdf',dataValue : homeManager.IpAvailability,title:'All_ IP_Availability_Chart',exportType:"pdf"},homeManager.onDashboardActionBtnClick);

        flux.bindKendoButtonClickEvent({element:'exportIPAvailabilitySvg',dataValue : homeManager.IpAvailability,title:'All_ IP_Availability_Chart',exportType:"svg"},homeManager.onDashboardActionBtnClick);

        flux.bindKendoButtonClickEvent({element:'recentlyDiscoveredPdf',dataValue : homeManager.RecentDiscovery},homeManager.onDashboardActionBtnClick);

        flux.bindKendoButtonClickEvent({element:'exportDnsStatusPdf',dataValue : homeManager.DnsStatusSummary,title:'DNS_Status_Chart',exportType:"pdf"},homeManager.onDashboardActionBtnClick);

        flux.bindKendoButtonClickEvent({element:'exportDnsStatusPng',dataValue : homeManager.DnsStatusSummary,title:'DNS_Status_Chart',exportType:"png"},homeManager.onDashboardActionBtnClick);

        flux.bindKendoButtonClickEvent({element:'exportDnsStatusSvg',dataValue : homeManager.DnsStatusSummary,title:'DNS_Status_Chart',exportType:"svg"},homeManager.onDashboardActionBtnClick);

        flux.bindKendoButtonClickEvent({element:'top10SubnetUtilizationPdf',dataValue : homeManager.Top10SubnetUtilization},homeManager.onDashboardActionBtnClick);

        flux.bindKendoButtonClickEvent({element:'top10CategoryUtilizationPdf',dataValue : homeManager.Top10CategoryUtilization},homeManager.onDashboardActionBtnClick);

        flux.bindKendoButtonClickEvent({element:'exportVendor',dataValue : homeManager.VendorWiseIp,title:"Vendorwise_IP_Chart",exportType:"png"},homeManager.onDashboardActionBtnClick);

        flux.bindKendoButtonClickEvent({element:'exportVendorPdf',dataValue : homeManager.VendorWiseIp,title:"Vendorwise_IP_Chart",exportType:"pdf"},homeManager.onDashboardActionBtnClick);

        flux.bindKendoButtonClickEvent({element:'exportVendorSvg',dataValue : homeManager.VendorWiseIp,title:"Vendorwise_IP_Chart",exportType:"svg"},homeManager.onDashboardActionBtnClick);

        // subnet summary action popup event
        flux.bindEvent({element: 'editSubnetAddress'}, leftPanel.onEditSubnetClick);

        flux.bindEvent({element: 'deleteSubnetAddress'}, leftPanel.removeSubnet);

        // Dhcp server summary action popup event
        flux.bindEvent({element:'scanDhcpServer'},dhcpServerStatistics.onScanServerBtnClick);

        flux.bindEvent({element:'exportDhcpServerPdf',dataValue:'dhcpChartSummaryGraph',title:"DHCP_Server_Summary_Chart",exportType:"pdf"},dhcpServerStatistics.onDhcpServerExportBtnClick);

        flux.bindEvent({element:'exportDhcpServerPng',dataValue:'dhcpChartSummaryGraph',title:"DHCP_Server_Summary_Chart",exportType:"png"},dhcpServerStatistics.onDhcpServerExportBtnClick);

        flux.bindEvent({element:'exportDhcpServerSvg',dataValue:'dhcpChartSummaryGraph',title:"DHCP_Server_Summary_Chart",exportType:"svg"},dhcpServerStatistics.onDhcpServerExportBtnClick);
    },

    // -----------------------------------------------------------------Browser history back btn navigation---------------------------------------------------------------------------------------------------------//

    onBrowserHistoryButtonClick: function (event)
    {
        event.preventDefault();

        navigationManager.doNavigation();
    },

    // ------------------------------------------------------------------Kendo btn event bind--------------------------------------------------------------------------------------------------------//

    bindKendoButtonClickEvent: function (context, callback)
    {
        $("#" + context.element).kendoButton({click: callback, prefix: context});
    },

    // ------------------------------------------------------------------Bind element event on id--------------------------------------------------------------------------------------------------------//

    bindEvent: function (context, callback)
    {
        if (context['selector'])
        {
            $("#" + context.element).on("click", context['selector'], context, callback);
        }
        else
        {
            $("#" + context.element).on("click", context, callback);
        }

    },

    // --------------------------------------------------------------------Bind body event------------------------------------------------------------------------------------------------------//

    bindBodyClickEvent: function (context, callback)
    {
        if(!context.event)
        {

            context['event'] = "click";

        }
        if (context['selector'])
        {
            context.element.on(context.event, context['selector'], context, callback);

        }
        else
        {
            context.element.on(context.event, context, callback);

        }

    },

    // --------------------------------------------------------------------Bind delete event------------------------------------------------------------------------------------------------------//

    bindDeleteEvent: function (context, callback)
    {
        if (context['selector'])
        {
            $("#" + context.element).one("click", context['selector'], context, callback);
        }
        else
        {
            $("#" + context.element).one("click", context, callback);
        }
    },

    bindElementEvent: function (context, callback)
    {
        if (context['selector'])
        {
            $("#" + context.element).on(context.event, context['selector'], context, callback);
        }

        else
        {

            $("#" + context.element).on(context.event, context, callback);
        }
    },

    // ------------------------------------------------------------------------Open kendo modal--------------------------------------------------------------------------------------------------//

    kendoWindowSetOption : function (context)
    {
        if(context)
        {
            var modal = context.kendoWindow;

                modal.kendoWindow({
                    draggable: false,
                    height: context.height,
                    modal: true,
                    pinned: false,
                    position: {
                        top: 100,
                        left: 100
                    },
                    resizable: false,
                    title: context.title,
                    width: context.width
                    /*deactivate: function () {
                        $(this.element).empty();
                    }*/
                }).data("kendoWindow").center().open();
            }
        },

    // --------------------------------------------------------------------------------Close kendo modal------------------------------------------------------------------------------------------//

    closeKendoModal: function (event) {
        if (event) {
            event.event.preventDefault();

            var kendoModal = event.sender.options.prefix.kendoModal;

            kendoModal.data('kendoWindow').close();
        }
    },

    // ---------------------------------------------------------------------------------Kendo dropdownlist remote data -----------------------------------------------------------------------------------------//

    getKendoDropDownListURL: function (context)
    {
        if (context != null && context != undefined)
        {
            var id = context.dropDownId;

            id.kendoDropDownList({
                dataTextField: context.dataTextField,
                dataValueField: context.dataValueField,
                optionLabel: context.optionLabel,
                valuePrimitive: true,
                dataSource:
                    {
                        transport:
                            {
                                read: function (options)
                                {
                                    appManager.executeGETRequest({ url: context.url, container: options, dropDownId: id, callback: flux.afterDropDownInit});
                                }
                            }
                    }
            });
        }
    },

    getKendoDropDownListURLWithCallback: function (context)
    {
        if (context != null && context != undefined)
        {
            var id = context.dropDownId;

            id.kendoDropDownList({
                dataTextField: context.dataTextField,
                dataValueField: context.dataValueField,
                optionLabel: context.optionLabel,
                valuePrimitive: true,
                dataSource:
                    {
                        transport:
                            {
                                read: function (options)
                                {
                                    appManager.executeGETRequest({ url: context.url, container: options, dropDownId: id, callback: context.callback});
                                }
                            }
                    }
            });
        }
    },

    // ----------------------------------------------------------------------------------Kendo multiselect dropdownlist remote data----------------------------------------------------------------------------------------//

    getMultiSelectKendoDropDownListURL: function (context) {
        if (context) {
            var id = context.dropDownId;

            id.kendoMultiSelect({
                value:context.value,
                dataTextField: context.dataTextField,
                dataValueField: context.dataValueField,
                dataSource: {
                    transport:
                        {
                            read: function (options)
                            {
                                appManager.executeGETRequest({ url: context.url, container: options, dropDownId: id, callback: flux.afterDropDownInit});
                            }
                        }
                },
                autoClose:true
            });
        }
    },

    // ---------------------------------------------------------------------------------Render dropdown-----------------------------------------------------------------------------------------//

    afterDropDownInit: function (context)
    {
        if (context.json.data != null && context.json.data!=undefined)
        {
            var result = context.json.data;

            context.container.success(result);
        }
        else
        {
            context.container.success("");
        }
    },

    // ---------------------------------------------------------------------------------Kendo dropdown with local data-----------------------------------------------------------------------------------------//

    getKendoDropDownList: function (context)
    {
        if (context) {
            var id = context.dropDownId;

            id.kendoDropDownList({
                value : context.value,
                dataTextField: context.dataTextField,
                dataValueField: context.dataValueField,
                dataSource: {
                    data: context.data
                },
                filter:context.filter
            });
        }
    },

    // ----------------------------------------------------------------------------------kendo multiselet dropdown with local data----------------------------------------------------------------------------------------//

    getMultiSelectKendoDropDownList: function (context) {
        if (context) {
            var id = context.dropDownId;

            id.kendoMultiSelect({
                value:context.value,
                dataTextField: context.dataTextField,
                dataValueField: context.dataValueField,
                dataSource: {
                    data: context.data
                },
                autoClose:true
            });
        }
    },

    // -------------------------------------------------------------------------------------kendo date picker-------------------------------------------------------------------------------------//

    getDatePicker : function (context)
    {
        if(context){
            var id = $("#"+context.eventId);
            id.kendoDatePicker({
                value: new Date(),
                min: new Date(Date.now()),
                format : "yyyy-MM-dd"
            });
        }

    },

    // ----------------------------------------------------------------------------------------Refresh kendo grid----------------------------------------------------------------------------------//

    refreshKendoGrid: function (context) {
        if (context) {
            var gridId = context.gridId;

            gridId.data("kendoGrid").dataSource.read();

            gridId.data("kendoGrid").refresh();

            if(gridId.data("kendoGrid").dataSource.data.length == 0)
            {
                context.container.success("");

                $(".k-grid-content").html(appConstant.NoDataSpan);
            }
        }
    },

    // ----------------------------------------------------------------------------------------Open kendo delete modal----------------------------------------------------------------------------------//

    deleteWindowExecute: function (context)
    {
        if (context)
        {
            var windowId = $("#" + context.windowId);

            windowId.width('550px');

            flux.kendoWindowSetOption({ height: '400', kendoWindow: windowId});

            var data = context.data;

            var coSequences = '';

            if(context && context.grid != undefined)
            {
                coSequences = context.coSequences;
            }

            var deleteWindow = windowId.data("kendoWindow");

            $('#deleteModal_wnd_title').html(context.title);

            if(context.grid == "DHCP")
            {
                deleteWindow.content('<div class="common-box-grid-panel"><div class="fixed-height-body-popup-panel margin-t-20 margin-b-10">'+coSequences+'</div><div class="footer-box-grid-box margin-t-10 align-right"><div class="bottom-form-panel"><button class="k-button k-button-icontext k-primary k-grid-update float-r" id="yesButton">Yes</button><button class="k-button k-button-icontext k-primary k-grid-update float-r" id="noButton">No</button></div></div></div>');
            }
            else
            {
                deleteWindow.content('<div class="common-box-grid-panel"><div class="fixed-height-body-popup-panel margin-t-20 margin-b-20 align-right"><div class="bottom-form-panel"><button class="k-button k-button-icontext k-primary k-grid-update float-r" id="yesButton">Yes</button><button class="k-button k-button-icontext k-primary k-grid-update float-r" id="noButton">No</button></div></div></div>');
            }

            deleteWindow.center().open();

            flux.bindDeleteEvent({element: 'yesButton', kendoModal: deleteWindow, data: data}, context.yesButtonEvent);

            flux.bindDeleteEvent({element: 'noButton', kendoModal: deleteWindow}, context.noButtonEvent);
        }
    },

    // ------------------------------------------------------------------------------------------open user update password modal--------------------------------------------------------------------------------//

    updatePasswordWindowExecute: function (context)
    {
        if (context)
        {
            userManagement.popupContent.close();

            var windowId = $("#" + context.windowId);

            windowId.width('450');

            var id  = context.gridId;

            flux.kendoWindowSetOption({title: context.title, height: '400', kendoWindow: windowId});

            var changePasswordWindow = windowId.data("kendoWindow");

            changePasswordWindow.content('<form id="editPasswordForm"><div class="common-box-grid-panel padding-t-20"><div class="fixed-height-body-popup-panel"><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"><label for="password">Password</label><input name="password" type="password" class="k-textbox" id="password" required validationMessage="Password is required" maxlength="20"/></div><div class="col-xs-12 col-md-12 col-lg-6"><label for="rePassword">Confirm Password</label><input name="Confirm Password" type="password" class="k-textbox" id="rePassword" required  maxlength="20"/><span id="password-error"></span></div></div><div class="row"><div class="footer-box-grid-box margin-t-10 align-right"><button class="k-button k-button-icontext k-primary k-grid-update float-r"id="editPasswordButton">Update</button></div></div></div></div></form>');

            changePasswordWindow.center().open();

            flux.bindKendoButtonClickEvent({ element: 'editPasswordButton', kendoModal: changePasswordWindow, rowId: id}, userManagement.editPasswordEntity);
        }
    },

    // ---------------------------------------------------------------------------------Open kendo modal-----------------------------------------------------------------------------------------//

    bindKendoModalEvent: function (context) {

        var kendoModalWindow = $("#" + context.container);

        var coSequences = '';

        if(context && context.uniqueId != undefined)
        {
            coSequences = context.coSequences;
        }

        kendoModalWindow.kendoWindow({
            visible: false,
            width: "550px",
            height: "400px",
            resizable: false,
            draggable: false,
            modal: true
        });

        var deleteWindow = kendoModalWindow.data("kendoWindow");

        $('#deleteModal_wnd_title').html(context.title);

        if(context.uniqueId == "scan" || context.uniqueId  == "deleteReport")
        {
            deleteWindow.content('<div class="common-box-grid-panel"><div class="fixed-height-body-popup-panel  margin-t-20 margin-b-20 align-right"><div class="bottom-form-panel"><button class="k-button k-button-icontext k-primary k-grid-update float-r" id="subentYesButton">Yes</button><button class="k-button k-button-icontext k-primary k-grid-update float-r" id="subentNoButton">No</button></div></div></div>');
        }
        else
        {
            deleteWindow.content('<div class="common-box-grid-panel"><div class="fixed-height-body-popup-panel margin-t-20 margin-b-10">'+coSequences+'</div><div class="footer-box-grid-box margin-t-10 align-right"><div class="bottom-form-panel"><button class="k-button k-button-icontext k-primary k-grid-update float-r" id="subentYesButton">Yes</button><button class="k-button k-button-icontext k-primary k-grid-update float-r" id="subentNoButton">No</button></div></div></div>');
        }

        deleteWindow.center().open();

        $("#" + context.container + " #subentYesButton").one("click", {context: context}, context.callback);

        $("#" + context.container + " #subentNoButton").one("click",{context: context},context.closeCallback);
    },

    // -----------------------------------------------------------------------------Close kendo delete modal---------------------------------------------------------------------------------------------//

    closeKendoDeleteModal : function (context)
    {
        $("#"+context.data.context.container).data('kendoWindow').close();
    },

    // -------------------------------------------------------------------------------Jquery number validation-------------------------------------------------------------------------------------------//

    numberValidation : function (event)
    {
        if(event)
        {
            var val = $(this).val();

            val = val.replace(/[^0-9\.]/g,'');

            if(val.split('.').length>0)
            {
                val =val.replace(/\.+$/,"");
            }
            $(this).val(val);
        }
    },

    maxNumberValidation : function (event)
    {
        if(event)
        {
            var val = $(this).val();

            val = val.replace(/[^0-9\.]/g,'');

            if(val > 60)
            {
                val = '60';
            }
            $(this).val(val);
        }
    },

    // --------------------------------------------------------------------------------Jquery host address validation------------------------------------------------------------------------------------------//

    hostAddressValidation : function (event)
    {
        if(event)
        {
            event.preventDefault();

            var id = $('#'+event.data.eventId);

            id.val(this.value);

            var val = $(this).val();

            var isLocalSubnet = $("#localSubnet");

            if(val !== undefined)
            {
                val = val.replace(/[^A-Fa-f0-9:.]/g, '');

                if(val.indexOf(":") !== -1 && isLocalSubnet.length && isLocalSubnet.is(':checked'))
                {
                    homeManager.loadIPV6dropDown();
                }
                else
                {
                    homeManager.loadIPV4dropDown();
                }
             }
            $(this).val(val);
        }
    },

    // -------------------------------------------------------------------------------Treeview search filter-------------------------------------------------------------------------------------------//

    searchTreeView : function (context)
    {
        var filterText = $(context.currentTarget).val().trim().toLowerCase();

        var id = $('#'+context.data.id);

        if (filterText !== "" )
        {
            id.find(".k-group .k-group .k-in").closest("li").hide();

            id.find(".k-group").closest("li").hide();

            id.find(".k-group .k-group .k-in").each(function ()
            {
                var value = $(this).text().toLowerCase();

                if(value.indexOf(filterText) != -1)
                {
                    $(this).closest('li.k-item').show();

                    $(this).closest(".k-item").closest(".k-group").closest(".k-item").show();
                }
            });

            id.data("kendoTreeView").expand(".k-item");
        }
        else
        {
            id.find(".k-group").find("li").show();

            id.find(".k-group").find("ul").hide();

            id.data("kendoTreeView").collapse(".k-item");
        }
    },

    searchTreeViewInInventory : function (context, viewName)
    {
        var filterText = $(context.currentTarget).val().trim().toLowerCase();

        var id = $('#'+viewName);

        var treeView = id.data("kendoTreeView");

        if (filterText !== "" )
        {
            id.find(".k-group .k-group .k-in").closest("li").hide();

            id.find(".k-group").closest("li").hide();

            id.find(".k-group .k-group .k-in").each(function ()
            {
                var value = $(this).text().toLowerCase();

                if(value.indexOf(filterText) != -1)
                {
                    $(this).closest('li.k-item').show();

                    $(this).closest(".k-item").closest(".k-group").closest(".k-item").show();
                }
            });

            id.data("kendoTreeView").expand(".k-item");
        }
        else
        {
            id.find(".k-group").find("li").show();

            id.find(".k-group").find("ul").hide();

            id.data("kendoTreeView").collapse(".k-item");
        }
    }
};