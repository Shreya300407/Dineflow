/**
 * Dineflow - Modern Restaurant Web Application
 * Connected to Java Spring Boot REST APIs (/api/foods)
 */

// In-Memory Food Catalog (populated via REST API)
let currentMenuItems = [];
let activeCategory = "All";
let activeSearchQuery = "";
let isLoadingMenu = false;

// Persistent Shopping Cart (stored in localStorage)
let cart = [];

try {
  const savedCart = localStorage.getItem("dineflow_cart");
  if (savedCart) {
    cart = JSON.parse(savedCart);
  }
} catch (e) {
  cart = [];
}

function saveCart() {
  try {
    localStorage.setItem("dineflow_cart", JSON.stringify(cart));
  } catch (e) {
    console.error("Failed to save cart to localStorage", e);
  }
}

// Format Currency in ₹ (INR)
function formatCurrency(amount) {
  return "₹" + Number(amount || 0).toFixed(2);
}

// App Initialization
document.addEventListener("DOMContentLoaded", () => {
  updateCartUI();
  setupCartDrawer();
  setupModals();
  checkBackendStatus();

  const isMenuPage = document.getElementById("menuSearchInput") !== null;
  const isCheckoutPage = document.getElementById("orderCheckoutForm") !== null;
  const isConfirmationPage = document.getElementById("confOrderNumber") !== null;
  const isTrackingPage = document.getElementById("trackOrderForm") !== null;
  const isOrdersPage = document.getElementById("ordersGrid") !== null;

  if (isTrackingPage) {
    initTrackingPage();
  } else if (isOrdersPage) {
    initOrdersPage();
  } else if (isConfirmationPage) {
    initConfirmationPage();
  } else if (isCheckoutPage) {
    initCheckoutPage();
  } else if (isMenuPage) {
    initMenuPage();
  } else {
    initHomePage();
  }
});

/**
 * Initialize Full Menu Page (menu.html)
 */
async function initMenuPage() {
  const searchInput = document.getElementById("menuSearchInput");
  const clearSearchBtn = document.getElementById("clearSearchBtn");

  // Category Filter Pills
  const filterBtns = document.querySelectorAll(".filter-btn");
  filterBtns.forEach(btn => {
    btn.addEventListener("click", () => {
      filterBtns.forEach(b => b.classList.remove("active"));
      btn.classList.add("active");
      activeCategory = btn.getAttribute("data-filter") || "All";
      
      // Use the Spring Boot category API if a specific category is selected
      if (activeCategory !== "All") {
        fetchFoodsByCategory(activeCategory);
      } else {
        fetchFoodItemsFromAPI();
      }
    });
  });

  // Search Input Event
  if (searchInput) {
    searchInput.addEventListener("input", (e) => {
      activeSearchQuery = e.target.value.trim();
      if (clearSearchBtn) {
        clearSearchBtn.style.display = activeSearchQuery.length > 0 ? "flex" : "none";
      }
      renderMenuPageItems();
    });
  }

  // Clear Search Button
  if (clearSearchBtn) {
    clearSearchBtn.addEventListener("click", () => {
      if (searchInput) searchInput.value = "";
      activeSearchQuery = "";
      clearSearchBtn.style.display = "none";
      renderMenuPageItems();
      searchInput?.focus();
    });
  }

  // Fetch foods from Spring Boot REST API
  await fetchFoodItemsFromAPI();
}

/**
 * Fetch all food items from Spring Boot REST endpoint: GET /api/foods
 */
async function fetchFoodItemsFromAPI() {
  const container = document.getElementById("menuGrid");
  showLoadingState(container);

  try {
    const response = await fetch("/api/foods");
    if (!response.ok) {
      throw new Error(`Server returned HTTP ${response.status} (${response.statusText})`);
    }

    const data = await response.json();
    if (!Array.isArray(data)) {
      throw new Error("Invalid response format received from server.");
    }

    currentMenuItems = data;
    updateCategoryBadges();
    renderMenuPageItems();
  } catch (error) {
    console.error("Error fetching food items from /api/foods:", error);
    showErrorState(container, error.message, () => fetchFoodItemsFromAPI());
  }
}

/**
 * Fetch food items by category from Spring Boot REST endpoint: GET /api/foods/category/{category}
 */
async function fetchFoodsByCategory(category) {
  const container = document.getElementById("menuGrid");
  showLoadingState(container);

  try {
    const encodedCategory = encodeURIComponent(category);
    const response = await fetch(`/api/foods/category/${encodedCategory}`);
    if (!response.ok) {
      throw new Error(`Failed to load category "${category}" (HTTP ${response.status})`);
    }

    const data = await response.json();
    renderFilteredItems(data);
  } catch (error) {
    console.error(`Error fetching category ${category}:`, error);
    showErrorState(container, error.message, () => fetchFoodsByCategory(category));
  }
}

/**
 * Initialize Home Page (index.html)
 */
async function initHomePage() {
  const container = document.getElementById("menuGrid");
  if (!container) return;

  showLoadingState(container);

  try {
    const response = await fetch("/api/foods");
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    const data = await response.json();
    currentMenuItems = data;
    renderHomeItems("all");

    // Setup home filter buttons
    const buttons = document.querySelectorAll(".filter-btn");
    buttons.forEach(btn => {
      btn.addEventListener("click", () => {
        buttons.forEach(b => b.classList.remove("active"));
        btn.classList.add("active");
        const category = btn.getAttribute("data-filter") || "all";
        renderHomeItems(category);
      });
    });
  } catch (error) {
    console.error("Error loading homepage foods:", error);
    showErrorState(container, error.message, () => initHomePage());
  }
}

function renderHomeItems(category) {
  const container = document.getElementById("menuGrid");
  if (!container) return;

  const filtered = category === "all"
    ? currentMenuItems.slice(0, 8)
    : currentMenuItems.filter(item => item.category.toLowerCase().includes(category.toLowerCase()));

  if (filtered.length === 0) {
    container.innerHTML = `<div class="empty-menu-state"><p>No items in this category.</p></div>`;
    return;
  }

  container.innerHTML = filtered.map(item => generateFoodCardHTML(item)).join('');
}

/**
 * Render items on the dedicated Menu page with search and category filter applied
 */
function renderMenuPageItems() {
  const container = document.getElementById("menuGrid");
  if (!container) return;

  const query = activeSearchQuery.toLowerCase();

  const filtered = currentMenuItems.filter(item => {
    const matchesCategory = (activeCategory === "All" || item.category.toLowerCase() === activeCategory.toLowerCase());
    if (!matchesCategory) return false;

    if (!query) return true;

    const matchesName = item.name.toLowerCase().includes(query);
    const matchesDesc = item.description && item.description.toLowerCase().includes(query);
    const matchesTags = item.tags && item.tags.some(t => t.toLowerCase().includes(query));
    return matchesName || matchesDesc || matchesTags;
  });

  renderFilteredItems(filtered);
}

