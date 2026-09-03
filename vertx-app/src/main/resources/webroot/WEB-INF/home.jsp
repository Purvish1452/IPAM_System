<%@taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>

<!DOCTYPE html>
<html lang="en-us">
<head>
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <meta name="description" content="Motadata v${version}">
    <meta name="author" content="Motadata">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

    <title>IPAM v${version}</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/materialPreloader.min.css">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/toggle-motadata.css">

    <c:if test="${cssMode eq 1}">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/default-theme-motadata.css">
    </c:if>
    <c:if test="${cssMode eq 2}">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/black-theme-motadata.css">
    </c:if>

    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/favicon.png">

    <script src="${pageContext.request.contextPath}/js/plugins/jquery-2.1.1.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/plugins/underscore.js"></script>
    <script src="${pageContext.request.contextPath}/js/plugins/jquery.mCustomScrollbar.concat.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/plugins/kendo.all.min.js"></script>

    <script src="${pageContext.request.contextPath}/js/materialPreloader.js"></script>
    <script src="${pageContext.request.contextPath}/js/SmartNotification.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/loader.js"></script>

    <script src="${pageContext.request.contextPath}/js/motadata/login.js"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/navigation.js"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/app.js"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/flux.js"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/popup-menu.js"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/form.js"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/top.js"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/notification.js"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/html-render.js"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/home.js"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/left.js"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/right.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/subnet-summary.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/ipaddress-summary.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/widget.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/widget-render.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/table.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/admin.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/reports.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/database-maintanance.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/dhcp-management.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/dhcp-server-statistics.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/mail-server-configuration.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/re-branding.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/configure-alert.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/user-management.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/global-settings.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/const.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/global-search.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/event-log.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/alerts.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/rogue-detection.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/discovery.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/ip-requests.js?v=4.0.1"></script>
    <script src="${pageContext.request.contextPath}/js/motadata/custom-column.js?v=4.0.1"></script>
</head>

<body>

