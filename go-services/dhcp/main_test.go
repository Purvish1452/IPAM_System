package main

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
)

// Tests the DHCP service health check endpoint.
func TestDHCPHealthEndpoint(t *testing.T) {
	req, err := http.NewRequest("GET", "/health", nil)
	if err != nil {
		t.Fatalf("Failed to create request: %v", err)
	}

	rr := httptest.NewRecorder()
	handler := http.HandlerFunc(healthHandler)
	handler.ServeHTTP(rr, req)

	if status := rr.Code; status != http.StatusOK {
		t.Errorf("Health check returned wrong status code: got %v want %v", status, http.StatusOK)
	}

	var body map[string]interface{}
	if err := json.Unmarshal(rr.Body.Bytes(), &body); err != nil {
		t.Fatalf("Failed to parse health JSON: %v", err)
	}
	if body["status"] != "UP" {
		t.Errorf("Expected status UP, got %v", body["status"])
	}
}

// Tests the DHCP scan endpoint for Windows DHCP servers.
func TestDHCPScanEndpoint_Windows(t *testing.T) {
	payload := DHCPScanRequest{
		HostAddress: "192.168.10.1",
		Type:        "windows",
		UserName:    "admin",
		Port:        5985,
	}
	body, _ := json.Marshal(payload)

	req, err := http.NewRequest("POST", "/api/v1/dhcp/scan", bytes.NewBuffer(body))
	if err != nil {
		t.Fatalf("Failed to create request: %v", err)
	}
	req.Header.Set("Content-Type", "application/json")

	rr := httptest.NewRecorder()
	handler := http.HandlerFunc(scanDhcpHandler)
	handler.ServeHTTP(rr, req)

	if status := rr.Code; status != http.StatusOK {
		t.Errorf("DHCP Scan endpoint returned wrong status code: got %v want %v", status, http.StatusOK)
	}

	var resp DHCPScanResponse
	if err := json.Unmarshal(rr.Body.Bytes(), &resp); err != nil {
		t.Fatalf("Failed to parse JSON response: %v", err)
	}

	if resp.HostAddress != "192.168.10.1" {
		t.Errorf("Expected hostAddress 192.168.10.1, got %v", resp.HostAddress)
	}
	if resp.Status != "SUCCESS" {
		t.Errorf("Expected status SUCCESS, got %v", resp.Status)
	}
	if len(resp.Scopes) == 0 {
		t.Errorf("Expected non-empty DHCP scopes list")
	}

	firstScope := resp.Scopes[0]
	if firstScope.TotalIPs <= 0 {
		t.Errorf("Expected TotalIPs > 0, got %d", firstScope.TotalIPs)
	}
	if firstScope.UsedIPs+firstScope.FreeIPs != firstScope.TotalIPs {
		t.Errorf("Expected UsedIPs (%d) + FreeIPs (%d) == TotalIPs (%d)", firstScope.UsedIPs, firstScope.FreeIPs, firstScope.TotalIPs)
	}
	if firstScope.Utilization < 0 || firstScope.Utilization > 100 {
		t.Errorf("Utilization out of range: %v", firstScope.Utilization)
	}
}

// Tests the DHCP scan endpoint for Cisco DHCP servers.
func TestDHCPScanEndpoint_Cisco(t *testing.T) {
	payload := DHCPScanRequest{
		HostAddress: "10.1.0.1",
		Type:        "cisco",
		Port:        22,
	}
	body, _ := json.Marshal(payload)

	req, err := http.NewRequest("POST", "/api/v1/dhcp/scan", bytes.NewBuffer(body))
	if err != nil {
		t.Fatalf("Failed to create request: %v", err)
	}
	req.Header.Set("Content-Type", "application/json")

	rr := httptest.NewRecorder()
	handler := http.HandlerFunc(scanDhcpHandler)
	handler.ServeHTTP(rr, req)

	if status := rr.Code; status != http.StatusOK {
		t.Errorf("Expected 200 OK, got %v", status)
	}

	var resp DHCPScanResponse
	if err := json.Unmarshal(rr.Body.Bytes(), &resp); err != nil {
		t.Fatalf("Failed to parse response: %v", err)
	}

	if resp.ServerType != "cisco" {
		t.Errorf("Expected serverType cisco, got %v", resp.ServerType)
	}
	if len(resp.Scopes) == 0 {
		t.Errorf("Expected scopes to be populated")
	}
}

// Tests the DHCP scan endpoint with user-supplied custom scope CIDRs.
func TestDHCPScanEndpoint_CustomScopes(t *testing.T) {
	payload := DHCPScanRequest{
		HostAddress: "172.16.1.1",
		Type:        "linux",
		Scopes:      []string{"172.16.50.0/24", "172.16.60.0/24"},
	}
	body, _ := json.Marshal(payload)

	req, err := http.NewRequest("POST", "/api/v1/dhcp/scan", bytes.NewBuffer(body))
	if err != nil {
		t.Fatalf("Failed to create request: %v", err)
	}
	req.Header.Set("Content-Type", "application/json")

	rr := httptest.NewRecorder()
	handler := http.HandlerFunc(scanDhcpHandler)
	handler.ServeHTTP(rr, req)

	var resp DHCPScanResponse
	json.Unmarshal(rr.Body.Bytes(), &resp)

	if len(resp.Scopes) != 2 {
		t.Errorf("Expected 2 custom scopes, got %d", len(resp.Scopes))
	}
	if resp.Scopes[0].ScopeID != "172.16.50.0" {
		t.Errorf("Expected scopeId 172.16.50.0, got %v", resp.Scopes[0].ScopeID)
	}
}

// Tests error handling for invalid or missing parameters in DHCP scan.
func TestDHCPScanEndpoint_InvalidInputs(t *testing.T) {
	// 1. Missing hostAddress
	payload := DHCPScanRequest{HostAddress: ""}
	body, _ := json.Marshal(payload)
	req, _ := http.NewRequest("POST", "/api/v1/dhcp/scan", bytes.NewBuffer(body))
	rr := httptest.NewRecorder()
	scanDhcpHandler(rr, req)
	if rr.Code != http.StatusBadRequest {
		t.Errorf("Expected 400 for empty hostAddress, got %d", rr.Code)
	}

	// 2. Invalid method GET
	reqGet, _ := http.NewRequest("GET", "/api/v1/dhcp/scan", nil)
	rrGet := httptest.NewRecorder()
	scanDhcpHandler(rrGet, reqGet)
	if rrGet.Code != http.StatusMethodNotAllowed {
		t.Errorf("Expected 405 Method Not Allowed, got %d", rrGet.Code)
	}
}

// Tests helper functions for deriving scopes and pseudo-MAC generation.
func TestHelperFunctions(t *testing.T) {
	scopes := deriveDefaultScopes("192.168.10.5", "windows")
	if len(scopes) != 2 {
		t.Errorf("Expected 2 derived scopes, got %d", len(scopes))
	}
	if !strings.Contains(scopes[0], "192.168.10.") {
		t.Errorf("Expected scope matching subnet 192.168.10, got %s", scopes[0])
	}

	mac := generatePseudoMAC("192.168.1.55")
	if !strings.HasPrefix(mac, "00:50:56:") {
		t.Errorf("Unexpected MAC format: %s", mac)
	}
}
