/* <header-content> : announcement bar, sticky main bar, mobile menu, search overlay */
class HeaderContent extends HTMLElement {
    connectedCallback() {
        this.innerHTML = `
<div class="st-topbar">
    <div class="container">
        <div class="msg"><i class="fa-solid fa-bolt"></i><span>Verified sellers<span class="long"> &middot; Island-wide delivery &middot; 24-hour returns</span></span></div>
        <ul>
            <li><a href="search.html">Browse phones</a></li>
            <li><a href="my-account.html#products">Sell a phone</a></li>
            <li><span>English &middot; LKR</span></li>
        </ul>
    </div>
</div>

<div class="st-header" id="stHeader">
    <div class="container">
        <div class="bar">
            <button class="st-iconbtn st-menu-btn" type="button" data-open-menu aria-label="Open menu"><i class="fa-regular fa-bars"></i></button>
            <a class="st-logo" href="index.html" aria-label="SmartTrade home"><img src="assets/images/logo/logo.png" alt="SmartTrade"></a>

            <ul class="st-nav">
                <li><a href="index.html">Home</a></li>
                <li><a href="search.html">Shop</a></li>
                <li><a href="index.html#brands">Brands</a></li>
                <li><a href="index.html#new-arrivals">New arrivals</a></li>
                <li><a href="my-account.html#products">Sell</a></li>
            </ul>

            <div class="st-actions">
                <button class="st-searchbtn" type="button" data-open-search aria-label="Search">
                    <i class="fa-regular fa-magnifying-glass"></i><span>Search phones, brands&hellip;</span><kbd>/</kbd>
                </button>
                <a class="st-iconbtn wish-link" href="search.html?wishlist=1" aria-label="Wishlist" title="Wishlist">
                    <i class="fa-regular fa-heart"></i><span class="st-badge-dot" id="stWishBadge" data-n="0"></span>
                </a>
                <a class="st-iconbtn" href="cart.html" data-open-cart aria-label="Cart" title="Cart">
                    <i class="fa-regular fa-bag-shopping"></i><span class="st-badge-dot" id="stCartBadge" data-n="0"></span>
                </a>
                <div class="st-account">
                    <button class="st-iconbtn" type="button" data-account-toggle aria-label="Account" title="Account"><i class="fa-regular fa-user"></i></button>
                    <div class="st-account-menu" id="stAccountMenu">
                        <div class="guest-cta">
                            <a class="st-btn st-btn-block" href="sign-in.html">Sign in</a>
                            <a class="st-btn st-btn-ghost st-btn-block" href="sign-up.html">Create account</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<aside class="st-mobile" aria-label="Menu">
    <div class="head">
        <a class="st-logo" href="index.html"><img src="assets/images/logo/logo.png" alt="SmartTrade"></a>
        <button class="st-iconbtn" type="button" data-close aria-label="Close menu"><i class="fa-regular fa-xmark"></i></button>
    </div>
    <nav>
        <a href="index.html"><i class="fa-regular fa-house"></i> Home</a>
        <a href="search.html"><i class="fa-regular fa-mobile-screen-button"></i> Shop</a>
        <a href="index.html#brands"><i class="fa-regular fa-tags"></i> Brands</a>
        <a href="index.html#new-arrivals"><i class="fa-regular fa-sparkles"></i> New arrivals</a>
        <a href="my-account.html#products"><i class="fa-regular fa-store"></i> Sell a phone</a>
        <a href="search.html?wishlist=1"><i class="fa-regular fa-heart"></i> Wishlist</a>
        <a href="my-account.html#orders"><i class="fa-regular fa-box-open"></i> My orders</a>
        <a href="my-account.html"><i class="fa-regular fa-user"></i> My account</a>
    </nav>
</aside>

<div class="st-search" id="stSearch" role="dialog" aria-label="Search">
    <div class="panel">
        <form id="stSearchForm" autocomplete="off">
            <i class="fa-regular fa-magnifying-glass"></i>
            <input id="stSearchInput" type="search" placeholder="Search phones, brands, storage&hellip;" aria-label="Search products">
            <button class="st-iconbtn" type="button" data-close aria-label="Close search"><i class="fa-regular fa-xmark"></i></button>
        </form>
        <div class="results" id="stSearchResults">
            <div class="hint">Type a brand, model or keyword. Press <b>Enter</b> to see every match.</div>
        </div>
    </div>
</div>`;
    }
}
customElements.define("header-content", HeaderContent);
