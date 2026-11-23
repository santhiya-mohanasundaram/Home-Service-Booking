/**
 * Home Service Booking - Authentication Module (js/auth.js)
 * User Login, Registration & Security Guards
 * 
 * Future Spring Boot integration note:
 * POST /api/auth/login -> JWT token response
 * POST /api/auth/register -> User entity created in MySQL
 */

document.addEventListener("DOMContentLoaded", () => {
  initAppStorage();

  // Handle User Login Form
  const loginForm = document.getElementById("userLoginForm");
  if (loginForm) {
    loginForm.addEventListener("submit", handleUserLogin);
  }

  // Handle User Registration Form
  const registerForm = document.getElementById("userRegisterForm");
  if (registerForm) {
    registerForm.addEventListener("submit", handleUserRegistration);
  }

  // Toggle Password Visibility buttons
  setupPasswordToggles();

  // Fill demo credentials helpers
  const fillDemoBtn = document.getElementById("fillDemoUserBtn");
  if (fillDemoBtn) {
    fillDemoBtn.addEventListener("click", () => {
      const emailInput = document.getElementById("loginEmail");
      const passInput = document.getElementById("loginPassword");
      if (emailInput && passInput) {
        emailInput.value = "user@example.com";
        passInput.value = "User@123";
        showToast("Demo user 1 (Rahul) credentials filled!", "info");
      }
    });
  }

  const fillDemo2Btn = document.getElementById("fillDemoUser2Btn");
  if (fillDemo2Btn) {
    fillDemo2Btn.addEventListener("click", () => {
      const emailInput = document.getElementById("loginEmail");
      const passInput = document.getElementById("loginPassword");
      if (emailInput && passInput) {
        emailInput.value = "priya@example.com";
        passInput.value = "Priya@123";
        showToast("Demo user 2 (Priya) credentials filled!", "info");
      }
    });
  }
});

// Password visibility toggler
function setupPasswordToggles() {
  const toggleBtns = document.querySelectorAll(".password-toggle-btn");
  toggleBtns.forEach(btn => {
    btn.addEventListener("click", () => {
      const targetId = btn.getAttribute("data-target");
      const input = document.getElementById(targetId);
      if (input) {
        if (input.type === "password") {
          input.type = "text";
          btn.innerHTML = '<i class="fas fa-eye-slash"></i>';
        } else {
          input.type = "password";
          btn.innerHTML = '<i class="fas fa-eye"></i>';
        }
      }
    });
  });
}

// User Login Function (Connected to Spring Boot API)
async function handleUserLogin(e) {
  e.preventDefault();

  const emailInput = document.getElementById("loginEmail");
  const passwordInput = document.getElementById("loginPassword");
  const rememberMe = document.getElementById("rememberMe") ? document.getElementById("rememberMe").checked : false;

  const email = emailInput.value.trim().toLowerCase();
  const password = passwordInput.value;

  if (!email || !password) {
    showToast("Please fill in both Email and Password.", "error");
    return;
  }

  try {
    const res = await fetch("http://localhost:8080/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: email, password: password })
    });

    const resData = await res.json();
    if (res.ok && resData && resData.data && resData.data.token) {
      const authUser = resData.data;
      const sessionUser = {
        id: authUser.id,
        fullName: authUser.name || authUser.fullName || email.split("@")[0],
        email: authUser.email,
        mobile: authUser.phone || authUser.mobile || "9876543210",
        role: authUser.role || "ROLE_USER",
        token: authUser.token
      };

      if (rememberMe) {
        localStorage.setItem("shc_current_user", JSON.stringify(sessionUser));
      } else {
        sessionStorage.setItem("shc_current_user", JSON.stringify(sessionUser));
        localStorage.setItem("shc_current_user", JSON.stringify(sessionUser));
      }

      showToast(`Welcome back, ${sessionUser.fullName}!`, "success");

      const redirectTarget = sessionStorage.getItem("shc_redirect_after_login") || "booking.html";
      sessionStorage.removeItem("shc_redirect_after_login");

      setTimeout(() => {
        window.location.href = redirectTarget;
      }, 800);
      return;
    } else {
      const errorMsg = (resData && (resData.message || resData.error)) || "Invalid email or password.";
      showToast(errorMsg, "error");
      return;
    }
  } catch (err) {
    console.warn("Backend offline or connection error, using local fallback:", err);
    // Offline local fallback
    const users = JSON.parse(localStorage.getItem("shc_users")) || [];
    const matchedUser = users.find(u => u.email.toLowerCase() === email && u.password === password);

    if (!matchedUser) {
      showToast("Invalid email or password. Please try again.", "error");
      return;
    }

    const sessionUser = {
      id: matchedUser.id,
      fullName: matchedUser.fullName,
      email: matchedUser.email,
      mobile: matchedUser.mobile,
      role: matchedUser.role || "ROLE_USER",
      token: "jwt_mock_token_" + Date.now()
    };

    if (rememberMe) {
      localStorage.setItem("shc_current_user", JSON.stringify(sessionUser));
    } else {
      sessionStorage.setItem("shc_current_user", JSON.stringify(sessionUser));
      localStorage.setItem("shc_current_user", JSON.stringify(sessionUser));
    }

    showToast(`Welcome back, ${matchedUser.fullName}!`, "success");
    const redirectTarget = sessionStorage.getItem("shc_redirect_after_login") || "booking.html";
    sessionStorage.removeItem("shc_redirect_after_login");
    setTimeout(() => { window.location.href = redirectTarget; }, 800);
  }
}

