var rogueDetection=
    {
        RogueDetectionPage: "rogueDetectionPage", LeftPanel:'rogueDetectionLeftPanel',

        RogueDetectionTable: "rogueDetectionTable", Page: undefined,

        init:function ()
        {
            loaderUtil.showModalLoader();

            loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

            topManager.setActiveMenu('rogueDetection');

            if(rogueDetection.Page === undefined)
            {
                navigationManager.addHistory("navigation=rogueDetection");
            }

            var root = $("#header_panel");

            root.empty();

            root.html('<div class="title-inner-box"> Rogue Detection </div>');

            $("#container-panel").html('<div id="rogueDetectionLeftPanel" class="left-panel stickyScrollLeft"></div><div id="rogueDetectionPage" class="content-panel"></div><div id="rogueDetectionRightPanel" class="right-panel stickyScrollRight"></div>');

            appManager.renderHTML(rogueDetection.RogueDetectionPage, $("#rogueDetectionPage"),undefined);

            appManager.renderHTML(rogueDetection.LeftPanel, $("#rogueDetectionLeftPanel"), undefined);

            $('#rogueIP').hide();

            $('#deleteMultipleIP').hide();

            appManager.togglePanel();

            rogueDetection.loadRogueDetectionPage();

            flux.bindKendoButtonClickEvent({element: 'rogueDetectionLeftArrow'}, leftPanel.onLeftArrowClick);

            flux.bindKendoButtonClickEvent({element: 'importTrustedMACAddresses'}, rogueDetection.onImportTrustedMACAddressesButtonClick);

            flux.bindKendoButtonClickEvent({element:'exportRogueDetectionPdf',exportType:1}, rogueDetection.onRogueDetectionPageExport);

            flux.bindKendoButtonClickEvent({element:'exportRogueDetectionCsv',exportType:2}, rogueDetection.onRogueDetectionPageExport);

            $("#authenticityPage").on("click", "li a", function (event)
            {
                event.preventDefault();

                const panel = $(this).data("value");

                navigationManager.addHistory("navigation=" + panel + "IndividualRogueDetection");

                navigationManager.doNavigation();
            });

            loaderUtil.hideModalLoader();

            loaderUtil.hideCentralModalLoader();

        },

        onImportTrustedMACAddressesButtonClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                if(appManager.validatePermission() == true)
                {
                    var importCSVModal = $("#addModal");

                    importCSVModal.width('750px');

                    flux.kendoWindowSetOption({height:'200px',kendoWindow:importCSVModal});

                    $('#addModal_wnd_title').html('Import Trusted MAC Addresses CSV');

                    importCSVModal.data('kendoWindow').content('<form id="trustedMACAddressCSVForm"><div class="fixed-height-body-popup-panel margin-t-20"><div class="row"><text><tspan>&nbsp;&nbsp;&nbsp;</tspan><b> NOTES : </b> <br> <tspan>&nbsp;&nbsp;&nbsp;</tspan> -> Import the MAC addresses of the devices in your network from a CSV file. <br> <tspan>&nbsp;&nbsp;&nbsp;</tspan> -> All MAC addresses imported here will automatically get marked as "Trusted".</text></div><br><div class="row"><div class="col-xs-12 col-md-12 col-lg-12 "> <label for="trustedMACAddressesCsv">Import Trusted MAC Addresses CSV</label><div class=""><div class="file-title"></div> <input class="upload-input-file" type="file" id="trustedMACAddressesCsv" name="trustedMACAddressesCsv" accept=".csv" required validationMessage="CSV File is required"/> <i class="fa fa-folder-open-o"></i></div> <a class="hyperClick" href="/csv/sampleRougeDetection.csv">Download Sample CSV</a></div></div></div><div class="footer-box-grid-box margin-t-10 align-right"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="importTrustedMACAddressButton">Import</button> <button class="k-button k-button-icontext float-l" id="cancelTrustedMACAddressButton">Cancel</button></div></form>');

                    flux.bindKendoButtonClickEvent({element:'importTrustedMACAddressButton',kendoModal:importCSVModal},rogueDetection.importTrustedMACAddressesCSVFile);

                    flux.bindKendoButtonClickEvent({element : 'cancelTrustedMACAddressButton',kendoModal:importCSVModal},flux.closeKendoModal);
                }
            }
        },

        importTrustedMACAddressesCSVFile : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var kendoEvent = event.sender.options.prefix;

                var kendoModal = kendoEvent.kendoModal;

                var form = $("#trustedMACAddressCSVForm");

                var validator = form.kendoValidator().data("kendoValidator");

                if(validator.validate())
                {
                    var formData = new FormData();

                    formData.append('trustedMACAddressesCsv', $("#trustedMACAddressesCsv")[0].files[0]);

                    loaderUtil.showModalLoader();

                    loaderUtil.showCentralModalLoader(appConstant.ImportMessage);

                    kendoModal.data('kendoWindow').close();

                    appManager.executeFileRequest({url: '/rogueDetectionTrustedMACAddressByCSV/',type: 'POST', container: kendoModal, callback: rogueDetection.afterCSVFileAdded,params: formData});
                }

            }
        },

        afterCSVFileAdded : function (callbackContext)
        {
            if(callbackContext)
            {
                if (callbackContext.json.success == true)
                {
                    navigationManager.doNavigation();

                    notification.showNotification({ notificationTitle: callbackContext.json.message, notificationType: "success"});
                }
                else
                {
                    notification.showNotification({ notificationTitle: callbackContext.json.message, notificationType: "error"});
                }

                loaderUtil.hideCentralModalLoader();

                loaderUtil.hideModalLoader();
            }
        },

        loadRogueDetectionPage : function ()
        {
            var gridId = $('#'+ rogueDetection.RogueDetectionTable);

            $('#rogueIP').hide();

            $('#deleteMultipleIP').hide();

            var callbackContexts =  {
                Read: function (options)
                {
                    loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

                    if(rogueDetection.Page !== undefined)
                    {
                        const title = `${rogueDetection.Page.charAt(0).toUpperCase() + rogueDetection.Page.slice(1)}`;

                        navigationManager.addHistory("navigation=" + rogueDetection.Page + "IndividualRogueDetection");

                        $("#rogueSubtitle").html(`<div class="widget-header-title"><i class="ipam dashboard-icon"></i>${title}</div>`);

                        appManager.executeGETRequest({url: '/rogueDetection/'+rogueDetection.Page,container:options,callback:rogueDetection.renderRogueDetectionGrid});
                    }
                    else
                    {
                        $("#rogueSubtitle").html(`<div class="widget-header-title"><i class="ipam dashboard-icon"></i>All</div>`);

                        appManager.executeGETRequest({url: '/rogueDetection/',container:options,callback:rogueDetection.renderRogueDetectionGrid});
                    }
                },
                EventId: rogueDetection.RogueDetectionTable,
                PageSize: 20,
                pageable: {
                    refresh: true,
                    pageSizes: [10,20,50,100],
                    buttonCount: 10
                },
                group: {
                    field: "ipAddress",
                    dir: "asc"
                },
                resizable:true,
                groupable: true,
                schema: {
                    model: {
                        id: "id",
                        fields: {
                            macAddress: { type: "string"},
                            ipAddress: { type: "string"},
                            discoveredAt: { type: "string" },
                            nicType: { type: "string"},
                            authenticity: {type : "string"}
                        }
                    }
                },
                Fields: [
                    {
                        headerTemplate : "<input type='checkbox' class='deleteMultipleIP' data-value='rogueDetectionTable' name='checkboxFilter' onclick='rogueDetection.selectAllCheckBox(this)' > " +
                            "<label class='k-checkbox-label label-2'>&nbsp;</label>",
                        width : "4%",
                        template: "<input type='checkbox' class='deleteMultipleIP' id='#= id #' data-value='rogueDetectionTable' name='checkboxFilter' onclick='rogueDetection.showHideBtnOnCheckboxClick()'>" +
                            "<label class='k-checkbox-label label-2'>&nbsp;</label>",
                        filterable:false
                    },
                    {field: "macAddress", title: "Mac Address",template:'# if (macAddress) { # <span title="#:macAddress#">#: macAddress # </span># } else { #<span></span># } #'},
                    {field: "ipAddress", title: "IP Address",template:'# if (ipAddress) { # <span title="#:ipAddress#">#: ipAddress # </span># } else { #<span></span># } #'},
                    {field: "discoveredAt", title: "DiscoveredAt",template:'# if (discoveredAt) { # <span title="#:discoveredAt#">#: discoveredAt # </span># } else { #<span></span># } #'},
                    {field: "nicType", title: "NIC Type",template:'# if (nicType) { # <span title="#:nicType#">#: nicType # </span># } else { #<span></span># } #'},
                    {field: "authenticity", title: "Authenticity",template:'# if (authenticity) { # <span title="#:authenticity#">#: authenticity # </span># } else { #<span></span># } #'},
                ]
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

            formManager.searchFilter($("#"+callbackContexts.EventId));

            flux.bindKendoButtonClickEvent({element: 'rogueIP'},rogueDetection.onRogueIPClick);

            flux.bindKendoButtonClickEvent({element: 'deleteMultipleIP'},rogueDetection.onMultipleDeleteButtonClick);

            flux.bindKendoButtonClickEvent({element: 'importTrustedMACAddresses'}, rogueDetection.onImportTrustedMACAddressesButtonClick);
        },

        renderRogueDetectionGrid : function (context)
        {
            if(context!=null && context!=undefined)
            {
                var result = context.json.data;

                if(result!=null && result !=undefined && context.json.success == true)
                {
                    context.container.success(result);

                    loaderUtil.hideCentralModalLoader();
                }
                else
                {
                    context.container.success("");

                    $(".k-grid-content").html(appConstant.NoDataSpan);

                    loaderUtil.hideCentralModalLoader();
                }
            }
        },

        onRogueIPClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var rogueId = event.sender.options.prefix.eventId;

                var grid = $("#"+rogueDetection.RogueDetectionTable).data("kendoGrid");

                var gridId = $("#"+rogueDetection.RogueDetectionTable);

                var checkedAuthenticity = [];

                var checkedIds = [];

                gridId.find("input:checked").each(function(index,value)
                {
                    if(!$.isEmptyObject(value.id) && value.id.length > 0)
                    {
                        var dataItem = grid.dataSource.get(value.id);

                        if (dataItem)
                        {
                            checkedAuthenticity.push({ authenticity: dataItem.authenticity });
                        }

                        checkedIds.push(value.id);
                    }
                });

                if(checkedIds.length > 0)
                {
                    if(appManager.validatePermission() == true)
                    {
                        var modal = $('#addModal');

                        modal.width('660px');

                        flux.kendoWindowSetOption({kendoWindow:modal});

                        $('#addModal_wnd_title').html('Are you sure you want to mark them as?');

                        modal.data("kendoWindow").content('<form id="rogueIPForm"><div class="content-box-grid-panel margin-t-20" id="rogueType"><div class="row"><div class="col-xs-12 col-md-12 col-lg-2"> <input type="radio" name="rogueType" class="k-radio" value="true" checked/> <label class="k-radio-label label-2" for="rogueType">Rogue</label></div><div class="col-xs-12 col-md-12 col-lg-4"> <input type="radio" name="rogueType" class="k-radio" value="false"/> <label class="k-radio-label label-2" for="rogueType ">Trusted</label></div></div></div><div class="footer-box-grid-box margin-t-10 align-right"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="rogueIPAddButton">Add</button> <button class="k-button k-button-icontext float-l" id="rogueIPCancelButton">Cancel</button></div></form>');

                        flux.bindKendoButtonClickEvent({element:'rogueIPAddButton',kendoModal:modal,container:checkedAuthenticity,checkedIds:checkedIds,eventId:rogueId},rogueDetection.addRogueIPStatus);

                        flux.bindKendoButtonClickEvent({element:'rogueIPCancelButton',kendoModal:modal},flux.closeKendoModal);
                    }
                }
                else
                {
                    notification.showNotification({notificationTitle:"No IP Address are selected", notificationType:"info"});
                }
            }
        },

        addRogueIPStatus : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var kendoEvent = event.sender.options.prefix;

                var kendoModal = kendoEvent.kendoModal;

                var addRougeIP = true;

                var checkedAuthenticity = kendoEvent.container;

                var checkedIds = kendoEvent.checkedIds;

                var eventId = kendoEvent.eventId;

                var selectedStatus = $("input[name='rogueType']:checked").val();

                checkedAuthenticity.forEach(function (item)
                {

                    if (item.authenticity === "rogue" && selectedStatus === "true")
                    {
                        notification.showNotification({
                            notificationTitle: "Invalid Selection : The selected MAC is already marked as rogue.",
                            notificationType: "error",
                        });

                        addRougeIP = false;

                        flux.closeKendoModal(event);
                    }

                    if (item.authenticity === "trusted" && selectedStatus === "false")
                    {
                        notification.showNotification({
                            notificationTitle: "Invalid Selection : The selected MAC is already marked as trusted.",
                            notificationType: "error",
                        });

                        addRougeIP = false;

                        flux.closeKendoModal(event);
                    }
                });

                if(addRougeIP)
                {
                    var formData = new FormData();

                    formData.append('status', selectedStatus);

                    formData.append('id', checkedIds);

                    loaderUtil.showModalLoader();

                    loaderUtil.showCentralModalLoader(appConstant.RogueAddMessage);

                    kendoModal.data('kendoWindow').close();

                    appManager.executeFileRequest({url: '/rogueDetectionMarkedAuthenticity/', type:'POST', container: kendoModal, eventId:eventId, callback: rogueDetection.afterEntityAddedOrUpdated, params:formData});
                }
            }
        },

        afterEntityAddedOrUpdated : function (callbackContexts)
        {
            if(callbackContexts)
            {
                navigationManager.doNavigation();

                if(callbackContexts.json.success == true)
                {
                    notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"success"});
                }
                else
                {
                    notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"error"});
                }
                loaderUtil.hideModalLoader();

                loaderUtil.hideCentralModalLoader();
            }
        },

        onMultipleDeleteButtonClick : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var gridId = $("#"+rogueDetection.RogueDetectionTable);

                var checkedId=[];

                var i = 0;

                gridId.find("input:checked").each(function(index,value)
                {
                    if(!$.isEmptyObject(value.id) && value.id.length > 0)
                    {
                        checkedId[i]=value.id;

                        i++;
                    }
                });

                if(checkedId.length > 0)
                {
                    if(appManager.validatePermission() == true)
                    {
                        var deleteIPCoseQuences = "Note: If the selected MAC addresses have an authenticity status of Rogue or Trusted, they will not be removed; instead, their authenticity will be changed to Discovered." ;

                        flux.bindKendoModalEvent({container: 'deleteModal',uniqueId:'deleteMACAddresses',coSequences:deleteIPCoseQuences,title:'Do you really want to delete the selected IP Addresses?', params:checkedId, closeCallback: flux.closeKendoDeleteModal,callback: rogueDetection.deleteConfirmationPrompt});
                    }
                }
                else
                {
                    notification.showNotification({notificationTitle:"No IP Address are selected", notificationType:"info"});
                }
            }
        },

        deleteConfirmationPrompt : function (event)
        {
            if(event)
            {
                event.preventDefault();

                var gridId = $("#"+rogueDetection.RogueDetectionTable);

                var context = event.data.context;

                var checkedId = context.params;

                var container = context.container;

                loaderUtil.showModalLoader();

                appManager.executeDELETERequest({url:'/rogueDetection/'+checkedId,container:container,callback:rogueDetection.afterEntityDeleted, gridId:gridId});
            }
        },

        afterEntityDeleted : function (context)
        {
            if(context)
            {
                var modal = $("#"+context.container).data('kendoWindow');

                var gridId = context.gridId;

                gridId.find('input[type="checkbox"]')[0].checked=false;

                modal.close();

                if(context.json.success == true)
                {
                    navigationManager.doNavigation();

                    notification.showNotification({notificationTitle: context.json.message, notificationType:"success"});
                }
                else
                {
                    notification.showNotification({notificationTitle: context.json.message, notificationType:"error"});
                }

                loaderUtil.hideModalLoader();
            }
        },

        onRogueDetectionPageExport : function (event)
        {
            if(event)
            {
                event.event.preventDefault();

                var context = event.sender.options.prefix;

                var url = navigationManager.getUrlParameter("navigation");

                var param = {};

                var gridId = $("#"+rogueDetection.RogueDetectionTable);

                var checkedId=[];

                var pdfUrl = "/exportRogueDetectionPdf/";

                var csvUrl = "/exportRogueDetectionCSV/";

                var i = 0;

                gridId.find("input:checked").each(function(index,value)
                {
                    if(value.id.length > 0)
                    {
                        checkedId[i]=value.id;

                        i++;
                    }
                });

                if(checkedId.length !== 0)
                {
                    pdfUrl += checkedId;

                    csvUrl += checkedId;
                }
                else
                {
                    param["url"] =  url;
                }

                var type = context.exportType;

                loaderUtil.showModalLoader();

                loaderUtil.showCentralModalLoader(appConstant.ExportMessage);

                if(type == 1)
                {
                    appManager.executeGETRequest({url:pdfUrl, callback:rogueDetection.downloadPdf, params: param});
                }
                else if (type == 2)
                {
                    appManager.executeGETRequest({url:csvUrl, callback:rogueDetection.downloadCsv, params: param});
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
                notification.showNotification({notificationTitle: context.json.message, notificationType:"error"});
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
                notification.showNotification({notificationTitle: context.json.message, notificationType:"error"});
            }

            loaderUtil.hideModalLoader();

            loaderUtil.hideCentralModalLoader();
        },

        selectAllCheckBox : function (event)
        {
            if(event)
            {
                var state = $(event).is(':checked');

                if (state == true)
                {
                    $('.deleteMultipleIP').prop('checked', true);

                    $('#rogueIP').show();
                }
                else
                {
                    $('.deleteMultipleIP').prop('checked', false);

                    $('#rogueIP').hide();
                }
                subnetSummary.showHideBtnOnCheckboxClick();
            }
        },

        showHideBtnOnCheckboxClick : function ()
        {
            if($("input[name='checkboxFilter']:checked").length > 0 )
            {
                $('#rogueIP').show();

                $('#deleteMultipleIP').show();
            }
            else
            {
                $('#rogueIP').hide();

                $('#deleteMultipleIP').hide();
            }
        },

        renderRogueDetectionFromURL : function (individualPage)
        {
            rogueDetection.Page = individualPage;

            rogueDetection.init();
        }
    };