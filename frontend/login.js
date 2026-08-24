// ============================================================
// CounterCheck Login Script
// Connects to Spring Boot REST API (http://localhost:8081/api/auth/login)
// ============================================================

const API_BASE_URL = "http://localhost:8081/api";

// Show / Hide Password
const togglePassword = document.getElementById("togglePassword");
const password = document.getElementById("password");

if (togglePassword && password) {
    togglePassword.addEventListener("click", function () {
        if (password.type === "password") {
            password.type = "text";
            this.classList.remove("fa-eye");
            this.classList.add("fa-eye-slash");
        } else {
            password.type = "password";
            this.classList.remove("fa-eye-slash");
            this.classList.add("fa-eye");
        }
    });
}


// Login Form Submission & Animation
const loginForm = document.getElementById("loginForm");

if (loginForm) {
    loginForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const button = document.querySelector(".login-btn");
        const originalButtonHtml = button ? button.innerHTML : "Login";

        const email = document.getElementById("email").value.trim();
        const passwordVal = document.getElementById("password").value;

        if (button) {
            button.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Logging in...';
            button.style.pointerEvents = "none";
        }

        try {
            const response = await fetch(`${API_BASE_URL}/auth/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password: passwordVal })
            });

            const data = await response.json();

            if (response.ok) {
                localStorage.setItem("counterCheckCurrentUser", JSON.stringify(data));
                window.location.href = "upload.html";
            } else {
                alert(data.error || data.message || "Invalid email or password.");
                if (button) {
                    button.innerHTML = originalButtonHtml;
                    button.style.pointerEvents = "auto";
                }
            }
        } catch (error) {
            console.error("Login Error:", error);
            alert("Could not connect to Spring Boot backend. Please ensure the backend is running on http://localhost:8081");
            if (button) {
                button.innerHTML = originalButtonHtml;
                button.style.pointerEvents = "auto";
            }
        }
    });
}