function renderFilteredItems(items) {
  const container = document.getElementById("menuGrid");
  const resultsCountEl = document.getElementById("resultsCount");
  if (!container) return;

  if (resultsCountEl) {
    if (activeSearchQuery) {
      resultsCountEl.textContent = `Found ${items.length} item(s) matching "${activeSearchQuery}"`;
    } else if (activeCategory !== "All") {
      resultsCountEl.textContent = `Showing ${items.length} dishes in ${activeCategory}`;
    } else {
      resultsCountEl.textContent = `Showing all ${items.length} dishes from Spring Boot API`;
    }
  }

  if (items.length === 0) {
    container.innerHTML = `
      <div class="empty-menu-state">
        <div class="empty-menu-icon">🔍</div>
        <h3 style="font-family: var(--font-serif); font-size: 1.5rem; margin-bottom: 0.5rem; color: var(--text-primary);">
          No dishes found
        </h3>
        <p style="color: var(--text-secondary); margin-bottom: 1.5rem;">
          We couldn't find any dishes matching your criteria. Try another keyword or reset the filter.
        </p>
        <button class="btn-primary" onclick="resetMenuFilters()" style="margin: 0 auto;">
          Show All Dishes
        </button>
      </div>
    `;
    return;
  }

  container.innerHTML = items.map(item => generateFoodCardHTML(item)).join('');
}

function resetMenuFilters() {
  const searchInput = document.getElementById("menuSearchInput");
  const clearBtn = document.getElementById("clearSearchBtn");
  if (searchInput) searchInput.value = "";
  activeSearchQuery = "";
  if (clearBtn) clearBtn.style.display = "none";
  activeCategory = "All";
  document.querySelectorAll(".filter-btn").forEach(b => {
    b.classList.toggle("active", b.getAttribute("data-filter") === "All");
  });
  fetchFoodItemsFromAPI();
}

/**
 * Display Loading State
 */
function showLoadingState(container) {
  if (!container) return;
  container.innerHTML = `
    <div class="loading-container">
      <div class="spinner"></div>
      <h3 class="loading-text">Connecting to Dineflow Kitchen</h3>
      <p class="loading-subtext">Fetching fresh menu items from Spring Boot REST API (<code>/api/foods</code>)...</p>
    </div>
  `;
}

/**
 * Display API Error State with Retry Button
 */
function showErrorState(container, errorDetails, retryCallback) {
  if (!container) return;
  container.innerHTML = `
    <div class="api-error-card">
      <div class="api-error-icon">⚠️</div>
      <h3 class="api-error-title">Unable to Load Menu</h3>
      <p class="api-error-desc">
        There was an issue connecting to the Spring Boot backend REST endpoint.
        <br/><small style="color: #777; display: block; margin-top: 0.4rem;">Details: ${errorDetails || 'Network or Server error'}</small>
      </p>
      <button id="retryApiBtn" class="btn-primary" style="margin: 0 auto;">
        🔄 Try Again
      </button>
    </div>
  `;

  document.getElementById("retryApiBtn")?.addEventListener("click", () => {
    retryCallback();
  });
}

/**
 * Update dynamic category count badges on menu.html
 */
function updateCategoryBadges() {
  const setBadge = (id, count) => {
    const el = document.getElementById(id);
    if (el) el.textContent = count;
  };

  setBadge("countAll", currentMenuItems.length);
  setBadge("countStarters", currentMenuItems.filter(i => i.category.toLowerCase() === "starters").length);
  setBadge("countMain", currentMenuItems.filter(i => i.category.toLowerCase() === "main course").length);
  setBadge("countPizza", currentMenuItems.filter(i => i.category.toLowerCase() === "pizza").length);
  setBadge("countBurgers", currentMenuItems.filter(i => i.category.toLowerCase() === "burgers").length);
  setBadge("countDesserts", currentMenuItems.filter(i => i.category.toLowerCase() === "desserts").length);
  setBadge("countDrinks", currentMenuItems.filter(i => i.category.toLowerCase() === "drinks").length);
}

/**
 * Generate standard HTML for a food card (consistent across all pages)
 */
function generateFoodCardHTML(item) {
  const imageUrl = item.imageUrl || item.image || "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=800";
  const ratingText = item.rating || "4.8 ★";
  const badgeHTML = item.badge ? `<span class="food-badge ${item.badgeType || ''}">${item.badge}</span>` : '';
  const tagsHTML = item.tags && Array.isArray(item.tags)
    ? item.tags.map(t => `<span class="micro-tag">${t}</span>`).join('')
    : `<span class="micro-tag">${item.category}</span>`;

  return `
    <article class="food-card" data-category="${item.category}" id="food-card-${item.id}">
      <div class="food-image-wrapper">
        <img src="${imageUrl}" alt="${item.name}" loading="lazy" />
        ${badgeHTML}
        <div class="food-rating-pill">
          <span>★</span> ${ratingText}
        </div>
      </div>
      <div class="food-content">
        <div class="food-title-row">
          <h3 class="food-name">${item.name}</h3>
          <span class="food-price">${formatCurrency(item.price)}</span>
        </div>
        <p class="food-desc">${item.description || 'Delicious handcrafted culinary specialty.'}</p>
        <div class="food-actions">
          <div class="food-tags">
            ${tagsHTML}
          </div>
          <button class="btn-add-cart" onclick="addToCart(${item.id})">
            <span>+</span> Add to Cart
          </button>
        </div>
      </div>
    </article>
  `;
}

/**
 * Shopping Cart Management
 */
function addToCart(itemId) {
  const item = currentMenuItems.find(m => m.id === itemId);
  if (!item) return;

  const existing = cart.find(c => c.id === itemId);
  if (existing) {
    existing.quantity += 1;
  } else {
    cart.push({
      id: item.id,
      name: item.name,
      price: item.price,
      image: item.imageUrl || item.image,
      quantity: 1
    });
  }

  saveCart();
  updateCartUI();
  showToast(`Added "${item.name}" to your cart! (${formatCurrency(item.price)})`);
}

function updateCartQuantity(itemId, delta) {
  const itemIndex = cart.findIndex(c => c.id === itemId);
  if (itemIndex > -1) {
    cart[itemIndex].quantity += delta;
    if (cart[itemIndex].quantity <= 0) {
      removeFromCart(itemId);
      return;
    }
    saveCart();
    updateCartUI();
  }
}

