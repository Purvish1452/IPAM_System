/**
 * Created by hardik on 5/7/18.
 */

var eventLog =
{
    // ----------------------------------------------------------------------Init event log dashboard, render timeline drop-down & grid data----------------------------------------------------------------------------------------------------//

    init : function ()
    {
        var gridId = $("#eventLogTable");

        flux.getKendoDropDownList({dropDownId:$("#eventTimeLine"),dataSource:[{ text: "Today", value: "0" },{ text: "Last 7 Days", value: "7" },{ text: "Last 30 Days", value: "30" }],dataTextField: "text",dataValueField: "value",value: "0"});

        flux.bindKendoDropDownListChangeEvent({dropDownId:$("#eventTimeLine")},eventLog.onChangeTimeLine);

        alerts.bindExportButtonClickEvent({element:'eventExportPdf', gridId:gridId, title:'Event Notifications', export:'PDF', timeline:$("#eventTimeLine")}, eventLog.onExportButtonClick);

        alerts.bindExportButtonClickEvent({element:'eventExportCsv', gridId:gridId, title:'Event Notifications', export:'CSV', timeline:$("#eventTimeLine")}, eventLog.onExportButtonClick);

        eventLog.onChangeTimeLine();

        navigationManager.stickyScroll();
    },

    // ----------------------------------------------------------------------Change timeline drop-down event----------------------------------------------------------------------------------------------------//

    onChangeTimeLine: function ()
    {
        var param = {};

        param['exportTimeline'] = $("#eventTimeLine").val();

        var gridId = $("#eventLogTable");

        loaderUtil.showCentralModalLoader();

        var callbackContexts =
        {
            container : gridId,
            url : '/event/',
            params : param,
            pageSize : 20,
            sort : { field: "generatedTime", dir: "desc" },
            callback : eventLog.renderEventLogGridData,
            columns: [
                {
                    field: "generatedTime",
                    title: "Generated Time",
                    template: "<span>#: appManager.formatDate(generatedTime) #</span>",
                    width:"20%"
                },
                {
                    field: "eventLog",
                    title: "Description",
                    template: "<span title='#: eventLog #'>#: eventLog #</span>",
                    width:"50%"
                },
                {
                    field: "ipAddress",
                    title: "IP Address",
                    template: "#if(ipAddress==null){#<span></span>#}else{#<span title='#: ipAddress #'>#: ipAddress #</span>#}#",
                    width:"15%"
                },
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
        if(context && context.json && context.json.data != null && context.json.success == true)
        {
            var result = context.json.data;

            if (result && Array.isArray(result.data)) {
                context.container.success(result.data);
            } else if (Array.isArray(result)) {
                context.container.success(result);
            } else {
                context.container.success([]);
            }
        }
        else
        {
            if (context && context.container && typeof context.container.success === 'function') {
                context.container.success([]);
            }
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
    }
};