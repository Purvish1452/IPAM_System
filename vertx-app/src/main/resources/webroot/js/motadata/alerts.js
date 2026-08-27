var alerts = {

    AlertsPage: 'alertsPage', AlertsTable : 'alertsTable',

    init : function ()
    {
        loaderUtil.showModalLoader();

        loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

        navigationManager.addHistory("navigation=alerts");

        topManager.setActiveMenu('alerts');

        var root = $("#header_panel");

        root.empty();

        root.html('<div class="title-inner-box"> Alerts </div>');

        $("#container-panel").html('<div id="leftPanel" class="left-panel alertsPage"></div><div id="alertsPage" class="content-panel"></div><div id="right-panel" class="right-panel stickyScrollRight"></div>');

        appManager.togglePanel();

        appManager.renderHTML(alerts.AlertsPage, $("#alertsPage"),undefined);

        var reportDropDown = $("#alertFilter");

        var data = [{text: "Live", value: "live" },{text: "Clear", value: "clear" }];

        flux.getKendoDropDownList({dropDownId:reportDropDown,dataTextField: "text",dataValueField: "value",data:data});

        var param = {};

        reportDropDown.data('kendoDropDownList').destroy();

        reportDropDown.kendoDropDownList
        ({
            change:function (e)
            {
                e.preventDefault();

                param['alertFilter'] = this.value();

                alerts.renderAlertsGrid(param);
            }
        });

        reportDropDown.getKendoDropDownList().trigger('change');

        reportDropDown.data('kendoDropDownList').refresh();
    },

    // ----------------------------------------------------------------------Load Alerts grid----------------------------------------------------------------------------------------------------//

    renderAlertsGrid : function (param)
    {
        var gridId = $('#'+alerts.AlertsTable);

        var callbackContexts = {

            Read: function (options)
            {
                param.page = options.data.page;
                param.pageSize = options.data.pageSize;
                appManager.executeGETRequest({url: '/alerts/',container:options,callback:alerts.renderAlertsGridData,params:param});
            },
            EventId: alerts.AlertsTable,
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
                        alertType:{type:'string'},
                        message:{type:'string'},
                        subnet: {type: "string"},
                        timestamp: {type: "string"},
                    }
                }
            },
            Fields: [
                {field: "alertType", title: "Alert Type",width:"15%",template:'# if (alertType) { # <span title="#:alertType#">#: alertType # </span># } else { #<span></span># } #'},
                {field: "message", title: "Message",width:"53%",template:'# if (message) { # <span title="#:message#">#: message # </span># } else { #<span></span># } #'},
                {field: "subnet", title: "Subnet",width:"17%",template:'# if (subnet) { # <span title="#:subnet#">#: subnet # </span># } else { #<span></span># } #'},
                {
                    field: "timestamp",
                    template: '# if (timestamp) { # <span title="#:timestamp#">#: timestamp # </span># } else { #<span></span># } #',
                    title: "Times",
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

        widgetRenderManager.renderGridDataWithPaging(callbackContexts);

        formManager.searchFilter(gridId);
    },

    // ----------------------------------------------------------------------Render Alert grid----------------------------------------------------------------------------------------------------//

    renderAlertsGridData : function (context)
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

    // ---------------------------------------------------------------------------Navigation-----------------------------------------------------------------------------------------------//

    renderAlertsFromURL : function ()
    {
        alerts.init();
    }
};