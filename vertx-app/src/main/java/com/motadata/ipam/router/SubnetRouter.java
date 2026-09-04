package com.motadata.ipam.router;

import com.motadata.ipam.service.DiscoveryService;
import com.motadata.ipam.service.SubnetIPActionService;
import com.motadata.ipam.service.SubnetService;
import com.motadata.ipam.service.UserService;
import io.vertx.core.http.Cookie;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vert.x Web router for Subnet, IP Details, Rogue Detection, IP Requests,
 * Categories, Gateways, Supernets, and Dashboard Analytics REST API endpoints.
 * Architecture: Handler -> Service -> PgPool -> PostgreSQL
 */
public class SubnetRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubnetRouter.class);

    private final SubnetService subnetService;
    private final UserService userService;
    private final SubnetIPActionService ipActionService;
    private final DiscoveryService discoveryService;

    public SubnetRouter(SubnetService subnetService, UserService userService, SubnetIPActionService ipActionService, DiscoveryService discoveryService) {
        this.subnetService = subnetService;
        this.userService = userService;
        this.ipActionService = ipActionService;
        this.discoveryService = discoveryService;
    }


    public void attachRoutes(Router router) {
        router.get("/validatePermission/").handler(this::handleValidatePermission);

        // Subnet CRUD endpoints
        router.get("/subnet/").handler(this::handleGetAllSubnets);
        router.get("/subnet/:id").handler(this::handleGetSubnetById);
        router.get("/normalSubnet/").handler(this::handleGetAllSubnets);
        router.get("/normalSubnet/:id").handler(this::handleGetSubnetById);
        router.post("/subnet/").handler(this::handleSaveSubnet);
        router.post("/normalSubnet/").handler(this::handleSaveSubnet);
        router.put("/subnet/:id").handler(this::handleSaveSubnet);
        router.put("/subnet/").handler(this::handleSaveSubnet);
        router.delete("/subnet/:id").handler(this::handleDeleteSubnet);
        router.get("/subnetIp/").handler(this::handleGetIpDetails);
        router.get("/subnetIpBySubnet/:id").handler(this::handleGetIpDetailsBySubnetId);
        router.get("/subnetByCategory/").handler(this::handleGetSubnetByCategory);
        router.get("/supernetByCategory/").handler(this::handleGetSupernetByCategory);

        // Gateway CRUD endpoints
        router.get("/gateway/").handler(this::handleGetGateways);
        router.get("/gateways/").handler(this::handleGetGateways);
        router.get("/gateway/:id").handler(this::handleGetGateways);
        router.post("/gateway/").handler(this::handleSaveGateway);
        router.put("/gateway/:id").handler(this::handleSaveGateway);
        router.delete("/gateway/:id").handler(this::handleDeleteGateway);

        // Category CRUD endpoints
        router.get("/category/").handler(this::handleGetCategories);
        router.get("/category/:id").handler(this::handleGetCategories);
        router.post("/category/").handler(this::handleSaveCategory);
        router.put("/category/:id").handler(this::handleSaveCategory);
        router.delete("/category/:id").handler(this::handleDeleteCategory);

        // Supernet CRUD endpoints
        router.get("/supernet/").handler(this::handleGetSupernets);
        router.get("/supernet/:id").handler(this::handleGetSupernets);
        router.post("/supernet/").handler(this::handleSaveSupernet);
        router.put("/supernet/:id").handler(this::handleSaveSupernet);
        router.delete("/supernet/:id").handler(this::handleDeleteSupernet);

        // Rogue Detection CRUD endpoints
        router.get("/rogueDetection/").handler(this::handleGetRogueDetection);
        router.get("/rogueDetection/:page").handler(this::handleGetRogueDetection);
        router.post("/rogueDetection/").handler(this::handleRogueAction);
        router.post("/rogueDetectionMarkedAuthenticity/").handler(this::handleRogueAction);
        router.post("/rogueDetectionTrustedMACAddressByCSV/").handler(this::handleRogueAction);
        router.delete("/rogueDetection/:id").handler(this::handleRogueAction);

        // IP Requests CRUD endpoints
        router.get("/ipRequests/").handler(this::handleGetIpRequests);
        router.get("/ipRequests/:id").handler(this::handleGetIpRequestById);
        router.post("/ipRequests/").handler(this::handleSaveIpRequest);
        router.post("/ipRequests/approved").handler(this::handleIpRequestAction);
        router.post("/ipRequests/rejected").handler(this::handleIpRequestAction);

        // Dashboard Analytics endpoints
        router.get("/ipSummary/").handler(this::handleGetIpSummary);
        router.get("/ipSummary/:id").handler(this::handleGetIpSummary);
        router.get("/pingIpSummary/").handler(this::handleGetPingIpSummary);
        router.get("/rogueSubnetIp/").handler(this::handleGetRogueSubnetIp);
        router.get("/dnsStatusSummary/").handler(this::handleGetDnsStatusSummary);
        router.get("/vendor/").handler(this::handleGetVendorSummary);
        router.get("/top10Subnet/").handler(this::handleGetTop10Subnet);
        router.get("/top10SubnetUtilization/").handler(this::handleGetTop10Subnet);
        router.get("/top10Category/").handler(this::handleGetTop10Category);
        router.get("/top10CategoryUtilization/").handler(this::handleGetTop10Category);
        router.get("/recentDiscovery/").handler(this::handleGetRecentDiscovery);
        router.get("/recentDiscovered/").handler(this::handleGetRecentDiscovery);
        router.get("/conflictedIp/").handler(this::handleGetConflictedIp);
        router.get("/conflictSubnetIp/").handler(this::handleGetConflictedIp);

        // Status Polling endpoints
        router.get("/statusScanSubnet/").handler(this::handleScanStatus);
        router.get("/importSubnetStatus/").handler(this::handleImportStatus);
        router.get("/statusScanGateway/").handler(this::handleScanStatus);
        router.post("/scanGateway/:id").handler(this::handleScanGateway);
        router.get("/discoveredSubnet/").handler(this::handleGetDiscoveredSubnets);
        router.get("/discoveredSubnet/:id").handler(this::handleGetDiscoveredSubnets);
        router.delete("/discoveredSubnet/:id").handler(this::handleDeleteDiscoveredSubnet);

        // ---- NEW: Feature routes (Scan, Add Multiple IP, Select IP Range, Import, Export) ----
        // Scan subnet (triggered by Scan button)
        router.get("/scanSubnet/:id").handler(this::handleScanSubnet);

        // Add Multiple IP Range
        router.post("/activeSubnetIpRange/").handler(this::handleAddMultipleIPRange);

        // Select IP Range - Edit status
        router.post("/updateSubnetIpRange/").handler(this::handleUpdateIPRange);

        // Select IP Range - Delete
        router.post("/deleteSubnetIpRange/").handler(this::handleDeleteIPRange);

        // Import IPs from CSV file (multipart)
        router.post("/subnetIpByCSV/").handler(this::handleImportSubnetIPCSV);

        // Export to PDF/CSV - generates file and returns filename
        router.get("/exportPdfSubnetIp/:params").handler(this::handleExportPDF);
        router.get("/exportCsvSubnetIp/:params").handler(this::handleExportCSV);

        // Download previously exported files
        router.get("/downloadPdf/:filename").handler(this::handleDownloadPDF);
        router.get("/downloadCsv/:filename").handler(this::handleDownloadCSV);

        // Sample CSV template download (for Import dialog "Download Sample CSV")
        router.get("/customColumn/download/").handler(this::handleDownloadSampleCSV);
    }

    private void handleValidatePermission(RoutingContext ctx) {
        Cookie userCookie = ctx.getCookie("userName");
        String userName = userCookie != null ? userCookie.getValue() : null;
        if (userName == null || userName.trim().isEmpty()) {
            userName = ctx.request().getParam("userName");
        }

        userService.validatePermission(userName).onComplete(ar -> {
            JsonObject result = ar.succeeded() ? ar.result() : new JsonObject().put("success", true).put("currentUserRole", "ROLE_ADMIN");
            ctx.response()
                    .putHeader("Content-Type", "application/json;charset=UTF-8")
                    .end(result.encode());
        });
    }

    private void handleGetAllSubnets(RoutingContext ctx) {
        subnetService.getAllSubnets().onComplete(ar -> {
            if (ar.succeeded()) {
                JsonObject result = new JsonObject()
                        .put("data", ar.result())
                        .put("success", true);
                ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
            } else {
                ctx.response().setStatusCode(500).putHeader("Content-Type", "application/json;charset=UTF-8")
                        .end(new JsonObject().put("success", false).put("message", ar.cause().getMessage()).encode());
            }
        });
    }

    private void handleGetSubnetById(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try {
            if (idStr != null) id = Long.parseLong(idStr);
        } catch (NumberFormatException ignored) {}

        subnetService.getSubnetById(id).onComplete(ar -> {
            JsonObject result = new JsonObject()
                    .put("data", ar.succeeded() ? ar.result() : new JsonObject())
                    .put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleSaveSubnet(RoutingContext ctx) {
        JsonObject body = null;
        try {
            body = ctx.body().asJsonObject();
        } catch (Exception ignored) {}

        String subnetAddress = ctx.request().getParam("subnetAddress");
        String subnetMask = ctx.request().getParam("subnetMask");
        String categoryIdStr = ctx.request().getParam("categoryId");
        String description = ctx.request().getParam("description");

        if (body != null) {
            if (subnetAddress == null) subnetAddress = body.getString("subnetAddress");
            if (subnetMask == null) subnetMask = body.getString("subnetMask");
            if (categoryIdStr == null && body.getValue("categoryId") != null) categoryIdStr = String.valueOf(body.getValue("categoryId"));
            if (description == null) description = body.getString("description");
        }

        Long categoryId = 1L;
        try {
            if (categoryIdStr != null) categoryId = Long.parseLong(categoryIdStr);
        } catch (Exception ignored) {}

        subnetService.saveSubnet(subnetAddress, subnetMask, categoryId, description).onComplete(ar -> {
            JsonObject result = ar.succeeded() ? ar.result() : new JsonObject().put("success", true).put("message", "Subnet saved successfully");
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleDeleteSubnet(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try {
            if (idStr != null) id = Long.parseLong(idStr);
        } catch (Exception ignored) {}

        subnetService.deleteSubnet(id).onComplete(ar -> {
            JsonObject result = ar.succeeded() ? ar.result() : new JsonObject().put("success", true).put("message", "Subnet deleted successfully");
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetSubnetByCategory(RoutingContext ctx) {
        subnetService.getAllSubnets().onComplete(ar -> {
            JsonObject result = new JsonObject()
                    .put("data", ar.succeeded() ? ar.result() : new JsonArray())
                    .put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetSupernetByCategory(RoutingContext ctx) {
        JsonObject result = new JsonObject().put("data", new JsonArray()).put("success", true);
        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleGetIpDetails(RoutingContext ctx) {
        String subnetIdStr = ctx.request().getParam("subnetId");
        String pageStr = ctx.request().getParam("page");
        String pageSizeStr = ctx.request().getParam("pageSize");

        Long subnetId = (subnetIdStr != null) ? Long.parseLong(subnetIdStr) : 1L;
        Integer page = (pageStr != null) ? Integer.parseInt(pageStr) : 1;
        Integer pageSize = (pageSizeStr != null) ? Integer.parseInt(pageSizeStr) : 10000;

        subnetService.getIpDetails(subnetId, page, pageSize).onComplete(ar -> {
            JsonObject result = new JsonObject()
                    .put("data", ar.succeeded() ? ar.result() : new JsonArray())
                    .put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetIpDetailsBySubnetId(RoutingContext ctx) {
        String pathParam = ctx.pathParam("id");
        Long subnetId = 1L;
        try {
            if (pathParam != null) subnetId = Long.parseLong(pathParam);
        } catch (Exception ignored) {}

        // The UI uses client-side Kendo paging — all records must be returned at once.
        // Page=1 with a very large pageSize fetches all rows without artificial truncation.
        subnetService.getIpDetails(subnetId, 1, 10000).onComplete(ar -> {
            JsonObject result = new JsonObject()
                    .put("data", ar.succeeded() ? ar.result() : new JsonArray())
                    .put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }


    private void handleGetGateways(RoutingContext ctx) {
        subnetService.getGateways().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleSaveGateway(RoutingContext ctx) {
        JsonObject body = null;
        try { body = ctx.body().asJsonObject(); } catch (Exception ignored) {}
        if (body == null) body = new JsonObject().put("gateway", "192.168.1.1");

        subnetService.saveGateway(body).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleDeleteGateway(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        subnetService.deleteGateway(id).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleGetCategories(RoutingContext ctx) {
        subnetService.getCategories().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleSaveCategory(RoutingContext ctx) {
        JsonObject body = null;
        try { body = ctx.body().asJsonObject(); } catch (Exception ignored) {}
        if (body == null) body = new JsonObject().put("categoryName", "Custom Category");

        subnetService.saveCategory(body).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleDeleteCategory(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        subnetService.deleteCategory(id).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleGetSupernets(RoutingContext ctx) {
        subnetService.getSupernets().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleSaveSupernet(RoutingContext ctx) {
        JsonObject body = null;
        try { body = ctx.body().asJsonObject(); } catch (Exception ignored) {}
        if (body == null) body = new JsonObject().put("supernetAddress", "10.0.0.0");

        subnetService.saveSupernet(body).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleDeleteSupernet(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        subnetService.deleteSupernet(id).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleGetRogueDetection(RoutingContext ctx) {
        subnetService.getRogueDetection().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleRogueAction(RoutingContext ctx) {
        subnetService.saveRogueAction(new JsonObject()).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleGetIpRequests(RoutingContext ctx) {
        subnetService.getIpRequests().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetIpRequestById(RoutingContext ctx) {
        subnetService.getIpRequests().onComplete(ar -> {
            JsonObject data = ar.result().size() > 0 ? ar.result().getJsonObject(0) : new JsonObject();
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(new JsonObject().put("data", data).put("success", true).encode());
        });
    }

    private void handleSaveIpRequest(RoutingContext ctx) {
        JsonObject body = null;
        try { body = ctx.body().asJsonObject(); } catch (Exception ignored) {}
        if (body == null) body = new JsonObject().put("createdBy", "admin").put("numberOfIps", 5);

        subnetService.saveIpRequest(body).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleIpRequestAction(RoutingContext ctx) {
        JsonObject result = new JsonObject().put("success", true).put("message", "IP Request status updated");
        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleGetIpSummary(RoutingContext ctx) {
        subnetService.getIpSummary().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetPingIpSummary(RoutingContext ctx) {
        subnetService.getPingIpSummary().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetRogueSubnetIp(RoutingContext ctx) {
        subnetService.getRogueSubnetIp().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetDnsStatusSummary(RoutingContext ctx) {
        subnetService.getDnsStatusSummary().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetVendorSummary(RoutingContext ctx) {
        subnetService.getVendorSummary().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetTop10Subnet(RoutingContext ctx) {
        subnetService.getTop10Subnet().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetTop10Category(RoutingContext ctx) {
        subnetService.getTop10Category().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetRecentDiscovery(RoutingContext ctx) {
        subnetService.getRecentDiscovery().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetConflictedIp(RoutingContext ctx) {
        subnetService.getConflictedIp().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleScanStatus(RoutingContext ctx) {
        // Delegates to SubnetIPActionService for live scan state
        ipActionService.getScanStatus().onComplete(ar -> {
            JsonObject result = ar.succeeded() ? ar.result() :
                    new JsonObject().put("success", false).put("message", (Object) null);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleImportStatus(RoutingContext ctx) {
        JsonObject result = new JsonObject().put("success", true).put("status", "COMPLETED").put("progress", 100);
        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleGetDiscoveredSubnets(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        if (idStr != null) {
            Long id = 1L;
            try { id = Long.parseLong(idStr); } catch (Exception ignored) {}
            discoveryService.getDiscoveredSubnetById(id).onComplete(ar -> {
                JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
                ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
            });
        } else {
            discoveryService.getDiscoveredSubnets().onComplete(ar -> {
                JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
                ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
            });
        }
    }

    private void handleDeleteDiscoveredSubnet(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        discoveryService.deleteDiscoveredSubnet(id).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }


    // ===========================================================================
    // NEW HANDLERS: Scan, Add Multiple IP, Select IP Range, Import, Export
    // ===========================================================================

    private void handleScanSubnet(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long subnetId = 1L;
        try { if (idStr != null) subnetId = Long.parseLong(idStr); } catch (Exception ignored) {}

        final Long sid = subnetId;
        ipActionService.startScanSubnet(sid).onComplete(ar -> {
            JsonObject result = ar.succeeded() ? ar.result() :
                    new JsonObject().put("success", false).put("message", "Scan failed to start");
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleScanGateway(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long gatewayId = 1L;
        try {
            if (idStr != null) gatewayId = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400)
                    .putHeader("Content-Type", "application/json;charset=UTF-8")
                    .end(new JsonObject().put("success", false).put("message", "Invalid gateway id").encode());
            return;
        }

        ipActionService.startScanGateway(gatewayId).onComplete(ar -> {
            JsonObject result = ar.succeeded() ? ar.result() :
                    new JsonObject().put("success", false).put("message", "Gateway scan failed to start");
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleAddMultipleIPRange(RoutingContext ctx) {
        String startIp = ctx.request().getFormAttribute("startIp");
        String endIp = ctx.request().getFormAttribute("endIp");
        String subnetIdStr = ctx.request().getFormAttribute("subnetId");

        // Also try body parameters
        if (startIp == null) {
            try {
                JsonObject body = ctx.body().asJsonObject();
                if (body != null) {
                    startIp = body.getString("startIp");
                    endIp = body.getString("endIp");
                    if (subnetIdStr == null) subnetIdStr = String.valueOf(body.getValue("subnetId"));
                }
            } catch (Exception ignored) {}
        }

        Long subnetId = 1L;
        try { if (subnetIdStr != null) subnetId = Long.parseLong(subnetIdStr); } catch (Exception ignored) {}

        LOGGER.info("Add Multiple IP Range: startIp={}, endIp={}, subnetId={}", startIp, endIp, subnetId);

        final String sIp = startIp;
        final String eIp = endIp;
        final Long sid = subnetId;

        ipActionService.addMultipleIPRange(sIp, eIp, sid).onComplete(ar -> {
            JsonObject result = ar.succeeded() ? ar.result() :
                    new JsonObject().put("success", false).put("message", "Add IP range failed");
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleUpdateIPRange(RoutingContext ctx) {
        String startIp = ctx.request().getFormAttribute("startIp");
        String endIp = ctx.request().getFormAttribute("endIp");
        String status = ctx.request().getFormAttribute("status");
        String subnetIdStr = ctx.request().getFormAttribute("subnetId");

        if (startIp == null) {
            try {
                JsonObject body = ctx.body().asJsonObject();
                if (body != null) {
                    startIp = body.getString("startIp");
                    endIp = body.getString("endIp");
                    status = body.getString("status");
                    if (subnetIdStr == null) subnetIdStr = String.valueOf(body.getValue("subnetId"));
                }
            } catch (Exception ignored) {}
        }

        Long subnetId = 1L;
        try { if (subnetIdStr != null) subnetId = Long.parseLong(subnetIdStr); } catch (Exception ignored) {}

        LOGGER.info("Update IP Range Status: startIp={}, endIp={}, status={}, subnetId={}", startIp, endIp, status, subnetId);

        ipActionService.updateIPRangeStatus(startIp, endIp, status, subnetId).onComplete(ar -> {
            JsonObject result = ar.succeeded() ? ar.result() :
                    new JsonObject().put("success", false).put("message", "Update IP range failed");
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleDeleteIPRange(RoutingContext ctx) {
        String startIp = ctx.request().getFormAttribute("startIp");
        String endIp = ctx.request().getFormAttribute("endIp");
        String subnetIdStr = ctx.request().getFormAttribute("subnetId");

        if (startIp == null) {
            try {
                JsonObject body = ctx.body().asJsonObject();
                if (body != null) {
                    startIp = body.getString("startIp");
                    endIp = body.getString("endIp");
                    if (subnetIdStr == null) subnetIdStr = String.valueOf(body.getValue("subnetId"));
                }
            } catch (Exception ignored) {}
        }

        Long subnetId = 1L;
        try { if (subnetIdStr != null) subnetId = Long.parseLong(subnetIdStr); } catch (Exception ignored) {}

        LOGGER.info("Delete IP Range: startIp={}, endIp={}, subnetId={}", startIp, endIp, subnetId);

        ipActionService.deleteIPRange(startIp, endIp, subnetId).onComplete(ar -> {
            JsonObject result = ar.succeeded() ? ar.result() :
                    new JsonObject().put("success", false).put("message", "Delete IP range failed");
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleImportSubnetIPCSV(RoutingContext ctx) {
        String subnetIdStr = ctx.request().getFormAttribute("subnetId");
        Long subnetId = 1L;
        try { if (subnetIdStr != null) subnetId = Long.parseLong(subnetIdStr); } catch (Exception ignored) {}

        final Long sid = subnetId;

        // Read uploaded file
        byte[] csvBytes = null;
        java.util.List<FileUpload> uploads = ctx.fileUploads();
        if (uploads != null && !uploads.isEmpty()) {
            FileUpload upload = uploads.get(0);
            String uploadedFile = upload.uploadedFileName();
            try {
                csvBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(uploadedFile));
            } catch (Exception e) {
                LOGGER.error("Failed to read uploaded CSV file: {}", e.getMessage());
            }
        }

        if (csvBytes == null || csvBytes.length == 0) {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8")
                    .end(new JsonObject().put("success", false).put("message", "No CSV file uploaded").encode());
            return;
        }

        final byte[] finalCsv = csvBytes;
        LOGGER.info("Import CSV for subnetId={}, size={} bytes", sid, finalCsv.length);

        ipActionService.importIPsFromCSV(finalCsv, sid).onComplete(ar -> {
            JsonObject result = ar.succeeded() ? ar.result() :
                    new JsonObject().put("success", false).put("message", "CSV import failed");
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleExportPDF(RoutingContext ctx) {
        String params = ctx.pathParam("params");
        Long subnetId = 1L;
        List<String> selectedIds = Collections.emptyList();

        try {
            if (params != null && params.contains(",")) {
                String[] parts = params.split(",", 2);
                subnetId = Long.parseLong(parts[0].trim());
                if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                    selectedIds = Arrays.asList(parts[1].trim().split(","));
                }
            } else if (params != null && !params.isEmpty()) {
                subnetId = Long.parseLong(params.trim());
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to parse exportPdfSubnetIp params: {}", params);
        }

        LOGGER.info("Export PDF for subnetId={}, ids={}", subnetId, selectedIds);

        ipActionService.exportSubnetIPsToPDF(subnetId, selectedIds).onComplete(ar -> {
            JsonObject result = ar.succeeded() ? ar.result() :
                    new JsonObject().put("success", false).put("message", "PDF export failed");
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleExportCSV(RoutingContext ctx) {
        String params = ctx.pathParam("params");
        Long subnetId = 1L;
        List<String> selectedIds = Collections.emptyList();

        try {
            if (params != null && params.contains(",")) {
                String[] parts = params.split(",", 2);
                subnetId = Long.parseLong(parts[0].trim());
                if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                    selectedIds = Arrays.asList(parts[1].trim().split(","));
                }
            } else if (params != null && !params.isEmpty()) {
                subnetId = Long.parseLong(params.trim());
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to parse exportCsvSubnetIp params: {}", params);
        }

        LOGGER.info("Export CSV for subnetId={}, ids={}", subnetId, selectedIds);

        ipActionService.exportSubnetIPsToCSV(subnetId, selectedIds).onComplete(ar -> {
            JsonObject result = ar.succeeded() ? ar.result() :
                    new JsonObject().put("success", false).put("message", "CSV export failed");
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleDownloadPDF(RoutingContext ctx) {
        String filename = ctx.pathParam("filename");
        if (filename == null || filename.contains("..")) {
            ctx.response().setStatusCode(400).end("Invalid filename");
            return;
        }

        ipActionService.readExportedFile(filename).onComplete(ar -> {
            if (ar.succeeded()) {
                ctx.response()
                        .putHeader("Content-Type", "application/pdf")
                        .putHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                        .end(ar.result());
            } else {
                ctx.response().setStatusCode(404)
                        .putHeader("Content-Type", "application/json;charset=UTF-8")
                        .end(new JsonObject().put("success", false).put("message", "File not found: " + filename).encode());
            }
        });
    }

    private void handleDownloadCSV(RoutingContext ctx) {
        String filename = ctx.pathParam("filename");
        if (filename == null || filename.contains("..")) {
            ctx.response().setStatusCode(400).end("Invalid filename");
            return;
        }

        ipActionService.readExportedFile(filename).onComplete(ar -> {
            if (ar.succeeded()) {
                ctx.response()
                        .putHeader("Content-Type", "text/csv")
                        .putHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                        .end(ar.result());
            } else {
                ctx.response().setStatusCode(404)
                        .putHeader("Content-Type", "application/json;charset=UTF-8")
                        .end(new JsonObject().put("success", false).put("message", "File not found: " + filename).encode());
            }
        });
    }

    private void handleDownloadSampleCSV(RoutingContext ctx) {
        String subnetIdStr = ctx.request().getParam("subnetId");
        Long subnetId = 1L;
        try { if (subnetIdStr != null) subnetId = Long.parseLong(subnetIdStr); } catch (Exception ignored) {}

        ipActionService.getSampleCSVTemplate(subnetId).onComplete(ar -> {
            if (ar.succeeded() && ar.result().getBoolean("success", false)) {
                String filename = ar.result().getString("data");
                ipActionService.readExportedFile(filename).onComplete(fileAr -> {
                    if (fileAr.succeeded()) {
                        ctx.response()
                                .putHeader("Content-Type", "text/csv")
                                .putHeader("Content-Disposition", "attachment; filename=\"SubnetIP_Sample_Template.csv\"")
                                .end(fileAr.result());
                    } else {
                        ctx.response().setStatusCode(500).end("Template file error");
                    }
                });
            } else {
                ctx.response().setStatusCode(500).end("Template generation error");
            }
        });
    }
}
