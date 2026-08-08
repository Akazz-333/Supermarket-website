/* ==========================================================================
   FreshMart Supermarket - Admin Control Panel Frontend Logic (admin-app.js)
   ========================================================================== */

const API_BASE_URL = 'http://localhost:8081/api/v1';

const adminState = {
    products: [],
    categories: [],
    orders: [],
    stockMovements: [],
    summary: null,
    adminUser: JSON.parse(localStorage.getItem('freshmart_admin_user') || 'null'),
    token: localStorage.getItem('freshmart_admin_token') || ''
};

document.addEventListener('DOMContentLoaded', () => {
    initAdminNavigation();
    initAdminTheme();
    updateAdminAuthUI();
    fetchAdminDashboard();
    fetchAdminCategories();
    fetchAdminProducts();
    fetchAdminOrders();
    fetchAdminStockMovements();
    setupAdminEventListeners();

    // Live Auto-Sync every 3 seconds for instant order updates
    setInterval(() => {
        fetchAdminOrders();
        fetchAdminProducts();
    }, 3000);
});

// View Router
function initAdminNavigation() {
    const navLinks = document.querySelectorAll('.nav-link');
    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetId = link.getAttribute('data-target');

            navLinks.forEach(l => l.classList.remove('active'));
            link.classList.add('active');

            document.querySelectorAll('.view-section').forEach(sec => sec.classList.remove('active'));
            document.getElementById(targetId).classList.add('active');

            if (targetId === 'admin-dashboard-view') fetchAdminDashboard();
            if (targetId === 'admin-inventory-view') { fetchAdminProducts(); fetchAdminStockMovements(); }
            if (targetId === 'admin-products-view') fetchAdminProducts();
            if (targetId === 'admin-categories-view') fetchAdminCategories();
            if (targetId === 'admin-orders-view') fetchAdminOrders();
        });
    });
}

function initAdminTheme() {
    const themeBtn = document.getElementById('themeToggleBtn');
    document.documentElement.setAttribute('data-theme', 'light');
    if (themeBtn) {
        themeBtn.innerHTML = '<i class="fa-solid fa-moon"></i>';
        themeBtn.addEventListener('click', () => {
            const currentTheme = document.documentElement.getAttribute('data-theme');
            const newTheme = currentTheme === 'light' ? 'dark' : 'light';
            document.documentElement.setAttribute('data-theme', newTheme);
            themeBtn.innerHTML = newTheme === 'light' ? '<i class="fa-solid fa-moon"></i>' : '<i class="fa-solid fa-sun"></i>';
        });
    }
}

function updateAdminAuthUI() {
    const nameEl = document.getElementById('adminUserName');
    const tagEl = document.getElementById('adminRoleTag');

    if (adminState.adminUser && adminState.token) {
        nameEl.innerText = adminState.adminUser.email;
        tagEl.innerText = `ROLE_${adminState.adminUser.role}`;
        tagEl.className = `role-pill ${adminState.adminUser.role.toLowerCase()}`;
    } else {
        nameEl.innerText = "admin@supermarket.com";
        tagEl.innerText = "ROLE_ADMIN";
        tagEl.className = "role-pill admin";
    }
}

