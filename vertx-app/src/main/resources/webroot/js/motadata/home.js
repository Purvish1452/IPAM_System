var homeManager =
    {
        HomeScreenPage: 'homeScreen', LeftPanel : 'leftPanel', RightPanel : 'rightPanel',

        SubnetUtilization:'subnetUtilization', DhcpUtilization : 'dhcpScopeUtilization', ConflictIP : 'conflictedIP', RecentDiscovery : 'recentlyDiscovered', DnsStatusSummary : 'dnsStatusSummary',Top10SubnetUtilization : 'top10SubnetUtilization', Top10CategoryUtilization : 'top10CategoryUtilization', IpAvailability : 'allIPAvailabilitySummary', VendorWiseIp :'vendorWiseIPAllocation', IpSummary : 'ipSummary',

        subnetType : {MANUALLY : 0, CSV : 1},

        importIPStatus : undefined,

        subnetPopup : {popupContent : ''},

        dhcpPopup : {popupContent : ''},

        conflictPopupDiv : {popupContent : ''},

        recentlyDiscoveredPopupDiv : {popupContent : ''},

        dnsStatusPopupDiv : {popupContent : ''},

        top10CategoryUtilizationPopupDiv : {popupContent : ''},

        top10SubnetUtilizationPopupDiv : {popupContent : ''},

        ipAvailabilityPopupDiv : {popupContent : ''},

        vendorPopupDiv : {popupContent : ''},

        // --------------------------------------------------------------------------------- Init Method with home screen grid, left & right panel , bind export event -----------------------------------------------------------------------------------------//
        init : function ()
        {
            loaderUtil.showModalLoader();

            loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

            navigationManager.addHistory("navigation=home");

            topManager.setActiveMenu('home');

            var headerPanel = '<div class="dropdown" style="position: relative; display: inline-block;"><button class="primery-btn dropdown-toggle" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" style="background-color: #007bff; color: white; border: none; padding: 10px 20px; font-size: 14px; cursor: pointer; border-radius: 5px; transition: background 0.3s ease-in-out; white-space: nowrap; display: flex; align-items: center; justify-content: center; width: 100px;">Actions <span class="caret"></span></button>' +
                '<ul id="actionDropDownMenu" class="dropdown-menu" aria-labelledby="dropdownMenuButton" style="position: absolute; top: 100%; left: auto; right: 0; background: white; border: 1px solid #ccc; border-radius: 5px; list-style: none; padding: 5px; width: 120px; display: none; box-shadow: 0px 4px 6px rgba(0,0,0,0.1);"><li><button id="subnetButton" class="primery-btn" title="Add Subnet" data-panel="addSubnetModal" style="background: none; border: none; width: 100%; text-align: left; padding: 8px; font-size: 14px; cursor: pointer; color: #007bff; outline: none; box-shadow: none; user-select: none;" onmouseover="this.style.color=\'#0056b3\';" onmouseout="this.style.color=\'#007bff\';" onfocus="this.blur();">' +
                'Add Subnet</button></li><li><button id="supernetButton" class="primery-btn" title="Add Supernet" data-panel="addSupernetModal" style="background: none; border: none; width: 100%; text-align: left; padding: 8px; font-size: 14px; cursor: pointer; color: #007bff; outline: none; box-shadow: none; user-select: none;" onmouseover="this.style.color=\'#0056b3\';" onmouseout="this.style.color=\'#007bff\';" onfocus="this.blur();">Add Supernet</button></li></ul></div>';

            $("#header_panel").html('<div class="title-inner-box"> Hi, ' + $("#userName").val() + '</div>\n' +
                '<div class="corner-content" style="display: flex; align-items: center; justify-content: flex-start; gap: 5px;"><button id="exportScreen" class="defualt-btn margin-r-5" title="Screenshot" style="margin-left: 10px;"><i class="fa fa-camera"></i> Screenshot </button>' +
                headerPanel+
                '</div>');

            homeManager.loadHomeScreenActionMenuEvents();

            $("#container-panel").html('<div id="leftPanel" class="left-panel stickyScrollLeft"></div><div id="homeScreen" class="content-panel"></div><div id="right-panel" class="right-panel stickyScrollRight"></div>');

            appManager.renderHTML(homeManager.HomeScreenPage, $("#homeScreen"), undefined);

            appManager.renderLeftRightPanel(null);

            appManager.toggleContentPanel();

            flux.bindKendoButtonClickEvent({element: 'exportScreen',eventId:'homeScreen',title:'Dashboard.pdf'},widgetRenderManager.onDashboardExportBtnClick);

            homeManager.loadHomeScreenWidgets();
        },

        loadHomeScreenWidgets : function ()
        {
            appManager.executeGETRequest({url: '/ipSummary/',callback:widget.renderIPAvailabilityStatus});

            appManager.executeGETRequest({url: '/pingIpSummary/',callback:widget.renderPingStatus});

            appManager.executeGETRequest({url: '/rogueSubnetIp/',callback:widget.renderRogueIPStatus});

            appManager.executeGETRequest({url: '/eventSummary/',callback:widget.loadIpSummaryGraph,params:homeManager.IpSummary});

            widget.renderSubnetUtilization();

            widget.renderDhcpUtilization();

            widget.renderTop10SubnetUtilization();

            widget.renderTop10CategoryUtilization();

            widget.renderRecentDiscovered();

            appManager.executeGETRequest({url: '/dnsStatusSummary/',callback:widget.renderDNSPieChart,length:130,StartAngle:-45,Padding:10,params:homeManager.DnsStatusSummary});

            widget.renderConflictedIP();

            appManager.executeGETRequest({url: '/ipSummary/',callback:widgetRenderManager.renderPieChart,length:130,StartAngle:-45,Padding:10,params:homeManager.IpAvailability});

            appManager.executeGETRequest({url: '/vendor/',callback:widget.renderVendorwiseIpSummary,params:homeManager.VendorWiseIp});

            loaderUtil.hideModalLoader();
        },

        loadHomeScreenActionMenuEvents : function ()
        {
            $("#dropdownMenuButton").click(function() {
                $("#actionDropDownMenu").toggle();
            });

            $(document).click(function(event) {
                if (!$(event.target).closest('.dropdown').length) {
                    $(".dropdown-menu").hide();
                }
            });
        },

        // ------------------------------------------------------------------------------- Report Export Method -------------------------------------------------------------------------------------------//

        onDashboardActionBtnClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var context = event.sender.options.prefix;

                var eventId = context.dataValue;

                var title = context.title;

                var exportType = context.exportType;

                homeManager.subnetPopup.popupContent.close();

                homeManager.dhcpPopup.popupContent.close();

                homeManager.conflictPopupDiv.popupContent.close();

                homeManager.recentlyDiscoveredPopupDiv.popupContent.close();

                homeManager.dnsStatusPopupDiv.popupContent.close();

                homeManager.top10CategoryUtilizationPopupDiv.popupContent.close();

                homeManager.top10SubnetUtilizationPopupDiv.popupContent.close();

                homeManager.ipAvailabilityPopupDiv.popupContent.close();

                homeManager.vendorPopupDiv.popupContent.close();

                loaderUtil.showModalLoader();

                loaderUtil.showCentralModalLoader(appConstant.ExportMessage);

                if(eventId == homeManager.SubnetUtilization)
                {
                    appManager.executeGETRequest({url:'/exportNormalPdfSubnet/',callback:homeManager.downloadPdf});
                }
                if(eventId == homeManager.DhcpUtilization)
                {
                    appManager.executeGETRequest({url:'/exportDhcpPdfSubnet/',callback:homeManager.downloadPdf});
                }
                if(eventId == homeManager.ConflictIP)
                {
                    appManager.executeGETRequest({url:'/exportPdfSubnetConflictIp/',callback:homeManager.downloadPdf});
                }
                if(eventId == homeManager.RecentDiscovery)
                {
                    appManager.executeGETRequest({url:'/exportPdfRecentlyDiscovered/',callback:homeManager.downloadPdf});
                }
                if(eventId == homeManager.Top10CategoryUtilization)
                {
                    appManager.executeGETRequest({url:'/exportPdfTop10CategoryUtilization/',callback:homeManager.downloadPdf});
                }
                if(eventId == homeManager.Top10SubnetUtilization)
                {
                    appManager.executeGETRequest({url:'/exportPdfTop10SubnetUtilization/',callback:homeManager.downloadPdf});
                }
                if(eventId == homeManager.IpAvailability || eventId == homeManager.VendorWiseIp || eventId == homeManager.DnsStatusSummary)
                {
                    var chart = $("#" + eventId).getKendoChart();

                    homeManager.renderChartData(chart,exportType,title);
                }
                loaderUtil.hideModalLoader();

                loaderUtil.hideCentralModalLoader();
            }
        },

        renderChartData : function (chart,exportType,title)
        {
            if(chart!=null && chart!=undefined)
            {
                if(exportType == "png")
                {
                    chart.exportImage().done(function(data) {
                        kendo.saveAs({
                            dataURI: data,
                            fileName: title+".png"
                        });
                    });
                }
                else if(exportType == "svg")
                {
                    chart.exportSVG().done(function(data) {
                        kendo.saveAs({
                            dataURI: data,
                            fileName: title+".svg"
                        });
                    });
                }
                else if(exportType == "pdf")
                {
                    chart.exportPDF().done(function(data) {
                        kendo.saveAs({
                            dataURI: data,
                            fileName: title+".pdf"
                        });
                    });
                }
            }
            else
            {
                notification.showNotification({notificationTitle:"No Data Available",notificationType:"info"});
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

        // ------------------------------------------------------------------------------Subnet Add Event--------------------------------------------------------------------------------------------//

        onAddSubnetButtonClick : function (event)
        {
            if(event)
            {
                if(appManager.validatePermission() == true)
                {
                    $("#actionDropDownMenu").hide();

                    var subnetHtml = $("#addSubnetDetails");

                    subnetHtml.remove();

                    event.event.preventDefault();

                    var subnetModal = $("#addModal");

                    subnetModal.width('750px');

                    flux.kendoWindowSetOption({kendoWindow:subnetModal});

                    $('#addModal_wnd_title').html('Add New Subnet');

                    subnetModal.data("kendoWindow").content('<div class="content-box-grid-panel margin-t-20" id="importType"><div class="row"></div> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-6"> <input type="radio" name="addSubnetType" id="manuallyAddType" class="k-radio" value="0"  checked/> <label class="k-radio-label label-2" for="addSubnetType">Add Manually</label> </div><div class="col-xs-12 col-md-12 col-lg-6"> <input type="radio" name="addSubnetType" id="csvImportType" class="k-radio" value="1"/> <label class="k-radio-label label-2" for="addSubnetType ">Import From CSV</label> </div></div></div><div class="common-box-grid-panel" id="addSubnetDetails"></div>');

                    flux.bindKendoButtonClickEvent({element: 'importType input'}, homeManager.onAddSubnetTypeClick);

                    homeManager.onAddSubnetTypeClick();
                }
            }
        },

        onAddSubnetTypeClick : function (event)
        {
            if(event !== undefined)
            {
                var modal = $("#addModal");

                var elementValue = parseInt($(event.event.currentTarget).val());

                if (elementValue == homeManager.subnetType.MANUALLY)
                {
                    homeManager.loadManuallyAddSubnet();
                }
                else if (elementValue == homeManager.subnetType.CSV)
                {
                    $("#addSubnetDetails").html('<form id="subnetCSVForm"><div class="fixed-height-body-popup-panel widthFullSpacer"><div class="row"><div class="col-xs-12 col-md-12 col-lg-12 "> <label for="subnetCsv">Subnet CSV</label><div class=""><div class="file-title"></div> <input class="upload-input-file" type="file" id="subnetCsv" name="subnetCsv" accept=".csv" required validationMessage="CSV File is required"/> <i class="fa fa-folder-open-o"></i></div><a class="hyperClick" href="/csv/sampleSubnetDetails.csv">Download Sample CSV</a></div> </div></div><div class="footer-box-grid-box margin-t-10 align-right"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="importSubnetButton">Import</button><button class="k-button k-button-icontext float-l" id="cancelImportCSVButton">Cancel</button></div></form>');

                    flux.bindKendoButtonClickEvent({element : 'importSubnetButton',kendoModal:modal},homeManager.onCSVButtonClick);

                    flux.bindKendoButtonClickEvent({element : 'cancelImportCSVButton',kendoModal:modal},flux.closeKendoModal);
                }
            }
            else
            {
                homeManager.loadManuallyAddSubnet();
            }

            appManager.initCustomScrollbar({container:$(".fixed-height-body-popup-panel")});
        },

        loadManuallyAddSubnet : function ()
        {
            $("#addSubnetDetails").html('<form id="subnetManuallyForm"><div class="fixed-height-body-popup-panel widthFullSpacer"><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"><input type="radio" style="width: 34px;" name="isLocalSubnet" id="localSubnet" class="k-radio" value="true"  checked/><label class="k-radio-label label-2" for="isLocalSubnet">Local Subnet</label></div><div class="col-xs-12 col-md-12 col-lg-6"><input type="radio" name="isLocalSubnet" id="remoteSubnet" class="k-radio" value="false"/><label class="k-radio-label label-2" for="isLocalSubnet ">Remote Subnet</label></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <label for="categoryId">Select Category</label> <select id="categoryId" name="categoryId" class="k-dropdown" required validationMessage="Category Name is required"/></div><div class="col-xs-12 col-md-12 col-lg-6"><button id="categoryButton" class="primery-btn margin-t-15" title="Add Category"> <i class="fa fa-plus"></i> Add Category </button><div id="categoryModal"></div></div></div><div id="subnettype" class="row"><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"><label for="gatewayId">Select Gateway</label><select id="gatewayId" name="gatewayId" class="k-dropdown"/></div><div class="col-xs-12 col-md-12 col-lg-6"><button id="gatewayButton" class="primery-btn margin-t-15" title="Add Gateway"> <i class="fa fa-plus"></i> Add Gateway </button><div id="gatewayModal"></div></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"> <label for="subnetAddress">Subnet/Network Address</label> <input id="subnetAddress" name="subnetAddress" type="text" class="k-textbox" maxlength="40" required validationMessage="Subnet Address is required"/></div><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <label for="subnetMask">Subnet Mask / Prefix </label> <select id="maskInfo" name="maskInfo" class="k-dropdown" maxlength="25" required validationMessage="Subnet Mask is required"/></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"> <label for="subnetName">Subnet Name</label> <input id="subnetName" name="subnetName" type="text" class="k-textbox" maxlength="50" required validationMessage="Subnet Name is required"/></div><div class="col-xs-12 col-md-12 col-lg-6"> <label for="dnsAddress">DNS Address</label> <input id="dnsAddress" name="dnsAddress" type="text" class="k-textbox" maxlength="25"/></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"> <label for="vlanName">VLAN Name</label> <input id="vlanName" name="vlanName" type="text" class="k-textbox" maxlength="50"/></div><div class="col-xs-12 col-md-12 col-lg-6"> <label for="location">Location</label> <input id="location" name="location" type="text" class="k-textbox" maxlength="50"/></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-12"> <label for="description">Description</label> <input id="description" name="description" type="text" class="k-textbox" maxlength="100"/></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-12 select-drop-menu"><input type="checkbox" id="scheduleStatus" name="scheduleStatus" class="k-checkbox" value="true"/> <label for="automaticScanning">Automatic Scanning</label><div id="autoScheduler"></div></div><div class="col-xs-12 col-md-12 col-lg-12"> <input type="checkbox" id="allowIcmp" name="allowIcmp" class="k-checkbox" value="true"/> <label class="k-checkbox-label" for="allowIcmp">To check the IP Availability using ICMP</label></div><div class="col-xs-12 col-md-12 col-lg-12"><input type="checkbox" id="allowDns" name="allowDns" class="k-checkbox" value="true"/> <label class="k-checkbox-label" for="allowDns">To perform DNS forward and reverse lookup</label></div></div></div><div class="footer-box-grid-box margin-t-10 align-right"><div class="bottom-form-panel"> <button class="k-button k-button-icontext k-primary k-grid-cancel float-l" id="testSubnet">Test</button> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="addSubnet"> Save </button> <button class="defualt-btn k-button k-grid-cancel" id="cancelSubnet">Cancel</button></div></div></form>');

            var modal = $("#addModal");

            var categoryList = $("#categoryId");

            homeManager.loadIPV4dropDown();

            var gatewayList = $("#gatewayId");

            flux.getKendoDropDownListURL({dropDownId:gatewayList,url:"/gateway/",dataTextField: "gateway",dataValueField: "id"});

            flux.bindKendoButtonClickEvent({element: 'gatewayButton'}, homeManager.onAddGatewayButtonClick);

            flux.bindElementEvent({event:'change',element:'addModal',selector:'#gatewayId' ,isEditable:true},homeManager.onGatewayChange);

            flux.getKendoDropDownListURL({dropDownId:categoryList,url:"/category/",dataTextField: "categoryName",dataValueField: "id"});

            flux.bindKendoButtonClickEvent({element: 'categoryButton'}, homeManager.onAddCategoryButtonClick);

            flux.bindElementEvent({event:'keyup',element:'addSubnetDetails',selector:'#subnetAddress',eventId:'subnetName'},flux.hostAddressValidation);

            flux.bindElementEvent({event:'change',element:'addModal',selector:'#scheduleStatus' ,isEditable:true},homeManager.renderAutoSchedulerDropDown);

            flux.bindKendoButtonClickEvent({element: 'addSubnet',kendoModal:modal},homeManager.addSubnetEntity);

            flux.bindKendoButtonClickEvent({element: 'localSubnet',kendoModal:modal},homeManager.toggleControl);

            flux.bindKendoButtonClickEvent({element: 'remoteSubnet',kendoModal:modal},homeManager.toggleControl);

            flux.bindKendoButtonClickEvent({element: 'testSubnet',container:'subnetManuallyForm'},homeManager.onTestSubnetButtonClick);

            flux.bindKendoButtonClickEvent({element : 'cancelSubnet',kendoModal:modal},flux.closeKendoModal);

            $('#subnettype').hide();
        },

        addSubnetEntity : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var kendoEvent = event.sender.options.prefix.kendoModal;

                var form = $("#subnetManuallyForm");

                var validator = form.kendoValidator().data("kendoValidator");

                if(validator.validate())
                {
                    var param = formManager.serializeForm(form);

                    loaderUtil.showModalLoader();

                    appManager.executePOSTRequest({url: '/subnet/', container: kendoEvent, callback: homeManager.afterSubnetAddedOrUpdated, params: param});
                }
            }
        },

        renderAutoSchedulerDropDown : function (event)
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
                        dataSource:[{name: "Hour", id: "Hours" }, {name: "Day", id: "Days" },{name: "Month", id: "Month" }],
                        dataTextField: "name",
                        dataValueField: "id"
                    }).data("kendoDropDownList");

                    scopeDuration.kendoDropDownList({
                        dataSource:[{text:'1',value:'1',id: 'Hours'},{text:'2',value:'2',id: 'Hours'},{text:'3',value:'3',id: 'Hours'},
                            {text:'4',value:'4',id: 'Hours'},{text:'5',value:'5',id: 'Hours'}, {text:'6',value:'6',id: 'Hours'},{text:'7',value:'7',id: 'Hours'}, {text:'8',value:'8',id: 'Hours'},
                            {text:'9',value:'9',id: 'Hours'},{text:'10',value:'10',id: 'Hours'},{text:'11',value:'11',id: 'Hours'}, {text:'12',value:'12',id: 'Hours'},
                            {text:'13',value:'13',id: 'Hours'},{text:'14',value:'14',id: 'Hours'},{text:'15',value:'15',id: 'Hours'},{text:'16',value:'16',id: 'Hours'},{text:'17',value:'17',id: 'Hours'},
                            {text:'18',value:'18',id: 'Hours'}, {text:'19',value:'19',id: 'Hours'},{text:'20',value:'20',id: 'Hours'},{text:'21',value:'21',id: 'Hours'},{text:'22',value:'22',id: 'Hours'},
                            {text:'23',value:'23',id: 'Hours'},
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

        onTestSubnetButtonClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var form = $("#"+event.sender.options.prefix.container);

                var param = formManager.serializeForm(form);

                var validator = form.kendoValidator().data("kendoValidator");

                if (validator.validate())
                {
                    loaderUtil.showModalLoader();

                    appManager.executePOSTRequest({ url: '/checkSubnet/', container: form, callback: homeManager.afterSubnetTest, params: param});
                }
            }
        },

        afterSubnetTest : function (callbackContexts)
        {
            if(callbackContexts)
            {
                if(callbackContexts.json.success == true)
                {
                    notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"info"});
                }
                else
                {
                    notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"error"});
                }
                loaderUtil.hideModalLoader();
            }
        },

        // ------------------------------------------------------------------------------Supernet Add Event--------------------------------------------------------------------------------------------//
        onAddSupernetButtonClick : function (event)
        {
            if(event)
            {
                if(appManager.validatePermission() == true)
                {
                    $("#actionDropDownMenu").hide();

                    var supernetHtml = $("#addSupernetDetails");

                    supernetHtml.remove();

                    event.event.preventDefault();

                    var supernetModal = $("#addModal");

                    supernetModal.width('400px');

                    flux.kendoWindowSetOption({kendoWindow:supernetModal});

                    $('#addModal_wnd_title').html('Add Supernet');

                    supernetModal.data("kendoWindow").content('<form id="supernetManuallyForm"><div class="fixed-height-body-popup-panel widthFullSpacer margin-t-20"><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"><label for="networkAddress">Network Address</label><input id="networkAddress" name="networkAddress" type="text" class="k-textbox" maxlength="40" required validationmessage="Network Address is required"></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"><label for="networkMask">Network Mask</label><input id="networkMask" name="networkMask" type="text" class="k-textbox" maxlength="25" required validationmessage="Network Mask is required"></div></div></div><div class="footer-box-grid-box margin-t-10 align-right"><div class="bottom-form-panel"><button class="k-button k-button-icontext k-primary k-grid-update float-r" id="addSupernet">Save</button><button class="defualt-btn k-button k-grid-cancel" id="cancelSupernet">Cancel</button></div></div></form>');

                    flux.bindElementEvent({event:'keyup',element:'addSupernetDetails',selector:'#supernetAddress',eventId:'supernetAddress'},flux.hostAddressValidation);

                    flux.bindKendoButtonClickEvent({element : 'cancelSupernet',kendoModal:supernetModal},flux.closeKendoModal);

                    flux.bindKendoButtonClickEvent({element : 'addSupernet',kendoModal:supernetModal},homeManager.addSupernet);
                }
            }
        },

        addSupernet : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var kendoEvent = event.sender.options.prefix.kendoModal;

                var form = $("#supernetManuallyForm");

                var validator = form.kendoValidator().data("kendoValidator");

                if(validator.validate())
                {
                    var param = formManager.serializeForm(form);

                    loaderUtil.showModalLoader();

                    appManager.executePOSTRequest({url: '/addSupernet/', container: kendoEvent, callback: homeManager.afterSupernetAddedOrUpdated, params: param});
                }
            }
        },

        afterSupernetAddedOrUpdated : function (context)
        {
            if(context)
            {
                var modal = context.container;

                if(context.json.success == true)
                {
                    modal.data('kendoWindow').close();

                    navigationManager.doNavigation();

                    notification.showNotification({notificationTitle: context.json.message, notificationType:"success"});

                }
                else
                {
                    notification.showNotification({notificationTitle: context.json.message, notificationType:"error"});
                }

                loaderUtil.hideCentralModalLoader();

                loaderUtil.hideModalLoader();
            }
        },

        // -------------------------------------------------------------------------------Category Add-------------------------------------------------------------------------------------------//

        onAddCategoryButtonClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var categoryModal = $("#innerModal");

                categoryModal.width('250px');

                flux.kendoWindowSetOption({height:'155px',kendoWindow:categoryModal});

                $('#innerModal_wnd_title').html('Add New Category');

                categoryModal.data("kendoWindow").content('<form id="addCategoryForm"><div class="content-box-grid-panel margin-t-20" id="importType"><div class="row"><div class="col-xs-12 col-md-12 col-lg-12"> <label for="categoryName">Add Category</label> <input id="categoryName" name="categoryName" type="text" class="k-textbox" required validationMessage="Category Name required" maxlength="25"/></div></div></div><div class="footer-box-grid-boxs margin-t-10 align-right"><div class="bottom-form-panel"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="addCategory">Add Category</button> <button class="k-button k-button-icontext k-grid-cancel float-l" id="cancelCategory">Cancel</button></div></div></form>');

                flux.bindKendoButtonClickEvent({element:'addCategory',kendoModal:categoryModal},homeManager.addCategoryEntity);

                flux.bindKendoButtonClickEvent({element : 'cancelCategory',kendoModal:categoryModal},flux.closeKendoModal);
            }
        },

        addCategoryEntity :function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var kendoEvent = event.sender.options.prefix.kendoModal;

                var form = $("#addCategoryForm");

                var param =formManager.serializeForm(form);

                var validator = form.kendoValidator().data("kendoValidator");

                if(validator.validate())
                {
                    loaderUtil.showModalLoader();

                    appManager.executePOSTRequest({ url: '/category/', container: kendoEvent, isInit:true, callback: homeManager.afterCategoryAdded, params: param});
                }
            }
        },

        afterCategoryAdded : function (context)
        {
            if(context)
            {
                var modal = context.container;

                modal.data("kendoWindow").close();

                leftPanel.initTreeView();

                if(context.isInit)
                {
                    $("#categoryId").data("kendoDropDownList").dataSource.read();
                }

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

        // -------------------------------------------------------------------------------Gateway Add-------------------------------------------------------------------------------------------//

        onAddGatewayButtonClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var gatewayModal = $("#innerModal");

                gatewayModal.width('500px');

                flux.kendoWindowSetOption({height:'155px',kendoWindow:gatewayModal});

                $('#innerModal_wnd_title').html('Add Gateway');

                gatewayModal.data("kendoWindow").content('<form id="addGatewayForm"><div class="content-box-grid-panel margin-t-20" id="importType"><div class="row"><div class="col-xs-6 col-md-6 col-lg-6"><label for="gateway">Gateway</label><input id="gateway" name="gateway" type="text" class="k-textbox" required="" validationmessage="Gateway required" maxlength="45"></div></div><div class="row"><div class="col-xs-6 col-md-6 col-lg-6 select-drop-menu"><label for="version">Version</label><select id="version" name="version" class="k-dropdown" required="">Version</select></div><div class="col-xs-6 col-md-6 col-lg-6"><label for="community">Community</label><input id="community" name="community" type="password" class="k-textbox" ></div></div><div class="row"><div class="col-xs-6 col-md-6 col-lg-6 select-drop-menu"><label for="securityLevel">Security Level</label><select id="securityLevel" name="securityLevel" class="k-dropdown" required="">securityLevel</select></div><div class="col-xs-6 col-md-6 col-lg-6 select-drop-menu"><label for="authenticationProtocol">Authentication Protocol</label><select id="authenticationProtocol" name="authenticationProtocol" class="k-dropdown">Authentication Protocol</select></div></div><div class="row"><div class="col-xs-6 col-md-6 col-lg-6 select-drop-menu"><label for="privacyProtocol">Privacy Protocol</label><select id="privacyProtocol" name="privacyProtocol" class="k-dropdown">Privacy Protocol</select></div><div class="col-xs-6 col-md-6 col-lg-6"><label for="securityUserName">Security User Name</label><input id="securityUserName" name="securityUserName" type="text" class="k-textbox"></div></div><div class="row"><div class="col-xs-6 col-md-6 col-lg-6"><label for="authenticationPassword">Authentication Password</label><input id="authenticationPassword" name="authenticationPassword" type="password" class="k-textbox"></div><div class="col-xs-6 col-md-6 col-lg-6"><label for="privatePassword">Private Password</label><input id="privatePassword" name="privatePassword" type="password" class="k-textbox"></div></div></div><div class="footer-box-grid-boxs margin-t-10 align-right"><div class="bottom-form-panel"><button class="k-button k-button-icontext k-primary k-grid-update float-r" id="addGateway">Add Gateway</button><button class="k-button k-button-icontext k-grid-cancel float-l" id="deleteGateway">Cancel</button></div></div></form>');

                var version = $("#version");

                var securityLevel = $("#securityLevel");

                var authenticationProtocol = $("#authenticationProtocol");

                var privacyProtocol = $("#privacyProtocol");

                var gatewayButton =  $('#gatewayButton');

                var versions = [{text: "V1", value: "v1" },{text: "V2c", value: "v2c" },{text: "V3", value: "v3" }];

                var securityLevels = [{text: "No Authentication No Privacy", value: "noAuthNoPriv" },{text: "Authentication No Privacy", value: "authNoPriv" },{text: "Authentication Privacy", value: "authPriv" }];

                var privacyProtocols = [{text: "DES", value: "DES" },{text: "3DES", value: "3DES" },{text: "AES", value: "AES" },{text: "AES128", value: "AES128" },{text: "AES192", value: "AES192" },{text: "AES256", value: "AES256" }];

                var authenticationProtocols = [{text: "MD5", value: "MD5" },{text: "SHA", value: "SHA" },{text: "SHA224", value: "SHA224" }, {text: "SHA256", value: "SHA256" }, {text: "SHA384", value: "SHA384" }, {text: "SHA512", value: "SHA512" }];

                flux.getKendoDropDownList({dropDownId:version, dataTextField: "text", dataValueField: "value", data:versions});

                flux.getKendoDropDownList({dropDownId:securityLevel, dataTextField: "text", dataValueField: "value", data:securityLevels});

                flux.getKendoDropDownList({dropDownId:authenticationProtocol, dataTextField: "text", dataValueField: "value", data:authenticationProtocols});

                flux.getKendoDropDownList({dropDownId:privacyProtocol, dataTextField: "text", dataValueField: "value", data:privacyProtocols});

                if(gatewayButton.data('isUpdate') === true)
                {
                    var gatewayId = gatewayButton.data('gatewayId');

                    $("#deleteGateway").html('Delete');

                    $("#addGateway").html('Update');

                    $('#innerModal_wnd_title').html('Update Gateway');

                    flux.bindKendoButtonClickEvent({element: 'deleteGateway',kendoModal:gatewayModal},homeManager.deleteGateway);

                    flux.bindKendoButtonClickEvent({element: 'addGateway', kendoModal:gatewayModal}, homeManager.editGatewayEntity);

                    appManager.executeGETRequest({url: '/gateway/' + gatewayId, container:gatewayModal, callback: formManager.renderForm, params:gatewayId});
                }
                else
                {
                    $('#community').val('public');

                    flux.bindKendoButtonClickEvent({element:'addGateway',kendoModal:gatewayModal},homeManager.addGatewayEntity);

                    flux.bindKendoButtonClickEvent({element : 'deleteGateway',kendoModal:gatewayModal},flux.closeKendoModal);
                }
            }
        },

        addGatewayEntity :function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var kendoEvent = event.sender.options.prefix.kendoModal;

                var form = $("#addGatewayForm");

                var param = formManager.serializeForm(form);

                var validator = form.kendoValidator().data("kendoValidator");

                if(validator.validate())
                {
                    loaderUtil.showModalLoader();

                    appManager.executePOSTRequest({ url: '/gateway/', container: kendoEvent, isInit:true, callback: homeManager.afterGatewayAdded, params: param});
                }
            }
        },

        afterGatewayAdded : function (context)
        {
            if(context)
            {
                if(context.json.success === true)
                {
                    var modal = context.container;

                    var gatewayList = $("#gatewayId");

                    modal.data('kendoWindow').close();

                    flux.getKendoDropDownListURLWithCallback({dropDownId:gatewayList, url:"/gateway/", dataTextField: "gateway", dataValueField: "id", callback: homeManager.afterGatewayDropDownInit});

                    notification.showNotification({notificationTitle: context.json.message, notificationType:"success"});
                }
                else
                {
                    notification.showNotification({notificationTitle: context.json.message, notificationType:"error"});
                }

                loaderUtil.hideModalLoader();
            }
        },

        afterGatewayDeleted : function (context)
        {
            if(context)
            {
                if(context.json.success === true)
                {
                    var gatewayList = $("#gatewayId");

                    flux.getKendoDropDownListURLWithCallback({dropDownId:gatewayList, url:"/gateway/", dataTextField: "gateway", dataValueField: "id", callback: homeManager.afterGatewayDropDownInit});

                    notification.showNotification({notificationTitle: context.json.message, notificationType:"success"});
                }
                else
                {
                    notification.showNotification({notificationTitle: context.json.message, notificationType:"error"});
                }

                loaderUtil.hideModalLoader();

                loaderUtil.hideCentralModalLoader();
            }
        },

        onGatewayChange : function ()
        {
            var gatewayButton =  $('#gatewayButton');

            var selectedGateway = $("#gatewayId option:selected");

            var gatewayId = selectedGateway.val();

            gatewayButton.data('gatewayId', gatewayId);

            if(selectedGateway.text() !== 'None')
            {
                gatewayButton.html('Update Gateway');

                gatewayButton.data('isUpdate', true);
            }
            else
            {
                gatewayButton.html(' <i class="fa fa-plus"></i> Add Gateway ');

                gatewayButton.data('isUpdate', false);
            }
        },

        afterGatewayDropDownInit: function (context)
        {
            if (context.json.data != null)
            {
                var result = context.json.data;

                context.container.success(result);
            }
            else
            {
                context.container.success("");
            }

            homeManager.onGatewayChange();
        },

        editGatewayEntity : function (event)
        {
            if (event)
            {
                event.preventDefault();

                var context = event.sender.options.prefix;

                var modal = context.kendoModal;

                var form = $("#addGatewayForm");

                var selectedGateway = $("#gatewayId option:selected");

                var gatewayId = selectedGateway.val();

                var param = formManager.serializeForm(form);

                var validator = form.kendoValidator().data("kendoValidator");

                if (validator.validate())
                {
                    loaderUtil.showModalLoader();

                    appManager.executePUTRequest({url: '/gateway/' + gatewayId, container:modal, callback: homeManager.afterGatewayAdded, params: param});
                }
            }
        },

        deleteGateway : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var context = event.sender.options.prefix;

                var modal = context.kendoModal;

                modal.data('kendoWindow').close();

                var gatewayId = $("#gatewayId option:selected").val();

                appManager.executeDELETERequest({url: '/gateway/'+gatewayId, callback: homeManager.afterGatewayDeleted});
            }
        },


        // ----------------------------------------------------------------------------------Subnet Edit----------------------------------------------------------------------------------------//

        afterEditSubnetFormRender : function (context)
        {
            //todo arpit change here
            if(context)
            {
                var autoScheduler = $('#scheduleStatus');

                if (context.json.data.localSubnet == true)
                {
                    homeManager.hideRemoteSubnetField();
                }
                else
                {
                    homeManager.displayRemoteSubnetField();
                }

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
            }
        },

        onEditSubnetEntity : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var context = event.sender.options.prefix;

                var id = context.editId;

                var modal = context.kendoModal;

                var form = $("#updateSubnetForm");

                var param =formManager.serializeForm(form);

                var validator = form.kendoValidator().data("kendoValidator");

                if(validator.validate())
                {
                    loaderUtil.showModalLoader();

                    appManager.executePUTRequest({url: '/subnet/'+id, container: modal,isEdit:true,callback: homeManager.afterSubnetAddedOrUpdated, params: param});
                }
            }
        },


        afterSubnetAddedOrUpdated : function (context)
        {
            if(context)
            {
                var modal = context.container;

                if(context.json.success == true)
                {
                    modal.data('kendoWindow').close();

                    navigationManager.doNavigation();

                    notification.showNotification({notificationTitle: context.json.message, notificationType:"success"});
                }
                else
                {
                    notification.showNotification({notificationTitle: context.json.message, notificationType:"error"});
                }
                loaderUtil.hideCentralModalLoader();

                loaderUtil.hideModalLoader();
            }
        },

        renderPageAfterSubnetAddedOrUpdated : function (params)
        {
            if(navigationManager.getUrlParameter("navigation") == navigationManager.menu.Home)
            {
                leftPanel.initTreeView();

                rightPanel.renderEventDetails();

                homeManager.loadHomeScreenWidgets();
            }
            else if(navigationManager.getUrlParameter("navigation") == navigationManager.menu.Subnet)
            {
                subnetSummary.renderSubnetSummaryFromURL(navigationManager.getUrlParameter("subnetId"),params.subnetName,
                    navigationManager.getUrlParameter("breadCrumbNavigation"),navigationManager.getUrlParameter("searchField"));
            }
            else if(navigationManager.getUrlParameter("navigation") == navigationManager.menu.IpAddressSummary)
            {
                ipAddressSummary.renderIPAddressSummaryFromURL(navigationManager.getUrlParameter("subnetId"),navigationManager.getUrlParameter("subnetAddress"),params.subnetName,
                    navigationManager.getUrlParameter("breadCrumbNavigation"),navigationManager.getUrlParameter("scopeId"),navigationManager.getUrlParameter("searchField"));
            }
            else
            {
                navigationManager.doNavigation();
            }
        },


        // ---------------------------------------------------------------------------------Import Subnet CSV-----------------------------------------------------------------------------------------//

        onCSVButtonClick :function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var kendoEvent = event.sender.options.prefix.kendoModal;

                var form = $("#subnetCSVForm");

                var validator = form.kendoValidator().data("kendoValidator");

                if(validator.validate())
                {
                    var formData = new FormData();

                    formData.append('subnetCsv', $("#subnetCsv")[0].files[0]);

                    loaderUtil.showModalLoader();

                    loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

                    kendoEvent.data('kendoWindow').close();

                    appManager.executeFileRequest({url: '/subnetByCSV/',type: 'POST',callback: homeManager.afterCSVAdded,params: formData});
                }
            }
        },

        // -----------------------------------------------------------------------------IP Address Import Discovery Running---------------------------------------------------------------------------------------------//

        initImportSubnetTracking:function ()
        {
            appManager.executeGETRequest({url:'/importSubnetStatus/', callback:homeManager.afterImportStatusChecked});
        },

        afterImportStatusChecked:function (callbackContext)
        {
            var scanMessage = "Subnet IP Address Imported Successfully";

            if(callbackContext.json.success === false)
            {
                if(callbackContext.intervalFunctionCall == undefined)
                {
                    //clearInterval(homeManager.importIPStatus);

                    homeManager.setRunningImportBlinkHTML();

                    homeManager.importIPStatus = setInterval(function()
                    {
                        appManager.executeGETRequest({url:'/importSubnetStatus/',intervalFunctionCall:true,callback:homeManager.afterImportStatusChecked});

                    }, 5000);
                }
            }
            else
            {
                $('#discovery-import-title').remove();

                clearInterval(homeManager.importIPStatus);

                if(callbackContext.intervalFunctionCall == true)
                {
                    navigationManager.renderPageAfterDiscovery(scanMessage);
                }
            }
            loaderUtil.hideModalLoader();

            //loaderUtil.hideCentralModalLoader();
        },

        setRunningImportBlinkHTML:function ()
        {
            $('#discovery-import-title').remove();

            $('body').append('<div class="discoveryImportPositionSection" id="discovery-import-title"><div class="discoveryMiddle"> <span id ="discovery-progress" class="fa fa-spinner fa-spin discovery-progress txt-color-white" data-original-title="Discovery Progress" rel="tooltip"></span><div class="discovery-title"> <span class="label bg-color-blue discoveryTextSubnet" rel="tooltip" title="Subnet IP Address Import is Running">Subnet IP Address Import is Running</span></div></div></div>');
        },

        afterCSVAdded : function (callbackContext)
        {
            if(callbackContext)
            {
                if(callbackContext.json.success == true)
                {
                    clearInterval(homeManager.importIPStatus);

                    homeManager.setRunningImportBlinkHTML();

                    homeManager.importIPStatus = setInterval(function()
                    {
                        appManager.executeGETRequest({url:'/importSubnetStatus/',intervalFunctionCall:true,callback:homeManager.afterImportStatusChecked});

                    }, 5000);

                    notification.showNotification({notificationTitle: callbackContext.json.message, notificationType:"info"});
                }
                else
                {
                    notification.showNotification({notificationTitle: callbackContext.json.message, notificationType:"error"});
                }
            }
            loaderUtil.hideModalLoader();

            loaderUtil.hideCentralModalLoader();
        },

        openSubnetActionPopup: function (event)
        {
            if(event)
            {
                event.preventDefault();

                var target = event.currentTarget;

                homeManager.subnetPopup.popupContent.setOptions({
                    anchor: $(target),
                    position: "top center"
                });

                homeManager.subnetPopup.popupContent.open();

                homeManager.subnetPopup.popupContent.position();
            }
        },

        openDhcpActionPopup : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var target = event.currentTarget;

                homeManager.dhcpPopup.popupContent.setOptions({
                    anchor: $(target),
                    position: "top center"
                });

                homeManager.dhcpPopup.popupContent.open();

                homeManager.dhcpPopup.popupContent.position();
            }
        },

        openConflictActionPopup : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var target = event.currentTarget;

                homeManager.conflictPopupDiv.popupContent.setOptions({
                    anchor: $(target),
                    position: "top center"
                });

                homeManager.conflictPopupDiv.popupContent.open();

                homeManager.conflictPopupDiv.popupContent.position();
            }
        },

        openRecentlyDiscoveredActionPopup : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var target = event.currentTarget;

                homeManager.recentlyDiscoveredPopupDiv.popupContent.setOptions({
                    anchor: $(target),
                    position: "top center"
                });

                homeManager.recentlyDiscoveredPopupDiv.popupContent.open();

                homeManager.recentlyDiscoveredPopupDiv.popupContent.position();
            }
        },

        openDnsStatusActionPopup : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var target = event.currentTarget;

                homeManager.dnsStatusPopupDiv.popupContent.setOptions({
                    anchor: $(target),
                    position: "top center"
                });

                homeManager.dnsStatusPopupDiv.popupContent.open();

                homeManager.dnsStatusPopupDiv.popupContent.position();
            }
        },


        openTop10SubnetUtilizationActionPopup : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var target = event.currentTarget;

                homeManager.top10SubnetUtilizationPopupDiv.popupContent.setOptions({
                    anchor: $(target),
                    position: "top center"
                });

                homeManager.top10SubnetUtilizationPopupDiv.popupContent.open();

                homeManager.top10SubnetUtilizationPopupDiv.popupContent.position();
            }
        },


        openTop10CategoryActionPopup : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var target = event.currentTarget;

                homeManager.top10CategoryUtilizationPopupDiv.popupContent.setOptions({
                    anchor: $(target),
                    position: "top center"
                });

                homeManager.top10CategoryUtilizationPopupDiv.popupContent.open();

                homeManager.top10CategoryUtilizationPopupDiv.popupContent.position();
            }
        },

        openIPAvailabilityChartActionPopup : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var target = event.currentTarget;

                homeManager.ipAvailabilityPopupDiv.popupContent.setOptions({
                    anchor: $(target),
                    position: "top center"
                });

                homeManager.ipAvailabilityPopupDiv.popupContent.open();

                homeManager.ipAvailabilityPopupDiv.popupContent.position();
            }
        },

        openVendorChartActionPopup : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var target = event.currentTarget;

                homeManager.vendorPopupDiv.popupContent.setOptions({
                    anchor: $(target),
                    position: "top center"
                });

                homeManager.vendorPopupDiv.popupContent.open();

                homeManager.vendorPopupDiv.popupContent.position();
            }
        },

        toggleControl: function (event)
        {
            var target = event.event.currentTarget.value;

            if (typeof target != 'undefined' && target == 'false')
            {
                homeManager.displayRemoteSubnetField();
            }
            else
            {
                homeManager.hideRemoteSubnetField();
            }

            var subnetAddress = $('#subnetAddress');

            if (subnetAddress.length > 0)
            {
                subnetAddress.val('');
            }

            var subnetName = $('#subnetName');

            if (subnetName.length > 0)
            {
                subnetName.val('');
            }
        },

        displayRemoteSubnetField : function()
        {
            $('#subnettype').show();

            $('#gatewayButton').data('gatewayId', $("#gatewayId option:selected").val());

            homeManager.onGatewayChange();

            flux.bindElementEvent({event:'keyup',element:'gatewayIp'},flux.hostAddressValidation);

            homeManager.loadIPV4dropDown();
        },

        hideRemoteSubnetField : function()
        {
            $('#subnettype').hide();

            $('#updateSubnetForm').append('<input type="text" hidden value="true" name="isLocalSubnet" />');

            $('#snmpCommunity').prop('required',false);

            $('#gatewayIp').prop('required',false);
        },

        loadIPV4dropDown : function ()
        {
            var dropDownId = $("#maskInfo");

            var data = [{text: "255.255.255.0/24", value: "255.255.255.0/24" }, {text: "255.255.254.128/25", value: "255.255.254.128/25" }, {text: "255.255.255.192/26", value: "255.255.255.192/26"}, {text: "255.255.254.226/27", value: "255.255.254.226/27" }, {text: "255.255.254.240/28", value: "255.255.254.240/28" }, {text: "255.255.254.248/29", value: "255.255.254.248/29" }, {text: "255.255.254.252/30", value: "255.255.254.252/30" }, {text: "255.255.254.254/31", value: "255.255.254.254/31" }, {text: "255.255.0.0/16", value: "255.255.0.0/16" }, {text: "255.255.128.0/17", value: "255.255.128.0/17" }, {text: "255.255.192.0/18", value: "255.255.192.0/18" }, {text: "255.255.224.0/19", value: "255.255.224.0/19" }, {text: "255.255.240.0/20", value: "255.255.240.0/20" }, {text: "255.255.248.0/21", value: "255.255.248.0/21" }, {text: "255.255.252.0/22", value: "255.255.252.0/22" }, {text: "255.255.254.0/23", value: "255.255.254.0/23" }];

            flux.getKendoDropDownList({dropDownId :dropDownId ,dataTextField: "text", dataValueField: "value",data: data});
        },

        loadIPV6dropDown : function ()
        {
            var dropDownId = $("#maskInfo");

            var data = [];

            for (var i = 128; i >= 1; i--)
            {
                data.push({ text: i, value: "/" + i });
            }

            flux.getKendoDropDownList({dropDownId :dropDownId ,dataTextField: "text", dataValueField: "value",data: data});
        }
    };