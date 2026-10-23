/* ==========================================================================
   SmartTrade - shared front-end runtime
   - helpers (escape / sanitise / money / api)
   - catalogue cache (api/data/products)
   - cart + wishlist store (localStorage)
   - header / drawer / search overlay behaviour
   ========================================================================== */
(function () {
    "use strict";
    document.documentElement.classList.add("js");

    const ST = (window.ST = window.ST || {});
    const BRAND_GREEN = "#2bd97f";

    /* ---------- helpers ---------- */
    ST.esc = (value) =>
        String(value == null ? "" : value).replace(/[&<>"']/g, (c) => ({
            "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
        }[c]));

    ST.money = (n) =>
        new Intl.NumberFormat("en-US", {minimumFractionDigits: 2, maximumFractionDigits: 2}).format(Number(n) || 0);
    ST.rs = (n) => "Rs. " + ST.money(n);

    ST.$ = (sel, root) => (root || document).querySelector(sel);
    ST.$$ = (sel, root) => Array.from((root || document).querySelectorAll(sel));

    ST.PLACEHOLDER =
        "data:image/svg+xml;utf8," +
        encodeURIComponent(
            "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 200 200'><rect width='200' height='200' fill='#eef2f0'/>" +
            "<rect x='66' y='30' width='68' height='140' rx='14' fill='none' stroke='#9fb1ab' stroke-width='6'/>" +
            "<circle cx='100' cy='150' r='5' fill='#9fb1ab'/></svg>"
        );

    /* images: never leave a broken icon on screen */
    document.addEventListener("error", function (e) {
        const t = e.target;
        if (t && t.tagName === "IMG" && t.dataset.fallback !== "1" && t.classList.contains("st-img")) {
            t.dataset.fallback = "1";
            t.src = ST.PLACEHOLDER;
        }
    }, true);

    /* html coming from the seller's rich text editor is never trusted */
    ST.sanitize = function (html) {
        const allowed = new Set(["P", "BR", "B", "STRONG", "I", "EM", "U", "S", "UL", "OL", "LI", "H1", "H2", "H3", "H4", "H5", "H6",
            "A", "SPAN", "DIV", "BLOCKQUOTE", "CODE", "PRE", "TABLE", "THEAD", "TBODY", "TR", "TD", "TH", "HR", "SUB", "SUP", "SMALL"]);
        const doc = new DOMParser().parseFromString("<div>" + String(html == null ? "" : html) + "</div>", "text/html");
        const root = doc.body.firstChild;
        (function walk(node) {
            Array.from(node.childNodes).forEach((child) => {
                if (child.nodeType === 8) {
                    child.remove();
                } else if (child.nodeType === 1) {
                    if (!allowed.has(child.tagName)) {
                        if (["SCRIPT", "STYLE", "IFRAME", "OBJECT", "EMBED", "NOSCRIPT", "TEMPLATE", "SVG", "MATH"].includes(child.tagName)) {
                            child.remove();
                            return;
                        }
                        walk(child);
                        child.replaceWith(...Array.from(child.childNodes));
                        return;
                    }
                    Array.from(child.attributes).forEach((attr) => {
                        const name = attr.name.toLowerCase();
                        if (child.tagName === "A" && name === "href" && /^(https?:|mailto:)/i.test(attr.value.trim())) return;
                        child.removeAttribute(attr.name);
                    });
                    if (child.tagName === "A") {
                        child.setAttribute("rel", "noopener noreferrer nofollow");
                        child.setAttribute("target", "_blank");
                    }
                    walk(child);
                }
            });
        })(root);
        return root.innerHTML;
    };

    ST.stripHtml = (html) => {
        const d = document.createElement("div");
        d.innerHTML = ST.sanitize(html);
        return (d.textContent || "").trim();
    };

    /* ---------- notifications ---------- */
    ST.initNotiflix = function () {
        if (typeof Notiflix === "undefined") return;
        Notiflix.Notify.init({
            position: "right-top", distance: "18px", borderRadius: "14px", timeout: 3200, clickToClose: true,
            fontFamily: "Segoe UI, system-ui, sans-serif", fontSize: "14px", cssAnimationStyle: "from-right", showOnlyTheLastOne: false,
            success: {background: "#12261c", textColor: "#d9fbe8", childClassName: "notiflix-notify-success", notiflixIconColor: BRAND_GREEN, fontAwesomeClassName: "fas fa-check-circle", fontAwesomeIconColor: BRAND_GREEN, backOverlayColor: "rgba(43,217,127,.2)"},
            failure: {background: "#2a1519", textColor: "#ffd9dd", childClassName: "notiflix-notify-failure", notiflixIconColor: "#ff6b78", fontAwesomeClassName: "fas fa-times-circle", fontAwesomeIconColor: "#ff6b78", backOverlayColor: "rgba(255,107,120,.2)"},
            warning: {background: "#2a2212", textColor: "#ffecc2", childClassName: "notiflix-notify-warning", notiflixIconColor: "#f6bb4f", fontAwesomeClassName: "fas fa-exclamation-circle", fontAwesomeIconColor: "#f6bb4f", backOverlayColor: "rgba(246,187,79,.2)"},
            info: {background: "#11232c", textColor: "#d3f0ff", childClassName: "notiflix-notify-info", notiflixIconColor: "#62c7ff", fontAwesomeClassName: "fas fa-info-circle", fontAwesomeIconColor: "#62c7ff", backOverlayColor: "rgba(98,199,255,.2)"}
        });
        Notiflix.Loading.init({backgroundColor: "rgba(6,10,13,.78)", svgColor: BRAND_GREEN, messageColor: "#b6c4cc", messageFontSize: "14px", clickToClose: false});
        Notiflix.Report.init({
            backgroundColor: "#141d24", borderRadius: "22px", messageFontSize: "15px", titleFontSize: "20px", titleMaxLength: 60, messageMaxLength: 1000,
            plainText: false, svgSize: "72px", cssAnimationStyle: "zoom",
            success: {svgColor: BRAND_GREEN, titleColor: "#eef4f1", messageColor: "#b6c4cc", buttonBackground: BRAND_GREEN, buttonColor: "#03140a", backOverlayColor: "rgba(5,9,12,.78)"},
            failure: {svgColor: "#ff6b78", titleColor: "#eef4f1", messageColor: "#b6c4cc", buttonBackground: "#ff6b78", buttonColor: "#2a0d11", backOverlayColor: "rgba(5,9,12,.78)"},
            warning: {svgColor: "#f6bb4f", titleColor: "#eef4f1", messageColor: "#b6c4cc", buttonBackground: "#f6bb4f", buttonColor: "#2a1d05", backOverlayColor: "rgba(5,9,12,.78)"},
            info: {svgColor: "#62c7ff", titleColor: "#eef4f1", messageColor: "#b6c4cc", buttonBackground: "#62c7ff", buttonColor: "#04202e", backOverlayColor: "rgba(5,9,12,.78)"}
        });
    };
    ST.toast = (type, msg) => {
        if (typeof Notiflix !== "undefined") Notiflix.Notify[type](msg);
    };

    /* ---------- api ---------- */
    ST.api = async function (url, options) {
        const response = await fetch(url, Object.assign({credentials: "same-origin"}, options || {}));
        if (!response.ok) throw new Error("Request failed (" + response.status + ")");
        const type = response.headers.get("content-type") || "";
        if (response.redirected || type.indexOf("json") === -1) {
            const err = new Error("Not signed in");
            err.redirected = true;
            throw err;
        }
        return response.json();
    };

    /* ---------- catalogue ---------- */
    let cataloguePromise = null;
    const registry = new Map();
    ST.catalogue = function () {
        if (!cataloguePromise) {
            cataloguePromise = fetch("api/data/products", {credentials: "same-origin"})
                .then((r) => {
                    if (!r.ok) throw new Error("Product data loading failed!");
                    return r.json();
                })
                .then((d) => {
                    const list = d.products || [];
                    list.forEach((p) => registry.set(Number(p.productId), p));
                    return list;
                })
                .catch((err) => {
                    cataloguePromise = null;
                    throw err;
                });
        }
        return cataloguePromise;
    };
    ST.registerProducts = (list) => (list || []).forEach((p) => registry.set(Number(p.productId), p));
    ST.product = (id) => registry.get(Number(id));

    /* the stock row we sell from: cheapest in-stock row, otherwise the first one */
    ST.stockOf = function (p) {
        const stocks = (p && p.stockDTOList) || [];
        if (!stocks.length) return null;
        const inStock = stocks.filter((s) => s.qty > 0).sort((a, b) => a.price - b.price);
        return inStock[0] || stocks[0];
    };
    ST.totalQty = (p) => ((p && p.stockDTOList) || []).reduce((n, s) => n + (s.qty || 0), 0);
    ST.cleanImgUrl = (url) => {
        if (!url) return ST.PLACEHOLDER;
        if (typeof url === "string") {
            const normalized = url.replace(/\\/g, "/");
            const uIdx = normalized.indexOf("/uploads/");
            if (uIdx !== -1) {
                return normalized.substring(uIdx + 1);
            }
            if (normalized.startsWith("uploads/")) {
                return normalized;
            }
            return normalized;
        }
        return url;
    };
    ST.image = (p, i) => {
        const imgs = (p && p.images) || [];
        const raw = imgs[i] || imgs[0];
        return ST.cleanImgUrl(raw);
    };

    /* ---------- cart / wishlist store ---------- */
    function activeUserId() {
        if (ST.session && ST.session.loggedIn && ST.session.userId) {
            return ST.session.userId;
        }
        const cached = sessionStorage.getItem("st.session_uid");
        if (cached && !isNaN(Number(cached))) {
            return Number(cached);
        }
        return null;
    }

    function cartKey() {
        const uid = activeUserId();
        return uid ? "st.cart.u_" + uid : "st.cart.guest";
    }

    function wishKey() {
        const uid = activeUserId();
        return uid ? "st.wish.u_" + uid : "st.wish.guest";
    }

    // Migrate legacy non-scoped storage once for guests if present
    try {
        if (!localStorage.getItem("st.cart.guest") && localStorage.getItem("st.cart")) {
            localStorage.setItem("st.cart.guest", localStorage.getItem("st.cart"));
            localStorage.removeItem("st.cart");
        }
        if (!localStorage.getItem("st.wish.guest") && localStorage.getItem("st.wish")) {
            localStorage.setItem("st.wish.guest", localStorage.getItem("st.wish"));
            localStorage.removeItem("st.wish");
        }
    } catch (e) {}

    function read(key) {
        try {
            const v = JSON.parse(localStorage.getItem(key) || "[]");
            return Array.isArray(v) ? v : [];
        } catch (e) {
            return [];
        }
    }
    function write(key, value) {
        try {
            localStorage.setItem(key, JSON.stringify(value));
        } catch (e) { /* private mode - keep going without persistence */ }
        document.dispatchEvent(new CustomEvent("st:store"));
    }

    ST.cart = {
        items: () => read(cartKey()),
        count: () => read(cartKey()).reduce((n, i) => n + i.qty, 0),
        total: () => read(cartKey()).reduce((n, i) => n + i.qty * i.price, 0),
        add(product, qty) {
            const stock = ST.stockOf(product);
            if (!stock || stock.qty <= 0) {
                ST.toast("failure", "This product is out of stock.");
                return false;
            }
            const key = cartKey();
            const items = read(key);
            const found = items.find((i) => i.stockId === stock.stockId);
            const wanted = (found ? found.qty : 0) + (qty || 1);
            if (wanted > stock.qty) {
                ST.toast("warning", "Only " + stock.qty + " unit(s) available.");
                return false;
            }
            if (found) {
                found.qty = wanted;
                found.max = stock.qty;
                found.price = stock.price;
            } else {
                items.push({
                    productId: product.productId, stockId: stock.stockId, title: product.title,
                    brand: product.brandName || "", spec: [product.storageValue, product.qualityValue].filter(Boolean).join(" · "),
                    price: stock.price, qty: qty || 1, max: stock.qty, image: ST.image(product, 0)
                });
            }
            write(key, items);

            if (activeUserId()) {
                fetch("api/cart", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    credentials: "same-origin",
                    body: JSON.stringify({stockId: stock.stockId, qty: qty || 1})
                }).catch(() => {});
            }
            return true;
        },
        setQty(stockId, qty) {
            const key = cartKey();
            const items = read(key);
            const item = items.find((i) => i.stockId === stockId);
            if (!item) return;
            qty = Math.max(1, Math.min(parseInt(qty, 10) || 1, item.max || 99));
            item.qty = qty;
            write(key, items);

            if (activeUserId()) {
                fetch("api/cart", {
                    method: "PUT",
                    headers: {"Content-Type": "application/json"},
                    credentials: "same-origin",
                    body: JSON.stringify({stockId: stockId, qty: qty})
                }).catch(() => {});
            }
        },
        remove(stockId) {
            const key = cartKey();
            write(key, read(key).filter((i) => i.stockId !== stockId));

            if (activeUserId()) {
                fetch("api/cart/" + stockId, {
                    method: "DELETE",
                    credentials: "same-origin"
                }).catch(() => {});
            }
        },
        clear() {
            const key = cartKey();
            write(key, []);

            if (activeUserId()) {
                fetch("api/cart", {
                    method: "DELETE",
                    credentials: "same-origin"
                }).catch(() => {});
            }
        }
    };

    ST.wish = {
        ids: () => read(wishKey()),
        has: (id) => read(wishKey()).includes(Number(id)),
        toggle(id) {
            id = Number(id);
            const key = wishKey();
            const ids = read(key);
            const i = ids.indexOf(id);
            if (i >= 0) ids.splice(i, 1); else ids.push(id);
            write(key, ids);

            if (activeUserId()) {
                fetch("api/wishlist/toggle/" + id, {
                    method: "POST",
                    credentials: "same-origin"
                }).catch(() => {});
            }
            return i < 0;
        }
    };

    /* ---------- product card ---------- */
    ST.productCard = function (p, opts) {
        opts = opts || {};
        const stock = ST.stockOf(p);
        const qty = ST.totalQty(p);
        const out = !stock || qty <= 0;
        const mainImg = ST.image(p, 0);
        const rawAlt = (p.images && p.images.length > 1) ? p.images[1] : null;
        const cleanedAlt = rawAlt ? ST.cleanImgUrl(rawAlt) : null;
        const alt = (cleanedAlt && cleanedAlt !== mainImg) ? cleanedAlt : null;
        const wished = ST.wish.has(p.productId);
        const isNew = opts.badge;
        return (
            '<article class="p-card' + (out ? " out" : "") + '" data-pid="' + p.productId + '">' +
            '<a class="p-media' + (alt ? " has-alt" : "") + '" href="single-product.html?productId=' + p.productId + '" aria-label="' + ST.esc(p.title) + '">' +
            '<img class="st-img main" loading="lazy" src="' + ST.esc(mainImg) + '" alt="' + ST.esc(p.title) + '">' +
            (alt ? '<img class="st-img alt" loading="lazy" src="' + ST.esc(alt) + '" alt="">' : "") +
            "</a>" +
            '<div class="p-flags">' + (isNew ? '<span class="tag green">New</span>' : "") + (out ? '<span class="tag">Sold out</span>' : "") +
            (p.qualityValue ? '<span class="tag">' + ST.esc(p.qualityValue) + "</span>" : "") + "</div>" +
            '<button class="p-wish' + (wished ? " on" : "") + '" type="button" data-wish="' + p.productId + '" aria-label="Add to wishlist"><i class="fa-' + (wished ? "solid" : "regular") + ' fa-heart"></i></button>' +
            '<div class="p-body">' +
            '<span class="p-brand">' + ST.esc(p.brandName || "SmartTrade") + "</span>" +
            '<h3 class="p-title"><a href="single-product.html?productId=' + p.productId + '">' + ST.esc(p.title) + "</a></h3>" +
            '<div class="p-specs">' +
            (p.colorValue ? '<span class="dot" title="' + ST.esc(p.colorValue) + '" style="background:' + ST.esc(p.colorValue) + '"></span>' : "") +
            (p.storageValue ? '<span class="tag">' + ST.esc(p.storageValue) + "</span>" : "") + "</div>" +
            '<div class="p-foot"><div class="p-price">' + (stock ? "<small>Rs.</small>" + ST.money(stock.price) : "N/A") + "</div>" +
            '<button class="p-add" type="button" data-add="' + p.productId + '" aria-label="Add to cart"' + (out ? " disabled" : "") + '><i class="fa-regular fa-bag-shopping"></i></button></div>' +
            "</div></article>"
        );
    };

    ST.skeletonCards = (n) =>
        Array.from({length: n}, () => '<div class="skeleton" style="aspect-ratio:3/4.1;border-radius:22px"></div>').join("");

    /* global delegation for add / wish buttons */
    document.addEventListener("click", function (e) {
        const add = e.target.closest("[data-add]");
        if (add) {
            e.preventDefault();
            const p = ST.product(add.dataset.add);
            if (p && ST.cart.add(p, 1)) ST.openCart(); // the drawer is the confirmation
            return;
        }
        const wish = e.target.closest("[data-wish]");
        if (wish) {
            e.preventDefault();
            const on = ST.wish.toggle(wish.dataset.wish);
            ST.toast(on ? "success" : "info", on ? "Saved to your wishlist" : "Removed from wishlist");
        }
    });

    /* keep every card heart in sync */
    function syncWishButtons() {
        ST.$$("[data-wish]").forEach((b) => {
            const on = ST.wish.has(b.dataset.wish);
            b.classList.toggle("on", on);
            const i = b.querySelector("i");
            if (i) i.className = "fa-" + (on ? "solid" : "regular") + " fa-heart";
        });
    }

    /* ---------- chrome (header / drawer / search) ---------- */
    let overlay;
    function ensureOverlay() {
        if (!overlay) {
            overlay = ST.$(".st-overlay");
            if (!overlay && document.body) {
                overlay = document.createElement("div");
                overlay.className = "st-overlay";
                overlay.setAttribute("data-close", "");
                document.body.appendChild(overlay);
            }
        }
        return overlay;
    }
    function closeAll() {
        ST.$$(".st-mobile.open,.st-drawer.open,.filters.open").forEach((el) => el.classList.remove("open"));
        const ov = ensureOverlay();
        if (ov) ov.classList.remove("open");
        document.body.style.overflow = "";
    }
    ST.openCart = function () {
        renderDrawer();
        const d = ST.$("#stDrawer");
        if (!d) return;
        d.classList.add("open");
        const ov = ensureOverlay();
        if (ov) ov.classList.add("open");
        document.body.style.overflow = "hidden";
    };
    window.viewCart = ST.openCart; // used by older markup

    function renderDrawer() {
        const body = ST.$("#stDrawerBody");
        if (!body) return;
        const items = ST.cart.items();
        if (!items.length) {
            body.innerHTML = '<div class="empty-state" style="border:0;background:none;padding:3rem 0"><div class="ic"><i class="fa-regular fa-bag-shopping"></i></div><h4>Your cart is empty</h4><p>Browse the latest phones and add your favourites.</p><a class="st-btn" href="search.html">Start shopping</a></div>';
        } else {
            body.innerHTML = items.map((i) =>
                '<div class="mini-row"><a class="th" href="single-product.html?productId=' + i.productId + '"><img class="st-img" src="' + ST.esc(i.image) + '" alt=""></a>' +
                '<div><b>' + ST.esc(i.title) + '</b><small>' + i.qty + " × " + ST.rs(i.price) + '</small></div>' +
                '<button class="rm" type="button" data-rm="' + i.stockId + '" aria-label="Remove"><i class="fa-regular fa-trash"></i></button></div>'
            ).join("");
        }
        const sub = ST.$("#stDrawerTotal");
        if (sub) sub.textContent = ST.rs(ST.cart.total());
        const foot = ST.$("#stDrawerFoot");
        if (foot) foot.style.display = items.length ? "" : "none";
    }

    function updateBadges() {
        const c = ST.cart.count();
        const w = ST.wish.ids().length;
        const cb = ST.$("#stCartBadge");
        const wb = ST.$("#stWishBadge");
        if (cb) { cb.textContent = c || ""; cb.dataset.n = c; }
        if (wb) { wb.textContent = w || ""; wb.dataset.n = w; }
        if (ST.$("#stDrawer.open")) renderDrawer();
        syncWishButtons();
    }
    document.addEventListener("st:store", updateBadges);
    window.addEventListener("storage", updateBadges);

    /* search overlay */
    let searchTimer;
    function openSearch() {
        const s = ST.$("#stSearch");
        if (!s) return;
        closeAll();
        s.classList.add("open");
        setTimeout(() => {
            const inp = ST.$("#stSearchInput");
            if (inp) inp.focus();
        }, 60);
        ST.catalogue().catch(() => {});
    }
    function closeSearch() {
        const s = ST.$("#stSearch");
        if (s) s.classList.remove("open");
    }
    function runLiveSearch(q) {
        const box = ST.$("#stSearchResults");
        q = q.trim().toLowerCase();
        if (!q) {
            box.innerHTML = '<div class="hint">Type a brand, model or keyword. Press <b>Enter</b> to see every match.</div>';
            return;
        }
        ST.catalogue().then((list) => {
            const hits = list.filter((p) =>
                [p.title, p.brandName, p.modelName, p.storageValue, p.colorValue].join(" ").toLowerCase().includes(q)).slice(0, 6);
            if (!hits.length) {
                box.innerHTML = '<div class="hint">No phones match "' + ST.esc(q) + '".</div>';
                return;
            }
            box.innerHTML = hits.map((p) => {
                const s = ST.stockOf(p);
                return '<a class="sr-item" href="single-product.html?productId=' + p.productId + '"><span class="th"><img class="st-img" src="' + ST.esc(ST.image(p, 0)) + '" alt=""></span>' +
                    '<span><b>' + ST.esc(p.title) + '</b><small>' + ST.esc(p.brandName || "") + " · " + ST.esc(p.storageValue || "") + "</small></span>" +
                    '<span class="pr">' + (s ? ST.rs(s.price) : "") + "</span></a>";
            }).join("");
        }).catch(() => {
            box.innerHTML = '<div class="hint">Search is unavailable right now.</div>';
        });
    }

    /* session aware account menu */
    function applySession(data) {
        const menu = ST.$("#stAccountMenu");
        if (!menu) return;
        if (data && data.loggedIn) {
            const avatar = data.profileImage || "assets/images/avatar.png";
            menu.innerHTML =
                '<div class="who" style="display:flex;align-items:center;gap:.75rem">' +
                '<img src="' + ST.esc(avatar) + '" alt="" style="width:36px;height:36px;border-radius:50%;object-fit:cover;border:1px solid var(--st-brand-line);flex-shrink:0">' +
                '<div style="min-width:0;overflow:hidden"><b style="display:block;text-overflow:ellipsis;overflow:hidden;white-space:nowrap">' + ST.esc(data.name) + "</b><span style=\"display:block;text-overflow:ellipsis;overflow:hidden;white-space:nowrap\">" + ST.esc(data.email) + "</span></div></div>" +
                '<a href="my-account.html"><i class="fa-regular fa-user"></i> My account</a>' +
                '<a href="my-account.html#orders"><i class="fa-regular fa-receipt"></i> My orders</a>' +
                '<a href="my-account.html#listings"><i class="fa-regular fa-list"></i> My listings</a>' +
                '<a href="my-account.html#products"><i class="fa-regular fa-store"></i> Sell a phone</a>' +
                '<a href="cart.html"><i class="fa-regular fa-bag-shopping"></i> My cart</a>' +
                '<button class="item" type="button" data-logout><i class="fa-regular fa-right-from-bracket"></i> Sign out</button>';
            ST.$$("[data-guest]").forEach((el) => (el.hidden = true));
        } else {
            menu.innerHTML =
                '<div class="guest-cta"><a class="st-btn st-btn-block" href="sign-in.html">Sign in</a>' +
                '<a class="st-btn st-btn-ghost st-btn-block" href="sign-up.html">Create account</a></div>';
        }
    }
    ST.session = null;
    ST.sessionReady = fetch("api/users/session", {credentials: "same-origin"})
        .then((r) => (r.ok ? r.json() : {loggedIn: false}))
        .catch(() => ({loggedIn: false}))
        .then((d) => {
            ST.session = d;
            if (d && d.loggedIn && d.userId) {
                sessionStorage.setItem("st.session_uid", d.userId);
                // Merge guest cart items into user cart on sign-in
                try {
                    const guestItems = read("st.cart.guest");
                    if (guestItems && guestItems.length > 0) {
                        const uKey = "st.cart.u_" + d.userId;
                        const uItems = read(uKey);
                        guestItems.forEach((gi) => {
                            const ex = uItems.find((ui) => ui.stockId === gi.stockId);
                            if (ex) {
                                ex.qty = Math.min((ex.qty || 1) + (gi.qty || 1), ex.max || 99);
                            } else {
                                uItems.push(gi);
                            }
                            fetch("api/cart", {
                                method: "POST",
                                headers: {"Content-Type": "application/json"},
                                credentials: "same-origin",
                                body: JSON.stringify({stockId: gi.stockId, qty: gi.qty || 1})
                            }).catch(() => {});
                        });
                        write(uKey, uItems);
                        localStorage.removeItem("st.cart.guest");
                    }
                } catch (_) {}

                // Sync cart from database
                fetch("api/cart", {credentials: "same-origin"})
                    .then(r => r.ok ? r.json() : null)
                    .then(res => {
                        if (res && Array.isArray(res.items)) {
                            localStorage.setItem(cartKey(), JSON.stringify(res.items));
                            document.dispatchEvent(new CustomEvent("st:store"));
                        }
                    }).catch(() => {});
                // Sync wishlist from database
                fetch("api/wishlist", {credentials: "same-origin"})
                    .then(r => r.ok ? r.json() : null)
                    .then(res => {
                        if (res && Array.isArray(res.ids)) {
                            localStorage.setItem(wishKey(), JSON.stringify(res.ids));
                            document.dispatchEvent(new CustomEvent("st:store"));
                        }
                    }).catch(() => {});
            } else {
                sessionStorage.removeItem("st.session_uid");
                document.dispatchEvent(new CustomEvent("st:store"));
            }
            return d;
        });

    document.addEventListener("click", function (e) {
        const t = e.target;
        if (t.closest("[data-open-menu]")) { closeAll(); ST.$(".st-mobile").classList.add("open"); ensureOverlay().classList.add("open"); document.body.style.overflow = "hidden"; return; }
        if (t.closest("[data-open-search]")) { openSearch(); return; }
        if (t.closest("[data-open-cart]")) { e.preventDefault(); ST.openCart(); return; }
        if (t.closest("[data-close]")) { closeAll(); closeSearch(); return; }
        if (t.closest("[data-open-filters]")) { const f = ST.$(".filters"); if (f) { f.classList.add("open"); ensureOverlay().classList.add("open"); } return; }
        const rm = t.closest("[data-rm]");
        if (rm) { ST.cart.remove(Number(rm.dataset.rm)); return; }
        if (t.closest("[data-logout]")) {
            fetch("api/users/logout", {credentials: "same-origin"}).finally(() => {
                sessionStorage.removeItem("st.session_uid");
                ST.session = {loggedIn: false};
                document.dispatchEvent(new CustomEvent("st:store"));
                window.location = "sign-in.html";
            });
            return;
        }
        const acct = ST.$(".st-account");
        if (acct) {
            if (t.closest("[data-account-toggle]")) acct.classList.toggle("open");
            else if (!t.closest(".st-account-menu")) acct.classList.remove("open");
        }
        if (t.id === "stSearch") closeSearch();
        if (t.classList && t.classList.contains("st-overlay")) closeAll();
        if (t.closest("#backToTop")) { e.preventDefault(); window.scrollTo({top: 0, behavior: "smooth"}); }
    });

    document.addEventListener("keydown", function (e) {
        if (e.key === "Escape") { closeAll(); closeSearch(); }
        const typing = /INPUT|TEXTAREA|SELECT/.test((document.activeElement || {}).tagName || "") || (document.activeElement || {}).isContentEditable;
        if ((e.key === "/" && !typing) || ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === "k")) {
            if (ST.$("#stSearch")) { e.preventDefault(); openSearch(); }
        }
    });

    document.addEventListener("input", function (e) {
        if (e.target.id === "stSearchInput") {
            clearTimeout(searchTimer);
            const v = e.target.value;
            searchTimer = setTimeout(() => runLiveSearch(v), 120);
        }
    });
    document.addEventListener("submit", function (e) {
        if (e.target.id === "stSearchForm") {
            e.preventDefault();
            const q = ST.$("#stSearchInput").value.trim();
            window.location = "search.html" + (q ? "?q=" + encodeURIComponent(q) : "");
        }
    });

    function init() {
        ST.initNotiflix();
        const header = ST.$("#stHeader");
        const btt = ST.$("#backToTop");
        const onScroll = () => {
            if (header) header.classList.toggle("is-scrolled", window.scrollY > 8);
            if (btt) btt.classList.toggle("show", window.scrollY > 600);
        };
        onScroll();
        window.addEventListener("scroll", onScroll, {passive: true});

        const path = (location.pathname.split("/").pop() || "index.html").toLowerCase();
        ST.$$(".st-nav a, .st-mobile nav a").forEach((a) => {
            const href = (a.getAttribute("href") || "").split("#")[0].split("?")[0].toLowerCase();
            if (href && href === path && !a.getAttribute("href").includes("#") && !a.getAttribute("href").includes("?")) a.classList.add("active");
        });

        updateBadges();
        ST.sessionReady.then(applySession);
    }

    if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", init); else init();
})();

/* password show / hide toggles (auth pages and forms) */
document.addEventListener("click", function (e) {
    const t = e.target.closest("[data-toggle-pass]");
    if (!t) return;
    const input = document.getElementById(t.dataset.togglePass);
    if (!input) return;
    const show = input.type === "password";
    input.type = show ? "text" : "password";
    t.innerHTML = '<i class="fa-regular fa-eye' + (show ? "-slash" : "") + '"></i>';
});
