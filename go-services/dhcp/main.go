package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"
)

//This represents the request coming from another application.
type DHCPScanRequest struct {
	HostAddress string `json:"hostAddress"`
	Type        string `json:"type"` // "windows" or "cisco"
	UserName    string `json:"userName"`
	Password    string `json:"password"`
	Port        int    `json:"port"`
}

//This represents information about one DHCP scope.
type DHCPScopeStats struct {
	ScopeID     string `json:"scopeId"`
	SubnetName  string `json:"subnetName"`
	TotalIPs    int    `json:"totalIps"`
	UsedIPs     int    `json:"usedIps"`
	FreeIPs     int    `json:"freeIps"`
	Utilization float64 `json:"utilization"`
}

//This is the final response returned to the caller.
type DHCPScanResponse struct {
	HostAddress string           `json:"hostAddress"`
	ServerType  string           `json:"serverType"`
	Status      string           `json:"status"`
	Scopes      []DHCPScopeStats `json:"scopes"`
	ScanTime    string           `json:"scanTime"`
	DurationMs  int64            `json:"durationMs"`
}

func main() {
	port := "8082"

	//This allows deployment systems to change the port in env only.
	if envPort := os.Getenv("PORT"); envPort != "" {
		port = envPort
	}

	http.HandleFunc("/health", healthHandler)
	http.HandleFunc("/api/v1/dhcp/scan", scanDhcpHandler)

	server := &http.Server{
		Addr:         ":" + port,
		ReadTimeout:  15 * time.Second,  //Maximum time allowed to read the incoming HTTP request.
		WriteTimeout: 60 * time.Second,  //Maximum time allowed to write the HTTP response.
	}

	go func() {
		log.Printf("IPAM DHCP Collector Golang Microservice listening on port %s...", port)
		if err := server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("HTTP server failure: %v", err)
		}
	}()

//Creates a channel for OS signals.
	stop := make(chan os.Signal, 1)
	signal.Notify(stop, os.Interrupt, syscall.SIGTERM)
	<-stop
	log.Println("Shutting down IPAM DHCP Microservice gracefully...")
}

func healthHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"status": "UP",
		"service": "go-dhcp",
		"timestamp": time.Now().Unix(),
	})
}

func scanDhcpHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req DHCPScanRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid JSON payload", http.StatusBadRequest)
		return
	}

	if req.HostAddress == "" {
		http.Error(w, "hostAddress parameter is required", http.StatusBadRequest)
		return
	}

	startTime := time.Now()
	serverType := strings.ToLower(req.Type)
	if serverType == "" {
		serverType = "windows"
	}

	scopes := collectDHCPScopes(req.HostAddress, serverType)

	resp := DHCPScanResponse{
		HostAddress: req.HostAddress,
		ServerType:  serverType,
		Status:      "SUCCESS",
		Scopes:      scopes,
		ScanTime:    time.Now().Format(time.RFC3339),
		DurationMs:  time.Since(startTime).Milliseconds(),
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

func collectDHCPScopes(host string, serverType string) []DHCPScopeStats {
	// Sample scope details returned by WinRM / Cisco CLI parser
	return []DHCPScopeStats{
		{
			ScopeID:     "192.168.1.0",
			SubnetName:  fmt.Sprintf("%s-Scope-1", strings.ToUpper(serverType)),
			TotalIPs:    254,
			UsedIPs:     45,
			FreeIPs:     209,
			Utilization: 17.71,
		},
		{
			ScopeID:     "10.0.0.0",
			SubnetName:  fmt.Sprintf("%s-Scope-2", strings.ToUpper(serverType)),
			TotalIPs:    510,
			UsedIPs:     120,
			FreeIPs:     390,
			Utilization: 23.52,
		},
	}
}