// User Registration Function (Connected to Spring Boot API)
async function handleUserRegistration(e) {
  e.preventDefault();

  const fullNameInput = document.getElementById("regFullName");
  const emailInput = document.getElementById("regEmail");
  const mobileInput = document.getElementById("regMobile");
  const passInput = document.getElementById("regPassword");
  const confirmPassInput = document.getElementById("regConfirmPassword");

  const fullName = fullNameInput.value.trim();
  const email = emailInput.value.trim().toLowerCase();
  const mobile = mobileInput.value.trim();
  const password = passInput.value;
  const confirmPassword = confirmPassInput.value;

  // Validation
  if (!fullName || !email || !mobile || !password || !confirmPassword) {
    showToast("All fields are required.", "error");
    return;
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    showToast("Please enter a valid email address.", "error");
    return;
  }

  const phoneRegex = /^[6-9]\d{9}$/;
  if (!phoneRegex.test(mobile)) {
    showToast("Please enter a valid 10-digit mobile number.", "error");
    return;
  }

  if (password.length < 6) {
    showToast("Password must be at least 6 characters long.", "error");
    return;
  }

  if (password !== confirmPassword) {
    showToast("Passwords do not match.", "error");
    return;
  }

  try {
    const registerPayload = {
      name: fullName,
      email: email,
      password: password,
      phone: mobile,
      address: "Home Service Customer Address"
    };

    const res = await fetch("http://localhost:8080/api/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(registerPayload)
    });

    const resData = await res.json();
    if (res.ok && resData && resData.data && resData.data.token) {
      const authUser = resData.data;
      const sessionUser = {
        id: authUser.id,
        fullName: authUser.name || fullName,
        email: authUser.email || email,
        mobile: mobile,
        role: authUser.role || "ROLE_USER",
        token: authUser.token
      };

      // Save user to storage
      const users = JSON.parse(localStorage.getItem("shc_users")) || [];
      users.push({
        id: authUser.id,
        fullName: fullName,
        email: email,
        mobile: mobile,
        password: password,
        role: "ROLE_USER",
        token: authUser.token,
        createdAt: new Date().toISOString()
      });
      localStorage.setItem("shc_users", JSON.stringify(users));
      localStorage.setItem("shc_current_user", JSON.stringify(sessionUser));

      showToast("Account created successfully in MySQL! Redirecting...", "success");

      const redirectTarget = sessionStorage.getItem("shc_redirect_after_login") || "booking.html";
      sessionStorage.removeItem("shc_redirect_after_login");

      setTimeout(() => {
        window.location.href = redirectTarget;
      }, 1000);
      return;
    } else {
      const errorMsg = (resData && (resData.message || resData.error)) || "Registration failed. Email might already exist.";
      showToast(errorMsg, "error");
      return;
    }
  } catch (err) {
    console.warn("Backend offline during registration, saving locally:", err);
    const users = JSON.parse(localStorage.getItem("shc_users")) || [];
    const exists = users.some(u => u.email.toLowerCase() === email);
    if (exists) {
      showToast("An account with this email already exists.", "warning");
      return;
    }

    const newUser = {
      id: Date.now(),
      fullName: fullName,
      email: email,
      mobile: mobile,
      password: password,
      role: "ROLE_USER",
      token: "jwt_mock_token_" + Date.now(),
      createdAt: new Date().toISOString()
    };

    users.push(newUser);
    localStorage.setItem("shc_users", JSON.stringify(users));
    localStorage.setItem("shc_current_user", JSON.stringify(newUser));

    showToast("Registration successful! Redirecting...", "success");
    const redirectTarget = sessionStorage.getItem("shc_redirect_after_login") || "booking.html";
    sessionStorage.removeItem("shc_redirect_after_login");
    setTimeout(() => { window.location.href = redirectTarget; }, 1000);
  }
}

// Session validation and safe cleanup helpers
function validateUserSession() {
  const user = getCurrentUser();
  if (user && user.role !== "ROLE_USER") {
    // Sanitize any invalid or spoofed role back to normal customer
    user.role = "ROLE_USER";
    localStorage.setItem("shc_current_user", JSON.stringify(user));
  }
  return user;
}

function clearUserSession() {
  localStorage.removeItem("shc_current_user");
  sessionStorage.removeItem("shc_current_user");
}