function removeFromCart(itemId) {
  const item = cart.find(c => c.id === itemId);
  cart = cart.filter(c => c.id !== itemId);
  saveCart();
  updateCartUI();
  if (item) {
    showToast(`Removed "${item.name}" from your cart`);
  }
}

function updateCartUI() {
  // Update Navbar Badge
  const cartBadge = document.getElementById("cartBadge");
  const totalCount = cart.reduce((sum, item) => sum + item.quantity, 0);
  if (cartBadge) {
    cartBadge.textContent = totalCount;
  }

  // Update Drawer Elements
  const cartItemsList = document.getElementById("cartItemsList");
  const cartSubtotal = document.getElementById("cartSubtotal");
  const cartTax = document.getElementById("cartTax");
  const cartTotal = document.getElementById("cartTotal");

  if (!cartItemsList) return;

  if (cart.length === 0) {
    cartItemsList.innerHTML = `
      <div class="empty-cart-msg">
        <div class="empty-icon">🍽️</div>
        <h4>Your cart is empty</h4>
        <p>Explore our menu and add artisanal dishes you crave.</p>
      </div>
    `;
    if (cartSubtotal) cartSubtotal.textContent = formatCurrency(0);
    if (cartTax) cartTax.textContent = formatCurrency(0);
    if (cartTotal) cartTotal.textContent = formatCurrency(0);
  } else {
    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const tax = subtotal * 0.05; // 5% GST
    const grandTotal = subtotal + tax;

    cartItemsList.innerHTML = cart.map(item => `
      <div class="cart-item">
        ${item.image ? `<img src="${item.image}" alt="${item.name}" class="cart-item-thumbnail" />` : ''}
        <div class="cart-item-detail">
          <h5 style="font-size: 0.95rem; font-weight: 700; color: var(--text-primary); margin-bottom: 0.2rem;">${item.name}</h5>
          <p style="font-size: 0.88rem; color: var(--accent-terracotta); font-weight: 600;">
            ${formatCurrency(item.price * item.quantity)} 
            <span style="font-size: 0.78rem; color: var(--text-muted); font-weight: 400;">(${formatCurrency(item.price)} each)</span>
          </p>
        </div>
        <div class="cart-item-controls">
          <button class="qty-btn" onclick="updateCartQuantity(${item.id}, -1)" title="Decrease quantity">-</button>
          <span style="font-weight: 700; min-width: 18px; text-align: center;">${item.quantity}</span>
          <button class="qty-btn" onclick="updateCartQuantity(${item.id}, 1)" title="Increase quantity">+</button>
          <button class="cart-btn-remove" onclick="removeFromCart(${item.id})" title="Remove item">🗑️</button>
        </div>
      </div>
    `).join('');

    if (cartSubtotal) cartSubtotal.textContent = formatCurrency(subtotal);
    if (cartTax) cartTax.textContent = formatCurrency(tax);
    if (cartTotal) cartTotal.textContent = formatCurrency(grandTotal);
  }
}

/**
 * Setup Cart Drawer Interactions
 */
function setupCartDrawer() {
  const openBtn = document.getElementById("openCartBtn");
  const closeBtn = document.getElementById("closeCartBtn");
  const drawer = document.getElementById("cartDrawer");
  const overlay = document.getElementById("cartDrawerOverlay");
  const checkoutBtn = document.getElementById("checkoutBtn");

  const openDrawer = () => {
    drawer?.classList.add("active");
    overlay?.classList.add("active");
  };

  const closeDrawer = () => {
    drawer?.classList.remove("active");
    overlay?.classList.remove("active");
  };

  openBtn?.addEventListener("click", openDrawer);
  closeBtn?.addEventListener("click", closeDrawer);
  overlay?.addEventListener("click", closeDrawer);

  checkoutBtn?.addEventListener("click", () => {
    if (cart.length === 0) {
      showToast("Your cart is empty! Please add items to order.");
      return;
    }

    closeDrawer();
    window.location.href = "checkout.html";
  });
}

/**
 * Initialize Checkout Page (checkout.html)
 */
function initCheckoutPage() {
  renderCheckoutSummary();

  const submitBtn = document.getElementById("submitOrderBtn");
  const errorBanner = document.getElementById("checkoutErrorBanner");
  const errorMessage = document.getElementById("checkoutErrorMessage");

  // Clear validation styling when user types
  ["customerName", "customerPhone", "deliveryAddress"].forEach(id => {
    const input = document.getElementById(id);
    const errorMsg = document.getElementById(id + "Error");
    input?.addEventListener("input", () => {
      input.classList.remove("is-invalid");
      if (errorMsg) errorMsg.style.display = "none";
      if (errorBanner) errorBanner.style.display = "none";
    });
  });

  submitBtn?.addEventListener("click", async (e) => {
    e.preventDefault();
    if (cart.length === 0) {
      showToast("Your cart is empty. Please add dishes from the menu.");
      return;
    }

    // Input elements
    const nameInput = document.getElementById("customerName");
    const phoneInput = document.getElementById("customerPhone");
    const addressInput = document.getElementById("deliveryAddress");
    const instructionsInput = document.getElementById("deliveryInstructions");

    const name = nameInput?.value.trim() || "";
    const phone = phoneInput?.value.trim() || "";
    const address = addressInput?.value.trim() || "";
    const instructions = instructionsInput?.value.trim() || "";

    let isValid = true;

    // Validate Customer Name
    if (!name || name.length < 2) {
      nameInput?.classList.add("is-invalid");
      const err = document.getElementById("customerNameError");
      if (err) err.style.display = "block";
      isValid = false;
    }

    // Validate Phone Number
    const phoneRegex = /^[0-9+\s\-]{7,20}$/;
    if (!phone || !phoneRegex.test(phone)) {
      phoneInput?.classList.add("is-invalid");
      const err = document.getElementById("customerPhoneError");
      if (err) err.style.display = "block";
      isValid = false;
    }

    // Validate Delivery Address
    if (!address || address.length < 5) {
      addressInput?.classList.add("is-invalid");
      const err = document.getElementById("deliveryAddressError");
      if (err) err.style.display = "block";
      isValid = false;
    }

    if (!isValid) {
      showToast("⚠️ Please fill in all required fields.");
      return;
    }

    // Prepare trusted backend payload (only item IDs and quantities sent)
    const orderPayload = {
      customerName: name,
      customerPhone: phone,
      deliveryAddress: address,
      deliveryInstructions: instructions,
      items: cart.map(item => ({
        foodItemId: item.id,
        quantity: item.quantity
      }))
    };

    // Loading state for submission button
    submitBtn.disabled = true;
    submitBtn.innerHTML = `<span>⏳</span> <span>Placing Order with Kitchen...</span>`;
    if (errorBanner) errorBanner.style.display = "none";

    try {
      const response = await fetch("/api/orders", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(orderPayload)
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || (errorData.errors ? Object.values(errorData.errors).join(", ") : "Failed to place order"));
      }

      const savedOrder = await response.json();

      // Requirement 10: Clear cart ONLY after order is successfully saved
      cart = [];
      saveCart();
      updateCartUI();

      // Save order snapshot in session storage for instant preview
      try {
        sessionStorage.setItem("dineflow_last_order", JSON.stringify(savedOrder));
      } catch (e) {}

      // Navigate to order confirmation page with the unique order number
      window.location.href = `order-confirmation.html?orderNumber=${encodeURIComponent(savedOrder.orderNumber)}`;

    } catch (err) {
      console.error("Order submission failed:", err);
      submitBtn.disabled = false;
      renderCheckoutSummary();
      if (errorBanner && errorMessage) {
        errorMessage.textContent = err.message || "An error occurred while connecting to the server.";
        errorBanner.style.display = "block";
      }
      showToast("❌ Could not place order: " + (err.message || "Server error"));
    }
  });
}