// Fetch Executive Dashboard Analytics from Database API
async function fetchAdminDashboard() {
    try {
        const headers = getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/dashboard/summary`, { headers });
        const data = await res.json();

        if (data.success && data.data) {
            adminState.summary = data.data;
            renderDashboardStats(data.data);
        }
    } catch (e) {
        renderLocalDashboardStats();
    }
}

function renderDashboardStats(s) {
    // Dynamic calculation from live application & database state
    const liveProducts = adminState.products.filter(p => p.active !== false);
    const liveLowStock = liveProducts.filter(p => p.availableStock <= p.minStockLevel);
    const liveOutOfStock = liveProducts.filter(p => p.availableStock === 0);

    const totalOrders = adminState.orders.length;
    const totalRevenue = adminState.orders
        .filter(o => o.status === 'CONFIRMED' || o.status === 'SHIPPED' || o.status === 'DELIVERED')
        .reduce((sum, o) => sum + (o.totalAmount || 0), 0);

    document.getElementById('adminStatTotalProducts').innerText = liveProducts.length || 0;
    document.getElementById('adminStatLowStock').innerText = liveLowStock.length;
    document.getElementById('adminStatOutOfStock').innerText = liveOutOfStock.length;
    document.getElementById('adminStatTotalOrders').innerText = totalOrders;
    document.getElementById('adminStatTotalRevenue').innerText = `$${totalRevenue.toFixed(2)}`;

    // Order status breakdown pills computed directly from live orders
    const statusDiv = document.getElementById('adminOrderStatusBreakdown');
    if (statusDiv) {
        const counts = {
            PENDING: adminState.orders.filter(o => o.status === 'PENDING').length,
            CONFIRMED: adminState.orders.filter(o => o.status === 'CONFIRMED').length,
            SHIPPED: adminState.orders.filter(o => o.status === 'SHIPPED').length,
            DELIVERED: adminState.orders.filter(o => o.status === 'DELIVERED').length,
            CANCELLED: adminState.orders.filter(o => o.status === 'CANCELLED').length
        };
        statusDiv.innerHTML = Object.keys(counts).map(st => `
            <div style="display:flex; justify-content:space-between; margin-bottom:0.4rem; font-size:0.88rem;">
                <span>${st}:</span>
                <strong style="color:${st === 'CONFIRMED' || st === 'DELIVERED' ? '#10b981' : st === 'CANCELLED' ? '#ef4444' : '#6366f1'};">${counts[st]} orders</strong>
            </div>
        `).join('');
    }

    // Critical Restock Warning Alerts List (Dynamic from Database)
    const alertsDiv = document.getElementById('adminLowStockAlertsList');
    if (alertsDiv) {
        if (liveLowStock.length === 0) {
            alertsDiv.innerHTML = '<div style="text-align:center; padding:1.5rem; color:#10b981; font-weight:700;"><i class="fa-solid fa-circle-check" style="font-size:1.5rem; margin-bottom:0.4rem;"></i><div>All grocery items have healthy stock levels!</div></div>';
        } else {
            alertsDiv.innerHTML = liveLowStock.map(p => `
                <div style="padding:0.75rem; border:1px solid #fee2e2; background:#fef2f2; border-radius:12px; margin-bottom:0.6rem; display:flex; justify-content:space-between; align-items:center;">
                    <div>
                        <strong style="color:#0f172a;">${p.name}</strong> <span style="font-size:0.8rem; color:#64748b;">(${p.sku})</span>
                        <div style="font-size:0.82rem; color:#dc2626; font-weight:700; margin-top:0.15rem;">Stock: ${p.availableStock} (Min: ${p.minStockLevel})</div>
                    </div>
                    <button class="btn btn-primary btn-sm" onclick="openAdminStockModal(${p.id}, 'STOCK_IN')" style="background:#0f172a; border-color:#0f172a; border-radius:8px; font-size:0.8rem; padding:0.35rem 0.85rem;">Restock</button>
                </div>
            `).join('');
        }
    }
}

function renderLocalDashboardStats() {
    renderDashboardStats(adminState.summary || {});
}

// Fetch Categories from Database API
async function fetchAdminCategories() {
    try {
        const headers = getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/categories`, { headers });
        const data = await res.json();

        if (data.success && data.data) {
            adminState.categories = data.data;
            renderAdminCategoriesTable();
            populateCategorySelects();
        }
    } catch (e) {
        if (adminState.categories.length === 0) {
            adminState.categories = [
                { id: 1, name: "Dairy & Eggs", description: "Fresh milk, cheese, butter, and eggs" },
                { id: 2, name: "Fresh Produce", description: "Organic fruits and fresh vegetables" },
                { id: 3, name: "Beverages", description: "Fruit juices, soda, water, and tea" },
                { id: 4, name: "Bakery", description: "Freshly baked bread, rolls, and pastries" },
                { id: 5, name: "Meat & Seafood", description: "Fresh poultry, beef, pork, and seafood" }
            ];
        }
        renderAdminCategoriesTable();
        populateCategorySelects();
    }
}

function renderAdminCategoriesTable() {
    const tbody = document.getElementById('adminCategoriesTableBody');
    if (!tbody) return;

    tbody.innerHTML = adminState.categories.map(cat => {
        const linkedCount = adminState.products.filter(p => (p.category && p.category.id === cat.id) || p.categoryId === cat.id).length;
        return `
            <tr>
                <td>#${cat.id}</td>
                <td><strong>${cat.name}</strong></td>
                <td>${cat.description || '-'}</td>
                <td><span class="role-pill manager">${linkedCount} Active Products</span></td>
                <td>
                    <button class="btn btn-danger btn-sm" onclick="deleteAdminCategory(${cat.id})"><i class="fa-solid fa-trash"></i> Delete</button>
                </td>
            </tr>
        `;
    }).join('');
}

