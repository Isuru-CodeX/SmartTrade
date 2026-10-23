/* My account : profile, addresses, sign out */
const loadingOpts = {clickToClose: false};

window.addEventListener("load", async () => {
    Notiflix.Loading.pulse("Loading your account...", loadingOpts);
    try {
        await getCities();
        await loadUserData();
        preloadOrdersBadge();
    } finally {
        Notiflix.Loading.remove();
    }
    openTabFromHash();
});

/* deep links: my-account.html#products | #listings | #addresses | #orders | #profile */
const TAB_HASHES = {
    products: "product-anchor",
    product: "product-anchor",
    sell: "product-anchor",
    "nav-add-product": "product-anchor",
    listings: "product-listing-anchor",
    listing: "product-listing-anchor",
    "nav-product-listing": "product-listing-anchor",
    addresses: "address-anchor",
    address: "address-anchor",
    "nav-address": "address-anchor",
    orders: "orders-anchor",
    order: "orders-anchor",
    "nav-orders": "orders-anchor",
    profile: "account-anchor",
    account: "account-anchor",
    "nav-account": "account-anchor"
};

function openTabFromHash() {
    const raw = (location.hash || "").replace("#", "").trim().toLowerCase();
    if (!raw) return;
    const id = TAB_HASHES[raw] || raw;
    const el = document.getElementById(id);
    if (!el) return;
    try {
        if (window.bootstrap && bootstrap.Tab) {
            bootstrap.Tab.getOrCreateInstance(el).show();
        } else if (typeof el.click === "function") {
            el.click();
        }
    } catch (e) {
        if (typeof el.click === "function") el.click();
    }
}
window.addEventListener("hashchange", openTabFromHash);

document.getElementById("address-anchor").addEventListener("shown.bs.tab", () => loadAddress());
const ordersAnchorEl = document.getElementById("orders-anchor");
if (ordersAnchorEl) {
    ordersAnchorEl.addEventListener("shown.bs.tab", () => loadMyOrders());
}

async function loadAddress() {
    Notiflix.Loading.pulse("Loading addresses...", loadingOpts);
    try {
        const response = await fetch("api/profiles/addresses");
        if (response.ok) {
            const data = await response.json();
            document.getElementById("addName").textContent = "Name: " + data.name;
            document.getElementById("addEmail").textContent = "Email: " + data.email;
            renderAddresses(data.addresses || []);
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    } finally {
        Notiflix.Loading.remove();
    }
}

function renderAddresses(addresses) {
    const list = document.getElementById("addressList");
    if (!addresses.length) {
        list.innerHTML = '<div class="empty-state" style="grid-column:1/-1;padding:2rem"><div class="ic"><i class="fa-regular fa-location-dot"></i></div><h4>No saved addresses</h4><p>Add your address under Profile &amp; security.</p></div>';
        return;
    }
    list.innerHTML = addresses.map((addr) =>
        '<div class="address-card ' + (addr.isPrimary ? "primary" : "") + '">' +
        "<h6>" + ST.esc(addr.lineOne) + " " + ST.esc(addr.lineTwo || "") + "</h6>" +
        "<p>" + ST.esc(addr.cityName) + "</p>" +
        '<p class="mono" style="color:var(--st-text)"><i class="fa-regular fa-phone me-2" style="color:var(--st-brand-3)"></i>' + ST.esc(addr.mobile) + "</p></div>"
    ).join("");
}

async function saveChanges() {
    const message = document.getElementById("message");
    message.textContent = "";
    Notiflix.Loading.pulse("Saving...", loadingOpts);

    const userObj = {
        firstName: document.getElementById("firstName").value,
        lastName: document.getElementById("lastName").value,
        lineOne: document.getElementById("lineOne").value,
        lineTwo: document.getElementById("lineTwo").value,
        postalCode: document.getElementById("postalCode").value,
        cityId: document.getElementById("citySelect").value,
        mobile: document.getElementById("mobile").value,
        password: document.getElementById("currentPassword").value,
        newPassword: document.getElementById("newPassword").value,
        confirmPassword: document.getElementById("confirmPassword").value,
        profileImage: (function () {
            const p = document.getElementById("profileAvatarPreview");
            const src = p ? p.getAttribute("src") : "";
            return (src && !src.includes("assets/images/avatar.png")) ? src : "";
        })()
    };

    try {
        const response = await fetch("api/profiles/update-profile", {
            method: "PUT",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(userObj)
        });
        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                Notiflix.Report.success("SmartTrade", data.message, "Okay");
                document.getElementById("newPassword").value = "";
                document.getElementById("confirmPassword").value = "";
                await loadUserData();
            } else {
                message.textContent = data.message;
                Notiflix.Notify.failure(data.message);
            }
        } else {
            Notiflix.Notify.failure("Profile update failed!");
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    } finally {
        Notiflix.Loading.remove(400);
    }
}

