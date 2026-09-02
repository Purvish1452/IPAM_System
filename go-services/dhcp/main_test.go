package main

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
)

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
}

func TestDHCPScanEndpoint(t *testing.T) {
	payload := DHCPScanRequest{
		HostAddress: "10.0.0.1",
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

	if resp.HostAddress != "10.0.0.1" {
		t.Errorf("Expected hostAddress 10.0.0.1, got %v", resp.HostAddress)
	}

	if len(resp.Scopes) == 0 {
		t.Errorf("Expected non-empty DHCP scopes list")
	}
}