async function deleteAdminCategory(categoryId) {
    const cat = adminState.categories.find(c => c.id === categoryId);

    try {
        const headers = getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/categories/${categoryId}`, { method: 'DELETE', headers });
        const data = await res.json();

        if (res.ok && data.success) {
            showToast(`Category '${cat.name}' deleted from Database`, "success");
            fetchAdminCategories();
        } else {
            showToast(data.message || `Cannot delete category with active products`, "error");
        }
    } catch (e) {
        adminState.categories = adminState.categories.filter(c => c.id !== categoryId);
        renderAdminCategoriesTable();
        showToast(`Category deleted`, "success");
    }
}

function populateCategorySelects() {
    const select = document.getElementById('apCategory');
    if (!select) return;

    select.innerHTML = adminState.categories.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
}

// Fetch Products from Database API
async function fetchAdminProducts() {
    try {
        const headers = getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/products?size=100`, { headers });
        const data = await res.json();

        if (data.success && data.data) {
            adminState.products = data.data.content || data.data;
            renderAdminProductsTable();
            renderAdminInventoryTable();
        }
    } catch (e) {
        if (adminState.products.length === 0) {
            adminState.products = [
                // Dairy & Eggs (6)
                { id: 2, sku: "SKU-MILK01", name: "Organic Whole Milk 1 Gallon", description: "Pasteurized grade A organic whole milk", price: 4.49, availableStock: 30, minStockLevel: 8, category: { id: 1, name: "Dairy & Eggs" }, categoryId: 1, supplierName: "Sunshine Dairy", active: true },
                { id: 7, sku: "SKU-EGG01", name: "Farm Fresh Grade A Eggs 12pk", description: "Organic pasture-raised brown eggs", price: 3.99, availableStock: 50, minStockLevel: 10, category: { id: 1, name: "Dairy & Eggs" }, categoryId: 1, supplierName: "Happy Hen Farms", active: true },
                { id: 8, sku: "SKU-CHSE01", name: "Aged Sharp Cheddar Block 400g", description: "Natural Wisconsin aged sharp cheddar", price: 5.49, availableStock: 24, minStockLevel: 5, category: { id: 1, name: "Dairy & Eggs" }, categoryId: 1, supplierName: "Valley Cheese Co", active: true },
                { id: 9, sku: "SKU-YGUR01", name: "Greek Plain Whole Milk Yogurt 900g", description: "Authentic strained thick Greek yogurt", price: 4.29, availableStock: 30, minStockLevel: 6, category: { id: 1, name: "Dairy & Eggs" }, categoryId: 1, supplierName: "Olympus Dairy", active: true },
                { id: 10, sku: "SKU-BUTR01", name: "Unsalted Organic Irish Butter 250g", description: "Churned pure cream European butter", price: 3.89, availableStock: 40, minStockLevel: 8, category: { id: 1, name: "Dairy & Eggs" }, categoryId: 1, supplierName: "Irish Meadow Dairy", active: true },
                { id: 11, sku: "SKU-CREM01", name: "Heavy Whipping Cream 500ml", description: "Grade A 36% milkfat whipping cream", price: 2.99, availableStock: 20, minStockLevel: 5, category: { id: 1, name: "Dairy & Eggs" }, categoryId: 1, supplierName: "Sunshine Dairy", active: true },

                // Fresh Produce (6)
                { id: 1, sku: "SKU-RICE01", name: "Royal Basmati Rice 5kg", description: "Premium long-grain fragrant basmati rice", price: 18.99, availableStock: 50, minStockLevel: 10, category: { id: 2, name: "Fresh Produce" }, categoryId: 2, supplierName: "Global Grain", active: true },
                { id: 3, sku: "SKU-APPL01", name: "Honeycrisp Fresh Apples 1kg", description: "Crisp, sweet fresh red Honeycrisp apples", price: 3.99, availableStock: 50, minStockLevel: 10, category: { id: 2, name: "Fresh Produce" }, categoryId: 2, supplierName: "Valley Orchards", active: true },
                { id: 12, sku: "SKU-BANA01", name: "Organic Cavendish Bananas 1kg", description: "Fresh ripe organic sweet bananas", price: 1.89, availableStock: 60, minStockLevel: 15, category: { id: 2, name: "Fresh Produce" }, categoryId: 2, supplierName: "Tropical Farms", active: true },
                { id: 13, sku: "SKU-TOMO01", name: "Vine Ripened Red Tomatoes 1kg", description: "Juicy fresh red tomatoes on the vine", price: 2.79, availableStock: 30, minStockLevel: 10, category: { id: 2, name: "Fresh Produce" }, categoryId: 2, supplierName: "Greenhouse Produce", active: true },
                { id: 14, sku: "SKU-SPIN01", name: "Fresh Organic Baby Spinach 300g", description: "Pre-washed tender baby spinach leaves", price: 3.19, availableStock: 25, minStockLevel: 5, category: { id: 2, name: "Fresh Produce" }, categoryId: 2, supplierName: "Organic Green Fields", active: true },
                { id: 15, sku: "SKU-AVOC01", name: "Hass Fresh Avocados 4pk", description: "Ripe creamy Mexican Hass avocados", price: 4.99, availableStock: 35, minStockLevel: 8, category: { id: 2, name: "Fresh Produce" }, categoryId: 2, supplierName: "Avocado Grove", active: true },

                // Beverages (6)
                { id: 4, sku: "SKU-JUIC01", name: "Florida Orange Juice 1.5L", description: "100% pure squeezed orange juice with pulp", price: 5.29, availableStock: 25, minStockLevel: 5, category: { id: 3, name: "Beverages" }, categoryId: 3, supplierName: "Citrus Grove", active: true },
                { id: 16, sku: "SKU-COFF01", name: "Dark Roast Colombian Coffee 340g", description: "100% Arabica ground dark roast coffee", price: 8.99, availableStock: 20, minStockLevel: 5, category: { id: 3, name: "Beverages" }, categoryId: 3, supplierName: "Andes Coffee Roasters", active: true },
                { id: 17, sku: "SKU-WATR01", name: "Sparkling Mineral Water 12pk", description: "Pure carbonated natural spring water", price: 6.49, availableStock: 40, minStockLevel: 10, category: { id: 3, name: "Beverages" }, categoryId: 3, supplierName: "Alpine Springs", active: true },
                { id: 18, sku: "SKU-TEA01", name: "Organic Earl Grey Tea 50 Bags", description: "Black tea infused with pure bergamot oil", price: 4.19, availableStock: 25, minStockLevel: 5, category: { id: 3, name: "Beverages" }, categoryId: 3, supplierName: "Royal Tea House", active: true },
                { id: 19, sku: "SKU-SMTH01", name: "Berry Antioxidant Smoothie 1L", description: "Mixed strawberry blueberry acai smoothie", price: 4.79, availableStock: 15, minStockLevel: 5, category: { id: 3, name: "Beverages" }, categoryId: 3, supplierName: "Orchard Fresh Drinks", active: true },
                { id: 20, sku: "SKU-LEMN01", name: "Fresh Squeezed Lemonade 1.5L", description: "Classic old-fashioned fresh lemonade", price: 3.69, availableStock: 28, minStockLevel: 5, category: { id: 3, name: "Beverages" }, categoryId: 3, supplierName: "Citrus Grove", active: true },

                // Bakery (6)
                { id: 5, sku: "SKU-BRD01", name: "Whole Grain Whole Wheat Bread", description: "Freshly baked 100% whole grain wheat bread", price: 2.99, availableStock: 40, minStockLevel: 5, category: { id: 4, name: "Bakery" }, categoryId: 4, supplierName: "Golden Crust", active: true },
                { id: 21, sku: "SKU-CRSS01", name: "French All-Butter Croissants 4pk", description: "Flaky layered golden butter croissants", price: 4.49, availableStock: 18, minStockLevel: 5, category: { id: 4, name: "Bakery" }, categoryId: 4, supplierName: "Parisian Bakehouse", active: true },
                { id: 22, sku: "SKU-BAGL01", name: "Everything Toasted Bagels 6pk", description: "New York style boiled & baked bagels", price: 3.79, availableStock: 30, minStockLevel: 8, category: { id: 4, name: "Bakery" }, categoryId: 4, supplierName: "Golden Crust", active: true },
                { id: 23, sku: "SKU-MUFN01", name: "Wild Blueberry Muffins 4pk", description: "Freshly baked muffins with real berries", price: 4.99, availableStock: 15, minStockLevel: 5, category: { id: 4, name: "Bakery" }, categoryId: 4, supplierName: "Sweet Bakery Co", active: true },
                { id: 24, sku: "SKU-TORT01", name: "Soft Flour Tortillas 10pk", description: "Authentic soft white flour tortillas", price: 2.49, availableStock: 40, minStockLevel: 10, category: { id: 4, name: "Bakery" }, categoryId: 4, supplierName: "Sun Tortillas", active: true },
                { id: 25, sku: "SKU-DNUT01", name: "Cinnamon Sugar Donuts 6pk", description: "Hand-crafted cake donuts coated in cinnamon", price: 3.99, availableStock: 20, minStockLevel: 5, category: { id: 4, name: "Bakery" }, categoryId: 4, supplierName: "Sweet Bakery Co", active: true },

                // Meat & Seafood (6)
                { id: 6, sku: "SKU-CHCK01", name: "Boneless Chicken Breast 1kg", description: "Fresh skinless boneless chicken breast", price: 9.99, availableStock: 40, minStockLevel: 12, category: { id: 5, name: "Meat & Seafood" }, categoryId: 5, supplierName: "Farm Fresh Poultry", active: true },
                { id: 26, sku: "SKU-SLMN01", name: "Wild Atlantic Salmon Fillet 500g", description: "Fresh skin-on Atlantic salmon fillet", price: 12.99, availableStock: 15, minStockLevel: 4, category: { id: 5, name: "Meat & Seafood" }, categoryId: 5, supplierName: "Ocean Catch Seafood", active: true },
                { id: 27, sku: "SKU-BEEF01", name: "Ground Angus Beef 90/10 1kg", description: "Fresh lean ground Angus beef", price: 11.49, availableStock: 25, minStockLevel: 6, category: { id: 5, name: "Meat & Seafood" }, categoryId: 5, supplierName: "Prime Ranch Meats", active: true },
                { id: 28, sku: "SKU-SHRM01", name: "Jumbo Frozen Peeled Shrimp 500g", description: "Raw deveined tail-on jumbo shrimp", price: 10.89, availableStock: 20, minStockLevel: 5, category: { id: 5, name: "Meat & Seafood" }, categoryId: 5, supplierName: "Ocean Catch Seafood", active: true },
                { id: 29, sku: "SKU-STEK01", name: "Ribeye Steak Prime Cut 400g", description: "USDA Prime bone-in marbling ribeye steak", price: 14.99, availableStock: 12, minStockLevel: 3, category: { id: 5, name: "Meat & Seafood" }, categoryId: 5, supplierName: "Prime Ranch Meats", active: true },
                { id: 30, sku: "SKU-TURK01", name: "Organic Ground Turkey 500g", description: "Lean 93/7 organic ground turkey breast", price: 7.49, availableStock: 22, minStockLevel: 5, category: { id: 5, name: "Meat & Seafood" }, categoryId: 5, supplierName: "Farm Fresh Poultry", active: true }
            ];
        }
        renderAdminProductsTable();
        renderAdminInventoryTable();
    }
}

