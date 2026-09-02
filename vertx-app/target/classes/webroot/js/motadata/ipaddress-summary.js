/**
 * Created by hardik on 27/5/18.
 */
var ipAddressSummary =
    {
        IpAddressSummaryPage : 'ipAddressSummaryPage', SubnetIPSummary : 'subnetIPSummary',

        // -------------------------------------------------------------------------IP address init-------------------------------------------------------------------------------------------------//

        loadIPAddressSummary : function (event,subnetId,subnetAddress,subnet,breadCrumbNavigation,scopeId,searchField)
        {
            var ipAddress;

            var ipAddressId;

            var menuName;

            var breadCrumbMenus;

            var menuId;

            var searchedValue;

            var root = $('#header_panel');

            if(event)
            {
                ipAddress = $(event.currentTarget).data('value');

                ipAddressId = $(event.currentTarget).data("id");

                menuName = $(event.currentTarget).data("name");

                breadCrumbMenus = event.data.breadCrumbMenu;

                menuId = $(event.currentTarget).data("uid");

                searchedValue = event.data.searchedValue;
            }

            if(subnetId && subnetAddress)
            {
                ipAddress = subnetAddress;

                ipAddressId = subnetId;

                menuName = subnet;

                breadCrumbMenus = breadCrumbNavigation;

                menuId = scopeId;

                searchedValue = searchField;
            }

            if(ipAddress && ipAddressId)
            {
                var html='';

                var breadCrumbMenuName;

                if(breadCrumbMenus == "home")
                {
                    breadCrumbMenuName = "Home";

                    html+='<div class="breadcrumb-panel"><ul><li><a href="#" data-page="'+breadCrumbMenus+'" data-link=breadCrumbNavigation>'+breadCrumbMenuName+'</a></li><li><a href="#" data-page="'+breadCrumbMenus+'" data-link=breadCrumbNavigation data-uid="'+menuId+'" data-name="'+menuName+'">'+menuName+'</a></li><li>'+ipAddress+'</li></ul></div>';
                }

                if(breadCrumbMenus == "globalSearch")
                {
                    breadCrumbMenuName = "Global Search";

                    html += '<div class="breadcrumb-panel"><ul><li><a href="#"  data-page="'+breadCrumbMenus+'" data-link=breadCrumbNavigation data-value="'+searchedValue+'">'+breadCrumbMenuName+'</a></li><li>'+ipAddress+'</li></ul></div>';
                }

                if(breadCrumbMenus == "report")
                {
                    breadCrumbMenuName = "Reports";

                    html += '<div class="breadcrumb-panel"><ul><li><a href="#" data-page="'+breadCrumbMenus+'" data-link=breadCrumbNavigation>'+breadCrumbMenuName+'</a></li><li>'+ipAddress+'</li></ul></div>';
                }

                root.empty();

                root.html('<div class="title-inner-box"> Hi, '+$("#userName").val()+'</div>'+html);

                $("#container-panel").html('<div id="leftPanel" class="left-panel stickyScrollLeft"></div><div id="ipAddressSummaryPage" class="content-panel"></div><div id="right-panel" class="right-panel stickyScrollRight"></div>');

                navigationManager.addHistory("navigation=ipAddressSummary~~subnetId="+ipAddressId+"~~subnetAddress="+ipAddress+"~~subnet="+menuName+"~~breadCrumbNavigation="+breadCrumbMenus+"~~scopeId="+menuId+"~~searchField="+searchedValue);

                topManager.setActiveMenu('home');

                appManager.renderHTML(ipAddressSummary.IpAddressSummaryPage, $("#ipAddressSummaryPage"),undefined);

                appManager.renderLeftRightPanel(menuName);

                appManager.toggleContentPanel();

                loaderUtil.showModalLoader();

                loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

                $("#ipName").html(ipAddress);

                $("#subnetIPSummary").html('<ul class="subnet-summary-panel"><li> <label>IP Address</label><p name="ipAddress"></p></li><li> <label>Status</label><p name="status"></p></li><li> <label>Mac Address</label><p name="macAddress"></p></li><li> <label>Device Type</label><p name="deviceType"></p></li><li> <label>IP To DNS</label><p name="ipToDns"></p></li><li> <label>DNS To IP</label><p name="dnsToIp"></p></li><li> <label>Last Alive Time</label><p name="lastAliveTime"></p></li></ul>');

                $("#subnetIPChangeLog").html('<div class="search-left-panel"><div class="search-box"><i class="icon-magnifier icons"></i><input class="input-box searchFilter" id="searchFilter" placeholder="Search" type="text"></div></div><div class="widget-content-box" id="changeLogTable"></div>');

                var gridId = $('#changeLogTable');

                var callbackContexts = {

                    Read: function (options)
                    {
                        appManager.executeGETRequest({url: '/changeLog/' + ipAddressId, container:options, callback:eventLog.renderEventLogGridData});
                    },
                    EventId: 'changeLogTable',
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
                                user:{type:'string'},
                                changelog:{type:'string'},
                                timestamp: {type: "string"},
                                ip: {type: "string"}
                            }
                        }
                    },
                    Fields: [
                        {field: "ip", title: "IP",width:"20%",template:'# if (ip) { # <span title="#:user#">#: ip # </span># } else { #<span></span># } #'},
                        {field: "changelog", title: "Log",width:"40%",template:'# if (changelog) { # <span title="#:changelog#">#: changelog # </span># } else { #<span></span># } #'},
                        {field: "timestamp", title: "Time",width:"20%",template:'# if (timestamp) { # <span title="#:timestamp#">#: timestamp # </span># } else { #<span></span># } #'},
                        {field: "user", title: "User",width:"20%",template:'# if (user) { # <span title="#:user#">#: user # </span># } else { #<span></span># } #'}
                    ],
                    sortable: true,
                    resizable:true
                };

                try {
                    gridId.data().kendoGrid.destroy();
                    gridId.empty();
                }
                catch(err)
                {
                }

                widgetRenderManager.renderGridData(callbackContexts);

                formManager.searchFilter(gridId);

                loaderUtil.hideCentralModalLoader();

                appManager.executeGETRequest({url:'/subnetIp/'+ipAddressId,callback:ipAddressSummary.renderIPAddressContext,params:ipAddressSummary.SubnetIPSummary});
            }
        },

        // --------------------------------------------------------------------------Render IP address summary context ------------------------------------------------------------------------------------------------//

        renderIPAddressContext : function (context)
        {
            var id = $("#"+context.params);

            var result = context.json.data;

            var ipAddressContext = id.find('ul');

            if(result != null && result != undefined)
            {
                if(ipAddressContext)
                {
                    $.each( result, function(key, value){

                        var textValue = value;

                        if(textValue == null || textValue.length == 0 || textValue == "")
                        {
                            textValue = "N/A";
                        }

                        ipAddressContext.find('[name=' + key + ']').text(textValue);

                    });
                }
            }

            loaderUtil.hideModalLoader();

            loaderUtil.hideCentralModalLoader();
        },

        // -----------------------------------------------------------------------------navigation---------------------------------------------------------------------------------------------//

        renderIPAddressSummaryFromURL : function (subnetId,subnetAddress,subnet,breadCrumbNavigation,scopeId,searchField)
        {
            ipAddressSummary.loadIPAddressSummary(null,subnetId,subnetAddress,subnet,breadCrumbNavigation,scopeId,searchField);
        }
    };