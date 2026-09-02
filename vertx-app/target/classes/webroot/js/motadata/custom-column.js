var customColumn =
    {
        popupContent : '',

        init : function ()
        {
            loaderUtil.showModalLoader();

            loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

            $("#settingMenuGrid").html('<div class="container-fluid"> <div class="content-section-panel"> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <div class="datatable-grid-panel"> <div class="widget-main-box"> <div class="widget-header-box"> <div class="widget-header-title">Custom Columns</div> </div> <div class="widget-content-box" id="customColumnTable" style="margin-bottom: 10px;"></div> <div class="note-text" style="padding: 10px; font-style: italic; color: #666;"> Note: The custom column is only applicable to subnet IPs. </div> </div> </div> </div> </div> <div class="common-box-grid-panel padding-t-20 padding-b-20"> <div class="fixed-height-body-popup-panel"> <div id="changePasswordWindow"></div> </div> </div> </div></div>');
            $('#deleteModal').empty();

            customColumn.renderCustomColumns();
        },

        renderUserManagementGrid : function (context)
        {
            if (context.json.data != null && context.json.success === true) {
                var data = context.json.data;
                context.container.success(data);
            } else {
                context.container.success([]);
                $(".k-grid-content").html(appConstant.NoDataSpan);
            }
            loaderUtil.hideCentralModalLoader();
        },

        addEntity : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var kendoEvent = event.sender.options.prefix.kendoModal;

                var form = $("#addCustomColumnForm");

                var param =formManager.serializeForm(form);

                loaderUtil.showModalLoader();

                appManager.executePOSTRequest({ url: '/customColumn/', container: kendoEvent, callback: customColumn.afterEntityAddedOrUpdated, params: param});
            }
        },

       afterEntityAddedOrUpdated : function (context)
        {
            if(context)
            {
                var gridId = $("#customColumnTable");

                var modal = context.container;

                if(context.json.success == true)
                {
                    modal.close();

                    flux.refreshKendoGrid({gridId : gridId});

                    notification.showNotification({notificationTitle:context.json.message,notificationType:"success"});
                }
                else
                {
                    notification.showNotification({notificationTitle:context.json.message,notificationType:"error"});
                }

                loaderUtil.hideModalLoader();
            }
        },

        afterCustomColumnDeleted : function (context)
        {
            if(context)
            {
                var gridId = context.gridId;

                if(context.json.success == true)
                {
                    notification.showNotification({ notificationTitle: context.json.message, notificationType: "success"});
                }
                else
                {
                    notification.showNotification({ notificationTitle: context.json.message, notificationType: "error"});
                }
                flux.refreshKendoGrid({gridId : gridId});
            }
        },

        deleteConfirmationPrompt : function (event)
        {
            if(event)
            {
                event.preventDefault();

                loaderUtil.showModalLoader();

                var modal =  event.data.kendoModal;

                var context =  event.data.data;

                var grid = $("#customColumnTable").data('kendoGrid');

                grid.dataSource.remove(context);

                grid.dataSource.sync();

                modal.close();

                loaderUtil.hideModalLoader();
            }
        },

        onCloseButtonClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var modal = event.sender.options.prefix.kendoModal;

                modal.close();

                var gridId = $("#customColumnTable");

                flux.refreshKendoGrid({gridId : gridId});

            }
        },
        renderCustomColumns : function ()
        {
            $('#deleteModal').empty();

            var customColumnTable = $("#customColumnTable");

            var callbackContext = [];

            callbackContext['id'] = customColumnTable;

            callbackContext['name'] = "User";

            callbackContext['dataSource'] = {
                pageSize: 10,
                transport: {
                    read: function (options) {
                        appManager.executeGETRequest({
                            url: '/customColumn/',
                            container: options,
                            callback: customColumn.renderUserManagementGrid
                        });
                    },
                    destroy: function (options) {
                        var id = options.data.models[0].id;
                        appManager.executeDELETERequest({
                            url: '/customColumn/' + id,
                            callback: customColumn.afterCustomColumnDeleted,
                            gridId: customColumnTable
                        });
                    },
                    parameterMap: function (options, operation) {
                        if (operation !== "read" && options.models) {
                            return { models: kendo.stringify(options.models) };
                        }
                    }
                },
                batch: true,
                schema: {
                    model: {
                        id: "id",
                        fields: {
                            columnName: { type: "string" },
                            columnAt: { type: "string" },
                            dataType: { type: "string" },
                            size: { type: "number" },
                            defaultValue: { type: "string" },
                            description: { type: "string" }
                        }
                    }
                }
            };

            callbackContext['toolbar'] = [
                { name: "create", text: "<span class='fa fa-plus'> Add Custom Column</span>" }
            ];

            callbackContext['columns'] = [
                {
                    field: "columnName",
                    title: "Column Name",
                    template: '# if (columnName) { # <span title="#:columnName#">#: columnName # </span># } else { #<span></span># } #'
                },
                {
                    field: "description",
                    title: "Description",
                    template: '# if (description) { # <span title="#:description#">#: description # </span># } else { #<span></span># } #'
                },
                {
                    field: "columnAt",
                    title: "Column At",
                    template: '# if (columnAt) { # <span title="#:columnAt#">#: columnAt # </span># } else { #<span></span># } #'
                },
                {
                    width: '10%',
                    command: [
                        {
                            name: "Delete",
                            text: "",
                            iconClass: "fa fa-trash",
                            click: function (e) {
                                e.preventDefault();

                                var tr = $(e.target).closest("tr");
                                var data = this.dataItem(tr);

                                var context = {};
                                context.data = data;
                                context.windowId = 'deleteModal';
                                context.title = 'Do you really want to delete the selected column?';
                                context.yesButtonEvent = customColumn.deleteConfirmationPrompt;
                                context.noButtonEvent = table.closeDeleteWindow;
                                context.grid = "Custom Column";

                                flux.deleteWindowExecute(context);
                            }
                        }
                    ]
                }
            ];


            callbackContext['edit'] = function (e)
            {
                var userModal = e.container.data("kendoWindow");

                userModal.content('<div class="common-box-grid-panel padding-t-20"> <form id="addCustomColumnForm"> <div class="fixed-height-body-popup-panel"> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-6"> <label for="columnName">Column Name</label> <input id="columnName" name="columnName" type="text" class="k-textbox" required maxlength="50" placeholder="Enter column name" /> </div> <div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <label for="columnAt">Access Control</label> <select id="columnAt" name="columnAt" class="k-dropdown" /> </div> </div> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="description">Description (optional)</label> <textarea id="description" name="description" class="k-textbox" placeholder="Enter description" style="padding: 5px;"></textarea> </div> </div> </div> <div class="footer-box-grid-box margin-t-10 align-right"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="addCustomColumn"> Save </button> <button class="defualt-btn k-button k-grid-cancel float-l" id="cancelCustomColumnPopUpButton">Cancel</button> </div> </form></div>');

                table.kendoWindowSetOption({title : 'Add Custom Column',width:'750px', kendoWindow : userModal});

                $("#columnAt").kendoDropDownList({
                    dataTextField: "label",
                    dataValueField: "value",
                    dataSource: [
                        { label: "Subnet IP", value: "subnetIp" }
                    ]
                });

                flux.bindKendoButtonClickEvent({element:'addCustomColumn',kendoModal : userModal},customColumn.addEntity);

                flux.bindKendoButtonClickEvent({element: 'cancelCustomColumnPopUpButton',kendoModal : userModal},customColumn.onCloseButtonClick);
            };

            customColumnTable.kendoTooltip({ filter: ".k-grid-toolbar a.k-grid-add", content: "Add", showAfter: 1000, position: "bottom"});

            customColumnTable.kendoTooltip({ filter: ".k-grid-edit", content: "Edit", showAfter: 1000, position: "bottom"});

            customColumnTable.kendoTooltip({ filter: ".k-grid-edit", content: "Edit", showAfter: 1000, position: "bottom"});

            customColumnTable.kendoTooltip({ filter: ".k-grid-Delete", content: "Delete", showAfter: 1000, position: "bottom"});

            customColumnTable.kendoTooltip({ filter: ".k-grid-Actions", content: "Actions", showAfter: 1000, position: "bottom"});

            $("#popupContent").kendoTooltip({ filter: "li[title]",showAfter: 1000, position: "bottom" });

            table.renderTable(callbackContext);

            loaderUtil.hideModalLoader();
        }
    };
