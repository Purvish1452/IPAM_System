package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net"
	"net/http"
	"os"
	"os/signal"
	"sync"
	"syscall"
	"time"

	"com.motadata/ipam/go-services/common"
)

type ScanRequest struct {
	SubnetCIDR string `json:"subnetCidr"`
	TimeoutMs  int    `json:"timeoutMs"`
	Concurrency int   `json:"concurrency"`
}

type HostResult struct {
	IP       string `json:"ip"`
	Status   string `json:"status"`
	Hostname string `json:"hostname,omitempty"`
	RTTMs    int64  `json:"rttMs"`
}

type ScanResponse struct {
	SubnetCIDR string       `json:"subnetCidr"`
	TotalHosts int          `json:"totalHosts"`
	ActiveCount int         `json:"activeCount"`
	Hosts      []HostResult `json:"hosts"`
	DurationMs int64        `json:"durationMs"`
}

func main() {
	port := "8081"
	if envPort := os.Getenv("PORT"); envPort != "" {
		port = envPort
	}

	http.HandleFunc("/health", healthHandler)
	http.HandleFunc("/api/v1/scan/subnet", scanSubnetHandler)

	server := &http.Server{
		Addr:         ":" + port,
		ReadTimeout:  15 * time.Second,
		WriteTimeout: 60 * time.Second,
	}

	go func() {
		log.Printf("IPAM Discovery Golang Microservice listening on port %s...", port)
		if err := server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("HTTP server failure: %v", err)
		}
	}()

	stop := make(chan os.Signal, 1)
	signal.Notify(stop, os.Interrupt, syscall.SIGTERM)
	<-stop
	log.Println("Shutting down IPAM Discovery Microservice gracefully...")
}

func healthHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"status": "UP",
		"service": "go-discovery",
		"timestamp": time.Now().Unix(),
	})
}

func scanSubnetHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req ScanRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid JSON payload", http.StatusBadRequest)
		return
	}

	if req.SubnetCIDR == "" {
		http.Error(w, "subnetCidr parameter is required", http.StatusBadRequest)
		return
	}

	if req.TimeoutMs <= 0 {
		req.TimeoutMs = 1000
	}

	if req.Concurrency <= 0 {
		req.Concurrency = 50
	}

	startTime := time.Now()

	ips, err := common.ExpandCIDR(req.SubnetCIDR)
	if err != nil {
		http.Error(w, fmt.Sprintf("Invalid CIDR format: %v", err), http.StatusBadRequest)
		return
	}

	results := scanIPs(ips, req.TimeoutMs, req.Concurrency)

	activeCount := 0
	for _, res := range results {
		if res.Status == "UP" {
			activeCount++
		}
	}

	resp := ScanResponse{
		SubnetCIDR:  req.SubnetCIDR,
		TotalHosts:  len(ips),
		ActiveCount: activeCount,
		Hosts:       results,
		DurationMs:  time.Since(startTime).Milliseconds(),
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

func scanIPs(ips []string, timeoutMs int, concurrency int) []HostResult {
	ipChan := make(chan string, len(ips))
	for _, ip := range ips {
		ipChan <- ip
	}
	close(ipChan)

	resultsChan := make(chan HostResult, len(ips))
	var wg sync.WaitGroup

	for i := 0; i < concurrency; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			for ip := range ipChan {
				res := pingAndResolve(ip, timeoutMs)
				resultsChan <- res
			}
		}()
	}

	wg.Wait()
	close(resultsChan)

	var results []HostResult
	for res := range resultsChan {
		results = append(results, res)
	}

	return results
}

func pingAndResolve(ip string, timeoutMs int) HostResult {
	start := time.Now()
	timeout := time.Duration(timeoutMs) * time.Millisecond

	conn, err := net.DialTimeout("tcp", net.JoinHostPort(ip, "80"), timeout)
	status := "DOWN"
	if err == nil {
		status = "UP"
		conn.Close()
	} else {
		// Fallback check port 443 / ICMP probe simulation
		conn443, err443 := net.DialTimeout("tcp", net.JoinHostPort(ip, "443"), timeout)
		if err443 == nil {
			status = "UP"
			conn443.Close()
		}
	}

	rtt := time.Since(start).Milliseconds()

	var hostname string
	if status == "UP" {
		names, err := net.LookupAddr(ip)
		if err == nil && len(names) > 0 {
			hostname = names[0]
		}
	}

	return HostResult{
		IP:       ip,
		Status:   status,
		Hostname: hostname,
		RTTMs:    rtt,
	}
}