async function loadUserData() {
    try {
        const response = await fetch("api/profiles/user-profile");
        if (response.ok) {
            if (response.redirected) {
                window.location.href = response.url;
                return;
            }
            const data = await response.json();
            const u = data.user;

            document.getElementById("username").textContent = "Hello, " + u.firstName + " " + u.lastName;
            const since = String(u.sinceAt || "").replace("-", " ").split(" ");
            document.getElementById("since").textContent = since.length > 1 ? "SmartTrade member since " + since[1] + " " + since[0] : "SmartTrade member";
            document.getElementById("firstName").value = u.firstName;
            document.getElementById("lastName").value = u.lastName;
            document.getElementById("lineOne").value = u.lineOne || "";
            document.getElementById("lineTwo").value = u.lineTwo || "";
            document.getElementById("postalCode").value = u.postalCode || "";
            document.getElementById("citySelect").value = u.cityId ? u.cityId : 0;
            document.getElementById("mobile").value = u.mobile || "";
            document.getElementById("currentPassword").value = u.password || "";

            const avatarSrc = u.profileImage ? u.profileImage : "assets/images/avatar.png";
            const sidebarAvatar = document.getElementById("sidebarAvatar");
            if (sidebarAvatar) sidebarAvatar.src = avatarSrc;
            const profilePreview = document.getElementById("profileAvatarPreview");
            if (profilePreview) profilePreview.src = avatarSrc;

            const removeBtn = document.getElementById("removeAvatarBtn");
            if (removeBtn) {
                removeBtn.style.display = u.profileImage ? "inline-flex" : "none";
            }
        } else {
            Notiflix.Notify.failure("Profile data loading failed!");
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    }
}

async function handleAvatarChange(event) {
    const file = event.target.files && event.target.files[0];
    if (!file) return;

    if (!file.type.startsWith("image/")) {
        Notiflix.Notify.failure("Please select a valid image file!");
        event.target.value = "";
        return;
    }

    if (file.size > 5 * 1024 * 1024) {
        Notiflix.Notify.failure("Image size cannot exceed 5MB!");
        event.target.value = "";
        return;
    }

    // Instant local preview
    const previewUrl = URL.createObjectURL(file);
    const profilePreview = document.getElementById("profileAvatarPreview");
    const sidebarAvatar = document.getElementById("sidebarAvatar");
    if (profilePreview) profilePreview.src = previewUrl;
    if (sidebarAvatar) sidebarAvatar.src = previewUrl;

    const formData = new FormData();
    formData.append("avatar", file);

    Notiflix.Loading.pulse("Uploading profile picture...", loadingOpts);
    try {
        const response = await fetch("api/profiles/upload-avatar", {
            method: "POST",
            body: formData
        });

        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                Notiflix.Notify.success(data.message || "Profile picture updated!");
                if (data.profileImage) {
                    if (profilePreview) profilePreview.src = data.profileImage;
                    if (sidebarAvatar) sidebarAvatar.src = data.profileImage;
                }
                const removeBtn = document.getElementById("removeAvatarBtn");
                if (removeBtn) removeBtn.style.display = "inline-flex";
            } else {
                Notiflix.Notify.failure(data.message || "Profile picture update failed!");
                await loadUserData();
            }
        } else {
            Notiflix.Notify.failure("Profile picture upload failed!");
            await loadUserData();
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message || "Error uploading image");
        await loadUserData();
    } finally {
        Notiflix.Loading.remove(300);
        event.target.value = "";
    }
}