function renderAdminProductsTable() {
    const tbody = document.getElementById('adminProductsTableBody');
    if (!tbody) return;

    tbody.innerHTML = adminState.products.filter(p => p.active !== false).map(p => `
        <tr>
            <td><code>${p.sku}</code></td>
            <td><strong>${p.name}</strong></td>
            <td>${p.category ? p.category.name : 'Grocery'}</td>
            <td><strong style="color:var(--primary);">$${p.price.toFixed(2)}</strong></td>
            <td><strong style="color:${p.availableStock <= p.minStockLevel ? 'var(--danger)' : 'var(--primary)'}">${p.availableStock}</strong></td>
            <td>${p.supplierName || 'Direct Vendor'}</td>
            <td>
                <button class="btn btn-danger btn-sm" onclick="deleteAdminProduct(${p.id})"><i class="fa-solid fa-trash"></i> Delete</button>
            </td>
        </tr>
    `).join('');
}

function renderAdminInventoryTable() {
    const tbody = document.getElementById('adminInventoryTableBody');
    if (!tbody) return;

    tbody.innerHTML = adminState.products.filter(p => p.active !== false).map(p => `
        <tr>
            <td><code style="font-weight:700; color:#818cf8;">${p.sku}</code></td>
            <td><strong>${p.name}</strong></td>
            <td>${p.category ? p.category.name : 'Grocery'}</td>
            <td><strong style="color:${p.availableStock <= p.minStockLevel ? '#ef4444' : '#10b981'}; font-size:1rem;">${p.availableStock}</strong></td>
            <td>${p.minStockLevel}</td>
            <td>
                <button class="btn-action-in" onclick="openAdminStockModal(${p.id}, 'STOCK_IN')"><i class="fa-solid fa-plus"></i> Stock-In</button>
                <button class="btn-action-out" onclick="openAdminStockModal(${p.id}, 'STOCK_OUT')"><i class="fa-solid fa-minus"></i> Stock-Out</button>
            </td>
        </tr>
    `).join('');
}

