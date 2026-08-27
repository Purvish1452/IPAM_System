var report=
    {
        ReportPage:'reportPage', RightPanel:'reportRightPanel', LeftPanel:'reportLeftPanel', ReportGrid:'reportGrid',

        ScheduleType :{Daily:'Daily',Weekly:'Weekly',Monthly:'Monthly'},

        // -------------------------------------------------------------Init report page with left & right panel-------------------------------------------------------------------------------------------------------------//

        init:function ()
        {
            loaderUtil.showModalLoader();

            var root = $("#header_panel");

            root.empty();

            root.html('<div class="title-inner-box"> Reports </div>');

            $("#container-panel").html('<div id="reportLeftPanel" class="left-panel stickyScrollLeft"></div><div id="reportPage" class="content-panel"></div><div id="reportRightPanel" class="right-panel stickyScrollRight"></div>');

            appManager.renderHTML(report.ReportPage, $("#reportPage"),undefined);

            appManager.renderHTML(report.LeftPanel, $("#reportLeftPanel"), undefined);

            appManager.renderHTML(report.RightPanel, $("#reportRightPanel"), undefined);

            report.renderReportTree();

            report.renderRightPanelContext();

            appManager.togglePanel();

            flux.bindKendoButtonClickEvent({element: 'reportLeftArrow'},leftPanel.onLeftArrowClick);

            flux.bindKendoButtonClickEvent({element: 'reportRightArrow'},rightPanel.onRightArrowClick);

            flux.bindKendoButtonClickEvent({element: 'addReportScheduler'}, report.onAddReportButtonClick);

            flux.bindKendoButtonClickEvent({element:'exportReport',exportType:1},report.onExportReportClick);

            flux.bindKendoButtonClickEvent({element:'exportCsvReport',exportType:2},report.onExportReportClick);
        },

        // -------------------------------------------------------------------Call report treeview-------------------------------------------------------------------------------------------------------//

        renderReportTree : function ()
        {
            var id = $("#reportTreeView");

            var homogeneous = new kendo.data.HierarchicalDataSource({
                transport:
                    {
                        read: function (options)
                        {
                            appManager.executeGETRequest({url: '/subnetByReport/',eventId:'reportTreeView',container:options,callback:report.afterReportTreeInit});
                        }
                    },
                schema: {
                    model: {
                        id: "id",
                        children: "subnets"
                    }
                }
            });

            id.kendoTreeView({

                loadOnDemand : false,

                    template: '#if(!item.subnets){if(item.networkInterface == "ALL"){#<input onclick="report.onCheckBoxClick(this)" name="reportFilter" data-uid="#:item.id#" data-link="subnetIpBySuAllbnet" type="checkbox" ' +
                    'class="All"> <label class="k-checkbox-label label-2">&nbsp;</label><span data-uid="#:item.id#" data-link="subnetIpBySubnet" data-name="#:item.subnetName#" data-property="All IP">#:item.subnetName#</span>#}' +
                    'else if(item.networkInterface == "USED"){#<input type="checkbox" onclick="report.onCheckBoxClick(this)" name="reportFilter"  data-uid="#:item.id#" data-link="usedSubnetIpBySubnet" class="Used">' +
                    '<label class="k-checkbox-label label-2">&nbsp;</label><span data-uid="#:item.id#" data-link="usedSubnetIpBySubnet" data-property="Used IP" data-name="#:item.subnetName#">#:item.subnetName#</span>#}' +
                    'else if(item.networkInterface == "AVAILABLE"){#<input type="checkbox" onclick="report.onCheckBoxClick(this)" name="reportFilter"  data-uid="#:item.id#" data-link="availableSubnetIpBySubnet" class="Available">' +
                    '<label class="k-checkbox-label label-2">&nbsp;</label><span data-uid="#:item.id#" data-link="availableSubnetIpBySubnet" data-property="Available IP" data-name="#:item.subnetName#">#:item.subnetName#</span>#}' +
                    'else if(item.networkInterface == "RESERVED"){#<input type="checkbox" onclick="report.onCheckBoxClick(this)" name="reportFilter"  data-uid="#:item.id#" data-link="reservedSubnetIpBySubnet" class="Reserved">' +
                    '<label class="k-checkbox-label label-2">&nbsp;</label><span data-uid="#:item.id#" data-link="reservedSubnetIpBySubnet" ' +
                    'data-property="Reserved IP" data-name="#:item.subnetName#">#:item.subnetName#</span>#}' +
                    ' else if(item.networkInterface == "TRANSIENT"){#<input type="checkbox" onclick="report.onCheckBoxClick(this)" name="reportFilter"  data-uid="#:item.id#" data-link="transientSubnetIpBySubnet" class="Transient">' +
                    '<label class="k-checkbox-label label-2">&nbsp;</label><span data-uid="#:item.id#" data-link="transientSubnetIpBySubnet" ' +
                    'data-property="Transient IP" data-name="#:item.subnetName#">#:item.subnetName#</span>#}' +
                    'else if(item.networkInterface == "ROGUE"){#<input type="checkbox" onclick="report.onCheckBoxClick(this)" name="reportFilter"  data-uid="#:item.id#" data-link="rogueSubnetIpBySubnet" class="Rogue">' +
                    '<label class="k-checkbox-label label-2">&nbsp;</label><span data-uid="#:item.id#" data-link="rogueSubnetIpBySubnet" ' +
                    'data-property="Rogue IP" data-name="#:item.subnetName#">#:item.subnetName#</span>#}' + 'else if(item.networkInterface == "TRUSTED"){#<input type="checkbox" onclick="report.onCheckBoxClick(this)" name="reportFilter"  data-uid="#:item.id#" data-link="trustedSubnetIpBySubnet" class="Trusted">' +
                    '<label class="k-checkbox-label label-2">&nbsp;</label><span data-uid="#:item.id#" data-link="trustedSubnetIpBySubnet" ' +
                    'data-property="Trusted IP" data-name="#:item.subnetName#">#:item.subnetName#</span>#}' + 'else if(item.networkInterface == "VENDOR SUMMARY")' +
                    '{#<input type="checkbox" onclick="report.onCheckBoxClick(this)" name="reportFilter"  data-uid="#:item.id#" data-link="vendorSubnetIpBySubnet" class="Vendor">' +
                    '<label class="k-checkbox-label label-2">&nbsp;</label><span data-uid="#:item.id#" data-link="vendorSubnetIpBySubnet" ' +
                    'data-property="Vendor Summary" data-name="#:item.subnetName#">#:item.subnetName#</span>#}}' +
                    'else if(item.subnets != null && item.subnets){# <span class="page">#:item.subnetAddress#</span>#}#',

                dataSource: homogeneous,

                dataTextField: "subnetAddress"
            });

            //flux.bindElementEvent({event:'click',element:'reportLeftPanel',selector:'#checkboxFilter'},report.onCheckBoxClick);

            flux.bindElementEvent({element:'reportLeftPanel',selector:'#reportSearch',id:'reportTreeView', event:'keyup'}, _.debounce(function(event){

                flux.searchTreeView(event);

            }, 500));
        },

        // --------------------------------------------------------------------------Treeview checkbox event & Load report grid with subnet id, subnet class and selected timeline------------------------------------------------------------------------------------------------//

        onCheckBoxClick : function (event)
        {
            if(event)
            {
                var reportTreeId = $('#reportTreeView');

                var subnetId;

                var status;

                var subnetStatus;

                //var className = $(event.currentTarget).context.className;

                var className = event.className;

                if(className != undefined)
                {
                    reportTreeId.find(".k-group li.k-item").each(function(){
                        switch (className) {
                            case 'All':
                                reportTreeId.find(".Used").prop('checked', false);
                                reportTreeId.find(".Available").prop('checked', false);
                                reportTreeId.find(".Reserved").prop('checked', false);
                                reportTreeId.find(".Transient").prop('checked', false);
                                reportTreeId.find(".Rogue").prop('checked', false);
                                reportTreeId.find(".Trusted").prop('checked', false);
                                reportTreeId.find(".Vendor").prop('checked', false);
                                break;
                            case 'Used':
                                reportTreeId.find(".All").prop('checked', false);
                                reportTreeId.find(".Available").prop('checked', false);
                                reportTreeId.find(".Reserved").prop('checked', false);
                                reportTreeId.find(".Transient").prop('checked', false);
                                reportTreeId.find(".Rogue").prop('checked', false);
                                reportTreeId.find(".Trusted").prop('checked', false);
                                reportTreeId.find(".Vendor").prop('checked', false);
                                break;
                            case 'Available':
                                reportTreeId.find(".All").prop('checked', false);
                                reportTreeId.find(".Used").prop('checked', false);
                                reportTreeId.find(".Reserved").prop('checked', false);
                                reportTreeId.find(".Transient").prop('checked', false);
                                reportTreeId.find(".Rogue").prop('checked', false);
                                reportTreeId.find(".Trusted").prop('checked', false);
                                reportTreeId.find(".Vendor").prop('checked', false);
                                break;
                            case 'Reserved':
                                reportTreeId.find(".All").prop('checked', false);
                                reportTreeId.find(".Used").prop('checked', false);
                                reportTreeId.find(".Available").prop('checked', false);
                                reportTreeId.find(".Transient").prop('checked', false);
                                reportTreeId.find(".Rogue").prop('checked', false);
                                reportTreeId.find(".Vendor").prop('checked', false);
                                break;
                            case 'Transient':
                                reportTreeId.find(".All").prop('checked', false);
                                reportTreeId.find(".Used").prop('checked', false);
                                reportTreeId.find(".Available").prop('checked', false);
                                reportTreeId.find(".Reserved").prop('checked', false);
                                reportTreeId.find(".Rogue").prop('checked', false);
                                reportTreeId.find(".Trusted").prop('checked', false);
                                reportTreeId.find(".Vendor").prop('checked', false);
                                break;
                            case 'Rogue':
                                reportTreeId.find(".All").prop('checked', false);
                                reportTreeId.find(".Used").prop('checked', false);
                                reportTreeId.find(".Available").prop('checked', false);
                                reportTreeId.find(".Reserved").prop('checked', false);
                                reportTreeId.find(".Transient").prop('checked', false);
                                reportTreeId.find(".Trusted").prop('checked', false);
                                reportTreeId.find(".Vendor").prop('checked', false);
                                break;
                            case 'Trusted':
                                reportTreeId.find(".All").prop('checked', false);
                                reportTreeId.find(".Used").prop('checked', false);
                                reportTreeId.find(".Available").prop('checked', false);
                                reportTreeId.find(".Reserved").prop('checked', false);
                                reportTreeId.find(".Transient").prop('checked', false);
                                reportTreeId.find(".Rogue").prop('checked', false);
                                reportTreeId.find(".Vendor").prop('checked', false);
                                break;
                            case 'Vendor':
                                reportTreeId.find(".All").prop('checked', false);
                                reportTreeId.find(".Used").prop('checked', false);
                                reportTreeId.find(".Available").prop('checked', false);
                                reportTreeId.find(".Reserved").prop('checked', false);
                                reportTreeId.find(".Transient").prop('checked', false);
                                reportTreeId.find(".Rogue").prop('checked', false);
                                reportTreeId.find(".Trusted").prop('checked', false);
                                break;
                            default:
                                reportTreeId.find(".Used").prop('checked', false);
                                reportTreeId.find(".Available").prop('checked', false);
                                reportTreeId.find(".Reserved").prop('checked', false);
                                reportTreeId.find(".Transient").prop('checked', false);
                                reportTreeId.find(".Rogue").prop('checked', false);
                                reportTreeId.find(".Trusted").prop('checked', false);
                                reportTreeId.find(".Vendor").prop('checked', false);
                        }
                    });
                }

                var arr = [];
                $.each($("input[name='reportFilter']:checked"), function(){
                    arr.push($(this).data('uid'));
                });

                if(event != undefined)
                {
                    subnetId = arr.join(",");

                    subnetStatus = className;
                }

                if(className == "Vendor")
                {
                    status = subnetStatus + " Summary";
                }
                else
                {
                    status = subnetStatus+" IP";
                }

                var reportDropDown = $("#reportTimeline");

                var data = [{text: "All", value: "10" },{text: "Today", value: "1" },{text: "Previous Day", value: "2" },{text: "This Week", value: "3" },{text: "This Month", value: "4" },{text: "This Quarter", value: "5" },{text: "Previous Quarter", value: "6" },
                    {text: "Last Six Month", value: "7" },{text: "This Year", value: "8" },{text: "Previous Year", value: "9" },{text: "Previous Week", value: "11" },{text: "Previous Month", value: "12" }];

                flux.getKendoDropDownList({dropDownId:reportDropDown,dataTextField: "text",dataValueField: "value",data:data});

                var firstuid = reportTreeId.find(".k-item .k-group span:first>span").data('uid');

                var param = {};

                reportDropDown.data('kendoDropDownList').destroy();

                reportDropDown.kendoDropDownList
                ({
                    change:function (e)
                    {
                        e.preventDefault();

                        param['subnetId'] = subnetId;

                        param['ipStatus'] = status;

                        param['exportTimeline'] = this.value();

                        // When all subnet deleted it doesn't call report grid

                        if(reportTreeId.find('input[type="checkbox"]:checked').length > 0 && reportTreeId.find('input[type="checkbox"]:checked').length != undefined)
                        {
                            $('#reportGrid').show();

                            if(className == "Vendor")
                            {
                                if(subnetId != null && subnetId != undefined && subnetId != "")
                                {
                                    report.loadVendorGridContext(null,subnetId,status,param.exportTimeline);
                                }
                                else
                                {
                                    report.loadVendorGridContext(null,firstuid,"Vendor Summary",10);
                                }
                            }
                            else
                            {
                                if(subnetId != null && subnetId != undefined && subnetId != "")
                                {
                                    report.loadReportGridContext(null,subnetId,status,param.exportTimeline);
                                }
                                else
                                {
                                    report.loadReportGridContext(null,firstuid,"All IP",10);
                                }
                            }
                        }
                        else
                        {
                            $(".k-grid-content").html(appConstant.NoDataSpan)
                        }
                    }
                });

                report.setActiveMenu(subnetId,status);

                reportDropDown.getKendoDropDownList().trigger('change');

                reportDropDown.data('kendoDropDownList').refresh();
            }
        },

        // ----------------------------------------------------------------------Render treeview----------------------------------------------------------------------------------------------------//

        afterReportTreeInit : function (context)
        {
            var treeId = $('#'+context.eventId);

            var reportPanel = $('#searchLeftPanel');

            var result = context.json.data;

            topManager.setActiveMenu("reports");

            if(result != null && context.json.success == true)
            {
                context.container.success(result);

                if(treeId.find(".k-item .k-group input:first").length != 0 && treeId.find(".k-item .k-group input:first").length != undefined)
                {
                    treeId.find(".k-item .k-group input:first").trigger('click');
                }
                else
                {
                    navigationManager.addHistory("navigation=reports");
                }

                var kendoTreeView = treeId.data("kendoTreeView");

                treeId.on("click", ".k-in .page", function(e) {
                    kendoTreeView.toggle($(e.target).closest(".k-item"));
                });

                if(navigationManager.getUrlParameter("subnetId") == undefined)
                {
                    reportPanel.find("[data-panel=reportTimeline]").remove();

                    reportPanel.find("[data-panel=searchReportGrid]").remove();

                    $('#reportGrid').html(appConstant.NoDataSpan);
                }
                appManager.resetWindowSize();

                appManager.initCustomScrollbar({container: $('#reportTreeView')});

                flux.bindEvent({element: 'reportGrid', selector: 'a[data-link=ipAddress]',breadCrumbMenu:"report"},ipAddressSummary.loadIPAddressSummary);
            }
            else
            {
                treeId.html(appConstant.NoDataSpan);
            }

            loaderUtil.hideModalLoader();
        },

        // -----------------------------------------------------------Load report grid with subnet id, subnet class and export timeline---------------------------------------------------------------------------------------------------------------//

        loadReportGridContext : function (event,firstUid,status,exportTimeline)
        {
            $('#searchFilter').val('');

            loaderUtil.showModalLoader();

            var id;

            var reportTimeline;

            var ipStatus;

            var reportTitle = $('.widget-header-title');

            if(firstUid && firstUid != "undefined")
            {
                id = firstUid;

                ipStatus = status;

                reportTimeline = exportTimeline;

                reportTitle.empty();

                reportTitle.html('<i class="ipam dashboard-icon"></i> '+ipStatus+' Report ');
            }

            navigationManager.addHistory("navigation=reports~~subnetId="+id+"~~status="+ipStatus+"~~exportTimeline="+reportTimeline);

            report.renderReportGrid(id,ipStatus,reportTimeline);

            loaderUtil.hideModalLoader();
        },

        // ---------------------------------------------------------------------Render report grid-----------------------------------------------------------------------------------------------------//

        renderReportGrid : function (id,ipStatus,reportTimeline)
        {
            var gridId = $('#'+report.ReportGrid);

            var param = {};

            param['subnetId'] = id;

            param['ipStatus'] = ipStatus;

            param['exportTimeline'] = reportTimeline;

            var callbackContexts =
                {
                    Read: function (options)
                    {
                        loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

                        appManager.executeGETRequest({url:'/subnetIpByReportTimeline/',container:options,callback:report.loadReportGridContextData,params:param});

                    },
                    EventId: report.ReportGrid,
                    PageSize: 20,
                    pageable: {
                        refresh: true,
                        pageSizes: [10,20,50,100],
                        buttonCount: 10
                    },
                    group: {
                        field: "ipAddress",
                        dir: "asc"
                    },
                    resizable:true,
                    groupable: true,
                    schema: {
                        model: {
                            id: "id",
                            fields: {
                                ipAddress: { type: "string"},
                                status: { type: "string" },
                                macAddress: { type: "string"},
                                deviceType: { type: "string"},
                                subnetName: {type : "string"},
                                authenticity: { type: "string"},
                                ipToDns: { type: "string" },
                                dnsToIp: { type: "string"},
                                lastAliveTime: { type: "string"}
                            }
                        }
                    },
                    Fields: [
                        {field: "ipAddress",width:"10%",title:"IP Address",template : "<a title='#: ipAddress #' data-uid='#: subnetId.id #' data-id='#: id#' data-link='ipAddress' data-value='#: ipAddress #' data-name='#:subnetId.subnetAddress#'>#: ipAddress #</a>"},
                        {field: "status", width:"8%", title: "Status",template:"#if(status == 'Available'){#<i class='fa fa-circle normal-v'></i><span>#:status#</span>#}else if(status == 'Used'){#<i class='fa fa-circle warning-v'></i><span>#:status#</span>#}if(status == 'Transient'){#<i class='fa fa-circle transient-v'></i><span>#:status#</span>#}else if(status == 'Reserved'){#<i class='fa fa-circle reserved-v'></i><span>#:status#</span>#}#"},
                        {
                            field:"subnetName",
                            template:"<span title='#:subnetName#'>#:subnetName#</span>",
                            title:"Scope",
                            filterable:false
                        },
                        {field: "macAddress", title: "Mac Address",template:'# if (macAddress) { # <span title="#:macAddress#">#: macAddress # </span># } else { #<span></span># } #'},
                        {field: "deviceType", title: "Vendor",template:'# if (deviceType) { # <span title="#:deviceType#">#: deviceType # </span># } else { #<span></span># } #'},
                        {field: "ipToDns", title: "IP To DNS",template:'# if (ipToDns) { # <span title="#:ipToDns#">#: ipToDns # </span># } else { #<span></span># } #'},
                        {field: "dnsToIp", title: "DNS To IP",template:'# if (dnsToIp) { # <span title="#:dnsToIp#">#: dnsToIp # </span># } else { #<span></span># } #'},
                        {field: "authenticity", title: "Authenticity",template:'# if (authenticity) { # <span title="#:authenticity#">#: authenticity # </span># } else { #<span></span># } #'},
                        {field:"lastAliveTime",title:"Last Alive Time",width:'13%',template:'# if (lastAliveTime) { # <span title="#:lastAliveTime#">#: lastAliveTime # </span># } else { #<span></span># } #'}
                    ],
                    sortable: true
                };

            // Destroy old grid context
            try {
                gridId.data().kendoGrid.destroy();
                gridId.empty();
            }
            catch(err)
            {
            }

            widgetRenderManager.renderGridData(callbackContexts);

            formManager.searchFilter(gridId);
        },

        // --------------------------------------------------------------------- Load vendor report grid with subnet id, subnet class and export timeline ----------------------------------------------------//

        loadVendorGridContext : function (event,firstUid,status,exportTimeline)
        {
            $('#searchFilter').val('');

            loaderUtil.showModalLoader();

            var id;

            var reportTimeline;

            var ipStatus;

            var reportTitle = $('.widget-header-title');

            if(firstUid && firstUid != "undefined")
            {
                id = firstUid;

                ipStatus = status;

                reportTimeline = exportTimeline;

                reportTitle.empty();

                reportTitle.html('<i class="ipam dashboard-icon"></i> Vendor Summary');
            }

            navigationManager.addHistory("navigation=reports~~subnetId="+id+"~~status="+ipStatus+"~~exportTimeline="+reportTimeline);

            report.renderVendorReportGrid(id,ipStatus,reportTimeline);

            loaderUtil.hideModalLoader();
        },

        renderVendorReportGrid : function (id,ipStatus,reportTimeline)
        {
            var gridId = $('#'+report.ReportGrid);

            gridId.empty();

            var param = {};

            param['subnetId'] = id;

            param['ipStatus'] = ipStatus;

            param['exportTimeline'] = reportTimeline;

            var callbackContexts =
                {
                    Read: function (options)
                    {
                        loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

                        appManager.executeGETRequest({url:'/subnetIpByReportTimeline/',container:options,callback:report.loadReportGridContextData,params:param});
                    },
                    EventId: report.ReportGrid,
                    PageSize: 20,
                    pageable: {
                        refresh: true,
                        pageSizes: [10,20,50,100],
                        buttonCount: 10
                    },
                    resizable:true,
                    groupable: true,
                    schema: {
                        model: {
                            fields: {
                                VendorName: { type: "string"},
                                VendorCount: { type: "number"},
                                VendorPercentage: {type : "number"}
                            }
                        }
                    },
                    Fields: [
                        {field: "VendorName",width:"60%",title:"Vendor Name",template : "<span title='#: VendorName #' data-link='VendorName' data-value='#: VendorName #'>#: VendorName #</span>"},
                        {field: "VendorCount", title: "Vendor Count",template:"<span title='#:VendorCount#'>#: VendorCount # </span>"},
                        {field: "VendorPercentage", title: "Percentage",template:'# if (VendorPercentage) { # <span title="#:VendorPercentage#">#: VendorPercentage # </span># } else { #<span></span># } #'},
                    ],
                    sortable: true
                };

            // Destroy old grid context
            try {
                gridId.data().kendoGrid.destroy();
                gridId.empty();
            }
            catch(err)
            {
            }

            widgetRenderManager.renderGridData(callbackContexts);

            formManager.searchFilter(gridId);

            loaderUtil.hideModalLoader();
        },

        loadReportGridContextData : function (context)
        {
            if(context.json.data != null && context.json.success == true)
            {
                var result = context.json.data;

                context.container.success(result);

                loaderUtil.hideCentralModalLoader();
            }
            else
            {
                context.container.success("");

                $(".k-grid-content").html(appConstant.NoDataSpan);

                loaderUtil.hideCentralModalLoader();
            }
        },

        // ------------------------------------------------------------------------Render report right panel--------------------------------------------------------------------------------------------------//

        renderRightPanelContext : function ()
        {
            appManager.executeGETRequest({url:'/reportScheduler/',eventId:'reportTreeView',callback:report.renderReportRightPanel});
        },

        renderReportRightPanel : function (context)
        {
            var reportPanel = $('#reportRightPanelContent');

            if(context.json.data !=null && context.json.data != undefined)
            {
                reportPanel.mCustomScrollbar("destroy");

                reportPanel.empty();

                var details;

                var repeatDay;

                var repeatDate;

                var repeatMonth;

                var repeatFlag;

                var html = '<ul>';

                $.each( context.json.data, function(key, value)
                {
                    repeatDate = value.repeatDate;

                    repeatMonth = value.repeatMonth;

                    repeatDay =  value.repeatDay;

                    repeatFlag = value.repeatFlag;

                    if(repeatDay == null && repeatDate == null && repeatMonth == null && repeatFlag == true)
                    {
                        details = 'Daily';
                    }
                    else if(repeatDay != null && repeatDate == null && repeatMonth == null && repeatFlag == true)
                    {
                        details = 'Weekly';
                    }
                    else if(repeatDate != null && repeatMonth != null && repeatDay == null && repeatFlag == true)
                    {
                        details = 'Monthly';
                    }
                    else if(repeatFlag == false)
                    {
                        details = 'Normal';
                    }

                    html += '<li> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"><label>Scheduler Name:</label> <p style="display: inline-block;">'+value.schedulerName+'</p></div><div class="col-xs-12 col-md-12 col-lg-3"> <label>Schedule For</label> <p name="ipFilter">'+value.ipFilter+'</p></div><div class="col-xs-12 col-md-12 col-lg-3"> <label>Date</label> <p name="schedulerDate">'+value.schedulerDate+'</p></div><div class="col-xs-12 col-md-12 col-lg-3"> <label>Time</label> <p name="schedulerTime">'+value.schedulerTime+'</p></div><div class="col-xs-12 col-md-12 col-lg-3"> <label>Frequently</label> <p name="frequently">'+details+'</p></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12 margin-0"> <a id="editReport" data-id="'+value.id+'" data-link="editReport" data-panel="editReport">Edit Scheduler</a> <a id="deleteReport" data-id="'+value.id+'" data-link="deleteReport" style="padding-left: 10px" data-panel="deleteReport">Delete Scheduler</a> </div></div></li>';

                    html += '<ul>';

                });
                reportPanel.append(html);

                appManager.resetWindowSize();

                appManager.initCustomScrollbar({container: reportPanel,selector:'.row'});

                flux.bindKendoButtonClickEvent({element: 'reportRightPanelContent #editReport'},report.onEditButtonClick);

                flux.bindKendoButtonClickEvent({element: 'reportRightPanelContent #deleteReport'},report.onDeleteButtonClick);
            }
            else
            {
                reportPanel.html('<div class="schedule-reports-top-panel" id="reportRightPanelContent">'+appConstant.NoDataSpan+'</div>');
            }
        },

        // ---------------------------------------------------------------------------------Edit report modal-----------------------------------------------------------------------------------------//

        onEditButtonClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                if(appManager.validatePermission() == true)
                {
                    loaderUtil.showModalLoader();

                    loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

                    var id = $(event.event.currentTarget).data('id');

                    var modal = $('#addModal');

                    modal.width('750px');

                    flux.kendoWindowSetOption({kendoWindow:modal});

                    $('#addModal_wnd_title').html('Edit Scheduler Report');

                    modal.data('kendoWindow').content('<form id="schedularForm"><div class="common-box-grid-panel padding-t-20" id="schedularEditModal"><div class="fixed-height-body-popup-panel widthFullSpacer"><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"> <label for="ipFilters">IP Filters</label> <input id="ipFilters" name="ipFilter" class="k-textbox" type="text" readonly/></div><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <label for="reportExportTimeline">Export Timeline</label> <select id="reportExportTimeline" name="reportExportTimeline" class="k-dropdown" required/></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"> <label for="schedulerDate">Select Date and Time</label><div class="row"><div class="col-xs-12 col-md-12 col-lg-4 date-select-drop-menu margin-0"> <input id="schedulerDate" name="schedulerDate" required/></div><div class="col-xs-12 col-md-12 col-lg-4 select-drop-menu margin-0"> <select id="schedulerTime" name="schedulerTime" required/></div><div class="col-xs-12 col-md-12 col-lg-4 select-drop-menu"> <input type="checkbox" id="repeatFlag" name="repeatFlag" class="k-checkbox" value="true"/> <label>Repeat</label></div></div></div><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <label for="exportType">Export Type</label> <select id="exportType" name="exportType" class="k-dropdown"/></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"> <label for="schedulerName">Scheduler Name</label> <input id="schedulerName" name="schedulerName" class="k-textbox" type="text" maxlength="50" required validationMessage="Scheduler Name is required"/></div><div class="col-xs-12 col-md-12 col-lg-6 auto-select-sm-panel"> <label for="emailTo">Email Report To</label> <select id="emailTo" name="emailTo" data-type="multiple" validationMessage="Valid Email Address is required" required/></div></div><div id="subnetPanel"></div><div id="repeatContent"></div></div><div class="footer-box-grid-box margin-t-10 align-right"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="updateSchedularButton">Update</button> <button class="defualt-btn k-button k-grid-cancel float-l" id="cancelSchedulerEditBtn">Cancel</button></div></div></form>');

                    var email = $("#emailTo");

                    var schedulerTime = $("#schedulerTime");

                    var exportType = $("#exportType");

                    var exportData = [{text:'PDF',value:'PDF'},{text:'CSV',value:'CSV'}];

                    var dateData = [{text:"00:00",value:"00:00"},{text:"00:15",value:"00:15"},{text:"00:30",value:"00:30"},{text:"00:45",value:"00:45"},{text:"01:00",value:"01:00"},{text:"01:15",value:"01:15"},{text:"01:30",value:"01:30"},{text:"01:45",value:"01:45"},{text:"02:00",value:"02:00"},{text:"02:15",value:"02:15"},{text:"02:30",value:"02:30"},{text:"02:45",value:"02:45"},{text:"03:00",value:"03:00"},{text:"03:15",value:"03:15"},{text:"03:30",value:"03:30"},{text:"03:45",value:"03:45"},{text:"04:00",value:"04:00"},{text:"04:15",value:"04:15"},{text:"04:30",value:"04:30"},{text:"04:45",value:"04:45"},{text:"05:00",value:"05:00"},{text:"05:15",value:"05:15"},{text:"05:30",value:"05:30"},{text:"05:45",value:"05:45"},{text:"06:00",value:"06:00"},{text:"06:15",value:"06:15"},{text:"06:30",value:"06:30"},{text:"06:45",value:"06:45"},{text:"07:00",value:"07:00"},{text:"07:15",value:"07:15"},{text:"07:30",value:"07:30"},{text:"07:45",value:"07:45"},{text:"08:00",value:"08:00"},{text:"08:15",value:"08:15"},{text:"08:30",value:"08:30"},{text:"08:45",value:"08:45"},{text:"09:00",value:"09:00"},{text:"09:15",value:"09:15"},{text:"09:30",value:"09:30"},{text:"09:45",value:"09:45"},{text:"10:00",value:"10:00"},{text:"10:15",value:"10:15"},{text:"10:30",value:"10:30"},{text:"10:45",value:"10:45"},{text:"11:00",value:"11:00"},{text:"11:15",value:"11:15"},{text:"11:30",value:"11:30"},{text:"11:45",value:"11:45"},{text:"12:00",value:"12:00"},{text:"12:15",value:"12:15"},{text:"12:30",value:"12:30"},{text:"12:45",value:"12:45"},{text:"13:00",value:"13:00"},{text:"13:15",value:"13:15"},{text:"13:30",value:"13:30"},{text:"13:45",value:"13:45"},{text:"14:00",value:"14:00"},{text:"14:15",value:"14:15"},{text:"14:30",value:"14:30"},{text:"14:45",value:"14:45"},{text:"15:00",value:"15:00"},{text:"15:15",value:"15:15"},{text:"15:30",value:"15:30"},{text:"15:45",value:"15:45"},{text:"16:00",value:"16:00"},{text:"16:15",value:"16:15"},{text:"16:30",value:"16:30"},{text:"16:45",value:"16:45"},{text:"17:00",value:"17:00"},{text:"17:15",value:"17:15"},{text:"17:30",value:"17:30"},{text:"17:45",value:"17:45"},{text:"18:00",value:"18:00"},{text:"18:15",value:"18:15"},{text:"18:30",value:"18:30"},{text:"18:45",value:"18:45"},{text:"19:00",value:"19:00"},{text:"19:15",value:"19:15"},{text:"19:30",value:"19:30"},{text:"19:45",value:"19:45"},{text:"20:00",value:"20:00"},{text:"20:15",value:"20:15"},{text:"20:30",value:"20:30"},{text:"20:45",value:"20:45"},{text:"21:00",value:"21:00"},{text:"21:15",value:"21:15"},{text:"21:30",value:"21:30"},{text:"21:45",value:"21:45"},{text:"22:00",value:"22:00"},{text:"22:15",value:"22:15"},{text:"22:30",value:"22:30"},{text:"22:45",value:"22:45"},{text:"23:00",value:"23:00"},{text:"23:15",value:"23:15"},{text:"23:30",value:"23:30"},{text:"23:45",value:"23:45"}];

                    var reportDropDown = $("#reportExportTimeline");

                    var data = [{text: "All", value: "10" },{text: "Today", value: "1" },{text: "Previous Day", value: "2" },{text: "This Week", value: "3" },{text: "This Month", value: "4" },{text: "This Quarter", value: "5" },{text: "Previous Quarter", value: "6" },
                        {text: "Last Six Month", value: "7" },{text: "This Year", value: "8" },{text: "Previous Year", value: "9" },{text: "Previous Week", value: "11" },{text: "Previous Month", value: "12" }];

                    flux.getKendoDropDownList({dropDownId:reportDropDown,dataTextField: "text",dataValueField: "value",data:data});

                    flux.getDatePicker({eventId:'schedulerDate'});

                    flux.getKendoDropDownList({dropDownId:schedulerTime,dataTextField: "text",dataValueField: "value",data:dateData});

                    flux.getKendoDropDownList({dropDownId:exportType,dataTextField: "text",dataValueField: "value",data:exportData});

                    flux.getMultiSelectKendoDropDownListURL({dropDownId:email,dataTextField: "mailToEmail",dataValueField: "mailToEmail",url:'/mail/'});

                    email.data('kendoMultiSelect').input.remove();

                    $('.k-multiselect-wrap').height('30px');

                    $('.k-multiselect-wrap li').remove('min-height');

                    flux.bindElementEvent({event:'change',element:'addModal',selector:'#repeatFlag' ,isEditable:true},report.renderRepeatContent);

                    flux.bindKendoButtonClickEvent({element : 'cancelSchedulerEditBtn',kendoModal:modal},report.closeSchedularModal);

                    var form = $("#schedularForm");

                    appManager.initCustomScrollbar({container:$(".fixed-height-body-popup-panel")});

                    appManager.executeGETRequest({url: '/reportScheduler/'+id, container: form, callback: formManager.renderForm,postCallback: report.afterRenderForm});

                    flux.bindEvent({element :'schedularEditModal #updateSchedularButton',kendoModal:modal,elementId:id},report.editSchedular);
                }
            }
        },

        // ---------------------------------------------------------------------Edit report post call back and render dropdown values-----------------------------------------------------------------------------------------------------//

        afterRenderForm : function (context)
        {
            if(context)
            {
                var dataContext = context.json.data;

                var repeatFlag = $('#repeatFlag');

                var ipFilters = $("#ipFilters");

                if(ipFilters.val() == "Event Log" || ipFilters.val() == "DHCP Utilization" || ipFilters.val() == "Subnet Utilization" || ipFilters.val() == "Conflict IP")
                {
                    $('#subnetPanel').html('<div id="subnetPanel"></div>');
                }
                else
                {
                    $('#subnetPanel').html('<div class="row" ><div class="col-xs-12 col-md-12 col-lg-12 auto-select-sm-panel"><label for="subnetId">Subnet Address</label><select id="subnetId" name="subnetId" data-type="multiple" validationMessage="Subnet Address is required" required/></div></div>');

                    var subnetAddress = $("#subnetId");

                    var subnetAddressValue = subnetAddress.kendoMultiSelect({
                        dataTextField: "subnetName",
                        dataValueField: "id",
                        dataSource: {
                            transport:
                                {
                                    read: function (options)
                                    {
                                        appManager.executeGETRequest({ url: '/subnet/', container: options, dropDownId: subnetAddress, callback: flux.afterDropDownInit});
                                    }
                                }
                        },
                        autoClose:true
                    }).data('kendoMultiSelect');

                    subnetAddress.data('kendoMultiSelect').input.remove();

                    if(dataContext.subnetId != null && dataContext.subnetId != undefined)
                    {
                        if(subnetAddressValue)
                        {
                            subnetAddressValue.value(dataContext.subnetId.split(","))
                        }
                    }
                }

                if(repeatFlag.prop('checked'))
                {
                    repeatFlag.trigger('change');

                    var schedulerTimeLine = dataContext.schedulerTimeLine;

                    var schedularDropdownList = $("#schedulerTimeLine").data('kendoDropDownList');

                    schedularDropdownList.value(schedulerTimeLine);

                    var value = dataContext.repeatHourTime.split(",");

                    var dropdownlist = $("#repeatHourTime").data('kendoMultiSelect');

                    dropdownlist.value(value);

                    if(schedulerTimeLine)
                    {
                        report.onScheduleTypeChange(null,schedulerTimeLine);

                        if(schedulerTimeLine == 1)
                        {
                            var repeatedDay = dataContext.repeatDay.split(",");

                            $("#repeatDay").data('kendoMultiSelect').value(repeatedDay);
                        }

                        else if(schedulerTimeLine == 2)
                        {
                            var repeatedDate = dataContext.repeatDate.split(",");

                            $("#repeatDate").data('kendoMultiSelect').value(repeatedDate);

                            var repeatedMonth = dataContext.repeatMonth.split(",");

                            $("#repeatMonth").data('kendoMultiSelect').value(repeatedMonth);
                        }
                    }
                }
            }
        },

        // ----------------------------------------------------------------------On report edit btn click event----------------------------------------------------------------------------------------------------//

        editSchedular : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var kendoEvent = event.data.kendoModal;

                var id = event.data.elementId;

                var form = $("#schedularForm");

                var validator = form.kendoValidator().data("kendoValidator");

                if(validator.validate())
                {
                    loaderUtil.showModalLoader();

                    var param = formManager.serializeForm(form);

                    appManager.executePUTRequest({url: '/reportScheduler/'+id, container: kendoEvent,callback: report.afterSchedulerAddOrUpdate, params: param});
                }
            }
        },

        // ----------------------------------------------------------------------------Delete report----------------------------------------------------------------------------------------------//

        onDeleteButtonClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                if(appManager.validatePermission() == true)
                {
                    var id = $(event.event.currentTarget).data('id');

                    flux.bindKendoModalEvent({container: 'deleteModal',uniqueId:'deleteReport',title:'Do you really want to delete the selected Scheduler?', params:id, closeCallback: flux.closeKendoDeleteModal, callback:report.deleteSchedulerPrompt});
                }
            }
        },

        deleteSchedulerPrompt : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var id =  event.data.context.params;

                var container = event.data.context.container;

                loaderUtil.showModalLoader();

                loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

                $("#"+container).data('kendoWindow').close();

                appManager.executeDELETERequest({url: '/reportScheduler/'+id, container:container, callback: report.afterEntityDeleted});
            }
        },

        afterEntityDeleted : function (context)
        {
            if(context)
            {
                if(context.json.success == true)
                {
                    report.renderRightPanelContext();

                    notification.showNotification({notificationTitle:context.json.message,notificationType:"success"});
                }
                else
                {
                    notification.showNotification({notificationTitle:context.json.message,notificationType:"error"});

                }
                loaderUtil.hideCentralModalLoader();

                loaderUtil.hideModalLoader();
            }
        },

        // ----------------------------------------------------------------------Add report modal----------------------------------------------------------------------------------------------------//

        onAddReportButtonClick : function (event)
        {
            if(event)
            {
                event.preventDefault();

                if(appManager.validatePermission() == true)
                {
                    var modal = $('#addModal');

                    modal.width('750px');

                    flux.kendoWindowSetOption({kendoWindow:modal});

                    $('#addModal_wnd_title').html('Create Scheduler Report');

                    modal.data('kendoWindow').content('<div class="common-box-grid-panel padding-t-20"> <form id="schedularForm"> <div class="fixed-height-body-popup-panel widthFullSpacer"> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <label for="ipFilters">IP Filters</label> <select id="ipFilters" name="ipFilter" class="k-dropdown"/></div><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <label for="reportExportTimeline">Export Timeline</label> <select id="reportExportTimeline" name="reportExportTimeline" class="k-dropdown" required/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-6"> <label for="schedulerDate">Select Date and Time</label> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-4 date-select-drop-menu margin-0"> <input id="schedulerDate" name="schedulerDate" required/></div><div class="col-xs-12 col-md-12 col-lg-4 select-drop-menu margin-0"> <select id="schedulerTime" name="schedulerTime" required/></div><div class="col-xs-12 col-md-12 col-lg-4 select-drop-menu"> <input type="checkbox" id="repeatFlag" name="repeatFlag" class="k-checkbox" value="true"/> <label>Repeat</label></div></div></div><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <label for="exportType">Export Type</label> <select id="exportType" name="exportType" class="k-dropdown"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-6"> <label for="schedulerName">Scheduler Name</label> <input id="schedulerName" name="schedulerName" class="k-textbox" type="text" maxlength="50" validationMessage="Scheduler Name is required" required/></div><div class="col-xs-12 col-md-12 col-lg-6 auto-select-sm-panel"> <label for="emailTo">Email Report To</label> <button id="addRecepientEmail" title="Add Email Recipients"> <i class="fa fa-plus"></i> </button> <select id="emailTo" name="emailTo" data-type="multiple" validationMessage="Valid Email Address is required" required/></div></div><div id="subnetPanel"></div><div id="repeatContent"></div></div><div class="footer-box-grid-box margin-t-10 align-right"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="addSchedularButton"> Create </button> <button class="defualt-btn k-button k-grid-cancel float-l" id="cancelSchedularButton">Cancel</button></div></form></div>');

                    var ipFiltersData = [{text: "All IP", value: "All IP" },{text: "Used IP", value: "Used IP" },{text: "Reserved IP", value: "Reserved IP" },
                        {text: "Transient IP", value: "Transient IP" }, {text: "Available IP", value: "Available IP"},{text: "Event Log", value: "Event Log" },
                        {text: "Rogue IP", value: "Rogue IP" },{text: "Trusted IP", value: "Trusted IP" },{text: "Conflict IP", value: "Conflict IP" },
                        {text: "DHCP Utilization", value: "DHCP Utilization" },{text: "Subnet Utilization", value: "Subnet Utilization" },{text: "Vendor Summary", value: "Vendor Summary"}];

                    var ipFilters = $("#ipFilters");

                    var email = $("#emailTo");

                    var schedulerTime = $("#schedulerTime");

                    var reportDropDown = $("#reportExportTimeline");

                    var exportType = $("#exportType");

                    ipFilters.change(function (e)
                    {
                       if(e)
                       {
                           e.preventDefault();

                           if(ipFilters.val() == "Event Log" || ipFilters.val() == "DHCP Utilization" || ipFilters.val() == "Subnet Utilization" || ipFilters.val() == "Conflict IP")
                           {
                               $('#subnetPanel').html('<div id="subnetPanel"></div>');
                           }
                           else
                           {
                               $('#subnetPanel').html('<div class="row" ><div class="col-xs-12 col-md-12 col-lg-12 auto-select-sm-panel"><label for="subnetId">Subnet Address</label><select id="subnetId" name="subnetId" data-type="multiple" validationMessage="Subnet Address is required" required/></div></div>');

                               var subnetAddress = $("#subnetId");

                               flux.getMultiSelectKendoDropDownListURL({dropDownId:subnetAddress,dataTextField: "subnetName",dataValueField: "id",url:'/subnet/'});

                               subnetAddress.data('kendoMultiSelect').input.remove();
                           }
                       }
                    });

                    ipFilters.trigger('change');

                    var exportData = [{text:'PDF',value:'pdf'},{text:'CSV',value:'csv'}];

                    var data = [{text: "All", value: "10" },{text: "Today", value: "1" },{text: "Previous Day", value: "2" },{text: "This Week", value: "3" },{text: "This Month", value: "4" },{text: "This Quarter", value: "5" },{text: "Previous Quarter", value: "6" },
                        {text: "Last Six Month", value: "7" },{text: "This Year", value: "8" },{text: "Previous Year", value: "9" },{text: "Previous Week", value: "11" },{text: "Previous Month", value: "12" }];

                    flux.getKendoDropDownList({dropDownId:reportDropDown,dataTextField: "text",dataValueField: "value",data:data});

                    var dateData = [{text:"00:00",value:"00:00"},{text:"00:15",value:"00:15"},{text:"00:30",value:"00:30"},{text:"00:45",value:"00:45"},{text:"01:00",value:"01:00"},{text:"01:15",value:"01:15"},{text:"01:30",value:"01:30"},{text:"01:45",value:"01:45"},{text:"02:00",value:"02:00"},{text:"02:15",value:"02:15"},{text:"02:30",value:"02:30"},{text:"02:45",value:"02:45"},{text:"03:00",value:"03:00"},{text:"03:15",value:"03:15"},{text:"03:30",value:"03:30"},{text:"03:45",value:"03:45"},{text:"04:00",value:"04:00"},{text:"04:15",value:"04:15"},{text:"04:30",value:"04:30"},{text:"04:45",value:"04:45"},{text:"05:00",value:"05:00"},{text:"05:15",value:"05:15"},{text:"05:30",value:"05:30"},{text:"05:45",value:"05:45"},{text:"06:00",value:"06:00"},{text:"06:15",value:"06:15"},{text:"06:30",value:"06:30"},{text:"06:45",value:"06:45"},{text:"07:00",value:"07:00"},{text:"07:15",value:"07:15"},{text:"07:30",value:"07:30"},{text:"07:45",value:"07:45"},{text:"08:00",value:"08:00"},{text:"08:15",value:"08:15"},{text:"08:30",value:"08:30"},{text:"08:45",value:"08:45"},{text:"09:00",value:"09:00"},{text:"09:15",value:"09:15"},{text:"09:30",value:"09:30"},{text:"09:45",value:"09:45"},{text:"10:00",value:"10:00"},{text:"10:15",value:"10:15"},{text:"10:30",value:"10:30"},{text:"10:45",value:"10:45"},{text:"11:00",value:"11:00"},{text:"11:15",value:"11:15"},{text:"11:30",value:"11:30"},{text:"11:45",value:"11:45"},{text:"12:00",value:"12:00"},{text:"12:15",value:"12:15"},{text:"12:30",value:"12:30"},{text:"12:45",value:"12:45"},{text:"13:00",value:"13:00"},{text:"13:15",value:"13:15"},{text:"13:30",value:"13:30"},{text:"13:45",value:"13:45"},{text:"14:00",value:"14:00"},{text:"14:15",value:"14:15"},{text:"14:30",value:"14:30"},{text:"14:45",value:"14:45"},{text:"15:00",value:"15:00"},{text:"15:15",value:"15:15"},{text:"15:30",value:"15:30"},{text:"15:45",value:"15:45"},{text:"16:00",value:"16:00"},{text:"16:15",value:"16:15"},{text:"16:30",value:"16:30"},{text:"16:45",value:"16:45"},{text:"17:00",value:"17:00"},{text:"17:15",value:"17:15"},{text:"17:30",value:"17:30"},{text:"17:45",value:"17:45"},{text:"18:00",value:"18:00"},{text:"18:15",value:"18:15"},{text:"18:30",value:"18:30"},{text:"18:45",value:"18:45"},{text:"19:00",value:"19:00"},{text:"19:15",value:"19:15"},{text:"19:30",value:"19:30"},{text:"19:45",value:"19:45"},{text:"20:00",value:"20:00"},{text:"20:15",value:"20:15"},{text:"20:30",value:"20:30"},{text:"20:45",value:"20:45"},{text:"21:00",value:"21:00"},{text:"21:15",value:"21:15"},{text:"21:30",value:"21:30"},{text:"21:45",value:"21:45"},{text:"22:00",value:"22:00"},{text:"22:15",value:"22:15"},{text:"22:30",value:"22:30"},{text:"22:45",value:"22:45"},{text:"23:00",value:"23:00"},{text:"23:15",value:"23:15"},{text:"23:30",value:"23:30"},{text:"23:45",value:"23:45"}];

                    flux.getDatePicker({eventId:'schedulerDate'});

                    flux.getKendoDropDownList({dropDownId:ipFilters,dataTextField: "text",dataValueField: "value",data:ipFiltersData});

                    flux.getKendoDropDownList({dropDownId:schedulerTime,dataTextField: "text",dataValueField: "value",data:dateData});

                    flux.getKendoDropDownList({dropDownId:exportType,dataTextField: "text",dataValueField: "value",data:exportData});

                    flux.getMultiSelectKendoDropDownListURL({dropDownId:email,dataTextField: "mailToEmail",dataValueField: "mailToEmail",url:'/mail/'});

                    email.data('kendoMultiSelect').input.remove();

                    $('.k-multiselect-wrap').height('30px');

                    $('.k-multiselect-wrap li').remove('min-height');

                    flux.bindElementEvent({event:'change',element:'addModal',selector:'#repeatFlag'},report.renderRepeatContent);

                    appManager.initCustomScrollbar({container:$(".fixed-height-body-popup-panel")});

                    flux.bindKendoButtonClickEvent({element : 'addSchedularButton',kendoModal:modal},report.addSchedular);

                    flux.bindKendoButtonClickEvent({element : 'addRecepientEmail'},report.onRecepientModalClick);

                    flux.bindKendoButtonClickEvent({element : 'cancelSchedularButton',kendoModal:modal},report.closeSchedularModal);
                }
            }
        },

        // --------------------------------------------------------------------Render repeat content of report scheduler------------------------------------------------------------------------------------------------------//

        renderRepeatContent : function (event)
        {
            if(event)
            {
                var repeatContent = $("#repeatContent");

                var checkedValue = $(event.currentTarget).context.checked;

                if(checkedValue)
                {
                    repeatContent.html('<div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <div class="col-xs-12 col-md-12 col-lg-6 padding-l-0 select-drop-menu"><label>Scheduler Time Line</label><select id="schedulerTimeLine" name="schedulerTimeLine" class="k-dropdown" /></div><div class="col-xs-12 col-md-12 col-lg-6 padding-r-0 auto-select-sm-panel"> <label for="repeatHourTime">Repeat On</label> <select id="repeatHourTime" name="repeatHourTime" data-type="multiple" validationMessage="Repeat Hours is required" required/></div></div></div><div id="repeatContext"></div>');

                    var schedulerTimeLine = $("#schedulerTimeLine");

                    var scheduleData = [{text: "Daily", value: 0},{text: "Weekly", value: 1 },{text: "Monthly", value: 2 }];

                    var repeatHourTime = $("#repeatHourTime");

                    flux.getKendoDropDownList({dropDownId:schedulerTimeLine,dataTextField: "text",dataValueField: "value",data:scheduleData});

                    var repeatValue = [{text:'12:00 AM',value:'00:00'},{text:'12:15 AM',value:'00:15'},{text:'12:30 AM',value:'00:30'},{text:'12:45 AM',value:'00:45'},{text:'1:00 AM',value:'01:00'},{text:'1:15 AM',value:'01:15'},{text:'1:30 AM',value:'01:30'},{text:'1:45 AM',value:'01:45'},{text:'2:00 AM',value:'02:00'},{text:'2:15 AM',value:'02:15'},{text:'2:30 AM',value:'02:30'},{text:'2:45 AM',value:'02:45'},{text:'3:00 AM',value:'03:00'},{text:'3:15 AM',value:'03:15'},{text:'3:30 AM',value:'03:30'},{text:'3:45 AM',value:'03:45'},{text:'4:00 AM',value:'04:00'},{text:'4:15 AM',value:'04:15'},
                        {text:'4:30 AM',value:'04:30'},{text:'4:45 AM',value:'04:45'},{text:'5:00 AM',value:'05:00'},{text:'5:15 AM',value:'05:15'},{text:'5:30 AM',value:'05:30'},{text:'5:45 AM',value:'05:45'},{text:'6:00 AM',value:'06:00'},{text:'6:15 AM',value:'06:15'},{text:'6:30 AM',value:'06:30'},{text:'6:45 AM',value:'06:45'},{text:'7:00 AM',value:'07:00'},{text:'7:15 AM',value:'07:15'},{text:'7:30 AM',value:'07:30'},{text:'7:45 AM',value:'07:45'},{text:'8:00 AM',value:'08:00'},{text:'8:15 AM',value:'08:15'},{text:'8:30 AM',value:'08:30'},{text:'8:45 AM',value:'08:45'},{text:'9:00 AM',value:'09:00'},{text:'9:15 AM',value:'09:15'},{text:'9:30 AM',value:'09:30'},{text:'9:45 AM',value:'09:45'}
                        ,{text:'10:00 AM',value:'10:00'},{text:'10:15 AM',value:'10:15'},{text:'10:30 AM',value:'10:30'},{text:'10:45 AM',value:'10:45'},{text:'11:00 AM',value:'11:00'},{text:'11:15 AM',value:'11:15'},{text:'11:30 AM',value:'11:30'},{text:'11:45 AM',value:'11:45'},{text:'12:00 PM',value:'12:00'},{text:'12:15 PM',value:'12:15'},{text:'12:30 PM',value:'12:30'},{text:'12:45 PM',value:'12:45'},{text:'1:00 PM',value:'13:00'},{text:'1:15 PM',value:'13:15'},{text:'1:30 PM',value:'13:30'},{text:'1:45 PM',value:'13:45'},{text:'2:00 PM',value:'14:00'},{text:'2:15 PM',value:'14:15'},{text:'2:30 PM',value:'14:30'},{text:'2:45 PM',value:'14:45'},{text:'3:00 PM',value:'15:00'},{text:'3:15 PM',value:'15:15'},{text:'3:30 PM',value:'15:30'},{text:'3:45 PM',value:'15:45'},{text:'4:00 PM',value:'16:00'},{text:'4:15 PM',value:'16:15'}
                        ,{text:'4:30 PM',value:'16:30'},{text:'4:45 PM',value:'16:45'},{text:'5:00 PM',value:'17:00'},{text:'5:15 PM',value:'17:15'},{text:'5:30 PM',value:'17:30'},{text:'5:45 PM',value:'17:45'},{text:'6:00 PM',value:'18:00'},{text:'6:15 PM',value:'18:15'},{text:'6:30 PM',value:'18:30'},{text:'6:45 PM',value:'18:45'},{text:'7:00 PM',value:'19:00'},{text:'7:15 PM',value:'19:15'},{text:'7:30 PM',value:'19:30'},{text:'7:45 PM',value:'19:45'},{text:'8:00 PM',value:'20:00'},{text:'8:15 PM',value:'20:15'},{text:'8:30 PM',value:'20:30'},{text:'8:45 PM',value:'20:45'},{text:'9:00 PM',value:'21:00'},{text:'9:15 PM',value:'21:15'},{text:'9:30 PM',value:'21:30'},{text:'9:45 PM',value:'21:45'}
                        ,{text:'10:00 PM',value:'22:00'},{text:'10:15 PM',value:'22:15'},{text:'10:30 PM',value:'22:30'},{text:'10:45 PM',value:'22:45'},{text:'11:00 PM',value:'23:00'},{text:'11:15 PM',value:'23:15'},{text:'11:30 PM',value:'23:30'},{text:'11:45 PM',value:'23:45'}];

                    flux.getMultiSelectKendoDropDownList({dropDownId:repeatHourTime,value:$("#schedulerTime").val(), dataTextField: "text",dataValueField: "value",data:repeatValue});

                    repeatHourTime.data('kendoMultiSelect').input.remove();

                    $('.k-multiselect-wrap').height('30px');

                    $('.k-multiselect-wrap li').remove('min-height');

                    flux.bindElementEvent({event:'change',element:'repeatContent',selector:'#schedulerTimeLine'},report.onScheduleTypeChange);
                }
                else
                {
                    repeatContent.html('<div id="repeatContext"></div>');
                }
            }
        },

        // ------------------------------------------------------------------Render value on report scheduler type change eg. daily,weekly,monthly--------------------------------------------------------------------------------------------------------//

        onScheduleTypeChange : function (event,schedularTimeLine)
        {
            var scheduleType;

            if(event)
            {
                scheduleType = $(event.currentTarget).val();
            }
            else if(schedularTimeLine)
            {
                scheduleType = schedularTimeLine;
            }

            if(scheduleType)
            {
                var repeatContext = $("#repeatContext");

                var repeatValue = $("#schedulerDate").val().split("/")[1];

                if(scheduleType == 1)
                {
                    repeatContext.html('<div class="row"> <div class="col-xs-12 col-md-12 col-lg-6 auto-select-sm-panel"> <label for="repeatDay">Day</label> <select id="repeatDay" name="repeatDay" data-type="multiple" validationMessage="Repeat Days is required" required/> </div></div>');

                    var repeatDay = $("#repeatDay");

                    var repeatDayData = [{text:'Sunday',value:'SUN'},{text:'Monday',value:'MON'},{text:'Tuesday',value:'TUE'},{text:'Wednesday',value:'WED'},{text:'Thursday',value:'THU'},{text:'Friday',value:'FRI'},{text:'Saturday',value:'SAT'}];

                    flux.getMultiSelectKendoDropDownList({dropDownId:repeatDay,value:repeatDay, dataTextField: "text",dataValueField: "value",data:repeatDayData});

                    repeatDay.data('kendoMultiSelect').input.remove();

                    $('.k-multiselect-wrap').height('30px');

                    $('.k-multiselect-wrap li').remove('min-height');

                }

                else if(scheduleType == 2)
                {
                    repeatContext.html('<div class="row"> <div class="col-xs-12 col-md-12 col-lg-6 auto-select-sm-panel"> <label for="repeatDate">Date</label> <select id="repeatDate" name="repeatDate" data-type="multiple" validationMessage="Repeate Dates is required" required/></div><div class="col-xs-12 col-md-12 col-lg-6 auto-select-sm-panel"> <label for="repeatMonth">Month</label> <select id="repeatMonth" name="repeatMonth" data-type="multiple" validationMessage="Repeat Months is required" required/></div></div>');

                    var repeatDateData =[{text:'1',value:1},{text:'2',value:2},{text:'3',value:3},{text:'4',value:4},{text:'5',value:5},{text:'6',value:6},{text:'7',value:7},{text:'8',value:8},{text:'9',value:9},{text:'10',value:10},
                        {text:'11',value:11},{text:'12',value:12},{text:'13',value:13},{text:'14',value:14},{text:'15',value:15},{text:'16',value:16},{text:'17',value:17},{text:'18',value:18},{text:'19',value:19},{text:'20',value:20},
                        {text:'21',value:21},{text:'22',value:22},{text:'23',value:23},{text:'24',value:24},{text:'25',value:25},{text:'26',value:26},{text:'27',value:27},{text:'28',value:28},{text:'29',value:29},{text:'30',value:30},
                        {text:'31',value:31}];

                    var repeatDate = $("#repeatDate");

                    var repeatMonth = $('#repeatMonth');

                    var repeatMonthData = [{text:'January',value:'JAN'},{text:'February',value:'FEB'},{text:'March',value:'MAR'},{text:'April',value:'APR'},{text:'May',value:'MAY'},{text:'June',value:'JUN'},{text:'July',value:'JUL'},{text:'August',value:'AUG'},{text:'September',value:'SEP'},{text:'October',value:'OCT'},{text:'November',value:'NOV'},{text:'December',value:'DEC'}];

                    flux.getMultiSelectKendoDropDownList({dropDownId:repeatDate,value:repeatValue, dataTextField: "text",dataValueField: "value",data:repeatDateData});

                    repeatDate.data('kendoMultiSelect').input.remove();

                    flux.getMultiSelectKendoDropDownList({dropDownId:repeatMonth, dataTextField: "text",dataValueField: "value",data:repeatMonthData});

                    repeatMonth.data('kendoMultiSelect').input.remove();

                    $('.k-multiselect-wrap').height('30px');

                    $('.k-multiselect-wrap li').remove('min-height');
                }
                else
                {
                    repeatContext.html('<div id="repeatContext"></div>');
                }
            }
        },

        // -------------------------------------------------------------------Add to recepient email modal-------------------------------------------------------------------------------------------------------//

        onRecepientModalClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var modal = $('#innerModal');

                modal.width('300px');

                flux.kendoWindowSetOption({kendoWindow:modal});

                $('#innerModal_wnd_title').html('Add Recipient Email');

                modal.data("kendoWindow").content('<form id="recipientEmailForm"><div class="fixed-height-body-popup-panel margin-t-20"><div class="row"><div class="col-xs-12 col-md-12 col-lg-12"> <label for="mailToEmail">Email Address</label> <input id="mailToEmail" type="email" name="mailToEmail" class="k-textbox" required validationMessage="Valid Email Address is required"/></div></div></div><div class="footer-box-grid-box margin-t-10 align-right"><div class="bottom-form-panel"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="recipientAddBtn">Add</button> <button class="defualt-btn k-button k-grid-cancel float-l" id="recipientCancelBtn">Cancel</button></div></div></form>');

                flux.bindKendoButtonClickEvent({element: 'recipientAddBtn', kendoModal:modal }, report.AddRecipientEmailEntity);

                flux.bindKendoButtonClickEvent({element: 'recipientCancelBtn', kendoModal:modal}, flux.closeKendoModal);
            }
        },

        AddRecipientEmailEntity : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var kendoModal = event.sender.options.prefix.kendoModal;

                var form = $('#recipientEmailForm');

                var validator = form.kendoValidator().data("kendoValidator");

                if(validator.validate())
                {
                    var formData = new FormData();

                    formData.append('mailToEmail', $("#mailToEmail").val());

                    loaderUtil.showModalLoader();

                    kendoModal.data('kendoWindow').close();

                    appManager.executeFileRequest({url:'/insertMail/', type: 'POST',container:kendoModal,callback:report.afterEmailRecipientAdded,params:formData});
                }
            }
        },

        afterEmailRecipientAdded :function (callbackContexts)
        {
            if(callbackContexts)
            {
                if(callbackContexts.json.success == true)
                {
                    $("#emailTo").data("kendoMultiSelect").dataSource.read();

                    notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"success"});
                }
                else
                {
                    notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"error"});
                }
                loaderUtil.hideModalLoader();
            }
        },

        // --------------------------------------------------------------------------------------Add report scheduler btn event------------------------------------------------------------------------------------//

        addSchedular : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var kendoEvent = event.sender.options.prefix.kendoModal;

                var form = $("#schedularForm");

                var validator = form.kendoValidator().data("kendoValidator");

                if(validator.validate())
                {
                    loaderUtil.showModalLoader();

                    var param = formManager.serializeForm(form);

                    appManager.executePOSTRequest({url: '/reportScheduler/',container:kendoEvent, callback: report.afterSchedulerAddOrUpdate, params: param});
                }
            }
        },

        // ---------------------------------------------------------------After report add & edit operations-----------------------------------------------------------------------------------------------------------//

        afterSchedulerAddOrUpdate : function (callbackContexts)
        {
            if(callbackContexts)
            {
                var modal = callbackContexts.container;

                if(callbackContexts.json.success == true)
                {
                    modal.data('kendoWindow').close();

                    report.renderRightPanelContext();

                    notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"success"});
                }
                else
                {
                    notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"error"});
                }
                loaderUtil.hideModalLoader();
            }
        },

        // ---------------------------------------------------------------------Close report modal-----------------------------------------------------------------------------------------------------//

        closeSchedularModal : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var modal = event.sender.options.prefix.kendoModal;

                modal.data('kendoWindow').close();
            }
        },

        // -------------------------------------------------------------------------Report Export event with subnetid,subnet class & export timeline-------------------------------------------------------------------------------------------------//

        onExportReportClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var exportType = event.sender.options.prefix.exportType;

                var param = {};

                param['subnetId'] = navigationManager.getUrlParameter("subnetId");

                param['ipStatus'] = navigationManager.getUrlParameter("status");

                param['exportTimeline'] = navigationManager.getUrlParameter("exportTimeline");

                var treeViewId = $("#reportTreeView");

                var checkedId=[];

                var i = 0;

                treeViewId.find("input:checked").each(function(index,value)
                {
                    if(!$.isEmptyObject(value.dataset.uid) && value.dataset.uid.length > 0)
                    {
                        checkedId[i]=value.dataset.uid;

                        i++;
                    }
                });

                if(checkedId.length > 0)
                {
                    if(param.subnetId != undefined && param.subnetId != "undefined")
                    {
                        loaderUtil.showCentralModalLoader(appConstant.ExportMessage);

                        loaderUtil.showModalLoader();

                        if(exportType == 1)
                        {
                            appManager.executeGETRequest({url:'/exportsubnetIpByReportTimeline/',callback:report.downloadPdf,params:param});
                        }
                        if(exportType == 2)
                        {
                            appManager.executeGETRequest({url:'/exportsubnetIpCsvByReportTimeline/',callback:report.downloadCsv,params:param});
                        }
                    }
                    else
                    {
                        notification.showNotification({notificationTitle:"No Data Available",notificationType:"info"});
                    }
                }
                else
                {
                    notification.showNotification({notificationTitle:"No Subnets are selected",notificationType:"info"});
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

        // --------------------------------------------------------------------------Navigation------------------------------------------------------------------------------------------------//

        renderReportPageFromUrl : function ()
        {
            report.init();
        },

        // --------------------------------------------------------------------------Expand selected parent------------------------------------------------------------------------------------------------//

        setActiveMenu : function (element,elementValue)
        {
            var treeViewName = $("#reportTreeView");

            var item = treeViewName.find("span:has(span[data-uid='"+element+"']):has(span[data-property='"+elementValue+"'])").closest('ul').closest('li');

            treeViewName.data('kendoTreeView').expand(item);
        }
    };