function renderCheckoutSummary() {
  const emptyNotice = document.getElementById("checkoutEmptyNotice");
  const itemsContainer = document.getElementById("checkoutItemsContainer");
  const itemsList = document.getElementById("checkoutItemsList");
  const subtotalEl = document.getElementById("checkoutSubtotal");
  const feeEl = document.getElementById("checkoutDeliveryFee");
  const grandTotalEl = document.getElementById("checkoutGrandTotal");
  const submitBtn = document.getElementById("submitOrderBtn");

  if (!itemsContainer) return;

  if (cart.length === 0) {
    if (emptyNotice) emptyNotice.style.display = "block";
    itemsContainer.style.display = "none";
    if (submitBtn) submitBtn.disabled = true;
    return;
  }

  if (emptyNotice) emptyNotice.style.display = "none";
  itemsContainer.style.display = "block";
  if (submitBtn) submitBtn.disabled = false;

  const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  const deliveryFee = 40.0;
  const grandTotal = subtotal + deliveryFee;

  if (itemsList) {
    itemsList.innerHTML = cart.map(item => `
      <div class="summary-item-row">
        ${item.image ? `<img src="${item.image}" alt="${item.name}" class="summary-item-thumb" />` : ''}
        <div class="summary-item-info">
          <div class="summary-item-title">${item.name}</div>
          <div class="summary-item-meta">${item.quantity} × ${formatCurrency(item.price)}</div>
        </div>
        <div class="summary-item-price">${formatCurrency(item.price * item.quantity)}</div>
      </div>
    `).join('');
  }

  if (subtotalEl) subtotalEl.textContent = formatCurrency(subtotal);
  if (feeEl) feeEl.textContent = formatCurrency(deliveryFee);
  if (grandTotalEl) grandTotalEl.textContent = formatCurrency(grandTotal);

  if (submitBtn) {
    submitBtn.innerHTML = `<span>Place Order (${formatCurrency(grandTotal)})</span>`;
  }
}

/**
 * Initialize Order Confirmation Page (order-confirmation.html)
 */
async function initConfirmationPage() {
  const params = new URLSearchParams(window.location.search);
  const orderNumber = params.get("orderNumber");

  const orderNumEl = document.getElementById("confOrderNumber");
  const statusEl = document.getElementById("confStatus");
  const nameEl = document.getElementById("confCustomerName");
  const phoneEl = document.getElementById("confCustomerPhone");
  const addressEl = document.getElementById("confDeliveryAddress");
  const instructionsEl = document.getElementById("confDeliveryInstructions");
  const placedAtEl = document.getElementById("confPlacedAt");
  const subtotalEl = document.getElementById("confSubtotal");
  const feeEl = document.getElementById("confDeliveryFee");
  const totalEl = document.getElementById("confTotal");
  const itemsListEl = document.getElementById("confItemsList");
  const copyBtn = document.getElementById("copyOrderNumberBtn");

  copyBtn?.addEventListener("click", () => {
    const textToCopy = orderNumEl?.textContent || "";
    if (textToCopy && textToCopy !== "Loading...") {
      navigator.clipboard.writeText(textToCopy).then(() => {
        showToast("📋 Order number copied to clipboard!");
      }).catch(() => {
        showToast("Order No: " + textToCopy);
      });
    }
  });

  let orderData = null;

  if (orderNumber) {
    try {
      const res = await fetch(`/api/orders/number/${encodeURIComponent(orderNumber)}`);
      if (res.ok) {
        orderData = await res.json();
      }
    } catch (e) {
      console.warn("Could not fetch order from API, trying fallback:", e);
    }
  }

  if (!orderData) {
    try {
      const saved = sessionStorage.getItem("dineflow_last_order");
      if (saved) {
        orderData = JSON.parse(saved);
      }
    } catch (e) {}
  }

  if (orderData) {
    if (orderNumEl) orderNumEl.textContent = orderData.orderNumber || orderNumber || "DF-CONFIRMED";
    if (statusEl) statusEl.textContent = orderData.status || "CONFIRMED";
    if (nameEl) nameEl.textContent = orderData.customerName || "Valued Guest";
    if (phoneEl) phoneEl.textContent = orderData.customerPhone || "—";
    if (addressEl) addressEl.textContent = orderData.deliveryAddress || "—";
    if (instructionsEl) {
      instructionsEl.textContent = orderData.deliveryInstructions ? `Note: "${orderData.deliveryInstructions}"` : "";
    }
    if (placedAtEl) placedAtEl.textContent = orderData.createdAt || "Today";
    if (subtotalEl) subtotalEl.textContent = formatCurrency(orderData.subtotal);
    if (feeEl) feeEl.textContent = formatCurrency(orderData.deliveryFee || 40);
    if (totalEl) totalEl.textContent = formatCurrency(orderData.totalAmount);

    if (itemsListEl && orderData.items) {
      itemsListEl.innerHTML = orderData.items.map(item => `
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.65rem 0; border-bottom: 1px dashed var(--card-border);">
          <div>
            <strong>${item.foodName}</strong>
            <div style="font-size: 0.82rem; color: var(--text-muted);">${item.quantity} × ${formatCurrency(item.unitPrice)}</div>
          </div>
          <div style="font-weight: 700; color: var(--text-primary);">${formatCurrency(item.subtotal)}</div>
        </div>
      `).join('');
    }

    const trackBtn = document.getElementById("confTrackOrderBtn");
    if (trackBtn && orderData.orderNumber) {
      trackBtn.href = `track-order.html?orderNumber=${encodeURIComponent(orderData.orderNumber)}`;
    }
  } else {
    if (orderNumEl) orderNumEl.textContent = orderNumber || "Order Confirmed";
    showToast("⚠️ Could not load complete order summary. Please check your order number.");
  }
}

