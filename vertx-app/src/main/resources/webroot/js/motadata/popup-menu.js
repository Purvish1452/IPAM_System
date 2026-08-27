/**
 * Created by hardik on 6/8/18.
 */
var popupMenu =
{
  init : function ()
  {
      /*left-tree popup*/
      var popupDiv = $("#leftTreePopupContent");

      popupDiv.css('display','block');

      popupDiv.kendoPopup({
          animation: false
      });

      leftPanel.popupContent = popupDiv.data("kendoPopup");

      /*user-grid popup*/
      var userPopupDiv = $("#popupContent");

      userPopupDiv.css('display','block');

      userPopupDiv.kendoPopup({
          animation: false
      });

      userManagement.popupContent = userPopupDiv.data("kendoPopup");

      /*subnet-summary popup*/
      var subnetSummaryPopupDiv = $("#subnetSummaryPopupContent");

      subnetSummaryPopupDiv.css('display','block');

      subnetSummaryPopupDiv.kendoPopup({
          animation: false
      });

      subnetSummary.popupContent = subnetSummaryPopupDiv.data("kendoPopup");


      /*Dhcp server-summary popup*/
      var dhcpServerPopupDiv = $("#dhcpServerPopupContent");

      dhcpServerPopupDiv.css('display','block');

      dhcpServerPopupDiv.kendoPopup({
          animation: false
      });

      dhcpServerStatistics.popupContent = dhcpServerPopupDiv.data("kendoPopup");


      /*subnet-grid popup*/
      var subnetPopupDiv = $("#subnetPopupContent");

      subnetPopupDiv.css('display','block');

      subnetPopupDiv.kendoPopup({
          animation: false
      });

      homeManager.subnetPopup.popupContent = subnetPopupDiv.data("kendoPopup");

      /*dhcp-grid popup*/
      var dhcpPopupDiv = $("#dhcpPopupContent");

      dhcpPopupDiv.css('display','block');

      dhcpPopupDiv.kendoPopup({
          animation: false
      });

      homeManager.dhcpPopup.popupContent = dhcpPopupDiv.data("kendoPopup");

      /*dhcp-grid popup*/
      var conflictPopupDiv = $("#conflictPopupContent");

      conflictPopupDiv.css('display','block');

      conflictPopupDiv.kendoPopup({
          animation: false
      });

      homeManager.conflictPopupDiv.popupContent = conflictPopupDiv.data("kendoPopup");

      var recentlyDiscovered = $("#recentlyDiscoveredPopupContent");

      recentlyDiscovered.css('display','block');

      recentlyDiscovered.kendoPopup({
          animation: false
      });

      homeManager.recentlyDiscoveredPopupDiv.popupContent = recentlyDiscovered.data("kendoPopup");

      var dnsStatus = $("#dnsStatusPopupContent");

      dnsStatus.css('display','block');

      dnsStatus.kendoPopup({
          animation: false
      });

      homeManager.dnsStatusPopupDiv.popupContent = dnsStatus.data("kendoPopup");

      var top10SubnetUtilization = $("#top10SubnetUtilizationPopupContent");

      top10SubnetUtilization.css('display','block');

      top10SubnetUtilization.kendoPopup({
          animation: false
      });

      homeManager.top10SubnetUtilizationPopupDiv.popupContent = top10SubnetUtilization.data("kendoPopup");

      var top10CategoryUtilization = $("#top10CategoryUtilizationPopupContent");

      top10CategoryUtilization.css('display','block');

      top10CategoryUtilization.kendoPopup({
          animation: false
      });

      homeManager.top10CategoryUtilizationPopupDiv.popupContent = top10CategoryUtilization.data("kendoPopup");

      /*Ip Availability-chart popup*/
      var ipAvailabilityPopupDiv = $("#ipAvailabilityPopupContent");

      ipAvailabilityPopupDiv.css('display','block');

      ipAvailabilityPopupDiv.kendoPopup({
          animation: false
      });

      homeManager.ipAvailabilityPopupDiv.popupContent = ipAvailabilityPopupDiv.data("kendoPopup");

      /*Vendor-chart popup*/
      var vendorPopupDiv = $("#vendorPopupContent");

      vendorPopupDiv.css('display','block');

      vendorPopupDiv.kendoPopup({
          animation: false
      });

      homeManager.vendorPopupDiv.popupContent = vendorPopupDiv.data("kendoPopup");

    //  $("#conflictPopupContent, #exportDhcpPdf, #dhcpPopupContent").removeClass('action-dropmenu-panel');
  }
};