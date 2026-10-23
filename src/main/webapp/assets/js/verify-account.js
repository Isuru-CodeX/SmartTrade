/* Verify account : POST api/verify-accounts */
const params = new URLSearchParams(window.location.search);
const emailInput = document.getElementById("email");
const verificationCodeInput = document.getElementById("verificationCode");

const urlEmail = params.get("email") || "";
const urlCode = params.get("verificationCode") || "";

if (emailInput && urlEmail) {
    emailInput.value = urlEmail;
}
if (verificationCodeInput && urlCode) {
    verificationCodeInput.value = urlCode.replace(/\D/g, "").slice(0, 6);
}

if (verificationCodeInput) {
    verificationCodeInput.addEventListener("input", () => {
        verificationCodeInput.value = verificationCodeInput.value.replace(/\D/g, "").slice(0, 6);
    });
}

document.getElementById("verifyForm").addEventListener("submit", (e) => {
    e.preventDefault();
    verifyAccount();
});

async function verifyAccount() {
    const message = document.getElementById("message");
    message.textContent = "";

    const email = emailInput ? emailInput.value.trim().toLowerCase() : "";
    const code = verificationCodeInput ? verificationCodeInput.value.trim() : "";

    if (!email) {
        message.textContent = "Please enter your email address.";
        if (emailInput) emailInput.focus();
        return;
    }
    const emailRegex = /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,}$/;
    if (!emailRegex.test(email)) {
        message.textContent = "Please provide a valid email address.";
        if (emailInput) emailInput.focus();
        return;
    }
    if (!code || !/^\d{6}$/.test(code)) {
        message.textContent = "Please enter the 6-digit verification code.";
        if (verificationCodeInput) verificationCodeInput.focus();
        return;
    }

    Notiflix.Loading.pulse("Verifying account...");
    try {
        const response = await fetch("api/verify-accounts", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({email: email, verificationCode: code})
        });

        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                Notiflix.Report.success("SmartTrade", data.message, "Sign in now", () => {
                    window.location = "sign-in.html";
                });
            } else {
                message.textContent = data.message;
                Notiflix.Notify.failure(data.message);
            }
        } else {
            Notiflix.Notify.failure("Verification process failed! Please check your code.");
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    } finally {
        Notiflix.Loading.remove(400);
    }
}
