/**
 * SmartTrade Shared Receipt Renderer
 * Renders high-fidelity, branded dark-themed order receipt
 * Supports viewing and 1-click Download / Print (PDF export)
 */
(function (global) {
    "use strict";

    function esc(s) {
        if (!s) return "";
        return String(s).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
    }

    function formatRs(num) {
        return "Rs. " + Number(num || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    function ensureModalContainer() {
        let modal = document.getElementById("stReceiptModal");
        if (!modal) {
            modal = document.createElement("div");
            modal.id = "stReceiptModal";
            modal.className = "receipt-modal-backdrop";
            modal.innerHTML = `
                <div class="receipt-modal-dialog">
                    <div class="st-receipt-card" id="stReceiptContent">
                        <!-- Rendered by STReceipt.show() -->
                    </div>
                </div>
            `;
            document.body.appendChild(modal);

            modal.addEventListener("click", function (e) {
                if (e.target === modal) {
                    STReceipt.hide();
                }
            });

            document.addEventListener("keydown", function (e) {
                if (e.key === "Escape" && modal.classList.contains("active")) {
                    STReceipt.hide();
                }
            });
        }
        return modal;
    }

    const STReceipt = {
        show(data, options) {
            options = options || {};
            const modal = ensureModalContainer();
            const container = document.getElementById("stReceiptContent");
            if (!container) return;

            const items = data.items || [];
            const itemsHtml = items.map(function (it) {
                const specParts = [it.storage, it.color, it.quality].filter(Boolean);
                const specText = specParts.length ? specParts.join(" · ") : (it.brand || "");
                const imgSrc = it.image || "assets/images/placeholder.png";

                return `
                    <tr>
                        <td>
                            <div class="receipt-item-cell">
                                <img src="${esc(imgSrc)}" class="receipt-item-img" alt="" onerror="this.src='assets/images/placeholder.png'">
                                <div class="receipt-item-meta">
                                    <div class="receipt-item-title">${esc(it.title)}</div>
                                    <div class="receipt-item-spec">${esc(specText)}</div>
                                </div>
                            </div>
                        </td>
                        <td class="text-center"><span class="mono">${it.qty}</span></td>
                        <td class="text-end mono">${formatRs(it.unitPrice || it.price)}</td>
                        <td class="text-end mono"><strong class="text-white">${formatRs(it.lineTotal || it.total || ((it.unitPrice || it.price) * it.qty))}</strong></td>
                    </tr>
                `;
            }).join("");

            const orderNo = data.orderNumber || ("#ST-ORD-" + String(data.orderId || 0).padStart(5, "0"));
            const dateStr = data.createdAt || "Just now";
            const custName = data.customerName || "Valued Customer";
            const custEmail = data.customerEmail || "—";
            const custMobile = data.customerMobile || "—";
            const shippingAddr = data.shippingAddress || "Standard delivery address";
            const deliveryType = data.deliveryType || "Standard Delivery";
            const deliveryFee = Number(data.deliveryFee || 0);
            const subtotal = Number(data.subtotal || 0);
            const total = Number(data.total || (subtotal + deliveryFee));
            const stripeTx = data.stripePaymentId && data.stripePaymentId !== "—" ? data.stripePaymentId : "Processed via Stripe Gateway";
            const statusLabel = data.orderStatus || "PAID";

            container.innerHTML = `
                <!-- Top actions bar -->
                <div class="receipt-actions-top">
                    <div class="receipt-status-pill">
                        <span class="dot"></span> ${esc(statusLabel)} VIA STRIPE
                    </div>
                    <div class="receipt-top-btns">
                        <button type="button" class="btn-action-sm btn-approve" onclick="STReceipt.print()" title="Print / Download PDF">
                            <i class="fa-regular fa-print"></i> Download / Print
                        </button>
                        <button type="button" class="receipt-btn-icon" onclick="STReceipt.hide()" aria-label="Close" title="Close">
                            <i class="fa-regular fa-xmark"></i>
                        </button>
                    </div>
                </div>

                <!-- Printable Receipt Body -->
                <div class="st-receipt-body">
                    <!-- Header -->
                    <div class="receipt-header">
                        <div class="receipt-brand-logo">
                            <img src="assets/images/logo/logo-light.png" alt="SmartTrade" onerror="this.src='assets/images/logo/logo.png'">
                            <span class="receipt-tagline">Premium Smart Devices &amp; Technology</span>
                        </div>
                        <div class="receipt-invoice-meta">
                            <div class="receipt-invoice-title">
                                <i class="fa-regular fa-receipt text-brand"></i> OFFICIAL RECEIPT
                            </div>
                            <div class="receipt-order-no">${esc(orderNo)}</div>
                            <div class="receipt-invoice-date"><i class="fa-regular fa-calendar-check me-1"></i>${esc(dateStr)}</div>
                        </div>
                    </div>

                    <!-- 2-Column Info Grid -->
                    <div class="receipt-grid-info">
                        <div class="receipt-info-block">
                            <h6><i class="fa-regular fa-user"></i> Billed &amp; Shipped To</h6>
                            <div class="receipt-info-text"><b>${esc(custName)}</b></div>
                            <div class="receipt-info-sub"><i class="fa-regular fa-envelope me-1"></i>${esc(custEmail)}</div>
                            <div class="receipt-info-sub"><i class="fa-regular fa-phone me-1"></i>${esc(custMobile)}</div>
                            <div class="receipt-info-sub mt-2"><i class="fa-regular fa-location-dot me-1"></i>${esc(shippingAddr)}</div>
                        </div>
                        <div class="receipt-info-block">
                            <h6><i class="fa-regular fa-credit-card"></i> Payment &amp; Logistics</h6>
                            <div class="receipt-info-text"><b>Payment Method:</b> Stripe Online Gateway</div>
                            <div class="receipt-info-sub"><b>Card Status:</b> <span class="text-brand">Verified &amp; Charged</span></div>
                            <div class="receipt-info-sub"><b>Transaction Ref:</b> <code class="mono" style="font-size:0.75rem">${esc(stripeTx)}</code></div>
                            <div class="receipt-info-text mt-2"><b>Delivery Method:</b> ${esc(deliveryType)}</div>
                        </div>
                    </div>

                    <!-- Itemized Purchased Table -->
                    <div class="receipt-items-wrap">
                        <table class="receipt-table">
                            <thead>
                                <tr>
                                    <th>Item Details</th>
                                    <th class="text-center" style="width:70px">Qty</th>
                                    <th class="text-end" style="width:130px">Unit Price</th>
                                    <th class="text-end" style="width:140px">Total</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${itemsHtml || '<tr><td colspan="4" class="text-center py-3 text-muted">No item details available</td></tr>'}
                            </tbody>
                        </table>
                    </div>

                    <!-- Totals Box -->
                    <div class="receipt-totals-row">
                        <div class="receipt-totals-box">
                            <div class="receipt-summary-line">
                                <span>Subtotal</span>
                                <span class="mono">${formatRs(subtotal)}</span>
                            </div>
                            <div class="receipt-summary-line">
                                <span>Delivery Fee</span>
                                <span class="mono">${formatRs(deliveryFee)}</span>
                            </div>
                            <div class="receipt-summary-line total">
                                <span>Grand Total</span>
                                <span class="price">${formatRs(total)}</span>
                            </div>
                        </div>
                    </div>

                    <!-- Footer / Security Note -->
                    <div class="receipt-footer-bar">
                        <div class="receipt-footer-note">
                            <i class="fa-regular fa-shield-check text-brand me-1"></i>
                            This is an authentic, computer-generated receipt issued by SmartTrade (Pvt) Ltd.
                            For inquiries, support, or warranty claims, contact support@smarttrade.lk.
                        </div>
                        <div class="receipt-footer-stamp">
                            <i class="fa-solid fa-certificate"></i> SmartTrade Verified
                        </div>
                    </div>
                </div>

                <!-- Footer Action Buttons -->
                <div class="receipt-modal-actions-foot">
                    ${options.onContinue ? `
                        <button type="button" class="st-btn-outline btn-action-sm" onclick="STReceipt.onContinueClick()">
                            <i class="fa-regular fa-bag-shopping"></i> Continue Shopping
                        </button>
                    ` : ''}
                    <button type="button" class="btn-action-sm btn-approve" onclick="STReceipt.print()">
                        <i class="fa-regular fa-print"></i> Download / Print Receipt
                    </button>
                    <button type="button" class="btn-action-sm btn-outline" onclick="STReceipt.hide()">
                        Close
                    </button>
                </div>
            `;

            STReceipt._onContinue = options.onContinue || null;
            modal.classList.add("active");
            document.body.style.overflow = "hidden";
        },

        hide() {
            const modal = document.getElementById("stReceiptModal");
            if (modal) {
                modal.classList.remove("active");
            }
            document.body.style.overflow = "";
        },

        print() {
            window.print();
        },

        onContinueClick() {
            if (typeof STReceipt._onContinue === "function") {
                STReceipt._onContinue();
            } else {
                STReceipt.hide();
            }
        }
    };

    global.STReceipt = STReceipt;

})(window);
