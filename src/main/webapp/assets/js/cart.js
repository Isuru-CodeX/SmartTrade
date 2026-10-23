/* Cart page : localStorage backed cart (see ST.cart in app.js) */
(function () {
    "use strict";
    const $ = ST.$;

    function render() {
        const items = ST.cart.items();
        const root = $("#cartRoot");
        $("#clearCart").hidden = !items.length;
        $("#cartSub").textContent = items.length ? ST.cart.count() + " item(s) ready for checkout" : "";

        if (!items.length) {
            root.innerHTML = '<div class="empty-state"><div class="ic"><i class="fa-regular fa-bag-shopping"></i></div><h4>Your cart is empty</h4><p>Looks like you have not added anything yet. Explore the latest phones from verified sellers.</p><a class="st-btn" href="search.html">Browse phones</a></div>';
            return;
        }
        root.innerHTML = '<div class="cart-layout"><div class="panel" style="padding-block:.6rem">' +
            items.map((i) =>
                '<div class="cart-row"><a class="th" href="single-product.html?productId=' + i.productId + '"><img class="st-img" src="' + ST.esc(i.image) + '" alt=""></a>' +
                '<div><h6><a href="single-product.html?productId=' + i.productId + '">' + ST.esc(i.title) + "</a></h6><small>" + ST.esc(i.spec || i.brand) + " &middot; " + ST.rs(i.price) + "</small>" +
                '<div class="mt-2"><button class="rm" type="button" data-rm="' + i.stockId + '"><i class="fa-regular fa-trash"></i> Remove</button></div></div>' +
                '<div class="qty sm" data-stock="' + i.stockId + '"><button type="button" data-d="-1" aria-label="Decrease"><i class="fa-regular fa-minus"></i></button><input type="text" inputmode="numeric" value="' + i.qty + '" aria-label="Quantity"><button type="button" data-d="1" aria-label="Increase"><i class="fa-regular fa-plus"></i></button></div>' +
                '<div class="line">' + ST.rs(i.price * i.qty) + "</div></div>").join("") +
            '</div><aside class="summary"><h5>Order summary</h5>' +
            '<div class="ln"><span>Items</span><span class="mono">' + ST.cart.count() + "</span></div>" +
            '<div class="ln"><span>Subtotal</span><span class="mono">' + ST.rs(ST.cart.total()) + "</span></div>" +
            '<div class="ln"><span>Delivery</span><span style="color:var(--st-muted)">Calculated at checkout</span></div>' +
            '<div class="ln total"><span>Total</span><b>' + ST.rs(ST.cart.total()) + "</b></div>" +
            '<a class="st-btn st-btn-block mt-3" href="checkout.html">Proceed to checkout <i class="fa-regular fa-arrow-right"></i></a>' +
            '<a class="st-btn st-btn-ghost st-btn-block mt-2" href="search.html">Continue shopping</a></aside></div>';
    }

    document.addEventListener("click", (e) => {
        const b = e.target.closest(".qty [data-d]");
        if (b) {
            const box = b.closest(".qty");
            const stockId = Number(box.dataset.stock);
            const item = ST.cart.items().find((i) => i.stockId === stockId);
            if (item) {
                if (item.qty + Number(b.dataset.d) > item.max) ST.toast("warning", "Only " + item.max + " unit(s) available.");
                ST.cart.setQty(stockId, item.qty + Number(b.dataset.d));
            }
        }
    });
    document.addEventListener("change", (e) => {
        if (e.target.closest(".qty") && e.target.tagName === "INPUT") {
            ST.cart.setQty(Number(e.target.closest(".qty").dataset.stock), e.target.value);
        }
    });
    $("#clearCart").addEventListener("click", () => {
        Notiflix.Confirm.show("Clear cart", "Remove every item from your cart?", "Clear", "Cancel", () => ST.cart.clear(), () => {}, {
            titleColor: "#eef4f1", messageColor: "#b6c4cc", backgroundColor: "#141d24", okButtonBackground: "#ff6b78", okButtonColor: "#2a0d11",
            cancelButtonBackground: "#212f39", cancelButtonColor: "#eef4f1", borderRadius: "18px", backOverlayColor: "rgba(5,9,12,.78)"
        });
    });
    document.addEventListener("st:store", render);
    render();
})();
