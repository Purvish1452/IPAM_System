/**
 * Created by hardik on 21/5/18.
 */
var widgetRenderManager =
{
    // -----------------------------------------------------------------------Render grid---------------------------------------------------------------------------------------------------//

    renderGridData : function(context)
    {
        if(context!=null && context!=undefined)
        {
            var gridId = $("#"+context.EventId);

            gridId.kendoGrid({
                editable: {
                    mode:"popup",
                    confirmation: true
                },
                dataSource: {
                    transport: {
                        read: context.Read
                    },
                    parameterMap: function(options, operation)
                    {
                        if (operation != "read" && options.models)
                        {
                            return {models: kendo.stringify(options.models)};
                        }
                    },
                    batch :true,
                    pageSize: context.PageSize
                },
                dataBound:function (e)
                {
                    if(e != null && e)
                    {
                        gridId.find(':checkbox').each(function () {
                            $(this).attr('checked', jQuery.inArray($(this).val()) > -1);
                        });

                        subnetSummary.showHideBtnOnCheckboxClick();
                    }
                },
                autoBind : true,
                toolbar : context.toolbar,
                sortable: true,
                resizable: context.resizable,
                persistSelection: true,
                pageable:context.pageable,
                reorderable: true,
                columns: context.Fields,
                edit: context['edit'],
                schema : context.schema,
                serverFiltering: true,
                serverPaging: true,
                serverSorting: true
            });
        }
    },

    renderGridDataWithPaging : function(context)
    {
        if(context!=null && context!=undefined)
        {
            var gridId = $("#"+context.EventId);

            gridId.kendoGrid({
                editable: {
                    mode:"popup",
                    confirmation: true
                },
                dataSource: {
                    transport: {
                        read: context.Read
                    },
                    parameterMap: function(options, operation)
                    {
                        if (operation != "read" && options.models)
                        {
                            return {models: kendo.stringify(options.models)};
                        }
                    },
                    batch :true,
                    pageSize: context.PageSize,
                    serverPaging: true,
                    schema: {
                        data: "data",   // Data field in the response
                        total: "total"  // Total count field in the response
                    },
                },
                dataBound:function (e)
                {
                    if(e != null && e)
                    {
                        gridId.find(':checkbox').each(function () {
                            $(this).attr('checked', jQuery.inArray($(this).val()) > -1);
                        });

                        subnetSummary.showHideBtnOnCheckboxClick();

                        if (e?.sender?.pager?.dataSource) {
                            e.sender.pager.dataSource._total = e.sender.pager.dataSource._pristineTotal;
                        }
                    }
                },
                autoBind : true,
                toolbar : context.toolbar,
                sortable: true,
                resizable: context.resizable,
                persistSelection: true,
                pageable:context.pageable,
                reorderable: true,
                columns: context.Fields,
                edit: context['edit'],
                schema : context.schema,
                serverFiltering: true,
                serverPaging: true,
                serverSorting: true
            });
        }
    },

    // ------------------------------------------------------------------Render pie chart--------------------------------------------------------------------------------------------------------//

    renderPieChart : function (context)
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
                        {"category" : "Available", "value": parseFloat(result.availableIpPercentage).toFixed(2),"title":result.availableIp},
                        {"category" : "Used", "value": parseFloat(result.usedIpPercentage).toFixed(2),"title":result.usedIp},
                        {"category" : "Transient", "value": parseFloat(result.transientIpPercentage).toFixed(2),"title":result.transientIp}
                    ];

                if(data)
                {
                    var chartHeight = data.length * length;

                    var chartWidth = data.length * length;

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
                    seriesColors: ["#008000", "#FFBB44", "#11A0F8"],
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
            flux.bindEvent({element:'ipAvailabilityAction #allIPAvailabilityExport'}, homeManager.openIPAvailabilityChartActionPopup);
        }
    },

    // ----------------------------------------------------------------------After subnet crud operations----------------------------------------------------------------------------------------------------//

    renderWidgetAfterSubnetOperation : function ()
    {
        if(navigationManager.getUrlParameter("navigation") == navigationManager.menu.Home)
        {
            leftPanel.initTreeView();

            rightPanel.renderEventDetails();

            homeManager.loadHomeScreenWidgets();
        }
        else
        {
            navigationManager.doNavigation();
        }
    },

    // -----------------------------------------------------------------Export dashboard/screenshot---------------------------------------------------------------------------------------------------------//

    onDashboardExportBtnClick : function (event)
    {
        if(event)
        {
            event.event.stopPropagation();

            var context = event.sender.options.prefix;

            var selector = context.eventId;

            var title = context.title;

            kendo.drawing.drawDOM($("#"+selector))
                .then(function(group) {
                    // Render the result as a PDF file
                    return kendo.drawing.exportPDF(group, {
                        paperSize: "auto",
                        margin: { left: "1cm", top: "1cm", right: "1cm", bottom: "1cm" }
                    });
                })
                .done(function(data) {
                    // Save the PDF file
                    kendo.saveAs({
                        dataURI: data,
                        fileName: title
                    });
                });

            homeManager.init();
        }
    }
};
