# StoreApp E-Commerce Challenge

Enterprise-style e-commerce sample with a Spring Boot backend, H2 local database, CSV product import, fake purchase flow, and React UI.

The provided example CSV was downloaded on June 25, 2026.

Backend build targets Spring Boot 3.3.x, Java 21, and Gradle 8.8 or newer.

## Approach

The solution is split into two runnable applications: a Spring Boot REST API and a React frontend. The backend owns product persistence, CSV import, validation, purchasing, audit events, idempotency, and inventory consistency. The frontend focuses on role-based workflows for product administration and purchasing, using demo login users to simulate admin and customer access.

The app starts with seeded product data from the provided CSV. Admin users can manage products and import CSV files. Purchase users can search products, add quantities to a cart, and complete a fake payment. The purchase flow validates stock on both the client and server, then records the purchase and decrements inventory in one transaction.

## Decisions

- Spring Boot exposes REST APIs for product CRUD, search, CSV import, and purchases.
- H2 is used as a local in-memory SQL database so the app is easy to run and reset.
- The backend imports `products.csv` on startup and also supports manual CSV upload from the UI.
- Manual CSV imports run as asynchronous jobs with persisted status, counts, timestamps, and summaries.
- Transient database failures during CSV row persistence are retried with short backoff.
- Product search supports server-side pagination.
- CSV rows are validated independently. Valid rows are upserted by SKU; invalid rows are returned in an import report.
- Purchases are fake payments: the backend validates stock, records the purchase, decrements inventory in one transaction, and supports idempotency keys.
- Inventory changes and purchase completions are persisted as audit events and emitted as structured key-value logs.
- Stock updates use pessimistic locking plus entity versioning to reduce overselling risk under concurrent purchases.
- React keeps the UI focused on the required workflows: product CRUD, search, import, and purchasing.
- The frontend is split by responsibility: API clients, reusable components, stateful hooks, and formatting/form utilities.
- Actuator health endpoints are enabled for container and operational visibility.
- Runtime configuration can be provided through environment variables such as `STOREAPP_DB_URL`, `STOREAPP_DB_USER`, `STOREAPP_DB_PASSWORD`, and `STOREAPP_SEED_CSV`.
- The frontend container adds basic security headers, including CSP, frame protection, content-type sniffing protection, referrer policy, and permissions policy.

## Alternatives Considered

- PostgreSQL would be better for production durability, but H2 keeps this challenge self-contained.
- A dedicated CSV library would reduce parser code, but the included parser avoids another dependency and handles quoted CSV fields.
- A full cart service was not necessary for the requested fake purchase workflow, so the frontend keeps cart state locally.
- Real authentication and authorization would be required in production, but demo users keep the challenge self-contained while still showing role-based UI behavior.

## Requirement Coverage

| Requirement | Implementation |
| --- | --- |
| Local DB | H2 in-memory database |
| Product CRUD | `/api/products` endpoints and Catalog UI |
| CSV import | Startup seed, synchronous import endpoint, and async import jobs |
| Search products | Text/category search with paginated endpoint |
| Purchase products | Fake payment flow with stock validation and audit |
| UI required | React Catalog and Purchase workflows |
| Docker runnable | `docker compose up --build` runs frontend and backend |
| README decisions/approach/alternatives | Documented in this README |
| Local run instructions | Backend, frontend, tests, and Docker commands included |

## AI Usage

AI-assisted development was used. Code comments were omitted per the challenge instructions.

## Run Locally

Backend:

```bash
gradle :backend:bootRun
```

If your machine defaults to Java 26, point Gradle to Java 23 or Java 21 first. On PowerShell:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-23'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
gradle.bat :backend:bootRun
```

Open/import `C:\Users\USER\Documents\dev\gila\storeapp` in your IDE, not `storeapp\backend`. The backend folder is a Gradle subproject, so importing only that folder can leave Spring/JPA classes unresolved.

If you need to generate a Gradle wrapper, run it from the repository root:

```powershell
cd C:\Users\USER\Documents\dev\gila\storeapp
gradle.bat wrapper --gradle-version 8.8
```

Do not run `gradle :backend:wrapper`; `wrapper` is a root project setup task.

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Frontend E2E smoke test:

```bash
cd frontend
npm install
npx playwright install
npm run test:e2e
```

Open `http://localhost:5173`.

Demo users:

| Username | Password | Role |
| --- | --- | --- |
| `admin` | `admin123` | Admin product management |
| `user1` | `user123` | Purchase flow |

The backend runs on `http://localhost:8080`. H2 console is available at `http://localhost:8080/h2-console` with JDBC URL `jdbc:h2:mem:storeapp`, user `sa`, and an empty password.

Run backend tests:

```bash
gradle :backend:test
```

## Run With Docker

Install and start Docker Desktop first. Then run from the repository root:

```bash
docker compose up --build
```

Open `http://localhost:5173`.

Verify the backend container is reachable:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/products
```

Stop the containers:

```bash
docker compose down
```

## API

- `GET /api/products?query=&category=` searches products.
- `GET /api/products/page?query=&category=&page=&size=` searches products with server-side pagination.
- `POST /api/products` creates a product.
- `PUT /api/products/{id}` updates a product.
- `DELETE /api/products/{id}` deletes a product.
- `POST /api/products/import` imports CSV using multipart field `file`.
- `POST /api/products/import-jobs` queues an asynchronous CSV import using multipart field `file`; optional field `idempotencyKey` deduplicates retries.
- `GET /api/products/import-jobs/{id}` returns async import status.
- `POST /api/purchases` creates a fake purchase; optional `idempotencyKey` deduplicates retries.
- `GET /actuator/health` returns application health.

## Test Coverage

- Unit coverage for retry behavior.
- Import integration coverage for valid rows, invalid-row isolation, and SKU upsert behavior.
- Service coverage for purchase stock decrement and insufficient-stock rollback.
- HTTP integration coverage for product CRUD/search, CSV import reports, and async import jobs.
- Playwright E2E coverage for the search and fake purchase UI flow.
