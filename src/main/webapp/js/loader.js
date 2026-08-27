var loaderUtil =
{
    modalLoader: undefined, modalCentralLoader: undefined,

    showModalLoader: function (title)
    {
        if(!loaderUtil.modalLoader)
        {
            loaderUtil.modalLoader = new $.materialPreloader({
                position: 'top',
                height: '5px',
                col_1: '#159756',
                col_2: '#da4733',
                col_3: '#3b78e7',
                col_4: '#fdba2c',
                fadeIn: 200,
                fadeOut: 200
            });

        }

        loaderUtil.modalLoader.on();

    },

    hideModalLoader: function ()
    {

        if(loaderUtil.modalLoader)
        {
            //loaderUtil.modalLoader.hide();
            loaderUtil.modalLoader.off();
        }

    },

    showCentralModalLoader:function(title)
    {

        var modalLoader = '<div class="cssload-loader"> <div class="cssload-dot"></div> <div class="cssload-dot"></div> <div class="cssload-dot"></div> <div class="cssload-dot"></div> <div class="cssload-dot"></div> </div>';

        $.SmartMessageBox({

            title: title,

            content: modalLoader,

            buttons: ''

        }, undefined);

        if (loaderUtil.modalCentralLoader)
        {
            loaderUtil.modalCentralLoader.show();
        }

        else
        {
            loaderUtil.modalCentralLoader = $("#MsgBoxBack");
        }
    },

    hideCentralModalLoader: function ()
    {
        $("#MsgBoxBack").hide();
    }
};