/**
 * Created by hardik on 21/5/18.
 */
var widget=
{
    // -------------------------------------------------------Render dashboard IP Summary context-------------------------------------------------------------------------------------------------------------------//

    renderIPAvailabilityStatus : function (context)
    {
        var dashboardIPAvailability = $('#dashboardIPSummary').find('li');

        var result = context.json.data;

        if(dashboardIPAvailability)
        {
            if(result != null && result != undefined)
            {
                dashboardIPAvailability.find('[name="dashboardUsedIP"]').text(result.usedIp);

                dashboardIPAvailability.find('[name="dashboardAvailableIP"]').text(result.availableIp);

                dashboardIPAvailability.find('[name="dashboardTransientIP"]').text(result.transientIp);
            }
            else
            {
                dashboardIPAvailability.find('[name="dashboardUsedIP"]').text('N/A');

                dashboardIPAvailability.find('[name="dashboardAvailableIP"]').text('N/A');

                dashboardIPAvailability.find('[name="dashboardTransientIP"]').text('N/A');
            }
        }
    },

    // -----------------------------------------------------------Render dashboard ping status---------------------------------------------------------------------------------------------------------------//

    renderPingStatus : function (context)
    {
        var dashboardIPAvailability = $('#dashboardIPSummary').find('li');

        var result = context.json.data;

        if(dashboardIPAvailability)
        {
            if(result != null && result != undefined)
            {
                dashboardIPAvailability.find('[name="dashboardTotalPing"]').text(result.totalIp);

                dashboardIPAvailability.find('[name="dashboardFailurePing"]').text(result.totalIp - result.usedIp);
            }
            else
            {
                dashboardIPAvailability.find('[name="dashboardTotalPing"]').text('N/A');

                dashboardIPAvailability.find('[name="dashboardFailurePing"]').text('N/A');
            }
        }
    },

    // -----------------------------------------------------------------Render dashboard Authenticty---------------------------------------------------------------------------------------------------------//

    renderRogueIPStatus : function (context)
    {
        var dashboardIPAvailability = $('#dashboardIPSummary').find('li');

        var result = context.json.data;

        if(dashboardIPAvailability)
        {
            if(result != null && result != undefined)
            {
                dashboardIPAvailability.find('[name="dashboardDiscoverRogue"]').text(result.totalIp);

                dashboardIPAvailability.find('[name="dashboardRogue"]').text(result.rogueIp);

                dashboardIPAvailability.find('[name="dashboardTrusted"]').text(result.trustedIp);

                loaderUtil.hideCentralModalLoader();
            }
            else
            {
                dashboardIPAvailability.find('[name="dashboardDiscoverRogue"]').text('N/A');

                dashboardIPAvailability.find('[name="dashboardRogue"]').text('N/A');

                dashboardIPAvailability.find('[name="dashboardTrusted"]').text('N/A');

                loaderUtil.hideCentralModalLoader();
            }
        }
    },

    // ------------------------------------------------------------------Load subnet utilization grid--------------------------------------------------------------------------------------------------------//

    renderSubnetUtilization : function ()
    {
        var gridId = $('#'+homeManager.SubnetUtilization);

        var callbackContexts =  {
            Read: function (options)
            {
                appManager.executeGETRequest({url: '/normalSubnet/',container:options,params:homeManager.SubnetUtilization,callback:widget.renderGridContext});
            },
            EventId: homeManager.SubnetUtilization,
            PageSize: 5,
            pageable: {
                refresh: true,
                pageSizes: false,
                buttonCount: 10
            },
            resizable:false,
            schema: {
                model: {
                    id: "id",
                    fields: {
                        subnetAddress: { type: "string"},
                        usedIp: { type: "string" },
                        totalIp: { type: "string"},
                        availableIp: { type: "string" }
                    }
                }
            },
            Fields: [
                {field:"id",hidden:true},
                {field: "subnetAddress", template : "<i class='ipam network-icon'></i> <a data-uid='#: id #' data-link='subnetAddress' data-name='#: subnetName #' title='#: subnetName #'>#: subnetName #</a>", title: "Subnet"},
                {field: "usedIpPercentage",width:'25%', title: "% in Space Used",template:'#if(severity == 1){#<div class="slim-chart-main"><div class="slim-chart-inner"><div class="critical-slim" style="width:#:usedIpPercentage#%"></div></div>#}else if(severity == 2){#</div><div class="slim-chart-main"><div class="slim-chart-inner"><div class="warning-slim" style="width:#:usedIpPercentage#%"></div></div>#}else{#</div><div class="slim-chart-main"><div class="slim-chart-inner"><div class="normal-slim" style="width:#:usedIpPercentage#%"></div></div>#}##= kendo.toString(usedIpPercentage,"n2")#%</div>'},
                {field: "usedIp", title: "Used IP",width:'15%'},
                {field: "availableIp", title: "Available IP",width:'15%'}]
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

        appManager.initCustomScrollbar({container:$('#subnetUtilization').find('.k-grid-content')});

        flux.bindEvent({element:'subnetAction #subnetUtilizationExport'}, homeManager.openSubnetActionPopup);

        flux.bindEvent({element: 'subnetUtilization', selector:'a[data-link=subnetAddress]',breadCrumbMenu:"home"},subnetSummary.renderSubnetPage);
    },

    renderTop10SubnetUtilization : function ()
    {
        var gridId = $('#'+homeManager.Top10SubnetUtilization);

        var callbackContexts =  {
            Read: function (options)
            {
                appManager.executeGETRequest({url: '/top10SubnetUtilization/',container:options,params:homeManager.Top10SubnetUtilization,callback:widget.renderGridContext});
            },
            EventId: homeManager.Top10SubnetUtilization,
            PageSize: 5,
            pageable: {
                refresh: true,
                pageSizes: false,
                buttonCount: 10
            },
            resizable:false,
            schema: {
                model: {
                    id: "id",
                    fields: {
                        subnetAddress: { type: "string"},
                        usedIp: { type: "string" },
                    }
                }
            },
            Fields: [
                {field:"id",hidden:true},
                {field: "subnetAddress", template : "<i class='ipam network-icon'></i> <a data-uid='#: id #' data-link='subnetAddress' data-name='#: subnetName #' title='#: subnetName #'>#: subnetName #</a>", title: "Subnet"},
                {field: "usedIpPercentage", title: "% in Space Used",template:'#if(severity == 1){#<div class="slim-chart-main"><div class="slim-chart-inner"><div class="critical-slim" style="width:#:usedIpPercentage#%"></div></div>#}else if(severity == 2){#</div><div class="slim-chart-main"><div class="slim-chart-inner"><div class="warning-slim" style="width:#:usedIpPercentage#%"></div></div>#}else{#</div><div class="slim-chart-main"><div class="slim-chart-inner"><div class="normal-slim" style="width:#:usedIpPercentage#%"></div></div>#}##= kendo.toString(usedIpPercentage,"n2")#%</div>'},
               ]
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

        appManager.initCustomScrollbar({container:$('#subnetUtilization').find('.k-grid-content')});

        flux.bindEvent({element:'top10SubnetUtilizationExport'}, homeManager.openTop10SubnetUtilizationActionPopup);

        flux.bindEvent({element: 'top10SubnetUtilization', selector:'a[data-link=subnetAddress]',breadCrumbMenu:"home"},subnetSummary.renderSubnetPage);

    },

    renderTop10CategoryUtilization : function ()
    {
        var gridId = $('#'+homeManager.Top10CategoryUtilization);

        var callbackContexts =
        {
            Read: function (options)
            {
                appManager.executeGETRequest({url: '/top10CategoryUtilization/',container:options,params:homeManager.Top10CategoryUtilization,callback:widget.renderGridContext});
            },
            EventId: homeManager.Top10CategoryUtilization,
            PageSize: 5,
            pageable: {
                refresh: true,
                pageSizes: false,
                buttonCount: 10
            },
            resizable:false,
            schema: {
                model: {
                    id: "id",
                    fields: {
                        categoryName: { type: "string"},
                        totalUsedIpPercentage: { type: "string" },
                    }
                }
            },
            Fields: [
                {field:"id",hidden:true},
                {field: "categoryName", template : "<i class='ipam network-icon'></i> <a data-uid='#: id #' data-link='categoryName' data-name='#: categoryName #' title='#: categoryName #'>#: categoryName #</a>", title: "Category"},
                {field: "totalUsedIpPercentage", title: "% in Space Used",template:'#if(severity == 1){#<div class="slim-chart-main"><div class="slim-chart-inner"><div class="critical-slim" style="width:#:totalUsedIpPercentage#%"></div></div>#}else if(severity == 2){#</div><div class="slim-chart-main"><div class="slim-chart-inner"><div class="warning-slim" style="width:#:totalUsedIpPercentage#%"></div></div>#}else{#</div><div class="slim-chart-main"><div class="slim-chart-inner"><div class="normal-slim" style="width:#:totalUsedIpPercentage#%"></div></div>#}##= kendo.toString(totalUsedIpPercentage,"n2")#%</div>'},
                ]
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

        appManager.initCustomScrollbar({container:$('#subnetUtilization').find('.k-grid-content')});

        flux.bindEvent({element:'top10CategoryUtilizationExport'}, homeManager.openTop10CategoryActionPopup);
    },

    renderDNSPieChart : function (context)
    {
        if(context)
        {
            var result = context.json.data;

            var length = context.length;

            var graphId = $("#"+context.params);

            if(result != null && result != undefined)
            {
                var data =
                    [
                        {"category" : "NA", "value": parseFloat(result.NA_percentage).toFixed(2),"title":result.NA},
                        {"category" : "Success", "value": parseFloat(result.success_percentage).toFixed(2),"title":result.success},
                        {"category" : "Reverse Lookup Failed", "value": parseFloat(result.reverseFailed_percentage).toFixed(2),"title":result.reverseFailed},
                        {"category" : "Forward Lookup Failed", "value": parseFloat(result.forwardFailed_percentage).toFixed(2),"title":result.forwardFailed},
                        {"category" : "Forward Lookup IP Mismatch", "value": parseFloat(result.forwardMismatch_percentage).toFixed(2),"title":result.forwardMismatch}
                    ];

                if(data)
                {
                    var chartHeight = 3.5 * length;

                    var chartWidth = 3.5 * length;

                    graphId.css('height',chartHeight);

                    graphId.css('width',chartWidth);
                }

                var legendItemWidth = 100;
                var legendItemHeight = 20;
                var legendItemMargin = 5;


                graphId.kendoChart({
                    dataSource: {
                        data: data
                    },
                    chartArea: context.chartArea,
                    series: [{
                        startAngle: context.startAngle,
                        padding: context.Padding,
                        type: "pie",
                        field: "value",
                        categoryField: "category"
                    }],
                    title: {
                        text: context.Title
                    },
                    legend: {
                        position: 'right',
                        item: {
                            visual: drawLegendItem
                        }
                    },
                    seriesColors: ["#008000", "#FFBB44", "#11A0F8", "#FF4444", "#A633FF"],
                    resizable: true,
                    tooltip: {
                        visible: true,
                        format: "{0}%"
                    }
                });

                function drawLegendItem(e) {

                    var value = e.series.value || e.series.data[e.pointIndex].value || '0';

                    var category = e.series.category || e.series.data[e.pointIndex].category || 'Unknown';

                    var ip = e.series.title || e.series.data[e.pointIndex].title || "0"; //This is undefined in Pie charts

                    var seriesName = value + "% ";

                    var legendItem = new kendo.geometry.Rect([0, 0],[legendItemWidth,legendItemHeight]);

                    var color = e.options.markers.background;

                    var boxHeight = legendItemHeight - legendItemMargin;

                    var legendItemLayout = new kendo.drawing.Layout(legendItem, {
                        alignContent: 'center'
                    });

                    var legendItemBox = new kendo.drawing.Path({
                        fill: {
                            color: color
                        },
                        stroke: {
                            color: color
                        },
                        cursor:'pointer'

                    }).moveTo(legendItemMargin, legendItemMargin)
                        .lineTo(legendItemMargin,boxHeight)
                        .lineTo(boxHeight,boxHeight)
                        .lineTo(boxHeight,legendItemMargin)
                        .close();

                    var position = new kendo.geometry.Point(legendItemHeight, (legendItemHeight / 2) - 20);

                    var ipPosition = new kendo.geometry.Point(legendItemHeight + 75, (legendItemHeight / 2) - 17);

                    var categoryPosition = new kendo.geometry.Point(legendItemHeight, (legendItemHeight / 2));

                    var spacingPosition = new kendo.geometry.Point(legendItemHeight, (legendItemHeight / 2) + 10);

                    var legendItemText = new kendo.drawing.Text(seriesName, position, {
                        fill: {
                            color: 'rgb(64,64,64)'
                        },
                        font:'18px Arial,Helvetica,sans-serif',
                        cursor:'pointer'
                    });

                    var ipItemText = new kendo.drawing.Text(ip, ipPosition, {
                        fill: {
                            color: 'grey'
                        },
                        cursor:'pointer'
                    });

                    var categoryItemText = new kendo.drawing.Text(category, categoryPosition, {
                        fill: {
                            color: 'grey'
                        },
                        cursor:'pointer'
                    });

                    var spacing = new kendo.drawing.Text(" ", spacingPosition, {
                        fill: {
                            color: 'grey'
                        }
                    });

                    legendItemLayout.append(legendItemBox, legendItemText, ipItemText, categoryItemText, spacing);

                    return legendItemLayout;
                }
            }
            else
            {
                graphId.html(appConstant.NoDataSpan)
            }
            flux.bindEvent({element:'dnsStatusExport'}, homeManager.openDnsStatusActionPopup);
        }
    },

    // -------------------------------------------------------------------------Load DHCP Utilization grid-------------------------------------------------------------------------------------------------//

    renderDhcpUtilization : function ()
    {
        var gridId = $('#'+homeManager.DhcpUtilization);

        var callbackContexts = {
            Read: function (options)
            {
                appManager.executeGETRequest({url: '/dhcpSubnet/',container:options,params:homeManager.DhcpUtilization,callback:widget.renderGridContext});
            },
            EventId: homeManager.DhcpUtilization,
            PageSize: 5,
            pageable: {
                refresh: true,
                pageSizes: false,
                buttonCount: 10
            },
            resizable:false,
            schema: {
                model: {
                    id: "id",
                    fields: {
                        subnetAddress: { type: "string"},
                        usedIp: { type: "string" },
                        type: {type:"string"},
                        totalIp: { type: "string"},
                        availableIp: { type: "string"}
                    }
                }
            },
            Fields: [
                {field:"id",hidden:true},
                {field: "subnetAddress",width:'35%', template : "<i class='ipam network-icon'></i> <a data-uid='#: id #' data-link='subnetAddress' data-name='#: subnetName #' title='#: subnetName #'>#: subnetName #</a>", title: "Subnet"},
                {field: "usedIpPercentage",width:'22%', title: "% in Space Used",template:'#if(severity == 1){#<div class="slim-chart-main"><div class="slim-chart-inner"><div class="critical-slim" style="width:#:usedIpPercentage#%"></div></div>#}else if(severity == 2){#</div><div class="slim-chart-main"><div class="slim-chart-inner"><div class="warning-slim" style="width:#:usedIpPercentage#%"></div></div>#}else{#</div><div class="slim-chart-main"><div class="slim-chart-inner"><div class="normal-slim" style="width:#:usedIpPercentage#%"></div></div>#}##= kendo.toString(usedIpPercentage,"n2")#%</div>'},
                {field: "type", title: "DHCP Type",width:'15%'},
                {field: "usedIp", title: "Used IP",width:'13%'},
                {field: "availableIp", title: "Available IP",width:'16%'}]
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

        appManager.initCustomScrollbar({container:$('#dhcpScopeUtilization').find('.k-grid-content')});

        flux.bindEvent({element:'dhcpAction #dhcpUtilizationExport'}, homeManager.openDhcpActionPopup);

        flux.bindEvent({element: 'dhcpScopeUtilization', selector:'a[data-link=subnetAddress]',breadCrumbMenu:"home"},subnetSummary.renderSubnetPage);
    },

    // -----------------------------------------------------------------------Load conflict ip grid---------------------------------------------------------------------------------------------------//

    renderConflictedIP : function ()
    {
        var gridId = $('#'+homeManager.ConflictIP);

        var callbackContexts = {
            Read: function (options)
            {
                appManager.executeGETRequest({url: '/conflictSubnetIp/',container:options,callback:widget.renderGridContext,params:homeManager.ConflictIP});
            },
            EventId: homeManager.ConflictIP,
            PageSize: 20,
            pageable: {
                refresh: true,
                pageSizes: false,
                buttonCount: 10
            },
            resizable:false,
            schema: {
                model: {
                    id: "id",
                    fields: {
                        ipAddress: { type: "string"},
                        lastAliveTime: { type: "string"},
                        previousMacAddress: { type: "string" },
                        conflictMac: {type:"string"},
                        subnetName: { type: "string"},
                        categoryName: { type: "string" }
                    }
                }
            },
            Fields: [
                {field:"id",hidden:true},
                { field: "ipAddress", title: "IP Address",template:"<a data-uid='#: subnetId.id #' data-id='#: id#' data-link='ipAddress' data-value='#: ipAddress #' data-name='#: subnetId.subnetName #' title='#: subnetId.subnetName #'>#: ipAddress #</a>"},
                { field: "subnetName", title: "Subnet",template:"<a data-uid='#: subnetId.id #' data-link='subnetName'  data-name='#: subnetId.subnetName #'  title='#: subnetId.subnetName #'>#: subnetId.subnetName #</a>"},
                { field: "categoryName", title: "Subnet Category",template:"<span title='#:subnetId.traceOrgCategory.categoryName#'>#:subnetId.traceOrgCategory.categoryName#</span>"},
                { field: "macAddress", title: "Assigned MAC",template:"<div title='#=macAddress#'>#=macAddress#</div>"},
                { field: "conflictMac", title: "Conflicting MAC",template:"<div title='#=conflictMac#'>#=conflictMac#</div>"},
                { field: "lastAliveTime", title: "Conflict Time",width:'20%',template:"<div title='#=lastAliveTime#'>#=lastAliveTime#</div>"}]
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

        appManager.initCustomScrollbar({container:$('#conflictedIP').find('.k-grid-content')});

        flux.bindEvent({element:'conflictIpAction #conflictedIpExport'}, homeManager.openConflictActionPopup);

        flux.bindEvent({element: 'conflictedIP', selector:'a[data-link=subnetName]',breadCrumbMenu:"home"},subnetSummary.renderSubnetPage);

        flux.bindEvent({element: 'conflictedIP', selector: 'a[data-link=ipAddress]',breadCrumbMenu:"home"},ipAddressSummary.loadIPAddressSummary);
    },

    renderRecentDiscovered : function ()
    {
        var gridId = $('#'+homeManager.RecentDiscovery);

        var callbackContexts =
        {
            Read: function (options)
            {
                appManager.executeGETRequest({url: '/recentDiscovered/',container:options,callback:widget.renderGridContext, params: homeManager.RecentDiscovery});
            },
            EventId: homeManager.RecentDiscovery,
            PageSize: 20,
            pageable: {
                refresh: true,
                pageSizes: false,
                buttonCount: 10
            },
            resizable:false,
            schema: {
                model: {
                    id: "id",
                    fields: {
                        ipAddress: { type: "string"},
                        macAddress: { type: "string" },
                        discoveredAt: { type: "string" }
                    }
                }
            },
            Fields: [
                { field:"id",hidden:true},
                { field: "ipAddress", title: "IP Address",template:"<div title='#=ipAddress#'>#=ipAddress#</div>"},
                { field: "macAddress", title: "MAC Address",template:"<div title='#=macAddress#'>#=macAddress#</div>"},
                { field: "discoveredAt", title: "Discovered At",width:'20%',template:"<div title='#=discoveredAt#'>#=discoveredAt#</div>"}]
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

        appManager.initCustomScrollbar({container:$('#conflictedIP').find('.k-grid-content')});

        flux.bindEvent({element:'recentlyDiscoveredExport'}, homeManager.openRecentlyDiscoveredActionPopup);
    },


    // ------------------------------------------------------------------Render subnet,dhcp & conflict ip grid data--------------------------------------------------------------------------------------------------------//

    renderGridContext : function (context)
    {
        var result = context.json.data;

        if(result != null && context.json.success == true)
        {
            context.container.success(result);
        }
        else
        {
            context.container.success("");

            $("#"+context.params).find('.k-grid-content').html(appConstant.NoDataSpan);
        }
    },

    // -------------------------------------------------------------------Render 12 month summary bar chart-------------------------------------------------------------------------------------------------------//

    loadIpSummaryGraph :function (context)
    {
        var graphId = $('#'+context.params);

        if(context.json.data != null && context.json.success == true)
        {
            graphId.kendoChart({
                dataSource: {
                    data: context.json.data
                },
                legend: {
                    visible: false
                },
                seriesDefaults: {
                    type: "column",
                    labels: {
                        visible: false,
                        background: "transparent"
                    },
                    gap:20
                },
                series: [{
                    field: "count",
                    colorField: "color"
                }],
                valueAxis: {
                    majorGridLines: {
                        visible: false
                    },
                    visible: false
                },
                categoryAxis: {
                    majorGridLines: {
                        visible: false
                    },
                    line: {
                        visible: false
                    }
                },
                chartArea: {
                    height: 60,
                    width:200
                },
                tooltip: {
                    visible: true
                },
                autoBind : true
            });
        }
        else
        {
            graphId.html('<div class="no-data-available">No Data Available</div>');
        }
    },

    // ----------------------------------------------------------------------Render vendor summary bar chart----------------------------------------------------------------------------------------------------//

    renderVendorwiseIpSummary :function (context)
    {
        if(context)
        {
            var vendorId = $("#"+context.params);

            var result = context.json.data;

            var chartHeight;

            if(result != null && result != undefined)
            {
                if(result.length <=2)
                {
                    chartHeight = result.length * 50;
                }
                else if(result.length >2)
                {
                    chartHeight = result.length * 30;
                }

                vendorId.css("height",chartHeight);

                vendorId.kendoChart({
                    dataSource:{
                        data:result
                    },
                    legend: {
                        visible: false
                    },
                    width : 20,
                    seriesDefaults: {
                        type: "bar",
                        padding: -50
                    },
                    seriesColors : ['#0487c4'],
                    series: [{field:'VendorCount',categoryField:'VendorName'}],
                    valueAxis: {
                        line: {
                            visible: false
                        },
                        minorGridLines: {
                            visible: false
                        },
                        majorGridLines: {
                            visible: true,
                            step:3
                        },
                        labels: {
                            visible:true,
                            step:3
                        }
                    },
                    categoryAxis: {
                        labels: {
                            rotation: "auto",
                            template: "#= widget.shortLabels(value)#"
                        },
                        categories: "VendorName",
                        majorGridLines: {
                            visible: false
                        }
                    },
                    autoBind : true,
                    tooltip: {
                        visible: true
                    }
                });
            }
            else
            {
                vendorId.html(appConstant.NoDataSpan);
            }
            flux.bindEvent({element:'vendorAction #vendorWiseIPExport'}, homeManager.openVendorChartActionPopup);
        }
    },

    // ----------------------------------------------------------------Ellipsis in vendor chart after 21 characters----------------------------------------------------------------------------------------------------------//

    shortLabels : function (value)
    {
        if (value.length > 20) {
            value = value.substring(0, 21);
            return value + "...";
        }
        return value
    }
};