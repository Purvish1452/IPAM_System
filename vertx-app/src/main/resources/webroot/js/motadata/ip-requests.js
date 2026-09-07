var selectedIPs = new Set();

var ipRequests = {
    IpRequests: "ipRequestsPage",
    IpRequestsTable: "ipRequestsTable",
    Page: undefined,

    init: function () {
        $('#container-panel').removeClass('leftOpenPanel');
        selectedIPs.clear();
        loaderUtil.showModalLoader();
        loaderUtil.showCentralModalLoader(appConstant.LoadingMessage);
        topManager.setActiveMenu('ipRequests');

        if (ipRequests.Page === undefined) {
            navigationManager.addHistory("navigation=ipRequests");
        }

        var root = $("#header_panel");
        root.empty();
        root.html(`
            <div class="title-inner-box" style="display: flex; justify-content: space-between; align-items: center; padding: 10px 15px; font-size: 18px; font-weight: bold; width: 100%;">
                <div style="flex: 1; text-align: left; display: flex; align-items: center; gap: 10px;">
                    <i class="fa fa-exchange" style="color: #007bff;"></i>
                    <span>IP Request & Approval Workflow</span>
                </div>
                <div>
                    <button id="addIpRequest" class="k-button k-primary" style="font-size: 14px; padding: 6px 16px; border-radius: 4px;">
                        <i class="fa fa-plus-circle" style="margin-right: 5px;"></i> Submit Request
                    </button>
                </div>
            </div>
        `);

        $("#container-panel").html('<div id="ipRequestsPage" class="content-panel" style="padding: 15px;"></div>');

        $("#addIpRequest").click(function () {
            ipRequests.loadAddRequestForm();
        });

        ipRequests.loadIpRequestsPage();
    },

    loadIpRequestsPage: function () {
        var gridContainer = $("#ipRequestsPage");
        gridContainer.html(`
            <div style="background: #fff; border-radius: 6px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); padding: 15px; margin-bottom: 15px;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                    <div>
                        <h4 style="margin: 0; font-size: 16px; font-weight: 600; color: #333;">Static IP Allocation Requests</h4>
                        <p style="margin: 4px 0 0; font-size: 13px; color: #666;">Self-service portal and administrative review queue for IP assignments.</p>
                    </div>
                    <div style="display: flex; gap: 10px; align-items: center;">
                        <input type="text" id="filterRequestsInput" class="form-control" placeholder="Search requests..." style="width: 240px; font-size: 13px; height: 34px;">
                    </div>
                </div>
                <div id="${ipRequests.IpRequestsTable}"></div>
            </div>
        `);

        var gridId = $('#' + ipRequests.IpRequestsTable);

        var callbackContexts = {
            EventId: ipRequests.IpRequestsTable,
            PageSize: 15,
            pageable: {
                refresh: true,
                pageSizes: [10, 15, 25, 50, 100],
                buttonCount: 5
            },
            resizable: true,
            schema: {
                model: {
                    id: "id",
                    fields: {
                        id: { type: "number" },
                        requestedBy: { type: "string" },
                        NoOfIps: { type: "number" },
                        deviceType: { type: "string" },
                        duration: { type: "string" },
                        subnetAddress: { type: "string" },
                        purpose: { type: "string" },
                        status: { type: "string" },
                        requestedOn: { type: "string" },
                        reviewedBy: { type: "string" },
                        reviewedOn: { type: "string" }
                    }
                }
            },
            Fields: [
                {
                    field: "id",
                    title: "Req #",
                    width: "70px",
                    template: function (dataItem) {
                        return `<span style="font-weight: bold; color: #555;">#${dataItem.id}</span>`;
                    }
                },
                {
                    field: "requestedBy",
                    title: "Requested By",
                    width: "140px",
                    template: function (dataItem) {
                        return dataItem.requestedBy
                            ? `<a href="#" class="requestedByLink" style="font-weight: 600; color: #007bff; text-decoration: none;"><i class="fa fa-user" style="margin-right: 4px; font-size: 12px;"></i>${kendo.htmlEncode(dataItem.requestedBy)}</a>`
                            : "N/A";
                    }
                },
                {
                    field: "NoOfIps",
                    title: "IPs Req.",
                    width: "80px",
                    template: function (dataItem) {
                        return `<span style="background: #e9ecef; color: #333; padding: 2px 8px; border-radius: 10px; font-weight: bold; font-size: 12px;">${dataItem.NoOfIps || 1}</span>`;
                    }
                },
                {
                    field: "deviceType",
                    title: "Device Type",
                    width: "130px",
                    template: function (dataItem) {
                        return `<span style="color: #495057;"><i class="fa fa-desktop" style="margin-right: 4px; color: #6c757d; font-size: 12px;"></i>${kendo.htmlEncode(dataItem.deviceType || 'Server')}</span>`;
                    }
                },
                {
                    field: "duration",
                    title: "Duration",
                    width: "110px",
                    template: function (dataItem) {
                        return `<span style="color: #495057;"><i class="fa fa-clock-o" style="margin-right: 4px; color: #6c757d; font-size: 12px;"></i>${kendo.htmlEncode(dataItem.duration || 'Permanent')}</span>`;
                    }
                },
                {
                    field: "subnetAddress",
                    title: "Subnet",
                    width: "140px",
                    template: function (dataItem) {
                        return dataItem.subnetAddress && dataItem.subnetAddress !== 'null'
                            ? `<code style="background: #f8f9fa; padding: 2px 6px; border: 1px solid #dee2e6; border-radius: 3px; color: #d63384; font-size: 12px;">${kendo.htmlEncode(dataItem.subnetAddress)}</code>`
                            : `<span style="color: #888; font-style: italic;">Auto-assign</span>`;
                    }
                },
                {
                    field: "purpose",
                    title: "Justification / Purpose",
                    template: function (dataItem) {
                        return `<span title="${kendo.htmlEncode(dataItem.purpose || '')}" style="display: block; max-width: 250px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; color: #333;">${kendo.htmlEncode(dataItem.purpose || 'Static IP Allocation')}</span>`;
                    }
                },
                {
                    field: "status",
                    title: "Status",
                    width: "110px",
                    template: function (dataItem) {
                        var st = (dataItem.status || 'PENDING').toUpperCase();
                        if (st === 'APPROVED') {
                            return `<span style="display: inline-block; background: #d1e7dd; color: #0f5132; border: 1px solid #badbcc; padding: 3px 10px; border-radius: 12px; font-weight: 600; font-size: 12px;"><i class="fa fa-check-circle" style="margin-right: 3px;"></i>Approved</span>`;
                        } else if (st === 'REJECTED') {
                            return `<span style="display: inline-block; background: #f8d7da; color: #842029; border: 1px solid #f5c2c7; padding: 3px 10px; border-radius: 12px; font-weight: 600; font-size: 12px;"><i class="fa fa-times-circle" style="margin-right: 3px;"></i>Rejected</span>`;
                        } else {
                            return `<span style="display: inline-block; background: #fff3cd; color: #664d03; border: 1px solid #ffecb5; padding: 3px 10px; border-radius: 12px; font-weight: 600; font-size: 12px;"><i class="fa fa-hourglass-half" style="margin-right: 3px;"></i>Pending</span>`;
                        }
                    }
                },
                {
                    field: "requestedOn",
                    title: "Requested On",
                    width: "150px"
                },
                {
                    field: "action",
                    title: "Action",
                    width: "100px",
                    template: function (dataItem) {
                        var btnText = (dataItem.status === 'PENDING' && ipRequests.hasRole('ROLE_ADMIN')) ? 'Review' : 'Details';
                        var btnClass = (dataItem.status === 'PENDING' && ipRequests.hasRole('ROLE_ADMIN')) ? 'btn-primary' : 'btn-default';
                        return `<button class="btn btn-xs ${btnClass} viewDetailsBtn" data-id="${dataItem.id}" style="font-size: 12px; padding: 3px 8px; border-radius: 4px;">${btnText}</button>`;
                    }
                }
            ]
        };

        try {
            gridId.data().kendoGrid.destroy();
            gridId.empty();
        } catch (err) {}

        appManager.executeGETRequest({
            url: "/ipRequests/",
            params: {},
            callback: function (response) {
                var data = response.json.data || [];
                if (!Array.isArray(data)) {
                    console.error("Invalid response format", data);
                    data = [];
                }

                var processedData = data.map(function (item) {
                    let formattedLastModifiedDate = "N/A";
                    let reviewedBy = "N/A";

                    if (item.status !== 'PENDING') {
                        formattedLastModifiedDate = ipRequests.formatDate(item.lastModifiedDate);
                        reviewedBy = item.lastModifiedBy || "admin";
                    }

                    const formattedCreatedDate = ipRequests.formatDate(item.createdDate);

                    return {
                        id: item.id,
                        NoOfIps: item.numberOfIps || 1,
                        deviceType: item.deviceType || "Server",
                        duration: item.duration || "Permanent",
                        subnetAddress: item.subnetAddress || (item.subnetId ? 'Subnet #' + item.subnetId : null),
                        purpose: item.purpose || "Static IP Allocation",
                        requestedBy: item.createdBy || item.requestedBy || "N/A",
                        requestedOn: formattedCreatedDate,
                        status: item.status || "PENDING",
                        reviewedOn: formattedLastModifiedDate,
                        reviewedBy: reviewedBy
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

                // Row click handler
                gridId.off("click", ".requestedByLink, .viewDetailsBtn").on("click", ".requestedByLink, .viewDetailsBtn", function (e) {
                    e.preventDefault();
                    var row = $(this).closest("tr");
                    var dataItem = gridId.data("kendoGrid").dataItem(row);
                    if (dataItem) {
                        ipRequests.fetchRequestDetails(dataItem.id);
                    }
                });

                // Live search filter
                $("#filterRequestsInput").on("input", function () {
                    var val = $(this).val().toLowerCase();
                    var grid = gridId.data("kendoGrid");
                    if (grid) {
                        grid.dataSource.filter({
                            logic: "or",
                            filters: [
                                { field: "requestedBy", operator: "contains", value: val },
                                { field: "deviceType", operator: "contains", value: val },
                                { field: "subnetAddress", operator: "contains", value: val },
                                { field: "purpose", operator: "contains", value: val },
                                { field: "status", operator: "contains", value: val }
                            ]
                        });
                    }
                });

                loaderUtil.hideCentralModalLoader();
                loaderUtil.hideModalLoader();
            }
        });
    },

    fetchRequestDetails: function (requestId) {
        loaderUtil.showModalLoader();
        appManager.executeGETRequest({
            url: "/ipRequests/" + requestId,
            params: {},
            callback: function (response) {
                loaderUtil.hideModalLoader();
                var data = response.json;
                if (!data || !data.data) {
                    notification.showNotification({ notificationTitle: "Failed to load request details", notificationType: "error" });
                    return;
                }

                $("#container-panel").html(ipRequests.renderRequestDetailsPage(data.data));
                ipRequests.loadAvailableIp(data.data);
            }
        });
    },

    formatDate: function (dateArray) {
        if (!dateArray) return "N/A";
        if (typeof dateArray === 'string') return dateArray;
        if (Array.isArray(dateArray) && dateArray.length >= 3) {
            const year = dateArray[0] || 2026;
            const month = (dateArray[1] || 1) - 1;
            const day = dateArray[2] || 1;
            const hour = dateArray[3] || 0;
            const minute = dateArray[4] || 0;
            const second = dateArray[5] || 0;

            const date = new Date(year, month, day, hour, minute, second);
            return date.toLocaleString("en-US", {
                month: "short",
                day: "2-digit",
                year: "numeric",
                hour: "2-digit",
                minute: "2-digit",
                hour12: true
            });
        }
        return "N/A";
    },

    renderRequestDetailsPage: function (data) {
        const statusStyles = {
            PENDING: { text: "Pending Review", bg: "#fff3cd", color: "#664d03", border: "#ffecb5", icon: "fa-hourglass-half" },
            APPROVED: { text: "Approved", bg: "#d1e7dd", color: "#0f5132", border: "#badbcc", icon: "fa-check-circle" },
            REJECTED: { text: "Rejected", bg: "#f8d7da", color: "#842029", border: "#f5c2c7", icon: "fa-times-circle" }
        };

        const status = (data.status || "PENDING").toUpperCase();
        const statusStyle = statusStyles[status] || statusStyles.PENDING;
        const formattedCreatedDate = ipRequests.formatDate(data.createdDate);

        let formattedLastModifiedDate = "N/A";
        let reviewedBy = "N/A";

        if (status !== 'PENDING') {
            formattedLastModifiedDate = ipRequests.formatDate(data.lastModifiedDate);
            reviewedBy = data.lastModifiedBy || "admin";
        }

        const isAdmin = ipRequests.hasRole('ROLE_ADMIN');
        const isPending = status === 'PENDING';

        // Pre-populate selectedIPs from data.ips if present
        selectedIPs.clear();
        if (Array.isArray(data.ips)) {
            data.ips.forEach(ip => { if (ip) selectedIPs.add(ip); });
        }

        let adminReviewSection = "";
        if (isPending && isAdmin) {
            adminReviewSection = `
                <div style="background: #f8f9fa; border: 1px solid #e9ecef; border-radius: 8px; padding: 20px; margin-top: 25px;">
                    <h4 style="font-size: 16px; font-weight: bold; margin-top: 0; margin-bottom: 15px; color: #212529;">
                        <i class="fa fa-sliders" style="color: #007bff; margin-right: 6px;"></i> Administrator Allocation & Review
                    </h4>
                    <div class="row">
                        <div class="col-md-6">
                            <label style="font-weight: 600; font-size: 13px; margin-bottom: 5px;">Allocate from Subnet</label>
                            <select id="subnetDropdown" class="form-control" style="width: 100%;"></select>
                        </div>
                        <div class="col-md-6">
                            <label style="font-weight: 600; font-size: 13px; margin-bottom: 5px;">Admin Remarks / Feedback</label>
                            <textarea id="adminRemarkInput" class="form-control" rows="2" placeholder="Add approval notes or rejection rationale..." style="font-size: 13px; border-radius: 4px;">${data.remark || ''}</textarea>
                        </div>
                    </div>
                    <div class="row mt-3" id="availableIpRender" style="display: none; margin-top: 15px;">
                        <div class="col-md-12">
                            <label style="font-weight: 600; font-size: 13px; margin-bottom: 5px;">Select Available IP Address(es)</label>
                            <div style="display: flex; gap: 10px; margin-bottom: 10px;">
                                <input type="text" id="searchIp" class="form-control" placeholder="Search available IP..." style="width: 250px; font-size: 13px;">
                                <div style="display: flex; align-items: center; font-size: 13px; color: #555;">
                                    <span>Selected: <strong id="selectedIpCount" style="color: #007bff;">${selectedIPs.size}</strong> of <strong>${data.numberOfIps || 1}</strong> required</span>
                                </div>
                            </div>
                            <div id="availableIpsGrid"></div>
                        </div>
                    </div>
                </div>
            `;
        }

        let ipsListHtml = '<em>No IPs allocated yet.</em>';
        if (data.ips && Array.isArray(data.ips) && data.ips.length > 0) {
            ipsListHtml = data.ips.map(ip => `<span style="background-color: #28a745; color: white; padding: 4px 10px; border-radius: 12px; font-weight: 600; display: inline-block; margin-right: 6px; margin-bottom: 4px; font-size: 13px;"><i class="fa fa-map-marker" style="margin-right: 4px;"></i>${ip}</span>`).join(' ');
        }

        return `
            <div style="width: 90%; max-width: 1100px; margin: 20px auto; font-family: 'Segoe UI', Arial, sans-serif; font-size: 14px; color: #333;">
                <div style="background: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 25px;">
                    
                    <!-- Header -->
                    <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; padding-bottom: 15px; margin-bottom: 20px;">
                        <div>
                            <span style="color: #888; font-size: 13px; text-transform: uppercase; font-weight: 600; letter-spacing: 0.5px;">Static IP Request</span>
                            <h2 style="font-size: 22px; font-weight: 700; margin: 4px 0 0; color: #212529;">Request #${data.id}</h2>
                        </div>
                        <div>
                            <span style="display: inline-block; background: ${statusStyle.bg}; color: ${statusStyle.color}; border: 1px solid ${statusStyle.border}; padding: 6px 16px; border-radius: 20px; font-weight: bold; font-size: 13px;">
                                <i class="fa ${statusStyle.icon}" style="margin-right: 5px;"></i> ${statusStyle.text}
                            </span>
                        </div>
                    </div>

                    <!-- Details Table -->
                    <table style="width: 100%; border-collapse: collapse; font-size: 14px;">
                        <tr style="border-bottom: 1px solid #f0f0f0;">
                            <td style="width: 25%; padding: 10px 0; font-weight: 600; color: #666;">Requested By</td>
                            <td style="padding: 10px 0; color: #212529; font-weight: 500;">
                                <i class="fa fa-user-circle" style="color: #6c757d; margin-right: 5px;"></i>${data.createdBy || data.requestedBy || 'N/A'}
                            </td>
                            <td style="width: 20%; padding: 10px 0; font-weight: 600; color: #666;">Requested On</td>
                            <td style="padding: 10px 0; color: #212529;">${formattedCreatedDate}</td>
                        </tr>
                        <tr style="border-bottom: 1px solid #f0f0f0;">
                            <td style="padding: 10px 0; font-weight: 600; color: #666;">No. of IPs</td>
                            <td style="padding: 10px 0; color: #212529;" id="NumberOfIp">${data.numberOfIps || 1}</td>
                            <td style="padding: 10px 0; font-weight: 600; color: #666;">Device Type</td>
                            <td style="padding: 10px 0; color: #212529;"><i class="fa fa-desktop" style="color: #6c757d; margin-right: 5px;"></i>${data.deviceType || 'Server'}</td>
                        </tr>
                        <tr style="border-bottom: 1px solid #f0f0f0;">
                            <td style="padding: 10px 0; font-weight: 600; color: #666;">Allocation Duration</td>
                            <td style="padding: 10px 0; color: #212529;"><i class="fa fa-clock-o" style="color: #6c757d; margin-right: 5px;"></i>${data.duration || 'Permanent'}</td>
                            <td style="padding: 10px 0; font-weight: 600; color: #666;">Target Subnet</td>
                            <td style="padding: 10px 0; color: #212529;">
                                <code style="background: #f8f9fa; padding: 2px 6px; border: 1px solid #dee2e6; border-radius: 3px; color: #0d6efd; font-size: 13px;">${data.subnetAddress || (data.subnetId ? 'Subnet #' + data.subnetId : 'Any / Auto-assign')}</code>
                            </td>
                        </tr>
                        <tr style="border-bottom: 1px solid #f0f0f0;">
                            <td style="padding: 10px 0; font-weight: 600; color: #666;">Business Justification</td>
                            <td colspan="3" style="padding: 10px 0; color: #212529; line-height: 1.5;">${data.purpose || 'Static IP Allocation'}</td>
                        </tr>
                        ${status !== 'PENDING' ? `
                        <tr style="border-bottom: 1px solid #f0f0f0;">
                            <td style="padding: 10px 0; font-weight: 600; color: #666;">Reviewed By</td>
                            <td style="padding: 10px 0; color: #212529;">${reviewedBy}</td>
                            <td style="padding: 10px 0; font-weight: 600; color: #666;">Reviewed On</td>
                            <td style="padding: 10px 0; color: #212529;">${formattedLastModifiedDate}</td>
                        </tr>
                        ` : ''}
                        <tr style="border-bottom: 1px solid #f0f0f0;">
                            <td style="padding: 10px 0; font-weight: 600; color: #666;">Reviewer Remarks</td>
                            <td colspan="3" style="padding: 10px 0; color: #495057;">${data.remark || '<em>None</em>'}</td>
                        </tr>
                        <tr>
                            <td style="padding: 12px 0; font-weight: 600; color: #666; vertical-align: top;">Allocated / Selected IPs</td>
                            <td colspan="3" style="padding: 12px 0;">
                                <div style="margin-bottom: 8px;">
                                    <button onclick="ipRequests.toggleView('ip')" style="background-color: #28a745; color: white; border: none; padding: 4px 12px; border-radius: 4px; cursor: pointer; font-size: 12px; font-weight: 600;">Chips View</button>
                                    <button onclick="ipRequests.toggleView('table')" style="background-color: #e9ecef; color: #333; border: none; padding: 4px 12px; border-radius: 4px; cursor: pointer; font-size: 12px; font-weight: 600; margin-left: 5px;">Table View</button>
                                </div>
                                <div id="ip-view" style="display: block; min-height: 30px;">
                                    ${ipsListHtml}
                                </div>
                                <div id="table-view" style="display: none; max-height: 180px; overflow-y: auto; border: 1px solid #dee2e6; border-radius: 4px;">
                                    <table style="width: 100%; border-collapse: collapse; font-size: 13px;">
                                        <thead>
                                            <tr style="background: #f8f9fa;">
                                                <th style="padding: 8px 12px; border-bottom: 1px solid #dee2e6; text-align: left;">Allocated IP Address</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            ${data.ips && data.ips.length > 0 ? data.ips.map(ip => `<tr><td style="padding: 8px 12px; border-bottom: 1px solid #eee;"><code>${ip}</code></td></tr>`).join('') : '<tr><td style="padding: 8px 12px; color: #888;">No IPs allocated</td></tr>'}
                                        </tbody>
                                    </table>
                                </div>
                            </td>
                        </tr>
                    </table>

                    <!-- Admin Review & Modify Section -->
                    ${adminReviewSection}

                    <!-- Actions -->
                    <div style="margin-top: 25px; display: flex; justify-content: flex-end; gap: 10px; border-top: 1px solid #eee; padding-top: 20px;">
                        <button onclick="ipRequests.goBackRequest()" class="btn btn-default" style="padding: 8px 20px; font-size: 14px; border-radius: 4px;">
                            <i class="fa fa-arrow-left" style="margin-right: 5px;"></i> Back to Queue
                        </button>
                        ${isPending && isAdmin ? `
                            <button onclick="ipRequests.declineRequest('${data.id}')" class="btn btn-danger" style="padding: 8px 20px; font-size: 14px; border-radius: 4px; background: #dc3545; color: white; border: none;">
                                <i class="fa fa-times" style="margin-right: 5px;"></i> Reject Request
                            </button>
                            <button onclick="ipRequests.acceptRequest('${data.id}', '${data.subnetId}')" class="btn btn-success" style="padding: 8px 24px; font-size: 14px; border-radius: 4px; background: #198754; color: white; border: none; font-weight: 600;">
                                <i class="fa fa-check" style="margin-right: 5px;"></i> Approve & Allocate
                            </button>
                        ` : ''}
                    </div>

                </div>
            </div>
        `;
    },

    toggleView: function (view) {
        var ipView = document.getElementById('ip-view');
        var tableView = document.getElementById('table-view');
        if (!ipView || !tableView) return;

        ipView.style.display = view === 'ip' ? 'block' : 'none';
        tableView.style.display = view === 'table' ? 'block' : 'none';

        const ipBtn = document.querySelector("button[onclick=\"ipRequests.toggleView('ip')\"]");
        const tableBtn = document.querySelector("button[onclick=\"ipRequests.toggleView('table')\"]");

        if (ipBtn && tableBtn) {
            if (view === 'ip') {
                ipBtn.style.backgroundColor = "#28a745";
                ipBtn.style.color = "white";
                tableBtn.style.backgroundColor = "#e9ecef";
                tableBtn.style.color = "#333";
            } else {
                tableBtn.style.backgroundColor = "#28a745";
                tableBtn.style.color = "white";
                ipBtn.style.backgroundColor = "#e9ecef";
                ipBtn.style.color = "#333";
            }
        }
    },

    acceptRequest: function (requestId, existingSubnetId) {
        var dropdown = $("#subnetDropdown").data("kendoDropDownList");
        var selectedSubnet = dropdown ? dropdown.value() : existingSubnetId;
        var remark = $('#adminRemarkInput').val() ? $('#adminRemarkInput').val().trim() : '';

        var requestData = {
            id: parseInt(requestId, 10),
            subnetId: selectedSubnet && selectedSubnet !== "null" ? selectedSubnet : existingSubnetId,
            ips: Array.from(selectedIPs),
            remark: remark,
            lastModifiedBy: ipRequests.getUserName()
        };

        loaderUtil.showModalLoader();
        appManager.executePOSTRequest({
            url: '/ipRequests/approved',
            callback: ipRequests.afterIpRequestStatusUpdated,
            params: requestData
        });
    },

    declineRequest: function (requestId) {
        var remark = $('#adminRemarkInput').val() ? $('#adminRemarkInput').val().trim() : '';
        selectedIPs.clear();

        var requestData = {
            id: parseInt(requestId, 10),
            remark: remark || 'Request declined by administrator',
            lastModifiedBy: ipRequests.getUserName()
        };

        loaderUtil.showModalLoader();
        appManager.executePOSTRequest({
            url: '/ipRequests/rejected',
            callback: ipRequests.afterIpRequestStatusUpdated,
            params: requestData
        });
    },

    goBackRequest: function () {
        selectedIPs.clear();
        ipRequests.init();
    },

    loadAddRequestForm: function () {
        selectedIPs.clear();

        var root = $("#header_panel");
        root.html(`
            <div class="title-inner-box" style="display: flex; justify-content: space-between; align-items: center; padding: 10px 15px; font-size: 18px; font-weight: bold; width: 100%;">
                <div style="flex: 1; text-align: left; display: flex; align-items: center; gap: 10px;">
                    <i class="fa fa-plus-circle" style="color: #007bff;"></i>
                    <span>Submit Static IP Request</span>
                </div>
                <div>
                    <button id="backToQueueTopBtn" class="k-button" style="font-size: 14px; padding: 6px 14px; border-radius: 4px;">
                        <i class="fa fa-arrow-left" style="margin-right: 5px;"></i> Back to Queue
                    </button>
                </div>
            </div>
        `);

        $("#backToQueueTopBtn").click(function () {
            ipRequests.init();
        });

        var formHtml = `
            <div class="container" style="max-width: 850px; margin: 25px auto; background: #fff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08); padding: 30px;">
                <div style="border-bottom: 1px solid #eee; padding-bottom: 15px; margin-bottom: 20px;">
                    <h3 style="margin: 0; font-size: 20px; font-weight: 700; color: #212529;">Static IP Allocation Form</h3>
                    <p style="margin: 5px 0 0; color: #6c757d; font-size: 13px;">Please complete the details below to request static IP address assignments for internal infrastructure or devices.</p>
                </div>

                <div class="row">
                    <div class="col-md-6 form-group">
                        <label for="numOfIps" style="font-weight: 600; font-size: 13px;">Number of IPs Required <span style="color: red;">*</span></label>
                        <input type="number" id="numOfIps" class="form-control" value="1" min="1" max="50" style="height: 38px; font-size: 14px;">
                    </div>
                    <div class="col-md-6 form-group">
                        <label for="deviceTypeDropdown" style="font-weight: 600; font-size: 13px;">Device Type <span style="color: red;">*</span></label>
                        <select id="deviceTypeDropdown" class="form-control" style="height: 38px; font-size: 14px;">
                            <option value="Server" selected>Server</option>
                            <option value="Virtual Machine (VM)">Virtual Machine (VM)</option>
                            <option value="Container">Container / Kubernetes Pod</option>
                            <option value="Workstation">Workstation / Laptop</option>
                            <option value="Router">Router</option>
                            <option value="Switch">Switch</option>
                            <option value="Firewall">Firewall / Security Appliance</option>
                            <option value="Access Point">Wireless Access Point (AP)</option>
                            <option value="IoT Device">IoT / Embedded Device</option>
                            <option value="Other">Other Infrastructure</option>
                        </select>
                    </div>
                </div>

                <div class="row mt-2">
                    <div class="col-md-6 form-group">
                        <label for="durationDropdown" style="font-weight: 600; font-size: 13px;">Allocation Duration <span style="color: red;">*</span></label>
                        <select id="durationDropdown" class="form-control" style="height: 38px; font-size: 14px;">
                            <option value="Permanent" selected>Permanent</option>
                            <option value="30 Days">30 Days</option>
                            <option value="60 Days">60 Days</option>
                            <option value="90 Days">90 Days</option>
                            <option value="6 Months">6 Months</option>
                            <option value="1 Year">1 Year</option>
                            <option value="Temporary">Temporary (Under 30 Days)</option>
                        </select>
                    </div>
                    <div class="col-md-6 form-group">
                        <label for="username" style="font-weight: 600; font-size: 13px;">Requester Username</label>
                        <input type="text" id="username" class="form-control" value="${ipRequests.getUserName()}" disabled style="height: 38px; font-size: 14px; background: #f8f9fa;">
                    </div>
                </div>

                <div class="row mt-2">
                    <div class="col-md-12 form-group">
                        <label for="purpose" style="font-weight: 600; font-size: 13px;">Business Justification / Workload Purpose <span style="color: red;">*</span></label>
                        <textarea id="purpose" class="form-control" rows="3" placeholder="Describe the workload, system role, host name, or project justification..." style="font-size: 14px;"></textarea>
                    </div>
                </div>

                <div class="row mt-2">
                    <div class="col-md-12">
                        <div style="background: #f8f9fa; border: 1px solid #e9ecef; border-radius: 6px; padding: 12px 15px;">
                            <label style="margin: 0; cursor: pointer; font-weight: 600; font-size: 14px; display: flex; align-items: center; gap: 8px;">
                                <input type="checkbox" id="preferredSubnet" style="margin: 0; width: 16px; height: 16px;">
                                <span>I have a specific subnet preference / want to select available IPs</span>
                            </label>
                        </div>
                    </div>
                </div>

                <div class="row mt-3" id="subnetContainer" style="display: none; margin-top: 15px;">
                    <div class="col-md-12">
                        <label style="font-weight: 600; font-size: 13px; margin-bottom: 5px;">Target Subnet</label>
                        <select id="subnetDropdown" class="form-control" style="width: 100%;"></select>
                    </div>
                </div>

                <div class="row mt-3" id="availableIpsContainer" style="display: none; margin-top: 15px;">
                    <div class="col-md-12">
                        <div style="border: 1px solid #dee2e6; border-radius: 6px; padding: 15px; background: #fafafa;">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                                <label style="font-weight: 600; font-size: 13px; margin: 0;">Available IPs in Selected Subnet</label>
                                <input type="text" id="searchIp" class="form-control" placeholder="Filter IPs..." style="width: 200px; height: 32px; font-size: 12px;">
                            </div>
                            <div id="availableIpsGrid"></div>
                            <div class="selected-ips mt-2" style="margin-top: 10px;">
                                <div style="font-size: 13px; font-weight: 600; margin-bottom: 5px; color: #495057;">
                                    Selected IPs: <span id="selectedCountBadge" style="color: #007bff;">0</span> / <span id="maxReqCount">1</span>
                                </div>
                                <div id="selectedIpsContainer" style="min-height: 40px; max-height: 90px; overflow-y: auto; border: 1px solid #ced4da; padding: 6px; border-radius: 4px; background: white;">
                                    <span id="selectedIps" style="color: #888; font-size: 13px;"><em>No specific IPs chosen (will be auto-allocated on approval).</em></span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row mt-4" style="margin-top: 25px; border-top: 1px solid #eee; padding-top: 20px;">
                    <div class="col-md-12" style="display: flex; justify-content: flex-end; gap: 10px;">
                        <button id="cancelRequest" class="btn btn-default" style="padding: 8px 20px; font-size: 14px;">Cancel</button>
                        <button id="saveRequest" class="btn btn-primary" style="padding: 8px 25px; font-size: 14px; font-weight: 600; background: #007bff; border-color: #007bff;">
                            <i class="fa fa-paper-plane" style="margin-right: 5px;"></i> Submit Request
                        </button>
                    </div>
                </div>
            </div>
        `;

        $("#container-panel").html(formHtml);

        flux.getKendoDropDownListURL({
            dropDownId: $("#subnetDropdown"),
            url: "/subnet/",
            dataTextField: "subnetAddress",
            dataValueField: "id",
            optionLabel: "Select a Subnet..."
        });

        function toggleSubnetFields() {
            if ($("#preferredSubnet").prop("checked")) {
                $("#subnetContainer").show();
                var dropdown = $("#subnetDropdown").data("kendoDropDownList");
                if (dropdown && dropdown.value()) {
                    $("#availableIpsContainer").show();
                }
            } else {
                selectedIPs.clear();
                updateSelectedIpsDisplay();
                $("#subnetContainer, #availableIpsContainer").hide();
            }
        }

        $("#preferredSubnet").change(toggleSubnetFields);

        $("#numOfIps").on("input change", function () {
            var val = parseInt($(this).val(), 10) || 1;
            $("#maxReqCount").text(val);
        });

        $("#subnetDropdown").on("change", function () {
            var selectedSubnetId = $(this).val();
            if (selectedSubnetId) {
                selectedIPs.clear();
                updateSelectedIpsDisplay();
                $("#availableIpsContainer").show();
                loadAvailableIps(selectedSubnetId);
            } else {
                $("#availableIpsContainer").hide();
            }
        });

        function loadAvailableIps(subnetId) {
            appManager.executeGETRequest({
                url: "/subnetIpBySubnet/" + subnetId,
                callback: function (request) {
                    var ipList = Array.isArray(request.json.data) ? request.json.data : (request.json.data ? [request.json.data] : []);

                    var ipData = ipList
                        .filter(item => item && String(item.status || "").toUpperCase() === "AVAILABLE")
                        .map(item => ({
                            ip: item.ipAddress || "N/A",
                            status: item.status || "AVAILABLE",
                            mac: item.macAddress || "-"
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
                            height: 220,
                            pageable: true,
                            columns: [
                                {
                                    field: "select",
                                    title: "<input type='checkbox' id='selectAllCheckbox'/>",
                                    template: "<input type='checkbox' class='ip-checkbox' data-ip='#: ip #'/>",
                                    width: "50px"
                                },
                                { field: "ip", title: "Available IP" },
                                { field: "mac", title: "MAC Address" },
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
            var numOfIps = parseInt($("#numOfIps").val(), 10) || 1;
            var grid = $("#availableIpsGrid").data("kendoGrid");
            if (!grid) return;
            var visibleData = grid.dataSource.view();

            if (isChecked) {
                visibleData.forEach(item => {
                    if (selectedIPs.size < numOfIps) {
                        selectedIPs.add(item.ip);
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
            var numOfIps = parseInt($("#numOfIps").val(), 10) || 1;
            if (isChecked) {
                if (selectedIPs.size >= numOfIps && !selectedIPs.has(ip)) {
                    notification.showNotification({
                        notificationTitle: "You can select up to " + numOfIps + " IP(s).",
                        notificationType: "error"
                    });
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
            $("#selectedCountBadge").text(selectedIPs.size);
            if (selectedIPs.size === 0) {
                $("#selectedIps").html('<em>No specific IPs chosen (will be auto-allocated on approval).</em>');
                return;
            }

            let selectedHtml = Array.from(selectedIPs)
                .map(ip => `
                    <span style="display: inline-block; background-color: #007bff; color: white; padding: 3px 8px; margin: 2px; border-radius: 12px; font-size: 12px; font-weight: 600;">
                        ${ip} <i class="fa fa-times remove-ip-tag" data-ip="${ip}" style="cursor: pointer; margin-left: 4px;"></i>
                    </span>
                `)
                .join("");

            $("#selectedIps").html(selectedHtml);

            $(".remove-ip-tag").off("click").on("click", function () {
                var ip = $(this).data("ip");
                selectedIPs.delete(ip);
                syncCheckboxUI();
                updateSelectedIpsDisplay();
            });
        }

        function updateSelectAllCheckbox() {
            var grid = $("#availableIpsGrid").data("kendoGrid");
            if (!grid) return;
            var visibleData = grid.dataSource.view();
            var selectedOnPage = visibleData.filter(item => selectedIPs.has(item.ip)).length;

            if (selectedOnPage === 0) {
                $("#selectAllCheckbox").prop("checked", false).prop("indeterminate", false);
            } else if (selectedOnPage === visibleData.length && visibleData.length > 0) {
                $("#selectAllCheckbox").prop("checked", true).prop("indeterminate", false);
            } else {
                $("#selectAllCheckbox").prop("indeterminate", true);
            }
        }

        $("#searchIp").on("input", function () {
            var value = $(this).val().toLowerCase();
            var grid = $("#availableIpsGrid").data("kendoGrid");
            if (grid) {
                grid.dataSource.filter({
                    logic: "or",
                    filters: [{ field: "ip", operator: "contains", value: value }]
                });
            }
        });

        $("#saveRequest").click(function () {
            var numOfIps = parseInt($("#numOfIps").val(), 10);
            var purpose = $("#purpose").val().trim();
            var deviceType = $("#deviceTypeDropdown").val();
            var duration = $("#durationDropdown").val();
            var preferredSubnet = $("#preferredSubnet").prop("checked");
            var dropdown = $("#subnetDropdown").data("kendoDropDownList");
            var subnetId = preferredSubnet && dropdown ? dropdown.value() : null;

            if (!numOfIps || numOfIps < 1) {
                notification.showNotification({ notificationTitle: "Please enter a valid number of IPs (at least 1)", notificationType: "error" });
                return;
            }

            if (!purpose) {
                notification.showNotification({ notificationTitle: "Please provide a business justification / purpose", notificationType: "error" });
                return;
            }

            var requestData = {
                numberOfIps: numOfIps,
                createdBy: ipRequests.getUserName(),
                deviceType: deviceType,
                duration: duration,
                purpose: purpose,
                subnetId: subnetId && subnetId !== "null" ? subnetId : null,
                ips: Array.from(selectedIPs),
                preferredSubnet: preferredSubnet
            };

            loaderUtil.showModalLoader();
            appManager.executePOSTRequest({
                url: '/ipRequests/',
                params: requestData,
                callback: ipRequests.afterIpRequestStatusUpdated
            });
        });

        $("#cancelRequest").click(function () {
            ipRequests.init();
        });
    },

    afterIpRequestStatusUpdated: function (context) {
        loaderUtil.hideCentralModalLoader();
        loaderUtil.hideModalLoader();
        if (context && context.json) {
            if (context.json.success === true) {
                notification.showNotification({ notificationTitle: context.json.message || "Operation completed successfully", notificationType: "success" });
                ipRequests.init();
            } else {
                notification.showNotification({ notificationTitle: context.json.message || "Operation failed", notificationType: "error" });
            }
        }
    },

    loadAvailableIp: function (data) {
        if (!data || data.status === 'REJECTED' || !ipRequests.hasRole('ROLE_ADMIN')) {
            return;
        }

        flux.getKendoDropDownListURL({
            dropDownId: $("#subnetDropdown"),
            url: "/subnet/",
            dataTextField: "subnetAddress",
            dataValueField: "id",
            optionLabel: "Select a Subnet...",
            callback: function () {
                var dropdown = $("#subnetDropdown").data("kendoDropDownList");
                if (dropdown && data.subnetId) {
                    dropdown.value(data.subnetId);
                    $("#availableIpRender").show();
                    loadAvailableIps(data.subnetId);
                }
            }
        });

        $("#subnetDropdown").on("change", function () {
            var selectedSubnetId = $(this).val();
            if (selectedSubnetId) {
                selectedIPs.clear();
                $("#availableIpRender").show();
                loadAvailableIps(selectedSubnetId);
            } else {
                $("#availableIpRender").hide();
            }
        });

        function loadAvailableIps(subnetId) {
            appManager.executeGETRequest({
                url: "/subnetIpBySubnet/" + subnetId,
                callback: function (request) {
                    var ipList = Array.isArray(request.json.data) ? request.json.data : (request.json.data ? [request.json.data] : []);

                    var ipData = ipList
                        .filter(item => item && String(item.status || "").toUpperCase() === "AVAILABLE")
                        .map(item => ({
                            ip: item.ipAddress || "N/A",
                            status: item.status || "AVAILABLE",
                            mac: item.macAddress || "-"
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
                            height: 220,
                            pageable: true,
                            columns: [
                                {
                                    field: "select",
                                    title: "<input type='checkbox' id='selectAllCheckbox'/>",
                                    template: "<input type='checkbox' class='ip-checkbox' data-ip='#: ip #'/>",
                                    width: "50px"
                                },
                                { field: "ip", title: "Available IP" },
                                { field: "mac", title: "MAC Address" },
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
            var numOfIps = parseInt($("#NumberOfIp").text(), 10) || 1;
            var grid = $("#availableIpsGrid").data("kendoGrid");
            if (!grid) return;
            var visibleData = grid.dataSource.view();

            if (isChecked) {
                visibleData.forEach(item => {
                    if (selectedIPs.size < numOfIps) {
                        selectedIPs.add(item.ip);
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
            var numOfIps = parseInt($("#NumberOfIp").text(), 10) || 1;

            if (isChecked) {
                if (selectedIPs.size >= numOfIps && !selectedIPs.has(ip)) {
                    notification.showNotification({
                        notificationTitle: "This request is for " + numOfIps + " IP(s).",
                        notificationType: "error"
                    });
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

        function updateSelectAllCheckbox() {
            var grid = $("#availableIpsGrid").data("kendoGrid");
            if (!grid) return;
            var visibleData = grid.dataSource.view();
            var selectedOnPage = visibleData.filter(item => selectedIPs.has(item.ip)).length;

            if (selectedOnPage === 0) {
                $("#selectAllCheckbox").prop("checked", false).prop("indeterminate", false);
            } else if (selectedOnPage === visibleData.length && visibleData.length > 0) {
                $("#selectAllCheckbox").prop("checked", true).prop("indeterminate", false);
            } else {
                $("#selectAllCheckbox").prop("indeterminate", true);
            }
        }

        $("#searchIp").on("input", function () {
            var value = $(this).val().toLowerCase();
            var grid = $("#availableIpsGrid").data("kendoGrid");
            if (grid) {
                grid.dataSource.filter({
                    logic: "or",
                    filters: [{ field: "ip", operator: "contains", value: value }]
                });
            }
        });
    },

    updateSelectedIPsView: function () {
        let ipArray = Array.from(selectedIPs);
        $("#selectedIpCount").text(ipArray.length);

        let ipViewHtml = ipArray.length
            ? ipArray.map(ip => `<span style="background-color: #28a745; color: white; padding: 4px 10px; border-radius: 12px; font-weight: 600; display: inline-block; margin-right: 6px; margin-bottom: 4px; font-size: 13px;"><i class="fa fa-map-marker" style="margin-right: 4px;"></i>${ip}</span>`).join(' ')
            : '<em>No IPs allocated yet.</em>';
        $("#ip-view").html(ipViewHtml);

        let tableViewHtml = ipArray.length
            ? ipArray.map(ip => `<tr><td style="padding: 8px 12px; border-bottom: 1px solid #eee;"><code>${ip}</code></td></tr>`).join('')
            : '<tr><td style="padding: 8px 12px; color: #888;">No IPs selected</td></tr>';
        $("#table-view table tbody").html(tableViewHtml);
    },

    getAuthoritiesFromCookie: function () {
        let authorities = [];
        let authoritiesCookie = document.cookie.split(';').find(cookie => cookie.trim().startsWith('authorities='));
        if (authoritiesCookie) {
            try {
                let decoded = decodeURIComponent(authoritiesCookie.split('=')[1] || '');
                if (decoded.startsWith('[') && decoded.endsWith(']')) {
                    let parsed = JSON.parse(decoded);
                    if (Array.isArray(parsed)) authorities.push(...parsed);
                } else {
                    const matches = authoritiesCookie.match(/(?:ROLE_|PERM_)[A-Za-z0-9_]+/g);
                    if (matches) authorities.push(...matches);
                }
            } catch (e) {
                const matches = authoritiesCookie.match(/(?:ROLE_|PERM_)[A-Za-z0-9_]+/g);
                if (matches) authorities.push(...matches);
            }
        }
        if (typeof getAuthoritiesFromCookie === 'function') {
            try {
                let gAuth = getAuthoritiesFromCookie();
                if (Array.isArray(gAuth)) authorities.push(...gAuth);
            } catch (e) {}
        }
        if (ipRequests.getUserName() === 'admin') {
            authorities.push('ROLE_ADMIN', 'PERM_IP_REQUESTS_WRITE', 'PERM_IP_REQUESTS_READ');
        }
        return [...new Set(authorities)];
    },

    hasRole: function (role) {
        if (ipRequests.getUserName() === 'admin') return true;
        const authorities = ipRequests.getAuthoritiesFromCookie();
        return Array.isArray(authorities) && authorities.includes(role);
    },

    getUserName: function (name) {
        name = name || 'userName';
        let cookie = document.cookie.split(';').find(cookie => cookie.trim().startsWith(name + '='));
        if (cookie) {
            let val = decodeURIComponent(cookie.split('=')[1] || '').trim();
            if (val) return val;
        }
        if ($("#userName").length && $("#userName").val()) {
            return $("#userName").val().trim();
        }
        return 'admin';
    },

    renderIpRequestsFromURL: function () {
        ipRequests.init();
    }
};