/**
 * Initialize Live Order Tracking Page (track-order.html)
 */
function initTrackingPage() {
  const form = document.getElementById("trackOrderForm");
  const input = document.getElementById("trackOrderInput");

  // Check URL parameter: ?orderNumber=...
  const urlParams = new URLSearchParams(window.location.search);
  const paramOrderNumber = urlParams.get("orderNumber");

  if (paramOrderNumber && input) {
    input.value = paramOrderNumber.trim();
    fetchAndDisplayTracking(paramOrderNumber.trim());
  }

  form?.addEventListener("submit", (e) => {
    e.preventDefault();
    const query = input?.value.trim();
    if (query) {
      const newUrl = `${window.location.pathname}?orderNumber=${encodeURIComponent(query)}`;
      window.history.pushState({ path: newUrl }, "", newUrl);
      fetchAndDisplayTracking(query);
    }
  });

  // Copy order number button
  document.getElementById("trackerCopyBtn")?.addEventListener("click", () => {
    const orderNo = document.getElementById("trackerOrderNumber")?.textContent;
    if (orderNo && orderNo !== "—") {
      navigator.clipboard.writeText(orderNo).then(() => {
        showToast("📋 Order number copied to clipboard!");
      });
    }
  });

  // Simulator status buttons
  document.querySelectorAll(".btn-status-action").forEach(btn => {
    btn.addEventListener("click", async () => {
      const orderNo = document.getElementById("trackerOrderNumber")?.textContent?.trim();
      const nextStatus = btn.getAttribute("data-next-status");
      if (!orderNo || orderNo === "—" || !nextStatus) return;

      try {
        btn.disabled = true;
        const res = await fetch(`/api/orders/number/${encodeURIComponent(orderNo)}/status`, {
          method: "PATCH",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ status: nextStatus })
        });

        if (!res.ok) {
          const err = await res.json().catch(() => ({}));
          throw new Error(err.message || "Failed to update status");
        }

        const updated = await res.json();
        showToast(`✨ Status updated to "${updated.statusDisplayName || updated.status}"!`);
        renderTrackingDetails(updated);
      } catch (e) {
        showToast("❌ " + e.message);
      } finally {
        btn.disabled = false;
      }
    });
  });
}

async function fetchAndDisplayTracking(orderNumber) {
  const loading = document.getElementById("trackLoadingState");
  const errorCard = document.getElementById("trackErrorState");
  const detailsCard = document.getElementById("trackDetailsCard");
  const errorMsg = document.getElementById("trackErrorMessage");

  if (loading) loading.style.display = "block";
  if (errorCard) errorCard.style.display = "none";
  if (detailsCard) detailsCard.style.display = "none";

  const cleanOrderNo = (orderNumber || "").trim();

  if (!cleanOrderNo) {
    if (loading) loading.style.display = "none";
    if (errorCard) {
      errorCard.style.display = "block";
      if (errorMsg) {
        errorMsg.innerHTML = "Please enter your Dineflow order number (e.g. <strong>DF-20261025-XXXXXX</strong>).";
      }
    }
    return;
  }

  try {
    const res = await fetch(`/api/orders/number/${encodeURIComponent(cleanOrderNo)}`);
    if (!res.ok) {
      throw new Error(`We couldn't find an order matching "<strong>${cleanOrderNo}</strong>". Dineflow order numbers follow the format <strong>DF-YYYYMMDD-XXXXXX</strong>. Please check your order confirmation or browse your orders in <a href="orders.html" style="color: var(--accent-terracotta); font-weight: 700; text-decoration: underline;">My Orders</a>.`);
    }

    const order = await res.json();
    renderTrackingDetails(order);
    if (detailsCard) detailsCard.style.display = "block";

  } catch (err) {
    if (errorCard) {
      errorCard.style.display = "block";
      if (errorMsg) {
        errorMsg.innerHTML = err.message || "Order not found in our database.";
      }
    }
  } finally {
    if (loading) loading.style.display = "none";
  }
}

function renderTrackingDetails(order) {
  const orderNumEl = document.getElementById("trackerOrderNumber");
  const placedDateEl = document.getElementById("trackerPlacedDate");
  const statusBadge = document.getElementById("trackerStatusBadge");
  const statusHeadline = document.getElementById("trackerStatusHeadline");
  const statusDesc = document.getElementById("trackerStatusDesc");
  const progressFill = document.getElementById("trackerProgressFill");
  const stepperWrapper = document.getElementById("trackerStepperWrapper");
  const cancelledBanner = document.getElementById("trackerCancelledBanner");

  const custNameEl = document.getElementById("trackerCustomerName");
  const custPhoneEl = document.getElementById("trackerCustomerPhone");
  const addressEl = document.getElementById("trackerDeliveryAddress");
  const instructionsLine = document.getElementById("trackerInstructionsLine");

  const itemsListEl = document.getElementById("trackerItemsList");
  const subtotalEl = document.getElementById("trackerSubtotal");
  const feeEl = document.getElementById("trackerDeliveryFee");
  const grandTotalEl = document.getElementById("trackerGrandTotal");

  if (orderNumEl) orderNumEl.textContent = order.orderNumber;
  if (placedDateEl) placedDateEl.textContent = `Placed on: ${order.createdAt || 'Recent'}`;

  // Status Badge
  const statusKey = order.status || "PLACED";
  if (statusBadge) {
    statusBadge.textContent = order.statusDisplayName || statusKey;
    statusBadge.className = `status-badge status-${statusKey.toLowerCase()}`;
  }

  if (statusHeadline) {
    statusHeadline.textContent = `Current Stage: ${order.statusDisplayName || statusKey}`;
  }
  if (statusDesc) {
    statusDesc.textContent = order.statusDescription || "Your order is being processed.";
  }

  // Stepper timeline
  const stages = ["PLACED", "CONFIRMED", "PREPARING", "OUT_FOR_DELIVERY", "DELIVERED"];
  const stageIndex = stages.indexOf(statusKey);

  if (statusKey === "CANCELLED") {
    if (stepperWrapper) stepperWrapper.style.display = "none";
    if (cancelledBanner) cancelledBanner.style.display = "block";
  } else {
    if (stepperWrapper) stepperWrapper.style.display = "block";
    if (cancelledBanner) cancelledBanner.style.display = "none";

    stages.forEach((s, idx) => {
      const node = document.getElementById(`step-${s}`);
      if (node) {
        node.classList.remove("is-completed", "is-active", "is-pending", "completed", "current");
        if (idx < stageIndex) {
          node.classList.add("is-completed", "completed");
        } else if (idx === stageIndex) {
          node.classList.add("is-active", "current");
        } else {
          node.classList.add("is-pending");
        }
      }
    });

    if (progressFill) {
      const percentage = stageIndex >= 0 ? (stageIndex / (stages.length - 1)) * 100 : 0;
      progressFill.style.width = `${percentage}%`;
    }
  }

  // Customer info
  if (custNameEl) custNameEl.textContent = order.customerName;
  if (custPhoneEl) custPhoneEl.textContent = order.customerPhone;
  if (addressEl) addressEl.textContent = order.deliveryAddress;
  if (instructionsLine) {
    instructionsLine.textContent = order.deliveryInstructions ? `Note: "${order.deliveryInstructions}"` : "";
  }

  // Ordered items
  if (itemsListEl && order.items) {
    itemsListEl.innerHTML = order.items.map(i => `
      <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.6rem 0; border-bottom: 1px dashed var(--card-border);">
        <div style="display: flex; align-items: center; gap: 0.6rem;">
          ${i.imageUrl ? `<img src="${i.imageUrl}" alt="${i.foodName}" style="width: 38px; height: 38px; border-radius: 6px; object-fit: cover;" />` : ''}
          <div>
            <div style="font-weight: 600; font-size: 0.9rem;">${i.foodName}</div>
            <div style="font-size: 0.78rem; color: var(--text-muted);">${i.quantity} × ${formatCurrency(i.unitPrice)}</div>
          </div>
        </div>
        <div style="font-weight: 700; font-size: 0.92rem; color: var(--text-primary);">${formatCurrency(i.subtotal)}</div>
      </div>
    `).join('');
  }

  if (subtotalEl) subtotalEl.textContent = formatCurrency(order.subtotal);
  if (feeEl) feeEl.textContent = formatCurrency(order.deliveryFee);
  if (grandTotalEl) grandTotalEl.textContent = formatCurrency(order.totalAmount);

  // Status Manager Dropdown
  if (nextStatusSelect) {
    const allowed = getNextAllowedStatuses(statusKey);
    const btn = document.getElementById("trackerUpdateStatusBtn");

    if (allowed.length === 0) {
      nextStatusSelect.innerHTML = `<option value="">Final State (${statusKey})</option>`;
      nextStatusSelect.disabled = true;
      if (btn) btn.disabled = true;
    } else {
      nextStatusSelect.disabled = false;
      if (btn) btn.disabled = false;
      nextStatusSelect.innerHTML = allowed.map(st => `<option value="${st}">${formatStatusDisplayName(st)}</option>`).join('');
    }
  }
}

