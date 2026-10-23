/* ==========================================================================
   Sign in & Forgot Password Flow
   - Sign in: POST api/users/login
   - Forgot password request: POST api/users/forgot-password
   - Reset password verification: POST api/users/reset-password
   ========================================================================== */
(function () {
    "use strict";

    // Views & Containers
    const signInView = document.getElementById("signInView");
    const forgotPasswordView = document.getElementById("forgotPasswordView");
    const forgotStep1 = document.getElementById("forgotStep1");
    const forgotStep2 = document.getElementById("forgotStep2");

    // Forms
    const signInForm = document.getElementById("signInForm");
    const forgotEmailForm = document.getElementById("forgotEmailForm");
    const resetPasswordForm = document.getElementById("resetPasswordForm");

    // Inputs & Elements
    const emailInput = document.getElementById("email");
    const passwordInput = document.getElementById("password");
    const signInMessage = document.getElementById("message");

    const forgotEmailInput = document.getElementById("forgotEmail");
    const forgotMessage1 = document.getElementById("forgotMessage1");

    const resetEmailHidden = document.getElementById("resetEmail");
    const sentCodeEmailText = document.getElementById("sentCodeEmailText");
    const resetCodeInput = document.getElementById("resetCode");
    const resetNewPasswordInput = document.getElementById("resetNewPassword");
    const resetConfirmPasswordInput = document.getElementById("resetConfirmPassword");
    const resetPwMeter = document.getElementById("resetPwMeter");
    const resetPwRules = document.getElementById("resetPwRules");
    const forgotMessage2 = document.getElementById("forgotMessage2");

    // Buttons
    const showForgotBtn = document.getElementById("showForgotBtn");
    const backToSignInBtn = document.getElementById("backToSignInBtn");
    const cancelResetBtn = document.getElementById("cancelResetBtn");
    const backToStep1Btn = document.getElementById("backToStep1Btn");
    const resendCodeBtn = document.getElementById("resendCodeBtn");

    // Password validation rules (matches server Validator)
    const PW_RULES = {
        len: (v) => v.length >= 8 && v.length <= 20,
        num: (v) => /\d/.test(v),
        low: (v) => /[a-z]/.test(v),
        up: (v) => /[A-Z]/.test(v),
        sp: (v) => /[@#$%^&+=_!\-*()[\]{}|;:,.<>?/~`]/.test(v)
    };

    function validatePasswordRules(val) {
        let score = 0;
        let allValid = true;
        Object.keys(PW_RULES).forEach((key) => {
            const ok = PW_RULES[key](val);
            const el = resetPwRules ? resetPwRules.querySelector('[data-r="' + key + '"]') : null;
            if (el) el.classList.toggle("ok", ok);
            if (ok) score++;
            else allValid = false;
        });
        if (resetPwMeter) {
            resetPwMeter.dataset.s = val ? Math.max(1, Math.min(4, score - (score === 5 ? 1 : 0))) : 0;
        }
        return allValid;
    }

    if (resetNewPasswordInput) {
        resetNewPasswordInput.addEventListener("input", () => {
            validatePasswordRules(resetNewPasswordInput.value);
        });
    }

    // View switching helpers
    function showSignIn() {
        if (forgotPasswordView) forgotPasswordView.classList.add("d-none");
        if (signInView) signInView.classList.remove("d-none");
        if (signInMessage) signInMessage.textContent = "";
    }

    function showStep1() {
        if (signInView) signInView.classList.add("d-none");
        if (forgotPasswordView) forgotPasswordView.classList.remove("d-none");
        if (forgotStep1) forgotStep1.classList.remove("d-none");
        if (forgotStep2) forgotStep2.classList.add("d-none");
        if (forgotMessage1) forgotMessage1.textContent = "";
        if (forgotEmailInput && emailInput && emailInput.value.trim()) {
            forgotEmailInput.value = emailInput.value.trim();
        }
        if (forgotEmailInput) forgotEmailInput.focus();
    }

    function showStep2(email, code) {
        if (signInView) signInView.classList.add("d-none");
        if (forgotPasswordView) forgotPasswordView.classList.remove("d-none");
        if (forgotStep1) forgotStep1.classList.add("d-none");
        if (forgotStep2) forgotStep2.classList.remove("d-none");

        if (email) {
            if (resetEmailHidden) resetEmailHidden.value = email;
            if (sentCodeEmailText) sentCodeEmailText.textContent = email;
        }
        if (code && resetCodeInput) {
            resetCodeInput.value = code;
        }

        if (forgotMessage2) forgotMessage2.textContent = "";
        if (resetNewPasswordInput) resetNewPasswordInput.value = "";
        if (resetConfirmPasswordInput) resetConfirmPasswordInput.value = "";
        validatePasswordRules("");

        if (!code && resetCodeInput) {
            resetCodeInput.focus();
        } else if (resetNewPasswordInput) {
            resetNewPasswordInput.focus();
        }
    }

    // Toggle events
    if (showForgotBtn) {
        showForgotBtn.addEventListener("click", (e) => {
            e.preventDefault();
            showStep1();
        });
    }

    if (backToSignInBtn) {
        backToSignInBtn.addEventListener("click", (e) => {
            e.preventDefault();
            showSignIn();
        });
    }

    if (cancelResetBtn) {
        cancelResetBtn.addEventListener("click", (e) => {
            e.preventDefault();
            showSignIn();
        });
    }

    if (backToStep1Btn) {
        backToStep1Btn.addEventListener("click", (e) => {
            e.preventDefault();
            showStep1();
        });
    }

    // Format code input (only numbers, max 6 digits)
    if (resetCodeInput) {
        resetCodeInput.addEventListener("input", () => {
            resetCodeInput.value = resetCodeInput.value.replace(/\D/g, "").slice(0, 6);
        });
    }

    // 1. Sign In submission
    if (signInForm) {
        signInForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            if (signInMessage) signInMessage.textContent = "";

            const email = emailInput.value.trim().toLowerCase();
            const password = passwordInput.value;

            if (!email || !password) {
                if (signInMessage) signInMessage.textContent = "Please enter your email and password.";
                return;
            }

            Notiflix.Loading.pulse("Signing you in...");
            try {
                const response = await fetch("api/users/login", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ email: email, password: password })
                });

                if (response.ok) {
                    const data = await response.json();
                    if (data.status) {
                        if (data.data && data.data.userId) {
                            sessionStorage.setItem("st.session_uid", data.data.userId);
                        }
                        Notiflix.Report.success("SmartTrade", data.message, "Continue", () => {
                            const next = new URLSearchParams(location.search).get("next");
                            window.location = next && /^[a-z0-9_-]+\.html/i.test(next) ? next : "index.html";
                        });
                    } else {
                        if (signInMessage) {
                            if (data.message && data.message.toLowerCase().includes("verify")) {
                                signInMessage.innerHTML = `${data.message} <a href="verify-account.html?email=${encodeURIComponent(email)}" class="text-info fw-semibold ms-1">Verify now</a>`;
                            } else {
                                signInMessage.textContent = data.message;
                            }
                        }
                        Notiflix.Notify.failure(data.message);
                    }
                } else {
                    Notiflix.Notify.failure("Login failed! Please try again");
                }
            } catch (err) {
                Notiflix.Notify.failure(err.message);
            } finally {
                Notiflix.Loading.remove(400);
            }
        });
    }

    // 2. Request Password Reset Code
    async function requestResetCode(email) {
        const normEmail = email ? email.trim().toLowerCase() : "";
        if (!normEmail) {
            if (forgotMessage1) forgotMessage1.textContent = "Please enter your email address.";
            return false;
        }
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normEmail)) {
            if (forgotMessage1) forgotMessage1.textContent = "Please enter a valid email address.";
            return false;
        }

        if (forgotMessage1) forgotMessage1.textContent = "";
        Notiflix.Loading.pulse("Sending verification code...");
        try {
            const response = await fetch("api/users/forgot-password", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email: normEmail })
            });

            if (response.ok) {
                const data = await response.json();
                if (data.status) {
                    Notiflix.Notify.success(data.message);
                    showStep2(normEmail, "");
                    return true;
                } else {
                    if (forgotMessage1) forgotMessage1.textContent = data.message;
                    Notiflix.Notify.failure(data.message);
                    return false;
                }
            } else {
                Notiflix.Notify.failure("Request failed! Please try again.");
                return false;
            }
        } catch (err) {
            Notiflix.Notify.failure(err.message);
            return false;
        } finally {
            Notiflix.Loading.remove(400);
        }
    }

    if (forgotEmailForm) {
        forgotEmailForm.addEventListener("submit", (e) => {
            e.preventDefault();
            requestResetCode(forgotEmailInput.value.trim());
        });
    }

    if (resendCodeBtn) {
        resendCodeBtn.addEventListener("click", (e) => {
            e.preventDefault();
            const email = resetEmailHidden.value.trim() || forgotEmailInput.value.trim();
            if (email) {
                requestResetCode(email);
            }
        });
    }

    // 3. Reset Password Submission
    if (resetPasswordForm) {
        resetPasswordForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            if (forgotMessage2) forgotMessage2.textContent = "";

            const email = (resetEmailHidden.value.trim() || "").toLowerCase();
            const code = resetCodeInput.value.trim();
            const newPassword = resetNewPasswordInput.value;
            const confirmPassword = resetConfirmPasswordInput.value;

            if (!email) {
                if (forgotMessage2) forgotMessage2.textContent = "Email is missing. Please start from Step 1.";
                return;
            }
            if (!code || !/^\d{6}$/.test(code)) {
                if (forgotMessage2) forgotMessage2.textContent = "Please enter the valid 6-digit verification code.";
                resetCodeInput.focus();
                return;
            }
            if (!newPassword) {
                if (forgotMessage2) forgotMessage2.textContent = "Please enter your new password.";
                resetNewPasswordInput.focus();
                return;
            }
            if (!validatePasswordRules(newPassword)) {
                if (forgotMessage2) forgotMessage2.textContent = "Password must meet all complexity requirements.";
                resetNewPasswordInput.focus();
                return;
            }
            if (newPassword !== confirmPassword) {
                if (forgotMessage2) forgotMessage2.textContent = "Passwords do not match.";
                resetConfirmPasswordInput.focus();
                return;
            }

            Notiflix.Loading.pulse("Resetting password...");
            try {
                const response = await fetch("api/users/reset-password", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        email: email,
                        verificationCode: code,
                        newPassword: newPassword,
                        confirmPassword: confirmPassword
                    })
                });

                if (response.ok) {
                    const data = await response.json();
                    if (data.status) {
                        Notiflix.Report.success("SmartTrade", data.message, "Sign in now", () => {
                            showSignIn();
                            if (emailInput) emailInput.value = email;
                            if (passwordInput) {
                                passwordInput.value = "";
                                passwordInput.focus();
                            }
                        });
                    } else {
                        if (forgotMessage2) forgotMessage2.textContent = data.message;
                        Notiflix.Notify.failure(data.message);
                    }
                } else {
                    Notiflix.Notify.failure("Reset failed! Please try again.");
                }
            } catch (err) {
                Notiflix.Notify.failure(err.message);
            } finally {
                Notiflix.Loading.remove(400);
            }
        });
    }

    // Check URL parameters for direct reset link or action
    const params = new URLSearchParams(window.location.search);
    const action = params.get("action");
    const paramEmail = params.get("email");
    const paramCode = params.get("code");

    if (action === "reset" || (paramEmail && paramCode)) {
        showStep2(paramEmail || "", paramCode || "");
    } else if (action === "forgot") {
        showStep1();
    }
})();
