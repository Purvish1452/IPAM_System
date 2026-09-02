package main

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestHealthEndpoint(t *testing.T) {
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

	var resp map[string]interface{}
	if err := json.Unmarshal(rr.Body.Bytes(), &resp); err != nil {
		t.Fatalf("Failed to parse JSON response: %v", err)
	}

	if resp["status"] != "UP" {
		t.Errorf("Expected status UP, got %v", resp["status"])
	}
}

func TestScanSubnetEndpoint(t *testing.T) {
	payload := ScanRequest{
		SubnetCIDR:  "127.0.0.1/30",
		TimeoutMs:   100,
		Concurrency: 2,
	}
	body, _ := json.Marshal(payload)

	req, err := http.NewRequest("POST", "/api/v1/scan/subnet", bytes.NewBuffer(body))
	if err != nil {
		t.Fatalf("Failed to create request: %v", err)
	}
	req.Header.Set("Content-Type", "application/json")

	rr := httptest.NewRecorder()
	handler := http.HandlerFunc(scanSubnetHandler)
	handler.ServeHTTP(rr, req)

	if status := rr.Code; status != http.StatusOK {
		t.Errorf("Scan endpoint returned wrong status code: got %v want %v", status, http.StatusOK)
	}

	var resp ScanResponse
	if err := json.Unmarshal(rr.Body.Bytes(), &resp); err != nil {
		t.Fatalf("Failed to parse JSON response: %v", err)
	}

	if resp.SubnetCIDR != "127.0.0.1/30" {
		t.Errorf("Expected CIDR 127.0.0.1/30, got %v", resp.SubnetCIDR)
	}
}
