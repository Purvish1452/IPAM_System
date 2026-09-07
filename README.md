# Motadata IPAM

Motadata IPAM is a web-based IP Address Management system for managing subnets, IP addresses, DHCP data, discovery results, users, permissions, alerts, events, reports, and related network settings.

The current implementation is a Vert.x 5 application backed by PostgreSQL, with optional Go services for subnet discovery and DHCP collection.

## Highlights

- Subnet, supernet, gateway, category, and IP address management
- IP request workflows and IP utilization summaries
- DHCP credentials, utilization, and scan operations
- User and role management with feature-level permissions
- JWT authentication and PBAC/RBAC-style permission checks
- Rogue-device detection and trusted MAC address imports
- Event and alert management
- PDF/CSV reporting and scheduled report configuration
- Discovery and DHCP helper services written in Go
- Database initialization and compatibility migrations at startup
- Vert.x background timers for scan queues and alert cleanup

## Architecture

```text
Browser (HTML/CSS/JavaScript)
          |
          v
Vert.x Web Router
  - Body and session handlers
  - JWT authentication handler
  - Permission handlers
          |
          v
Vert.x service layer
          |
          v
Reactive PostgreSQL connection pool
          |
          v
PostgreSQL

Optional network helpers:
  Go Discovery service :8081
  Go DHCP service      :8082
  Go command-line ping engine
```

The Java application is assembled in `MainVerticle` using the following flow:

```text
HTTP handler -> service -> Vert.x PostgreSQL pool -> PostgreSQL
```

## Repository Layout

```text
IPAM_Real/
├── config/
│   └── ipm-conf.yml                 # Runtime server and database configuration
├── database/
│   └── migrations/                  # Versioned SQL migration scripts
├── go-engine/
│   ├── go.mod
│   └── ping.go                      # Command-line concurrent ping engine
├── go-services/
│   ├── common/                      # Shared Go helpers
│   ├── discovery/                   # Discovery HTTP service
│   ├── dhcp/                        # DHCP collector HTTP service
│   └── go.mod
├── vertx-app/
│   ├── pom.xml
│   ├── src/main/java/
│   │   └── com/motadata/ipam/
│   │       ├── config/               # YAML configuration loading
│   │       ├── db/                   # PostgreSQL pool and schema initialization
│   │       ├── model/                # Domain models
│   │       ├── router/               # HTTP/API route handlers
│   │       ├── scheduler/            # Background jobs and timers
│   │       ├── security/             # JWT, password, and permission handling
│   │       └── service/              # Application business logic
│   ├── src/main/resources/
│   │   ├── db/init_ipam_postgres.sql # Initial schema and seed data
│   │   ├── log4j2.xml
│   │   └── webroot/                  # Frontend assets and pages
│   └── src/test/                     # Java unit and integration tests
├── pom.xml                           # Maven parent project
└── README.md
```

Generated files under `target/` and runtime exports/uploads should not be treated as source files.

## Technology Stack

| Area | Technology |
|---|---|
| Backend | Java 21, Vert.x 5.0.0, Vert.x Web |
| Database | PostgreSQL, Vert.x reactive PostgreSQL client |
| Database setup | Flyway dependency plus startup schema initialization/migrations |
| Authentication | Vert.x JWT Auth with HS256 |
| Passwords | BCrypt |
| Frontend | HTML, CSS, JavaScript, jQuery, Kendo UI |
| Reports | DynamicJasper and OpenPDF |
| Network helpers | Go 1.20 (`go-services`), Go 1.18 (`go-engine`) |
| Build and tests | Maven, JUnit 5, Mockito |

## Requirements

Install:

- JDK 21
- Maven 3.8 or newer
- PostgreSQL
- Go (the service module targets Go 1.20; the ping engine targets Go 1.18)

Verify the tools:

```bash
java -version
mvn -version
psql --version
go version
```

## Configuration

The application reads `config/ipm-conf.yml`. When running from `vertx-app`, it also checks the parent configuration path. If the file cannot be loaded, built-in defaults are used.

Important settings:

```yaml
server-port: 8080
server-host: localhost
db-host: localhost
db-port: 5432
db-name: ipam_db
db-user: postgres
db-password: change-this-password
max-ping-check-timeout: 10
max-ping-check-retry-count: 2
max-concurrent-ping: 500
process-request-timeout: 1200
```

Create the PostgreSQL database before starting the application:

```bash
createdb -h localhost -U postgres ipam_db
```

On first startup, `vertx-app/src/main/resources/db/init_ipam_postgres.sql` is used to initialize the schema and seed data. Existing installations receive the compatibility changes implemented by `DatabaseInit`.

Do not use the repository's development password in a shared or production environment. Database credentials should be supplied through deployment-specific configuration, and the JWT signing key should be externalized before production deployment.

## Run the Vert.x Application

From the repository root:

```bash
mvn clean package
java -jar vertx-app/target/vertx-ipam-4.0.0-fat.jar
```

Alternatively, run the module during development:

```bash
cd vertx-app
mvn clean package
mvn exec:java
```

The web application is served at:

```text
http://localhost:8080
```

