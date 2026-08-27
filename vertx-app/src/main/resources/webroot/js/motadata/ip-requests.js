var selectedIPs = new Set();

var ipRequests=
    {
        IpRequests: "ipRequestsPage",

        IpRequestsTable: "ipRequestsTable", Page: undefined,

        init:function ()
        {
            $('#container-panel').removeClass('leftOpenPanel');

            selectedIPs.clear();

            loaderUtil.showModalLoader();

            loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);

            topManager.setActiveMenu('ipRequests');

            if(ipRequests.Page === undefined)
            {
                navigationManager.addHistory("navigation=ipRequests");
            }

            var root = $("#header_panel");

            root.empty();

            root.html('<div class="title-inner-box" style="display: flex; justify-content: space-between; align-items: center; padding: 10px; font-size: 18px; font-weight: bold; width: 100%;"> <div style="flex: 1; text-align: left;">IP Requests</div> <div><button id="addIpRequest" class="k-button k-primary" style="font-size: 14px;">+ Add IP Request</button></div></div>');

            $("#container-panel").html('<div id="ipRequestsPage" class="content-panel" style="padding: 15px; "></div>');

            $("#addIpRequest").click(function ()
            {
                root.html('<div class="title-inner-box" style="display: flex; justify-content: space-between; align-items: center; padding: 10px; font-size: 18px; font-weight: bold; width: 100%;"> <div style="flex: 1; text-align: left;">IP Requests</div></div>');

                ipRequests.loadAddRequestForm();
            });

            ipRequests.loadIpRequestsPage();
        },

        loadIpRequestsPage: function ()
        {
            var gridContainer = $("#ipRequestsPage");

            gridContainer.html('<div id="' + ipRequests.IpRequestsTable + '" style="border: 1px solid #ddd; border-radius: 5px; padding: 10px;"></div>');

            var gridId = $('#' + ipRequests.IpRequestsTable);

            var callbackContexts = {
                EventId: ipRequests.IpRequestsTable,
                PageSize: 20,
                pageable: {
                    refresh: true,
                    pageSizes: [10, 20, 50, 100],
                    buttonCount: 10
                },
                resizable: true,
                schema: {
                    model: {
                        id: "id",
                        fields: {
                            NoOfIps: { type: "string" },
                            location: { type: "string" },
                            requestedBy: { type: "string" },
                            status: { type: "string" }
                        }
                    }
                },
                Fields: [
                    {
                        field: "requestedBy",
                        title: "Requested By",
                        template: function(dataItem) {
                            return dataItem.requestedBy
                                ? `<a href="#" class="requestedByLink">${kendo.htmlEncode(dataItem.requestedBy)}</a>`
                                : "N/A";
                        }
                    },
                    { field: "NoOfIps", title: "No. of ips" },
                    { field: "requestedOn", title: "Requested On" },
                    {
                        field: "status",
                        title: "Status"
                    },
                    { field: "reviewedOn", title: "Reviewed On" },
                    { field: "reviewedBy", title: "Reviewed By" }
                ]
            };

            try
            {
                gridId.data().kendoGrid.destroy();

                gridId.empty();
            }
            catch (err) {}

            appManager.executeGETRequest(
                {
                url: "/ipRequests/",
                params: {},
                callback: function (response)
                {
                    var data = response.json.data || [];

                    if (!Array.isArray(data))
                    {
                        console.error("Invalid response format", data);

                        return;
                    }

                    var processedData = data.map(function (item) {

                        let formattedLastModifiedDate="N/A";

                        let reviewdBy="N/A";

                        if(item.status!=='PENDING')
                        {
                            formattedLastModifiedDate = ipRequests.formatDate(item.lastModifiedDate);

                            reviewdBy=item.lastModifiedBy;
                        }

                        const formattedCreatedDate = ipRequests.formatDate(item.createdDate);

                        return {
                            id: item.id,
                            NoOfIps : item.numberOfIps,
                            requestedBy: item.createdBy || "N/A",
                            requestedOn : formattedCreatedDate,
                            status: item.status,
                            reviewedOn : formattedLastModifiedDate,
                            reviewedBy: reviewdBy
                        };
                    });

                    gridId.kendoGrid({
                        dataSource: {
                            data: processedData,
                            schema: callbackContexts.schema,
                            pageSize: callbackContexts.PageSize
                        },
                        pageable: callbackContexts.pageable,
                        resizable: callbackContexts.resizable,
                        columns: callbackContexts.Fields
                    });

                    gridId.off("click", ".requestedByLink").on("click", ".requestedByLink", function (e)
                    {
                        e.preventDefault();

                        var row = $(this).closest("tr");

                        var dataItem = gridId.data("kendoGrid").dataItem(row);

                        if (dataItem)
                        {
                            var requestId = dataItem.id;

                            ipRequests.fetchRequestDetails(requestId);
                        }
                    });

                    loaderUtil.hideCentralModalLoader();

                    loaderUtil.hideModalLoader();
                }
            });
        },

     fetchRequestDetails: function(requestId)
     {
            appManager.executeGETRequest({

                url: "/ipRequests/" + requestId,
                params: {},
                callback: function (response) {
                    var data = response.json;
                    if (!data) {
                        console.error("Invalid response format", data);
                        return;
                    }

                    $("#container-panel").html(ipRequests.renderRequestDetailsPage(data.data));

                    ipRequests.loadAvailableIp(data.data);

                }
            });
    },
    formatDate: function(dateArray)
    {
        if (!dateArray || dateArray.length < 6) return "N/A";

        const [year, month, day, hour, minute, second] = dateArray;

        const date = new Date(year, month - 1, day, hour, minute, second);

        return date.toLocaleString("en-US", {
            weekday: "short",
            day: "2-digit",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit",
            hour12: true,
            timeZone: "Asia/Kolkata"
        });
    },
    renderRequestDetailsPage: function(data)
    {
     const statusStyles = {
         PENDING: { text: "Pending", color: "#ffcc00", textColor: "black" },
         APPROVED: { text: "Approved", color: "#28a745", textColor: "white" },
         REJECTED: { text: "Rejected", color: "#dc3545", textColor: "white" }
     };

     const status = data.status || "PENDING";

     const statusStyle = statusStyles[status] || statusStyles.PENDING;

     const formattedCreatedDate = ipRequests.formatDate(data.createdDate);

     let formattedLastModifiedDate="N/A";

     let reviewdBy="N/A";

     if(data.status!=='PENDING')
     {
         formattedLastModifiedDate = ipRequests.formatDate(data.lastModifiedDate);

         reviewdBy=data.lastModifiedBy;
     }

     let subnetSelection = "";

     if (!data.preferredSubnet && data.status!=='REJECTED' && ipRequests.hasRole('ROLE_ADMIN')) {
         subnetSelection = ` <tr> <td style="padding: 12px; font-weight: bold; border-bottom: 1px solid #ddd;">Select Subnet</td> <td style="padding: 12px; border-bottom: 1px solid #ddd;"> <div class="row mt-2" id="subnetContainer"> <div class="col-md-6"> <select id="subnetDropdown" class="form-control custom-dropdown"></select> </div> </div> </td> </tr> <tr id="availableIpRender"> <td style="padding: 12px; font-weight: bold; border-bottom: 1px solid #ddd;">Available IPs </td> <td style="padding: 12px; border-bottom: 1px solid #ddd;"> <div class="row mt-3" id="availableIpsContainer"> <div class="col-md-12"> <input type="text" id="searchIp" class="form-control" placeholder="Search IP..."> <div id="availableIpsGrid" class="mt-2"></div> </div> </div> </td> </tr>`;
     }
     return `<div style="width: 80%; max-width: 1100px; margin: 30px auto; font-family: Arial, sans-serif; font-size: 16px; color: #000;"> <h2 style="font-size: 22px; font-weight: bold; margin-bottom: 20px;">Requested IPs</h2> <table style="width: 100%; border-collapse: collapse;"> <tr> <td style="width: 25%; padding: 12px; font-weight: bold; border-bottom: 1px solid #ddd;">No. of ips</td> <td style="padding: 12px; border-bottom: 1px solid #ddd;" id="NumberOfIp">${data.numberOfIps || 'NA'}</td> </tr> <tr> <td style="padding: 12px; font-weight: bold; border-bottom: 1px solid #ddd;">Remark</td> <td style="padding: 12px; border-bottom: 1px solid #ddd;"> <textarea id="remark" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px;">${data.remark || 'NA'}</textarea> </td></tr> <tr> <td style="width: 25%; padding: 12px; font-weight: bold; border-bottom: 1px solid #ddd;">Purpose</td> <td style="padding: 12px; border-bottom: 1px solid #ddd;">${data.purpose || 'NA'}</td> </tr> <tr> <td style="padding: 12px; font-weight: bold; border-bottom: 1px solid #ddd;">User Name</td> <td style="padding: 12px; border-bottom: 1px solid #ddd;">${data.createdBy || 'NA'}</td> </tr> <tr> <td style="padding: 12px; font-weight: bold; border-bottom: 1px solid #ddd;">Status</td> <td style="padding: 12px; border-bottom: 1px solid #ddd;"> <span style="display: inline-block; background-color: ${statusStyle.color}; color: ${statusStyle.textColor}; padding: 6px 12px; border-radius: 6px; font-weight: bold;"> ${statusStyle.text} </span> </td> </tr> ${subnetSelection} <tr> <td style="padding: 12px; font-weight: bold; border-bottom: 1px solid #ddd;">Preferred Subnet</td> <td style="padding: 12px; border-bottom: 1px solid #ddd;">${data.subnetId || 'NA'}</td> </tr> <tr> <td style="padding: 12px; font-weight: bold; border-bottom: 1px solid #ddd;">Selected IPs</td> <td style="padding: 12px; border-bottom: 1px solid #ddd;"> <button onclick="ipRequests.toggleView('ip')" style="background-color: #28a745; color: white; border: none; padding: 6px 10px; border-radius: 4px; cursor: pointer; font-size: 14px;">IPs View</button> <button onclick="ipRequests.toggleView('table')" style="background-color: #ddd; color: black; border: none; padding: 6px 10px; border-radius: 4px; cursor: pointer; font-size: 14px;">Table View</button> <br /> <br /> <div id="ip-view" style="display: block;"> ${data.ips?.map(ip => `<span style="background-color: #28a745; color: white; padding: 6px 12px; margin-top: 6px; border-radius: 6px; font-weight: bold; display: inline-block; margin-right: 5px;">${ip}</span>`).join(' ') || 'NA'} </div> <div id="table-view" style="display: none; max-height: 200px; overflow-y: auto; border: 1px solid #ddd;"> <table style="width: 100%; border-collapse: collapse;"> <tr> <th style="padding: 10px; border-bottom: 1px solid #ddd; text-align: left;">IP Address</th> </tr> ${data.ips?.map(ip => ` <tr> <td style="padding: 10px; border-bottom: 1px solid #ddd;">${ip}</td> </tr> `).join('') || ' <tr> <td style="padding: 10px; border-bottom: 1px solid #ddd;">NA</td> </tr> '} </table> </div> </td> </tr> <tr> <td style="padding: 12px; font-weight: bold; border-bottom: 1px solid #ddd;">Requested On</td> <td style="padding: 12px; border-bottom: 1px solid #ddd;">${formattedCreatedDate}</td> </tr> <tr> <td style="padding: 12px; font-weight: bold; border-bottom: 1px solid #ddd;">Reviewed by</td> <td style="padding: 12px; border-bottom: 1px solid #ddd;">${reviewdBy || 'N/A'}</td> </tr> <tr> <td style="padding: 12px; font-weight: bold; border-bottom: 1px solid #ddd;">Reviewed on</td> <td style="padding: 12px; border-bottom: 1px solid #ddd;">${formattedLastModifiedDate}</td> </tr> </table> <div style="margin-top: 30px; text-align: right;"> <button onclick="ipRequests.goBackRequest()" style="background-color: #ddd; color: black; border: none; padding: 12px 20px; border-radius: 6px; cursor: pointer; font-size: 16px; margin-right: 15px;">Back</button> ${status === 'PENDING' && ipRequests.hasRole('ROLE_ADMIN')? ` <button onclick="ipRequests.declineRequest('${data.id}')" style="background-color: #dc3545; color: white; border: none; padding: 12px 20px; border-radius: 6px; cursor: pointer; font-size: 16px; margin-right: 10px;">Decline</button> <button onclick="ipRequests.acceptRequest('${data.id}','${data.subnetId}')" style="background-color: #28a745; color: white; border: none; padding: 12px 20px; border-radius: 6px; cursor: pointer; font-size: 16px;">Accept</button> ` : ''} </div></div>`;
     },
     toggleView: function(view) {
        document.getElementById('ip-view').style.display = view === 'ip' ? 'block' : 'none';
        document.getElementById('table-view').style.display = view === 'table' ? 'block' : 'none';

        const ipBtn = document.querySelector("button[onclick=\"ipRequests.toggleView('ip')\"]");
        const tableBtn = document.querySelector("button[onclick=\"ipRequests.toggleView('table')\"]");

        if (view === 'ip') {
            ipBtn.style.backgroundColor = "#28a745";
            ipBtn.style.color = "white";
            tableBtn.style.backgroundColor = "#ddd";
            tableBtn.style.color = "black";
        } else {
            tableBtn.style.backgroundColor = "#28a745";
            tableBtn.style.color = "white";
            ipBtn.style.backgroundColor = "#ddd";
            ipBtn.style.color = "black";
        }
    },
    acceptRequest: function(requestId,subnetId)
    {
        var requestData;

        if (subnetId === "null") {
            requestData = {
                id: requestId,
                subnetId: $("#subnetDropdown").data("kendoDropDownList").value(),
                ips: [...selectedIPs],
                remark: $('#remark').val().trim(),
            };
        }
        else
        {
            requestData = {
                id: requestId,
                subnetId: subnetId,
                ips: [...selectedIPs],
                remark: $('#remark').val().trim()
            };
        }
        appManager.executePOSTRequest({ url: '/ipRequests/approved',callback: ipRequests.afterIpRequestStatusUpdated,params: requestData});
    },
    declineRequest: function(requestId)
    {
            selectedIPs.clear();
            let requestData = {
                id: requestId,
                remark: $('#remark').val().trim()
            };
            appManager.executePOSTRequest({ url: '/ipRequests/rejected',callback: ipRequests.afterIpRequestStatusUpdated,params: requestData});
    },
    goBackRequest: function(requestId)
    {
            selectedIPs.clear();
            ipRequests.init();
    },
    loadAddRequestForm: function ()
    {
            selectedIPs.clear();

            var formHtml = `<div class="container mt-4" style="max-height: 80vh; overflow-y: auto; box-shadow: 0px 4px 10px rgba(0, 0, 0, 0.1); border-radius: 8px; padding: 20px; background: white;"> <h3>New IP Request</h3> <div class="row"> <div class="col-md-6"> <label for="numOfIps">No. Of IPs</label> <input type="number" id="numOfIps" class="form-control" value="2" min="1"> </div> <div class="col-md-6"> <label for="purpose">Purpose</label> <input type="text" id="purpose" class="form-control" value="NA"> </div> </div> <div class="row mt-2"> <div class="col-md-6"> <label for="username">User Name</label> <input type="text" id="username" class="form-control" value="${ipRequests.getUserName("userName")}" disabled> </div> </div> <div class="row mt-2"> <div class="col-md-12"> <div class="form-check"> <input type="checkbox" id="preferredSubnet" class="form-check-input"> <label for="preferredSubnet" class="form-check-label">Preferred Subnet</label> </div> </div> </div> <div class="row mt-2" id="subnetContainer"> <div class="col-md-12"> <select id="subnetDropdown" class="form-control custom-dropdown"></select> </div> </div> <div class="row mt-3" id="availableIpsContainer"> <div class="col-md-12"> <input type="text" id="searchIp" class="form-control" placeholder="Search IP..."> <div id="availableIpsGrid" class="mt-2"></div> <div class="selected-ips mt-2"> Selected IPs: <div id="selectedIpsContainer" style="max-height: 100px; overflow-y: auto; border: 1px solid #ccc; padding: 5px; border-radius: 5px;"> <span id="selectedIps"></span> </div> </div> </div> </div> <div class="row mt-3" id="availableIpsContainer"> <div class="col-md-12"> <input type="text" id="searchIp" class="form-control" placeholder="Search IP..."> <div id="availableIpsGrid" class="mt-2"></div> <div class="selected-ips mt-2"> Selected IPs: <div id="selectedIpsContainer" style="max-height: 100px; overflow-y: auto; border: 1px solid #ccc; padding: 5px; border-radius: 5px;"> <span id="selectedIps"></span> </div> </div> </div></div> <div class="row mt-3"> <div class="col-md-6"> <button id="saveRequest" class="k-button k-primary">Submit</button> </div> <div class="col-md-6"> <button id="cancelRequest" class="k-button k-primary">Cancel</button> </div> </div> </div>`;

            $("#container-panel").html(formHtml);

            $("#availableIpsContainer").hide(); // Hide initially

            $("#subnetDropdown").change(function () {
                if ($(this).val()) {
                    $("#availableIpsContainer").show();
                } else {
                    $("#availableIpsContainer").hide();
                }
            });

            flux.getKendoDropDownListURL({
                dropDownId: $("#subnetDropdown"),
                url: "/subnet/",
                dataTextField: "subnetAddress",
                dataValueField: "id",
                optionLabel: "Select a Subnet..."
            });

            function toggleSubnetFields() {
                if ($("#preferredSubnet").prop("checked")) {
                    selectedIPs.clear();
                    updateSelectedIpsDisplay();
                    $("#subnetContainer").show();
                } else {
                    selectedIPs.clear();
                    updateSelectedIpsDisplay();
                    $("#subnetDropdown").data("kendoDropDownList").value("");

                    $("#subnetContainer, #availableIpsContainer").hide();
                }
            }

            toggleSubnetFields();

            $("#preferredSubnet").change(toggleSubnetFields);

            $("#subnetDropdown").on("change", function () {
                var selectedSubnetId = $(this).val();
                if (selectedSubnetId) {
                    selectedIPs.clear();
                    loadAvailableIps(selectedSubnetId);
                }
            });
        function loadAvailableIps(subnetId) {
            updateSelectedIpsDisplay();
            appManager.executeGETRequest({
                url: "/subnetIpBySubnet/" + subnetId,
                callback: function (request) {
                    console.log("API Response:", request.json);

                    var ipList = Array.isArray(request.json.data) ? request.json.data : [request.json.data];

                    var ipData = ipList
                        .filter(item => item && item.status === "Available")
                        .map(item => ({
                            ip: item.ipAddress || "N/A",
                            status: item.status || "Unknown",
                            mac: item.macAddress || "N/A"
                        }));

                    var grid = $("#availableIpsGrid").data("kendoGrid");
                    if (grid) {
                        grid.dataSource.data(ipData);
                    } else {
                        $("#availableIpsGrid").kendoGrid({
                            dataSource: {
                                data: ipData,
                                schema: {
                                    model: {
                                        fields: {
                                            ip: { type: "string" },
                                            status: { type: "string" },
                                            mac: { type: "string" }
                                        }
                                    }
                                },
                                pageSize: 5
                            },
                            height: 250,
                            pageable: true,
                            persistSelection: true,
                            columns: [
                                {
                                    field: "select",
                                    title: "<input type='checkbox' id='selectAllCheckbox' style='opacity: 1;'/>",
                                    template: "<input type='checkbox' class='ip-checkbox' data-ip='#= ip #' style='opacity: 1;'/>",
                                    width: "60px"
                                },
                                { field: "ip", title: "Available IPs" },
                                { field: "status", title: "Status" }
                            ],
                            dataBound: function () {
                                syncCheckboxUI();

                                $(".ip-checkbox").off("change").on("change", function () {
                                    var ip = $(this).data("ip");
                                    handleCheckboxSelection(ip, $(this).is(":checked"));
                                });

                                $("#selectAllCheckbox").off("change").on("change", function () {
                                    handleSelectAll($(this).is(":checked"));
                                });

                                updateSelectAllCheckbox();
                            }
                        });
                    }
                }
            });
        }

        function syncCheckboxUI() {
            $(".ip-checkbox").each(function () {
                var ip = $(this).data("ip");
                $(this).prop("checked", selectedIPs.has(ip));
            });
            updateSelectAllCheckbox();
        }

        function handleSelectAll(isChecked) {
            var numOfIps = parseInt($("#numOfIps").val());
            var grid = $("#availableIpsGrid").data("kendoGrid");
            var visibleData = grid.dataSource.view();

            if (isChecked) {
                visibleData.forEach(item => {
                    if (selectedIPs.size < numOfIps) {
                        selectedIPs.add(item.ip);
                    }
                    else
                    {
                        notification.showNotification({
                            notificationTitle: "You can select only " + numOfIps + " IPs.",
                            notificationType: "error"
                        });
                    }
                });
            } else {
                visibleData.forEach(item => selectedIPs.delete(item.ip));
            }

            syncCheckboxUI();
            updateSelectedIpsDisplay();
            updateSelectAllCheckbox();
        }

        function handleCheckboxSelection(ip, isChecked) {
            var numOfIps = parseInt($("#numOfIps").val());
            if (isChecked) {
                if (selectedIPs.size > numOfIps) {
                    notification.showNotification({
                        notificationTitle: "You can select only " + numOfIps + " IPs.",
                        notificationType: "error"
                    });
                    selectedIPs.delete(ip);
                    updateSelectedIpsDisplay();
                    syncCheckboxUI();
                    return;
                }
                selectedIPs.add(ip);
            } else {
                selectedIPs.delete(ip);
            }

            syncCheckboxUI();
            updateSelectedIpsDisplay();
            updateSelectAllCheckbox();
        }

        function updateSelectedIpsDisplay() {
            let selectedHtml = Array.from(selectedIPs)
                .map(ip => `<span style="
                display: inline-block;
                background-color: #007bff;
                color: white;
                padding: 5px 10px;
                margin: 3px;
                border-radius: 15px;
                font-size: 14px;
                cursor: pointer;
            " data-ip="${ip}">${ip}</span>`)
                .join("");

            $("#selectedIps").html(selectedHtml);

            let container = $("#selectedIpsContainer");
            container.css("overflow-y", selectedIPs.size > 5 ? "auto" : "hidden");
        }

        function updateSelectAllCheckbox() {
            var grid = $("#availableIpsGrid").data("kendoGrid");
            var visibleData = grid.dataSource.view();
            var selectedOnPage = visibleData.filter(item => selectedIPs.has(item.ip)).length;

            if (selectedOnPage === 0) {
                $("#selectAllCheckbox").prop("checked", false).prop("indeterminate", false);
            } else if (selectedOnPage === visibleData.length) {
                $("#selectAllCheckbox").prop("checked", true).prop("indeterminate", false);
            } else {
                $("#selectAllCheckbox").prop("indeterminate", true);
            }
        }

        $(document).on("change", ".ip-checkbox", function () {
            var ip = $(this).data("ip");
            handleCheckboxSelection(ip, $(this).is(":checked"));
        });

        $("#searchIp").on("input", function () {
                var value = $(this).val().toLowerCase();
                var grid = $("#availableIpsGrid").data("kendoGrid");
                grid.dataSource.filter({
                    logic: "or",
                    filters: [{ field: "ip", operator: "contains", value: value }]
                });
            });

            $("#saveRequest").click(function () {

                var requestData = {
                    numberOfIps: $("#numOfIps").val(),
                    purpose: $("#purpose").val(),
                    location: $("#location").val(),
                    subnetId: $("#preferredSubnet").prop("checked") ? $("#subnetDropdown").data("kendoDropDownList").value() : null,
                    ips: [...selectedIPs],
                    preferredSubnet: $("#preferredSubnet").prop("checked")
                };

                if(requestData.numberOfIps>0)
                {
                    appManager.executePOSTRequest({
                        url: '/ipRequests/',
                        params: requestData,
                        callback: ipRequests.afterIpRequestStatusUpdated
                    });
                }
                else
                {
                    notification.showNotification({
                        notificationTitle: "Please add valid number of ips",
                        notificationType: "error"
                    });
                }
            });

            $("#cancelRequest").click(function () {
                ipRequests.init();
            });
        },
        afterIpRequestStatusUpdated : function (context)
        {
            if(context)
            {
                if(context.json.success == true)
                {
                    notification.showNotification({notificationTitle:context.json.message,notificationType:"success"});
                    ipRequests.init();
                }
                else
                {
                    notification.showNotification({notificationTitle:context.json.message,notificationType:"error"});
                }
            }
        },

        loadAvailableIp : function (data)
        {
            $("#availableIpRender").hide();

            $("#subnetDropdown").change(function () {
                if ($(this).val()) {
                    $("#availableIpRender").show();
                } else {
                    $("#availableIpRender").hide();
                }
            });
            flux.getKendoDropDownListURL({
                dropDownId: $("#subnetDropdown"),
                url: "/subnet/",
                dataTextField: "subnetAddress",
                dataValueField: "id",
                optionLabel: "Select a Subnet..."
            });

            $("#subnetDropdown").on("change", function () {
                var selectedSubnetId = $(this).val();
                if (selectedSubnetId) {
                    selectedIPs.clear();
                    loadAvailableIps(selectedSubnetId);
                }
            });

            function loadAvailableIps(subnetId)
            {
                    ipRequests.updateSelectedIPsView();
                    appManager.executeGETRequest({
                        url: "/subnetIpBySubnet/" + subnetId,
                        callback: function (request) {
                            console.log("API Response:", request.json);

                            var ipList = Array.isArray(request.json.data) ? request.json.data : [request.json.data];

                            var ipData = ipList
                                .filter(item => item && item.status === "Available")
                                .map(item => ({
                                    ip: item.ipAddress || "N/A",
                                    status: item.status || "Unknown",
                                    mac: item.macAddress || "N/A"
                                }));

                            var grid = $("#availableIpsGrid").data("kendoGrid");
                            if (grid) {
                                grid.dataSource.data(ipData);
                            } else {
                                $("#availableIpsGrid").kendoGrid({
                                    dataSource: {
                                        data: ipData,
                                        schema: {
                                            model: {
                                                fields: {
                                                    ip: { type: "string" },
                                                    status: { type: "string" },
                                                    mac: { type: "string" }
                                                }
                                            }
                                        },
                                        pageSize: 5
                                    },
                                    height: 250,
                                    pageable: true,
                                    persistSelection: true,
                                    columns: [
                                        {
                                            field: "select",
                                            title: "<input type='checkbox' id='selectAllCheckbox' style='opacity: 1;'/>",
                                            template: "<input type='checkbox' class='ip-checkbox' data-ip='#= ip #' style='opacity: 1;'/>",
                                            width: "60px"
                                        },
                                        { field: "ip", title: "Available IPs" },
                                        { field: "status", title: "Status" }
                                    ],
                                    dataBound: function () {
                                        syncCheckboxUI();

                                        $(".ip-checkbox").off("change").on("change", function () {
                                            var ip = $(this).data("ip");
                                            handleCheckboxSelection(ip, $(this).is(":checked"));
                                            ipRequests.updateSelectedIPsView();
                                        });

                                        $("#selectAllCheckbox").off("change").on("change", function () {
                                            handleSelectAll($(this).is(":checked"));
                                            ipRequests.updateSelectedIPsView();
                                        });

                                        updateSelectAllCheckbox();
                                    }
                                });
                            }
                        }
                    });


                function syncCheckboxUI() {
                    $(".ip-checkbox").each(function () {
                        var ip = $(this).data("ip");
                        $(this).prop("checked", selectedIPs.has(ip));
                    });
                    updateSelectAllCheckbox();
                }

                function handleSelectAll(isChecked) {
                    var numOfIps = $("#NumberOfIp").text();
                    var grid = $("#availableIpsGrid").data("kendoGrid");
                    var visibleData = grid.dataSource.view();

                    if (isChecked) {
                        visibleData.forEach(item => {
                            if (selectedIPs.size < numOfIps) {
                                selectedIPs.add(item.ip);
                            }
                            else
                            {
                                notification.showNotification({
                                    notificationTitle: "You can select only " + numOfIps + " IPs.",
                                    notificationType: "error"
                                });
                            }
                        });
                    } else {
                        visibleData.forEach(item => selectedIPs.delete(item.ip));
                    }

                    syncCheckboxUI();
                    ipRequests.updateSelectedIPsView();
                    updateSelectAllCheckbox();
                }

                function handleCheckboxSelection(ip, isChecked) {
                    var numOfIps = $("#NumberOfIp").text();

                    if (isChecked) {
                        if (selectedIPs.size > numOfIps) {
                            notification.showNotification({
                                notificationTitle: "You can select only " + numOfIps + " IPs.",
                                notificationType: "error"
                            });
                            selectedIPs.delete(ip);
                            ipRequests.updateSelectedIPsView();
                            syncCheckboxUI();
                            return;
                        }
                        selectedIPs.add(ip);
                    } else {
                        selectedIPs.delete(ip);
                    }

                    syncCheckboxUI();
                    ipRequests.updateSelectedIPsView();
                    updateSelectAllCheckbox();
                }

                function updateSelectedIpsDisplay() {
                    let selectedHtml = Array.from(selectedIPs)
                        .map(ip => `<span style="
                        display: inline-block;
                        background-color: #007bff;
                        color: white;
                        padding: 5px 10px;
                        margin: 3px;
                        border-radius: 15px;
                        font-size: 14px;
                        cursor: pointer;
                    " data-ip="${ip}">${ip}</span>`)
                        .join("");

                    $("#selectedIps").html(selectedHtml);

                    let container = $("#selectedIpsContainer");
                    container.css("overflow-y", selectedIPs.size > 5 ? "auto" : "hidden");
                }

                function updateSelectAllCheckbox() {
                    var grid = $("#availableIpsGrid").data("kendoGrid");
                    var visibleData = grid.dataSource.view();
                    var selectedOnPage = visibleData.filter(item => selectedIPs.has(item.ip)).length;

                    if (selectedOnPage === 0) {
                        $("#selectAllCheckbox").prop("checked", false).prop("indeterminate", false);
                    } else if (selectedOnPage === visibleData.length) {
                        $("#selectAllCheckbox").prop("checked", true).prop("indeterminate", false);
                    } else {
                        $("#selectAllCheckbox").prop("indeterminate", true);
                    }
                }

                $(document).on("change", ".ip-checkbox", function () {
                    var ip = $(this).data("ip");
                    handleCheckboxSelection(ip, $(this).is(":checked"));
                });
            }
            $(document).on("change", ".ip-checkbox", function () {
                var numOfIps = parseInt($("#numOfIps").val());

                if (selectedIPs.size > numOfIps) {
                    notification.showNotification({
                        notificationTitle: "You can select only "+ data.numberOfIps + " IPs.",
                        notificationType: "error"
                    });
                    $(this).prop("checked", false);
                    return;
                }
            });

            $("#searchIp").on("input", function () {
                var value = $(this).val().toLowerCase();
                var grid = $("#availableIpsGrid").data("kendoGrid");
                grid.dataSource.filter({
                    logic: "or",
                    filters: [{ field: "ip", operator: "contains", value: value }]
                });
            });

        },
        updateSelectedIPsView: function()
        {
            let ipArray = Array.from(selectedIPs);

            let ipViewHtml = ipArray.length
                ? ipArray.map(ip => `<span style="background-color: #28a745; color: white; padding: 6px 12px; margin-top: 6px; border-radius: 6px; font-weight: bold; display: inline-block; margin-right: 5px;">${ip}</span>`).join(' ')
                : 'NA';
            $("#ip-view").html(ipViewHtml);

            let tableViewHtml = ipArray.length
                ? ipArray.map(ip => `<tr><td style="padding: 10px; border-bottom: 1px solid #ddd;">${ip}</td></tr>`).join('')
                : '<tr><td style="padding: 10px; border-bottom: 1px solid #ddd;">NA</td></tr>';
            $("#table-view table").html(`<tr><th style="padding: 10px; border-bottom: 1px solid #ddd; text-align: left;">IP Address</th></tr>` + tableViewHtml);
        },
        getAuthoritiesFromCookie: function()
        {
            let authoritiesCookie = document.cookie.split(';').find(cookie => cookie.trim().startsWith('authorities='));
            const roleRegex = /ROLE_[A-Za-z0-9\s]+/g;
            return authoritiesCookie.match(roleRegex);
        },

        hasRole: function(role)
        {
            const authorities = ipRequests.getAuthoritiesFromCookie();
            return authorities.includes(role);
        },

        getUserName: function(name)
        {
            let cookie = document.cookie.split(';').find(cookie => cookie.trim().startsWith(name + '='));
            return cookie ? cookie.split('=')[1] : '';
        },

        renderIpRequestsFromURL : function ()
        {
            ipRequests.init();
        }
    };