async function removeAvatar() {
    Notiflix.Confirm.show(
        "Remove Photo",
        "Are you sure you want to remove your profile picture?",
        "Yes, Remove",
        "Cancel",
        async () => {
            Notiflix.Loading.pulse("Removing picture...", loadingOpts);
            try {
                const response = await fetch("api/profiles/remove-avatar", {
                    method: "POST"
                });
                if (response.ok) {
                    const data = await response.json();
                    if (data.status) {
                        Notiflix.Notify.success(data.message || "Profile picture removed!");
                        const defaultAvatar = "assets/images/avatar.png";
                        const profilePreview = document.getElementById("profileAvatarPreview");
                        const sidebarAvatar = document.getElementById("sidebarAvatar");
                        if (profilePreview) profilePreview.src = defaultAvatar;
                        if (sidebarAvatar) sidebarAvatar.src = defaultAvatar;
                        const removeBtn = document.getElementById("removeAvatarBtn");
                        if (removeBtn) removeBtn.style.display = "none";
                    } else {
                        Notiflix.Notify.failure(data.message);
                    }
                } else {
                    Notiflix.Notify.failure("Failed to remove profile picture!");
                }
            } catch (e) {
                Notiflix.Notify.failure(e.message);
            } finally {
                Notiflix.Loading.remove(300);
            }
        }
    );
}

