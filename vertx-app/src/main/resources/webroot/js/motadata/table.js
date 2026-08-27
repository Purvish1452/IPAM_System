var table =
{
    // --------------------------------------------------------------------------Render user & dhcp management grid------------------------------------------------------------------------------------------------//
    renderTable : function (callbackContexts)
    {
        if(callbackContexts)
        {
            var gridId = callbackContexts['id'];

            if (callbackContexts)
            {
                gridId.kendoGrid({
                    dataSource: callbackContexts['dataSource'],
                    scrollable: true,
                    persistSelection: false,
                    sortable: true,
                    toolbar: callbackContexts['toolbar'],
                    resizable: true,
                    columns:callbackContexts['columns'],
                    rowTemplate : callbackContexts['rowTemplate'],
                    PageSize: callbackContexts.pageSize,
                    pageable: {
                        refresh: true,
                        pageSizes: [10,20,50,100],
                        buttonCount: 5
                    },
                    autoBind : true,
                    serverFiltering: true,
                    serverPaging: true,
                    reorderable: true,
                    autoSync: true,
                    editable: {
                        mode:"popup",
                        confirmation: true
                    },
                    edit: callbackContexts['edit']
                });
            }
        }
    },

    // ----------------------------------------------------------------------Open user & dhcp modal----------------------------------------------------------------------------------------------------//

    kendoWindowSetOption : function (context)
    {
        if(context)
        {
            var kendoWindow = context.kendoWindow;

            kendoWindow.setOptions({
                modal: true,
                width:context.width,
                height:context.height,
                title: context.title,
                pinned: false,
                position: {
                    top: 100,
                    left: 100
                },
                resizable: false,
                scrollable:false,
                draggable:false
            });
            kendoWindow.center();
        }
    },

    // ---------------------------------------------------------------------Close user & dhcp delete modal-----------------------------------------------------------------------------------------------------//

    closeDeleteWindow : function (event)
    {
        if(event)
        {
            event.preventDefault();

            var modal =  event.data.kendoModal;

            modal.close();
        }
    },

    searchFilter : function (id, filter)
    {
        filter.on('input', function (e)
        {
            var grid = id.data('kendoGrid');

            var columns = grid.columns;

            var filter =
                {
                    logic: 'or',
                    filters: []
                };

            columns.forEach(function (x)
            {
                if (x.field && x.field !== 'id')
                {
                    var type = grid.dataSource.options.schema.model.fields[x.field].type;

                    if (type == 'string')
                    {
                        filter.filters.push({
                            field: x.field,
                            operator: 'contains',
                            value: e.target.value.trim()
                        })
                    }
                    else if (type == 'number')
                    {
                        if (formManager.isNumeric(e.target.value))
                        {
                            filter.filters.push({
                                field: x.field,
                                operator: 'eq',
                                value: e.target.value.trim()
                            });
                        }
                    }
                    else if (type == 'date')
                    {
                        var data = grid.dataSource.data();
                        for (var i=0;i<data.length ; i++)
                        {
                            var dateStr = kendo.format(x.format, data[i][x.field]);
                            if(dateStr.startsWith(e.target.value)){
                                filter.filters.push({
                                    field: x.field,
                                    operator:'eq',
                                    value: data[i][x.field].trim()
                                })
                            }
                        }
                    }
                    else if (type == 'boolean' && formManager.getBoolean(e.target.value) !== null)
                    {
                        var bool = formManager.getBoolean(e.target.value);
                        filter.filters.push({
                            field: x.field,
                            operator: 'eq',
                            value: bool
                        });
                    }
                }
            });
            grid.dataSource.filter(filter);
        });
    },
};