<%@ page import="com.motadata.traceorg.ipam.util.TraceOrgCommonConstants" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/default-theme-motadata.css">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/favicon.png">
    <title>IPAM Login</title>
</head>

<body class="login">

<!--[if lt IE 10]>
<script>
    if(document.documentMode!= undefined)
    {
        if(document.documentMode  <10)
        {
            document.getElementById('ie-error').innerHTML =
                    '<h1 class="alert alert-danger">The doctype of browser is not supported, please change <b>Document mode</b> or <b>Compatibility view</b><br>' +
                    '<a href="http://msdn.microsoft.com/en-us/library/ie/bg182625%28v=vs.85%29.aspx" target="_blank">Internet Explorer 11</a><br>' +
                    '<a href="http://www.k-state.edu/its/helpdesk/ie10-enable-compatibility-mode.html" target="_blank">Internet Explorer 10</a>' +
                    '</h1>';
        }
    }
    else
    {
        document.getElementById('ie-error').innerHTML = '<h1 class="alert alert-danger">The version of browser is not supported, please update your browser by going to <b>www.microsoft.com/download</b></h1>';
    }
</script>
<![endif]-->

<span id="notification"></span>
<div class="login-main-panel">
    <%--<div id="success"></div>--%>
    <div class="login-box-panel" >
        <div class="login-title-main">
            <h1>IP Address Manager</h1>
        </div>
        <div class="login-middle-panel">
            <div class="logo-brand-login">
                <img src="${pageContext.request.contextPath}/images/logo.png">
            </div>
            <div id="loginContainer">
                <form name="sentMessage" action="/loginUser.html" method="post" id="loginForm">
                    <div class="login-header">Sign In</div>
                    <div class="msg error-msg" id="loginErrorMsg" style="color: red"><c:out value="${message}"/></div>
                    <div class="row control-group">
                        <div class="form-group col-xs-12 floating-label-form-group controls">
                            <label>Username</label>
                            <input type="text" class="form-control" placeholder="User name" id="userName" name="userName" required validationMessage="User Name is required">
                            <p class="help-block text-danger"></p>
                        </div>
                    </div>
                    <div class="row control-group">
                        <div class="form-group col-xs-12 floating-label-form-group controls">
                            <label>Password</label>
                            <input type="password" class="form-control" placeholder="Password" id="password" name="password" required validationMessage="Password is required">
                            <p class="help-block text-danger"></p>
                            <i class="fa fa-eye" id="showPassword"></i>
                        </div>
                    </div>
                    <div class="row control-group">
                        <div class="form-group col-xs-12 floating-label-form-group controls">
                            <a class="forgot-password" id="forgetPassword">Forgot Password?</a>
                        </div>
                    </div>
                    <div id="success"></div>
                    <div class="row">
                        <div class="form-group col-xs-6 reminder-me">
                            <input type="checkbox" id="remember" name="remember" class="k-checkbox">
                            <label class="k-checkbox-label" for="remember">Remember me</label>
                        </div>
                        <div class="form-group col-xs-6 align-right margin-t-10">
                            <button type="submit" class="k-button k-button-icontext k-primary k-grid-update float-r margin-0" style="margin: 0">Sign In</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <div class="login-footer-copy">
            <b>Powered by <%= TraceOrgCommonConstants.WHITE_LABEL%></b>
        </div>
    </div>
</div>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/plugins/jquery-2.1.1.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/plugins/kendo.all.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/materialPreloader.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/SmartNotification.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/loader.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/motadata/login.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/motadata/flux.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/motadata/app.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/motadata/navigation.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/motadata/form.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/motadata/notification.js"></script>


<script type="text/javascript">
    $(document).ready(function ()
    {
        login.init();
    });

</script>
</body>
</html>
<c:remove var="message" scope="session" />