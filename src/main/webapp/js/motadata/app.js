var appManager =
{
    init: function ()
    {
        topManager.init();

        flux.init();

        popupMenu.init();

        navigationManager.doNavigation();

        /*Discovery Header*/
        subnetSummary.initRunningSubnetTracking();

        homeManager.initImportSubnetTracking();

        //intercept browser back and forward button event.. only supported for HTML 5 browser ...

        window.removeEventListener('popstate', flux.onBrowserHistoryButtonClick);

        window.addEventListener("popstate", flux.onBrowserHistoryButtonClick);
    },

    resetWindowSize : function ()
    {
        $height = $(window).height() - 270;

        $('body .nav-panel').css('max-height', $height);
    },

    // ------------------------------------------------------------------------------------- Validation For user role -------------------------------------------------------------------------------------//

    validatePermission: function ()
    {
        var authorized = false;

        $.ajax(
            {
                url: '/validatePermission/',

                beforeSend: function (request) {
                    request.setRequestHeader("accessToken", appManager.getCookie("token"));
                },

                type: "GET",

                cache: false,

                async: false,

                success: function (callbackContext)
                {
                    authorized = true;
                },

                dataType: "json"
            });

        return authorized;
    },

    // ---------------------------------------------------------------------------------------- Check Cookie ----------------------------------------------------------------------------------//

    getCookie : function(token)
    {
        var name = token + "=";

        var decodedCookie = decodeURIComponent(document.cookie);

        var cookie = decodedCookie.split(';');

        for(var i = 0; i < cookie.length; i++)
        {
            var cookies = cookie[i];

            while (cookies.charAt(0) == ' ')
            {
                cookies = cookies.substring(1);
            }

            if (cookies.indexOf(name) == 0)
            {
                return cookies.substring(name.length, cookies.length);
            }
        }
        return "";
    },

    // -------------------------------------------------------------------------------------------- Toggle left right panel and render ------------------------------------------------------------------------------//

    togglePanel : function ()
    {
        var toggleId = $("#container-panel");

        toggleId.removeClass("leftOpenPanel");

        toggleId.removeClass("rightOpenPanel");

        toggleId.removeClass("contentOpenPanel");
    },

    toggleContentPanel : function ()
    {
        var toggleId = $('#container-panel');

        if(toggleId.hasClass('leftOpenPanel'))
        {
            $('#homeLeftArrow').addClass('open');
        }
        if(toggleId.hasClass('rightOpenPanel'))
        {
            $('#homeRightArrow').addClass('open');
        }
    },

    renderLeftRightPanel : function (menuName)
    {
        appManager.renderHTML(homeManager.LeftPanel, $("#leftPanel"), undefined);

        appManager.renderHTML(homeManager.RightPanel, $("#right-panel"), undefined);

        flux.bindKendoButtonClickEvent({element: 'homeLeftArrow'},leftPanel.onLeftArrowClick);

        leftPanel.renderTreeView(menuName);

        flux.bindKendoButtonClickEvent({element: 'homeRightArrow'},rightPanel.onRightArrowClick);

        rightPanel.renderEventDetails();

        flux.bindKendoButtonClickEvent({element: 'subnetButton'}, homeManager.onAddSubnetButtonClick);

        flux.bindKendoButtonClickEvent({element: 'supernetButton'}, homeManager.onAddSupernetButtonClick);
    },

    // ------------------------------------------------------------------------------------------- Render HTMl -------------------------------------------------------------------------------//

    renderHTML: function (page, container, context)
    {
        var htmlPage = htmlRender.getHTML(page);

        if (context)
        {
            if (context.preTask = 'Replace')
            {

                $.each(context, function (key, value) {

                    htmlPage = htmlPage.replace(key, value)

                });
            }
        }
        container.html(htmlPage);

    },

    /////////////////////////////////////////// CUSTOM SCROLLBAR ////////////////////////////////////////////////////////////////////////////////////////////

    initCustomScrollbar: function (context)
    {
        // for report right panel width scroll issue
        if(context.selector)
        {
            context.container.mCustomScrollbar({

                theme: "minimal-dark",

                scrollInertia:0.2,

                axis:"y",

                scrollButtons: {
                    enable: context.scrollButtons
                }

            });
        }
        else
        {

            context.container.mCustomScrollbar({

                theme: "minimal-dark",

                scrollInertia:0.2,

                axis:"yx",

                // For width toggle issue set 100% instead of auto
                setWidth: "100%",

                scrollButtons: {
                    enable: context.scrollButtons
                }

            });
        }

    },

    // -------------------------------------------------------------------------------------------- AjaX call ------------------------------------------------------------------------------//

    executeGETRequest: function (request)
    {
        $.ajax(
            {
                url: request.url,

                beforeSend: function (request) {
                    request.setRequestHeader("accessToken", appManager.getCookie("token"));
                },

                type: "GET",

                contentType: "application/json",

                cache: false,

                data: request.params,

                timeout: 600000,

                success: function (json) {
                    var callbacks;

                    if (request.callback != undefined)
                    {
                        callbacks = $.Callbacks();

                        callbacks.add(request.callback);

                        request.json = json;

                        callbacks.fire(request);

                        callbacks.remove(request.callback);
                    }
                },
                error: function (json)
                {
                    if(json && json.responseJSON && json.responseJSON.message==='Access is denied')
                    {
                        loaderUtil.hideCentralModalLoader();

                        loaderUtil.hideModalLoader();

                        notification.showNotification({
                            notificationTitle: "Permission denied. Please contact the administrator.",
                            notificationType: "error"
                        });
                    }

                },

                dataType: "json"
            });
    },

    executePOSTRequest: function (request)
    {
        $.ajax({
            url: request.url,

            beforeSend: function (request)
            {
                request.setRequestHeader("accessToken", appManager.getCookie("token"));
            },

            type: "POST",

            contentType: "application/json",

            dataType: "json",

            cache: false,

            data: JSON.stringify(request.params),

            timeout: 600000,

            success: function (json)
            {
                var callbacks;

                if (request.callback != undefined)
                {
                    callbacks = $.Callbacks();

                    callbacks.add(request.callback);

                    request.json = json;

                    callbacks.fire(request);

                    callbacks.remove(request.callback);

                }

            },
            error: function (json)
            {
                if(json && json.responseJSON && json.responseJSON.message==='Access is denied')
                {
                    loaderUtil.hideCentralModalLoader();

                    loaderUtil.hideModalLoader();

                    notification.showNotification({
                        notificationTitle: "Permission denied. Please contact the administrator.",
                        notificationType: "error"
                    });
                }
            },

        });
    },

    executeFileRequest: function (request)
    {
        $.ajax({
            url: request.url,

            beforeSend: function (request)
            {
                request.setRequestHeader("accessToken", appManager.getCookie("token"));
            },

            type: request.type,

            contentType: false,

            processData: false,

            cache: false,

            data: request.params,

            timeout: 600000,

            success: function (json)
            {
                var callbacks;

                if (request.callback != undefined)
                {
                    callbacks = $.Callbacks();

                    callbacks.add(request.callback);

                    request.json = json;

                    callbacks.fire(request);

                    callbacks.remove(request.callback);
                }
            },
            error: function (json)
            {
                if(json && json.responseJSON && json.responseJSON.message==='Access is denied')
                {
                    loaderUtil.hideCentralModalLoader();

                    loaderUtil.hideModalLoader();

                    notification.showNotification({
                        notificationTitle: "Permission denied. Please contact the administrator.",
                        notificationType: "error"
                    });
                }
            },
        });
    },

    executePUTRequest: function (request)
    {
        $.ajax({
            url: request.url,

            beforeSend: function (request) {
                request.setRequestHeader("accessToken", appManager.getCookie("token"));
            },

            type: "PUT",

            contentType: "application/json",

            dataType: "json",

            cache: false,

            data: JSON.stringify(request.params),

            timeout: 600000,

            success: function (json) {
                var callbacks;

                if (request.callback != undefined) {
                    callbacks = $.Callbacks();

                    callbacks.add(request.callback);

                    request.json = json;

                    callbacks.fire(request);

                    callbacks.remove(request.callback);

                }
            },
            error: function (json)
            {
                if(json && json.responseJSON && json.responseJSON.message==='Access is denied')
                {
                    loaderUtil.hideCentralModalLoader();

                    loaderUtil.hideModalLoader();

                    notification.showNotification({
                        notificationTitle: "Permission denied. Please contact the administrator.",
                        notificationType: "error"
                    });
                }
            },
        });
    },

    executeDELETERequest: function (request)
    {
        $.ajax({
            url: request.url,

            beforeSend: function (request) {
                request.setRequestHeader("accessToken", appManager.getCookie("token"));
            },

            type: "DELETE",

            contentType: "application/json",

            dataType: "json",

            cache: false,

            data: request.params,

            timeout: 600000,

            success: function (json) {
                var callbacks;

                if (request.callback != undefined) {
                    callbacks = $.Callbacks();

                    callbacks.add(request.callback);

                    request.json = json;

                    callbacks.fire(request);

                    callbacks.remove(request.callback);

                }
            },
            error: function (json)
            {
                if(json && json.responseJSON && json.responseJSON.message==='Access is denied')
                {
                    loaderUtil.hideCentralModalLoader();

                    loaderUtil.hideModalLoader();

                    notification.showNotification({
                        notificationTitle: "Permission denied. Please contact the administrator.",
                        notificationType: "error"
                    });
                }
            }
        });
    },

    formatDate: function (date)
    {
        if(date)
        {
            var date = new Date(date);

            return date.getFullYear() + "-" + appManager.checkLeadingZero(date.getMonth() + 1) + "-" + appManager.checkLeadingZero(date.getDate()) + " " + appManager.checkLeadingZero(date.getHours()) + ":" + appManager.checkLeadingZero(date.getMinutes()) + ":" + appManager.checkLeadingZero(date.getSeconds());
        }
        else
        {
            return "";
        }
    },

    checkLeadingZero : function (context)
    {
        return (context < 10 ? '0' : '') + context;
    }
};