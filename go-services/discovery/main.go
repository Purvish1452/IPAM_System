package main

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net"
	"net/http"
	"os"
	"os/signal"
	"strings"
	"sync"
	"syscall"
	"time"

	"com.motadata/ipam/go-services/common"
)

// ScanRequest represents the request sent by the client or CLI.
type ScanRequest struct {
	SubnetCIDR  string `json:"subnetCidr"`
	TimeoutMs   int    `json:"timeoutMs"`
	Concurrency int    `json:"concurrency"`
}

// HostResult represents the result for one IP address.
type HostResult struct {
	IP       string `json:"ip"`
	Status   string `json:"status"`
	Hostname string `json:"hostname,omitempty"`
	RTTMs    int64  `json:"rttMs"`
}

// ScanResponse represents the complete scanning response.
type ScanResponse struct {
	SubnetCIDR  string       `json:"subnetCidr"`
	TotalHosts  int          `json:"totalHosts"`
	ActiveCount int          `json:"activeCount"`
	Hosts       []HostResult `json:"hosts"`
	DurationMs  int64        `json:"durationMs"`
}

func main() {
	args := os.Args[1:]

	// Check if invoked in CLI / JSON Plugin mode via arguments or stdin
	var rawInput string
	for i, arg := range args {
		if arg == "--json" && i+1 < len(args) {
			rawInput = args[i+1]
			break
		} else if strings.HasPrefix(arg, "{") && strings.HasSuffix(arg, "}") {
			rawInput = arg
			break
		}
	}

	// Check stdin if not passed via arguments and stdin is not a terminal
	if rawInput == "" {
		stat, _ := os.Stdin.Stat()
		if (stat.Mode() & os.ModeCharDevice) == 0 {
			bytes, err := io.ReadAll(os.Stdin)
			if err == nil && len(bytes) > 0 && strings.HasPrefix(strings.TrimSpace(string(bytes)), "{") {
				rawInput = string(bytes)
			}
		}
	}

	// If JSON input is detected, execute in Plugin Mode and output JSON to stdout
	if rawInput != "" {
		var req ScanRequest
		if err := json.Unmarshal([]byte(rawInput), &req); err != nil {
			fmt.Fprintf(os.Stderr, "Error parsing JSON input: %v\n", err)
			os.Exit(1)
		}
		resp, err := performScan(req)
		if err != nil {
			fmt.Fprintf(os.Stderr, "Scan error: %v\n", err)
			os.Exit(1)
		}
		output, _ := json.Marshal(resp)
		fmt.Println(string(output))
		return
	}

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

// performScan executes the CIDR sweep and returns the structured ScanResponse.
func performScan(req ScanRequest) (*ScanResponse, error) {
	if req.SubnetCIDR == "" {
		return nil, fmt.Errorf("subnetCidr is required")
	}

	if req.TimeoutMs <= 0 {
		req.TimeoutMs = 1000
	}

	if req.Concurrency <= 0 {
		req.Concurrency = 500
	}

	startTime := time.Now()

	ips, err := common.ExpandCIDR(req.SubnetCIDR)
	if err != nil {
		return nil, fmt.Errorf("invalid CIDR: %w", err)
	}

	results := scanIPs(ips, req.TimeoutMs, req.Concurrency)

	activeCount := 0
	for _, res := range results {
		if res.Status == "UP" {
			activeCount++
		}
	}

	return &ScanResponse{
		SubnetCIDR:  req.SubnetCIDR,
		TotalHosts:  len(ips),
		ActiveCount: activeCount,
		Hosts:       results,
		DurationMs:  time.Since(startTime).Milliseconds(),
	}, nil
}

// Returns the health status of the discovery microservice.
func healthHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"status":    "UP",
		"service":   "go-discovery",
		"timestamp": time.Now().Unix(),
	})
}

// Receives the API request and creates the final response
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

	resp, err := performScan(req)
	if err != nil {
		http.Error(w, err.Error(), http.StatusBadRequest)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

// Performs concurrent scanning using worker goroutines
func scanIPs(ips []string, timeoutMs int, concurrency int) []HostResult {
	ipChan := make(chan string, len(ips))
	for _, ip := range ips {
		ipChan <- ip
	}
	close(ipChan)

	resultsChan := make(chan HostResult, len(ips))
	var wg sync.WaitGroup

	if concurrency > len(ips) && len(ips) > 0 {
		concurrency = len(ips)
	}
	if concurrency <= 0 {
		concurrency = 32
	}

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

// Checks an individual IP and resolves its hostname
func pingAndResolve(ip string, timeoutMs int) HostResult {
	start := time.Now()
	timeout := time.Duration(timeoutMs) * time.Millisecond
	if timeout > 2500*time.Millisecond {
		timeout = 2500 * time.Millisecond
	}

	status := "DOWN"

	// Probe common ports for host responsiveness (80, 443, 22, 53, 8080)
	probePorts := []string{"80", "443", "22", "53", "8080", "3389", "5432"}
	for _, port := range probePorts {
		conn, err := net.DialTimeout("tcp", net.JoinHostPort(ip, port), timeout)
		if err == nil {
			status = "UP"
			conn.Close()
			break
		}
	}

	if status == "DOWN" && (ip == "127.0.0.1" || ip == "localhost") {
		conn, err := net.DialTimeout("tcp", net.JoinHostPort(ip, "8080"), timeout)
		if err == nil {
			status = "UP"
			conn.Close()
		}
	}

	rtt := time.Since(start).Milliseconds()

	var hostname string
	if status == "UP" {
		names, err := net.LookupAddr(ip)
		if err == nil && len(names) > 0 {
			hostname = strings.TrimSuffix(names[0], ".")
		}
	}

	return HostResult{
		IP:       ip,
		Status:   status,
		Hostname: hostname,
		RTTMs:    rtt,
	}
}
