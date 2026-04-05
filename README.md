# Serene — dealer management

Full-stack dealer management for the **Serene** brand: **Spring Boot 3** (Java 17) + **React** (TypeScript, Vite, Ant Design), **MySQL** with HikariCP, **JWT** authentication, role-based access (**ADMIN**, **DEALER**, **CUSTOMER**), and separate **Admin** and **Dealer** portals.

## Prerequisites

- **JDK 17+** (project targets Java 17 bytecode)
- **Maven 3.9+**
- **Node.js 18+** and npm
- **MySQL 8** listening on **port 3310**, user `root`, password `1234` (as configured)

## Database

Create the database (or let the JDBC URL create it):

```sql
CREATE DATABASE IF NOT EXISTS sems_db;
```

Connection is configured in [`backend/src/main/resources/application.yml`](backend/src/main/resources/application.yml):

- URL: `jdbc:mysql://localhost:3310/sems_db?createDatabaseIfNotExist=true&...`
- Username: `root` / Password: `1234`

## Backend

```bash
cd backend
mvn spring-boot:run
```

- API base: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Default admin (seeded on first run)

| Field    | Value     |
|----------|-----------|
| Username | `admin`   |
| Password | `admin123`|

Dealers can self-register via `POST /api/auth/register` or the UI **Create dealer account** link.

### Demo catalog & bulk data (first backend startup)

On startup, the app:

- **Renames** legacy demo SKUs if they still use the old names (e.g. “Serene Air Purifier Pro” → **Serene Signature Bundle**) so existing rows stay valid for orders.
- If the product table is **empty**, inserts three neutral **Serene** SKUs (Signature / Everyday / Refill).
- If user **`bharat3_dlr_01`** does not exist yet: any prior **`bharat3_dlr_*`**, **`bharat2_dlr_*`**, **`bharat_dlr_*`**, or **`serene_dealer_*`** demo accounts (and their orders, customers, and dealer rows) are **removed**, then **25** Indian demo dealers (one per major city) and **1 000** customers (**40** per dealer; fixed full-name list from bundled resources) are inserted.

| Demo dealers | Login | Password   |
|--------------|-------|------------|
| `bharat3_dlr_01` … `bharat3_dlr_25` | (see left) | `Dealer123!` |

The seeder skips when `bharat3_dlr_01` already exists. To **re-run** the Indian bulk seed, delete the `bharat3_dlr_*` users (or the whole DB) and restart; older demo prefixes and `@v2.bharatsems.demo` dealer emails are purged automatically whenever the v3 marker user is absent.

## Frontend

```bash
cd frontend
npm install
npm run dev
```

App runs at **http://localhost:3000** (see [`frontend/vite.config.ts`](frontend/vite.config.ts)).

The UI expects the backend at `http://localhost:8080` (see [`frontend/src/api/axiosInstance.ts`](frontend/src/api/axiosInstance.ts)).

## Features (summary)

- **Security:** JWT (Bearer), BCrypt passwords, stateless sessions, account lockout after 5 failed logins (5 minutes), optional fixed `accountExpiry`, sign-in blocked if last successful login was more than **365 days** ago (admin **Unlock** refreshes last login), generic login errors.
- **REST:** Paginated list endpoints (`page`, `size`, `sort`), OpenAPI 3 / Swagger with JWT scheme.
- **Audit:** JPA auditing (`createdBy`, `createdAt`, `updatedBy`, `updatedAt`) on entities.
- **UI:** Responsive Ant Design layout, admin and dealer dashboards, CRUD for dealers, customers, products, orders, and users (admin).

## Project layout

```
SeMS/
├── backend/          # Spring Boot application
├── frontend/         # Vite + React + TypeScript
└── README.md
```

## API quick reference

| Area        | Base path |
|------------|-----------|
| Auth       | `/api/auth` |
| Admin      | `/api/admin/...` |
| Dealer     | `/api/dealer/...` |
| Products (read) | `/api/products` (authenticated) |
| Products (write)| `/api/admin/products` |

## License

Proprietary / internal use for Serene.
