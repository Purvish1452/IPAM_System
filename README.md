# IPAM Migration

A modernized **IP Address Management (IPAM)** application migrated to a **Vert.x-based backend architecture** with a web-based frontend.

The project provides IP address management, subnet management, DHCP management, user management, event logging, alerts, reports, and rogue device detection.

---

## 🚀 Project Overview

The IPAM Migration project focuses on migrating and modernizing the existing IPAM application into a modular and scalable architecture.

### Main Technologies

* **Java**
* **Vert.x**
* **Vert.x Web**
* **PostgreSQL**
* **Go**
* **JavaScript**
* **HTML/CSS**
* **Maven**
* **Git**

---

## 📁 Project Structure

```text
IPAM_Real/
│
├── vertx-app/
│   ├── pom.xml
│   │
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/
│           │       └── motadata/
│           │           └── ipam/
│           │               ├── dao/
│           │               ├── model/
│           │               ├── router/
│           │               └── service/
│           │
│           └── resources/
│               └── webroot/
│
├── go-services/
│   └── go.mod
│
├── database/
│   └── migrations/
│
├── deployment/
│
├── src/
│   └── main/
│       └── webapp/
│           └── js/
│               └── motadata/
│
├── file-uploads/
│
├── .gitignore
└── README.md
```

---

## 🏗️ Architecture

```text
                    ┌──────────────────────┐
                    │      Web Browser     │
                    │  HTML / CSS / JS     │
                    └──────────┬───────────┘
                               │
                               │ HTTP
                               ▼
                    ┌──────────────────────┐
                    │     Vert.x Web       │
                    │      Routers         │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │      Service Layer   │
                    │ Business Logic       │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │       DAO Layer      │
                    │ Database Operations  │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │      PostgreSQL      │
                    │       Database       │
                    └──────────────────────┘
```

---

## 🔧 Backend

The backend is implemented using **Vert.x** and follows a layered architecture.

### DAO Layer

Responsible for database operations.

```text
dao/
├── DhcpDao.java
├── EventDao.java
├── SubnetDao.java
└── UserDao.java
```

### Model Layer

Contains application data models.

```text
model/
├── Event.java
└── SubnetDetails.java
```

### Router Layer

Handles HTTP requests and API endpoints.

```text
router/
├── AlertRouter.java
├── AuthRouter.java
├── DhcpRouter.java
├── EventRouter.java
├── ReportRouter.java
├── SettingsRouter.java
└── SubnetRouter.java
```

### Service Layer

Contains business logic.

```text
service/
├── EventService.java
├── SubnetService.java
└── UserService.java
```

---

## 🌐 Frontend

The frontend provides the web interface for IPAM functionality.

Current modules include:

* Dashboard
* IP Request
* DHCP Management
* User Management
* Event Logs
* Reports
* Rogue Detection
* Global Settings
* Alert Management
* Mail Server Configuration

---

## ✅ Implemented Features

### User Management

* User management migration
* User-related operations
* Authentication-related functionality

### DHCP Management

* DHCP management migration
* DHCP-related operations

### Alerts

* Alert configuration
* Alert management

### Dashboard

* Dashboard functionality
* IPAM information display

### IP Request

* IP request functionality

### Mail Server

* Mail server configuration

---

## 🚧 Work in Progress

The following modules are currently being developed or migrated:

* Event Logs
* Reports
* Rogue Detection

---

## ⚙️ Requirements

Make sure the following software is installed:

```text
Java 17+
Maven 3.8+
PostgreSQL
Go
Git
```

Verify installations:

```bash
java -version
mvn -version
psql --version
go version
git --version
```

---

## ▶️ Running the Project

### 1. Clone the Repository

```bash
git clone <repository-url>
cd IPAM_Real
```

### 2. Configure Database

Create the required PostgreSQL database and configure the database connection according to the project configuration.

Example:

```text
Host: localhost
Port: 5432
Database: ipam
Username: postgres
```

---

### 3. Build the Vert.x Application

```bash
cd vertx-app
mvn clean package
```

---

### 4. Run the Application

```bash
mvn exec:java
```

Or use the project's configured Maven execution command.

The application will be available at:

```text
http://localhost:8080
```

---

## 🧪 Development

During development, use:

```bash
mvn clean package
```

To verify the project:

```bash
mvn test
```

Check Git status:

```bash
git status
```

---

## 🔀 Git Workflow

Create a feature branch:

```bash
git checkout -b feature/<feature-name>
```

Stage changes:

```bash
git add <file-or-folder>
```

Commit changes:

```bash
git commit -m "feat: <description>"
```

Push the branch:

```bash
git push origin feature/<feature-name>
```

---

## 📝 Commit Convention

Use meaningful commit messages.

Examples:

```text
feat: update frontend functionality
feat: update DHCP management
feat: update user management
feat: update event log functionality
feat: update rogue detection
feat: update IPAM DAO operations
feat: update API routers
feat: update service layer
fix: resolve event log API issue
fix: resolve DHCP management issue
refactor: improve subnet service
chore: update project configuration
```

---

## 🚫 Ignored Files

Generated build files should not be committed to Git.

For example:

```text
target/
*.class
*.jar
```

The Maven `target/` directory contains generated build artifacts and should normally remain outside source control.

---

## 📌 Current Status

| Module                    | Status         |
| ------------------------- | -------------- |
| Project Setup             | ✅ Completed    |
| Vert.x Backend            | ✅ In Progress  |
| Dashboard                 | ✅ Implemented  |
| IP Request                | ✅ Implemented  |
| User Management           | ✅ Migrated     |
| DHCP Management           | ✅ Migrated     |
| Alert Configuration       | ✅ Implemented  |
| Mail Server Configuration | ✅ Implemented  |
| Event Logs                | 🚧 In Progress |
| Reports                   | 🚧 In Progress |
| Rogue Detection           | 🚧 In Progress |

---

## 👨‍💻 Developer

**Purvish**

IPAM Migration Project

---

## 📄 License

This project is intended for internal development and project migration purposes.
