/* ==========================================================================
   FreshMart Supermarket & Grocery Store - Frontend Logic (app.js)
   ========================================================================== */

// API Configuration
const API_BASE_URL = 'http://localhost:8081/api/v1';
let isBackendLive = false;
let authToken = '';

// Local Reactive State (Pre-seeded matching Java DataInitializer)
const state = {
    categories: [
        { id: 1, name: "Dairy & Eggs", description: "Fresh milk, cheese, butter, and eggs" },
        { id: 2, name: "Fresh Produce", description: "Organic fruits and fresh vegetables" },
        { id: 3, name: "Beverages", description: "Fruit juices, soda, water, and tea" },
        { id: 4, name: "Bakery", description: "Freshly baked bread, rolls, and pastries" },
        { id: 5, name: "Meat & Seafood", description: "Fresh poultry, beef, pork, and seafood" }
    ],
    products: [
        { id: 1, sku: "SKU-RICE01", name: "Royal Basmati Rice 5kg", description: "Premium long-grain fragrant basmati rice", price: 18.99, availableStock: 50, minStockLevel: 10, categoryId: 2, categoryName: "Fresh Produce", supplierName: "Global Grain Suppliers", active: true },
        { id: 2, sku: "SKU-MILK01", name: "Organic Whole Milk 1 Gallon", description: "Pasteurized grade A organic whole milk", price: 4.49, availableStock: 30, minStockLevel: 8, categoryId: 1, categoryName: "Dairy & Eggs", supplierName: "Sunshine Dairy Farms", active: true },
        { id: 3, sku: "SKU-APPL01", name: "Honeycrisp Fresh Apples 1kg", description: "Crisp, sweet fresh red Honeycrisp apples", price: 3.99, availableStock: 4, minStockLevel: 10, categoryId: 2, categoryName: "Fresh Produce", supplierName: "Valley Orchards", active: true },
        { id: 4, sku: "SKU-JUIC01", name: "Florida Orange Juice 1.5L", description: "100% pure squeezed orange juice with pulp", price: 5.29, availableStock: 25, minStockLevel: 5, categoryId: 3, categoryName: "Beverages", supplierName: "Citrus Grove Inc", active: true },
        { id: 5, sku: "SKU-BRD01", name: "Whole Grain Whole Wheat Bread", description: "Freshly baked 100% whole grain wheat bread", price: 2.99, availableStock: 0, minStockLevel: 5, categoryId: 4, categoryName: "Bakery", supplierName: "Golden Crust Bakery", active: true },
        { id: 6, sku: "SKU-CHCK01", name: "Boneless Chicken Breast 1kg", description: "Fresh skinless boneless chicken breast", price: 9.99, availableStock: 40, minStockLevel: 12, categoryId: 5, categoryName: "Meat & Seafood", supplierName: "Farm Fresh Poultry", active: true }
    ],
    cart: [],
    orders: [
        {
            id: 101,
            orderNumber: "ORD-20260808-10001",
            customerName: "Alice Johnson",
            customerEmail: "alice@example.com",
            customerPhone: "+1-555-0101",
            status: "CONFIRMED",
            totalAmount: 51.45,
            items: [
                { productId: 1, productName: "Royal Basmati Rice 5kg", quantity: 2, unitPrice: 18.99, subtotal: 37.98 },
                { productId: 2, productName: "Organic Whole Milk 1 Gallon", quantity: 3, unitPrice: 4.49, subtotal: 13.47 }
            ],
            createdAt: new Date().toISOString()
        },
        {
            id: 102,
            orderNumber: "ORD-20260808-10002",
            customerName: "Bob Smith",
            customerEmail: "bob@example.com",
            customerPhone: "+1-555-0102",
            status: "PENDING",
            totalAmount: 25.27,
            items: [
                { productId: 4, productName: "Florida Orange Juice 1.5L", quantity: 1, unitPrice: 5.29, subtotal: 5.29 },
                { productId: 6, productName: "Boneless Chicken Breast 1kg", quantity: 2, unitPrice: 9.99, subtotal: 19.98 }
            ],
            createdAt: new Date().toISOString()
        }
    ],
    stockMovements: [
        { id: 1, productId: 1, productName: "Royal Basmati Rice 5kg", type: "STOCK_IN", quantity: 50, previousStock: 0, newStock: 50, reason: "Initial Shipment", performedBy: "admin@supermarket.com", createdAt: new Date().toISOString() },
        { id: 2, productId: 1, productName: "Royal Basmati Rice 5kg", type: "ORDER_DEDUCTION", quantity: 2, previousStock: 52, newStock: 50, reason: "Order #ORD-20260808-10001", performedBy: "System", createdAt: new Date().toISOString() }
    ]
};