async function getCities() {
    try {
        const response = await fetch("api/data/cities");
        if (response.ok) {
            const data = await response.json();
            const citySelect = document.getElementById("citySelect");
            data.cities.forEach((city) => {
                const option = document.createElement("option");
                option.value = city.id;
                option.textContent = city.name;
                citySelect.appendChild(option);
            });
        } else {
            Notiflix.Notify.failure("City loading failed!");
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    }
}

async function signOut() {
    Notiflix.Loading.pulse("Signing out...", loadingOpts);
    try {
        const response = await fetch("api/users/logout", {method: "GET", credentials: "include"});
        sessionStorage.removeItem("st.session_uid");
        if (response.ok) {
            Notiflix.Report.success("SmartTrade", "Logout successful", "Okay", () => {
                window.location = "sign-in.html";
            });
        } else {
            Notiflix.Notify.failure("Something went wrong. Log out process failed!");
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    } finally {
        Notiflix.Loading.remove(400);
    }
}

/* ════════════════════ CUSTOMER ORDERS & RECEIPTS ════════════════════ */
let rawOrders = [];
let ordersLoaded = false;

async function preloadOrdersBadge() {
    try {
        const response = await fetch("api/users/orders", { credentials: "include" });
        if (response.ok) {
            const data = await response.json();
            if (data.status === "success" && Array.isArray(data.orders)) {
                rawOrders = data.orders;
                const badge = document.getElementById("ordersNavBadge");
                if (badge && rawOrders.length > 0) {
                    badge.textContent = rawOrders.length;
                    badge.classList.remove("d-none");
                }
                updateOrderMetrics(rawOrders);
                if (location.hash === "#orders") {
                    filterOrders();
                }
            }
        }
    } catch (_) {}
}

async function loadMyOrders(force = false) {
    if (ordersLoaded && !force && rawOrders.length > 0) {
        filterOrders();
        return;
    }

    const spinner = document.getElementById("ordersLoadingSpinner");
    const listWrap = document.getElementById("ordersListWrap");
    const emptyState = document.getElementById("ordersEmptyState");
    const refreshIcon = document.getElementById("refreshOrdersIcon");

    if (refreshIcon) refreshIcon.classList.add("fa-spin");
    if (spinner) spinner.hidden = false;
    if (emptyState) emptyState.hidden = true;
    if (listWrap && force) listWrap.innerHTML = "";

    try {
        const response = await fetch("api/users/orders", { credentials: "include" });
        if (response.status === 401) {
            window.location.href = "sign-in.html";
            return;
        }
        if (!response.ok) {
            Notiflix.Notify.failure("Failed to load orders from server.");
            return;
        }

        const data = await response.json();
        if (data.status === "success" && Array.isArray(data.orders)) {
            rawOrders = data.orders;
            ordersLoaded = true;
            updateOrderMetrics(rawOrders);

            const navBadge = document.getElementById("ordersNavBadge");
            if (navBadge) {
                navBadge.textContent = rawOrders.length;
                navBadge.classList.toggle("d-none", rawOrders.length === 0);
            }

            filterOrders();
        } else {
            rawOrders = [];
            updateOrderMetrics([]);
            filterOrders();
        }
    } catch (e) {
        Notiflix.Notify.failure("Error loading orders: " + e.message);
    } finally {
        if (spinner) spinner.hidden = true;
        if (refreshIcon) refreshIcon.classList.remove("fa-spin");
    }
}

function updateOrderMetrics(orders) {
    const totalCount = orders.length;
    let activeCount = 0;
    let deliveredCount = 0;
    let totalSpent = 0;

    orders.forEach((o) => {
        const s = (o.status || "").toUpperCase();
        if (["PACKING", "PROCESSING", "PENDING"].includes(s)) {
            activeCount++;
        } else if (["DELIVERED", "COMPLETED", "PAID"].includes(s)) {
            deliveredCount++;
        }
        if (!["CANCELED", "CANCELLED", "REJECTED"].includes(s)) {
            totalSpent += Number(o.total || 0);
        }
    });

    const elTotal = document.getElementById("metricTotalOrders");
    const elActive = document.getElementById("metricActiveOrders");
    const elDelivered = document.getElementById("metricDeliveredOrders");
    const elSpent = document.getElementById("metricTotalSpent");

    if (elTotal) elTotal.textContent = totalCount;
    if (elActive) elActive.textContent = activeCount;
    if (elDelivered) elDelivered.textContent = deliveredCount;
    if (elSpent) elSpent.textContent = ST.rs(totalSpent);
}

function filterOrders() {
    const searchInput = document.getElementById("orderSearchInput");
    const filterSelect = document.getElementById("orderStatusFilterSelect");

    const query = (searchInput ? searchInput.value : "").trim().toLowerCase();
    const statusFilter = (filterSelect ? filterSelect.value : "ALL").toUpperCase();

    const filtered = rawOrders.filter((order) => {
        // Status filter
        const orderStatus = (order.status || "PAID").toUpperCase();
        let matchStatus = true;
        if (statusFilter !== "ALL") {
            if (statusFilter === "DELIVERED") {
                matchStatus = ["DELIVERED", "COMPLETED"].includes(orderStatus);
            } else if (statusFilter === "CANCELED") {
                matchStatus = ["CANCELED", "CANCELLED", "REJECTED"].includes(orderStatus);
            } else {
                matchStatus = orderStatus === statusFilter;
            }
        }
        if (!matchStatus) return false;

        // Query filter
        if (!query) return true;
        const matchNumber = String(order.orderNumber || "").toLowerCase().includes(query);
        const matchId = String(order.id || "").includes(query);
        const matchPayment = String(order.stripePaymentId || "").toLowerCase().includes(query);
        const matchAddress = String(order.shippingAddress || "").toLowerCase().includes(query);
        const matchItems = (order.items || []).some((it) => String(it.title || "").toLowerCase().includes(query));

        return matchNumber || matchId || matchPayment || matchAddress || matchItems;
    });

    renderMyOrders(filtered, Boolean(query || statusFilter !== "ALL"));
}

function resetOrderFilters() {
    const searchInput = document.getElementById("orderSearchInput");
    const filterSelect = document.getElementById("orderStatusFilterSelect");
    if (searchInput) searchInput.value = "";
    if (filterSelect) filterSelect.value = "ALL";
    filterOrders();
}

function renderMyOrders(orders, isFiltered) {
    const listWrap = document.getElementById("ordersListWrap");
    const emptyState = document.getElementById("ordersEmptyState");
    const emptyTitle = document.getElementById("ordersEmptyTitle");
    const emptyText = document.getElementById("ordersEmptyText");

    if (!listWrap || !emptyState) return;

    if (!orders || orders.length === 0) {
        listWrap.innerHTML = "";
        emptyState.hidden = false;
        if (isFiltered) {
            if (emptyTitle) emptyTitle.textContent = "No matching orders found";
            if (emptyText) emptyText.innerHTML = 'Try adjusting your search keywords or filter.<br><button type="button" class="st-btn st-btn-sm mt-3" onclick="resetOrderFilters()"><i class="fa-regular fa-arrows-rotate me-1"></i> Reset Filters</button>';
        } else {
            if (emptyTitle) emptyTitle.textContent = "No orders placed yet";
            if (emptyText) emptyText.innerHTML = 'When you buy items on SmartTrade, your purchases, delivery progress, and official invoices will show up here.<br><a class="st-btn mt-3" href="search.html"><i class="fa-regular fa-bag-shopping me-1"></i> Start shopping</a>';
        }
        return;
    }

    emptyState.hidden = true;
    listWrap.innerHTML = orders.map((order) => renderOrderCard(order)).join("");
}

function getOrderStatusMeta(statusStr) {
    const s = String(statusStr || "PAID").toUpperCase();
    switch (s) {
        case "DELIVERED":
            return { cls: "status-delivered", icon: "fa-solid fa-circle-check", label: "Delivered" };
        case "COMPLETED":
            return { cls: "status-completed", icon: "fa-solid fa-circle-check", label: "Completed" };
        case "PACKING":
            return { cls: "status-packing", icon: "fa-solid fa-box-open", label: "Packing" };
        case "PROCESSING":
            return { cls: "status-packing", icon: "fa-solid fa-truck-ramp-box", label: "Processing" };
        case "PAID":
            return { cls: "status-paid", icon: "fa-solid fa-shield-check", label: "Paid (Stripe)" };
        case "PENDING":
            return { cls: "status-pending", icon: "fa-solid fa-clock", label: "Pending" };
        case "CANCELED":
        case "CANCELLED":
            return { cls: "status-canceled", icon: "fa-solid fa-circle-xmark", label: "Canceled" };
        case "REJECTED":
            return { cls: "status-rejected", icon: "fa-solid fa-ban", label: "Rejected" };
        default:
            return { cls: "status-paid", icon: "fa-solid fa-circle-info", label: s };
    }
}

function renderOrderCard(order) {
    const meta = getOrderStatusMeta(order.status);
    const orderNo = ST.esc(order.orderNumber || ("#ST-ORD-" + String(order.id || 0).padStart(5, "0")));
    const dateStr = ST.esc(order.createdAt || "Recent");
    const items = order.items || [];

    const itemsHtml = items.map((it) => {
        const title = ST.esc(it.title || "Ordered Item");
        const imgSrc = ST.esc(it.image || "assets/images/placeholder.png");
        const unitPrice = ST.rs(it.unitPrice || 0);
        const lineTotal = ST.rs(it.total || ((it.unitPrice || 0) * (it.qty || 1)));
        const productLink = it.productId ? `single-product.html?productId=${it.productId}` : `#`;

        return `
            <div class="st-order-item-row">
                <a href="${productLink}" class="st-order-item-thumb" title="${title}">
                    <img src="${imgSrc}" alt="${title}" onerror="this.src='assets/images/placeholder.png'" class="st-img">
                </a>
                <div class="st-order-item-details">
                    <h6 class="st-order-item-title">
                        <a href="${productLink}">${title}</a>
                    </h6>
                    <div class="st-order-item-meta">
                        <span class="item-qty-pill">&times; ${it.qty || 1} ${it.qty > 1 ? 'items' : 'item'}</span>
                        <span>${unitPrice} each</span>
                    </div>
                </div>
                <div class="st-order-item-price">
                    ${lineTotal}
                </div>
            </div>
        `;
    }).join("");

    const deliveryType = ST.esc(order.deliveryType || "Standard Delivery");
    const address = order.shippingAddress && order.shippingAddress !== "—" ? ST.esc(order.shippingAddress) : "";
    const stripeTx = order.stripePaymentId && order.stripePaymentId !== "—" ? ST.esc(order.stripePaymentId) : "";

    return `
        <article class="st-order-card" id="order-card-${order.id}">
            <header class="st-order-header">
                <div class="st-order-meta-left">
                    <div class="st-order-no-wrap">
                        <span class="st-order-number">${orderNo}</span>
                        <span class="st-order-id-sub">ID: #${order.id}</span>
                    </div>
                    <div class="st-order-date">
                        <i class="fa-regular fa-calendar-day"></i>
                        <span>${dateStr}</span>
                    </div>
                </div>

                <div class="st-order-meta-right">
                    <div class="order-status-badge ${meta.cls}">
                        <span class="status-dot"></span>
                        <i class="${meta.icon}"></i>
                        <span>${meta.label}</span>
                    </div>
                    <div class="st-order-actions">
                        <button type="button" class="btn-table-action btn-table-view" onclick="viewOrderReceipt(${order.id})" title="View Official Receipt">
                            <i class="fa-regular fa-file-invoice"></i> Receipt
                        </button>
                        <button type="button" class="btn-table-action" onclick="printOrderReceipt(${order.id})" title="Print or Download PDF Receipt">
                            <i class="fa-regular fa-print"></i>
                        </button>
                    </div>
                </div>
            </header>

            <div class="st-order-items-wrap">
                <div class="st-order-items-list">
                    ${itemsHtml || '<p class="text-muted small mb-0">No item details available.</p>'}
                </div>
            </div>

            <footer class="st-order-footer">
                <div class="st-order-shipping-col">
                    <div class="shipping-info-item">
                        <i class="fa-regular fa-truck"></i>
                        <span><strong>Delivery:</strong> ${deliveryType}</span>
                    </div>
                    ${address ? `
                        <div class="shipping-info-item address-item">
                            <i class="fa-regular fa-location-dot"></i>
                            <span title="${address}"><strong>Ship to:</strong> ${address}</span>
                        </div>
                    ` : ''}
                    ${stripeTx ? `
                        <div class="shipping-info-item">
                            <i class="fa-brands fa-stripe"></i>
                            <span>Paid via Stripe Card &bull; Ref: <code class="mono" style="font-size:0.75rem">${stripeTx}</code></span>
                        </div>
                    ` : ''}
                </div>

                <div class="st-order-totals-col">
                    <div class="st-totals-breakdown">
                        <div class="totals-line">
                            <span>Subtotal:</span>
                            <span class="font-mono text-white">${ST.rs(order.subtotal || 0)}</span>
                        </div>
                        <div class="totals-line">
                            <span>Delivery Fee:</span>
                            <span class="font-mono text-white">${ST.rs(order.deliveryFee || 0)}</span>
                        </div>
                    </div>
                    <div class="st-grand-total">
                        <span class="grand-label">Grand Total</span>
                        <span class="grand-amount">${ST.rs(order.total || 0)}</span>
                    </div>
                </div>
            </footer>
        </article>
    `;
}

function buildFallbackReceipt(order) {
    const userFirstName = document.getElementById("firstName") ? document.getElementById("firstName").value : "";
    const userLastName = document.getElementById("lastName") ? document.getElementById("lastName").value : "";
    const fullName = (userFirstName + " " + userLastName).trim() || "Valued Customer";
    const mobile = document.getElementById("mobile") ? document.getElementById("mobile").value : "—";

    return {
        orderId: order.id,
        orderNumber: order.orderNumber || ("#ST-ORD-" + String(order.id || 0).padStart(5, "0")),
        createdAt: order.createdAt || "Recent",
        orderStatus: order.status || "PAID",
        stripePaymentId: order.stripePaymentId && order.stripePaymentId !== "—" ? order.stripePaymentId : "Processed via Stripe Gateway",
        paymentMethod: "Stripe (Card Payment)",
        customerName: fullName,
        customerEmail: "—",
        customerMobile: mobile,
        shippingAddress: order.shippingAddress || "Standard delivery address",
        deliveryType: order.deliveryType || "Standard Delivery",
        deliveryFee: Number(order.deliveryFee || 0),
        subtotal: Number(order.subtotal || 0),
        total: Number(order.total || 0),
        items: (order.items || []).map((it) => ({
            qty: it.qty || 1,
            unitPrice: it.unitPrice || 0,
            lineTotal: it.total || ((it.unitPrice || 0) * (it.qty || 1)),
            title: it.title || "Ordered Item",
            image: it.image || "assets/images/placeholder.png"
        }))
    };
}

async function viewOrderReceipt(orderId) {
    Notiflix.Loading.pulse("Loading official receipt...", loadingOpts);
    try {
        const response = await fetch(`api/checkout/receipt/${orderId}`, { credentials: "include" });
        if (response.ok) {
            const data = await response.json();
            if (data.status === "success") {
                if (window.STReceipt) {
                    window.STReceipt.show(data);
                } else {
                    Notiflix.Notify.warning("Receipt viewer component not loaded.");
                }
                return;
            }
        }
        // Fallback using stored raw order data
        const fallbackOrder = rawOrders.find((o) => o.id === orderId);
        if (fallbackOrder && window.STReceipt) {
            window.STReceipt.show(buildFallbackReceipt(fallbackOrder));
        } else {
            Notiflix.Notify.failure("Unable to retrieve receipt for this order.");
        }
    } catch (e) {
        const fallbackOrder = rawOrders.find((o) => o.id === orderId);
        if (fallbackOrder && window.STReceipt) {
            window.STReceipt.show(buildFallbackReceipt(fallbackOrder));
        } else {
            Notiflix.Notify.failure("Error loading receipt: " + e.message);
        }
    } finally {
        Notiflix.Loading.remove(300);
    }
}

async function printOrderReceipt(orderId) {
    Notiflix.Loading.pulse("Preparing printable receipt...", loadingOpts);
    try {
        const response = await fetch(`api/checkout/receipt/${orderId}`, { credentials: "include" });
        if (response.ok) {
            const data = await response.json();
            if (data.status === "success") {
                if (window.STReceipt) {
                    window.STReceipt.show(data);
                    setTimeout(() => window.STReceipt.print(), 350);
                }
                return;
            }
        }
        const fallbackOrder = rawOrders.find((o) => o.id === orderId);
        if (fallbackOrder && window.STReceipt) {
            window.STReceipt.show(buildFallbackReceipt(fallbackOrder));
            setTimeout(() => window.STReceipt.print(), 350);
        } else {
            Notiflix.Notify.failure("Unable to prepare print receipt.");
        }
    } catch (e) {
        const fallbackOrder = rawOrders.find((o) => o.id === orderId);
        if (fallbackOrder && window.STReceipt) {
            window.STReceipt.show(buildFallbackReceipt(fallbackOrder));
            setTimeout(() => window.STReceipt.print(), 350);
        } else {
            Notiflix.Notify.failure("Error printing receipt: " + e.message);
        }
    } finally {
        Notiflix.Loading.remove(300);
    }
}
