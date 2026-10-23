/* <footer-content> : footer, cart drawer, overlay, back-to-top */
class FooterContent extends HTMLElement {
    connectedCallback() {
        this.innerHTML = `
<div class="top">
    <div class="container">
        <div class="row g-5">
            <div class="col-lg-4 brandcol">
                <img src="assets/images/logo/logo.png" alt="SmartTrade">
                <p>Buy and sell phones with people you can trust. Verified sellers, honest listings and a checkout that stays out of your way.</p>
                <div class="social">
                    <a href="#" aria-label="Facebook"><i class="fa-brands fa-facebook-f"></i></a>
                    <a href="#" aria-label="Instagram"><i class="fa-brands fa-instagram"></i></a>
                    <a href="#" aria-label="X"><i class="fa-brands fa-x-twitter"></i></a>
                    <a href="#" aria-label="LinkedIn"><i class="fa-brands fa-linkedin-in"></i></a>
                </div>
            </div>
            <div class="col-6 col-lg-2">
                <h6>Shop</h6>
                <ul>
                    <li><a href="search.html">All phones</a></li>
                    <li><a href="index.html#brands">Brands</a></li>
                    <li><a href="index.html#new-arrivals">New arrivals</a></li>
                    <li><a href="search.html?wishlist=1">Wishlist</a></li>
                </ul>
            </div>
            <div class="col-6 col-lg-2">
                <h6>Account</h6>
                <ul>
                    <li><a href="my-account.html">My account</a></li>
                    <li><a href="my-account.html#orders">My orders</a></li>
                    <li><a href="sign-in.html">Sign in</a></li>
                    <li><a href="sign-up.html">Register</a></li>
                    <li><a href="cart.html">Cart</a></li>
                </ul>
            </div>
            <div class="col-6 col-lg-2">
                <h6>Support</h6>
                <ul>
                    <li><a href="mailto:support@smarttrade.lk"><i class="fa-regular fa-envelope me-1"></i>support@smarttrade.lk</a></li>
                    <li><a href="#">Privacy policy</a></li>
                    <li><a href="#">Terms of use</a></li>
                    <li><a href="#">FAQ</a></li>
                </ul>
            </div>
            <div class="col-6 col-lg-2">
                <h6>Get the app</h6>
                <div class="app-link">
                    <a href="#"><img src="assets/images/others/app-store.png" alt="App Store" width="130"></a>
                    <a href="#"><img src="assets/images/others/play-store.png" alt="Google Play" width="130"></a>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="bottom">
    <div class="container">
        <span class="copy">&copy; <span id="stYear">2025</span> SmartTrade. All rights reserved.</span>
        <span class="pay">We accept
            <img src="assets/images/icons/visa.png" alt="Visa">
            <img src="assets/images/icons/mastercard.png" alt="Mastercard">
        </span>
    </div>
</div>

<div class="st-overlay" data-close></div>

<aside class="st-drawer" id="stDrawer" aria-label="Cart">
    <div class="dh">
        <h3>Your cart</h3>
        <button class="st-iconbtn" type="button" data-close aria-label="Close cart"><i class="fa-regular fa-xmark"></i></button>
    </div>
    <div class="db" id="stDrawerBody"></div>
    <div class="df" id="stDrawerFoot">
        <div class="sum-line"><span>Subtotal</span><strong id="stDrawerTotal">Rs. 0.00</strong></div>
        <div class="d-grid gap-2">
            <a class="st-btn" href="checkout.html">Checkout <i class="fa-regular fa-arrow-right"></i></a>
            <a class="st-btn st-btn-ghost" href="cart.html">View cart</a>
        </div>
    </div>
</aside>

<a href="#" class="back-to-top" id="backToTop" aria-label="Back to top"><i class="fa-regular fa-arrow-up"></i></a>`;
        const y = this.querySelector("#stYear");
        if (y) y.textContent = new Date().getFullYear();
    }
}
customElements.define("footer-content", FooterContent);