// DOM Elements Initialization
document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    initTheme();
    checkBackendHealth();
    renderAllViews();
    setupEventListeners();
});

// View Navigation Router
function initNavigation() {
    const navLinks = document.querySelectorAll('.nav-link');
    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetId = link.getAttribute('data-target');

            navLinks.forEach(l => l.classList.remove('active'));
            link.classList.add('active');

            document.querySelectorAll('.view-section').forEach(sec => sec.classList.remove('active'));
            document.getElementById(targetId).classList.add('active');

            // Refresh view data on switch
            if (targetId === 'dashboard-view') renderDashboardView();
            if (targetId === 'categories-view') renderCategoriesView();
            if (targetId === 'inventory-view') renderInventoryView();
            if (targetId === 'orders-view') renderOrdersView();
        });
    });
}

// Light/Dark Theme Switcher
function initTheme() {
    const themeBtn = document.getElementById('themeToggleBtn');
    themeBtn.addEventListener('click', () => {
        const currentTheme = document.documentElement.getAttribute('data-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        document.documentElement.setAttribute('data-theme', newTheme);
        themeBtn.innerHTML = newTheme === 'dark' ? '<i class="fa-solid fa-moon"></i>' : '<i class="fa-solid fa-sun"></i>';
    });
}

// Check if Spring Boot REST API is running locally
async function checkBackendHealth() {
    try {
        const response = await fetch(`${API_BASE_URL}/auth/me`, { method: 'GET' });
        if (response.status !== 404) {
            isBackendLive = true;
            showToast("Connected to Spring Boot REST API", "success");
        }
    } catch (e) {
        isBackendLive = false;
        console.log("Running in reactive client mode (Spring Boot backend offline)");
    }
}

// Render All Views
function renderAllViews() {
    renderCategoryFilters();
    renderCatalogGrid();
    renderCategoriesView();
    renderInventoryView();
    renderOrdersView();
    renderDashboardView();
    updateCartUI();
}

// Render Category Filter Dropdowns
function renderCategoryFilters() {
    const filterSelect = document.getElementById('categoryFilter');
    const productCatSelect = document.getElementById('pCategory');

    let optionsHtml = '<option value="">All Categories</option>';
    let formOptionsHtml = '';

    state.categories.forEach(cat => {
        optionsHtml += `<option value="${cat.id}">${cat.name}</option>`;
        formOptionsHtml += `<option value="${cat.id}">${cat.name}</option>`;
    });

    if (filterSelect) filterSelect.innerHTML = optionsHtml;
    if (productCatSelect) productCatSelect.innerHTML = formOptionsHtml;
}

// Render Product Catalog Grid
function renderCatalogGrid() {
    const grid = document.getElementById('productGrid');
    const searchVal = document.getElementById('searchInput').value.toLowerCase().trim();
    const catVal = document.getElementById('categoryFilter').value;
    const priceVal = document.getElementById('priceFilter').value;
    const lowStockVal = document.getElementById('lowStockOnly').checked;

    let filtered = state.products.filter(p => p.active);

    if (searchVal) {
        filtered = filtered.filter(p => p.name.toLowerCase().includes(searchVal) || p.sku.toLowerCase().includes(searchVal) || p.description.toLowerCase().includes(searchVal));
    }

    if (catVal) {
        filtered = filtered.filter(p => p.categoryId == catVal);
    }

    if (priceVal) {
        if (priceVal === '0-5') filtered = filtered.filter(p => p.price <= 5);
        if (priceVal === '5-15') filtered = filtered.filter(p => p.price > 5 && p.price <= 15);
        if (priceVal === '15-50') filtered = filtered.filter(p => p.price > 15);
    }

    if (lowStockVal) {
        filtered = filtered.filter(p => p.availableStock <= p.minStockLevel);
    }

    if (filtered.length === 0) {
        grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 3rem; color: var(--text-secondary);">
            <i class="fa-solid fa-basket-shopping" style="font-size: 3rem; margin-bottom: 1rem; color: var(--text-muted);"></i>
            <h3>No products found matching filters</h3>
        </div>`;
        return;
    }

    grid.innerHTML = filtered.map(p => {
        const isLow = p.availableStock <= p.minStockLevel && p.availableStock > 0;
        const isOut = p.availableStock === 0;

        let badgeClass = 'badge-success';
        let badgeText = 'IN STOCK';
        let progressClass = '';

        if (isOut) {
            badgeClass = 'badge-danger';
            badgeText = 'OUT OF STOCK';
            progressClass = 'danger';
        } else if (isLow) {
            badgeClass = 'badge-warning';
            badgeText = 'LOW STOCK';
            progressClass = 'warning';
        }

        const maxStockRef = Math.max(p.availableStock, p.minStockLevel * 3, 20);
        const percent = Math.min(100, Math.round((p.availableStock / maxStockRef) * 100));

        return `
            <div class="product-card">
                <span class="product-badge ${badgeClass}">${badgeText}</span>
                <div>
                    <div class="product-cat">${getCategoryName(p.categoryId)}</div>
                    <div class="product-title">${p.name}</div>
                    <div class="product-sku">${p.sku}</div>
                    <div class="product-price">$${p.price.toFixed(2)}</div>
                </div>

                <div class="stock-meter">
                    <div class="stock-info">
                        <span>Available: <strong>${p.availableStock}</strong></span>
                        <span>Min: ${p.minStockLevel}</span>
                    </div>
                    <div class="progress-bar">
                        <div class="progress-fill ${progressClass}" style="width: ${percent}%"></div>
                    </div>
                </div>

                <button class="btn btn-primary" onclick="addToCart(${p.id})" ${isOut ? 'disabled style="opacity:0.5; cursor:not-allowed;"' : ''}>
                    <i class="fa-solid fa-cart-plus"></i> ${isOut ? 'Out of Stock' : 'Add to Cart'}
                </button>
            </div>
        `;
    }).join('');
}

// Shopping Cart Management
function addToCart(productId) {
    const product = state.products.find(p => p.id === productId);
    if (!product || product.availableStock <= 0) {
        showToast("Product is out of stock!", "error");
        return;
    }

    const existing = state.cart.find(item => item.productId === productId);
    if (existing) {
        if (existing.quantity >= product.availableStock) {
            showToast(`Cannot add more. Available stock limit reached (${product.availableStock})`, "warning");
            return;
        }
        existing.quantity += 1;
    } else {
        state.cart.push({
            productId: product.id,
            name: product.name,
            price: product.price,
            quantity: 1
        });
    }

    updateCartUI();
    showToast(`Added ${product.name} to cart`, "success");
}

function updateCartQty(productId, change) {
    const item = state.cart.find(i => i.productId === productId);
    const product = state.products.find(p => p.id === productId);
    if (!item) return;

    item.quantity += change;
    if (item.quantity > product.availableStock) {
        item.quantity = product.availableStock;
        showToast(`Stock limit reached (${product.availableStock})`, "warning");
    }

    if (item.quantity <= 0) {
        state.cart = state.cart.filter(i => i.productId !== productId);
    }

    updateCartUI();
}

function updateCartUI() {
    const cartBadge = document.getElementById('cartBadgeCount');
    const container = document.getElementById('cartItemsContainer');
    const totalEl = document.getElementById('cartTotalBill');

    const totalCount = state.cart.reduce((sum, item) => sum + item.quantity, 0);
    const totalBill = state.cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);

    if (cartBadge) cartBadge.innerText = totalCount;
    if (totalEl) totalEl.innerText = `$${totalBill.toFixed(2)}`;

    if (!container) return;

    if (state.cart.length === 0) {
        container.innerHTML = '<p style="text-align:center; color:var(--text-muted); padding:1.5rem;">Your cart is empty</p>';
        return;
    }

    container.innerHTML = state.cart.map(item => `
        <div style="display:flex; justify-content:space-between; align-items:center; padding:0.6rem 0; border-bottom:1px solid var(--border-color);">
            <div>
                <div style="font-weight:600; font-size:0.92rem;">${item.name}</div>
                <div style="font-size:0.8rem; color:var(--primary);">$${item.price.toFixed(2)} each</div>
            </div>
            <div style="display:flex; align-items:center; gap:0.5rem;">
                <button class="btn btn-outline btn-sm" onclick="updateCartQty(${item.productId}, -1)">-</button>
                <span style="font-weight:700;">${item.quantity}</span>
                <button class="btn btn-outline btn-sm" onclick="updateCartQty(${item.productId}, 1)">+</button>
            </div>
        </div>
    `).join('');
}

// Order Checkout Submission
document.getElementById('checkoutForm')?.addEventListener('submit', (e) => {
    e.preventDefault();

    if (state.cart.length === 0) {
        showToast("Your cart is empty! Add products first.", "warning");
        return;
    }

    const name = document.getElementById('custName').value.trim();
    const email = document.getElementById('custEmail').value.trim();
    const phone = document.getElementById('custPhone').value.trim();
    const notes = document.getElementById('custNotes').value.trim();

    const orderNumber = "ORD-" + new Date().toISOString().slice(0,10).replace(/-/g,'') + "-" + Math.floor(10000 + Math.random() * 90000);
    const totalAmount = state.cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);

    const newOrder = {
        id: state.orders.length + 101,
        orderNumber: orderNumber,
        customerName: name,
        customerEmail: email,
        customerPhone: phone,
        notes: notes,
        status: "PENDING",
        totalAmount: totalAmount,
        items: state.cart.map(i => ({ productId: i.productId, productName: i.name, quantity: i.quantity, unitPrice: i.price, subtotal: i.price * i.quantity })),
        createdAt: new Date().toISOString()
    };

    state.orders.unshift(newOrder);
    state.cart = [];
    updateCartUI();

    document.getElementById('cartModal').classList.remove('active');
    showToast(`Order #${orderNumber} created successfully!`, "success");
    renderOrdersView();
    renderDashboardView();
});

// Category Management View
function renderCategoriesView() {
    const tbody = document.getElementById('categoriesTableBody');
    if (!tbody) return;

    tbody.innerHTML = state.categories.map(cat => {
        const count = state.products.filter(p => p.categoryId === cat.id && p.active).length;
        return `
            <tr>
                <td>#${cat.id}</td>
                <td><strong>${cat.name}</strong></td>
                <td>${cat.description || '-'}</td>
                <td><span class="role-pill manager">${count} Products</span></td>
                <td>
                    <button class="btn btn-danger btn-sm" onclick="deleteCategory(${cat.id})"><i class="fa-solid fa-trash"></i> Delete</button>
                </td>
            </tr>
        `;
    }).join('');
}

function deleteCategory(categoryId) {
    const category = state.categories.find(c => c.id === categoryId);
    const hasProducts = state.products.some(p => p.categoryId === categoryId && p.active);

    if (hasProducts) {
        showToast(`Cannot delete '${category.name}' because products are linked to it!`, "error");
        return;
    }

    state.categories = state.categories.filter(c => c.id !== categoryId);
    renderCategoriesView();
    renderCategoryFilters();
    showToast(`Category deleted successfully`, "success");
}

// Inventory & Stock Operations View
function renderInventoryView() {
    const tbody = document.getElementById('inventoryTableBody');
    const historyLog = document.getElementById('movementHistoryLog');
    if (!tbody) return;

    tbody.innerHTML = state.products.filter(p => p.active).map(p => `
        <tr>
            <td><code>${p.sku}</code></td>
            <td><strong>${p.name}</strong></td>
            <td>${getCategoryName(p.categoryId)}</td>
            <td><strong style="color:${p.availableStock <= p.minStockLevel ? 'var(--danger)' : 'var(--primary)'};">${p.availableStock}</strong></td>
            <td>${p.minStockLevel}</td>
            <td>
                <button class="btn btn-primary btn-sm" onclick="openStockModal(${p.id}, 'STOCK_IN')"><i class="fa-solid fa-plus"></i> Stock In</button>
                <button class="btn btn-danger btn-sm" onclick="openStockModal(${p.id}, 'STOCK_OUT')"><i class="fa-solid fa-minus"></i> Stock Out</button>
            </td>
        </tr>
    `).join('');

    if (historyLog) {
        historyLog.innerHTML = state.stockMovements.map(m => `
            <div style="padding:0.75rem; border-bottom:1px solid var(--border-color); font-size:0.85rem;">
                <div style="display:flex; justify-content:space-between; font-weight:600;">
                    <span>${m.productName}</span>
                    <span class="role-pill ${m.type.includes('IN') || m.type.includes('RESTOCK') ? 'staff' : 'admin'}">${m.type}</span>
                </div>
                <div style="color:var(--text-secondary); margin-top:0.2rem;">
                    Qty: <strong>${m.quantity}</strong> | ${m.previousStock} → ${m.newStock}
                </div>
                <div style="font-size:0.75rem; color:var(--text-muted);">${m.reason || 'Manual Adjustment'}</div>
            </div>
        `).join('');
    }
}

function openStockModal(productId, type) {
    const product = state.products.find(p => p.id === productId);
    if (!product) return;

    document.getElementById('stockProductId').value = productId;
    document.getElementById('stockOperationType').value = type;
    document.getElementById('stockProductName').value = `${product.name} (${product.sku})`;
    document.getElementById('stockModalTitle').innerText = type === 'STOCK_IN' ? 'Stock-In (Add Inventory)' : 'Stock-Out (Deduct Inventory)';
    document.getElementById('stockQuantity').value = 10;
    document.getElementById('stockReason').value = type === 'STOCK_IN' ? 'Vendor delivery shipment' : 'Manual stock count deduction';

    document.getElementById('stockModal').classList.add('active');
}

document.getElementById('stockForm')?.addEventListener('submit', (e) => {
    e.preventDefault();
    const productId = parseInt(document.getElementById('stockProductId').value);
    const type = document.getElementById('stockOperationType').value;
    const qty = parseInt(document.getElementById('stockQuantity').value);
    const reason = document.getElementById('stockReason').value.trim();

    const product = state.products.find(p => p.id === productId);
    if (!product) return;

    const previousStock = product.availableStock;

    if (type === 'STOCK_OUT' && previousStock < qty) {
        showToast(`Cannot deduct ${qty}. Available stock is only ${previousStock}`, "error");
        return;
    }

    const newStock = type === 'STOCK_IN' ? previousStock + qty : previousStock - qty;
    product.availableStock = newStock;

    state.stockMovements.unshift({
        id: state.stockMovements.length + 1,
        productId: product.id,
        productName: product.name,
        type: type,
        quantity: qty,
        previousStock: previousStock,
        newStock: newStock,
        reason: reason,
        performedBy: "admin@supermarket.com",
        createdAt: new Date().toISOString()
    });

    document.getElementById('stockModal').classList.remove('active');
    showToast(`Stock updated! New level: ${newStock}`, "success");
    renderInventoryView();
    renderCatalogGrid();
    renderDashboardView();
});

// Order Lifecycle Workflow View
function renderOrdersView() {
    const tbody = document.getElementById('ordersTableBody');
    if (!tbody) return;

    tbody.innerHTML = state.orders.map(o => `
        <tr>
            <td><code>${o.orderNumber}</code></td>
            <td><strong>${o.customerName}</strong><br><span style="font-size:0.78rem; color:var(--text-muted);">${o.customerEmail || ''}</span></td>
            <td>${o.items.map(i => `${i.productName} (x${i.quantity})`).join(', ')}</td>
            <td><strong style="color:var(--primary);">$${o.totalAmount.toFixed(2)}</strong></td>
            <td><span class="role-pill ${o.status === 'CONFIRMED' ? 'staff' : o.status === 'DELIVERED' ? 'staff' : o.status === 'CANCELLED' ? 'admin' : 'manager'}">${o.status}</span></td>
            <td style="font-size:0.8rem; color:var(--text-muted);">${new Date(o.createdAt).toLocaleDateString()}</td>
            <td>
                <select class="select-control" style="padding:0.25rem 0.5rem; font-size:0.8rem;" onchange="updateOrderStatus(${o.id}, this.value)">
                    <option value="PENDING" ${o.status === 'PENDING' ? 'selected' : ''}>PENDING</option>
                    <option value="CONFIRMED" ${o.status === 'CONFIRMED' ? 'selected' : ''}>CONFIRMED (Deduct Stock)</option>
                    <option value="SHIPPED" ${o.status === 'SHIPPED' ? 'selected' : ''}>SHIPPED</option>
                    <option value="DELIVERED" ${o.status === 'DELIVERED' ? 'selected' : ''}>DELIVERED</option>
                    <option value="CANCELLED" ${o.status === 'CANCELLED' ? 'selected' : ''}>CANCELLED (Restore Stock)</option>
                </select>
            </td>
        </tr>
    `).join('');
}

function updateOrderStatus(orderId, newStatus) {
    const order = state.orders.find(o => o.id === orderId);
    if (!order) return;

    const currentStatus = order.status;
    if (currentStatus === newStatus) return;

    // Rule 1: Confirming order deducts stock
    if (newStatus === 'CONFIRMED' && currentStatus === 'PENDING') {
        for (let item of order.items) {
            const product = state.products.find(p => p.id === item.productId);
            if (product && product.availableStock < item.quantity) {
                showToast(`Cannot confirm. Insufficient stock for ${product.name}!`, "error");
                renderOrdersView(); // Reset dropdown
                return;
            }
        }

        // Apply deduction
        for (let item of order.items) {
            const product = state.products.find(p => p.id === item.productId);
            if (product) {
                const prev = product.availableStock;
                product.availableStock -= item.quantity;
                state.stockMovements.unshift({
                    id: state.stockMovements.length + 1,
                    productId: product.id,
                    productName: product.name,
                    type: "ORDER_DEDUCTION",
                    quantity: item.quantity,
                    previousStock: prev,
                    newStock: product.availableStock,
                    reason: `Auto deduction for confirmed order #${order.orderNumber}`,
                    performedBy: "System",
                    createdAt: new Date().toISOString()
                });
            }
        }
        showToast(`Order confirmed & stock automatically deducted!`, "success");
    }

    // Rule 2: Cancelling confirmed/shipped order restores stock
    if (newStatus === 'CANCELLED' && (currentStatus === 'CONFIRMED' || currentStatus === 'SHIPPED')) {
        for (let item of order.items) {
            const product = state.products.find(p => p.id === item.productId);
            if (product) {
                const prev = product.availableStock;
                product.availableStock += item.quantity;
                state.stockMovements.unshift({
                    id: state.stockMovements.length + 1,
                    productId: product.id,
                    productName: product.name,
                    type: "ORDER_RESTOCK",
                    quantity: item.quantity,
                    previousStock: prev,
                    newStock: product.availableStock,
                    reason: `Auto restoration for cancelled order #${order.orderNumber}`,
                    performedBy: "System",
                    createdAt: new Date().toISOString()
                });
            }
        }
        showToast(`Order cancelled & stock restored to inventory!`, "warning");
    }

    order.status = newStatus;
    renderOrdersView();
    renderCatalogGrid();
    renderInventoryView();
    renderDashboardView();
}

// Executive Dashboard Analytics View
function renderDashboardView() {
    const totalProducts = state.products.filter(p => p.active).length;
    const lowStockCount = state.products.filter(p => p.active && p.availableStock <= p.minStockLevel && p.availableStock > 0).length;
    const outOfStockCount = state.products.filter(p => p.active && p.availableStock === 0).length;
    const totalOrders = state.orders.length;

    const totalRevenue = state.orders
        .filter(o => o.status === 'CONFIRMED' || o.status === 'SHIPPED' || o.status === 'DELIVERED')
        .reduce((sum, o) => sum + o.totalAmount, 0);

    document.getElementById('statTotalProducts').innerText = totalProducts;
    document.getElementById('statLowStock').innerText = lowStockCount;
    document.getElementById('statOutOfStock').innerText = outOfStockCount;
    document.getElementById('statTotalOrders').innerText = totalOrders;
    document.getElementById('statTotalRevenue').innerText = `$${totalRevenue.toFixed(2)}`;

    // Order status breakdown pills
    const statusDiv = document.getElementById('orderStatusBreakdown');
    if (statusDiv) {
        const counts = { PENDING: 0, CONFIRMED: 0, SHIPPED: 0, DELIVERED: 0, CANCELLED: 0 };
        state.orders.forEach(o => counts[o.status] = (counts[o.status] || 0) + 1);

        statusDiv.innerHTML = Object.keys(counts).map(st => `
            <div style="display:flex; justify-content:space-between; margin-bottom:0.4rem; font-size:0.88rem;">
                <span>${st}:</span>
                <strong>${counts[st]} orders</strong>
            </div>
        `).join('');
    }

    // Low stock warnings
    const alertsDiv = document.getElementById('lowStockAlertsList');
    if (alertsDiv) {
        const lowItems = state.products.filter(p => p.active && p.availableStock <= p.minStockLevel);
        if (lowItems.length === 0) {
            alertsDiv.innerHTML = '<p style="color:var(--text-muted);">All grocery items have healthy inventory levels!</p>';
        } else {
            alertsDiv.innerHTML = lowItems.map(p => `
                <div style="padding:0.6rem; border:1px solid var(--border-color); border-radius:var(--radius-sm); margin-bottom:0.5rem; display:flex; justify-content:space-between; align-items:center;">
                    <div>
                        <strong>${p.name}</strong> (${p.sku})
                        <div style="font-size:0.78rem; color:var(--danger);">Current Stock: ${p.availableStock} (Min: ${p.minStockLevel})</div>
                    </div>
                    <button class="btn btn-primary btn-sm" onclick="openStockModal(${p.id}, 'STOCK_IN')">Restock</button>
                </div>
            `).join('');
        }
    }
}

// Create Product Form Handler
document.getElementById('productForm')?.addEventListener('submit', (e) => {
    e.preventDefault();
    const sku = document.getElementById('pSku').value.trim();
    const name = document.getElementById('pName').value.trim();
    const categoryId = parseInt(document.getElementById('pCategory').value);
    const price = parseFloat(document.getElementById('pPrice').value);
    const availableStock = parseInt(document.getElementById('pStock').value);
    const minStockLevel = parseInt(document.getElementById('pMinStock').value);

    if (state.products.some(p => p.sku.toLowerCase() === sku.toLowerCase())) {
        showToast(`SKU '${sku}' already exists!`, "error");
        return;
    }

    const newProduct = {
        id: state.products.length + 1,
        sku: sku,
        name: name,
        description: name,
        price: price,
        availableStock: availableStock,
        minStockLevel: minStockLevel,
        categoryId: categoryId,
        active: true
    };

    state.products.push(newProduct);
    document.getElementById('productModal').classList.remove('active');
    showToast(`Product '${name}' created!`, "success");
    renderCatalogGrid();
    renderInventoryView();
    renderDashboardView();
});

// Setup Modal Listeners & Event Handlers
function setupEventListeners() {
    // Open/Close Cart Drawer
    document.getElementById('openCartBtn')?.addEventListener('click', () => {
        document.getElementById('cartModal').classList.add('active');
    });
    document.getElementById('closeCartBtn')?.addEventListener('click', () => {
        document.getElementById('cartModal').classList.remove('active');
    });

    // Stock Modal Close
    document.getElementById('closeStockModalBtn')?.addEventListener('click', () => {
        document.getElementById('stockModal').classList.remove('active');
    });

    // Product Modal Open/Close
    document.getElementById('addProductModalBtn')?.addEventListener('click', () => {
        document.getElementById('productModal').classList.add('active');
    });
    document.getElementById('closeProductModalBtn')?.addEventListener('click', () => {
        document.getElementById('productModal').classList.remove('active');
    });

    // Search & Filter Events
    document.getElementById('searchInput')?.addEventListener('input', renderCatalogGrid);
    document.getElementById('categoryFilter')?.addEventListener('change', renderCatalogGrid);
    document.getElementById('priceFilter')?.addEventListener('change', renderCatalogGrid);
    document.getElementById('lowStockOnly')?.addEventListener('change', renderCatalogGrid);
}

// Helpers
function getCategoryName(catId) {
    const cat = state.categories.find(c => c.id === catId);
    return cat ? cat.name : 'General';
}

function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    const icon = type === 'success' ? 'fa-circle-check' : type === 'error' ? 'fa-circle-xmark' : 'fa-triangle-exclamation';
    toast.innerHTML = `<i class="fa-solid ${icon}" style="color:${type==='success'?'var(--primary)':type==='error'?'var(--danger)':'var(--accent)'}"></i> <span>${message}</span>`;

    container.appendChild(toast);
    setTimeout(() => toast.remove(), 3500);
}
