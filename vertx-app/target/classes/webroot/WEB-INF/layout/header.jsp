<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@page session="true"%>
<header id="header">
    <div id="leftHeader" class="company-panel">
        <a title="Home">IP Address Manager</a>
    </div>
    <div class="logo-panel" id="centralHeader">
        <a href="javascript:void(0);" style="cursor: default;"><img src="${pageContext.request.contextPath}/images/logo.png"></a>
        <span class="logo-slogan" title="${brandName}">${brandName}</span>
    </div>
    <div class="header-user-panel">
        <ul class="header-navbar">
            <li>
                <div class="search-box"> <i class="icon-magnifier icons"></i> <input class="input-box searchFilter" id="globalSearch" placeholder="Search" type="text" maxlength="20"></div>
            </li>
            <li id="alertsMenu">
                <a href="javascript:void(0);" data-value="alerts" data-original-title="Alerts" title="Alerts"><i class="icon-bell icons"></i></a>
            </li>
            <li id="eventLogMenu">
                <a href="javascript:void(0);" data-value="eventLog" data-original-title="Notifications" title="Event Notifications"><i class="icon-event icons"></i></a>
            </li>
            <li id="reportsMenu">
                <a id="add-grid" href="javascript:void(0);" data-value="reports" title="Reports"><i class="icon-docs icons"></i></a>
            </li>
            <li id="rogueDetectionMenu">
                <a href="javascript:void(0);" data-value="rogueDetection" data-original-title="RogueDetection" title="Rogue Detection"><i class="icon-shield icons"></i></a>
            </li>
            <li id="ipRequestsMenu">
                <a href="javascript:void(0);" data-value="ipRequests" data-original-title="IpRequests" title="IP Requests">
                    <i class="fa fa-globe" aria-hidden="true" style="font-size: 24px; position: relative;"></i>
                </a>
            </li>

            <li id="settingsMenu">
                <a href="javascript:void(0);" data-value="settings" data-original-title="Settings" title="Settings" id="settingOption" data-property="dhcpManagement"><i class="icon-settings icons"></i></a>
            </li>
            <a class="logout" href="/logout.html" data-value="logout" data-original-title="Logout" title="LogOut"><i class="fa fa-power-off cursor-pointer"></i></a>
        </ul>
    </div>
</header>
<section id="header_panel" class="title-panel stickyScrollHeader"></section>

<script>

    function getAuthoritiesFromCookie()
    {
        let authoritiesCookie = document.cookie.split(';').find(cookie => cookie.trim().startsWith('authorities='));
        const roleRegex = /PERM_[A-Z_]+(?:\s[A-Z_]+)?/g;
        const roles = authoritiesCookie.match(roleRegex);
        return roles;
    }

    function hasRole(role)
    {
        const authorities = getAuthoritiesFromCookie();
        return authorities.includes(role);
    }

    document.addEventListener('DOMContentLoaded', function()
    {
        if (!hasRole('PERM_ALERTS_READ') && !hasRole('PERM_ALERTS_WRITE'))
        {
            document.getElementById('alertsMenu').style.display = 'none';
        }
        if (!hasRole('PERM_EVENT NOTIFICATIONS_READ') && !hasRole('PERM_EVENT NOTIFICATIONS_WRITE'))
        {
            document.getElementById('eventLogMenu').style.display = 'none';
        }

        if (!hasRole('PERM_REPORTS_READ') && !hasRole('PERM_REPORTS_WRITE'))
        {
            document.getElementById('reportsMenu').style.display = 'none';
        }
        if (!hasRole('PERM_ROGUE DETECTION_READ') && !hasRole('PERM_ROGUE DETECTION_WRITE'))
        {
            document.getElementById('rogueDetectionMenu').style.display = 'none';
        }

        if (!hasRole('PERM_SETTINGS_READ') && !hasRole('PERM_SETTINGS_WRITE'))
        {
            document.getElementById('settingsMenu').style.display = 'none';
        }

        if (!hasRole('PERM_IP REQUESTS_READ') && !hasRole('PERM_IP REQUESTS_WRITE'))
        {
            document.getElementById('ipRequestsMenu').style.display = 'none';
        }
    });
</script>