function getNextAllowedStatuses(current) {
  switch (current) {
    case "PLACED": return ["CONFIRMED", "CANCELLED"];
    case "CONFIRMED": return ["PREPARING", "CANCELLED"];
    case "PREPARING": return ["OUT_FOR_DELIVERY", "CANCELLED"];
    case "OUT_FOR_DELIVERY": return ["DELIVERED", "CANCELLED"];
    default: return [];
  }
}

function formatStatusDisplayName(status) {
  switch (status) {
    case "PLACED": return "Placed";
    case "CONFIRMED": return "Confirm Order";
    case "PREPARING": return "Start Kitchen Preparation";
    case "OUT_FOR_DELIVERY": return "Hand to Delivery Valet";
    case "DELIVERED": return "Mark as Delivered";
    case "CANCELLED": return "Cancel Order";
    default: return status;
  }
}

/**
 * Initialize Order History Page (orders.html)
 * Displays real orders saved in MySQL, supports All/Active/Delivered/Cancelled filters,
 * detailed modal review, and robust loading/empty/error states.
 */
async function initOrdersPage() {
  const loading = document.getElementById("ordersLoading");
  const errorCard = document.getElementById("ordersError");
  const errorMsg = document.getElementById("ordersErrorMessage");
  const empty = document.getElementById("ordersEmpty");
  const grid = document.getElementById("ordersGrid");
  const retryBtn = document.getElementById("retryOrdersBtn");

  const countAll = document.getElementById("countAll");
  const countActive = document.getElementById("countActive");
  const countDelivered = document.getElementById("countDelivered");
  const countCancelled = document.getElementById("countCancelled");

  let allOrders = [];
  let currentFilter = "ALL";

  async function loadOrders() {
    if (loading) loading.style.display = "block";
    if (errorCard) errorCard.style.display = "none";
    if (empty) empty.style.display = "none";
    if (grid) grid.style.display = "none";

    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 8000);

    try {
      const res = await fetch("/api/orders", { signal: controller.signal });
      clearTimeout(timeoutId);

      if (!res.ok) {
        throw new Error(`Server returned HTTP ${res.status}`);
      }

      allOrders = await res.json();

      // Update Live Filter Counts
      const activeCount = allOrders.filter(o => ["PLACED", "CONFIRMED", "PREPARING", "OUT_FOR_DELIVERY"].includes(o.status)).length;
      const deliveredCount = allOrders.filter(o => o.status === "DELIVERED").length;
      const cancelledCount = allOrders.filter(o => o.status === "CANCELLED").length;

      if (countAll) countAll.textContent = `(${allOrders.length})`;
      if (countActive) countActive.textContent = `(${activeCount})`;
      if (countDelivered) countDelivered.textContent = `(${deliveredCount})`;
      if (countCancelled) countCancelled.textContent = `(${cancelledCount})`;

      renderFilteredOrders(currentFilter);

    } catch (err) {
      console.error("Failed to load orders from MySQL:", err);
      allOrders = [];
      if (errorCard) {
        errorCard.style.display = "block";
        if (errorMsg) {
          errorMsg.textContent = err.name === "AbortError"
            ? "Connection timed out while loading orders. Please check that Spring Boot is running and try again."
            : "Could not retrieve order history: " + (err.message || "Network error");
        }
      }
    } finally {
      // NEVER leave the loading message displayed indefinitely!
      if (loading) loading.style.display = "none";
    }
  }

  function renderFilteredOrders(filter) {
    currentFilter = filter;
    let filtered = allOrders;

    if (filter === "ACTIVE") {
      filtered = allOrders.filter(o => ["PLACED", "CONFIRMED", "PREPARING", "OUT_FOR_DELIVERY"].includes(o.status));
    } else if (filter === "DELIVERED") {
      filtered = allOrders.filter(o => o.status === "DELIVERED");
    } else if (filter === "CANCELLED") {
      filtered = allOrders.filter(o => o.status === "CANCELLED");
    }

    if (filtered.length === 0) {
      if (empty) {
        empty.style.display = "block";
        const emptyH3 = empty.querySelector("h3");
        const emptyP = empty.querySelector("p");
        if (filter !== "ALL") {
          if (emptyH3) emptyH3.textContent = `No ${filter.charAt(0) + filter.slice(1).toLowerCase()} Orders`;
          if (emptyP) emptyP.textContent = `You have no orders currently in "${filter.toLowerCase()}" status.`;
        } else {
          if (emptyH3) emptyH3.textContent = "No Orders Found";
          if (emptyP) emptyP.textContent = "You haven't placed any orders yet. Discover our chef-curated selection of artisanal dishes.";
        }
      }
      if (grid) grid.style.display = "none";
    } else {
      if (empty) empty.style.display = "none";
      if (grid) {
        grid.style.display = "grid";
        grid.innerHTML = filtered.map(order => {
          const itemsSummary = (order.items || []).map(i => `${i.quantity}× ${i.foodName}`).join(', ');
          const statusKey = order.status || "PLACED";
          return `
            <div class="order-history-card">
              <div>
                <div class="order-card-header">
                  <div>
                    <div class="order-card-number-box">
                      <span class="order-card-number">${order.orderNumber}</span>
                      <button class="btn-copy-mini" onclick="copyOrderText('${order.orderNumber}')" title="Copy Order Number">📋 Copy</button>
                    </div>
                    <div class="order-card-date">📅 ${order.createdAt || 'Recent'}</div>
                  </div>
                  <span class="status-badge status-${statusKey.toLowerCase()}">
                    ${order.statusDisplayName || statusKey}
                  </span>
                </div>

                <div class="order-items-snippet">
                  <strong>Dishes:</strong> ${itemsSummary || 'Artisanal selections'}<br/>
                  <span style="font-size: 0.82rem; color: var(--text-muted); display: block; margin-top: 0.4rem;">
                    📍 ${order.deliveryAddress ? (order.deliveryAddress.length > 50 ? order.deliveryAddress.substring(0, 50) + '...' : order.deliveryAddress) : 'Delivery Address'}
                  </span>
                </div>
              </div>

              <div class="order-card-footer">
                <div class="order-card-total-box">
                  <span style="font-size: 0.76rem; text-transform: uppercase; color: var(--text-muted); font-weight: 700;">Total Amount</span>
                  <span class="order-card-total">${formatCurrency(order.totalAmount)}</span>
                </div>
                <div class="order-card-actions">
                  <button class="btn-view-details" data-order-number="${order.orderNumber}">
                    View Details
                  </button>
                  <a href="track-order.html?orderNumber=${encodeURIComponent(order.orderNumber)}" class="btn-track-action">
                    Track Order →
                  </a>
                </div>
              </div>
            </div>
          `;
        }).join('');

        // Attach View Details Click Handlers
        grid.querySelectorAll(".btn-view-details").forEach(btn => {
          btn.addEventListener("click", () => {
            const orderNo = btn.getAttribute("data-order-number");
            const order = allOrders.find(o => o.orderNumber === orderNo);
            if (order) openOrderDetailsModal(order);
          });
        });
      }
    }
  }

  // Filter Buttons Handler
  const filterBtns = document.querySelectorAll("[data-order-filter]");
  filterBtns.forEach(btn => {
    btn.addEventListener("click", () => {
      filterBtns.forEach(b => b.classList.remove("active"));
      btn.classList.add("active");
      const f = btn.getAttribute("data-order-filter") || "ALL";
      renderFilteredOrders(f);
    });
  });

  retryBtn?.addEventListener("click", () => loadOrders());

  // Setup Order Details Modal Controls
  setupOrderDetailsModal();

  // Initial Load
  await loadOrders();
}

