var admin =
{
    Menu : {
        UserManagement : 'userManagement', ReBranding : 'reBranding', MailServerConfiguration : 'mailServerConfiguration', DatabaseMaintenance : 'databaseMaintenance', DhcpManagement : 'dhcpManagement', GlobalSettings : 'globalSettings', ConfigureAlert : 'configureAlert', Discovery : 'discovery', CustomColumn : "customColumn"
    },

    currentUserRole : {
        Admin : 'ROLE_ADMIN',User: 'ROLE_USER'
    },

    // ----------------------------------------------------------------------------------------Init admin panel----------------------------------------------------------------------------------//

    init : function ()
    {
        loaderUtil.showModalLoader();

        appManager.executeGETRequest({url: '/validatePermission/',callback:admin.getUserRole});

        $("#container-panel").html('<div id="leftPanel" class="left-panel stickyScrollLeft"> <a href="javascript:void(0)" id="homeLeftArrow" class="leftArrow" title="Settings"> <i class="icon-globe icons"></i> <i class="fa fa-arrow-left"></i> </a> <div class="left-inner-box"> <div class="title-part-panel"> <span class="h6Title"> <i class="icon-settings icons"></i> Settings </span> </div> <div class="menu-left-setting"> <ul class=""> <li data-panel="dhcpManagement"><a data-value="dhcpManagement" id="dhcpOption">DHCP Management</a></li> <li data-panel="userManagement"><a data-value="userManagement" id="userOption">User Management</a></li> <li data-panel="mailServerConfiguration"><a data-value="mailServerConfiguration" id="mailOption">Mail Server Configuration</a></li> <li data-panel="reBranding"><a data-value="reBranding" id="reBrandOption">Rebranding</a></li> <li data-panel="databaseMaintenance"><a data-value="databaseMaintenance" id="databaseOption">Database Maintenance</a></li> <li data-panel="configureAlert"><a data-value="configureAlert" id="configureAlertOption">Configure Alert</a></li> <li data-panel="discovery"><a data-value="discovery" id="discoveryOption"> Discovery </a></li> <li data-panel="customColumn"><a data-value="customColumn" id="customColumnOption"> Custom Column </a></li> <li data-panel="globalSettings"><a data-value="globalSettings" id="globalOption">Global Settings</a></li> </ul> </div> </div></div><div id="settingMenuGrid" class="content-panel"></div>');

        appManager.togglePanel();

        flux.bindKendoButtonClickEvent({element: 'leftPanel li a'}, admin.onAdminMenuClick);

        flux.bindKendoButtonClickEvent({element: 'homeLeftArrow'},leftPanel.onLeftArrowClick);

        admin.onAdminMenuClick();

        loaderUtil.hideModalLoader();
    },

    // ----------------------------------------------------------------------------Find current user role & hide admin panel if it is user----------------------------------------------------------------------------------------------//

    getUserRole : function (context)
    {
        if(context)
        {
            var permission= context.json.currentUserRole;

            var leftPanel = $("#leftPanel");

            if(permission == admin.currentUserRole.User)
            {
                leftPanel.find("[data-panel=userManagement]").remove();

                leftPanel.find("[data-panel=mailServerConfiguration]").remove();

                leftPanel.find("[data-panel=configureAlert]").remove();

                leftPanel.find("[data-panel=discovery]").remove();

                leftPanel.find("[data-panel=reBranding]").remove();

                leftPanel.find("[data-panel=databaseMaintenance]").remove();

                leftPanel.find("[data-panel=globalSettings]").remove();
            }
        }
    },

    // -----------------------------------------------------------------------------------------On admin panel li click---------------------------------------------------------------------------------//

    onAdminMenuClick : function (event)
    {
        var adminMenu = '';

        var header = $("#header_panel");

        if(event)
        {
            event.event.preventDefault();

            adminMenu = $(event.event.currentTarget).data('value');
        }
        else if(navigationManager.getUrlParameter("settingsTab"))
        {
            adminMenu = navigationManager.getUrlParameter("settingsTab");
        }

        header.empty();

        header.html('<div class="title-inner-box"> Settings </div>');

        admin.setActiveMenu(adminMenu);

        switch (adminMenu)
        {
            case admin.Menu.DhcpManagement :

                dhcpManagement.init();

                break;

            case admin.Menu.UserManagement :

                userManagement.init();

                navigationManager.addHistory("navigation=settings~~settingsTab=userManagement");

                break;

            case admin.Menu.MailServerConfiguration :

                mailServerConfiguration.init();

                navigationManager.addHistory("navigation=settings~~settingsTab=mailServerConfiguration");

                break;

            case admin.Menu.ReBranding :

                reBrandingManager.init();

                navigationManager.addHistory("navigation=settings~~settingsTab=reBranding");

                break;

            case admin.Menu.ConfigureAlert :

                configureAlertManager.init();

                navigationManager.addHistory("navigation=settings~~settingsTab=configureAlert");

                break;

            case admin.Menu.Discovery :

                discovery.init();

                navigationManager.addHistory("navigation=settings~~settingsTab=discovery");

                break;

            case admin.Menu.DatabaseMaintenance :

                databaseMaintenance.init();

                navigationManager.addHistory("navigation=settings~~settingsTab=databaseMaintenance");

                break;

            case admin.Menu.GlobalSettings :

                globalSettings.init();

                navigationManager.addHistory("navigation=settings~~settingsTab=globalSettings");

                break;

            case admin.Menu.CustomColumn :

                customColumn.init();

                navigationManager.addHistory("navigation=settings~~settingsTab=customColumn");

                break;

        }
    },

    // --------------------------------------------------------------------------------------Navigation------------------------------------------------------------------------------------//

    renderUserManagementFromURL:function ()
    {
        admin.init();

        var menuId = navigationManager.getUrlParameter("navigation");

        topManager.setActiveMenu(menuId);
    },

    // ---------------------------------------------------------------------------------------Add active class--------------------------------------------------------------------------------//

    setActiveMenu : function (element)
    {
        var leftPanel = $("#leftPanel");

        leftPanel.find('li.active').removeClass("active");

        leftPanel.find('li:has(a[data-value=' + element + '])').addClass("active");
    }
};