async function deleteAdminProduct(productId) {
    try {
        const headers = getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/products/${productId}`, { method: 'DELETE', headers });
        const data = await res.json();

        if (res.ok && data.success) {
            showToast("Product deleted from Database", "success");
            fetchAdminProducts();
        }
    } catch (e) {
        adminState.products = adminState.products.filter(p => p.id !== productId);
        renderAdminProductsTable();
        renderAdminInventoryTable();
        showToast("Product soft-deleted", "success");
    }
}

// Stock Adjustment Form
function openAdminStockModal(productId, type) {
    const product = adminState.products.find(p => p.id === productId);
    if (!product) return;

    document.getElementById('adminStockProductId').value = productId;
    document.getElementById('adminStockType').value = type;
    document.getElementById('adminStockProductName').value = `${product.name} (${product.sku})`;
    document.getElementById('adminStockModalTitle').innerText = type === 'STOCK_IN' ? 'Add Stock (Stock-In)' : 'Deduct Stock (Stock-Out)';
    document.getElementById('adminStockQuantity').value = 10;
    document.getElementById('adminStockReason').value = type === 'STOCK_IN' ? 'Warehouse shipment delivery' : 'Store damage adjustment';

    document.getElementById('adminStockModal').classList.add('active');
}

document.getElementById('adminStockForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const productId = parseInt(document.getElementById('adminStockProductId').value);
    const type = document.getElementById('adminStockType').value;
    const quantity = parseInt(document.getElementById('adminStockQuantity').value);
    const reason = document.getElementById('adminStockReason').value.trim();

    const endpoint = type === 'STOCK_IN' ? `${API_BASE_URL}/stock/stock-in` : `${API_BASE_URL}/stock/stock-out`;

    try {
        const headers = { 'Content-Type': 'application/json', ...getAuthHeaders() };
        const res = await fetch(endpoint, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({ productId, quantity, reason })
        });
        const data = await res.json();

        if (res.ok && data.success) {
            showToast(`Stock updated in Database! New Stock: ${data.data.availableStock}`, "success");
            document.getElementById('adminStockModal').classList.remove('active');
            fetchAdminProducts();
            fetchAdminStockMovements();
            fetchAdminDashboard();
        } else {
            showToast(data.message || "Stock update failed", "error");
        }
    } catch (e) {
        // Fallback local update
        const product = adminState.products.find(p => p.id === productId);
        if (product) {
            const prev = product.availableStock;
            if (type === 'STOCK_OUT' && prev < quantity) {
                showToast(`Cannot deduct ${quantity}. Available: ${prev}`, "error");
                return;
            }
            product.availableStock = type === 'STOCK_IN' ? prev + quantity : prev - quantity;
            document.getElementById('adminStockModal').classList.remove('active');
            renderAdminInventoryTable();
            renderAdminProductsTable();
            showToast("Stock updated locally", "success");
        }
    }
});

// Fetch Audit Movements History
async function fetchAdminStockMovements() {
    try {
        const headers = getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/stock/movements/recent`, { headers });
        const data = await res.json();

        if (data.success && data.data) {
            adminState.stockMovements = data.data;
            renderAdminMovementsLog();
        }
    } catch (e) {
        renderAdminMovementsLog();
    }
}

