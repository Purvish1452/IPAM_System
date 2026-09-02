/**
 * Created by hardik on 29/5/18.
 */
var notification =
{
    // ---------------------------------------------------------------------------Kendo Notification-----------------------------------------------------------------------------------------------//

    showNotification : function (callbackContexts)
    {
        if(callbackContexts)
        {
            var notification = $("#notification").kendoNotification({
                position: {
                    pinned: false,
                    top: 50,
                    right: 30
                },
                width: 250,
                height: 50,
                autoHideAfter: 5000,
                allowHideAfter: 1000,
                button: true,
                stacking: "down",
                animation: {
                    open: {
                        effects: "slideIn:left"
                    },
                    close: {
                        effects: "slideIn:left",
                        reverse: true
                    }
                }

            }).data("kendoNotification");

            notification.show(callbackContexts.notificationTitle,callbackContexts.notificationType);
        }
    }
};