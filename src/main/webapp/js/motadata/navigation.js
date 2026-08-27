var navigationManager = {

    menu:
        {
            Settings:'settings', Subnet : 'subnetSummary',IpAddressSummary:'ipAddressSummary', GlobalSearch : 'globalSearch', Reports:'reports',EventLog : 'eventLog', Alerts : 'alerts', Home:'home',DHCPServerSummary:'dhcpServerSummary',RogueDetection: "rogueDetection", IndividualRogueDetection: "IndividualRogueDetection",

            UserManagement: 'userManagement',ReBranding:'reBranding',MailServerConfiguration:'mailServerConfiguration',DatabaseMaintenance:'databaseMaintenance', DhcpManagement:'dhcpManagement', ConfigureAlert: 'configureAlert'
        },
    // --------------------------------------------------------------Add history in navigations------------------------------------------------------------------------------------------------------------//

    addHistory:function (hashString)
    {
        if (window.location.hash.substring(1) != btoa(hashString))
        {
            history.pushState("", "", "#" + btoa(hashString));
        }
    },

    // -------------------------------------------------------------Refresh page navigation-------------------------------------------------------------------------------------------------------------//

    doNavigation:function ()
    {
        if (navigationManager.getUrlParameter("navigation") == navigationManager.menu.Settings)
        {
            admin.renderUserManagementFromURL(navigationManager.getUrlParameter("settingsTab"));
        }
        else if (navigationManager.getUrlParameter("navigation") == navigationManager.menu.Subnet)
        {
            subnetSummary.renderSubnetSummaryFromURL(navigationManager.getUrlParameter("subnetId"),navigationManager.getUrlParameter("scopeAddress"),
                navigationManager.getUrlParameter("breadCrumbNavigation"),navigationManager.getUrlParameter("searchField"));
        }
        else if(navigationManager.getUrlParameter("navigation") == navigationManager.menu.IpAddressSummary)
        {
            ipAddressSummary.renderIPAddressSummaryFromURL(navigationManager.getUrlParameter("subnetId"),navigationManager.getUrlParameter("subnetAddress"),navigationManager.getUrlParameter("subnet"),
                navigationManager.getUrlParameter("breadCrumbNavigation"),navigationManager.getUrlParameter("scopeId"),navigationManager.getUrlParameter("searchField"));
        }
        else if(navigationManager.getUrlParameter("navigation") == navigationManager.menu.GlobalSearch)
        {
            globalSearch.renderGlobalSearchFromURL(navigationManager.getUrlParameter("searchField"),navigationManager.getUrlParameter("breadCrumbNavigation"));
        }

        else if(navigationManager.getUrlParameter("navigation") == navigationManager.menu.Reports)
        {
            report.renderReportPageFromUrl();
        }
        else if(navigationManager.getUrlParameter("navigation") == navigationManager.menu.EventLog)
        {
            eventLog.renderEventLogFromURL();
        }
        else if(navigationManager.getUrlParameter("navigation") == navigationManager.menu.Alerts)
        {
            alerts.renderAlertsFromURL();
        }
        else if(navigationManager.getUrlParameter("navigation") == navigationManager.menu.DHCPServerSummary)
        {
            dhcpServerStatistics.renderDHCPStatisticsFromURL(navigationManager.getUrlParameter("serverId"),navigationManager.getUrlParameter("serverAddress"),
                navigationManager.getUrlParameter("breadCrumbNavigation"));
        }
        else if(navigationManager.getUrlParameter("navigation") == navigationManager.menu.RogueDetection)
        {
            rogueDetection.renderRogueDetectionFromURL(undefined);
        }
        else if(navigationManager.getUrlParameter("navigation") == "all" + navigationManager.menu.IndividualRogueDetection)
        {
            rogueDetection.renderRogueDetectionFromURL(undefined);
        }
        else if(navigationManager.getUrlParameter("navigation") == "discovered" + navigationManager.menu.IndividualRogueDetection)
        {
            rogueDetection.renderRogueDetectionFromURL("discovered");
        }
        else if(navigationManager.getUrlParameter("navigation") == "trusted" + navigationManager.menu.IndividualRogueDetection)
        {
            rogueDetection.renderRogueDetectionFromURL("trusted");
        }
        else if(navigationManager.getUrlParameter("navigation") == "rogue" + navigationManager.menu.IndividualRogueDetection)
        {
            rogueDetection.renderRogueDetectionFromURL("rogue");
        }
        else if(navigationManager.getUrlParameter("navigation") == "ipRequests")
        {
            ipRequests.renderIpRequestsFromURL();
        }
        else
        {
            homeManager.init();
        }
    },

    // ----------------------------------------------------------Get navigation parameters----------------------------------------------------------------------------------------------------------------//

    getUrlParameter: function (param)
    {
        var pageURL = window.location.hash.substring(1);

        try{

            pageURL = atob(pageURL);

            var variables = pageURL.split('~~');

            for (var variableIndex = 0; variableIndex < variables.length; variableIndex++)
            {
                var parameterName = variables[variableIndex].split('=');

                if (parameterName[0] == param)
                {
                    return decodeURI(parameterName[1]);
                }
            }
        }
        catch (error)
        {
            //TODO : 404 not found URL
            //            container.replaceContainerContent('<div class="row">  <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">  <div class="row"> <div class="col-sm-12"> <div class="text-center error-box"> <h1 class="error-text-2 bounceInDown animated"> Error 404 <span class="particle particle--c"></span><span class="particle particle--a"></span><span class="particle particle--b"></span></h1> <h2 class="font-xl"><strong><i class="fa fa-fw fa-warning fa-lg text-warning"></i> Page <u>Not</u> Found</strong></h2> <br /> <p class="lead"> The requested URL #'+pageURL+' was not found on the server. </p> </div>  </div>  </div>  </div> ');
        }
    },

    // -------------------------------------------------------------Breadcrumb navigation-------------------------------------------------------------------------------------------------------------//

    doBreadCrumbNavigation : function (event)
    {
        if(event)
        {
            event.preventDefault();

            var eventContext = $(event.currentTarget);

            var subnetId = eventContext.data('uid');

            var subnet = eventContext.data('name');

            var breadCrumbNavigation = eventContext.data('page');

            var searchField = eventContext.data('value');

            if(breadCrumbNavigation == "home" && subnetId == null && subnetId == undefined && subnet == null && subnet == undefined)
            {
                homeManager.init();
            }

            else if(breadCrumbNavigation == "home" && subnetId!=null && subnetId!=undefined && subnet != null && subnet != undefined)
            {
                subnetSummary.renderSubnetSummaryFromURL(subnetId,subnet,breadCrumbNavigation,searchField);
            }

            else if(breadCrumbNavigation == "globalSearch")
            {
                globalSearch.renderGlobalSearch(searchField,breadCrumbNavigation);
            }

            else if(breadCrumbNavigation == "report")
            {
                window.history.back();
                //report.init();
            }
            else if(breadCrumbNavigation == "DHCP Management")
            {
                dhcpManagement.init();
            }
        }
    },

    // -------------------------------------------------------Navigation after discovery success-------------------------------------------------------------------------------------------------------------------//

    renderPageAfterDiscovery : function (statusMessage)
    {
        navigationManager.doNavigation();

        notification.showNotification({notificationTitle: statusMessage, notificationType:"success"});
    }
};
