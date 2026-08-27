var reBrandingManager =
{
    // -------------------------------------------------------------------------Rebranding init-------------------------------------------------------------------------------------------------//

    init : function ()
    {
        loaderUtil.showModalLoader();

        $("#settingMenuGrid").html('<div class="container-fluid"><div class="content-section-panel"><div class="row"><div class="col-xs-12 col-md-12 col-lg-4"><div class="datatable-grid-panel"><div class="widget-main-box"><div class="widget-header-box"><div class="widget-header-title"> Rebranding</div></div><form id="rebrandingForm"><div class="content-box-grid-panel"><div class="row"><div class="col-xs-12 col-md-12 col-lg-12"> <label for="productName">Product Name</label> <input id="productName" name="productName" type="text" class="k-textbox" required validationMessage="Product Name is required" maxlength="50"/></div></div><div class="row"><div class="col-xs-12 col-md-12 col-lg-12 "> <label for="brandLogo">Header Image</label> <div class=""><div class="file-title"></div><input class="upload-input-file" type="file" id="brandLogo" name="brandLogo" accept=".jpg,.jpeg,.png" required validationMessage="Image file is required"/><i class="fa fa-folder-open-o"></i></div></div></div></div><div class="footer-box-grid-panel margin-t-10 align-right"><div class="bottom-form-panel"> <button class="k-button k-button-icontext k-primary k-grid-update float-r" id="brandAddButton">Upload</button> <button class="k-button k-button-icontext k-grid-cancel" id="brandCancelButton">Reset</button></div></div></form></div></div></div></div></div></div>');

        flux.bindKendoButtonClickEvent({element: 'brandAddButton'},reBrandingManager.onBrandAddButtonClick);

        flux.bindKendoButtonClickEvent({element:'brandCancelButton'},reBrandingManager.onBrandCancelButtonClik);

        loaderUtil.hideModalLoader();
    },

    // ---------------------------------------------------------------------------Add brand details event-----------------------------------------------------------------------------------------------//

    onBrandAddButtonClick : function (event)
    {
        if(event)
        {
            event.event.preventDefault();

            var form = $("#rebrandingForm");

            var validator= form.kendoValidator().data("kendoValidator");

            if(validator.validate())
            {
                var formData = new FormData();

                formData.append('brandLogo', $("#brandLogo")[0].files[0]);

                formData.append('productName', $("#productName").val());

                loaderUtil.showModalLoader();

                appManager.executeFileRequest({url: '/brand/1', type: 'PUT',container: form,  callback: reBrandingManager.afterBrandingAddedOrUpdated, params: formData});
            }
        }
    },

    // -------------------------------------------------------------------------After branding operation-------------------------------------------------------------------------------------------------//

    afterBrandingAddedOrUpdated : function (callbackContexts)
    {
        if(callbackContexts)
        {
            if(callbackContexts.json.success == true)
            {
                notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"success"});
            }
            else
            {
                notification.showNotification({notificationTitle: callbackContexts.json.message, notificationType:"error"});
            }
            loaderUtil.showCentralModalLoader();

            location.reload();

            loaderUtil.hideModalLoader();

            loaderUtil.hideCentralModalLoader();
        }
    },

    // ---------------------------------------------------------------------------Reinit branding on cancel btn click-----------------------------------------------------------------------------------------------//

    onBrandCancelButtonClik : function (event)
    {
        if(event)
        {
            event.event.preventDefault();

            reBrandingManager.init();
        }
    }
};