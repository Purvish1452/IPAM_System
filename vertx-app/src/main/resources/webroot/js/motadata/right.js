var rightPanel =
{
    Severity:{critical:0,warning:1,transient:2},

    // ---------------------------------------------------------Open right panel-----------------------------------------------------------------------------------------------------------------//

    onRightArrowClick: function (event)
    {
        var toggleId = $("#container-panel");

        toggleId.removeClass("contentOpenPanel");

        if(toggleId.hasClass('rightOpenPanel'))
        {
            toggleId.removeClass('rightOpenPanel');
        }
        else
        {
            toggleId.addClass('rightOpenPanel')
        }

        if(toggleId.attr('class')===('leftOpenPanel rightOpenPanel'))
        {
            toggleId.addClass("contentOpenPanel")
        }

        $('.rightArrow').toggleClass('open');
    },

    // ------------------------------------------------------------Render right panel event data--------------------------------------------------------------------------------------------------------------//

    renderEventDetails:function ()
    {
        appManager.executeGETRequest({url: '/topEvent/',callback:rightPanel.renderRightPanel});
    },

    renderRightPanel : function (context)
    {
        var html = '<ul>';

        var panel = $('#rightPanelContent');

        var severity;

        if(context.json.data != null && context.json.data != undefined && context.json.success == true)
        {
            panel.mCustomScrollbar("destroy");

            panel.empty();

            $.each( context.json.data, function(key, value)
            {
                switch (value.severity)
                {
                    case rightPanel.Severity.critical:

                        severity = 'critical-v';

                        break;

                    case rightPanel.Severity.warning :

                        severity = 'warning-v';

                        break;

                    case rightPanel.Severity.transient :

                        severity = 'transient-v';

                        break;
                }

                html += '<li><div class="notitication-title"><div class="title-left-box"><i class="icon-question icons '+severity+'"></i> System</div><div class="title-date-time">'+value.timestamp+'</div></div><div class="notitication-content">'+value.eventContext+'</div></li>';

            });

            html += '<div id="showMoreEvents" class="read-more-right-panel"><a id="eventLogList" data-link="eventLogList">Show More</a></div>';

            panel.append(html);

            appManager.resetWindowSize();

            appManager.initCustomScrollbar({container:panel});
        }
        else
        {
            panel.html('<div class="schedule-reports-top-panel" id="reportRightPanelContent">'+appConstant.NoDataSpan+'</div>');
        }

        flux.bindKendoButtonClickEvent({element:'showMoreEvents'},eventLog.init);
    }
};