/* Home page: hero cards, ticker, brand tiles and new arrivals */
(function () {
    "use strict";
    const $ = ST.$;

    function heroCard(p) {
        const s = ST.stockOf(p);
        const qty = ST.totalQty(p);
        const stockTag = qty > 0
            ? '<span class="tag green">' + qty + " in stock</span>"
            : '<span class="tag">Sold out</span>';
        const priceStr = s ? ST.rs(s.price) : "";
        return '<a class="market-card" href="single-product.html?productId=' + p.productId + '">' +
            '<span class="th"><img class="st-img" src="' + ST.esc(ST.image(p, 0)) + '" alt="' + ST.esc(p.title) + '"></span>' +
            '<span><span class="meta"><i class="fa-solid fa-arrow-trend-up"></i> ' + ST.esc(p.brandName || "New") + " &middot; " + ST.esc(p.qualityValue || "Listed") + "</span>" +
            "<h5>" + ST.esc(p.title) + "</h5>" +
            '<span class="pl"><span class="price">' + priceStr + "</span>" + stockTag + "</span></span></a>";
    }

    function renderTicker(list) {
        const track = $("#tickerTrack");
        if (!track) return;
        let items;
        if (list.length) {
            items = list.slice(0, 10).map((p) => {
                const s = ST.stockOf(p);
                return '<span class="it"><b>' + ST.esc((p.brandName || "SMART").toUpperCase()) + "</b> " + ST.esc(p.modelName || p.title) + " " +
                    (s ? '<span class="up">' + ST.rs(s.price) + " &#9650;</span>" : "") + "</span>";
            });
        } else {
            items = ["Verified sellers", "24-hour returns", "Island-wide delivery", "Secure checkout", "Free listing for sellers"]
                .map((t) => '<span class="it"><b>' + t + '</b> <span class="up">&#9650;</span></span>');
        }
        const html = items.join("");
        track.innerHTML = html + html; // doubled for the seamless loop
    }

    function renderBrands(brands, list) {
        const rail = $("#brandRail");
        if (!rail) return;
        if (!brands.length) {
            rail.innerHTML = '<div class="empty-state" style="grid-column:1/-1"><div class="ic"><i class="fa-regular fa-tags"></i></div><h4>No brands yet</h4><p>Brands will show up here as soon as they are added.</p></div>';
            return;
        }
        rail.innerHTML = brands.map((b) => {
            const n = list.filter((p) => p.brandId === b.id).length;
            return '<a class="brand-tile" href="search.html?brand=' + b.id + '&name=' + encodeURIComponent(b.name) + '">' +
                '<span class="mono-mark">' + ST.esc((b.name || "?").charAt(0).toUpperCase()) + "</span>" +
                "<h6>" + ST.esc(b.name) + "</h6><small>" + n + (n === 1 ? " listing" : " listings") + "</small></a>";
        }).join("");
    }

    async function load() {
        const grid = $("#newArrivals");
        if (grid) grid.innerHTML = ST.skeletonCards(4);

        let list = [];
        let brands = [];
        try {
            list = await ST.catalogue();
        } catch (e) {
            ST.toast("failure", e.message || "Product data loading failed!");
            if (grid) grid.innerHTML = '<div class="empty-state" style="grid-column:1/-1"><div class="ic"><i class="fa-regular fa-triangle-exclamation"></i></div><h4>We could not load products</h4><p>Check your connection and try again.</p><button class="st-btn" onclick="location.reload()">Retry</button></div>';
        }
        try {
            const r = await fetch("api/data/brands");
            if (r.ok) brands = (await r.json()).brands || [];
        } catch (e) { /* tiles fall back to the empty state */ }

        const statListings = $("#statListings");
        if (statListings) statListings.textContent = list.length;
        const statBrands = $("#statBrands");
        if (statBrands) statBrands.textContent = brands.length;

        renderTicker(list);
        renderBrands(brands, list);

        const hero = $("#heroMarket");
        if (hero) {
            hero.innerHTML = list.length
                ? list.slice(0, 3).map(heroCard).join("")
                : '<div class="market-empty"><i class="fa-regular fa-mobile-screen-button fa-2x"></i><p class="mt-3 mb-0">Fresh listings will appear here.</p></div>';
        }

        if (grid) {
            if (list.length) {
                grid.innerHTML = list.slice(0, 8).map((p) => ST.productCard(p, {badge: true})).join("");
            } else if (!grid.querySelector(".empty-state")) {
                grid.innerHTML = '<div class="empty-state" style="grid-column:1/-1"><div class="ic"><i class="fa-regular fa-box-open"></i></div><h4>Nothing listed yet</h4><p>Be the first to list a phone on SmartTrade.</p><a class="st-btn" href="my-account.html#products">List a phone</a></div>';
            }
        }
    }

    load();
})();
