/**
 * Created by nilesh on 27/6/18.
 */
var eventLog = {

    EventLogPage: 'eventLogPage', EventLogTable : 'eventLogTable',

    // ----------------------------------------------------------------------Init event logs with timeline dropdown----------------------------------------------------------------------------------------------------//

    init : function ()
    {
        loaderUtil.showModalLoader();

        loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

        navigationManager.addHistory("navigation=eventLog");

        topManager.setActiveMenu('eventLog');

        var root = $("#header_panel");

        root.empty();

        root.html('<div class="title-inner-box"> Event Logs</div>');

        $("#container-panel").html('<div id="leftPanel" class="left-panel eventLogPage"></div><div id="eventLogPage" class="content-panel"></div><div id="right-panel" class="right-panel stickyScrollRight"></div>');

        appManager.togglePanel();

        appManager.renderHTML(eventLog.EventLogPage, $("#eventLogPage"),undefined);

        var reportDropDown = $("#eventTimeLine");

        var data = [{text: "All", value: "10" },{text: "Today", value: "1" },{text: "Previous Day", value: "2" },{text: "This Week", value: "3" },{text: "This Month", value: "4" },{text: "This Quarter", value: "5" },{text: "Previous Quarter", value: "6" },
            {text: "Last Six Month", value: "7" },{text: "This Year", value: "8" },{text: "Previous Year", value: "9" },{text: "Previous Week", value: "11" },{text: "Previous Month", value: "12" }];

        flux.getKendoDropDownList({dropDownId:reportDropDown,dataTextField: "text",dataValueField: "value",data:data});

        var param = {};

        reportDropDown.data('kendoDropDownList').destroy();

        reportDropDown.kendoDropDownList
        ({
            change:function (e)
            {
                e.preventDefault();

                param['exportTimeline'] = this.value();

                eventLog.renderEventLogGrid(param);
            }
        });

        reportDropDown.getKendoDropDownList().trigger('change');

        reportDropDown.data('kendoDropDownList').refresh();

        flux.bindKendoButtonClickEvent({element:'exportEventPdf',export:1},eventLog.onExportButtonClick);

        flux.bindKendoButtonClickEvent({element:'exportEventCsv',export:2},eventLog.onExportButtonClick);
    },

    // ----------------------------------------------------------------------Load Eventlog grid on selected timeline----------------------------------------------------------------------------------------------------//

    renderEventLogGrid : function (param)
    {
        var gridId = $('#'+eventLog.EventLogTable);

        var callbackContexts = {

            Read: function (options)
            {
                appManager.executeGETRequest({url: '/event/',container:options,callback:eventLog.renderEventLogGridData,params:param});
            },
            EventId: eventLog.EventLogTable,
            PageSize: 20,
            pageable: {
                refresh: true,
                pageSizes: [10,20,50,100],
                buttonCount: 10
            },
            DataType: 'json',
            groupable: true,
            schema: {
                model: {
                    id: "id",
                    fields: {
                        eventType:{type:'string'},
                        eventContext:{type:'string'},
                        timestamp: {type: "string"},
                        userName: {type: "string"}
                    }
                }
            },
            Fields: [
                {field: "eventType", title: "Event Type",width:"15%",template:'# if (eventType) { # <span title="#:eventType#">#: eventType # </span># } else { #<span></span># } #'},
                {field: "eventContext", title: "Event Context",width:"53%",template:'# if (eventContext) { # <span title="#:eventContext#">#: eventContext # </span># } else { #<span></span># } #'},
                {field: "timestamp", title: "Time",width:"17%",template:'# if (timestamp) { # <span title="#:timestamp#">#: timestamp # </span># } else { #<span></span># } #'},
                {
                    field: "userName",
                    template: "#if(doneBy==null){#<span></span>#}else{#<span title='#: doneBy.userName #'>#: doneBy.userName #</span>#}#",
                    title: "Username",
                    width:"15%"
                }
            ],
            sortable: true,
            resizable:true
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

    // ----------------------------------------------------------------------Render Event log grid----------------------------------------------------------------------------------------------------//

    renderEventLogGridData : function (context)
    {
        if(context.json.data != null && context.json.success == true)
        {
            var result = context.json.data;

            context.container.success(result);
        }
        else
        {
            context.container.success("");

            $(".k-grid-content").html(appConstant.NoDataSpan);
        }
        loaderUtil.hideModalLoader();

        loaderUtil.hideCentralModalLoader();
    },

    // -------------------------------------------------------------------------Export eventlog with selected timeline-------------------------------------------------------------------------------------------------//

    onExportButtonClick : function (event)
    {
        if(event)
        {
            event.event.preventDefault();

            var context = event.sender.options.prefix;

            var exportType = context.export;

            var param = {};

            param['exportTimeline'] = $("#eventTimeLine").val();

            loaderUtil.showModalLoader();

            loaderUtil.showCentralModalLoader(appConstant.ExportMessage);

            if(exportType == 1)
            {
                appManager.executeGETRequest({url:'/exportEventPdf/',callback:eventLog.downloadPdf,params:param});
            }
            else if(exportType == 2)
            {
                appManager.executeGETRequest({url:'/exportEventCsv/',callback:eventLog.downloadCsv,params:param});
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

    // ---------------------------------------------------------------------------Navigation-----------------------------------------------------------------------------------------------//

    renderEventLogFromURL : function ()
    {
        eventLog.init();
    }
};