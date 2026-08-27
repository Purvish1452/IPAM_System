/**
 * Created by hardik on 27/5/18.
 */
var subnetSummary =
    {
        SubnetSummaryPage: 'subnetSummaryPage',

        SubnetSummaryDetails : 'subnetSummary', SubnetIPAddresses : 'subnetIPAddresses',

        subnetScopeAddress:undefined,

        checkScanStatus:undefined,

        breadCrumbMenu:'',

        searchedValue:'',

        popupContent : '',

        // -------------------------------------------------------------------------Subnet Page init,chart & grid render-------------------------------------------------------------------------------------------------//

        renderSubnetPage : function (event,subnetId,scopeAddress,breadCrumbNavigation,searchField)
        {
            var menuId;

            var menuName;

            var breadCrumbMenus;

            var searchData;

            if(event != null && event != undefined)
            {
                menuId = $(event.currentTarget).data('uid');

                menuName = $(event.currentTarget).data('name');

                breadCrumbMenus = event.data.breadCrumbMenu;

                searchData = event.data.searchedValue;
            }

            if(subnetId)
            {
                menuId = subnetId;

                menuName = scopeAddress;

                breadCrumbMenus = breadCrumbNavigation;

                searchData = searchField;
            }

            if(menuId)
            {
                switch (breadCrumbMenus)
                {
                    case "home":
                        breadCrumbMenuName = "Home";
                        break;
                    case "ipAddressSummary":
                        breadCrumbMenuName = "Home";
                        break;
                    case  "globalSearch":
                        breadCrumbMenuName = "Global Search";
                        break;
                    default:
                        breadCrumbMenuName = "Home";
                }

                loaderUtil.showModalLoader();

                var headerPanel = '<div class="dropdown" style="position: relative; display: inline-block;"><button class="primery-btn dropdown-toggle" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" style="background-color: #007bff; color: white; border: none; padding: 10px 20px; font-size: 14px; cursor: pointer; border-radius: 5px; transition: background 0.3s ease-in-out; white-space: nowrap; display: flex; align-items: center; justify-content: center; width: 100px;">Actions <span class="caret"></span></button>' +
                    '<ul id="actionDropDownMenu" class="dropdown-menu" aria-labelledby="dropdownMenuButton" style="position: absolute; top: 100%; left: auto; right: 0; background: white; border: 1px solid #ccc; border-radius: 5px; list-style: none; padding: 5px; width: 120px; display: none; box-shadow: 0px 4px 6px rgba(0,0,0,0.1);"><li><button id="subnetButton" class="primery-btn" title="Add Subnet" data-panel="addSubnetModal" style="background: none; border: none; width: 100%; text-align: left; padding: 8px; font-size: 14px; cursor: pointer; color: #007bff; outline: none; box-shadow: none; user-select: none;" onmouseover="this.style.color=\'#0056b3\';" onmouseout="this.style.color=\'#007bff\';" onfocus="this.blur();">' +
                    'Add Subnet</button></li><li><button id="supernetButton" class="primery-btn" title="Add Supernet" data-panel="addSupernetModal" style="background: none; border: none; width: 100%; text-align: left; padding: 8px; font-size: 14px; cursor: pointer; color: #007bff; outline: none; box-shadow: none; user-select: none;" onmouseover="this.style.color=\'#0056b3\';" onmouseout="this.style.color=\'#007bff\';" onfocus="this.blur();">Add Supernet</button></li></ul></div>';

                $("#header_panel").html('<div class="title-inner-box"> Hi, '+$("#userName").val()+'</div><div class="breadcrumb-panel"><ul><li><a href="#" data-page="'+breadCrumbMenus+'" data-link=breadCrumbNavigation data-value="'+searchData+'">'+breadCrumbMenuName+'</a></li><li>'+menuName+'</li></ul></div><div class="corner-content">'+ headerPanel +'</div>');

                $("#container-panel").html('<div id="leftPanel" class="left-panel stickyScrollLeft"></div><div id="subnetSummaryPage" class="content-panel"></div><div id="right-panel" class="right-panel stickyScrollRight"></div>');

                navigationManager.addHistory("navigation=subnetSummary~~subnetId="+menuId+"~~scopeAddress="+menuName+"~~breadCrumbNavigation="+breadCrumbMenus+"~~searchField="+searchData);

                topManager.setActiveMenu('home');

                appManager.renderHTML(subnetSummary.SubnetSummaryPage, $("#subnetSummaryPage"),undefined);

                appManager.renderLeftRightPanel(menuName);

                appManager.toggleContentPanel();

                subnetSummary.loadSubnetSummary({eventId:menuId});

                flux.bindEvent({element:'subnetAction #subnetSummaryActions',widgetId:'subnetSummaryPopup'}, subnetSummary.openSubnetSummaryActionPopup);

                homeManager.loadHomeScreenActionMenuEvents();

                $('#deleteMultipleIP').hide();

                subnetSummary.loadIPAddresses({eventId:menuId,scopeAddress:menuName,searchedValue:searchData,breadCrumbMenu:breadCrumbMenus});

                loaderUtil.hideModalLoader();
            }
        },

        // -----------------------------------------------------------------------Render Subnet summary widget---------------------------------------------------------------------------------------------------//

        loadSubnetSummary : function (context)
        {
            if(context!=null && context!=undefined)
            {
                var id = context.eventId;

                $("#subnetSummary").html('<ul class="subnet-summary-panel"><li> <label>Subnet Address</label><p name="subnetAddress"></p></li><li> <label>Subnet Name</label><p name="subnetName"></p></li><div id="subnetMaskDiv"><li> <label>Subnet Mask</label><p name="subnetMask"></p></li></div><li> <label id="subnetCidrLabel" >Subnet CIDR </label><p name="subnetCidr"></p></li><li> <label>Subnet Usage</label><p name="subnetUsage"></p></li><li> <label>VLAN Name</label><p name="vlanName"></p></li><li> <label>Location</label><p name="location"></p></li><li> <label>Scope Type</label><p name="type"></p></li><li> <label>Description</label><p name="description"></p></li><li> <label>Last Scan Time</label><p name="lastScanTime"></p></li><li> <label>Total IPs</label><p name="totalIp"></p></li></ul>');

                appManager.executeGETRequest({url:'/subnet/'+id,callback:subnetSummary.renderSubnetSummaryDetails,params:id});

                appManager.executeGETRequest({url: '/ipSummary/'+id,callback:widgetRenderManager.renderPieChart,length:110,chartArea:{background:"transparent"},Padding:0,StartAngle:-45,params:'subnetSummaryGraph'});
            }
        },

        renderSubnetSummaryDetails : function (context)
        {
            if(context)
            {
                var result = context.json.data;

                var summaryContext = $('#subnetSummary').find('ul');

                if(result!=null && result!=undefined)
                {
                    if(summaryContext)
                    {

                        $.each( result, function(key, value){

                            var textValue = value;

                            if(textValue == null || textValue.length == 0 || textValue == "")
                            {
                                textValue = "N/A";
                            }
                            if(key == "usedIpPercentage")
                            {
                                key = "subnetUsage";

                                textValue = value+"%";
                            }
                            if(key === "ipv6" &&  value === true)
                            {
                                $("#subnetMaskDiv").hide();

                                $("#subnetCidrLabel").text("Prefix");

                                $("#selectIPRangeDiv").hide();

                                $("#addMultipleIPDiv").hide();
                            }

                            summaryContext.find('[name=' + key + ']').text(textValue);

                        });
                    }
                }
            }
        },

        openSubnetSummaryActionPopup : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var widgetId = $('.'+event.data.widgetId);

                var target = event.currentTarget;

                subnetSummary.popupContent.setOptions({
                    anchor: $(target),
                    position: "top center"
                });

                var id = navigationManager.getUrlParameter("subnetId");

                var scope = navigationManager.getUrlParameter("scopeAddress");

                widgetId.attr('data-uid',id);

                widgetId.attr('data-name',scope);

                subnetSummary.popupContent.open();

                subnetSummary.popupContent.position();
            }
        },

        // --------------------------------------------------------------------------------Load subnet summary grid & bind events------------------------------------------------------------------------------------------//

        loadIPAddresses : function (context)
        {
            if(context!=null && context!=undefined)
            {
                var id = context.eventId;

                var scopeAddress = context.scopeAddress;

                var gridId = $('#'+subnetSummary.SubnetIPAddresses);
                var lastUpdatedColumns = null; // Track last updated column structure

                var callbackContexts = {
                    Read: function (options) {
                        loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

                        // Fetch all available custom column names
                        appManager.executeGETRequest({
                            url: '/customColumn/',
                            callback: function (columnResponse) {
                                if (!columnResponse || !Array.isArray(columnResponse.json.data)) {
                                    console.warn("Failed to fetch column names.");
                                    return;
                                }

                                let availableColumns = columnResponse.json.data.map(item => item.columnName);// List of all valid custom column names

                                // Now fetch the subnet data
                                appManager.executeGETRequest({
                                    url: '/subnetIpBySubnet/' + id,
                                    container: options,
                                    callback: function (response) {
                                        if (!response || !Array.isArray(response.json.data)) {
                                            console.warn("Invalid data response.");
                                            return;
                                        }

                                        let gridData = response.json.data;
                                        let firstRowCustomColumns = gridData.length > 0 ? gridData[0].customColumns || {} : {};

                                        // Generate dynamic custom columns based on fetched column names
                                        let customFields = availableColumns.map(key => ({
                                            field: `customColumns.${key}`,
                                            title: key.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase()).trim(),
                                            template: `# if (customColumns["${key}"]) { #
                                                <span style="display: block; text-align: center; padding: 5px; white-space: normal;"
                                                      title="#: customColumns['${key}'] #">
                                                    #: customColumns['${key}'] #
                                                </span>
                                            # } else { #
                                                <span style="display: block; text-align: center; padding: 5px;"></span>
                                            # } #`,
                                            width: "10%"
                                        }));

                                        let newFields = [...callbackContexts.Fields, ...customFields];

                                        let grid = $(gridId).data("kendoGrid");

                                        if (grid) {
                                            let newColumnsJSON = JSON.stringify(newFields);
                                            if (newColumnsJSON !== lastUpdatedColumns) {
                                                lastUpdatedColumns = newColumnsJSON;

                                                requestIdleCallback(() => {
                                                    grid.setOptions({ columns: newFields });

                                                    setTimeout(() => {
                                                        if (grid.dataSource.total() === 0) {
                                                            grid.dataSource.read();
                                                        }
                                                    }, 150);
                                                });
                                            }
                                        } else {
                                            console.warn("Kendo Grid is not initialized yet.");
                                        }

                                        subnetSummary.renderSubnetIPSummaryGrid(response || []);
                                    }
                                });
                            }
                        });
                    },
                    EventId: subnetSummary.SubnetIPAddresses,
                    PageSize: 20,
                    pageable: {
                        refresh: true,
                        pageSizes: [10, 20, 50, 100],
                        buttonCount: 10
                    },
                    group: {
                        field: "ipAddress",
                        dir: "asc"
                    },
                    resizable: true,
                    groupable: true,
                    schema: {
                        model: {
                            id: "id",
                            fields: {
                                ipAddress: { type: "string" },
                                status: { type: "string" },
                                macAddress: { type: "string" },
                                deviceType: { type: "string" },
                                subnetName: { type: "string" },
                                authenticity: { type: "string" },
                                ipToDns: { type: "string" },
                                dnsToIp: { type: "string" },
                                lastAliveTime: { type: "string" }
                            }
                        }
                    },
                    Fields: [
                        {
                            headerTemplate: "<input type='checkbox' class='deleteMultipleIP' data-value='subnetIPAddresses' name='checkboxFilter' onclick='subnetSummary.selectAllCheckBox(this)' > " +
                                "<label class='k-checkbox-label label-2'>&nbsp;</label>",
                            width: "4%",
                            template: "<input type='checkbox' class='deleteMultipleIP' id='#= id #' data-value='subnetIPAddresses' name='checkboxFilter' onclick='subnetSummary.showHideBtnOnCheckboxClick()'>" +
                                "<label class='k-checkbox-label label-2'>&nbsp;</label>",
                            filterable: false
                        },
                        { field: "ipAddress", width: "10%", template: "<a data-uid='#: subnetId.id #' data-id='#: id#' data-link='ipAddress' data-value='#: ipAddress #' data-name='#:subnetId.subnetName#' title='#:ipAddress#'>#: ipAddress #</a>", title: "IP Address" },
                        { field: "status", title: "Status", width: "8%", attributes: { style: "text-align: center; white-space: normal;" } },
                        { field: "macAddress", title: "MAC Address", width: "10%", attributes: { style: "text-align: center; white-space: normal;" } },
                        { field: "deviceType", title: "Device Type", width: "10%", attributes: { style: "text-align: center; white-space: normal;" } },
                        { field: "ipToDns", title: "IP To DNS", width: "10%", attributes: { style: "text-align: center; white-space: normal;" } },
                        { field: "dnsToIp", title: "DNS To IP", width: "10%", attributes: { style: "text-align: center; white-space: normal;" } },
                        { field: "authenticity", title: "Authenticity", width: "10%", attributes: { style: "text-align: center; white-space: normal;" } },
                        { field: "lastAliveTime", title: "Last Alive Time", width: "13%", attributes: { style: "text-align: center; white-space: normal;" } }
                    ]
                };

                try {
                    gridId.data().kendoGrid.destroy();
                    gridId.empty();
                }
                catch(err)
                {
                }

                widgetRenderManager.renderGridData(callbackContexts);

                formManager.searchFilter($("#"+callbackContexts.EventId));

                flux.bindKendoButtonClickEvent({element: 'exportIP',eventId:id, exportType:1}, subnetSummary.onSubnetSummaryPageExport);

                flux.bindKendoButtonClickEvent({element: 'exportCsvIP',eventId:id, exportType:2}, subnetSummary.onSubnetSummaryPageExport);

                flux.bindKendoButtonClickEvent({element: 'addMultipleIP',eventId:id},subnetSummary.onMultipleIPAddButtonClick);

                flux.bindKendoButtonClickEvent({element: 'selectIPRange',eventId:id},subnetSummary.onSelectIPRangeButtonClick);

                flux.bindKendoButtonClickEvent({element: 'deleteMultipleIP'},subnetSummary.onMultipleDeleteButtonClick);

                flux.bindKendoButtonClickEvent({element : 'scanIP'},subnetSummary.onScanButtonClick);

                flux.bindKendoButtonClickEvent({element: 'importIP',eventId:id},subnetSummary.onSubnetIPImportCSVClick);

                flux.bindEvent({element: 'subnetIPAddresses', selector: 'a[data-link=ipAddress]',breadCrumbMenu:context.breadCrumbMenu,searchedValue:context.searchedValue},ipAddressSummary.loadIPAddressSummary);
            }
        },

        renderSubnetIPSummaryGrid : function (context)
        {
            if(context!=null && context!=undefined)
            {
                var result = context.json.data;

                if(result!=null && result !=undefined && context.json.success == true)
                {
                    context.container.success(result);

                    loaderUtil.hideCentralModalLoader();
                }
                else
                {
                    context.container.success("");

                    $(".k-grid-content").html(appConstant.NoDataSpan);

                    loaderUtil.hideCentralModalLoader();
                }
            }
        },

        // ------------------------------------------------------------------------------Checkbox selection of grid--------------------------------------------------------------------------------------------//

        selectAllCheckBox : function (event)
        {
            if(event)
            {
                var state = $(event).is(':checked');

                if (state == true)
                {
                    $('.deleteMultipleIP').prop('checked', true);
                }
                else
                {
                    $('.deleteMultipleIP').prop('checked', false);
                }
                subnetSummary.showHideBtnOnCheckboxClick();
            }
        },

        // ------------------------------------------------------------------------------- Show / hide Delete IP button on checkbox click------------------------------------------------//

        showHideBtnOnCheckboxClick : function ()
        {
            if($("input[name='checkboxFilter']:checked").length > 0 )
            {
                $('#deleteMultipleIP').show();
            }
            else
            {
                $('#deleteMultipleIP').hide();
            }
        },

        // ----------------------------------------------------------------------------Delete Multiple IP----------------------------------------------------------------------------------------------//

        onMultipleDeleteButtonClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var gridId = $("#"+subnetSummary.SubnetIPAddresses);

                var checkedId=[];

                var i = 0;

                gridId.find("input:checked").each(function(index,value)
                {
                    if(!$.isEmptyObject(value.id) && value.id.length > 0)
                    {
                        checkedId[i]=value.id;

                        i++;
                    }
                });

                if(checkedId.length > 0)
                {
                    if(appManager.validatePermission() == true)
                    {
                        var deleteIPCoseQuences = "Warning : Selected IP Addresses will be removed from the subnet !";

                        flux.bindKendoModalEvent({container: 'deleteModal',uniqueId:'deleteSubnetIP',coSequences:deleteIPCoseQuences,title:'Do you really want to delete the selected IP Addresses?', params:checkedId, closeCallback: flux.closeKendoDeleteModal,callback: subnetSummary.deleteConfirmationPrompt});
                    }
                }
                else
                {
                    notification.showNotification({notificationTitle:"No IP Address are selected", notificationType:"info"});
                }
            }
        },

        deleteConfirmationPrompt : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var gridId = $("#"+subnetSummary.SubnetIPAddresses);

                var context = event.data.context;

                var checkedId = context.params;

                var container = context.container;

                loaderUtil.showModalLoader();

                appManager.executeDELETERequest({url:'/subnetIp/'+checkedId,container:container,callback:subnetSummary.afterEntityDeleted, gridId:gridId});
            }
        },

        afterEntityDeleted : function (context)
        {
            if(context)
            {
                var modal = $("#"+context.container).data('kendoWindow');

                var gridId = context.gridId;

                gridId.find('input[type="checkbox"]')[0].checked=false;

                modal.close();

                if(context.json.success == true)
                {
                    navigationManager.doNavigation();

                    notification.showNotification({notificationTitle: context.json.message, notificationType:"success"});
                }
                else
                {
                    notification.showNotification({notificationTitle: context.json.message, notificationType:"error"});
                }

                loaderUtil.hideModalLoader();
            }
        },

        // -----------------------------------------------------------------------------Scan subnet modal---------------------------------------------------------------------------------------------//

        onScanButtonClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                if(appManager.validatePermission() == true)
                {
                    var scanId = navigationManager.getUrlParameter("subnetId");

                    var scopeAddress = navigationManager.getUrlParameter("scopeAddress");

                    flux.bindKendoModalEvent({container: 'deleteModal',uniqueId :'scan',title:'Do you really want to Start Scan for '+scopeAddress+' ?', scopeAddress : scopeAddress, params:scanId, closeCallback: flux.closeKendoDeleteModal, callback:subnetSummary.onSubnetScanConfirmationClick});
                }
            }
        },

        onSubnetScanConfirmationClick : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var context = event.data.context;

                var subnetId =  context.params;

                var container = context.container;

                var scopeAddress = context.scopeAddress;

                $("#"+container).data('kendoWindow').close();

                appManager.executeGETRequest({url:'/scanSubnet/'+subnetId,scanId:subnetId,scopeAddress:scopeAddress, callback:subnetSummary.afterSubnetScanned});
            }
        },

        // ------------------------------------------------------------------------------Scan header discovery--------------------------------------------------------------------------------------------//

        initRunningSubnetTracking:function ()
        {
            appManager.executeGETRequest({url:'/statusScanSubnet/', callback:subnetSummary.afterStatusChecked});
        },

        afterStatusChecked:function (callbackContext)
        {
            var statusMessage = "Scan Completed Successfully";

            if(callbackContext.json.success === true)
            {
                if(callbackContext.json.message)
                {
                    subnetSummary.subnetScopeAddress = callbackContext.json.message;
                }

                if(callbackContext.intervalFunctionCall == undefined)
                {
                    //clearInterval(subnetSummary.checkScanStatus);

                    subnetSummary.setRunningDiscoveryBlinkHTML();

                    subnetSummary.checkScanStatus = setInterval(function()
                    {
                        appManager.executeGETRequest({url:'/statusScanSubnet/',intervalFunctionCall:true,callback:subnetSummary.afterStatusChecked});

                    }, 5000);
                }
            }
            else
            {
                $('#discovery-title').remove();

                subnetSummary.subnetScopeAddress = undefined;

                clearInterval(subnetSummary.checkScanStatus);

                if(callbackContext.intervalFunctionCall == true)
                {
                    navigationManager.renderPageAfterDiscovery(statusMessage);
                }
            }
        },

        setRunningDiscoveryBlinkHTML:function ()
        {
            //$('#discovery-title').remove();

            $('body').append('<div class="discoveryPositionSection" id="discovery-title"><div class="discoveryMiddle"> <span id ="discovery-progress" class="fa fa-spinner fa-spin discovery-progress txt-color-white" data-original-title="Discovery Progress" rel="tooltip"></span><div class="discovery-title"> <span class="label bg-color-blue discoveryTextSubnet" rel="tooltip" title="Scan is Running for @@@">Scan is Running for @@@</span></div></div></div>'.replace(/@@@/g,subnetSummary.subnetScopeAddress));
        },

        afterSubnetScanned : function (callbackContexts)
        {
            if(callbackContexts && callbackContexts.json.success == true)
            {
                clearInterval(subnetSummary.checkScanStatus);

                subnetSummary.subnetScopeAddress = callbackContexts.scopeAddress;

                subnetSummary.setRunningDiscoveryBlinkHTML();

                subnetSummary.checkScanStatus = setInterval(function()
                {
                    appManager.executeGETRequest({url:'/statusScanSubnet/',intervalFunctionCall:true,callback:subnetSummary.afterStatusChecked});

                }, 5000);

                notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"info"});
            }
            else
            {
                if(callbackContexts.json.message == "Please wait for some time, Import is running")
                {
                    notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"info"});
                }
                else
                {
                    notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"error"});
                }

            }
        },

        // --------------------------------------------------------------------------------Import Subnet IP------------------------------------------------------------------------------------------//

        onSubnetIPImportCSVClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                if(appManager.validatePermission() == true)
                {
                    var importCSVModal = $("#addModal");

                    var id = event.sender.options.prefix.eventId;

                    importCSVModal.width('600px');

                    flux.kendoWindowSetOption({height:'200px',kendoWindow:importCSVModal});

                    $('#addModal_wnd_title').html('Import CSV');

                    importCSVModal.data('kendoWindow').content('<form id="subnetIPCSVForm"><div class="fixed-height-body-popup-panel margin-t-20"><div class="row"><div class="col-xs-12 col-md-12 col-lg-12 "> <label for="subnetIpCsv">IP Address CSV</label><div class=""><div class="file-title"></div> <input class="upload-input-file" type="file" id="subnetIpCsv" name="subnetIpCsv" accept=".csv" required validationMessage="CSV File is required"/> <i class="fa fa-folder-open-o"></i></div> <a class="hyperClick" id="downloadCsv">Download Sample CSV</a></div></div></div><div class="footer-box-grid-box margin-t-10 align-right"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="importSubnetIPButton">Import</button> <button class="k-button k-button-icontext float-l" id="cancelSubnetIPButton">Cancel</button></div></form>');

                    flux.bindKendoButtonClickEvent({element:'importSubnetIPButton',kendoModal:importCSVModal,id:id},subnetSummary.importIPCSVFile);

                    flux.bindKendoButtonClickEvent({element:'downloadCsv'},subnetSummary.downloadCsvForImport);

                    flux.bindKendoButtonClickEvent({element : 'cancelSubnetIPButton',kendoModal:importCSVModal},flux.closeKendoModal);
                }
            }
        },
        downloadCsvForImport : function ()
        {
            appManager.executeGETRequest({url:"/customColumn/download/",callback:subnetSummary.downloadCsv});
        },
        importIPCSVFile : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var kendoEvent = event.sender.options.prefix;

                var kendoModal = kendoEvent.kendoModal;

                var id = kendoEvent.id;

                var form = $("#subnetIPCSVForm");

                var validator = form.kendoValidator().data("kendoValidator");

                if(validator.validate())
                {
                    var formData = new FormData();

                    formData.append('subnetIpCsv', $("#subnetIpCsv")[0].files[0]);

                    formData.append('subnetId',id);

                    loaderUtil.showModalLoader();

                    loaderUtil.showCentralModalLoader(appConstant.ImportMessage);

                    kendoModal.data('kendoWindow').close();

                    appManager.executeFileRequest({url: '/subnetIpByCSV/',type: 'POST', eventId : id,container: kendoModal, callback: subnetSummary.afterCSVFileAdded,params: formData});
                }

            }
        },

        afterCSVFileAdded : function (callbackContext)
        {
            if(callbackContext)
            {
                if (callbackContext.json.success == true)
                {
                    navigationManager.doNavigation();

                    notification.showNotification({ notificationTitle: callbackContext.json.message, notificationType: "success"});
                }
                else
                {
                    notification.showNotification({ notificationTitle: callbackContext.json.message, notificationType: "error"});
                }
                loaderUtil.hideCentralModalLoader();

                loaderUtil.hideModalLoader();
            }
        },

        // ---------------------------------------------------------------------------------Select Multiple IP Modal-----------------------------------------------------------------------------------------//

        onSelectIPRangeButtonClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                if(appManager.validatePermission() == true)
                {
                    var modal = $("#addModal");

                    var id = event.sender.options.prefix.eventId;

                    modal.width('500px');

                    flux.kendoWindowSetOption({kendoWindow:modal});

                    $('#addModal_wnd_title').html('Select IP Range');

                    modal.data("kendoWindow").content('<form id="selectIPRangeForm"><div class="fixed-height-body-popup-panel  margin-t-20"><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"> <label for="startIp">Start IP</label> <input id="startIp" type="text" name="startIp" class="k-textbox" required validationMessage="Start IP is required" maxlength="40" /></div><div class="col-xs-12 col-md-12 col-lg-6"> <label for="endIp">End IP</label> <input id="endIp" type="text" name="endIp" class="k-textbox" required validationMessage="End IP is required" maxlength="40" /></div></div></div><div class="footer-box-grid-box margin-t-10 align-right"><div class="bottom-form-panel"> <button class="k-button k-button-icontext k-grid-cancel float-l" id="ipRangeCancelButton">Cancel</button> <button class="k-button k-button-icontext k-primary k-grid-update" id="multipleSubnetEdit">Select + Edit<div id="selectIPModal"></div> </button> <button class="k-button k-button-icontext k-primary k-grid-update" id="multipleSubnetDelete">Select + Remove</button></div></div></form>');

                    flux.bindElementEvent({event:'keyup',element:'addModal',selector:'#startIp'},flux.hostAddressValidation);

                    flux.bindElementEvent({event:'keyup',element:'addModal',selector:'#endIp'},flux.hostAddressValidation);

                    flux.bindKendoButtonClickEvent({element:'ipRangeCancelButton',kendoModal:modal}, flux.closeKendoModal);

                    flux.bindKendoButtonClickEvent({element:'multipleSubnetEdit',id:id}, subnetSummary.editMultipleSelectedIP);

                    flux.bindKendoButtonClickEvent({element:'multipleSubnetDelete',kendoModal:modal,id:id}, subnetSummary.deleteMultipleSelectedIP);
                }
            }
        },

        // -----------------------------------------------------------------------------------Edit selected IP---------------------------------------------------------------------------------------//

        editMultipleSelectedIP : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var id = event.sender.options.prefix.id;

                var form = $("#selectIPRangeForm");

                var validator = form.kendoValidator().data("kendoValidator");

                if(validator.validate())
                {
                    var modal = $('#innerModal');

                    modal.width('500px');

                    flux.kendoWindowSetOption({kendoWindow:modal});

                    $('#innerModal_wnd_title').html('Edit IP Status');

                    modal.data("kendoWindow").content('<form id="selectIPStatusForm"><div class="fixed-height-body-popup-panel margin-t-20"><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"> <label for="startIp">Start IP Range</label> <input id="startIp" type="text" name="startIp" value="'+$("#startIp").val()+'" readonly class="k-textbox" required validationMessage="Start IP Range required"/></div><div class="col-xs-12 col-md-12 col-lg-6"> <label for="endIp">End IP Range</label> <input id="endIp" value="'+$("#endIp").val()+'" type="text" name="endIp" readonly class="k-textbox" required validationMessage="End IP Range required"/></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <label for="status">Status</label> <select id="status" name="status" class="k-dropdown"/></div></div></div><div class="footer-box-grid-box margin-t-10 align-right"><div class="bottom-form-panel"> <button class="k-button k-button-icontext k-grid-cancel float-l" id="selectStatusCancelButton">Cancel</button> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="selectStatusAddButton">Add</button></div></div></form>');

                    var dropDownId = $("#status");

                    var data = [{text: "Used", value: "Used" }, {text: "Available", value: "Available" },{text: "Transient", value: "Transient" },{text: "Reserved", value: "Reserved" }];

                    flux.getKendoDropDownList({dropDownId:dropDownId,dataTextField: "value",dataValueField: "text",data:data});

                    flux.bindKendoButtonClickEvent({element: 'selectStatusAddButton', kendoModal:modal , gridId:id}, subnetSummary.onSelectIPStatusClick);

                    flux.bindKendoButtonClickEvent({element: 'selectStatusCancelButton', kendoModal:modal}, flux.closeKendoModal);
                }
            }
        },

        onSelectIPStatusClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var kendoEvent = event.sender.options.prefix;

                var kendoModal = kendoEvent.kendoModal;

                var id = kendoEvent.gridId;

                var formData = new FormData();

                formData.append('startIp', $("#startIp").val());

                formData.append('endIp', $("#endIp").val());

                formData.append('status', $("#status").find(":selected").val());

                formData.append('subnetId',id);

                loaderUtil.showModalLoader();

                appManager.executeFileRequest({url: '/updateSubnetIpRange/',type: 'POST', container: kendoModal, gridId:id, callback: subnetSummary.afterStatusSelectEntity, params: formData});
            }
        },

        afterStatusSelectEntity : function (context)
        {
            if(context)
            {
                navigationManager.doNavigation();

                if(context.json.success == true)
                {
                    $("#innerModal").data('kendoWindow').close();

                    notification.showNotification({notificationTitle: context.json.message, notificationType:"success"});
                }
                else
                {
                    notification.showNotification({notificationTitle: context.json.message, notificationType:"error"});
                }
                loaderUtil.hideModalLoader();
            }
        },

        // --------------------------------------------------------------------------------Delete selected IP------------------------------------------------------------------------------------------//

        deleteMultipleSelectedIP : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var kendoEvent = event.sender.options.prefix;

                var kendoModal = kendoEvent.kendoModal;

                var id = kendoEvent.id;

                var form = $("#selectIPRangeForm");

                var validator = form.kendoValidator().data("kendoValidator");

                if(validator.validate())
                {
                    var formData = new FormData();

                    formData.append('startIp', $("#startIp").val());

                    formData.append('endIp', $("#endIp").val());

                    formData.append('subnetId',id);

                    loaderUtil.showModalLoader();

                    appManager.executeFileRequest({url:'/deleteSubnetIpRange/',type: 'POST',container:kendoModal,callback:subnetSummary.afterSelectedIPDeleted,params:formData})
                }
            }
        },

        afterSelectedIPDeleted : function (context)
        {
            if(context)
            {
                var kendoModal = context.container;

                kendoModal.data('kendoWindow').close();

                subnetSummary.renderSubnetGrid();

                if(context.json.success == true)
                {
                    notification.showNotification({notificationTitle: context.json.message, notificationType:"success"});
                }
                else
                {
                    notification.showNotification({notificationTitle: context.json.message, notificationType:"error"});
                }
                loaderUtil.hideModalLoader();
            }
        },

        // ---------------------------------------------------------------------------------Add Multiple IP Range -----------------------------------------------------------------------------------------//

        onMultipleIPAddButtonClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                if(appManager.validatePermission() == true)
                {
                    var modal = $("#addModal");

                    var id = event.sender.options.prefix.eventId;

                    modal.width('500px');

                    flux.kendoWindowSetOption({kendoWindow:modal});

                    $('#addModal_wnd_title').html('Add Multiple IP');

                    modal.data("kendoWindow").content('<form id="addMultipleIPForm"><div class="fixed-height-body-popup-panel margin-t-20"><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"> <label for="startIp">Start IP</label> <input id="startIp" type="text" name="startIp" class="k-textbox" required validationMessage="Start IP is required" maxlength="40" /></div><div class="col-xs-12 col-md-12 col-lg-6"> <label for="endIp">End IP</label> <input id="endIp" type="text" name="endIp" class="k-textbox" required validationMessage="End IP is required" maxlength="40" /></div></div></div><div class="footer-box-grid-box margin-t-10 align-right"><div class="bottom-form-panel"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="addMultipleIPButton">Add</button> <button class="k-button k-button-icontext k-grid-cancel float-l" id="cancelMultipleIPButton">Cancel</button></div></div></form>');

                    flux.bindElementEvent({event:'keyup',element:'addModal',selector:'#startIp'},flux.hostAddressValidation);

                    flux.bindElementEvent({event:'keyup',element:'addModal',selector:'#endIp'},flux.hostAddressValidation);

                    flux.bindKendoButtonClickEvent({element: 'addMultipleIPButton', kendoModal:modal ,id:id}, subnetSummary.onAddMultipleIPEntity);

                    flux.bindKendoButtonClickEvent({element: 'cancelMultipleIPButton', kendoModal:modal}, flux.closeKendoModal);
                }
            }
        },

        onAddMultipleIPEntity : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var kendoEvent = event.sender.options.prefix;

                var kendoModal = kendoEvent.kendoModal;

                var id = kendoEvent.id;

                var form = $("#addMultipleIPForm");

                var validator = form.kendoValidator().data("kendoValidator");

                if(validator.validate())
                {
                    var formData = new FormData();

                    formData.append('startIp', $("#startIp").val());

                    formData.append('endIp', $("#endIp").val());

                    formData.append('subnetId',id);

                    loaderUtil.showModalLoader();

                    loaderUtil.showCentralModalLoader(appConstant.SubnetMultipleIPAddMessage);

                    kendoModal.data('kendoWindow').close();

                    appManager.executeFileRequest({url: '/activeSubnetIpRange/',type: 'POST', container: kendoModal,  eventId:id,callback: subnetSummary.afterEntityAddedOrUpdated, params: formData});
                }
            }
        },

        // ---------------------------------------------------------------------------------------Render subnet Grid-----------------------------------------------------------------------------------//

        renderSubnetGrid : function ()
        {
            var gridId = $("#"+subnetSummary.SubnetIPAddresses);

            gridId.data("kendoGrid").dataSource.read(1);
        },

        // --------------------------------------------------------------------------------------Export subnet grid------------------------------------------------------------------------------------//

        onSubnetSummaryPageExport : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var context = event.sender.options.prefix;

                var gridId = $("#"+subnetSummary.SubnetIPAddresses);

                var checkedId=[];

                var i = 0;

                gridId.find("input:checked").each(function(index,value)
                {
                    if(value.id.length > 0)
                    {
                        checkedId[i]=value.id;

                        i++;
                    }
                });

                var pdfId = context.eventId;

                var type = context.exportType;

                loaderUtil.showModalLoader();

                loaderUtil.showCentralModalLoader(appConstant.ExportMessage);

                if(type == 1)
                {
                    appManager.executeGETRequest({url:"/exportPdfSubnetIp/"+pdfId+ "," + checkedId,callback:subnetSummary.downloadPdf});
                }
                else if (type == 2)
                {
                    appManager.executeGETRequest({url:"/exportCsvSubnetIp/"+pdfId+ "," + checkedId,callback:subnetSummary.downloadCsv});
                }
            }
        },

        downloadPdf : function (callbackContext)
        {
            if(callbackContext.json.data!=null && callbackContext.json.data !=undefined)
            {
                window.location.href = "/downloadPdf/"+encodeURIComponent(callbackContext.json.data);
            }
            else
            {
                notification.showNotification({notificationTitle:"No Data Available",notificationType:"info"});
            }
            loaderUtil.hideModalLoader();

            loaderUtil.hideCentralModalLoader();
        },

        downloadCsv : function (callbackContext)
        {
            if(callbackContext.json.data!=null && callbackContext.json.data !=undefined)
            {
                window.location.href = "/downloadCsv/"+encodeURIComponent(callbackContext.json.data);
            }
            else
            {
                notification.showNotification({notificationTitle:"No Data Available",notificationType:"info"});
            }
            loaderUtil.hideModalLoader();

            loaderUtil.hideCentralModalLoader();
        },

        // -------------------------------------------------------------------------------Close kendo modal-------------------------------------------------------------------------------------------//

        closeKendoModal: function (event)
        {
            if(event)
            {
                event.preventDefault();

                var modal =  event.data.kendoModal;

                modal.close();
            }
        },

        // -----------------------------------------------------------------------------------After subnet add & update operation---------------------------------------------------------------------------------------//

        afterEntityAddedOrUpdated : function (callbackContexts)
        {
            if(callbackContexts)
            {
                navigationManager.doNavigation();

                if(callbackContexts.json.success == true)
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

        // ----------------------------------------------------------------------------Init subnet page on navigation----------------------------------------------------------------------------------------------//

        renderSubnetSummaryFromURL : function (subnetId,scopeAddress,breadCrumbNavigation,searchField)
        {
            subnetSummary.renderSubnetPage(null,subnetId,scopeAddress,breadCrumbNavigation,searchField);
        }
    };