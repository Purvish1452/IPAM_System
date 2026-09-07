/**
 * Created by hardik on 5/7/18.
 */

var eventLog =
{
    EventLogPage: 'eventLogPage',
    EventLogTable: 'eventLogTable',

    // ----------------------------------------------------------------------Init event log dashboard, render timeline drop-down & grid data----------------------------------------------------------------------------------------------------//

    init : function ()
    {
        loaderUtil.showModalLoader();
        loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

        navigationManager.addHistory("navigation=eventLog");
        topManager.setActiveMenu('eventLog');

        var root = $("#header_panel");
        root.empty();
        root.html('<div class="title-inner-box"> Event Logs </div>');

        $("#container-panel").html('<div id="leftPanel" class="left-panel eventLogPage"></div><div id="eventLogPage" class="content-panel"></div><div id="right-panel" class="right-panel stickyScrollRight"></div>');

        appManager.togglePanel();

        appManager.renderHTML(htmlRender.page.EventLog, $("#eventLogPage"), undefined);

        var reportDropDown = $("#eventTimeLine");
        var data = [
            { text: "All", value: "-1" },
            { text: "Today", value: "0" },
            { text: "Last 7 Days", value: "7" },
            { text: "Last 30 Days", value: "30" }
        ];

        flux.getKendoDropDownList({ dropDownId: reportDropDown, dataTextField: "text", dataValueField: "value", data: data });

        var param = {};

        try {
            var dd = reportDropDown.data('kendoDropDownList');
            if (dd) {
                dd.destroy();
            }
        } catch (e) {}

        reportDropDown.kendoDropDownList({
            dataTextField: "text",
            dataValueField: "value",
            dataSource: data,
            value: "-1",
            change: function (e) {
                if (e && e.preventDefault) e.preventDefault();
                param['exportTimeline'] = this.value();
                eventLog.renderEventLogGrid(param);
            }
        });

        // Bind export button clicks
        flux.bindKendoButtonClickEvent({ element: 'exportEventPdf', export: 'PDF' }, eventLog.onExportButtonClick);
        flux.bindKendoButtonClickEvent({ element: 'exportEventCsv', export: 'CSV' }, eventLog.onExportButtonClick);
        flux.bindKendoButtonClickEvent({ element: 'eventExportPdf', export: 'PDF' }, eventLog.onExportButtonClick);
        flux.bindKendoButtonClickEvent({ element: 'eventExportCsv', export: 'CSV' }, eventLog.onExportButtonClick);

        param['exportTimeline'] = reportDropDown.val() || "-1";
        eventLog.renderEventLogGrid(param);
    },

    // ----------------------------------------------------------------------Change timeline drop-down event----------------------------------------------------------------------------------------------------//

    onChangeTimeLine: function ()
    {
        var param = {};
        param['exportTimeline'] = $("#eventTimeLine").val() || "-1";
        eventLog.renderEventLogGrid(param);
    },

    renderEventLogGrid : function (param)
    {
        var gridId = $("#" + eventLog.EventLogTable);

        loaderUtil.showCentralModalLoader();

        var callbackContexts =
        {
            EventId: eventLog.EventLogTable,
            Read: function (options)
            {
                var requestParams = $.extend({}, param, {
                    page: options.data.page,
                    pageSize: options.data.pageSize
                });
                appManager.executeGETRequest({
                    url: "/event/",
                    container: options,
                    callback: eventLog.renderEventLogGridData,
                    params: requestParams
                });
            },
            container: gridId,
            PageSize: 20,
            pageable: {
                refresh: true,
                pageSizes: [10, 20, 50, 100],
                buttonCount: 10
            },
            DataType: 'json',
            groupable: true,
            schema: {
                model: {
                    id: "id",
                    fields: {
                        generatedTime: { type: "number" },
                        eventLog: { type: "string" },
                        message: { type: "string" },
                        eventType: { type: "string" },
                        eventContext: { type: "string" },
                        userName: { type: "string" }
                    }
                }
            },
            sort: { field: "id", dir: "desc" },
            Fields: [
                {
                    field: "generatedTime",
                    title: "Generated Time",
                    template: "<span># if (typeof generatedTime !== 'undefined' && generatedTime) { # #: appManager.formatDate(generatedTime) # # } else if (typeof timestamp !== 'undefined' && timestamp) { # #: timestamp # # } else { # - # } #</span>",
                    width: "20%"
                },
                {
                    field: "eventType",
                    title: "Event Type",
                    template: "# if (typeof eventType !== 'undefined' && eventType) { # <span title='#: eventType #'>#: eventType #</span> # } else { # <span>-</span> # } #",
                    width: "15%"
                },
                {
                    field: "eventContext",
                    title: "Context",
                    template: "# if (typeof eventContext !== 'undefined' && eventContext) { # <span title='#: eventContext #'>#: eventContext #</span> # } else { # <span>-</span> # } #",
                    width: "15%"
                },
                {
                    field: "eventLog",
                    title: "Description",
                    template: "# if (typeof eventLog !== 'undefined' && eventLog) { # <span title='#: eventLog #'>#: eventLog #</span> # } else if (typeof message !== 'undefined' && message) { # <span title='#: message #'>#: message #</span> # } else { # <span>-</span> # } #",
                    width: "35%"
                },
                {
                    field: "userName",
                    template: "# if (typeof doneBy !== 'undefined' && doneBy != null && doneBy.userName) { # <span title='#: doneBy.userName #'>#: doneBy.userName #</span> # } else if (typeof userName !== 'undefined' && userName) { # <span title='#: userName #'>#: userName #</span> # } else { # <span>admin</span> # } #",
                    title: "Username",
                    width: "15%"
                }
            ],
            sortable: true,
            resizable: true
        };

        // Destroy old grid context
        try {
            var kGrid = gridId.data("kendoGrid");
            if (kGrid) {
                kGrid.destroy();
            }
            gridId.empty();
        }
        catch(err)
        {
        }

        widgetRenderManager.renderGridDataWithPaging(callbackContexts);

        formManager.searchFilter(gridId);
    },

    // ----------------------------------------------------------------------Render Event log grid----------------------------------------------------------------------------------------------------//

    renderEventLogGridData : function (context)
    {
        try {
            if(context && context.json && context.json.data != null && context.json.success === true)
            {
                var result = context.json.data;
                var totalCount = (context.json.total !== undefined && context.json.total !== null) ? context.json.total : (Array.isArray(result) ? result.length : 0);

                if (Array.isArray(result)) {
                    context.container.success({
                        data: result,
                        total: totalCount
                    });
                } else if (result && Array.isArray(result.data)) {
                    context.container.success(result);
                } else {
                    context.container.success({ data: [], total: 0 });
                }
            }
            else
            {
                if (context && context.container && typeof context.container.success === 'function') {
                    context.container.success({ data: [], total: 0 });
                }
                $(".k-grid-content").html(appConstant.NoDataSpan);
            }
        } finally {
            loaderUtil.hideModalLoader();
            loaderUtil.hideCentralModalLoader();
        }
    },

    // -------------------------------------------------------------------------Export eventlog with selected timeline-------------------------------------------------------------------------------------------------//

    onExportButtonClick : function (event)
    {
        if(event)
        {
            if (event.event && event.event.preventDefault) {
                event.event.preventDefault();
            }

            var exportType = "PDF";
            if (event.sender && event.sender.options && event.sender.options.prefix) {
                exportType = event.sender.options.prefix.export;
            } else if (event.data && event.data.export) {
                exportType = event.data.export;
            }

            var param = {};
            param['exportTimeline'] = $("#eventTimeLine").val() || "-1";

            if(exportType == 'PDF')
            {
                param['pdf'] = true;
            }
            else
            {
                param['csv'] = true;
            }

            var exportUrl = "/event/";
            window.location = exportUrl + "?" + $.param(param);
        }
    },

    // ---------------------------------------------------------------------------Navigation-----------------------------------------------------------------------------------------------//

    renderEventLogFromURL : function ()
    {
        eventLog.init();
    }
};