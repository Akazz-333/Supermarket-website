# 🛒 SB Marts - Full-Stack Online Supermarket & Inventory Management System

A full-stack supermarket web application featuring a customer storefront, express grocery ordering, real-time visual order tracking, an executive admin control panel, and inventory lifecycle management backed by Java 17 Spring Boot and MySQL 8.0.

---

## 🌟 Key Features

### 🛍️ Customer Storefront (`frontend/index.html`)
- ☀️ **Default Light Theme**: Ultra-premium luxury aesthetic with light/dark mode toggling (`🌙`).
- 🧭 **Header Navigation Bar**: Interactive Categories dropdown, Special Deals, Wishlist drawer badge, My Orders, and Cart.
- 👆 **Full Product Card Click**: Click anywhere on a product card to open the dedicated **Product Details View**.
- 🔍 **Instant Global Search Modal**: Real-time instant search as you type.
- 🔥 **Weekly Deals Page**: 20% OFF produce, BOGO dairy, and 15% OFF bakery bundles.
- 🔒 **Cart Authentication Protection**: Automatically prompts Sign In / Sign Up when opening cart without an account.
- 📦 **Real-Time Order Stepper Tracker**: 4-step visual progress bar (`Order Placed` → `Confirmed` → `Out for Delivery` → `Delivered`) updating live every 3 seconds!

### ⚙️ Admin Control Panel (`frontend/admin.html`)
- ☀️ **Executive Light Slate Theme**: B&W minimalist SaaS interface.
- 📈 **Real-Time Operations Dashboard**: Sales revenue, total orders count, order breakdown, and dynamic low-stock warning alerts.
- 📦 **Stock-In & Stock-Out Operations**: Incremental inventory adjustments with audit trail logging.
- 🏷️ **Product & Category CRUD**: Manage supermarket inventory and category structure.
- 🔄 **Order Lifecycle Management**: Transition order statuses (`PENDING` → `CONFIRMED` → `SHIPPED` → `DELIVERED` | `CANCELLED`) with automatic MySQL inventory deduction and restoration.

---

## 🛠️ Technology Stack

- **Frontend**: HTML5, Vanilla JavaScript, Custom CSS3 Design System, Font Awesome 6.
- **Backend**: Java 17, Spring Boot 3.2.5, Spring Security (JWT Authentication), Spring Data JPA, Hibernate.
- **Database**: MySQL 8.0 (`supermarket_db`).

---

## 📁 Repository Structure

```
Supermarket-website/
├── frontend/
│   ├── index.html            # Customer Storefront Webpage
│   ├── admin.html            # Admin Control Panel Webpage
│   ├── user-app.js           # Storefront Application Logic
│   ├── admin-app.js          # Admin Control Panel Logic
│   ├── user-store.css        # Storefront CSS Design System
│   ├── admin-theme.css       # Admin Control Panel CSS Theme
│   └── styles.css            # Base Stylesheet
└── backend/
    ├── pom.xml               # Maven Dependencies
    └── src/main/java/com/supermarket/inventory/
        ├── controller/       # REST API Controllers
        ├── entity/           # JPA Entities (Product, Category, Order, StockMovement)
        ├── repository/       # Data Access Repositories
        ├── security/         # JWT Security Configuration
        └── service/          # Business Logic Services
```

---

## 🚀 Local Setup & Run Instructions

### 1. Database Setup (MySQL)
Create database schema in MySQL:
```sql
CREATE DATABASE supermarket_db;
```

### 2. Run Backend (Spring Boot)
```bash
cd backend
.\mvnw spring-boot:run
```
The REST API will start live on `http://localhost:8081/api/v1`.

### 3. Run Frontend
Open `frontend/index.html` and `frontend/admin.html` in your browser or run via local HTTP server:
```bash
cd frontend
python -m http.server 8080
```
- Storefront: `http://localhost:8080/index.html`
- Admin Panel: `http://localhost:8080/admin.html`
