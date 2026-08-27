package main

import (
	"context"
	"encoding/json"
	"fmt"
	"os"
	"os/exec"
	"strconv"
	"strings"
	"sync"
	"time"
)

var pingTimeout = 10

var pingRetry = 2

var concurrentPing = 500

var result = make(map[string]interface{})

var upIps []string

var downIps []string

func pingIP(ipAddress string, wg *sync.WaitGroup) {

	defer wg.Done()

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)

	defer cancel()

	cmd := exec.CommandContext(ctx, "ping", "-n", strconv.Itoa(pingRetry), "-w", strconv.Itoa(pingTimeout), ipAddress)

	combinedOutput, _ := cmd.CombinedOutput()

	if len(combinedOutput) > 0 {

		if !strings.Contains(string(combinedOutput), "100% loss") {

			upIps = append(upIps, ipAddress)

		} else {

			downIps = append(downIps, ipAddress)

		}
	}
}

func main() {

	args := os.Args[1:]

	var wg sync.WaitGroup

	if len(args) > 0 && strings.Contains(args[0], ".txt") {

		path := args[0]

		file, err := os.OpenFile(path, os.O_RDWR, 0644)

		if err == nil {

			content, err := os.ReadFile(path)

			if err == nil {

				defer file.Close()

				content := strings.ReplaceAll(string(content), "\n", "")

				var contentMap map[string]string

				err := json.Unmarshal([]byte(content), &contentMap)

				if err != nil {

					return
				}

				if value, ok := contentMap["max-ping-check-retry-count"]; ok {

					pingRetry, _ = strconv.Atoi(value)
				}

				if value, ok := contentMap["max-ping-check-timeout"]; ok {

					pingTimeout, _ = strconv.Atoi(value)
				}

				if value, ok := contentMap["max-concurrent-ping"]; ok {

					concurrentPing, _ = strconv.Atoi(value)
				}

				ipString, ok := contentMap["ip-addresses"]

				if ok {

					ips := strings.Split(ipString, ",")

					ipQueue := make(chan string, len(ips))

					for _, ip := range ips {

						ipQueue <- ip
					}

					close(ipQueue)

					for i := 0; i < concurrentPing; i++ {

						go func() {

							for ip := range ipQueue {

								wg.Add(1)

								pingIP(ip, &wg)
							}
						}()
					}

					time.Sleep(2 * time.Second)

					wg.Wait()

					result["up"] = upIps

					result["down"] = downIps

					jsonData, err := json.MarshalIndent(result, "", "  ")

					if err == nil {

						fmt.Println(string(jsonData))

						return
					}
				}
			}
		}
	}
}