The login page is `/` or `/login.html`. Static assets and the home page are served from `vertx-app/src/main/resources/webroot`.

## Authentication and Permissions

`JwtAuthHandler` runs before the API routers. It accepts a token from the following locations, in order:

1. `accessToken` request header
2. `Authorization: Bearer <token>` header
3. `token` cookie

The login and static asset paths are public. Valid JWT claims are attached to the Vert.x routing context. Route-level permission checks use authorities such as:

```text
ROLE_ADMIN
PERM_ALERTS_READ
PERM_ALERTS_WRITE
PERM_SETTINGS_READ
PERM_SETTINGS_WRITE
```

Tokens currently use HS256 and are issued for 30 days. The application also keeps session and authority-cookie fallbacks for browser compatibility. Invalid or missing tokens are allowed through the authentication handler so that the existing UI can render; protected routes enforce access with `PermissionHandler` and return HTTP 403 when permission is absent.

## HTTP API Areas

The Vert.x routers expose endpoints for:

| Router | Main areas |
|---|---|
| `AuthRouter` | Login, logout, home page, global search, permission validation |
| `SubnetRouter` | Subnets, supernets, gateways, categories, IP details, scans, discovery, rogue detection, IP requests, imports, exports, summaries |
| `DhcpRouter` | DHCP credentials, credential checks, utilization, DHCP scans |
| `SettingsRouter` | Users, roles, global settings, branding, mail, alerts, custom columns, discovery, database maintenance |
| `EventRouter` | Events and event summaries |
| `AlertRouter` | Alert retrieval |
| `ReportRouter` | Report schedules, mail recipients, PDF/CSV report exports |

Representative endpoints include:

```text
POST /loginUser.html
GET  /subnet/
GET  /subnetIp/
GET  /dhcp/
GET  /event/
GET  /alerts/
GET  /rogueDetection/
GET  /ipRequests/
GET  /api/v1/reports/subnets/pdf
```

Most application endpoints use the existing legacy URL naming and trailing-slash conventions.

## Go Services

The Go services are standalone HTTP processes and read their port from the `PORT` environment variable.

### Discovery service

Default port: `8081`

```bash
cd go-services
go test ./...
go run ./discovery
```

Endpoints:

```text
GET  /health
POST /api/v1/scan/subnet
```

Example request:

```bash
curl -X POST http://localhost:8081/api/v1/scan/subnet \
  -H 'Content-Type: application/json' \
  -d '{"subnetCidr":"192.168.1.0/24","timeoutMs":1000,"concurrency":500}'
```

The response contains the expanded hosts, UP/DOWN status, optional reverse-DNS names, round-trip time, and scan duration. The implementation probes TCP ports 80 and 443.

### DHCP collector service

Default port: `8082`

```bash
cd go-services
go run ./dhcp
```

Endpoints:

```text
GET  /health
POST /api/v1/dhcp/scan
```

Example request:

```bash
curl -X POST http://localhost:8082/api/v1/dhcp/scan \
  -H 'Content-Type: application/json' \
  -d '{"hostAddress":"192.168.1.10","type":"windows","userName":"user","password":"password","port":5985}'
```

The service accepts `windows` or `cisco` as the server type and returns DHCP scope utilization data.

### Ping engine

`go-engine/ping.go` is a command-line utility. It expects a JSON configuration file path containing `ip-addresses` and optional ping settings such as `max-ping-check-timeout`, `max-ping-check-retry-count`, and `max-concurrent-ping`.

```bash
cd go-engine
go build -o ping-engine .
./ping-engine /path/to/ping-config.txt
```

It prints a JSON object containing `up` and `down` IP lists.

## Background Scheduling

`JobScheduler` starts with the Vert.x application and currently maintains:

- A 10-second subnet scan queue check timer
- An hourly alert cleanup timer
- Dynamic recurring jobs for subnet scans, DHCP scans, and report generation

Cron expressions in the supported `0 0/<minutes> * * * ?` form are converted to minute intervals. Other expressions currently fall back to a daily interval. Scheduled jobs can be triggered or deleted through the scheduler service integration.

## Testing and Development

Run the Java tests:

```bash
mvn test
```

Run the Go service tests:

```bash
cd go-services
go test ./...
```

Build all Java artifacts:

```bash
mvn clean package
```

The Maven package creates:

```text
vertx-app/target/vertx-ipam-4.0.0.jar
vertx-app/target/vertx-ipam-4.0.0-fat.jar
```

## Git Workflow

```bash
git checkout -b feature/<feature-name>
git add <file-or-folder>
git commit -m "feat: describe the change"
git push origin feature/<feature-name>
```

Use conventional prefixes such as `feat:`, `fix:`, `refactor:`, `test:`, and `chore:`.

## Current Status

The core Vert.x web application, PostgreSQL integration, authentication, permissions, user management, subnet/IP management, DHCP management, alerts, events, reports, discovery, rogue detection, and IP request surfaces are present in the current source tree. Some network collection and scheduled-job classes still contain placeholder or simulated execution logic and should be treated as integration points for production collectors.

## License

This project is intended for internal development and migration purposes. Licensing and deployment terms should be defined by the project owner.