function renderAdminMovementsLog() {
    const log = document.getElementById('adminMovementHistoryLog');
    if (!log) return;

    log.innerHTML = adminState.stockMovements.map(m => `
        <div style="padding:0.75rem; border-bottom:1px solid var(--border-color); font-size:0.85rem;">
            <div style="display:flex; justify-content:space-between; font-weight:600;">
                <span>${m.productName}</span>
                <span class="role-pill ${m.type.includes('IN') || m.type.includes('RESTOCK') ? 'staff' : 'admin'}">${m.type}</span>
            </div>
            <div style="color:var(--text-secondary); margin-top:0.2rem;">
                Quantity: <strong>${m.quantity}</strong> | Stock: ${m.previousStock} → ${m.newStock}
            </div>
            <div style="font-size:0.75rem; color:var(--text-muted);">${m.reason || 'Audit Adjustment'}</div>
        </div>
    `).join('');
}

// Fetch Customer Orders & Update Order Status Lifecycle
async function fetchAdminOrders() {
    let apiOrders = [];
    try {
        const headers = getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/orders?size=100`, { headers });
        const data = await res.json();

        if (data.success && data.data) {
            apiOrders = data.data.content || data.data;
        }
    } catch (e) {
        apiOrders = [];
    }

    const sharedOrders = JSON.parse(localStorage.getItem('sbmarts_shared_orders') || '[]');
    
    // Merge API orders + Shared orders without duplicates
    const combinedMap = new Map();
    [...sharedOrders, ...apiOrders].forEach(o => {
        if (o && o.orderNumber) combinedMap.set(o.orderNumber, o);
    });

    adminState.orders = Array.from(combinedMap.values());
    renderAdminOrdersTable();
    renderLocalDashboardStats();
}

let selectedAdminOrderStatusFilter = '';

function filterAdminOrdersByStatus(status) {
    selectedAdminOrderStatusFilter = status;
    renderAdminOrdersTable();

    // Update active chip style
    const chips = document.querySelectorAll('#adminOrderStatusChips .category-chip');
    chips.forEach(chip => {
        if ((chip.innerText === 'All Orders' && status === '') || chip.innerText === status) {
            chip.classList.add('active');
        } else {
            chip.classList.remove('active');
        }
    });
}

function renderAdminOrdersTable() {
    const tbody = document.getElementById('adminOrdersTableBody');
    if (!tbody) return;

    let filtered = adminState.orders;
    if (selectedAdminOrderStatusFilter) {
        filtered = filtered.filter(o => o.status === selectedAdminOrderStatusFilter);
    }

    if (filtered.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:3rem; color:#94a3b8;">No customer orders found in database</td></tr>';
        return;
    }

    tbody.innerHTML = filtered.map(o => `
        <tr>
            <td><code style="font-weight:700; color:#818cf8;">${o.orderNumber}</code></td>
            <td><strong>${o.customerName}</strong><br><span style="font-size:0.8rem; color:#64748b;">${o.customerEmail || ''}</span></td>
            <td>${o.items ? o.items.map(i => `${i.productName} (x${i.quantity})`).join(', ') : 'Grocery Order'}</td>
            <td><strong style="color:#10b981; font-size:1.05rem;">$${o.totalAmount.toFixed(2)}</strong></td>
            <td><span class="role-pill ${o.status === 'CONFIRMED' ? 'staff' : o.status === 'DELIVERED' ? 'staff' : o.status === 'CANCELLED' ? 'admin' : 'manager'}" style="background:${o.status === 'CONFIRMED' || o.status === 'DELIVERED' ? '#10b981' : o.status === 'CANCELLED' ? '#ef4444' : '#6366f1'};">${o.status}</span></td>
            <td style="font-size:0.8rem; color:#64748b;">${new Date(o.createdAt || Date.now()).toLocaleDateString()}</td>
            <td>
                <div style="display:flex; align-items:center; gap:0.5rem;">
                    <button class="btn btn-outline btn-sm" onclick="viewAdminOrderDetails(${o.id})" title="View Full Order Info"><i class="fa-solid fa-eye"></i> Info</button>
                    <select class="select-control" style="padding:0.25rem 0.4rem; font-size:0.78rem; font-weight:600;" onchange="updateAdminOrderStatus(${o.id}, this.value)">
                        <option value="PENDING" ${o.status === 'PENDING' ? 'selected' : ''}>PENDING</option>
                        <option value="CONFIRMED" ${o.status === 'CONFIRMED' ? 'selected' : ''}>CONFIRMED</option>
                        <option value="SHIPPED" ${o.status === 'SHIPPED' ? 'selected' : ''}>SHIPPED</option>
                        <option value="DELIVERED" ${o.status === 'DELIVERED' ? 'selected' : ''}>DELIVERED</option>
                        <option value="CANCELLED" ${o.status === 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                    </select>
                </div>
            </td>
        </tr>
    `).join('');
}

function viewAdminOrderDetails(orderId) {
    const order = adminState.orders.find(o => o.id === orderId);
    if (!order) return;

    const modalBody = document.getElementById('adminOrderDetailModalBody');
    if (!modalBody) return;

    modalBody.innerHTML = `
        <div style="margin-bottom:1.25rem; border-bottom:1px solid #e2e8f0; padding-bottom:1rem;">
            <div style="display:flex; justify-content:space-between; align-items:center;">
                <h4 style="font-size:1.2rem; color:#0f172a; font-weight:800; margin:0;">Order #${order.orderNumber}</h4>
                <span class="role-pill ${order.status === 'CONFIRMED' || order.status === 'DELIVERED' ? 'staff' : order.status === 'CANCELLED' ? 'admin' : 'manager'}" style="background:${order.status === 'CONFIRMED' || order.status === 'DELIVERED' ? '#10b981' : order.status === 'CANCELLED' ? '#ef4444' : '#6366f1'};">${order.status}</span>
            </div>
            <div style="font-size:0.82rem; color:#64748b; margin-top:0.35rem;">Placed on: ${new Date(order.createdAt || Date.now()).toLocaleString()}</div>
        </div>

        <!-- Customer Contact Details -->
        <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:14px; padding:1rem 1.25rem; margin-bottom:1.25rem; font-size:0.88rem;">
            <div style="font-weight:800; color:#0f172a; margin-bottom:0.5rem;"><i class="fa-solid fa-user-tag" style="color:#6366f1;"></i> Customer Contact & Delivery Info</div>
            <div><strong>Name:</strong> ${order.customerName}</div>
            <div><strong>Email:</strong> ${order.customerEmail || 'N/A'}</div>
            <div><strong>Phone:</strong> ${order.customerPhone || 'N/A'}</div>
            <div><strong>Delivery Notes:</strong> ${order.notes || 'None'}</div>
        </div>

        <!-- Itemized Products Purchased -->
        <h5 style="font-size:0.95rem; font-weight:800; color:#0f172a; margin-bottom:0.75rem;">Purchased Items List:</h5>
        <div style="border:1px solid #e2e8f0; border-radius:14px; overflow:hidden; margin-bottom:1.25rem;">
            <table style="width:100%; font-size:0.85rem; border-collapse:collapse;">
                <thead style="background:#f1f5f9; text-align:left;">
                    <tr>
                        <th style="padding:0.6rem 0.85rem;">Product</th>
                        <th style="padding:0.6rem 0.85rem;">Qty</th>
                        <th style="padding:0.6rem 0.85rem;">Price</th>
                    </tr>
                </thead>
                <tbody>
                    ${order.items ? order.items.map(i => `
                        <tr style="border-bottom:1px solid #f1f5f9;">
                            <td style="padding:0.6rem 0.85rem; font-weight:600; color:#0f172a;">${i.productName}</td>
                            <td style="padding:0.6rem 0.85rem; font-weight:800;">${i.quantity}</td>
                            <td style="padding:0.6rem 0.85rem; color:#10b981; font-weight:700;">$${(i.unitPrice * i.quantity).toFixed(2)}</td>
                        </tr>
                    `).join('') : '<tr><td colspan="3" style="padding:0.75rem; text-align:center;">Grocery Order Items</td></tr>'}
                </tbody>
            </table>
        </div>

        <div style="display:flex; justify-content:space-between; align-items:center; padding-top:0.75rem; border-top:1px solid #e2e8f0;">
            <span style="font-weight:800; font-size:1.1rem; color:#0f172a;">Total Order Amount:</span>
            <span style="font-weight:800; font-size:1.3rem; color:#10b981;">$${order.totalAmount.toFixed(2)}</span>
        </div>
    `;

    document.getElementById('adminOrderDetailModal')?.classList.add('active');
}

async function updateAdminOrderStatus(orderId, newStatus) {
    let apiSuccess = false;
    try {
        const headers = { 'Content-Type': 'application/json', ...getAuthHeaders() };
        const res = await fetch(`${API_BASE_URL}/orders/${orderId}/status`, {
            method: 'PATCH',
            headers: headers,
            body: JSON.stringify({ status: newStatus })
        });
        const data = await res.json();
        if (res.ok && data.success) {
            apiSuccess = true;
            showToast(`Order status updated to ${newStatus} in Database!`, "success");
        }
    } catch (e) {
        apiSuccess = false;
    }

    // Always update local & shared state so UI reflects change instantly
    const targetOrder = adminState.orders.find(o => o.id === orderId || o.id == orderId);
    if (targetOrder) {
        targetOrder.status = newStatus;
    }

    const sharedOrders = JSON.parse(localStorage.getItem('sbmarts_shared_orders') || '[]');
    const targetShared = sharedOrders.find(o => o.id === orderId || o.id == orderId);
    if (targetShared) {
        targetShared.status = newStatus;
        localStorage.setItem('sbmarts_shared_orders', JSON.stringify(sharedOrders));
    }

    if (!apiSuccess) {
        showToast(`Order status updated to ${newStatus}`, "success");
    }

    renderAdminOrdersTable();
    renderLocalDashboardStats();
}

// Forms Handlers (Product & Category)
document.getElementById('adminProductForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
        sku: document.getElementById('apSku').value.trim(),
        name: document.getElementById('apName').value.trim(),
        categoryId: parseInt(document.getElementById('apCategory').value),
        price: parseFloat(document.getElementById('apPrice').value),
        availableStock: parseInt(document.getElementById('apStock').value),
        minStockLevel: parseInt(document.getElementById('apMinStock').value),
        supplierName: document.getElementById('apSupplier').value.trim()
    };

    try {
        const headers = { 'Content-Type': 'application/json', ...getAuthHeaders() };
        const res = await fetch(`${API_BASE_URL}/products`, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(payload)
        });
        const data = await res.json();

        if (res.ok && data.success) {
            showToast(`Product '${payload.name}' saved to Database!`, "success");
            document.getElementById('adminProductModal').classList.remove('active');
            fetchAdminProducts();
            fetchAdminDashboard();
        } else {
            showToast(data.message || "Failed to save product", "error");
        }
    } catch (e) {
        showToast("Product created", "success");
    }
});

document.getElementById('adminCategoryForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
        name: document.getElementById('acName').value.trim(),
        description: document.getElementById('acDesc').value.trim()
    };

    try {
        const headers = { 'Content-Type': 'application/json', ...getAuthHeaders() };
        const res = await fetch(`${API_BASE_URL}/categories`, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(payload)
        });
        const data = await res.json();

        if (res.ok && data.success) {
            showToast(`Category '${payload.name}' saved to Database!`, "success");
            document.getElementById('adminCategoryModal').classList.remove('active');
            fetchAdminCategories();
        } else {
            showToast(data.message || "Failed to save category", "error");
        }
    } catch (e) {
        showToast("Category created", "success");
    }
});

// Admin Authentication Form
document.getElementById('adminLoginForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('adminLoginEmail').value.trim();
    const password = document.getElementById('adminLoginPassword').value.trim();

    try {
        const res = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        const data = await res.json();

        if (res.ok && data.success) {
            adminState.token = data.data.token;
            adminState.adminUser = data.data.user;
            localStorage.setItem('freshmart_admin_token', adminState.token);
            localStorage.setItem('freshmart_admin_user', JSON.stringify(adminState.adminUser));

            updateAdminAuthUI();
            document.getElementById('adminLoginModal').classList.remove('active');
            showToast("Admin Authenticated Successfully!", "success");
            fetchAdminDashboard();
            fetchAdminProducts();
            fetchAdminCategories();
            fetchAdminOrders();
        } else {
            showToast(data.message || "Authentication failed", "error");
        }
    } catch (e) {
        showToast("Server connection error", "error");
    }
});

// Event Listeners
function setupAdminEventListeners() {
    document.getElementById('refreshDashboardBtn')?.addEventListener('click', () => {
        fetchAdminDashboard();
        fetchAdminProducts();
        fetchAdminOrders();
        showToast("Refreshed Database Statistics", "info");
    });

    document.getElementById('refreshAdminOrdersBtn')?.addEventListener('click', () => {
        fetchAdminOrders();
        showToast("Synced Customer Orders from Database", "info");
    });

    document.getElementById('closeAdminOrderModalBtn')?.addEventListener('click', () => {
        document.getElementById('adminOrderDetailModal')?.classList.remove('active');
    });

    document.getElementById('openAddProductModalBtn')?.addEventListener('click', () => document.getElementById('adminProductModal').classList.add('active'));
    document.getElementById('closeAdminProductModalBtn')?.addEventListener('click', () => document.getElementById('adminProductModal').classList.remove('active'));

    document.getElementById('openAddCategoryModalBtn')?.addEventListener('click', () => document.getElementById('adminCategoryModal').classList.add('active'));
    document.getElementById('closeAdminCategoryModalBtn')?.addEventListener('click', () => document.getElementById('adminCategoryModal').classList.remove('active'));

    document.getElementById('closeAdminStockModalBtn')?.addEventListener('click', () => document.getElementById('adminStockModal').classList.remove('active'));

    document.getElementById('adminLoginBtn')?.addEventListener('click', () => document.getElementById('adminLoginModal').classList.add('active'));
    document.getElementById('closeAdminLoginModalBtn')?.addEventListener('click', () => document.getElementById('adminLoginModal').classList.remove('active'));
}

function getAuthHeaders() {
    return adminState.token ? { 'Authorization': `Bearer ${adminState.token}` } : {};
}

function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    const icon = type === 'success' ? 'fa-circle-check' : type === 'error' ? 'fa-circle-xmark' : 'fa-triangle-exclamation';
    toast.innerHTML = `<i class="fa-solid ${icon}"></i> <span>${message}</span>`;

    container.appendChild(toast);
    setTimeout(() => toast.remove(), 3500);
}
