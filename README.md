# Motadata IPAM (IP Address Management)

**Motadata IPAM** is an enterprise-grade, high-performance web-based IP Address Management system designed to discover, track, allocate, monitor, and audit IPv4 subnets, IP addresses, DHCP servers, and rogue network devices.

The system is built on **Eclipse Vert.x 5** using a fully asynchronous, reactive **Multi-Reactor & Worker Verticle Architecture** backed by **PostgreSQL** (`vertx-pg-client`), with high-speed **Go microservices** for network subnet discovery and DHCP collection.

---

## Table of Contents
- [Key Features](#key-features)
- [Architecture & Concurrency Model](#architecture--concurrency-model)
- [Threading & Worker Pool Design](#threading--worker-pool-design)
- [Technology Stack](#technology-stack)
- [Project Directory Structure](#project-directory-structure)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Database Setup & Migrations](#database-setup--migrations)
- [Build & Run Instructions](#build--run-instructions)
- [REST API Reference](#rest-api-reference)
- [Go Microservices](#go-microservices)
- [Testing & Verification](#testing--verification)

---

## Key Features

### 1. Subnet & Supernet Management
- **Hierarchy & Allocation**: Organize subnets by Supernet, Gateway, and Category.
- **Real-Time Utilization**: Live tracking of `TOTAL`, `USED`, `AVAILABLE`, `RESERVED`, and `TRANSIENT` IP counts with visual utilization gauges.
- **Bulk Operations**: Add IP ranges, batch-edit statuses, reserve IP blocks, and delete ranges reactively.
- **CSV Import / Export**: Import subnet allocations from CSV files; export complete IP tables to CSV and PDF.

### 2. IP Request & Approval Workflow
- **Self-Service Portal**: Internal teams can submit static IP allocation requests specifying Device Type (Server, VM, Container, Router, Switch, Firewall, AP, IoT), Allocation Duration (Permanent, 30/60/90 Days, 6 Months, 1 Year, Temporary), Quantity, and Business Justification.
- **Admin Review Queue**: Network administrators can review, modify subnet assignments, select specific available IPs from interactive grids, enter approval/rejection remarks, and trigger one-click IP reservation.
- **Automated Lifecycle**: Approved requests automatically transition target IPs to `USED`, update subnet utilization metrics, and write immutable audit records to the event log.

### 3. Network Discovery & Live Probing
- **High-Speed ICMP Scans**: Concurrent subnet ping sweeps with real-time status updates.
- **TCP Port Probing**: Multi-port scanning (e.g. ports 21, 22, 23, 25, 53, 80, 443, 3306, 3389, 5432, 8080).
- **DNS & Reverse DNS**: Automatic hostname discovery and reverse DNS resolution.
- **Traceroute**: Network path inspection and hop-by-hop latency measurement.
- **Go Discovery Engine**: Standalone high-concurrency Go microservice for massive CIDR sweeps.

### 4. Rogue Device & Threat Detection
- **Unauthorized IP Identification**: Flags unknown MAC addresses and unauthorized devices on active subnets.
- **Trusted MAC Whitelist**: Bulk import and management of authorized device MAC addresses.
- **Instant Categorization**: Distinguish between `TRUSTED` and `UNAUTHORIZED / ROGUE` assets.

### 5. DHCP Server Management
- **Multi-Vendor Support**: Windows DHCP Server and Cisco DHCP monitoring.
- **Scope Utilization**: Real-time lease tracking, active reservations, and free address pool metrics.
- **Automated Sync**: Background DHCP polling via dedicated Go collection worker.

### 6. Alerts & Event Audit Trail
- **Live Alert Stream**: Subnet capacity threshold alerts (e.g., >80% used), rogue device alerts, and IP conflict notifications.
- **Event Timeline**: Audit log recording every user action, IP request status change, subnet scan, and system event.
- **Automated Maintenance**: Background cleanup timers to prune resolved alerts and archive logs.

### 7. Document Reporting & Scheduling
- **Custom PDF Reports**: Generated using DynamicJasper and OpenPDF layout engines.
- **Spreadsheet Exports**: CSV and Excel (.xlsx) export streams for Subnets, Alerts, Events, and DHCP data.
- **Report Schedulers**: Cron-based automated report delivery with recipient email lists.

### 8. Security & RBAC
- **JWT Authentication**: Token-based authentication with HS256 signatures, 30-day lifespans, and cookie fallbacks.
- **Granular Permissions**: Role-Based & Policy-Based Access Control (`ROLE_ADMIN`, `PERM_SUBNET_READ`, `PERM_SUBNET_WRITE`, `PERM_ALERTS_READ`, etc.).
- **Password Hashing**: Industry-standard BCrypt password encryption.

---

## Deep-Dive: Vert.x Reactive Architecture, Verticles & Thread Pools

The application is architected around **Eclipse Vert.x 5** and **Netty**, adopting a **Decoupled Multi-Verticle & Multi-Reactor Pattern**.

```mermaid
flowchart TD
    subgraph Clients["1. External Layer"]
        C["Web Browsers & REST Clients"]
    end

    subgraph EventLoopLayer["2. Event Loop Layer: HttpServerVerticle"]
        EL["Netty Event Loop Threads (2 x Cores)"]
        Router["HTTP Router & JWT Auth Middleware"]
        Services["Reactive Services (SubnetService, UserService)"]
        PgDriver["PgPool Reactive Driver (20 Sockets)"]
    end

    subgraph EventBusLayer["3. Messaging Backbone"]
        EB["Vert.x EventBus (Non-Blocking Message Queue)"]
    end

    subgraph NetworkWorker["4. Worker Verticle: NetworkWorkerVerticle"]
        NetPool["Network Worker Pool (30 Threads)"]
        NetTasks["ICMP Ping Sweeps<br/>TCP Port Probing<br/>DNS Lookups & CSV Parsing"]
    end

    subgraph ReportWorker["5. Worker Verticle: ReportWorkerVerticle"]
        RepPool["Report Worker Pool (5 Threads)"]
        RepTasks["DynamicJasper Compilation<br/>OpenPDF Layout Export<br/>Excel Data Streams"]
    end

    subgraph DatabaseLayer["6. Persistent Storage"]
        DB[("PostgreSQL Database")]
    end

    C -->|"HTTP Requests"| EL
    EL --> Router
    Router --> Services
    Services --> PgDriver
    PgDriver <-->|"Non-Blocking SQL"| DB

    Router -->|"Asynchronous Request"| EB
    EB -->|"Dispatch Network Tasks"| NetPool
    NetPool --> NetTasks
    NetTasks -.->|"Async Result Reply"| Router

    EB -->|"Dispatch Report Tasks"| RepPool
    RepPool --> RepTasks
    RepTasks -.->|"Async Buffer Reply"| Router
```

---

### 1. The Verticles in the System

| Verticle | Threading Model | Purpose & Responsibilities |
| :--- | :--- | :--- |
| **`MainVerticle`** | Standard (Deployer) | Primary startup orchestrator. Loads `AppConfig`, initializes `PgClientProvider` and database schema via `DatabaseInit`, starts `JobScheduler`, and deploys the sub-verticles with specialized deployment options. |
| **`HttpServerVerticle`** | Standard (Event Loop) | Runs strictly on Netty Event Loop threads. Mounts HTTP Server on port `8080`, manages JWT/Session filters, serves static web assets from `webroot`, and handles all fast REST API CRUD endpoints via non-blocking `PgPool`. |
| **`NetworkWorkerVerticle`** | `ThreadingModel.WORKER` | Deployed with `setWorkerPoolName("ipam-network-worker-pool")` and size `30`. Consumes EventBus messages for ICMP ping sweeps, TCP port probing, reverse DNS lookups, traceroute probes, and CSV import parsing. |
| **`ReportWorkerVerticle`** | `ThreadingModel.WORKER` | Deployed with `setWorkerPoolName("ipam-report-worker-pool")` and size `5`. Consumes EventBus messages to compile DynamicJasper reports, OpenPDF documents, and Excel spreadsheets in an isolated, memory-safe thread pool. |

---

### 2. How the Event Loop Works

- **The Multi-Reactor Pattern**: Vert.x assigns each incoming TCP socket connection to one of `2 * CPU Cores` Event Loop threads (e.g. `vert.x-eventloop-thread-0` to `15`).
- **The Golden Rule**: *"Never block the Event Loop thread."*
- **Non-Blocking Execution**: When a client requests subnet records (`GET /subnet/`):
  1. The Event Loop thread executes `SubnetRouter` and calls `SubnetService.getAllSubnets()`.
  2. `SubnetService` issues a query via `PgPool.query().execute()`.
  3. **The Event Loop thread NEVER waits or sleeps for PostgreSQL**. It registers an asynchronous callback (`Future`) and immediately moves on to process subsequent concurrent requests from other clients.
  4. When PostgreSQL returns the query results over the network socket, Netty notifies the Event Loop, which formats the JSON response and sends it back to the client.

---

### 3. How Worker Pools & Bulkheading Work

Certain tasks cannot be performed reactively (e.g., waiting for OS ICMP echo replies, socket connect timeouts, or rasterizing 50-page PDF documents).

Instead of running these on the Event Loop or competing for a single generic thread pool, the architecture implements the **Bulkhead Pattern** using isolated worker pools:

```
┌───────────────────────────────────────────────────────────────────────────────┐
│                              Worker Isolation                                 │
├───────────────────────────────────────┬───────────────────────────────────────┤
│    ipam-network-worker-pool (30)      │      ipam-report-worker-pool (5)      │
├───────────────────────────────────────┼───────────────────────────────────────┤
│ • ICMP Ping Sweeps (isReachable)      │ • DynamicJasper Compilation           │
│ • TCP Port Probing (Socket.connect)   │ • OpenPDF Layout Rendering            │
│ • Reverse DNS Hostname Resolution     │ • Large Excel (.xlsx) Exports         │
│ • CSV Bulk File Parsing               │                                       │
│ ➔ Scaled for high network concurrency │ ➔ Capped at 5 to protect JVM Heap     │
└───────────────────────────────────────┴───────────────────────────────────────┘
```

#### Why This Prevents System Outages:
1. **Zero Worker Starvation**: If multiple users generate large PDF reports, they only occupy threads in `ipam-report-worker-pool`. Network discovery scans and ping sweeps running in `ipam-network-worker-pool` continue running at full speed.
2. **Zero Event Loop Latency Spikes**: Because all blocking code is isolated on worker threads, the HTTP web server remains 100% responsive with sub-millisecond response times for REST APIs.
3. **Memory Protection (OOM Guard)**: PDF generation requires substantial heap memory. Restricting the report worker pool to 5 threads guarantees that simultaneous export requests cannot exhaust JVM memory.

---

### 4. EventBus Messaging Protocol

Communication between the Event Loop Verticle (`HttpServerVerticle`) and the Worker Verticles occurs exclusively via the **Vert.x EventBus**:

| EventBus Address | Message Payload (Input) | Reply Payload (Output) | Consumer Verticle |
| :--- | :--- | :--- | :--- |
| `ipam.worker.network.ping` | `{"ip": "192.168.1.10", "timeout": 1000}` | `{"success": true, "ip": "...", "reachable": true, "status": "ONLINE"}` | `NetworkWorkerVerticle` |
| `ipam.worker.network.scan` | `{"subnetId": 1, "subnetAddress": "192.168.1.0", "cidr": 24}` | `{"success": true, "totalIps": 254, "activeIps": 42, "message": "..."}` | `NetworkWorkerVerticle` |
| `ipam.worker.network.dns` | `{"ip": "192.168.1.10"}` | `{"success": true, "ip": "...", "hostname": "gw.local", "resolved": true}` | `NetworkWorkerVerticle` |
| `ipam.worker.network.portscan`| `{"ip": "192.168.1.10", "ports": [80, 443, 22]}` | `{"success": true, "ip": "...", "openPorts": [80, 443]}` | `NetworkWorkerVerticle` |
| `ipam.worker.network.importCsv`| `{"csvText": "...", "subnetId": 1}` | `{"success": true, "imported": 250, "message": "..."}` | `NetworkWorkerVerticle` |
| `ipam.worker.report.subnet.pdf`| `{"data": [...], "subLabel": "192.168.1.0"}` | `{"success": true, "filename": "SubnetIP_Export_1.pdf", "size": 18240}` | `ReportWorkerVerticle` |
| `ipam.worker.report.vendor.pdf`| `{"data": [...], "subLabel": "All"}` | `{"success": true, "filename": "Vendor_Summary_1.pdf", "size": 12400}` | `ReportWorkerVerticle` |
| `ipam.worker.report.dynamic.pdf`| `{"title": "...", "data": [...], "columns": [...]}` | `Buffer` (Raw PDF binary stream) | `ReportWorkerVerticle` |

---

### 5. Thread Pools & Resource Summary

| Pool Name | Managed By | Default Size | Purpose |
| :--- | :--- | :--- | :--- |
| `vert.x-eventloop-thread-*` | Netty / Vert.x Core | `2 * CPU Cores` | Non-blocking HTTP sockets, routing, JWT parsing, and reactive SQL. |
| `ipam-network-worker-pool-*`| Dedicated Worker Pool | `30` Threads | ICMP ping sweeps, port scans, DNS lookups, CSV parsing. |
| `ipam-report-worker-pool-*` | Dedicated Worker Pool | `5` Threads | DynamicJasper and OpenPDF report compilation. |
| `vert.x-internal-blocking-*`| Vert.x Core | `20` Threads | Internal Vert.x file operations (`StaticHandler`). |
| `PgPool` Connection Pool | `vertx-pg-client` | `20` Sockets | Asynchronous PostgreSQL database wire connections. |

---



## Technology Stack

| Domain | Technologies |
| :--- | :--- |
| **Backend Core** | Java 21, Eclipse Vert.x 5.0.0 (`vertx-core`, `vertx-web`, `vertx-auth-jwt`, `vertx-sql-client`, `vertx-pg-client`) |
| **Database** | PostgreSQL 12+, Flyway migrations, Vert.x Reactive PgPool |
| **Security** | JWT (HS256), BCrypt, PBAC/RBAC permission interceptors |
| **Document Generation** | DynamicJasper 5.0.9, JasperReports 6.3.0, OpenPDF 1.3.30 |
| **Frontend** | HTML5, CSS3, JavaScript (ES6+), jQuery, Kendo UI, Bootstrap |
| **Microservices** | Go 1.20 (HTTP REST Discovery & DHCP services), Go 1.18 (CLI Ping Engine) |
| **Build & Test** | Maven 3.8+, JUnit 5, Vert.x JUnit 5 Extension, Mockito, AssertJ |

---

## Project Directory Structure

```text
IPAM_Real/
├── config/
│   └── ipm-conf.yml                     # Central application configuration
├── database/
│   └── migrations/                      # Versioned SQL schema migration scripts
├── go-engine/
│   ├── go.mod
│   └── ping.go                          # Standalone high-speed CLI ping utility
├── go-services/
│   ├── common/                          # Shared Go network and CIDR utilities
│   ├── discovery/                       # Subnet Auto-Discovery Microservice (:8081)
│   ├── dhcp/                            # DHCP Collector Microservice (:8082)
│   └── go.mod
├── vertx-app/
│   ├── pom.xml                          # Maven build configuration
│   ├── src/main/java/com/motadata/ipam/
│   │   ├── MainVerticle.java            # Startup deployer & verticle orchestrator
│   │   ├── config/                      # YAML config parser (AppConfig)
│   │   ├── db/                          # PgPool provider & schema initializer
│   │   ├── model/                       # Domain models (SubnetDetails, IpRequest, etc.)
│   │   ├── router/                      # HTTP Routers (Auth, Subnet, Alert, Report, etc.)
│   │   ├── scheduler/                   # Vert.x background periodic timers & cron jobs
│   │   ├── security/                    # JWT Auth provider & PermissionHandler
│   │   ├── service/                     # Reactive business services
│   │   └── verticle/                    # Event Loop & Worker Verticles:
│   │       ├── HttpServerVerticle.java  # Non-blocking web/REST verticle
│   │       ├── NetworkWorkerVerticle.java # Network probing & scanning worker
│   │       └── ReportWorkerVerticle.java  # PDF & Jasper report worker
│   ├── src/main/resources/
│   │   ├── db/init_ipam_postgres.sql    # PostgreSQL schema & seed dataset
│   │   ├── log4j2.xml                   # Logging configuration
│   │   └── webroot/                     # Frontend UI, JS controllers & CSS
│   └── src/test/                        # Unit and integration test suite
├── pom.xml                              # Root Maven project POM
└── README.md                            # Comprehensive project documentation
```

---

## Prerequisites

Ensure the following tools are installed on your system:

- **JDK 21** or later (`openjdk-21-jdk`)
- **Maven 3.8.0** or later
- **PostgreSQL 12+**
- **Go 1.20+** (for Go microservices)

Verify tool installations:
```bash
java -version
mvn -version
psql --version
go version
```

---

## Configuration

The application reads configuration from `config/ipm-conf.yml`:

```yaml
server-port: 8080
server-host: localhost
min-memory: 1024
max-memory: 2048

# PostgreSQL Database Configuration
db-host: localhost
db-port: 5432
db-name: ipam_db
db-user: postgres
db-password: password

# Network Discovery & Scan Tuning
max-ping-check-timeout: 10
max-ping-check-retry-count: 2
max-concurrent-ping: 500
process-request-timeout: 1200
```

---

## Database Setup & Migrations

1. **Create the PostgreSQL Database**:
   ```bash
   createdb -h localhost -p 5432 -U postgres ipam_db
   ```

2. **Automatic Initialization**:
   Upon startup, [`DatabaseInit.java`](file:///home/purvish/Documents/IPAM_Real/vertx-app/src/main/java/com/motadata/ipam/db/DatabaseInit.java) automatically executes [`init_ipam_postgres.sql`](file:///home/purvish/Documents/IPAM_Real/vertx-app/src/main/resources/db/init_ipam_postgres.sql) and applies incremental migrations (such as adding `device_type`, `duration`, and `preferred_subnet` columns to `ip_requests`).

---

## Build & Run Instructions

### 1. Build and Run the Vert.x Application

From the root repository directory:

```bash
# Clean, compile, and package the executable fat JAR
mvn clean package -DskipTests

# Run the fat JAR
java -jar vertx-app/target/vertx-ipam-4.0.0-fat.jar
```

Or run directly with Maven:
```bash
cd vertx-app
mvn exec:java
```

The web application will be accessible at:
```text
http://localhost:8080
```

**Default Credentials**:
- **Username**: `admin`
- **Password**: `admin`

---

### 2. Run the Go Discovery Microservice (Optional)

```bash
cd go-services
go run ./discovery
```
*Listens on `http://localhost:8081`.*

### 3. Run the Go DHCP Collector Microservice (Optional)

```bash
cd go-services
go run ./dhcp
```
*Listens on `http://localhost:8082`.*

---

## REST API Reference

### Authentication & Authorization
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/loginUser.html` | Authenticates user credentials, sets session and JWT cookies | Public |
| `GET` | `/logoutUser.html` | Invalidates session and clears tokens | Public |
| `GET` | `/validatePermission/` | Returns current user role, authorities, and permissions | Token Required |
| `GET` | `/globalSearch/` | Searches subnets, IPs, and events across the system | Token Required |

### Subnets & IP Management
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/subnet/` | Returns all subnets with CIDR, IP counts, and gateway info |
| `POST` | `/subnet/` | Creates a new subnet definition |
| `PUT` | `/subnet/` | Updates subnet details |
| `DELETE` | `/subnet/:id` | Deletes a subnet and cleans associated IP records |
| `GET` | `/subnetIp/` | Lists IP addresses for a subnet with pagination & filtering |
| `POST` | `/subnetIp/scan` | Dispatches asynchronous subnet ICMP scan to `NetworkWorkerVerticle` |
| `POST` | `/subnetIp/addRange` | Batch inserts a range of IP addresses |
| `POST` | `/subnetIp/updateRange` | Batch updates status (`USED`, `AVAILABLE`, `RESERVED`, `TRANSIENT`) |
| `POST` | `/subnetIp/deleteRange` | Batch deletes a range of IP addresses |
| `POST` | `/subnetIp/importCsv` | Imports IP definitions from uploaded CSV |
| `GET` | `/subnetIp/exportCsv` | Exports subnet IP records to CSV |
| `GET` | `/subnetIp/exportPdf` | Exports subnet IP records to PDF |

### IP Request & Approval Workflow
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/ipRequests/` | Returns all IP requests with status, device type, and duration |
| `POST` | `/ipRequests/` | Submits a new IP request from self-service portal |
| `POST` | `/ipRequests/approved` | Approves request, allocates selected IPs as `USED`, updates stats |
| `POST` | `/ipRequests/rejected` | Rejects request with admin remark |

### Alerts, Events & Reports
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/alerts/` | Returns active alert stream records |
| `GET` | `/event/` | Returns event audit logs and timeline records |
| `GET` | `/reports/schedulers` | Lists configured report schedules |
| `POST` | `/reports/schedulers` | Creates/updates report schedule cron definition |
| `GET` | `/exportsubnetIpByReportTimeline/` | Generates on-demand timeline report (PDF / CSV) |

---

## Go Microservices

### Discovery Microservice (`:8081`)
- **`GET /health`**: Healthcheck endpoint.
- **`POST /api/v1/scan/subnet`**: Scans a CIDR block with configurable concurrency and timeout.
  ```json
  {
    "subnetCidr": "192.168.1.0/24",
    "timeoutMs": 1000,
    "concurrency": 250
  }
  ```

### DHCP Collector Microservice (`:8082`)
- **`GET /health`**: Healthcheck endpoint.
- **`POST /api/v1/dhcp/scan`**: Collects DHCP lease tables and scope utilization.
  ```json
  {
    "hostAddress": "192.168.1.10",
    "type": "windows",
    "userName": "admin",
    "password": "secretPassword",
    "port": 5985
  }
  ```

---

## Testing & Verification

Run the comprehensive unit and integration test suite:

```bash
# Run all unit and integration tests
mvn test

# Run specific test classes
mvn test -Dtest=ModelTest,AppConfigTest,SchedulerTest,SecurityTest,MainVerticleTest
```

Test coverage includes:
- **`MainVerticleTest`**: Verticle deployment, HTTP routing pipeline, login redirects, and permission handlers.
- **`SecurityTest`**: JWT token generation, claims extraction, and BCrypt verification.
- **`SchedulerTest`**: Vert.x timer lifecycle, job scheduling, and cron parsing.
- **`ModelTest`**: JSON serialization and model binding for IPAM domain entities.
- **`AppConfigTest`**: YAML configuration loading and default fallbacks.

---

## License

This project is developed for enterprise IPAM infrastructure. All rights reserved.
