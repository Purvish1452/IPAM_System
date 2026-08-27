/**
 * Created by hardik on 6/7/18.
 */
var dhcpServerStatistics=
    {
        DHCPSummaryPage : 'dhcpSummaryPage', DHCPStatistics:'dhcpStatistics',

        popupContent : '',


        // --------------------------------------------------------------Dhcp server statistics init------------------------------------------------------------------------------------------------------------//

        init : function (event,serverId,serverAddress,breadCrumbNavigation)
        {
            var menuName;

            var menuId;

            var breadCrumbMenus;

            if(event)
            {
                menuName = $(event.currentTarget).data('value');

                menuId = $(event.currentTarget).data("uid");

                breadCrumbMenus = event.data.breadCrumbMenu;
            }
            if(serverId && serverAddress)
            {
                menuName = serverAddress;

                menuId = serverId;

                breadCrumbMenus = breadCrumbNavigation;
            }

            if(menuName && menuId)
            {
                appManager.executeGETRequest({url: '/validatePermission/',callback:admin.getUserRole});

                $("#header_panel").html('<div class="title-inner-box">Settings</div><div class="breadcrumb-panel"><ul><li><a href="#" data-page="'+breadCrumbMenus+'" data-link=breadCrumbNavigation >'+breadCrumbMenus+'</a></li><li>'+menuName+'</li></ul></div>');

                $("#container-panel").html('<div id="leftPanel" class="left-panel stickyScrollLeft"> <a href="javascript:void(0)" id="homeLeftArrow" class="leftArrow" title="Settings"> <i class="icon-globe icons"></i> <i class="fa fa-arrow-left"></i> </a><div class="left-inner-box"><div class="title-part-panel"> <span class="h6Title"> <i class="icon-settings icons"></i> Settings </span></div><div class="menu-left-setting"><ul class=""><li data-panel="dhcpManagement"> <a data-value="dhcpManagement" id="dhcpOption">DHCP Management</a></li><li data-panel="userManagement"> <a data-value="userManagement" id="userOption">User Management</a></li><li data-panel="mailServerConfiguration"> <a data-value="mailServerConfiguration" id="mailOption">Mail Server Configuration</a></li><li data-panel="reBranding"> <a data-value="reBranding" id="reBrandOption">Rebranding</a></li><li data-panel="databaseMaintenance"> <a data-value="databaseMaintenance" id="databaseOption">Database Maintenance</a></li><li data-panel="configureAlert"> <a data-value="configureAlert" id="configureAlertOption">Configure Alert</a></li><li data-panel="discovery"><a data-value="discovery" id="discoveryOption"> Discovery </a>  </li><li data-panel="globalSettings"> <a data-value="globalSettings" id="globalOption">Global Settings</a></li></ul></div></div></div><div id="settingMenuGrid" class="content-panel"></div>');

                $("#settingMenuGrid").html('<div class="container-fluid"><div class="content-section-panel"><div class="row"><div class="col-sm-12 col-xs-12 col-md-12 col-lg-12 openColFullCollom"><div class="datatable-grid-panel"><div class="widget-main-box"><div class="widget-header-box" id="dhcpServerAction"><div class="widget-header-title"> <i class="ipam dashboard-icon"></i> DHCP Server Summary |<div class="sub-title" id="dhcpServer"></div></div><div class="widget-header-action"> <i class="icon-options-vertical icons" id="dhcpServerActions" title="Actions"></i></div></div><div class="widget-content-box padding-20" ><div class="row"><div class="col-sm-12 col-xs-12 col-md-12 col-lg-7" id="dhcpStatistics"></div><div class="col-sm-12 col-xs-12 col-md-12 col-lg-5" ><div class="ip-availability-panel" id="dhcpChartSummaryGraph" style="left: -80px;top:-60px;"></div></div></div></div></div></div></div></div></div></div>');

                navigationManager.addHistory("navigation=dhcpServerSummary~~serverId="+menuId+"~~serverAddress="+menuName+"~~breadCrumbNavigation="+breadCrumbMenus);

                loaderUtil.showModalLoader();

                loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

                admin.setActiveMenu("dhcpManagement");

                topManager.setActiveMenu("settings");

                appManager.toggleContentPanel();

                flux.bindKendoButtonClickEvent({element: 'leftPanel li a'}, admin.onAdminMenuClick);

                flux.bindKendoButtonClickEvent({element: 'homeLeftArrow'},leftPanel.onLeftArrowClick);

                $("#dhcpServer").html(menuName);

                $("#dhcpStatistics").html('<ul class="subnet-summary-panel"><li> <label>Total Scopes</label><p name="addressScopes"></p></li><li> <label>Type</label><p name="type"></p></li><li> <label>Declines</label><p name="declines"></p></li><li> <label>Offers</label><p name="offers"></p></li><li> <label>Requests</label><p name="requests"></p></li><li> <label>Discovers</label><p name="discovers"></p></li><li> <label>Releases</label><p name="releases"></p></li><li> <label>Ack</label><p name="acks"></p></li><li> <label>Naks</label><p name="naks"></p></li></ul>');

                appManager.executeGETRequest({url:'/dhcpUtilization/'+menuId,callback:dhcpServerStatistics.renderDHCPStatisticsContext,StartAngle:-45,Padding:20,eventId:menuId,eventName:menuName});

                flux.bindEvent({element:'dhcpServerAction #dhcpServerActions',widgetId:'scanDhcpServer'}, dhcpServerStatistics.openDhcpServerPopupAction);
            }
        },

        // ----------------------------------------------------------------------Render DHCP server statistics context----------------------------------------------------------------------------------------------------//

        renderDHCPStatisticsContext : function (context)
        {
            var id = $("#"+dhcpServerStatistics.DHCPStatistics);

            var result = context.json.data;

            var dhcpServerContext = id.find('ul');

            if(result != null && result != undefined)
            {
                if(dhcpServerContext)
                {
                    $.each( result, function(key, value){

                        var textValue = value;

                        if(textValue == null || textValue.length == 0 || textValue == "")
                        {
                            textValue = "N/A";
                        }

                        if(key == "dhcpCredentialDetailId")
                        {
                            if(result.dhcpCredentialDetailId.type)
                            {
                                key = "type";

                                textValue = value.type;
                            }
                        }
                        dhcpServerContext.find('[name=' + key + ']').text(textValue);
                    });
                }
                var graphId = $("#dhcpChartSummaryGraph");

                if(result != null && result != undefined) {
                    var data =
                        [
                            {
                                "category": "Ack",
                                "value": result.acks
                            },
                            {
                                "category": "Address Scopes",
                                "value": result.addressScopes
                            },
                            {
                                "category": "Declines",
                                "value": result.declines
                            },
                            {
                                "category": "Discovers",
                                "value": result.discovers
                            },
                            {
                                "category": "Naks",
                                "value": result.naks
                            },
                            {
                                "category": "Offers",
                                "value": result.offers
                            },
                            {
                                "category": "Release",
                                "value": result.releases
                            },
                            {
                                "category": "Requests",
                                "value": result.requests
                            }
                        ];

                    if (data) {
                        var chartHeight = data.length * 50;

                        var chartWidth = data.length * 50;

                        graphId.css('height', chartHeight);

                        graphId.css('width', chartWidth);
                    }

                    var legendItemWidth = 100;
                    var legendItemHeight = 20;
                    var legendItemMargin = 5;


                    graphId.kendoChart({
                        dataSource: {
                            data: data
                        },
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
                        seriesColors: ["#004d80", "#006bb3", "#008ae6","#1aa3ff","#4db8ff","#80ccff","#b3e0ff","#e6f5ff"],
                        resizable: true,
                        tooltip: {
                            visible: true,
                            format: "{0}"
                        }
                    });

                    function drawLegendItem(e) {

                        var value = e.series.value || e.series.data[e.pointIndex].value || '0';

                        var category = e.series.category || e.series.data[e.pointIndex].category || 'Unknown';

                        var seriesName = value;

                        var legendItem = new kendo.geometry.Rect([0, 0], [legendItemWidth, legendItemHeight]);

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
                            cursor: 'pointer'

                        }).moveTo(legendItemMargin, legendItemMargin)
                            .lineTo(legendItemMargin, boxHeight)
                            .lineTo(boxHeight, boxHeight)
                            .lineTo(boxHeight, legendItemMargin)
                            .close();

                        var position = new kendo.geometry.Point(legendItemHeight, (legendItemHeight / 2) - 20);

                        var ipPosition = new kendo.geometry.Point(legendItemHeight + 75, (legendItemHeight / 2) - 17);

                        var categoryPosition = new kendo.geometry.Point(legendItemHeight, (legendItemHeight / 2));

                        var spacingPosition = new kendo.geometry.Point(legendItemHeight, (legendItemHeight / 2) + 10);

                        var legendItemText = new kendo.drawing.Text(seriesName, position, {
                            fill: {
                                color: 'rgb(64,64,64)'
                            },
                            font: '18px Arial,Helvetica,sans-serif',
                            cursor: 'pointer'
                        });

                        var categoryItemText = new kendo.drawing.Text(category, categoryPosition, {
                            fill: {
                                color: 'grey'
                            },
                            cursor: 'pointer'
                        });

                        var spacing = new kendo.drawing.Text(" ", spacingPosition, {
                            fill: {
                                color: 'grey'
                            }
                        });

                        legendItemLayout.append(legendItemBox, legendItemText, categoryItemText, spacing);

                        return legendItemLayout;
                    }
                }
            }

            loaderUtil.hideModalLoader();

            loaderUtil.hideCentralModalLoader();
        },

        // --------------------------------------------------------------------------Scan DHCP Server------------------------------------------------------------------------------------------------//

        onScanServerBtnClick : function (event)
        {
            if(event)
            {
                event.preventDefault();

                if(appManager.validatePermission() == true)
                {
                    dhcpServerStatistics.popupContent.close();

                    var scanId = navigationManager.getUrlParameter("serverId");

                    var scopeAddress = navigationManager.getUrlParameter("serverAddress");

                    flux.bindKendoModalEvent({container: 'deleteModal',uniqueId :'scan',title:'Do you really want to Start Scan for '+scopeAddress+' ?', scopeAddress : scopeAddress, params:scanId, closeCallback: flux.closeKendoDeleteModal, callback:dhcpServerStatistics.onScanConfirmationClick});
                }
            }
        },

        onScanConfirmationClick : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var context = event.data.context;

                var subnetId =  context.params;

                var container = context.container;

                var scopeAddress = context.scopeAddress;

                $("#"+container).data('kendoWindow').close();

                appManager.executeGETRequest({url:'/scanDhcp/'+subnetId,scanId:subnetId,scopeAddress:scopeAddress, callback:dhcpServerStatistics.afterDHCPServerScanned});
            }
        },

        afterDHCPServerScanned : function (callbackContexts)
        {
            if(callbackContexts && callbackContexts.json.success == true)
            {
                clearInterval(subnetSummary.checkScanStatus);

                subnetSummary.subnetScopeAddress = callbackContexts.scopeAddress;

                subnetSummary.setRunningDiscoveryBlinkHTML();

                subnetSummary.checkScanStatus = setInterval(function()
                {
                    appManager.executeGETRequest({url:'/statusScanSubnet/',intervalFunctionCall:true,callback:subnetSummary.afterStatusChecked});

                }, 2000);

                notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"info"});
            }
            else if(callbackContexts)
            {
                notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"error"});
            }
        },

        // ------------------------------------------------------------------------Dhcp server popup--------------------------------------------------------------------------------------------------//

        openDhcpServerPopupAction : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var widgetId = $('#'+event.data.widgetId);

                var target = event.currentTarget;

                dhcpServerStatistics.popupContent.setOptions({
                    anchor: $(target),
                    position: "top center"
                });

                var id = navigationManager.getUrlParameter("serverId");

                var scope = navigationManager.getUrlParameter("serverAddress");

                widgetId.attr('data-uid',id);

                widgetId.attr('data-name',scope);

                dhcpServerStatistics.popupContent.open();

                dhcpServerStatistics.popupContent.position();

            }
        },

        // ----------------------------------------------------------------------- Dhcp server Export Chart ---------------------------------------------------------------------------------//

        onDhcpServerExportBtnClick : function (event)
        {
            if(event)
            {
                event.preventDefault();

                dhcpServerStatistics.popupContent.close();

                var context = event.data;

                var eventId = context.dataValue;

                var title = context.title;

                var exportType = context.exportType;

                var chart = $('#'+eventId).getKendoChart();

                if(chart != null && chart != undefined)
                {
                    switch(exportType)
                    {
                        case "png":
                            chart.exportImage().done(function(data) {
                                kendo.saveAs({
                                    dataURI: data,
                                    fileName: title+".png"
                                });
                            });
                            break;
                        case "svg":
                            chart.exportSVG().done(function(data) {
                                kendo.saveAs({
                                    dataURI: data,
                                    fileName: title+".svg"
                                });
                            });
                            break;
                        case "pdf":
                            chart.exportPDF().done(function(data) {
                                kendo.saveAs({
                                    dataURI: data,
                                    fileName: title+".pdf"
                                });
                            });
                            break;
                        default:
                            break;
                    }
                }
                else
                {
                    notification.showNotification({notificationTitle:"No Data Available",notificationType:"info"});
                }
            }
        },

        // ------------------------------------------------------------------------Navigation--------------------------------------------------------------------------------------------------//

        renderDHCPStatisticsFromURL : function (serverId,serverAddress,breadCrumbNavigation)
        {
            dhcpServerStatistics.init(null,serverId,serverAddress,breadCrumbNavigation);
        }
    };