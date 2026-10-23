/* Shop / advanced search : client side filters, sorting and pagination over api/data/products */
(function () {
    "use strict";
    const $ = ST.$;
    const PAGE_SIZE = 9;
    const state = {q: "", brands: new Set(), qualities: new Set(), colors: new Set(), storages: new Set(), min: null, max: null, sort: "latest", page: 1, wish: false};
    let all = [];

    const priceOf = (p) => {
        const s = ST.stockOf(p);
        return s ? s.price : Infinity;
    };

    /* ---- state <-> url ---- */
    function readUrl() {
        const u = new URLSearchParams(location.search);
        state.q = u.get("q") || "";
        state.wish = u.get("wishlist") === "1";
        if (u.get("brand")) u.get("brand").split(",").forEach((b) => state.brands.add(Number(b)));
        state.sort = u.get("sort") || "latest";
        state.page = parseInt(u.get("page"), 10) || 1;
    }
    function writeUrl() {
        const u = new URLSearchParams();
        if (state.q) u.set("q", state.q);
        if (state.wish) u.set("wishlist", "1");
        if (state.brands.size) u.set("brand", Array.from(state.brands).join(","));
        if (state.sort !== "latest") u.set("sort", state.sort);
        if (state.page > 1) u.set("page", state.page);
        history.replaceState(null, "", location.pathname + (u.toString() ? "?" + u : ""));
    }

    /* ---- filter options ---- */
    function counts(list, keyFn) {
        const m = new Map();
        list.forEach((p) => {
            const k = keyFn(p);
            if (k !== undefined && k !== null && k !== "") m.set(k, (m.get(k) || 0) + 1);
        });
        return m;
    }
    function renderOptions() {
        const brands = new Map();
        all.forEach((p) => brands.set(p.brandId, p.brandName));
        const bc = counts(all, (p) => p.brandId);
        $("#fBrand").innerHTML = Array.from(brands).sort((a, b) => String(a[1]).localeCompare(b[1])).map(([id, name]) =>
            '<span class="chip' + (state.brands.has(id) ? " on" : "") + '" data-g="brands" data-v="' + id + '">' + ST.esc(name) + " <small>" + bc.get(id) + "</small></span>").join("") || '<small class="text-secondary">No brands</small>';

        const simple = (sel, group, keyFn) => {
            const c = counts(all, keyFn);
            $(sel).innerHTML = Array.from(c).sort((a, b) => String(a[0]).localeCompare(b[0], undefined, {numeric: true})).map(([v, n]) =>
                '<span class="chip' + (state[group].has(v) ? " on" : "") + '" data-g="' + group + '" data-v="' + ST.esc(v) + '">' + ST.esc(v) + " <small>" + n + "</small></span>").join("") || '<small class="text-secondary">&ndash;</small>';
        };
        simple("#fQuality", "qualities", (p) => p.qualityValue);
        simple("#fStorage", "storages", (p) => p.storageValue);

        const cc = counts(all, (p) => p.colorValue);
        $("#fColor").innerHTML = Array.from(cc.keys()).map((v) =>
            '<span class="chip' + (state.colors.has(v) ? " on" : "") + '" data-g="colors" data-v="' + ST.esc(v) + '"><span class="swatch" style="width:14px;height:14px;background:' + ST.esc(v) + '"></span>' + ST.esc(v) + "</span>").join("") || '<small class="text-secondary">&ndash;</small>';

        const prices = all.map(priceOf).filter(isFinite);
        if (prices.length) {
            $("#fMin").title = "Lowest price: " + ST.rs(Math.min(...prices));
            $("#fMax").title = "Highest price: " + ST.rs(Math.max(...prices));
            $("#fRange").textContent = "Listings range from " + ST.rs(Math.min(...prices)) + " to " + ST.rs(Math.max(...prices));
        }
    }

    /* ---- filtering ---- */
    function filtered() {
        const q = state.q.trim().toLowerCase();
        const wished = new Set(ST.wish.ids());
        let list = all.filter((p) => {
            if (state.wish && !wished.has(Number(p.productId))) return false;
            if (q && ![p.title, p.brandName, p.modelName, p.storageValue, p.colorValue, p.qualityValue].join(" ").toLowerCase().includes(q)) return false;
            if (state.brands.size && !state.brands.has(p.brandId)) return false;
            if (state.qualities.size && !state.qualities.has(p.qualityValue)) return false;
            if (state.colors.size && !state.colors.has(p.colorValue)) return false;
            if (state.storages.size && !state.storages.has(p.storageValue)) return false;
            const price = priceOf(p);
            if (state.min != null && price < state.min) return false;
            if (state.max != null && price > state.max) return false;
            return true;
        });
        if (state.sort === "price-asc") list = list.slice().sort((a, b) => priceOf(a) - priceOf(b));
        else if (state.sort === "price-desc") list = list.slice().sort((a, b) => priceOf(b) - priceOf(a));
        else if (state.sort === "name") list = list.slice().sort((a, b) => a.title.localeCompare(b.title));
        return list;
    }

    function renderActive() {
        const pills = [];
        const add = (label, group, v) => pills.push('<span class="chip">' + ST.esc(label) + ' <button type="button" data-x="' + group + '" data-v="' + ST.esc(v) + '" aria-label="Remove filter"><i class="fa-regular fa-xmark"></i></button></span>');
        state.brands.forEach((id) => { const p = all.find((x) => x.brandId === id); add(p ? p.brandName : "Brand", "brands", id); });
        state.qualities.forEach((v) => add(v, "qualities", v));
        state.colors.forEach((v) => add(v, "colors", v));
        state.storages.forEach((v) => add(v, "storages", v));
        if (state.min != null) add("From Rs. " + ST.money(state.min), "min", "");
        if (state.max != null) add("Up to Rs. " + ST.money(state.max), "max", "");
        if (state.q) add('"' + state.q + '"', "q", "");
        $("#activeFilters").innerHTML = pills.join("");
    }

    function renderPager(pages) {
        const pg = $("#pager");
        if (pages <= 1) { pg.innerHTML = ""; return; }
        const nums = [];
        for (let i = 1; i <= pages; i++) {
            if (i === 1 || i === pages || Math.abs(i - state.page) <= 1) nums.push(i);
            else if (nums[nums.length - 1] !== "…") nums.push("…");
        }
        pg.innerHTML = '<button type="button" data-p="' + (state.page - 1) + '"' + (state.page === 1 ? " disabled" : "") + ' aria-label="Previous page"><i class="fa-regular fa-chevron-left"></i></button>' +
            nums.map((n) => n === "…" ? '<button disabled>…</button>' : '<button type="button" data-p="' + n + '" class="' + (n === state.page ? "on" : "") + '">' + n + "</button>").join("") +
            '<button type="button" data-p="' + (state.page + 1) + '"' + (state.page === pages ? " disabled" : "") + ' aria-label="Next page"><i class="fa-regular fa-chevron-right"></i></button>';
    }

    function render(scroll) {
        const list = filtered();
        const pages = Math.max(1, Math.ceil(list.length / PAGE_SIZE));
        state.page = Math.min(Math.max(1, state.page), pages);
        const slice = list.slice((state.page - 1) * PAGE_SIZE, state.page * PAGE_SIZE);

        $("#totalText").textContent = list.length;
        $("#rangeText").textContent = list.length ? (state.page - 1) * PAGE_SIZE + 1 + "–" + ((state.page - 1) * PAGE_SIZE + slice.length) : "0";
        $("#shopTitle").textContent = state.wish ? "Your wishlist" : "Shop phones";
        $("#shopSub").textContent = state.wish ? "Phones you have saved for later." : "Filter by brand, condition, colour, storage and price.";

        $("#results").innerHTML = slice.length
            ? slice.map((p) => ST.productCard(p)).join("")
            : '<div class="empty-state" style="grid-column:1/-1"><div class="ic"><i class="fa-regular fa-' + (state.wish ? "heart" : "magnifying-glass") + '"></i></div><h4>' +
              (state.wish ? "Your wishlist is empty" : "No phones match your filters") + "</h4><p>" +
              (state.wish ? "Tap the heart on any phone to save it here." : "Try removing a filter or searching for something else.") +
              '</p><button class="st-btn" type="button" id="emptyReset">' + (state.wish ? "Browse phones" : "Reset filters") + "</button></div>";
        renderActive();
        renderPager(pages);
        writeUrl();
        if (scroll) $("#results").scrollIntoView({behavior: "smooth", block: "start"});
    }

    function reset() {
        state.q = ""; state.brands.clear(); state.qualities.clear(); state.colors.clear(); state.storages.clear();
        state.min = state.max = null; state.page = 1; state.wish = false;
        $("#fQ").value = ""; $("#fMin").value = ""; $("#fMax").value = "";
        renderOptions();
        render();
    }

    function readPrice() {
        const lo = parseFloat($("#fMin").value);
        const hi = parseFloat($("#fMax").value);
        state.min = isNaN(lo) ? null : lo;
        state.max = isNaN(hi) ? null : hi;
    }

    /* ---- events ---- */
    document.addEventListener("click", (e) => {
        const chip = e.target.closest(".chip[data-g]");
        if (chip) {
            const g = chip.dataset.g;
            const v = g === "brands" ? Number(chip.dataset.v) : chip.dataset.v;
            state[g].has(v) ? state[g].delete(v) : state[g].add(v);
            chip.classList.toggle("on");
            state.page = 1;
            render();
            return;
        }
        const x = e.target.closest("[data-x]");
        if (x) {
            const g = x.dataset.x;
            if (g === "min") { state.min = null; $("#fMin").value = ""; }
            else if (g === "max") { state.max = null; $("#fMax").value = ""; }
            else if (g === "q") { state.q = ""; $("#fQ").value = ""; }
            else state[g].delete(g === "brands" ? Number(x.dataset.v) : x.dataset.v);
            state.page = 1;
            renderOptions();
            render();
            return;
        }
        const pg = e.target.closest("#pager button[data-p]");
        if (pg) { state.page = Number(pg.dataset.p); render(true); return; }
        if (e.target.closest("#emptyReset")) { state.wish ? (location.href = "search.html") : reset(); }
        if (e.target.closest("#fReset")) reset();
        if (e.target.closest("#fApply")) {
            readPrice(); state.page = 1; render();
            const f = $("#filters"); if (f) f.classList.remove("open");
            const ov = $(".st-overlay"); if (ov) ov.classList.remove("open");
        }
    });
    $("#searchForm").addEventListener("submit", (e) => {
        e.preventDefault();
        state.q = $("#fQ").value; state.page = 1; render();
    });
    $("#fSort").addEventListener("change", (e) => { state.sort = e.target.value; state.page = 1; render(); });
    document.addEventListener("st:store", () => { if (state.wish) render(); });

    /* ---- boot ---- */
    (async function () {
        readUrl();
        $("#fQ").value = state.q;
        $("#fSort").value = state.sort;
        $("#results").innerHTML = ST.skeletonCards(6);
        try {
            all = await ST.catalogue();
        } catch (e) {
            ST.toast("failure", e.message || "Product data loading failed!");
            $("#results").innerHTML = '<div class="empty-state" style="grid-column:1/-1"><div class="ic"><i class="fa-regular fa-triangle-exclamation"></i></div><h4>We could not load products</h4><p>Check your connection and try again.</p><button class="st-btn" onclick="location.reload()">Retry</button></div>';
            return;
        }
        renderOptions();
        render();
    })();
})();
