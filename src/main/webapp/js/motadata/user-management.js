var userManagement =
{
    popupContent : '',

    rolePopupContent : '',
    // ---------------------------------------------------------------------User management init-----------------------------------------------------------------------------------------------------//
    
    init : function ()
    {
        loaderUtil.showModalLoader();

        loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

        $("#settingMenuGrid").html('<div class="container-fluid"> <div class="content-section-panel"> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <div class="datatable-grid-panel"> <div class="widget-main-box"> <div class="widget-header-box"><div class="widget-header-title">User Management</div></div> <div class="widget-content-box" id="userManagementTable"></div> </div> </div> </div> </div> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <div class="datatable-grid-panel"> <div class="widget-main-box"> <div class="widget-header-box"><div class="widget-header-title">Role Management</div></div> <div class="widget-content-box" id="userRoleManagementTable"></div> </div> </div> </div> </div> <div class="common-box-grid-panel padding-t-20 padding-b-20"> <div class="fixed-height-body-popup-panel"><div id="changePasswordWindow"></div></div> </div> </div></div>');

        $('#deleteModal').empty();

        userManagement.renderUserManagement();

        userManagement.renderUserRoleManagement();

    },

    // ---------------------------------------------------------------------------Render User Grid-----------------------------------------------------------------------------------------------//
    
    renderUserManagementGrid : function (context)
    {
        if(context.json.data != null && context.json.success == true)
        {
            var data = context.json.data;

            for(var index=0;index < data.length ; index++)
            {
                data[index].roleId = data[index].userRoleId.role;
            }

            context.container.success(data);

        }
        else
        {
            context.container.success("");

            $(".k-grid-content").html(appConstant.NoDataSpan);
        }
        loaderUtil.hideCentralModalLoader();
    },

    // -----------------------------------------------------------------------Update Password action popup event---------------------------------------------------------------------------------------------------//
    
    onUpdatePasswordClick : function (event)
    {
        if(event)
        {
            event.preventDefault();

            var gridId = $(event.currentTarget).attr('data-id');

            var context = {};

            context.windowId = 'changePasswordWindow';

            context.title = 'Edit Password';

            context.gridId = gridId;

            flux.updatePasswordWindowExecute(context);
        }
    },

    // ------------------------------------------------------------------------Add user event--------------------------------------------------------------------------------------------------//
    
    addEntity : function (event)
    {
        if(event)
        {
            event.event.preventDefault();

            var kendoEvent = event.sender.options.prefix.kendoModal;

            var form = $("#addUserForm");

            var param =formManager.serializeForm(form);

            var validator = form.kendoValidator({
                rules: {
                    verifyPasswords: function(input)
                    {
                        var ret = true;
                        if (input.is("[name=Confirm Password]")) {
                            ret = input.val() === $("#password").val();
                        }
                        return ret;
                    }
                },
                messages: {
                    verifyPasswords: "Passwords do not match!"
                }
            }).data("kendoValidator");

            if(validator.validate())
            {
                loaderUtil.showModalLoader();

                appManager.executePOSTRequest({ url: '/user/', container: kendoEvent, callback: userManagement.afterEntityAddedOrUpdated, params: param});
            }
        }
    },

    // ----------------------------------------------------------------------------Edit user event----------------------------------------------------------------------------------------------//
    
    editEntity : function (event)
    {
        if(event)
        {
            event.event.preventDefault();

            var kendoEvent = event.sender.options.prefix.kendoModal;

            var id = $(event.event.currentTarget).data('id');

            var form = $("#editUserForm");

            var param =formManager.serializeForm(form);

            var validator = form.kendoValidator().data("kendoValidator");

            if(validator.validate())
            {
                loaderUtil.showModalLoader();

                appManager.executePUTRequest({url: '/user/'+id, container: kendoEvent,callback: userManagement.afterEntityAddedOrUpdated, params: param});
            }
        }
    },

    // ------------------------------------------------------------------------------Update password event--------------------------------------------------------------------------------------------//
    
    editPasswordEntity : function (event)
    {
        if(event)
        {
            event.preventDefault();

            var id = event.sender.options.prefix.rowId;

            var kendoEvent = event.sender.options.prefix.kendoModal;

            var form = $("#editPasswordForm");

            var param = formManager.serializeForm(form);

            var validator = form.kendoValidator({
                rules: {
                    verifyPasswords: function(input)
                    {
                        var ret = true;
                        if (input.is("[name=Confirm Password]")) {
                            ret = input.val() === $("#password").val();
                        }
                        return ret;
                    }
                },
                messages: {
                    verifyPasswords: "Passwords do not match!"
                }

            }).data("kendoValidator");

            if(validator.validate())
            {
                loaderUtil.showModalLoader();

                appManager.executePUTRequest({url:'/changePassword/'+id,container:kendoEvent,callback:userManagement.afterEntityAddedOrUpdated,params:param});
            }
        }
    },

    // ----------------------------------------------------------------------------After user add,update & password update operations----------------------------------------------------------------------------------------------//
    
    afterEntityAddedOrUpdated : function (context)
    {
        if(context)
        {
            var gridId = $("#userManagementTable");

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

    // -------------------------------------------------------------------------------User Delete-------------------------------------------------------------------------------------------//

    afterUserDeleted : function (context)
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

            var grid = $("#userManagementTable").data('kendoGrid');

            grid.dataSource.remove(context);

            grid.dataSource.sync();

            modal.close();

            loaderUtil.hideModalLoader();
        }
    },

    // -----------------------------------------------------------------------------Close user grid modal---------------------------------------------------------------------------------------------//

    onCloseButtonClick : function (event)
    {
        if(event)
        {
            event.event.preventDefault();

            var modal = event.sender.options.prefix.kendoModal;

            modal.close();

            var gridId = $("#userManagementTable");

            flux.refreshKendoGrid({gridId : gridId});

        }
    },
    renderUserManagement : function () {
        $('#deleteModal').empty();

        var userManagementTable = $("#userManagementTable");

        var callbackContext = [];

        callbackContext['id'] = userManagementTable;

        callbackContext['name'] = "User";

        callbackContext['dataSource'] =
            {
                pageSize: 10,
                transport:
                    {
                        read: function (options)
                        {
                            appManager.executeGETRequest({url: '/user/',container:options,callback:userManagement.renderUserManagementGrid});
                        },
                        destroy:function (options)
                        {
                            var id = options.data.models[0].id;

                            appManager.executeDELETERequest({ url: '/user/'+id, callback: userManagement.afterUserDeleted, gridId:userManagementTable});
                        },

                        parameterMap: function(options, operation)
                        {
                            if (operation != "read" && options.models)
                            {
                                return {models: kendo.stringify(options.models)};
                            }
                        }
                    },

                batch: true,
                schema: {
                    model: {
                        id: "id",
                        fields: {
                            userName: { type: "string"},
                            roleId: { type: "string" },
                            activeStatus: { type: "string"},
                            email: { type: "string"},
                            previousLoginStatus: { type: "string" },
                            description: { type: "string"},
                            password: { type: "password" },
                            rePassword: { type: "password"}
                        }
                    }
                }
            };

        callbackContext['toolbar'] = [{name: "create", text: "<span class='fa fa-plus'> Add New User</span>"}];

        callbackContext['columns'] = [
            { field:"userName", title: "Name",template:'# if (userName) { # <span title="#:userName#">#: userName # </span># } else { #<span></span># } #'},
            { field: "roleId", title:"Access Control",width:'10%',template:'# if (roleId) { # <span title="#:roleId#">#: roleId # </span># } else { #<span></span># } #'},
            { field: "activeStatus", title:"Status",width:'7%',template:'# if (activeStatus) { # <span title="#:activeStatus#">#: activeStatus # </span># } else { #<span></span># } #'},
            { field: "email", title:"Email",template:'# if (email) { # <span title="#:email#">#: email # </span># } else { #<span></span># } #'},
            { field: "previousLoginStatus", title:"Previous Login",template:'# if (previousLoginStatus) { # <span title="#:previousLoginStatus#">#: previousLoginStatus # </span># } else { #<span></span># } #'},
            { field: "description", title:"Description",template:'# if (description) { # <span title="#:description#">#: description # </span># } else { #<span></span># } #'},
            {
                width:'10%',
                command: [
                    {name: "edit", text: {edit: "", update: "Save", cancel: "Cancel"}, iconClass: "fa fa-pencil",title:"Edit"},
                    {name: "Delete", text: "", iconClass: "fa fa-trash",click:function (e)
                        {
                            e.preventDefault();

                            var tr = $(e.target).closest("tr");

                            var data = this.dataItem(tr);

                            var context = {};

                            context.data = data;

                            context.windowId = 'deleteModal';

                            context.title = 'Do you really want to delete the selected User?';

                            context.yesButtonEvent = userManagement.deleteConfirmationPrompt;

                            context.noButtonEvent = table.closeDeleteWindow;

                            context.grid = "User";

                            flux.deleteWindowExecute(context);
                        }},
                    {name: "Actions", text: "", iconClass: "fa fa-ellipsis-h",click:function (e) {

                            e.preventDefault();

                            var target = e.currentTarget;

                            var tr = $(e.target).closest("tr");

                            var data = this.dataItem(tr);

                            userManagement.popupContent.setOptions({
                                anchor: $(target),
                                position: "top center"
                            });

                            $("#popupContent").find('li').attr('data-id',data.id);

                            userManagement.popupContent.open();

                            userManagement.popupContent.position();
                        }}
                ]
            }
        ];

        callbackContext['edit'] = function (e)
        {
            var userModal = e.container.data("kendoWindow");

            if (!e.model.isNew())
            {
                var id = e.model.id;

                userModal.content('<div class="common-box-grid-panel padding-t-20"><form id="editUserForm"><div class="fixed-height-body-popup-panel"><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"> <label for="userName">Name</label> <input name="userName" type="text" class="k-textbox" required validationMessage="Name is required" maxlength="50"/></div><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <label for="roleId">Access Control</label> <select id="roleId" name="roleId" class="k-dropdown" /></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"> <label for="email">Email Id</label> <input name="email" type="email" class="k-textbox" required validationMessage="Valid Email address is required" maxlength="100"/></div><div class="col-xs-12 col-md-12 col-lg-6"> <label>Status</label><div class="col-xs-12 col-md-12 col-lg-12 padding-0 margin-t-5"> <input type="radio" name="activeStatus" class="k-radio" value="Enable" /> <label class="k-radio-label label-2" for="activeStatus">Enabled</label> <input type="radio" name="activeStatus" class="k-radio" value="Disable"/> <label class="k-radio-label label-2" for="activeStatus">Disabled</label></div></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-12"> <label for="description">Description</label> <input name="description" type="text" class="k-textbox" maxlength="100"/></div></div></div><div class="footer-box-grid-box margin-t-10 align-right"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="editUserManagement" data-id="'+id+'">Update</button> <button class="defualt-btn k-button k-grid-cancel float-l" id="cancelUserButton">Cancel</button></div></form></div>');

                table.kendoWindowSetOption({title : 'Edit User',width:'750px', kendoWindow : userModal});

                flux.getKendoDropDownListURL({dropDownId:$("#roleId"),url:"/userRole/",dataTextField: "role",dataValueField: "id"});

                var formContext = $("#editUserForm");

                loaderUtil.showModalLoader();

                appManager.executeGETRequest({url: '/user/'+id, container: formContext, callback: formManager.renderForm, params: {userId : id}});

                loaderUtil.hideModalLoader();

                flux.bindKendoButtonClickEvent({element:'editUserManagement',kendoModal : userModal},userManagement.editEntity);

                flux.bindKendoButtonClickEvent({element: 'cancelUserButton',kendoModal : userModal},userManagement.onCloseButtonClick);
            }
            else
            {
                userModal.content('<div class="common-box-grid-panel padding-t-20"><form id="addUserForm"><div class="fixed-height-body-popup-panel"><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"> <label for="userName">Name</label> <input name="userName" type="text" class="k-textbox" required validationMessage="Name is required" maxlength="50"/></div><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <label for="roleId">Access Control</label> <select id="roleId" name="roleId" class="k-dropdown" /></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"> <label for="password">Password</label> <input name="password" type="password" class="k-textbox" id="password" required validationMessage="Password is required" maxlength="20"/></div><div class="col-xs-12 col-md-12 col-lg-6"> <label for="rePassword">Confirm Password</label> <input name="Confirm Password" type="password" class="k-textbox" id="rePassword" required maxlength="20"/> <span class="k-invalid-msg" data-for="password-confirm"></span></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-6"> <label for="email">Email Id</label> <input name="email" type="email" class="k-textbox" required validationMessage="Valid Email address is required" maxlength="100" /></div><div class="col-xs-12 col-md-12 col-lg-6"> <label>Status</label><div class="col-xs-12 col-md-12 col-lg-12 padding-0 margin-t-5"> <input type="radio" name="activeStatus" class="k-radio" value="Enable" checked /> <label class="k-radio-label label-2" for="activeStatus">Enabled</label> <input type="radio" name="activeStatus" class="k-radio" value="Disable"/> <label class="k-radio-label label-2" for="status">Disabled</label></div></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-12"> <label for="description">Description</label> <input name="description" type="text" class="k-textbox" maxlength="100"/></div></div></div><div class="footer-box-grid-box margin-t-10 align-right"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="addUserManagement"> Save </button> <button class="defualt-btn k-button k-grid-cancel float-l" id="cancelUserButton">Cancel</button></div></form></div>');

                table.kendoWindowSetOption({title : 'Add New User',width:'750px', kendoWindow : userModal});

                flux.getKendoDropDownListURL({dropDownId:$("#roleId"),url:"/userRole/",dataTextField: "role",dataValueField: "id"});

                flux.bindKendoButtonClickEvent({element:'addUserManagement',kendoModal : userModal},userManagement.addEntity);

                flux.bindKendoButtonClickEvent({element: 'cancelUserButton',kendoModal : userModal},userManagement.onCloseButtonClick);
            }
        };

        userManagementTable.kendoTooltip({ filter: ".k-grid-toolbar a.k-grid-add", content: "Add", showAfter: 1000, position: "bottom"});

        userManagementTable.kendoTooltip({ filter: ".k-grid-edit", content: "Edit", showAfter: 1000, position: "bottom"});
        userManagementTable.kendoTooltip({ filter: ".k-grid-edit", content: "Edit", showAfter: 1000, position: "bottom"});

        userManagementTable.kendoTooltip({ filter: ".k-grid-Delete", content: "Delete", showAfter: 1000, position: "bottom"});

        userManagementTable.kendoTooltip({ filter: ".k-grid-Actions", content: "Actions", showAfter: 1000, position: "bottom"});

        $("#popupContent").kendoTooltip({ filter: "li[title]",showAfter: 1000, position: "bottom" });

        table.renderTable(callbackContext);

        loaderUtil.hideModalLoader();
    },
    renderUserRoleManagement : function () {

        var userRoleManagementTable = $("#userRoleManagementTable");

        var callbackContext = [];

        callbackContext['id'] = userRoleManagementTable;

        callbackContext['name'] = "User";

        callbackContext['dataSource'] = {
            pageSize: 10,
            transport: {
                read: function (options) {
                    appManager.executeGETRequest({url: '/userRole/', container: options, callback: userManagement.renderUserRoleManagementGrid});
                },
                destroy: function (options) {
                    var id = options.data.models[0].id;
                    appManager.executeDELETERequest({ url: '/userRole/' + id, callback: userManagement.afterUserDeleted, gridId: userRoleManagementTable});
                },

                parameterMap: function (options, operation) {
                    if (operation != "read" && options.models) {
                        return {models: kendo.stringify(options.models)};
                    }
                }
            },

            batch: true,
            schema: {
                model: {
                    id: "id",
                    fields: {
                        role: { type: "string" },
                        description: { type: "string" },
                        permissions: { type: "object" }, // Permissions field as an object
                    }
                }
            }
        };

        callbackContext['toolbar'] = [{name: "create", text: "<span class='fa fa-plus'> Add New Role</span>"}];

        callbackContext['columns'] = [
           // { field: "roleName", title: "Role Name", template: '# if (role) { # <span title="#:roleName#">#: roleName # </span># } else { #<span></span># } #'},
            { field: "role", title: "Role Name", template: "#: role ? role : 'N/A' #"},
            { field: "description", title: "Description", template: "#: description ? description : 'N/A' #"},
            {
                field: "Permissions", title: "Permissions",
                width: '10%',
                command: [
                    { name: "showPermissions", text: "", iconClass: "fa fa-eye clickable-icon", click: function (e) {
                            e.preventDefault();
                            var tr = $(e.target).closest("tr");
                            var data = this.dataItem(tr);
                            userManagement.showFeaturePermissionsBox(data);
                        }}
                ]
            },
            {
                field: "Actions", title: "Actions",
                width: '10%',
                command: [
                    { name: "edit", text: { edit: "", update: "Save", cancel: "Cancel" }, iconClass: "fa fa-pencil clickable-icon", title: "Edit" },
                    {name: "Delete", text: "", iconClass: "fa fa-trash",click:function (e)
                        {
                            e.preventDefault();

                            var tr = $(e.target).closest("tr");

                            var data = this.dataItem(tr);

                            var context = {};

                            context.data = data;

                            context.windowId = 'deleteModal';

                            context.title = 'Do you really want to delete the selected Role?';

                            context.yesButtonEvent = userManagement.deleteRoleConfirmationPrompt;

                            context.noButtonEvent = table.closeDeleteWindow;

                            context.grid = "Role";

                            flux.deleteWindowExecute(context);
                        }}
                ]
            }
        ];

        callbackContext['edit'] = function (e)
        {
            var userModal = e.container.data("kendoWindow");

            if (!e.model.isNew())
            {
                var id = e.model.id;

                userModal.content('<div class="common-box-grid-panel padding-t-20"> <form id="editUserRoleForm"> <div class="fixed-height-body-popup-panel"> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-6"><label for="role">Name</label> <input id="role" name="roleName" type="text" class="k-textbox" required validationMessage="Name is required" maxlength="50" /></div> </div> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"><label for="description">Description</label> <input id="description" name="description" type="text" class="k-textbox" maxlength="100" /></div> </div> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="editPermissionsGrid">Permission</label> <div id="editPermissionsGrid"></div> <!-- Kendo Grid --> </div> </div> </div> <div class="footer-box-grid-box margin-t-10 align-right"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="editUserRoleManagement" data-id="'+id+'">Update</button> <button class="defualt-btn k-button k-grid-cancel float-l" id="cancelUserRoleButton">Cancel</button> </div> </form></div>');

                table.kendoWindowSetOption({ title: 'Edit Role', width: '750px', kendoWindow: userModal });

                $("#editPermissionsGrid").kendoGrid({
                    dataSource: {
                        data: [], // Initially empty, will be populated after API call
                        schema: {
                            model: {
                                id: "id",
                                fields: {
                                    featureName: { type: "string" },
                                    read: { type: "boolean" },
                                    write: { type: "boolean" }
                                }
                            }
                        }
                    },
                    columns: [
                        { field: "featureName", title: "Feature Name" },
                        {
                            field: "read", title: "Read", width: "100px",
                            template: '<input type="checkbox" class="read-checkbox" data-id="#=id#" #= read ? "checked" : "" # style="opacity:1;" />'
                        },
                        {
                            field: "write", title: "Write", width: "100px",
                            template: '<input type="checkbox" class="write-checkbox" data-id="#=id#" #= write ? "checked" : "" # style="opacity:1;" />'
                        }
                    ],
                    editable: false,
                    pageable: false
                });

                var formContext = $("#editUserRoleForm");

                loaderUtil.showModalLoader();

                // Fetch user role data from API
                appManager.executeGETRequest({
                    url: '/userRole/' + id,
                    container: formContext,
                    callback: userManagement.renderFormUserRole,
                    params: { userId: id }
                });

                let grid = $("#editPermissionsGrid").data("kendoGrid");
                console.log("Grid DataSource:", grid.dataSource);

                loaderUtil.hideModalLoader();

                flux.bindKendoButtonClickEvent(
                    { element: 'editUserRoleManagement', kendoModal: userModal, id: id },
                    function (event) {
                        userManagement.editRoleEntity(event, id);
                    }
                );

                flux.bindKendoButtonClickEvent({ element: 'cancelUserRoleButton', kendoModal: userModal }, userManagement.onCloseRoleButtonClick);

            }
            else
            {
                userModal.content('<div class="common-box-grid-panel padding-t-20"> <form id="addUserForm"> <div class="fixed-height-body-popup-panel"> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-6"><label for="userName">Name</label> <input name="roleName" type="text" class="k-textbox" required validationMessage="Name is required" maxlength="50" /></div> </div> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"><label for="description">Description</label> <input name="description" type="text" class="k-textbox" maxlength="100" /></div> </div> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="Permissions">Permission</label> <div id="addPermissionsGrid"></div> <!-- Kendo Grid --> </div> </div> </div> <div class="footer-box-grid-box margin-t-10 align-right"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="addUserRoleManagement" data-id="'+id+'">Save</button> <button class="defualt-btn k-button k-grid-cancel float-l" id="cancelUserRoleButton">Cancel</button> </div> </form></div>');

                table.kendoWindowSetOption({title : 'Add New Role',width:'750px', kendoWindow : userModal});

                $("#addPermissionsGrid").kendoGrid({
                    dataSource: {
                        transport: {
                            read: {
                                url: "/userRole/feature/",
                                dataType: "json",
                                type: "GET"
                            }
                        },
                        schema: {
                            model: {
                                id: "id",
                                fields: {
                                    featureName: { type: "string" },
                                    read: { type: "boolean" , defaultValue: false},
                                    write: { type: "boolean" , defaultValue: false}
                                }
                            }
                        }
                        ,pageSize: 8
                    },
                    columns: [
                        { field: "featureName", title: "Feature Name" },
                        {
                            field: "read", title: "Read", width: "100px",
                            template: '<input type="checkbox" class="read-checkbox" data-id="#=id#" #= read ? "checked" : "" # style="opacity:1;" />'
                        },
                        {
                            field: "write", title: "Write", width: "100px",
                            template: '<input type="checkbox" class="write-checkbox" data-id="#=id#" #= write ? "checked" : "" # style="opacity:1;" />'
                        }
                    ],
                    pageable: true
                });

                flux.bindKendoButtonClickEvent({element:'addUserRoleManagement',kendoModal : userModal},userManagement.addRoleEntity);

                flux.bindKendoButtonClickEvent({element: 'cancelUserRoleButton',kendoModal : userModal},userManagement.onCloseRoleButtonClick);
            }
        };

        userRoleManagementTable.kendoTooltip({ filter: ".k-grid-toolbar a.k-grid-add", content: "Add", showAfter: 1000, position: "bottom" });
        userRoleManagementTable.kendoTooltip({ filter: ".k-grid-edit", content: "Edit", showAfter: 1000, position: "bottom" });
        userRoleManagementTable.kendoTooltip({ filter: ".k-grid-Delete", content: "Delete", showAfter: 1000, position: "bottom" });
        userRoleManagementTable.kendoTooltip({ filter: ".k-grid-Actions", content: "Actions", showAfter: 1000, position: "bottom" });

        $("#rolePopupContent").kendoTooltip({ filter: "li[title]", showAfter: 1000, position: "bottom" });

        table.renderTable(callbackContext);

        loaderUtil.hideModalLoader();
    },
    renderUserRoleManagementGrid: function (context) {
        if (context.json.data != null && context.json.success == true) {
            var data = context.json.data;

            context.container.success(data);
        } else {
            context.container.success("");
            $(".k-grid-content").html(appConstant.NoDataSpan);
        }
        loaderUtil.hideCentralModalLoader();
    },

    showFeaturePermissionsBox: function (dataItem)
    {
        const permissions = dataItem.roleFeaturePermissions || {};

        const filteredPermissions = permissions.map(permission => ({
            feature: permission.feature.name, // Extracting feature name
            read: permission.readPermission || false,
            write: permission.writePermission || false
        }));

        let $rolePopupContent= $("#rolePopupContent");

        if ($rolePopupContent.length === 0)
        {
            $("body").append('<div id="rolePopupContent"></div>');

            $rolePopupContent = $("#rolePopupContent");
        }

        $rolePopupContent.html('<div id="permissionsGrid"></div>');

        let grid = $("#permissionsGrid").data("kendoGrid");

        if (grid)
        {
            grid.setDataSource(new kendo.data.DataSource({ data: filteredPermissions }));

            grid.refresh();
        }
        else
        {
            $("#permissionsGrid").kendoGrid({
                dataSource: {
                    data: filteredPermissions,
                    schema: {
                        model: {
                            fields: {
                                feature: { type: "string" },
                                read: { type: "boolean" },
                                write: { type: "boolean" }
                            }
                        }
                    },
                    pageSize: 10
                },
                pageable: true,
                columns: [
                    { field: "feature", title: "Feature Name", width: "50%"},
                    {
                        field: "read",
                        title: "Read",
                        width: "25%",
                        template: data => `<input type="checkbox" ${data.read ? 'checked' : ''} disabled 
                style="cursor: not-allowed; transform: scale(1.5); opacity:1;" />`
                    },
                    {
                        field: "write",
                        title: "Write",
                        width: "25%",
                        template: data => `<input type="checkbox" ${data.write ? 'checked' : ''} disabled
                style="cursor: not-allowed; transform: scale(1.5); opacity:1;" />`
                    }
                ]
            });
        }

        let kendoWindow = $rolePopupContent.data("kendoWindow");
        if (!kendoWindow) {
            kendoWindow = $rolePopupContent.kendoWindow({
                title: "Feature Permissions",
                width: "450px",
                height: "auto",
                actions: ["Close"],
                resizable: false,
                modal: true
            }).data("kendoWindow");
        }

        setTimeout(() => {
            let wrapper = kendoWindow.wrapper;

            wrapper.css({
                "width": "450px",
                "min-width": "350px",
                "min-height": "350px",
                "overflow": "visible"
            });

            wrapper.find(".k-window-content").css({
                "height": "auto",
                "min-height": "auto",
                "overflow": "visible"
            });

        }, 100);

        kendoWindow.center().open();
    },

    addRoleEntity : function (event)
    {
        if(event)
        {
            event.event.preventDefault();

            let grid = $("#addPermissionsGrid").data("kendoGrid");

            // Get data source items
            let data = grid.dataSource.view();

            // Extract the required data
            let selectedPermissions = data.map(item => ({
                id: item.id,
                featureName: item.featureName,
                read: $(`input.read-checkbox[data-id="${item.id}"]`).is(":checked"),
                write: $(`input.write-checkbox[data-id="${item.id}"]`).is(":checked")
            }));

            console.log("Permissions Data:", selectedPermissions);

            var kendoEvent = event.sender.options.prefix.kendoModal;

            var form = $("#addUserForm");

            var param =formManager.serializeForm(form);

            param.permissions = selectedPermissions;

            var validator = form.kendoValidator({
                rules: {
                    verifyPasswords: function(input)
                    {
                        var ret = true;
                        if (input.is("[name=Confirm Password]")) {
                            ret = input.val() === $("#password").val();
                        }
                        return ret;
                    }
                },
                messages: {
                    verifyPasswords: "Passwords do not match!"
                }
            }).data("kendoValidator");

            if(validator.validate())
            {
                loaderUtil.showModalLoader();

                appManager.executePOSTRequest({ url: '/userRole/', container: kendoEvent, callback: userManagement.afterUserRoleEntityAddedOrUpdated, params: param});
            }
        }
    },

    editRoleEntity : function (event, id)
    {
        if(event)
        {
            event.event.preventDefault();

            let grid = $("#editPermissionsGrid").data("kendoGrid");

            let data = grid.dataSource.view();

            let selectedPermissions = data.map(item => ({
                featureName: item.feature,
                read: item.read,
                write: item.write
            }));

            console.log("Permissions Data:", selectedPermissions);

            var form = $("#editUserRoleForm");

            var param =formManager.serializeForm(form);

            param.permissions = selectedPermissions;

            param.id=id;

            var kendoEvent = event.sender.options.prefix.kendoModal;

            loaderUtil.showModalLoader();

            appManager.executePUTRequest({ url: '/userRole/', container: kendoEvent, callback: userManagement.afterUserRoleEntityAddedOrUpdated, params: param});

        }
    },

    // ----------------------------------------------------------------------------Edit user event----------------------------------------------------------------------------------------------//

    afterUserRoleEntityAddedOrUpdated : function (context)
    {
        if(context)
        {
            var gridId = $("#userRoleManagementTable");

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

    deleteRoleConfirmationPrompt : function (event)
    {
        if(event)
        {
            event.preventDefault();

            loaderUtil.showModalLoader();

            var modal =  event.data.kendoModal;

            var context =  event.data.data;

            var grid = $("#userRoleManagementTable").data('kendoGrid');

            grid.dataSource.remove(context);

            grid.dataSource.sync();

            modal.close();

            loaderUtil.hideModalLoader();
        }
    },
    onCloseRoleButtonClick : function (event)
    {
        if(event)
        {
            event.event.preventDefault();

            var modal = event.sender.options.prefix.kendoModal;

            modal.close();

            var gridId = $("#userRoleManagementTable");

            flux.refreshKendoGrid({gridId : gridId});

        }
    },
    renderFormUserRole: function (response) {
        if (response.json && response.json.data) {
            let userData = response.json.data;

            $("input[name='roleName']").val(userData.role);
            $("input[name='description']").val(userData.description);

            let gridElement = $("#editPermissionsGrid");
            let existingGrid = gridElement.data("kendoGrid");

            // Destroy existing grid before re-initializing
            if (existingGrid) {
                existingGrid.destroy();
                gridElement.empty();
            }

            const permissions = userData.roleFeaturePermissions || [];
            const filteredPermissions = permissions.map(permission => ({
                feature: permission.feature.name,
                read: permission.readPermission || false,
                write: permission.writePermission || false
            }));

            // Initialize new grid
            gridElement.kendoGrid({
                dataSource: {
                    data: filteredPermissions,
                    schema: {
                        model: {
                            fields: {
                                feature: { type: "string" },
                                read: { type: "boolean" },
                                write: { type: "boolean" }
                            }
                        }
                    },
                    pageSize: 8
                },
                pageable: true,
                editable: false, // Prevents unintended UI re-renders
                columns: [
                    { field: "feature", title: "Feature Name", width: "50%" },
                    {
                        field: "read",
                        title: "Read",
                        width: "25%",
                        template: function (dataItem) {
                            return `<input type="checkbox" class="read-checkbox" data-uid="${dataItem.uid}" ${dataItem.read ? 'checked' : ''} style="transform: scale(1.5); opacity:1;" />`;
                        }
                    },
                    {
                        field: "write",
                        title: "Write",
                        width: "25%",
                        template: function (dataItem) {
                            return `<input type="checkbox" class="write-checkbox" data-uid="${dataItem.uid}" ${dataItem.write ? 'checked' : ''} style="transform: scale(1.5); opacity:1;" />`;
                        }
                    }
                ]
            });

            // Handle checkbox state updates
            gridElement.on("change", ".read-checkbox, .write-checkbox", function () {
                let grid = gridElement.data("kendoGrid");
                let row = $(this).closest("tr");
                let dataItem = grid.dataItem(row);

                if (dataItem) {
                    let isWriteCheckbox = $(this).hasClass("write-checkbox");
                    let isReadCheckbox = $(this).hasClass("read-checkbox");
                    let checked = $(this).is(":checked");

                    if (isWriteCheckbox) {
                        if (checked) {
                            dataItem.set("read", true);
                            row.find(".read-checkbox").prop("checked", true);
                        }
                        dataItem.set("write", checked);
                    }

                    if (isReadCheckbox) {
                        dataItem.set("read", checked);
                    }

                    grid.refresh(); // Ensure UI updates properly

                    console.log(`Updated ${isWriteCheckbox ? "Write" : "Read"} for ${dataItem.feature} to ${checked}`);
                }
            });

            console.log("Grid DataSource1:", gridElement.data("kendoGrid").dataSource);
        }
    }
};
