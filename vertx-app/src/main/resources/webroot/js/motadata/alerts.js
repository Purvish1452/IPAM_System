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

        var param = {};

        reportDropDown.kendoDropDownList({
            dataTextField: "text",
            dataValueField: "value",
            dataSource: data,
            value: "live",
            change: function (e)
            {
                if (e && e.preventDefault) e.preventDefault();
                param['alertFilter'] = this.value();
                alerts.renderAlertsGrid(param);
            }
        });

        param['alertFilter'] = reportDropDown.val() || "live";
        alerts.renderAlertsGrid(param);
    },

    // ----------------------------------------------------------------------Load Alerts grid----------------------------------------------------------------------------------------------------//

    renderAlertsGrid : function (param)
    {
        var gridId = $('#'+alerts.AlertsTable);

        var callbackContexts = {

            Read: function (options)
            {
                var requestParams = $.extend({}, param, {
                    page: options.data.page,
                    pageSize: options.data.pageSize
                });
                var searchVal = $('#searchFilter').val();
                if (searchVal && searchVal.trim().length > 0) {
                    requestParams.search = searchVal.trim();
                }

                appManager.executeGETRequest({
                    url: '/alerts/',
                    container: options,
                    callback: alerts.renderAlertsGridData,
                    params: requestParams
                });
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
                        timestamp: {type: "string"}
                    }
                }
            },
            Fields: [
                {
                    field: "alertType",
                    title: "Alert Type",
                    width: "15%",
                    template: "# var t = (typeof alertType !== 'undefined' && alertType) ? alertType.toUpperCase() : ''; " +
                              "if(t === 'CRITICAL'){ #<span class='label label-danger' style='font-weight:600;padding:3px 8px;border-radius:3px;'><i class='fa fa-exclamation-circle'></i> Critical</span># } " +
                              "else if(t === 'MAJOR'){ #<span class='label label-warning' style='font-weight:600;padding:3px 8px;border-radius:3px;background-color:rgb(230,126,34);'><i class='fa fa-warning'></i> Major</span># } " +
                              "else if(t === 'WARNING'){ #<span class='label label-warning' style='font-weight:600;padding:3px 8px;border-radius:3px;'><i class='fa fa-exclamation-triangle'></i> Warning</span># } " +
                              "else if(t === 'INFO' || t === 'INFORMATION'){ #<span class='label label-info' style='font-weight:600;padding:3px 8px;border-radius:3px;'><i class='fa fa-info-circle'></i> Info</span># } " +
                              "else if(t === 'CLEARED' || t === 'CLEAR'){ #<span class='label label-success' style='font-weight:600;padding:3px 8px;border-radius:3px;'><i class='fa fa-check-circle'></i> Cleared</span># } " +
                              "else { #<span title='#: alertType || \"\" #'>#: alertType || \"-\" #</span># } #"
                },
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
        try {
            if(context && context.json && context.json.success === true && context.json.data != null)
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
        } catch (err) {
            console.error("Error rendering alerts grid data:", err);
            if (context && context.container && typeof context.container.success === 'function') {
                context.container.success({ data: [], total: 0 });
            }
        } finally {
            loaderUtil.hideModalLoader();
            loaderUtil.hideCentralModalLoader();
        }
    },

    // ---------------------------------------------------------------------------Navigation-----------------------------------------------------------------------------------------------//

    renderAlertsFromURL : function ()
    {
        alerts.init();
    }
};