var leftPanel =
{
    popupContent : '',

    // ----------------------------------------------------------------------Open left panel----------------------------------------------------------------------------------------------------//
    
    onLeftArrowClick: function (event)
    {
        var toggleId = $("#container-panel");

        toggleId.removeClass("contentOpenPanel");

        if(toggleId.hasClass('leftOpenPanel'))
        {
            toggleId.removeClass('leftOpenPanel');
        }
        else
        {
            toggleId.addClass('leftOpenPanel');
        }

        if(toggleId.attr('class')===('rightOpenPanel leftOpenPanel'))
        {
            toggleId.addClass("contentOpenPanel")
        }

        $('.leftArrow').toggleClass('open');
    },

    // --------------------------------------------------------------------Call leftpanel treeview------------------------------------------------------------------------------------------------------//
    
    renderTreeView: function (menuName)
    {
        var homogeneous = new kendo.data.HierarchicalDataSource({
            transport:
                {
                    read: function (options)
                    {
                        appManager.executeGETRequest({url: '/subnetByCategory/',container:options,menuName:menuName, eventId:'categoryView',callback:leftPanel.afterTreeViewInit});
                    }
                },
            schema: {
                model: {
                    id: "id",
                    children: "subnets"
                }
            }
        });

        leftPanel.loadKendoTreeView("categoryView", homogeneous);

        leftPanel.renderTreeViewSupernet(menuName)

        flux.bindEvent({element: 'categoryView', selector: 'span[data-link=subnetAddressDelete]'}, leftPanel.removeSubnet);

        flux.bindEvent({element: 'categoryView', selector: 'span[data-link=subnetAddressUpdate]'}, leftPanel.onEditSubnetClick);

        flux.bindEvent({element: 'supernetView', selector: 'span[data-link=subnetAddressDelete]'}, leftPanel.removeSubnet);

        flux.bindEvent({element: 'supernetView', selector: 'span[data-link=subnetAddressUpdate]'}, leftPanel.onEditSubnetClick);

        flux.bindElementEvent({element:'leftPanel',selector:'#categorySearch',id:'supernetView', event:'keyup'}, _.debounce(function(event){

                flux.searchTreeViewInInventory(event,"categoryView");

                flux.searchTreeViewInInventory(event,"supernetView");

        }, 500));


        flux.bindKendoButtonClickEvent({element: 'categoryExpand'}, homeManager.onAddSubnetButtonClick);
    },

    renderTreeViewSupernet: function (menuName)
    {
        var homogeneous = new kendo.data.HierarchicalDataSource({
            transport:
                {
                    read: function (options)
                    {
                            appManager.executeGETRequest({url: '/supernetByCategory/',container:options,menuName:menuName, eventId:'supernetView',callback:leftPanel.afterTreeViewInitSupernet});
                    }
                },
            schema: {
                model: {
                    id: "id",
                    children: "subnets"
                }
            }
        });

        leftPanel.loadKendoTreeView("supernetView", homogeneous);


    },

    loadKendoTreeView: function (viewName, homogeneous)
    {
        $("#"+viewName).kendoTreeView({

            autoScroll : true,

            loadOnDemand : false,

            dragAndDrop: (viewName === "supernetView") ? false : true,

            dataSource:  homogeneous,

            dragstart: function(e) {

                if ($(e.sourceNode).parentsUntil(".k-treeview", ".k-item").length == 0) {

                    e.preventDefault();
                }
            },

            drop: function (e) {

                var sourceTree = $(e.sourceNode).closest(".k-treeview").attr("id");

                var destinationTree = $(e.destinationNode).closest(".k-treeview").attr("id");

                if (sourceTree === "categoryView" && destinationTree === "supernetView")
                {
                    e.setValid(false);

                    return;
                }

                if($(e.destinationNode).parentsUntil(".k-treeview", ".k-item").length == 1 || e.dropPosition == "before" || e.dropPosition == "after" || $(e.dropTarget).hasClass('nav-panel'))
                {
                    e.setValid(false);
                }

                else if($(e.destinationNode).hasClass('k-item'))
                {
                    if(appManager.validatePermission() == true)
                    {
                        var treeView = this;

                        var sourceDataUid = $(e.sourceNode).data("uid");

                        var childUid = treeView.dataSource.getByUid(sourceDataUid);

                        if(childUid!=undefined)
                        {
                            var subnetId = childUid.id;
                        }

                        var destinationDataUid = $(e.destinationNode).data("uid");

                        var parentUid = treeView.dataSource.getByUid(destinationDataUid);

                        if(parentUid!=undefined)
                        {
                            var categoryId = parentUid.id;
                        }

                        var modal = $('#addModal');

                        modal.width('450px');

                        flux.kendoWindowSetOption({kendoWindow:modal});

                        $('#addModal_wnd_title').html('Do you really want to move subnet category?');

                        modal.data('kendoWindow').content('<div class="common-box-grid-panel padding-t-20 padding-b-20"><div class="fixed-height-body-popup-panel"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="subnetDragYesButton">Yes</button> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="subnetDragNoButton">No</button></div></div>');

                        flux.bindKendoButtonClickEvent({element:'subnetDragYesButton',kendoModal:modal,categoryId:categoryId,subnetId:subnetId},leftPanel.onSubnetDropClick);

                        //flux.bindKendoButtonClickEvent({element:'subnetDragNoButton',kendoModal:modal},flux.closeKendoModal);

                        $("#subnetDragNoButton").on("click",function(){
                            modal.data('kendoWindow').close();
                            leftPanel.initTreeView();
                        });

                        $('body').keyup(function(e) {
                            if(e.which === 27){
                                leftPanel.initTreeView();
                            }
                        });
                    }
                    else
                    {
                        e.setValid(false);
                    }
                }
            },

            template: '#if(!item.subnets){#<span class="tree-name" data-uid="#:item.id#" data-link="subnetAddress" data-name="#: item.subnetName #" title="#:item.subnetName#">#:item.subnetName#</span>' +
            '<span class="tree-edit" data-uid="#:item.id#" data-link="subnetAddressUpdate" data-name="#: item.subnetAddress #" title="Edit Subnet"><i class="fa fa-edit"></i></span>' +
            '<span class="tree-delete fa fa-trash" data-uid="#:item.id#" data-link="subnetAddressDelete" data-name="#: item.subnetAddress #" href="\\#" title="Delete Subnet"></span>#}' +
            'else if(item.subnets != null && item.subnets){# <span class="page">#:item.subnetAddress#</span>' +
            '#if(item.severity == 1){#<span class="value critical-v" style="cursor: default">#:parseFloat(item.totalUsedIpPercentage).toFixed(2)#%</span>#}' +
            'else if(item.severity == 2){#<span class="value warning-v" style="cursor: default">#:parseFloat(item.totalUsedIpPercentage).toFixed(2)#%</span>#}' +
            'else{#<span class="value normal-v" style="cursor: default">#:parseFloat(item.totalUsedIpPercentage).toFixed(2)#%</span>#}#<span class="action">' +
            '<span class="fa fa-ellipsis-h" data-uid="#:item.id#" id="categoryPopupAction" href="\\#" title="Actions" ></span></span>#}#'
        });
    },

    // ---------------------------------------------------------------------------Render treeview-----------------------------------------------------------------------------------------------//

    afterTreeViewInitSupernet : function (context)
    {
        var treeViewId = $("#"+context.eventId);

        if(context.json.data != null && context.json.success == true)
        {
            treeViewId.show();

            var result = context.json.data;

            context.container.success(result);

            var kendoTreeView = treeViewId.data("kendoTreeView");

            treeViewId.on("click", ".k-in .page", function(e) {
                kendoTreeView.toggle($(e.target).closest(".k-item"));
            });

            appManager.resetWindowSize();

            appManager.initCustomScrollbar({container: $('#supernetView')});

            flux.bindEvent({element:'supernetView span[data-link=subnetAddress]',breadCrumbMenu:"home"}, subnetSummary.renderSubnetPage);

            flux.bindEvent({element:'supernetView #categoryPopupAction', isSupernet: 'yes'}, leftPanel.openEditingPopup);

            flux.bindEvent({element:'leftTreePopupContent #deleteSupernetCategory'},leftPanel.deleteSupernetCategory);
        }
        else
        {
            treeViewId.hide();

        }

        leftPanel.setActiveMenu(context.eventId, context.menuName);
    },

    afterTreeViewInit : function (context)
    {
        var treeViewId = $("#"+context.eventId);

        if(context.json.data != null && context.json.success == true)
        {
            var result = context.json.data;

            context.container.success(result);

            var kendoTreeView = treeViewId.data("kendoTreeView");

            treeViewId.on("click", ".k-in .page", function(e) {
                kendoTreeView.toggle($(e.target).closest(".k-item"));
            });

            appManager.resetWindowSize();

            appManager.initCustomScrollbar({container: $('#categoryView')});

            flux.bindEvent({element:'categoryView span[data-link=subnetAddress]',breadCrumbMenu:"home"}, subnetSummary.renderSubnetPage);

            flux.bindEvent({element:'categoryView #categoryPopupAction', isSupernet: 'no'}, leftPanel.openEditingPopup);

            flux.bindEvent({element:'leftTreePopupContent #renameCategory'},leftPanel.renameCategory);

            flux.bindEvent({element:'leftTreePopupContent #deleteCategory'},leftPanel.deleteCategory);
        }
        else
        {
            treeViewId.html(appConstant.NoDataSpan);
        }

        leftPanel.setActiveMenu(context.eventId, context.menuName);
    },

    // ------------------------------------------------------------------Subet drag & drop click event--------------------------------------------------------------------------------------------------------//
    
    onSubnetDropClick : function (event)
    {
        if(event)
        {
            event.event.preventDefault();

            var context = event.sender.options.prefix;

            var subnetID = context.subnetId;

            var categoryID = context.categoryId;

            var modal = context.kendoModal;

            var formData = new FormData();

            formData.append('subnetId', subnetID);

            formData.append('categoryId', categoryID);

            loaderUtil.showModalLoader();

            appManager.executeFileRequest({url: '/subnetCategory/',type:'POST',callback:leftPanel.afterSubnetDropped,container:modal, params:formData});
        }
    },

    // -------------------------------------------------------------------------After subnet drop operations-------------------------------------------------------------------------------------------------//
    
    afterSubnetDropped : function (context)
    {
        var modal = context.container;

        modal.data("kendoWindow").close();

        if(context.json.success == true)
        {
            notification.showNotification({notificationTitle:context.json.message,notificationType:"success"});

            widgetRenderManager.renderWidgetAfterSubnetOperation();
        }
        else
        {
            notification.showNotification({notificationTitle:context.json.message,notificationType:"error"});
        }
        loaderUtil.hideModalLoader();
    },

    // -------------------------------------------------------------------------Treeview action popup-------------------------------------------------------------------------------------------------//
    
    openEditingPopup : function (event)
    {
        if(event)
        {
            event.preventDefault();

            if(appManager.validatePermission() == true)
            {
                var target = event.currentTarget;

                var id = $(target).data('uid');

                var isSupernet = event.data.isSupernet;

                if (isSupernet != undefined)
                {
                    if (isSupernet === 'yes')
                    {
                        leftPanel.popupContent.setOptions({
                            anchor: $(target),
                            position: "bottom"
                        });

                        $("#renameCategory").hide();

                        $("#deleteCategory").hide();

                        $("#deleteSupernetCategory").show();

                        $("#deleteSupernetCategory").attr('data-id',id);
                    }
                    else if (isSupernet === 'no')
                    {
                        leftPanel.popupContent.setOptions({
                            anchor: $(target),
                            position: "center left"
                        });

                        $("#deleteSupernetCategory").hide();

                        $("#renameCategory").show();

                        $("#deleteCategory").show();

                        $("#renameCategory").attr('data-id',id);

                        $("#deleteCategory").attr('data-id',id);

                    }
                }

                leftPanel.popupContent.open();

                leftPanel.popupContent.position();
            }
        }
    },
    
    // -----------------------------------------------------------------------------Category Edit modal---------------------------------------------------------------------------------------------//
    
    renameCategory : function (event)
    {
        if(event)
        {
            event.preventDefault();

            leftPanel.popupContent.close();

            var categoryModal = $("#addModal");

            categoryModal.width('275px');

            var categoryId = $(event.currentTarget).attr('data-id');

            flux.kendoWindowSetOption({height:'155px',kendoWindow:categoryModal});

            $('#addModal_wnd_title').html('Edit Category');

            categoryModal.data("kendoWindow").content('<form id="editCategoryForm"><div class="content-box-grid-panel margin-t-20" id="importType"><div class="row"><div class="col-xs-12 col-md-12 col-lg-12"> <label for="categoryName">Rename</label> <input id="categoryName" name="categoryName" type="text" class="k-textbox" required validationMessage="Category Name required" maxlength="25"/></div></div></div><div class="footer-box-grid-boxs margin-t-10 align-right"><div class="bottom-form-panel"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="editCategory">Update</button> <button class="k-button k-button-icontext k-grid-cancel float-l" id="cancelCategory">Cancel</button></div></div></form>');

            flux.bindEvent({element:'editCategory',kendoModal:categoryModal,categoryId:categoryId},leftPanel.editCategoryEntity);

            flux.bindKendoButtonClickEvent({element : 'cancelCategory',kendoModal:categoryModal},flux.closeKendoModal);
        }
    },

    editCategoryEntity : function (event)
    {
        if(event)
        {
            event.preventDefault();

            var kendoEvent = event.data.kendoModal;

            var form = $("#editCategoryForm");

            var categoryId = event.data.categoryId;

            var param =formManager.serializeForm(form);

            var validator = form.kendoValidator().data("kendoValidator");

            if(validator.validate())
            {
                loaderUtil.showModalLoader();

                appManager.executePUTRequest({ url: '/category/'+categoryId, container: kendoEvent, callback: homeManager.afterCategoryAdded, params: param});
            }
        }
    },

    // -----------------------------------------------------------------------Delete category modal---------------------------------------------------------------------------------------------------//
    
    deleteCategory : function (event)
    {
        if(event)
        {
            event.preventDefault();

            leftPanel.popupContent.close();

            var subnetId = $(event.currentTarget).attr('data-id');

            flux.bindKendoModalEvent({container: 'deleteModal',uniqueId:'category',coSequences : 'Warning : Related all subnet addresses will be deleted from this subnet category ! ', title:'Do you really want to delete the selected subnet category?', params:subnetId, closeCallback: flux.closeKendoDeleteModal, callback:leftPanel.categoryDeleteConfirmationPrompt});
        }
    },

    categoryDeleteConfirmationPrompt : function (event)
    {
        if(event)
        {
            event.preventDefault();

            var categoryId =  event.data.context.params;

            var container = event.data.context.container;

            loaderUtil.showModalLoader();

            loaderUtil.showCentralModalLoader(appConstant.CategoryDeleteMessage);

            $("#"+container).data('kendoWindow').close();

            appManager.executeDELETERequest({url: '/category/'+categoryId, container:container, eventId:categoryId,callback: leftPanel.afterCategoryDeleted});
        }
    },

    afterCategoryDeleted : function (context)
    {
        if(context)
        {
            if(context.json.success == true)
            {
                homeManager.init();

                notification.showNotification({notificationTitle:context.json.message,notificationType:"success"});
            }
            else
            {
                if(context.json.message == "Scanning is in Progress, So cannot delete the subnet" || context.json.message == "Scanning is in progress, so cannot delete the subnet category" || context.json.message == "Please wait for some time, Import is running")
                {
                    notification.showNotification({notificationTitle:context.json.message,notificationType:"info"});
                }
                else
                {
                    notification.showNotification({notificationTitle:context.json.message,notificationType:"error"});
                }
            }
            loaderUtil.hideCentralModalLoader();

            loaderUtil.hideModalLoader();
        }
    },

    // -----------------------------------------------------------------------Delete category modal---------------------------------------------------------------------------------------------------//

    deleteSupernetCategory : function (event)
    {
        if(event)
        {
            event.preventDefault();

            leftPanel.popupContent.close();

            var supernetId = $(event.currentTarget).attr('data-id');

            flux.bindKendoModalEvent({container: 'deleteModal',uniqueId:'category',coSequences : 'Warning : Selected Supernet Category will be deleted ! ', title:'Do you really want to delete the selected supernet category?', params:supernetId, closeCallback: flux.closeKendoDeleteModal, callback:leftPanel.supernetCategoryDeleteConfirmationPrompt});
        }
    },

    supernetCategoryDeleteConfirmationPrompt : function (event)
    {
        if(event)
        {
            event.preventDefault();

            var supernetCategoryId =  event.data.context.params;

            var container = event.data.context.container;

            loaderUtil.showModalLoader();

            loaderUtil.showCentralModalLoader(appConstant.SupernetCategoryDeleteMessage);

            $("#"+container).data('kendoWindow').close();

            appManager.executeDELETERequest({url: '/removeSupernet/'+supernetCategoryId, container:container, eventId:supernetCategoryId,callback: leftPanel.afterSupernetCategoryDeleted});
        }
    },

    afterSupernetCategoryDeleted : function (context)
    {
        if(context)
        {
            if(context.json.success == true)
            {
                homeManager.init();

                notification.showNotification({notificationTitle:context.json.message,notificationType:"success"});
            }
            else
            {
                notification.showNotification({notificationTitle:context.json.message,notificationType:"error"});
            }
            loaderUtil.hideCentralModalLoader();

            loaderUtil.hideModalLoader();
        }
    },


    // ----------------------------------------------------------------------------Edit subnet modal---------------------------------------------------------------------------------------------//
    
    onEditSubnetClick : function (event)
    {
        if(event)
        {
            event.preventDefault();

            subnetSummary.popupContent.close();

            if(appManager.validatePermission() == true)
            {
                var subnetId;

                var subnet;

                subnetId = $(event.currentTarget).attr('data-uid');

                subnet = $(event.currentTarget).attr('data-name');

                var modal = $("#addModal");

                modal.width('650px');

                flux.kendoWindowSetOption({kendoWindow:modal});

                $('#addModal_wnd_title').html('Edit Subnet');

                modal.data("kendoWindow").content('<form id="updateSubnetForm"> <div class="fixed-height-body-popup-panel margin-t-20 widthFullSpacer"> <div class="row"> <div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <label for="categoryId">Select Category</label> <select id="categoryId" name="categoryId" class="k-dropdown" required validationMessage="Category Name is required"/></div><div class="col-xs-12 col-md-12 col-lg-6"> <button id="categoryButton" class="primery-btn margin-t-15" title="Add Category"> <i class="fa fa-plus"></i> Add Category </button> <div id="categoryModal"></div></div></div><div id="subnettype" class="row"><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"><label for="gatewayId">Select Gateway</label><select id="gatewayId" name="gatewayId" class="k-dropdown"/></div><div class="col-xs-12 col-md-12 col-lg-6"><button id="gatewayButton" class="primery-btn margin-t-15" title="Add Gateway"> <i class="fa fa-plus"></i> Add Gateway </button><div id="gatewayModal"></div></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-6"> <label for="subnetAddress">Subnet/Network Address</label> <input id="subnetAddress" name="subnetAddress" type="text" class="k-textbox" maxlength="40" required validationMessage="Subnet Address is required" readonly/></div><div class="col-xs-12 col-md-12 col-lg-6 select-drop-menu"> <label for="subnetMask">Subnet Mask / Prefix </label> <input id="subnetMask" name="subnetMask" class="k-textbox" maxlength="25" type="text" required validationMessage="Subnet Mask is required" readonly/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-6"> <label for="subnetName">Subnet Name</label> <input id="subnetName" name="subnetName" type="text" class="k-textbox" maxlength="50" required validationMessage="Subnet Name is required"/></div><div class="col-xs-12 col-md-12 col-lg-6"> <label for="dnsAddress">DNS Address</label> <input id="dnsAddress" name="dnsAddress" type="text" class="k-textbox" maxlength="25"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-6"> <label for="vlanName">VLAN Name</label> <input id="vlanName" name="vlanName" type="text" class="k-textbox" maxlength="50"/></div><div class="col-xs-12 col-md-12 col-lg-6"> <label for="location">Location</label> <input id="location" name="location" type="text" class="k-textbox" maxlength="50"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12"> <label for="description">Description</label> <input id="description" name="description" type="text" class="k-textbox" maxlength="100"/></div></div><div class="row"> <div class="col-xs-12 col-md-12 col-lg-12 select-drop-menu"> <input type="checkbox" id="scheduleStatus" name="scheduleStatus" class="k-checkbox" value="true"/> <label for="automaticScanning">Automatic Scanning</label> <div id="autoScheduler"></div></div><div class="col-xs-12 col-md-12 col-lg-12"> <input type="checkbox" id="allowIcmp" name="allowIcmp" class="k-checkbox" value="true"/> <label class="k-checkbox-label" for="allowIcmp">To check the IP Availability using ICMP</label> </div><div class="col-xs-12 col-md-12 col-lg-12"> <input type="checkbox" id="allowDns" name="allowDns" class="k-checkbox" value="true"/> <label class="k-checkbox-label" for="allowDns">To perform DNS forward and reverse lookup</label> </div></div></div><div class="footer-box-grid-box margin-t-10 align-right"> <button class="k-button k-button-icontext k-primary k-grid-cancel float-l margin-0" id="editTestSubnet">Test</button> <div class="bottom-form-panel"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="editSubnet">Update</button> <button class="defualt-btn k-button k-grid-cancel" id="editCancelSubnet">Cancel</button></div></div></form>');

                var formContext = $('#updateSubnetForm');

                var categoryList = $("#categoryId");

                flux.getKendoDropDownListURL({dropDownId:categoryList,url:"/category/",dataTextField: "categoryName",dataValueField: "id"});

                var gatewayList = $("#gatewayId");

                flux.getKendoDropDownListURL({dropDownId:gatewayList,url:"/gateway/",dataTextField: "gateway",dataValueField: "id"});

                flux.bindKendoButtonClickEvent({element: 'gatewayButton'}, homeManager.onAddGatewayButtonClick);

                flux.bindElementEvent({event:'change',element:'addModal',selector:'#gatewayId' ,isEditable:true},homeManager.onGatewayChange);

                appManager.initCustomScrollbar({container:$(".fixed-height-body-popup-panel")});

                flux.bindKendoButtonClickEvent({element: 'categoryButton'}, homeManager.onAddCategoryButtonClick);

                appManager.executeGETRequest({url: '/subnet/'+subnetId,container:formContext,callback: formManager.renderForm,params:subnetId,postCallback:homeManager.afterEditSubnetFormRender});

                flux.bindElementEvent({event:'change',element:'addModal',selector:'#scheduleStatus' ,isEditable:true},homeManager.renderAutoSchedulerDropDown);

                flux.bindKendoButtonClickEvent({element: 'editSubnet',kendoModal:modal,editId:subnetId},homeManager.onEditSubnetEntity);

                flux.bindKendoButtonClickEvent({element: 'editTestSubnet',container:'updateSubnetForm'},homeManager.onTestSubnetButtonClick);

                flux.bindKendoButtonClickEvent({element : 'editCancelSubnet',kendoModal:modal},flux.closeKendoModal);
            }
        }
    },

    // ------------------------------------------------------------------------Subnet Delete modal--------------------------------------------------------------------------------------------------//
    
    removeSubnet : function (event)
    {
        if(event)
        {
            event.preventDefault();

            subnetSummary.popupContent.close();

            if(appManager.validatePermission() == true)
            {
                var subnetId = $(event.currentTarget).attr('data-uid');

                var subnet = $(event.currentTarget).attr('data-name');

                var deleteSubnet = "Warning: Related IP Addresses will be removed after deleting the subnet !";

                flux.bindKendoModalEvent({container: 'deleteModal',uniqueId :'deleteSubnet',coSequences:deleteSubnet,title:'Do you really want to delete the selected subnet?', params:subnetId,scopeName:subnet, closeCallback: flux.closeKendoDeleteModal, callback:leftPanel.deleteConfirmationPrompt});
            }
        }
    },

    deleteConfirmationPrompt : function (event)
    {
        if(event)
        {
            event.preventDefault();

            var context = event.data.context;

            var subnetId =  context.params;

            var subnet = context.scopeName;

            var container = context.container;

            loaderUtil.showModalLoader();

            loaderUtil.showCentralModalLoader(appConstant.SubnetDeleteMessage);

            $("#"+container).data('kendoWindow').close();

            appManager.executeDELETERequest({url: '/subnet/'+subnetId, eventId:subnetId,eventName:subnet,container:container, callback: leftPanel.afterSubnetAddressDeleted});
        }
    },
    
    afterSubnetAddressDeleted : function (context)
    {
        if(context)
        {
            if(context.json.success == true)
            {
                if(context.eventId == navigationManager.getUrlParameter("subnetId") || context.eventName == navigationManager.getUrlParameter("subnet"))
                {
                    homeManager.init();
                }
                else
                {
                    widgetRenderManager.renderWidgetAfterSubnetOperation();
                }

                notification.showNotification({notificationTitle:context.json.message,notificationType:"success"});
            }
            else
            {
                if(context.json.message == "Scanning is in Progress, So cannot delete the subnet" || context.json.message == "Scanning is in progress, so cannot delete the subnet category" || context.json.message == "Please wait for some time, Import is running")
                {
                    notification.showNotification({notificationTitle:context.json.message,notificationType:"info"});
                }
                else
                {
                    notification.showNotification({notificationTitle:context.json.message,notificationType:"error"});
                }
            }
            loaderUtil.hideCentralModalLoader();

            loaderUtil.hideModalLoader();
        }
    },

    // -------------------------------------------------------------------------Reinit treeview-------------------------------------------------------------------------------------------------//

    initTreeView : function ()
    {
        var treeView = $("#categoryView").data('kendoTreeView');

        if(treeView !== undefined && treeView.dataSource !== undefined)
        {
            treeView.dataSource.read(1);

            var supernetTreeView = $("#supernetView").data('kendoTreeView');

            supernetTreeView.dataSource.read(1);
        }
    },

    // --------------------------------------------------------------------------Add treeview active class------------------------------------------------------------------------------------------------//

    setActiveMenu : function (eventId, element)
    {
        var leftPanel = $("#leftPanel");

        leftPanel.find('span.active').removeClass("k-state-selected");

        leftPanel.find("span:has(span[data-name='"+element+"'])").addClass("k-state-selected");

        var treeViewName = $("#"+eventId);

        var item = treeViewName.find("span:has(span[data-name='"+element+"'])").closest('ul').closest('li');

        treeViewName.data('kendoTreeView').expand(item);
    }
};