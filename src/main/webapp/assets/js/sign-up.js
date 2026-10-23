/* Sign up : POST api/users */
const signUpForm = document.getElementById("signUpForm");
const pwInput = document.getElementById("password");

signUpForm.addEventListener("submit", (e) => {
    e.preventDefault();
    signUp();
});

/* password strength feedback - same rules as the server validation */
const PW_RULES = {
    len: (v) => v.length >= 8 && v.length <= 20,
    num: (v) => /\d/.test(v),
    low: (v) => /[a-z]/.test(v),
    up: (v) => /[A-Z]/.test(v),
    sp: (v) => /[@#$%^&+=_!\-*()[\]{}|;:,.<>?/~`]/.test(v)
};

pwInput.addEventListener("input", () => {
    const v = pwInput.value;
    let score = 0;
    Object.keys(PW_RULES).forEach((k) => {
        const ok = PW_RULES[k](v);
        const el = document.querySelector('#pwRules [data-r="' + k + '"]');
        if (el) el.classList.toggle("ok", ok);
        if (ok) score++;
    });
    const meter = document.getElementById("pwMeter");
    if (meter) {
        meter.dataset.s = v ? Math.max(1, Math.min(4, score - (score === 5 ? 1 : 0))) : 0;
    }
});

async function signUp() {
    const firstName = document.getElementById("firstName");
    const lastName = document.getElementById("lastName");
    const email = document.getElementById("email");
    const message = document.getElementById("message");
    message.textContent = "";

    const fn = firstName.value.trim();
    const ln = lastName.value.trim();
    const em = email.value.trim().toLowerCase();
    const pw = pwInput.value;

    if (!fn || !ln || !em || !pw) {
        message.textContent = "Please fill in every field.";
        Notiflix.Notify.warning("Please fill in every field.");
        return;
    }

    if (fn.length > 45) {
        message.textContent = "First name must not exceed 45 characters.";
        firstName.focus();
        return;
    }

    if (ln.length > 45) {
        message.textContent = "Last name must not exceed 45 characters.";
        lastName.focus();
        return;
    }

    const emailRegex = /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,}$/;
    if (!emailRegex.test(em)) {
        message.textContent = "Please provide a valid email address.";
        email.focus();
        return;
    }

    if (!PW_RULES.len(pw) || !PW_RULES.num(pw) || !PW_RULES.low(pw) || !PW_RULES.up(pw) || !PW_RULES.sp(pw)) {
        message.textContent = "Password must be 8-20 characters long and include uppercase, lowercase, digit, and special character.";
        pwInput.focus();
        return;
    }

    Notiflix.Loading.pulse("Creating your account...");
    try {
        const response = await fetch("api/users", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                firstName: fn,
                lastName: ln,
                email: em,
                password: pw
            })
        });

        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                signUpForm.reset();
                const meter = document.getElementById("pwMeter");
                if (meter) meter.dataset.s = 0;
                document.querySelectorAll("#pwRules [data-r]").forEach(el => el.classList.remove("ok"));
                Notiflix.Report.success("SmartTrade", data.message, "Verify Account", () => {
                    window.location = "verify-account.html?email=" + encodeURIComponent(em);
                });
            } else {
                message.textContent = data.message;
                Notiflix.Notify.failure(data.message);
            }
        } else {
            Notiflix.Notify.failure("Something went wrong. Please check your details");
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message);
    } finally {
        Notiflix.Loading.remove(400);
    }
}
