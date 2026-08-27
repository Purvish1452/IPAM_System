var globalSearch =
    {
        GlobalSearchPage : 'globalSearchPage', SerachSubnetIPAddress:'searchSubnetIPAddress',

        // ---------------------------------------------------------------------Init search value-----------------------------------------------------------------------------------------------------//
        
        renderGlobalSearch : function (searchValue,breadCrumbNavigation)
        {
            loaderUtil.showModalLoader();

            loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

            var searchField = "'"+searchValue+"'";

            $("#header_panel").html('<div class="title-inner-box">" Search results for '+searchField+' " </div>');

            $("#container-panel").html('<div id="leftPanel" class="left-panel stickyScrollLeft"></div><div id="globalSearchPage" class="content-panel"></div><div id="right-panel" class="right-panel stickyScrollRight"></div>');

            navigationManager.addHistory("navigation=globalSearch~~searchField="+searchValue+"~~breadCrumbNavigation="+breadCrumbNavigation);

            appManager.renderHTML(globalSearch.GlobalSearchPage, $("#globalSearchPage"),undefined);

            appManager.renderLeftRightPanel(null);

            appManager.toggleContentPanel();

            globalSearch.loadSubnetIPAddresses({eventId:searchValue,breadCrumbMenu:breadCrumbNavigation});
        },

        // -------------------------------------------------------------------------Load global search value grid-------------------------------------------------------------------------------------------------//
        
        loadSubnetIPAddresses : function (context)
        {
            if(context)
            {
                var id = context.eventId;

                var gridId = $('#'+globalSearch.SerachSubnetIPAddress);

                var callbackContexts = {

                    Read: function (options)
                    {
                        var formData = new FormData();

                        formData.append('searchParam', id);

                        appManager.executeFileRequest({url: '/search/',type: 'POST',container:options, callback: globalSearch.renderGlobalSearchValueGrid,params:formData});
                    },
                    EventId: globalSearch.SerachSubnetIPAddress,
                    PageSize: 20,
                    pageable: {
                        refresh: true,
                        pageSizes: [10,20,50,100],
                        buttonCount: 10
                    },
                    DataType: 'json',
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
                                subnetAddress:{type:"string"},
                                ipAddress: {type: "string"},
                                status: {type: "string"},
                                macAddress: {type: "string"},
                                deviceType: {type: "string"},
                                authenticity: {type: "string"},
                                ipToDns: {type: "string"},
                                dnsToIp: {type: "string"},
                                subnetName:{type:"string"},
                                lastAliveTime: {type: "string"}
                            }
                        }
                    },
                    Fields: [
                        {
                            field: "ipAddress",
                            template: "<a data-uid='#: subnetId.id #' data-id='#: id#' data-link='ipAddress' data-value='#: ipAddress #' data-name='#: subnetId.subnetAddress #' title='#:ipAddress#'>#: ipAddress #</a>",
                            title: "IP Address",
                            width:'10%'
                        },
                        {field: "status", width:"8%", title: "Status",template:"#if(status == 'Available'){#<i class='fa fa-circle normal-v'></i><span>#:status#</span>#}else if(status == 'Used'){#<i class='fa fa-circle warning-v'></i><span>#:status#</span>#}if(status == 'Transient'){#<i class='fa fa-circle transient-v'></i><span>#:status#</span>#}else if(status == 'Reserved'){#<i class='fa fa-circle reserved-v'></i><span>#:status#</span>#}#"},
                        {
                            field: "subnetName",
                            template: "<a data-uid='#: subnetId.id #' data-link='subnetAddress' data-name='#: subnetName #' title='#: subnetName #'>#: subnetId.subnetName #</a>",
                            title: "Scope"
                        },
                        {field: "macAddress", width:'12%',title: "Mac Address",template:'# if (macAddress) { # <span title="#:macAddress#">#: macAddress # </span># } else { #<span></span># } #'},
                        {field: "deviceType", width:'12%',title: "Vendor",template:'# if (deviceType) { # <span title="#:deviceType#">#: deviceType # </span># } else { #<span></span># } #'},
                        {field: "ipToDns",width:'10%', title: "IP To DNS",template:'# if (ipToDns) { # <span title="#:ipToDns#">#: ipToDns # </span># } else { #<span></span># } #'},
                        {field: "dnsToIp",width:'10%', title: "DNS To IP",template:'# if (dnsToIp) { # <span title="#:dnsToIp#">#: dnsToIp # </span># } else { #<span></span># } #'},
                        {field: "authenticity",width:'10%', title: "Authenticity",template:'# if (authenticity) { # <span title="#:authenticity#">#: authenticity # </span># } else { #<span></span># } #'},
                        {field:"lastAliveTime",title:"Last Alive Time",width:'13%',template:'# if (lastAliveTime) { # <span title="#:lastAliveTime#">#: lastAliveTime # </span># } else { #<span></span># } #'}
                    ],
                    sortable: true
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

                flux.bindEvent({element: 'searchSubnetIPAddress', selector: 'a[data-link=ipAddress]',breadCrumbMenu:"globalSearch",searchedValue:context.eventId},ipAddressSummary.loadIPAddressSummary);

                flux.bindEvent({element: 'searchSubnetIPAddress', selector: 'a[data-link=subnetAddress]',breadCrumbMenu:"globalSearch",searchedValue:context.eventId}, subnetSummary.renderSubnetPage);
            }
        },

        // -------------------------------------------------------------------------Render global search grid-------------------------------------------------------------------------------------------------//

        renderGlobalSearchValueGrid :function (context)
        {
            if(context.json.data != null && context.json.success == true)
            {
                var result = context.json.data;

                if(result)
                {
                    context.container.success(result);
                }
            }
            else
            {
                context.container.success("");

                $(".k-grid-content").html(appConstant.NoDataSpan)
            }
            loaderUtil.hideModalLoader();

            loaderUtil.hideCentralModalLoader();
        },

        // ------------------------------------------------------------------------------Navigation--------------------------------------------------------------------------------------------//

        renderGlobalSearchFromURL : function (searchValue,breadCrumbNavigation)
        {
            if(searchValue)
            {
                globalSearch.renderGlobalSearch(searchValue,breadCrumbNavigation);
            }
        }
    };