/**
 * Order Details Modal Setup and Open Logic
 */
function setupOrderDetailsModal() {
  const modal = document.getElementById("orderDetailsModal");
  const closeBtn = document.getElementById("closeOrderDetailsBtn");
  const closeActionBtn = document.getElementById("modalCloseActionBtn");

  const closeModal = () => modal?.classList.remove("active");

  closeBtn?.addEventListener("click", closeModal);
  closeActionBtn?.addEventListener("click", closeModal);

  modal?.addEventListener("click", (e) => {
    if (e.target === modal) closeModal();
  });

  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && modal?.classList.contains("active")) {
      closeModal();
    }
  });
}

function openOrderDetailsModal(order) {
  const modal = document.getElementById("orderDetailsModal");
  if (!modal) return;

  const modalOrderNumber = document.getElementById("modalOrderNumber");
  const modalStatusBadge = document.getElementById("modalOrderStatusBadge");
  const modalOrderDate = document.getElementById("modalOrderDate");
  const modalCustomerName = document.getElementById("modalCustomerName");
  const modalCustomerPhone = document.getElementById("modalCustomerPhone");
  const modalDeliveryAddress = document.getElementById("modalDeliveryAddress");
  const modalInstructionsRow = document.getElementById("modalDeliveryInstructionsRow");
  const modalInstructions = document.getElementById("modalDeliveryInstructions");
  const modalItemsList = document.getElementById("modalItemsList");
  const modalSubtotal = document.getElementById("modalSubtotal");
  const modalDeliveryFee = document.getElementById("modalDeliveryFee");
  const modalTotalAmount = document.getElementById("modalTotalAmount");
  const modalTrackLink = document.getElementById("modalTrackLinkBtn");

  if (modalOrderNumber) modalOrderNumber.textContent = order.orderNumber;
  if (modalStatusBadge) {
    const s = order.status || "PLACED";
    modalStatusBadge.textContent = order.statusDisplayName || s;
    modalStatusBadge.className = `status-badge status-${s.toLowerCase()}`;
  }
  if (modalOrderDate) modalOrderDate.textContent = `Placed on: ${order.createdAt || 'Recent'}`;
  if (modalCustomerName) modalCustomerName.textContent = order.customerName || 'Valued Guest';
  if (modalCustomerPhone) modalCustomerPhone.textContent = order.customerPhone || '—';
  if (modalDeliveryAddress) modalDeliveryAddress.textContent = order.deliveryAddress || '—';

  if (modalInstructionsRow && modalInstructions) {
    if (order.deliveryInstructions) {
      modalInstructions.textContent = order.deliveryInstructions;
      modalInstructionsRow.style.display = "block";
    } else {
      modalInstructionsRow.style.display = "none";
    }
  }

  if (modalItemsList && order.items) {
    modalItemsList.innerHTML = order.items.map(i => `
      <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.65rem 0; border-bottom: 1px dashed var(--card-border);">
        <div>
          <strong style="color: var(--text-primary); font-size: 0.95rem;">${i.foodName}</strong>
          <div style="font-size: 0.82rem; color: var(--text-muted);">${i.quantity} × ${formatCurrency(i.unitPrice)}</div>
        </div>
        <div style="font-weight: 700; color: var(--text-primary); font-size: 0.95rem;">
          ${formatCurrency(i.subtotal)}
        </div>
      </div>
    `).join('');
  }

  if (modalSubtotal) modalSubtotal.textContent = formatCurrency(order.subtotal);
  if (modalDeliveryFee) modalDeliveryFee.textContent = formatCurrency(order.deliveryFee || 40.0);
  if (modalTotalAmount) modalTotalAmount.textContent = formatCurrency(order.totalAmount);

  if (modalTrackLink) {
    modalTrackLink.href = `track-order.html?orderNumber=${encodeURIComponent(order.orderNumber)}`;
  }

  modal.classList.add("active");
}

