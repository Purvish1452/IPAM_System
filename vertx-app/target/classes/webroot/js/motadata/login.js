/**
 * Created by hardik on 3/7/18.
 */
var login =
{
    formContext :
    {
        formName:'loginForm', container : 'loginContainer', submitButton : 'loginFormSubmitBtn'
    },

    // ----------------------------------------------------------------------Init login page, set value in local storage & bind password show event----------------------------------------------------------------------------------------------------//

    init : function ()
    {
        flux.bindEvent({element: 'loginForm', selector: '#forgetPassword'}, login.onForgotPasswordClick);

        var form = $('#'+login.formContext.formName);

        flux.bindKendoButtonClickEvent({element:'remember'},login.setLocalStorage);

        if (typeof(Storage) !== "undefined" && localStorage.getItem('motadataCredential') != "undefined" && localStorage.getItem('motadataCredential') != null)
        {
            var credential = JSON.parse(localStorage.getItem('motadataCredential'));

            formManager.setElementValue(form, 'userName', credential.userName);

            formManager.setElementValue(form, 'password', credential.password);

            $("[name='remember']").prop("checked", true);
        }

        localStorage.clear();

        $('#showPassword').hover(function () {
            $('#password').attr('type', 'text');
        }, function () {
            $('#password').attr('type', 'password');
        });
    },

    // ---------------------------------------------------------------------Set value as a json in local storage-----------------------------------------------------------------------------------------------------//

    setLocalStorage: function ()
    {
        if (typeof(Storage) !== "undefined")
        {
            var form = $('#'+login.formContext.formName);

            if ($("[name='remember']").prop("checked"))
            {
                var credential = {};

                credential.userName = formManager.getElementByName(form, 'userName').val();

                credential.password = formManager.getElementByName(form, 'password').val();

                localStorage.setItem('motadataCredential', JSON.stringify(credential));
            }
            else
            {
                localStorage.removeItem('motadataCredential');
            }
        }
    },

    // ------------------------------------------------------------------------Forgot password event--------------------------------------------------------------------------------------------------//

    onForgotPasswordClick : function (event)
    {
        event.preventDefault();

        $("#loginErrorMsg").text('');

        $('#'+login.formContext.container).html('<form name="sentMessage" action="/loginUser.html" method="post" id="loginForm"><div class="login-header">Forgot Password</div><fieldset class="errorMessageShow" id="errorMessageShow"></fieldset><div class="row control-group"><div class="form-group col-xs-12 floating-label-form-group controls"> <label>Username</label> <input class="form-control" placeholder="User name" id="userName" name="userName" required="" validationmessage="User Name is required" type="text"><p class="help-block text-danger"></p></div></div><div id="forgotPasswordRows"></div><div class="row control-group"><div class="form-group col-xs-12 floating-label-form-group controls"> <a class="forgot-password" id="rememberedPassword" href="javascript:void(0);">I Remembered My Password!</a></div></div><div id="success"></div><div class="row"><div class="form-group col-xs-12 align-right margin-t-10"> <button class="k-button k-button-icontext k-primary k-grid-update margin-0" type="submit" id="loginFormSubmitBtn" > Generate Verification Code </button></div></div></form>');

        login.clearValidationMessage();

        flux.bindEvent({element: 'loginForm', selector: '#'+login.formContext.submitButton}, login.generateVerificationCode);

        flux.bindEvent({element: 'loginForm', selector: '#rememberedPassword'}, login.onRememberedPasswordClick);
    },

    // -------------------------------------------------------------------------Generate verification code-------------------------------------------------------------------------------------------------//

    generateVerificationCode : function (event)
    {
        event.preventDefault();

        var form = $("#"+login.formContext.formName);

        var validator = form.kendoValidator().data("kendoValidator");

        if (validator.validate())
        {
            var formData = new FormData();

            formData.append('userName',$('#userName').val());

            $('#'+login.formContext.submitButton).css('opacity','0.1');

            appManager.executeFileRequest({url: '/forgotPassword/',type:'POST',container:form,callback:login.afterVerificationCodeEmailed, params:formData});
        }
    },

    // -------------------------------------------------------------------------After verification code emailed-------------------------------------------------------------------------------------------------//

    afterVerificationCodeEmailed : function (context)
    {
        var form = $("#"+login.formContext.formName);

        var button = $('#'+login.formContext.submitButton);

        button.css('opacity','1');

        login.clearValidationMessage();

        if(context.json.success == true)
        {
            login.clearErrorMessages(form);

            login.setSuccessMessage(form, context.json.message);

            formManager.disableElements(context,['userName']);

            button.parent().hide();

            $('#rememberedPassword').parent().hide();

            $('#forgotPasswordRows').html('<div class="row control-group" id="verificationRow"><div class="form-group col-xs-12 floating-label-form-group controls"> <label>Verification Code</label> <input class="form-control" placeholder="Verification Code" id="verificationCode" name="verificationCode" required validationMessage="Verification Code is required" type="text"><p class="help-block text-danger"></p></div><div class="form-group col-xs-12 align-right margin-0"> <button class="k-button k-button-icontext k-primary k-grid-update margin-0" type="submit" id="loginButton" > Add </button></div></div><div class="row control-group" id="clearfix">');

            flux.bindKendoButtonClickEvent({element:'loginButton',container:form},login.afterVerificationCodeAdded);
        }
        else
        {
            login.clearErrorMessages(form);

            login.setErrorMessage(form, context.json.message);
        }
    },

    // ------------------------------------------------------------------------After emailed code submit--------------------------------------------------------------------------------------------------//

    afterVerificationCodeAdded : function (event)
    {
        if(event)
        {
            event.preventDefault();

            login.clearValidationMessage();

            var form = event.sender.options.prefix.container;

            var validator = form.kendoValidator().data("kendoValidator");

            if(validator.validate())
            {
                var formData = new FormData();

                formData.append('userName',$('#userName').val());

                formData.append('verificationCode',$('#verificationCode').val());

                appManager.executeFileRequest({url: '/verifyPasswordToken/',type:'POST',container:form,callback:login.addNewPassword, params:formData});
            }
        }
    },

    // ---------------------------------------------------------------------------Add new password modal-----------------------------------------------------------------------------------------------//

    addNewPassword : function (context)
    {
        if(context)
        {
            var form = context.container;

            login.clearValidationMessage();

            if(context.json.success == true)
            {
                login.clearErrorMessages(form);

                login.setSuccessMessage(form, context.json.message);

                formManager.disableElements(context,['verificationCode']);

                $('#loginButton').parent().hide();

                login.clearValidationMessage();

                $('#clearfix').html('<div class="row"><div class="form-group col-xs-11 floating-label-form-group controls" style="left: 15px;"> <label>New Password</label> <input class="form-control" id="password" name="password" type="password" placeholder="Password" validationMessage="Password is required" required /><p class="help-block text-danger"></p></div></div><div class="row"><div class="form-group col-xs-11 floating-label-form-group controls" style="left: 15px;"> <label>Confirm Password</label> <input class="form-control" id="rePassword" name="Confirm Password" type="password" placeholder="Confirm Password" required /><p class="help-block text-danger"></p></div></div><div class="row"><div class="form-group col-xs-12 align-right margin-t-10"> <button class="k-button k-button-icontext k-primary k-grid-update margin-0" type="submit" id="loginPasswordButton" > Update </button></div></div>');

                flux.bindKendoButtonClickEvent({element:'loginPasswordButton',container:form},login.updateLoginPassword);
            }
            else
            {
                login.clearErrorMessages(form);

                login.setErrorMessage(form, context.json.message);
            }
        }
    },

    // --------------------------------------------------------------------------------Update password------------------------------------------------------------------------------------------//

    updateLoginPassword : function (event)
    {
        if(event)
        {
            event.preventDefault();

            login.clearValidationMessage();

            var form = event.sender.options.prefix.container;

            var validator = form.kendoValidator({
                rules: {
                    verifyPasswords: function(input)
                    {
                        var ret = true;
                        if (input.is("[name=Confirm Password]")) {
                            ret = input.val() === $("#password").val();
                        }
                        return ret;
                    }
                },
                messages: {
                    verifyPasswords: "Passwords do not match!"
                }

            }).data("kendoValidator");

            if(validator.validate())
            {
                var formData = new FormData();

                formData.append('userName',$('#userName').val());

                formData.append('verificationCode',$('#verificationCode').val());

                formData.append('password',$('#password').val());

                appManager.executeFileRequest({url: '/newPassword/',type:'POST',container:form,callback:login.afterPasswordUpdated, params:formData});
            }
        }
    },

    // ----------------------------------------------------------------------------------After password changed----------------------------------------------------------------------------------------//

    afterPasswordUpdated : function (context)
    {
        if(context)
        {
            if(context.json.success == true)
            {
                $('#loginContainer').html('<form name="sentMessage" action="/loginUser.html" method="post" id="loginForm"><div class="login-header">Sign In</div><div class="row control-group"><div class="form-group col-xs-12 floating-label-form-group controls"> <label>Username</label> <input type="text" class="form-control" placeholder="User name" id="userName" name="userName" required validationMessage="User Name is required"><p class="help-block text-danger"></p></div></div><div class="row control-group"><div class="form-group col-xs-12 floating-label-form-group controls"> <label>Password</label> <input type="password" class="form-control" placeholder="Password" id="password" name="password" required validationMessage="Password is required"><p class="help-block text-danger"></p> <i class="fa fa-eye" id="showPassword"></i></div></div><div class="row control-group"><div class="form-group col-xs-12 floating-label-form-group controls"> <a class="forgot-password" id="forgetPassword">Forgot Password?</a></div></div><div id="success"></div><div class="row"><div class="form-group col-xs-6 reminder-me"> <input type="checkbox" id="remember" name="remember" class="k-checkbox"> <label class="k-checkbox-label" for="remember">Remember me</label></div><div class="form-group col-xs-6 align-right margin-t-10"> <button type="submit" class="k-button k-button-icontext k-primary k-grid-update float-r margin-0" style="margin: 0">Sign In</button></div></div></form>');

                login.init();

                var form = $('#'+login.formContext.formName);

                notification.showNotification({notificationTitle:context.json.message,notificationType:"success"});
            }
            else
            {
                notification.showNotification({notificationTitle:context.json.message,notificationType:"error"});
            }
        }
    },

    // ---------------------------------------------------------------------------Clear form error messages-----------------------------------------------------------------------------------------------//

    clearErrorMessages: function (form)
    {
        form.find(".msg").remove();
    },

    // ---------------------------------------------------------------------------Set form success messages-----------------------------------------------------------------------------------------------//

    setSuccessMessage : function (form, message)
    {
        form.find("fieldset").prepend('<div class="msg success-msg">'+ message +'</div>');
    },

    // ---------------------------------------------------------------------------Set form error messages-----------------------------------------------------------------------------------------------//

    setErrorMessage : function (form, message)
    {
        form.find("fieldset").prepend('<div class="msg error-msg">'+ message +'</div>');
    },

    // ---------------------------------------------------------------------------Clear kendo validation messages-----------------------------------------------------------------------------------------------//

    clearValidationMessage : function ()
    {
        $('.k-invalid-msg').hide();
    },

    onRememberedPasswordClick: function (event)
    {
        event.preventDefault();

        $("#loginErrorMsg").text('');

        $('#loginContainer').html('<form name="sentMessage" action="/loginUser.html" method="post" id="loginForm"><div class="login-header">Sign In</div><div class="row control-group"><div class="form-group col-xs-12 floating-label-form-group controls"> <label>Username</label> <input type="text" class="form-control" placeholder="User name" id="userName" name="userName" required validationMessage="User Name is required"><p class="help-block text-danger"></p></div></div><div class="row control-group"><div class="form-group col-xs-12 floating-label-form-group controls"> <label>Password</label> <input type="password" class="form-control" placeholder="Password" id="password" name="password" required validationMessage="Password is required"><p class="help-block text-danger"></p> <i class="fa fa-eye" id="showPassword"></i></div></div><div class="row control-group"><div class="form-group col-xs-12 floating-label-form-group controls"> <a class="forgot-password" id="forgetPassword">Forgot Password?</a></div></div><div id="success"></div><div class="row"><div class="form-group col-xs-6 reminder-me"> <input type="checkbox" id="remember" name="remember" class="k-checkbox"> <label class="k-checkbox-label" for="remember">Remember me</label></div><div class="form-group col-xs-6 align-right margin-t-10"> <button type="submit" class="k-button k-button-icontext k-primary k-grid-update float-r margin-0" style="margin: 0">Sign In</button></div></div></form>');

        login.init();
    }
};