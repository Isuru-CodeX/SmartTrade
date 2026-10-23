/* Checkout : summary from the cart, shipping form, Stripe Card Elements & Receipt generation */
(function () {
    "use strict";
    const $ = ST.$;
    const items = ST.cart.items();

    if (!items.length) {
        const empty = $("#checkoutEmpty");
        empty.hidden = false;
        empty.innerHTML = '<div class="empty-state"><div class="ic"><i class="fa-regular fa-bag-shopping"></i></div><h4>Nothing to check out</h4><p>Add a phone to your cart first.</p><a class="st-btn" href="search.html">Browse phones</a></div>';
        return;
    }
    $("#checkoutForm").hidden = false;

    function shipping() {
        return Number((document.querySelector("input[name=ship]:checked") || {}).value || 1000);
    }

    function renderSummary() {
        $("#orderLines").innerHTML = items.map((i) =>
            '<div class="order-line"><span class="th"><img class="st-img" src="' + ST.esc(i.image) + '" alt=""></span><span><b>' + ST.esc(i.title) + "</b><br><small>Qty " + i.qty + '</small></span><span class="pr">' + ST.rs(i.price * i.qty) + "</span></div>").join("");
        $("#ckSub").textContent = ST.rs(ST.cart.total());
        $("#ckShip").textContent = ST.rs(shipping());
        $("#ckTotal").textContent = ST.rs(ST.cart.total() + shipping());
    }
    document.querySelectorAll("input[name=ship]").forEach((r) => r.addEventListener("change", renderSummary));
    renderSummary();

    /* ── Cities & Profile Pre-fill ── */
    let profile = null;
    (async function () {
        try {
            const r = await fetch("api/data/cities");
            if (r.ok) (await r.json()).cities.forEach((c) => {
                const o = document.createElement("option");
                o.value = c.id; o.textContent = c.name;
                $("#city-select").appendChild(o);
            });
        } catch (e) { /* ignore */ }

        const session = await ST.sessionReady;
        if (!session.loggedIn) {
            $("#ckSignin").innerHTML = '<a href="sign-in.html?next=checkout.html">Sign in</a> to use your saved address.';
            $("#sameAddress").disabled = true;
            return;
        }
        try {
            profile = (await ST.api("api/profiles/user-profile")).user;
            if (profile && profile.email) {
                $("#email").value = profile.email;
            }
        } catch (e) { /* ignore */ }
    })();

    function syncCardholderName() {
        const cardNameInput = $("#card-name");
        if (cardNameInput && !cardNameInput.value.trim()) {
            const fn = ($("#first-name")?.value || "").trim();
            const ln = ($("#last-name")?.value || "").trim();
            if (fn || ln) {
                cardNameInput.value = (fn + " " + ln).trim();
            }
        }
    }
    $("#first-name")?.addEventListener("input", syncCardholderName);
    $("#last-name")?.addEventListener("input", syncCardholderName);

    $("#sameAddress").addEventListener("change", (e) => {
        if (!e.target.checked || !profile) return;
        $("#first-name").value = profile.firstName || "";
        $("#last-name").value = profile.lastName || "";
        if (profile.email) $("#email").value = profile.email;
        $("#line-one").value = profile.lineOne || "";
        $("#line-two").value = profile.lineTwo || "";
        $("#postal-code").value = profile.postalCode || "";
        $("#city-select").value = profile.cityId || 0;
        $("#mobile").value = profile.mobile || "";
        syncCardholderName();
    });

    /* ── Stripe Elements Setup (Distinct Card Number, Expiry & CVC) ── */
    let stripe = null;
    let cardNumber = null;
    let cardExpiry = null;
    let cardCvc = null;
    let isStripeLive = false;
    let stripeConfig = { publishableKey: "pk_test_sample", isConfigured: false };

    function showCardError(msg) {
        const displayError = $("#card-errors");
        if (!displayError) return;
        if (msg) {
            displayError.textContent = msg;
            displayError.style.display = "block";
        } else {
            displayError.textContent = "";
            displayError.style.display = "none";
        }
    }

    function updateCardBrandIcon(brand) {
        const icon = $("#card-brand-icon");
        if (!icon) return;
        if (brand === "visa") {
            icon.innerHTML = '<i class="fa-brands fa-cc-visa text-primary" style="font-size:1.45rem"></i>';
        } else if (brand === "mastercard") {
            icon.innerHTML = '<i class="fa-brands fa-cc-mastercard text-warning" style="font-size:1.45rem"></i>';
        } else if (brand === "amex") {
            icon.innerHTML = '<i class="fa-brands fa-cc-amex text-info" style="font-size:1.45rem"></i>';
        } else if (brand === "discover") {
            icon.innerHTML = '<i class="fa-brands fa-cc-discover text-danger" style="font-size:1.45rem"></i>';
        } else {
            icon.innerHTML = '<i class="fa-regular fa-credit-card"></i>';
        }
    }

    async function initStripe() {
        try {
            const res = await fetch("api/checkout/stripe-config");
            if (res.ok) {
                stripeConfig = await res.json();
            }
        } catch (e) {
            console.warn("Could not load Stripe config:", e);
        }

        const isRealKey = stripeConfig.publishableKey &&
                          !stripeConfig.publishableKey.includes("sample") &&
                          !stripeConfig.publishableKey.includes("placeholder");

        if (window.Stripe && isRealKey) {
            try {
                stripe = Stripe(stripeConfig.publishableKey);
                const elements = stripe.elements();
                const elementStyles = {
                    base: {
                        color: "#eef4f1",
                        fontFamily: '"Poppins", system-ui, -apple-system, sans-serif',
                        fontSize: "15px",
                        fontSmoothing: "antialiased",
                        "::placeholder": { color: "#62747e" }
                    },
                    invalid: {
                        color: "#ff6b78",
                        iconColor: "#ff6b78"
                    }
                };

                cardNumber = elements.create("cardNumber", {
                    style: elementStyles,
                    showIcon: false
                });
                cardNumber.mount("#card-number-mount");
                cardNumber.on("change", function (event) {
                    updateCardBrandIcon(event.brand);
                    if (event.error) showCardError(event.error.message);
                    else showCardError("");
                });

                cardExpiry = elements.create("cardExpiry", {
                    style: elementStyles
                });
                cardExpiry.mount("#card-expiry-mount");
                cardExpiry.on("change", function (event) {
                    if (event.error) showCardError(event.error.message);
                    else showCardError("");
                });

                cardCvc = elements.create("cardCvc", {
                    style: elementStyles
                });
                cardCvc.mount("#card-cvc-mount");
                cardCvc.on("change", function (event) {
                    if (event.error) showCardError(event.error.message);
                    else showCardError("");
                });

                isStripeLive = true;
            } catch (err) {
                console.error("Stripe mount error:", err);
                mountFallbackCardInputs();
            }
        } else {
            mountFallbackCardInputs();
        }
    }

    function mountFallbackCardInputs() {
        isStripeLive = false;
        const numMount = $("#card-number-mount");
        const expMount = $("#card-expiry-mount");
        const cvcMount = $("#card-cvc-mount");

        if (numMount) {
            numMount.innerHTML = '<input type="text" id="fb-card-number" class="border-0 bg-transparent text-white w-100 p-0" placeholder="4242 4242 4242 4242" maxlength="19" autocomplete="cc-number" style="outline:none">';
            const input = $("#fb-card-number");
            input?.addEventListener("input", (e) => {
                let v = e.target.value.replace(/\D/g, "").slice(0, 16);
                e.target.value = v.replace(/(\d{4})(?=\d)/g, "$1 ");
                if (v.startsWith("4")) updateCardBrandIcon("visa");
                else if (v.startsWith("5")) updateCardBrandIcon("mastercard");
                else if (v.startsWith("3")) updateCardBrandIcon("amex");
                else updateCardBrandIcon("");
            });
        }
        if (expMount) {
            expMount.innerHTML = '<input type="text" id="fb-card-expiry" class="border-0 bg-transparent text-white w-100 p-0" placeholder="MM / YY" maxlength="7" autocomplete="cc-exp" style="outline:none">';
            const input = $("#fb-card-expiry");
            input?.addEventListener("input", (e) => {
                let v = e.target.value.replace(/\D/g, "").slice(0, 4);
                if (v.length >= 3) e.target.value = v.slice(0, 2) + " / " + v.slice(2);
                else e.target.value = v;
            });
        }
        if (cvcMount) {
            cvcMount.innerHTML = '<input type="password" id="fb-card-cvc" class="border-0 bg-transparent text-white w-100 p-0" placeholder="CVC" maxlength="4" autocomplete="cc-csc" style="outline:none">';
        }
    }

    initStripe();

    /* ── Form Submit & Payment Handling ── */
    const submitBtn = $("#placeOrderBtn");
    const submitBtnText = $("#placeOrderText");

    function setSubmitting(loading) {
        if (!submitBtn) return;
        submitBtn.disabled = loading;
        if (loading) {
            submitBtnText.innerHTML = '<i class="fa-regular fa-spinner fa-spin me-1"></i> Processing Payment...';
        } else {
            submitBtnText.innerHTML = 'Pay &amp; Place Order';
        }
    }

    $("#checkoutForm").addEventListener("submit", async (e) => {
        e.preventDefault();

        // 1. Validation
        const need = [
            ["first-name", "First name"],
            ["last-name", "Last name"],
            ["email", "Email address"],
            ["line-one", "Address line 1"],
            ["postal-code", "Postal code"],
            ["mobile", "Mobile"]
        ];

        for (const [id, label] of need) {
            if (!$("#" + id).value.trim()) {
                ST.toast("failure", label + " is required.");
                $("#" + id).focus();
                return;
            }
        }

        const emailVal = $("#email").value.trim();
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailVal)) {
            ST.toast("failure", "Please enter a valid email address.");
            $("#email").focus();
            return;
        }

        if ($("#city-select").value === "0") {
            ST.toast("failure", "Please select your city.");
            return;
        }

        const mobVal = $("#mobile").value.trim();
        if (!/^07[0-8]{1}[0-9]{7}$/.test(mobVal)) {
            ST.toast("failure", "Enter a valid mobile number (07XXXXXXXX).");
            $("#mobile").focus();
            return;
        }

        const cardholderName = ($("#card-name")?.value || "").trim();
        if (!cardholderName) {
            ST.toast("failure", "Cardholder Name is required.");
            $("#card-name")?.focus();
            return;
        }

        setSubmitting(true);
        showCardError("");

        const deliveryTypeId = document.querySelector("input[name=ship]:checked")?.value === "2500" ? 2 : 1;

        const payload = {
            items: items.map((i) => ({ stockId: i.stockId, qty: i.qty })),
            deliveryTypeId: deliveryTypeId,
            shipping: {
                firstName: $("#first-name").value.trim(),
                lastName: $("#last-name").value.trim(),
                email: emailVal,
                cityId: Number($("#city-select").value),
                lineOne: $("#line-one").value.trim(),
                lineTwo: $("#line-two").value.trim(),
                postalCode: $("#postal-code").value.trim(),
                mobile: mobVal
            }
        };

        try {
            // Step 1: Create Stripe PaymentIntent
            const intentRes = await fetch("api/checkout/create-intent", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            const intentData = await intentRes.json();
            if (!intentRes.ok || intentData.status !== "success") {
                throw new Error(intentData.message || "Failed to initialize payment gateway.");
            }

            let paymentIntentId = intentData.paymentIntentId;

            // Step 2: Confirm Card Payment with Stripe if live element is active
            if (stripe && cardNumber && isStripeLive && !intentData.isMock) {
                const stripeResult = await stripe.confirmCardPayment(intentData.clientSecret, {
                    payment_method: {
                        card: cardNumber,
                        billing_details: {
                            name: cardholderName,
                            email: payload.shipping.email,
                            phone: payload.shipping.mobile,
                            address: {
                                line1: payload.shipping.lineOne,
                                line2: payload.shipping.lineTwo || undefined,
                                postal_code: payload.shipping.postalCode
                            }
                        }
                    }
                });

                if (stripeResult.error) {
                    showCardError(stripeResult.error.message || "Card payment failed.");
                    throw new Error(stripeResult.error.message || "Card payment failed.");
                }

                if (stripeResult.paymentIntent && stripeResult.paymentIntent.id) {
                    paymentIntentId = stripeResult.paymentIntent.id;
                }
            } else {
                // Short simulation delay for mock sandbox
                await new Promise((r) => setTimeout(r, 700));
            }

            // Step 3: Complete order in backend & obtain generated receipt
            const confirmPayload = Object.assign({}, payload, { paymentIntentId: paymentIntentId });
            const orderRes = await fetch("api/checkout/confirm", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(confirmPayload)
            });

            const receiptData = await orderRes.json();
            if (!orderRes.ok || receiptData.status !== "success") {
                throw new Error(receiptData.message || "Order placement failed.");
            }

            // Step 4: Clear cart & show receipt modal!
            ST.cart.clear();
            ST.toast("success", "Payment confirmed! Order successfully placed.");

            // Display Receipt Modal with option to view order
            if (window.STReceipt) {
                window.STReceipt.show(receiptData, {
                    onContinue: () => {
                        window.location.href = "my-account.html#orders";
                    }
                });
            } else {
                Notiflix.Report.success("Payment Successful", "Order #" + receiptData.orderNumber + " confirmed! Thank you for your purchase.", "View My Orders", () => {
                    window.location.href = "my-account.html#orders";
                });
            }

        } catch (err) {
            console.error("Checkout error:", err);
            ST.toast("failure", err.message || "An error occurred while processing payment.");
            Notiflix.Report.failure("Payment Error", err.message || "Transaction could not be completed. Please try again.", "Okay");
        } finally {
            setSubmitting(false);
        }
    });
})();
