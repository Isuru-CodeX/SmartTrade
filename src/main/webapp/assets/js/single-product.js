/* Single product : api/data/products (catalogue) filtered by ?productId= */
(function () {
    "use strict";
    const $ = ST.$;
    const id = Number(new URLSearchParams(location.search).get("productId"));

    function notFound(msg) {
        $("#productRoot").innerHTML = '<div class="empty-state"><div class="ic"><i class="fa-regular fa-mobile-screen"></i></div><h4>' + ST.esc(msg) + '</h4><p>The phone may have been removed or the link is incorrect.</p><a class="st-btn" href="search.html">Browse phones</a></div>';
    }

    function render(p) {
        const stock = ST.stockOf(p);
        const total = ST.totalQty(p);
        const rawImgs = (p.images && p.images.length ? p.images : [ST.PLACEHOLDER]);
        const imgs = rawImgs.map((src) => ST.cleanImgUrl(src));
        document.title = p.title + " | SmartTrade";
        $("#crumbTitle").textContent = p.title;

        const stockPill = total <= 0 ? '<span class="stock-pill out"><i class="fa-solid fa-circle-xmark"></i> Out of stock</span>'
            : total <= 3 ? '<span class="stock-pill low"><i class="fa-solid fa-bolt"></i> Only ' + total + " left</span>"
            : '<span class="stock-pill"><i class="fa-solid fa-circle-check"></i> In stock &middot; ' + total + " units</span>";

        $("#productRoot").innerHTML =
            '<div class="pd"><div class="gallery"><div class="thumbs" id="thumbs">' +
            imgs.map((src, i) => '<button type="button" class="' + (i === 0 ? "on" : "") + '" data-i="' + i + '" aria-label="Image ' + (i + 1) + '"><img class="st-img" src="' + ST.esc(src) + '" alt=""></button>').join("") +
            '</div><div class="stage"><img class="st-img" id="stageImg" src="' + ST.esc(imgs[0]) + '" alt="' + ST.esc(p.title) + '"></div></div>' +
            '<div class="pd-info"><span class="eyebrow">' + ST.esc(p.brandName || "SmartTrade") + "</span><h1>" + ST.esc(p.title) + "</h1>" + stockPill +
            '<div class="pd-price"><b>' + (stock ? ST.rs(stock.price) : "Not available") + "</b></div>" +
            '<div class="spec-grid">' +
            '<div class="spec"><small>Brand</small><span>' + ST.esc(p.brandName || "-") + "</span></div>" +
            '<div class="spec"><small>Model</small><span>' + ST.esc(p.modelName || "-") + "</span></div>" +
            '<div class="spec"><small>Condition</small><span>' + ST.esc(p.qualityValue || "-") + "</span></div>" +
            '<div class="spec"><small>Storage</small><span>' + ST.esc(p.storageValue || "-") + "</span></div>" +
            '<div class="spec"><small>Colour</small><span><i class="swatch" style="cursor:default;background:' + ST.esc(p.colorValue) + '"></i>' + ST.esc(p.colorValue || "-") + "</span></div>" +
            '<div class="spec"><small>Listed</small><span>' + ST.esc(stock ? String(stock.createdAt).substring(0, 10) : "-") + "</span></div></div>" +
            '<div class="buy-row"><div class="qty" id="qtyBox"><button type="button" data-q="-1" aria-label="Decrease"><i class="fa-regular fa-minus"></i></button><input id="qtyInput" type="text" inputmode="numeric" value="1" aria-label="Quantity"><button type="button" data-q="1" aria-label="Increase"><i class="fa-regular fa-plus"></i></button></div>' +
            '<button class="st-btn" type="button" id="addBtn"' + (total <= 0 ? " disabled" : "") + '><i class="fa-regular fa-bag-shopping"></i> Add to cart</button>' +
            '<button class="st-btn st-btn-dark" type="button" id="buyBtn"' + (total <= 0 ? " disabled" : "") + ">Buy now</button>" +
            '<button class="st-iconbtn p-wish ' + (ST.wish.has(p.productId) ? "on" : "") + '" style="position:static;width:48px;height:48px" type="button" data-wish="' + p.productId + '" aria-label="Wishlist"><i class="fa-' + (ST.wish.has(p.productId) ? "solid" : "regular") + ' fa-heart"></i></button></div>' +
            '<ul class="kv mt-4"><li><i class="fa-regular fa-truck-fast"></i>Island-wide delivery &middot; standard or express</li><li><i class="fa-regular fa-arrow-rotate-left"></i>24-hour return window</li><li><i class="fa-regular fa-shield-halved"></i>Sold by a verified seller</li></ul>' +
            "</div></div>";

        /* gallery */
        $("#thumbs").addEventListener("click", (e) => {
            const b = e.target.closest("button[data-i]");
            if (!b) return;
            $("#stageImg").src = imgs[b.dataset.i];
            ST.$$("#thumbs button").forEach((x) => x.classList.toggle("on", x === b));
        });
        /* quantity */
        const input = $("#qtyInput");
        const clamp = () => { input.value = Math.max(1, Math.min(parseInt(input.value, 10) || 1, stock ? stock.qty : 1)); return Number(input.value); };
        $("#qtyBox").addEventListener("click", (e) => {
            const b = e.target.closest("[data-q]");
            if (b) { input.value = (parseInt(input.value, 10) || 1) + Number(b.dataset.q); clamp(); }
        });
        input.addEventListener("change", clamp);
        $("#addBtn").addEventListener("click", () => { if (ST.cart.add(p, clamp())) ST.openCart(); });
        $("#buyBtn").addEventListener("click", () => { if (ST.cart.add(p, clamp())) location.href = "checkout.html"; });

        /* description (seller html is sanitised) */
        const html = ST.sanitize(p.description);
        if (html.trim()) {
            $("#productDescription").innerHTML = html;
            $("#descSection").hidden = false;
        }
    }

    function similar(p, all) {
        const list = all.filter((x) => x.productId !== p.productId && (x.brandId === p.brandId || x.modelId === p.modelId)).slice(0, 4);
        if (!list.length) return;
        $("#similarGrid").innerHTML = list.map((x) => ST.productCard(x)).join("");
        $("#similarSection").hidden = false;
    }

    (async function () {
        if (!id) return notFound("No product selected");
        try {
            let p = null;
            let all = [];
            try {
                all = await ST.catalogue();
                if (all && all.length) {
                    p = all.find((x) => Number(x.productId) === id);
                }
            } catch (ignored) {}

            // Fallback: If not found in catalogue (e.g. freshly created listing), fetch product directly
            if (!p) {
                const res = await fetch("api/products/" + id, { credentials: "include" });
                if (res.ok) {
                    const data = await res.json();
                    if (data && data.status && data.product) {
                        p = data.product;
                    }
                }
            }

            if (!p) return notFound("Product not found");

            // Ensure stock info is present on product
            if ((!p.stockDTOList || !p.stockDTOList.length) && (p.price || p.qty)) {
                p.stockDTOList = [{
                    productId: p.productId,
                    price: p.price || 0,
                    qty: p.qty || 0,
                    createdAt: p.createdAt || "-",
                    status: p.status || "APPROVED"
                }];
            }

            render(p);
            if (all && all.length) {
                similar(p, all);
            }
        } catch (e) {
            ST.toast("failure", e.message || "Product data loading failed!");
            notFound("We could not load this product");
        }
    })();
})();