<div id="wrapper">

    <%--header--%>
    <jsp:include page="layout/header.jsp"></jsp:include>

    <input type="hidden" id="userName" value="${userName}">

    <input type="hidden" id="url" value="${url}">

    <%--container-panel--%>
    <section id="container-panel">
    </section>

    <%--left treeview popup--%>
    <div id="leftTreePopupContent" class="action-dropmenu-panel leftDropDown" style="display: none"><ul><li id="renameCategory" style="cursor: pointer" title="Rename Category">Rename</li><li id="deleteCategory" style="cursor: pointer" title="Delete Category">Delete</li><li id="deleteSupernetCategory" style="cursor: pointer" title="Delete Category">Delete</li></ul></div>

    <%--user grid popup--%>
    <div id="popupContent" class="action-dropmenu-panel" style="display: none"><ul><li id="updatePassword" style="cursor: pointer" title="Update">Update Password</li></ul></div>

    <%--subnet-summary popup--%>
    <div id="subnetSummaryPopupContent" class="action-dropmenu-panel" style="display: none"><ul><li id="editSubnetAddress" style="cursor: pointer" title="Edit Subnet" class="subnetSummaryPopup">Edit Subnet</li><li class="subnetSummaryPopup" id="deleteSubnetAddress" style="cursor: pointer" title="Delete Subnet">Delete Subnet</li></ul></div>

    <%--Dhcp server-summary popup--%>
    <div id="dhcpServerPopupContent" class="action-dropmenu-panel" style="display: none"><ul><li id="scanDhcpServer" style="cursor: pointer" title="Scan DHCP Server"> Scan </li><li id="exportDhcpServerPdf" style="cursor: pointer" title="PDF"> Export PDF </li><li id="exportDhcpServerPng" style="cursor: pointer" title="PNG"> Export PNG </li><li id="exportDhcpServerSvg" style="cursor: pointer" title="SVG"> Export SVG </li></ul></div>

    <%--subnet-grid actions--%>
    <div id="subnetPopupContent" class="action-dropmenu-panel" style="display: none"><ul><li id="exportSubnetPdf" style="cursor: pointer" title="PDF">Export PDF</li></ul></div>

    <%--dhcp-grid actions--%>
    <div id="dhcpPopupContent" class="action-dropmenu-panel" style="display: none"><ul><li id="exportDhcpPdf" data-value="dhcpScopeUtilization" style="cursor: pointer" title="PDF">Export PDF</li></ul></div>

    <%--conflict-grid actions--%>
    <div id="conflictPopupContent" class="action-dropmenu-panel" style="display: none"><ul><li id="exportConflictPdf" data-value="conflictedIP" style="cursor: pointer" title="PDF">Export PDF</li></ul></div>

    <div id="recentlyDiscoveredPopupContent" class="action-dropmenu-panel" style="display: none"><ul><li id="recentlyDiscoveredPdf" data-value="recentlyDiscovered" style="cursor: pointer" title="PDF">Export PDF</li></ul></div>

    <div id="top10SubnetUtilizationPopupContent" class="action-dropmenu-panel" style="display: none"><ul><li id="top10SubnetUtilizationPdf" data-value="top10SubnetUtilization" style="cursor: pointer" title="PDF">Export PDF</li></ul></div>

    <div id="top10CategoryUtilizationPopupContent" class="action-dropmenu-panel" style="display: none"><ul><li id="top10CategoryUtilizationPdf" data-value="top10CategoryUtilization" style="cursor: pointer" title="PDF">Export PDF</li></ul></div>

    <div id="dnsStatusPopupContent" class="action-dropmenu-panel" style="display: none"><ul><li id="exportDnsStatusPdf" style="cursor: pointer" title="PDF"> Export PDF </li><li id="exportDnsStatusPng" style="cursor: pointer" title="PNG"> Export PNG </li><li id="exportDnsStatusSvg" style="cursor: pointer" title="SVG"> Export SVG </li></ul></div>

    <%--IP Availability -chart actions--%>
    <div id="ipAvailabilityPopupContent" class="action-dropmenu-panel" style="display: none"><ul><li id="exportIPAvailabilityPdf" style="cursor: pointer" title="PDF"> Export PDF </li><li id="exportIPAvailability" style="cursor: pointer" title="PNG"> Export PNG </li><li id="exportIPAvailabilitySvg" style="cursor: pointer" title="SVG"> Export SVG </li></ul></div>

    <%--vendor-chart actions--%>
    <div id="vendorPopupContent" class="action-dropmenu-panel" style="display: none"><ul><li id="exportVendorPdf" style="cursor: pointer" title="PDF"> Export PDF </li><li id="exportVendor" style="cursor: pointer" title="PNG"> Export PNG </li><li id="exportVendorSvg" style="cursor: pointer" title="SVG"> Export SVG </li></ul></div>

    <%--Subnet summary widget actions--%>
    <div id="subnetSummaryPopupContent" class="action-dropmenu-panel" style="display: none"><ul><li id="exportSubnetSummary" style="cursor: pointer" title="Screenshot">Screenshot</li></ul></div>

    <%--notification--%>
    <span id="notification"></span>

    <%--delete modal--%>
    <div id="deleteModal"></div>

    <%--add modal--%>
    <div id="addModal"></div>

    <%--add modal--%>
    <div id="innerModal"></div>

    <%--footer--%>
    <footer id="footer-panel">
        <jsp:include page="layout/footer.jsp"></jsp:include>
    </footer>
</div>

<script>

    $(document).ready(function()
    {
        if(appManager.getCookie("token") == null || appManager.getCookie("token") == "")
        {
            location.href = $("#url").val();
        }

        var root = $('body');

        appManager.init();

        var lastScroll = 0;

        $(window).scroll(function(event) {

            if ($(window).scrollTop() >= 100)
            {
                $('.stickyScrollLeft').addClass('stickyStickyLeft');
                $('.stickyScrollRight').addClass('stickyStickyRight');
                $height = $(window).height() - 170;
                $('body.stickyScrollUp .nav-panel').css('max-height', $height);
            }
            else
            {
                $('.stickyScrollLeft').removeClass('stickyStickyLeft');
                $('.stickyScrollRight').removeClass('stickyStickyRight');
                appManager.resetWindowSize();
            }

            var st = $(this).scrollTop();
            // Make sure they scroll more than delta
            if(Math.abs(lastScroll - st) <= 5)
                return;

            if (st < lastScroll)
            {
                if($(window).scrollTop() < 10)
                {
                    $('.stickyScrollLeft').removeClass('stickyStickyLeft');
                    $('.stickyScrollRight').removeClass('stickyStickyRight');
                    root.removeClass('stickyScrollDown');
                    root.removeClass('stickyScrollUp');
                }
                else
                {
                    root.removeClass('stickyScrollUp').addClass('stickyScrollDown');
                }
            }
            else
            {
                root.removeClass('stickyScrollDown').addClass('stickyScrollUp');
            }

            lastScroll = st;
        });

        appManager.resetWindowSize();

        $(window).resize(function() {
            appManager.resetWindowSize();
        });
    });
</script>


</body>
</html>