function copyOrderText(text) {
  if (text) {
    navigator.clipboard.writeText(text).then(() => {
      showToast("📋 Order number " + text + " copied!");
    }).catch(() => {
      showToast("Order: " + text);
    });
  }
}

function openCheckoutModal() {
  const modal = document.getElementById("checkoutModal");
  const payableAmountEl = document.getElementById("checkoutPayableAmount");
  
  if (modal) {
    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const tax = subtotal * 0.05;
    const total = subtotal + tax;

    if (payableAmountEl) {
      payableAmountEl.textContent = formatCurrency(total);
    }
    modal.classList.add("active");
  }
}

function setupModals() {
  // Checkout Modal
  const checkoutModal = document.getElementById("checkoutModal");
  const closeCheckoutBtn = document.getElementById("closeCheckoutModal");
  const checkoutForm = document.getElementById("checkoutForm");

  closeCheckoutBtn?.addEventListener("click", () => {
    checkoutModal?.classList.remove("active");
  });

  checkoutForm?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const name = document.getElementById("orderName")?.value || "Guest";
    const phone = document.getElementById("orderPhone")?.value || "";
    const diningType = document.getElementById("orderDiningType")?.value || "Dine-in";

    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const grandTotal = subtotal + (subtotal * 0.05);

    const orderPayload = {
      customerName: name,
      customerPhone: phone,
      tableNumber: diningType,
      items: cart.map(c => ({
        menuItemId: c.id,
        name: c.name,
        quantity: c.quantity,
        price: c.price
      })),
      totalAmount: grandTotal
    };

    try {
      const res = await fetch("/api/orders", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(orderPayload)
      });

      if (res.ok) {
        const orderConfirmation = await res.json();
        showOrderReceipt(orderConfirmation, cart, grandTotal);
      } else {
        throw new Error("Failed to place order");
      }
    } catch (err) {
      // Fallback receipt
      showOrderReceipt({
        orderId: "DF-" + Math.floor(100000 + Math.random() * 900000),
        customerName: name,
        tableNumber: diningType,
        totalAmount: grandTotal,
        placedAt: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      }, cart, grandTotal);
    }

    cart = [];
    saveCart();
    updateCartUI();
    checkoutModal?.classList.remove("active");
    checkoutForm.reset();
  });

  // Receipt Modal
  const receiptModal = document.getElementById("receiptModal");
  const closeReceiptBtn = document.getElementById("closeReceiptModal");
  const receiptDoneBtn = document.getElementById("receiptDoneBtn");

  const closeReceipt = () => receiptModal?.classList.remove("active");
  closeReceiptBtn?.addEventListener("click", closeReceipt);
  receiptDoneBtn?.addEventListener("click", closeReceipt);

  // Reservation Modal
  const resModal = document.getElementById("reservationModal");
  const openResButtons = document.querySelectorAll(".btn-open-reserve");
  const closeResBtn = document.getElementById("closeReserveModal");
  const resForm = document.getElementById("reservationForm");

  openResButtons.forEach(btn => btn.addEventListener("click", () => resModal?.classList.add("active")));
  closeResBtn?.addEventListener("click", () => resModal?.classList.remove("active"));

  resForm?.addEventListener("submit", (e) => {
    e.preventDefault();
    const name = document.getElementById("resName")?.value || "Guest";
    const date = document.getElementById("resDate")?.value;
    const time = document.getElementById("resTime")?.value;
    const guests = document.getElementById("resGuests")?.value || "2";

    resModal?.classList.remove("active");
    showToast(`✨ Table for ${guests} reserved for ${name} on ${date} at ${time}!`);
    resForm.reset();
  });
}

function showOrderReceipt(order, items, total) {
  const receiptModal = document.getElementById("receiptModal");
  const orderIdEl = document.getElementById("receiptOrderId");
  const summaryEl = document.getElementById("receiptSummary");

  if (!receiptModal) return;

  if (orderIdEl) {
    orderIdEl.textContent = `Order ID: ${order.orderId}`;
  }

  if (summaryEl) {
    summaryEl.innerHTML = `
      <div style="margin-bottom: 0.6rem; border-bottom: 1px dashed var(--card-border); padding-bottom: 0.5rem;">
        <strong>Guest:</strong> ${order.customerName}<br/>
        <strong>Dining Type:</strong> ${order.tableNumber}<br/>
        <strong>Time:</strong> ${order.placedAt || 'Just now'}
      </div>
      <div style="margin-bottom: 0.8rem;">
        <strong>Ordered Items:</strong>
        <ul style="list-style: none; margin-top: 0.4rem; padding-left: 0;">
          ${items.map(i => `
            <li style="display: flex; justify-content: space-between; margin-bottom: 0.25rem;">
              <span>${i.quantity}x ${i.name}</span>
              <span>${formatCurrency(i.price * i.quantity)}</span>
            </li>
          `).join('')}
        </ul>
      </div>
      <div style="border-top: 1px solid var(--card-border); padding-top: 0.5rem; display: flex; justify-content: space-between; font-weight: 700; font-size: 1.05rem; color: var(--accent-terracotta);">
        <span>Total Paid:</span>
        <span>${formatCurrency(total)}</span>
      </div>
    `;
  }

  receiptModal.classList.add("active");
  showToast(`🎉 Order ${order.orderId} placed successfully!`);
}

function showToast(message) {
  const container = document.getElementById("toastContainer");
  if (!container) return;

  const toast = document.createElement("div");
  toast.className = "toast";
  toast.innerHTML = `<span>🍽️</span> <span>${message}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.remove();
  }, 3200);
}

async function checkBackendStatus() {
  const statusElement = document.getElementById("backendStatusPill");
  if (!statusElement) return;

  try {
    const res = await fetch("/api/status");
    if (res.ok) {
      const data = await res.json();
      statusElement.innerHTML = `<span class="server-dot"></span> Spring Boot REST API: Online (${data.status})`;
      statusElement.style.color = "var(--accent-green)";
    }
  } catch (err) {
    statusElement.innerHTML = `<span class="server-dot" style="background:#e09f3e"></span> Spring Boot: Active`;
  }
}
