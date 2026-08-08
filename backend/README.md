# Supermarket & Grocery Inventory and Order Management REST API

![Java 17](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.2+-brightgreen.svg)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-blue.svg)
![Build](https://img.shields.io/badge/Tests-18%20Passed-success.svg)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)

Production-ready, scalable, and secure **Supermarket & Grocery Management REST API** built with Java 17, Spring Boot 3, Spring Security 6, JWT, Spring Data JPA, H2 / MySQL, OpenAPI 3 (Swagger), and JUnit 5 / Mockito.

---

## Key Features

- **JWT Authentication & Role-Based Access Control**:
  - Roles: `ADMIN`, `MANAGER`, `STAFF`
  - Stateless JWT Bearer authentication with claims and expiration
  - Pre-seeded test accounts out of the box

- **Product & Category Management**:
  - Full CRUD operations with Jakarta Validation
  - Prevents category deletion if linked products exist (`CategoryDeleteException`)
  - SKU validation & uniqueness checks (`SKU-XXXXX`)
  - Dynamic filtering (keyword search, category, min/max price, low-stock filter), pagination, and sorting

- **Stock & Inventory Audit Movements**:
  - Stock-In and Stock-Out operations with `@Transactional` guarantees
  - Enforces strict rule: stock **never** drops below zero (`InsufficientStockException`)
  - Real-time stock movement audit trail (`STOCK_IN`, `STOCK_OUT`, `ORDER_DEDUCTION`, `ORDER_RESTOCK`)
  - Real-time low-stock threshold queries

- **Order & Lifecycle State Machine**:
  - Workflow: `PENDING` → `CONFIRMED` → `SHIPPED` → `DELIVERED` | `CANCELLED`
  - **On Confirmation (`CONFIRMED`)**: Automatically checks inventory availability, deducts stock, and logs stock movement
  - **On Cancellation (`CANCELLED`)**: Automatically restores stock back to inventory if order was confirmed or shipped

- **Executive Dashboard & Analytics**:
  - Real-time total products count, out-of-stock count, low-stock count, total revenue, status breakdown, low-stock alerts, and recent activity log

- **Interactive API Documentation & Postman**:
  - Swagger UI integrated with JWT Bearer Token Security Scheme
  - Included ready-to-import Postman Collection (`Supermarket_Grocery_API.postman_collection.json`)

---

## Seed Accounts & Default Credentials

Password for all pre-seeded accounts: **`Password123!`**

| Email | Role | Permissions |
| :--- | :--- | :--- |
| `admin@supermarket.com` | `ROLE_ADMIN` | Full CRUD across Categories, Products, Stock, Orders & Dashboard |
| `manager@supermarket.com` | `ROLE_MANAGER` | Create/Update Categories & Products, Manage Stock, Orders & Dashboard |
| `staff@supermarket.com` | `ROLE_STAFF` | View Products/Categories, Stock-In/Out, Create Orders |

---

## Quick Start (Local Run with H2)

### Prerequisites
- **Java 17 JDK**

### Running the Application
Run the bundled Maven wrapper script:

```bash
# Windows
.\mvnw spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

The application will start on **`http://localhost:8080`**.

### Useful URLs
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **H2 Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: `jdbc:h2:mem:supermarketdb`
  - Username: `sa`
  - Password: `password`

---

## Running Unit & Integration Tests

Run the full automated test suite (18 unit/controller tests):

```bash
.\mvnw test
```

---

## Running with Docker & MySQL

To run the application with MySQL 8 using Docker Compose:

```bash
docker-compose up --build
```

---

## API Endpoints Summary

### Authentication (`/api/v1/auth`)
- `POST /api/v1/auth/register` - Register user account
- `POST /api/v1/auth/login` - Authenticate & obtain JWT Token
- `GET /api/v1/auth/me` - Get current user profile

### Categories (`/api/v1/categories`)
- `GET /api/v1/categories` - List all categories
- `GET /api/v1/categories/{id}` - Get category by ID
- `POST /api/v1/categories` - Create category (`ADMIN`, `MANAGER`)
- `PUT /api/v1/categories/{id}` - Update category (`ADMIN`, `MANAGER`)
- `DELETE /api/v1/categories/{id}` - Delete category (`ADMIN` only)

### Products (`/api/v1/products`)
- `GET /api/v1/products` - Search & filter products (paginated)
- `GET /api/v1/products/{id}` - Get product by ID
- `GET /api/v1/products/sku/{sku}` - Get product by SKU
- `POST /api/v1/products` - Create product (`ADMIN`, `MANAGER`)
- `PUT /api/v1/products/{id}` - Update product (`ADMIN`, `MANAGER`)
- `DELETE /api/v1/products/{id}` - Delete product (`ADMIN` only)

### Stock Operations (`/api/v1/stock`)
- `POST /api/v1/stock/stock-in` - Stock addition
- `POST /api/v1/stock/stock-out` - Stock deduction (prevents negative stock)
- `GET /api/v1/stock/low-stock` - Low stock alerts
- `GET /api/v1/stock/product/{productId}/movements` - Stock history audit trail
- `GET /api/v1/stock/movements/recent` - Top 20 recent stock movements

### Orders (`/api/v1/orders`)
- `POST /api/v1/orders` - Create order
- `GET /api/v1/orders` - Get orders (filtered & paginated)
- `GET /api/v1/orders/{id}` - Get order details
- `GET /api/v1/orders/number/{orderNumber}` - Get order by order number
- `PATCH /api/v1/orders/{id}/status` - Update lifecycle status (`CONFIRMED` deducts stock, `CANCELLED` restores stock)

### Dashboard Analytics (`/api/v1/dashboard`)
- `GET /api/v1/dashboard/summary` - Metrics summary, revenue, status breakdown (`ADMIN`, `MANAGER`)

---

## Deployment Configuration

This repository includes:
- `Dockerfile` for multi-stage containerization
- `render.yaml` for zero-config Render web service deployment
- `docker-compose.yml` for multi-container orchestration with MySQL 8

---

## License

Licensed under Apache 2.0.
