/**
 * Home Service Booking - Main JavaScript (js/main.js)
 * Global Application State, Data Initialization & Shared Utilities
 * 
 * Future Java Spring Boot REST Architecture:
 * - AuthController: /api/auth/login, /api/auth/register, /api/auth/logout
 * - ServiceController: /api/services (GET, PUT /prices)
 * - BookingController: /api/bookings (POST, GET /user, GET /admin, PUT /{id}/status, DELETE /{id})
 * - ContactController: /api/contact (POST, GET)
 */

// ==========================================
// 1. Initial Default Datasets (Prompt Specified)
// ==========================================
const DEFAULT_SERVICES = [
  {
    id: 1,
    code: "KITCHEN",
    name: "Kitchen Cleaning",
    price: 999,
    image: "images/services/kitchen.jpg",
    description: "Deep degreasing, chimney scrubbing, countertop disinfection, sink descaling, and appliance exterior sanitization.",
    features: ["Oil & Grease Removal", "Countertops & Cabinets", "Sink & Drain Sanitization", "Chimney & Hob Cleaning"]
  },
  {
    id: 2,
    code: "BEDROOM",
    name: "Bedroom Cleaning",
    price: 850,
    image: "images/services/bedroom.jpg",
    description: "Comprehensive dusting, mattress vacuuming, wardrobe exterior cleaning, mirror polishing, and floor disinfection.",
    features: ["Dusting & Cobweb Removal", "Mattress Vacuuming", "Wardrobe Wiping", "Floor Mopping & Fragrance"]
  },
  {
    id: 3,
    code: "BATHROOM",
    name: "Bathroom Cleaning",
    price: 1000,
    image: "images/services/bathroom.jpg",
    description: "Anti-bacterial tile scrubbing, hard-water stain removal, toilet sanitization, fixture descaling, and odor treatment.",
    features: ["Tile & Grout Acid Scrub", "Hard Water Stain Removal", "Commode & Washbasin Sanitization", "Mirror & Tap Polishing"]
  },
  {
    id: 4,
    code: "DEEP",
    name: "Deep Cleaning",
    price: 1500,
    image: "images/services/deep-cleaning.jpg",
    description: "High-intensity top-to-bottom sanitization using hospital-grade disinfectants, steam cleaners, and scrubbing machines.",
    features: ["Industrial Machine Scrubbing", "Steam Sterilization", "Hidden Dirt & Crevice Cleanse", "Full Odor Elimination"]
  },
  {
    id: 5,
    code: "CORRIDOR",
    name: "Corridor Cleaning",
    price: 1000,
    image: "images/services/corridor.jpg",
    description: "Hallway floor scrubbing, skirting board polishing, shoe rack dusting, doorway cleaning, and light fixture dusting.",
    features: ["High-Traffic Floor Buffing", "Skirting Boards & Switches", "Shoe Rack & Mat Sanitization", "Spiderweb Removal"]
  },
  {
    id: 6,
    code: "HALL",
    name: "Hall Cleaning",
    price: 1300,
    image: "images/services/hall.jpg",
    description: "Living area upholstery vacuuming, sofa shampooing, TV unit detailing, ceiling fan wiping, and floor buffing.",
    features: ["Sofa & Carpet Vacuuming", "TV Console & Electronics Dusting", "Ceiling Fan & Chandelier Detailing", "Marble/Tile Floor Buffing"]
  },
  {
    id: 7,
    code: "FULL_HOME",
    name: "Full Home Cleaning",
    price: 1999,
    image: "images/services/full-home.jpg",
    description: "Complete turnkey home rejuvenation covering kitchen, all bedrooms, bathrooms, living hall, balconies, and corridors.",
    features: ["Complete All-Room Deep Scrub", "Full Sanitization & Deodorizing", "Eco-friendly Biodegradable Solutions", "Dedicated Team of Cleaners"]
  }
];

const DEFAULT_USERS = [
  {
    id: 101,
    fullName: "Rahul Sharma",
    email: "user@example.com",
    mobile: "9876543210",
    password: "User@123",
    role: "ROLE_USER",
    createdAt: "2026-11-01T10:00:00Z"
  },
  {
    id: 102,
    fullName: "Priya Sundaram",
    email: "priya@example.com",
    mobile: "9876543211",
    password: "Priya@123",
    role: "ROLE_USER",
    createdAt: "2026-11-02T10:00:00Z"
  }
];

const DEFAULT_ADMIN = {
  id: 1,
  fullName: "System Administrator",
  email: "admin@homeservice.com",
  password: "Admin@123",
  role: "ROLE_ADMIN",
  isSystemAdmin: true
};

const DEFAULT_BOOKINGS = [
  {
    id: "SHC-2026-89102",
    userId: 101,
    customerName: "Rahul Sharma",
    customerMobile: "9876543210",
    customerEmail: "user@example.com",
    customerAddress: "Flat 402, Green Meadows, Sector 14, Bengaluru",
    specialInstructions: "Please use pet-safe floor cleaning liquid.",
    teamPreference: "Female",
    selectedServices: [
      { id: 1, name: "Kitchen Cleaning", price: 999 },
      { id: 3, name: "Bathroom Cleaning", price: 1000 }
    ],
    roomSelectionType: "All Rooms",
    roomCount: 2,
    selectedRooms: ["Kitchen", "Bathroom"],
    bookingDate: "2026-11-22",
    bookingTime: "09:00 AM - 11:00 AM",
    totalAmount: 3998,
    status: "Confirmed",
    createdAt: "2026-11-15T09:30:00Z"
  },
  {
    id: "SHC-2026-73629",
    userId: null,
    customerName: "Ananya Deshmukh",
    customerMobile: "9812345678",
    customerEmail: "ananya.d@gmail.com",
    customerAddress: "Bungalow 7, Silver Woods, Mumbai",
    specialInstructions: "Deep clean all bathroom wall tiles thoroughly.",
    teamPreference: "Male",
    selectedServices: [
      { id: 7, name: "Full Home Cleaning", price: 1999 }
    ],
    roomSelectionType: "All Rooms",
    roomCount: 1,
    selectedRooms: ["All Rooms"],
    bookingDate: "2026-11-25",
    bookingTime: "11:00 AM - 01:00 PM",
    totalAmount: 1999,
    status: "Pending",
    createdAt: "2026-11-16T14:15:00Z"
  },
  {
    id: "SHC-2026-55201",
    userId: null,
    customerName: "Vikram Malhotra",
    customerMobile: "9745123680",
    customerEmail: "vikram.m@outlook.com",
    customerAddress: "Villa 22, Palm Grove Estate, Delhi",
    specialInstructions: "Handle vintage glass cabinets with extra care.",
    teamPreference: "Male",
    selectedServices: [
      { id: 4, name: "Deep Cleaning", price: 1500 },
      { id: 6, name: "Hall Cleaning", price: 1300 }
    ],
    roomSelectionType: "All Rooms",
    roomCount: 2,
    selectedRooms: ["Hall", "Living Area"],
    bookingDate: "2026-11-10",
    bookingTime: "02:00 PM - 04:00 PM",
    totalAmount: 5600,
    status: "Completed",
    createdAt: "2026-11-08T11:00:00Z"
  }
];

const DEFAULT_MESSAGES = [
  {
    id: 1,
    name: "Sunita Roy",
    email: "sunita.roy@gmail.com",
    phone: "9823114455",
    message: "Do you offer post-renovation paint stain removal for full apartments?",
    createdAt: "2026-11-14T16:20:00Z"
  }
];

// ==========================================
// 2. Storage Initialization
// ==========================================
function initAppStorage() {
  // Always verify service prices match current prompt specification
  const currentServices = localStorage.getItem("shc_services");
  if (!currentServices) {
    localStorage.setItem("shc_services", JSON.stringify(DEFAULT_SERVICES));
  } else {
    try {
      const parsed = JSON.parse(currentServices);
      // If old dummy prices exist (e.g. kitchen was 499), reset to exact prompt prices
      if (parsed[0] && parsed[0].price === 499) {
        localStorage.setItem("shc_services", JSON.stringify(DEFAULT_SERVICES));
      }
    } catch (e) {
      localStorage.setItem("shc_services", JSON.stringify(DEFAULT_SERVICES));
    }
  }

  if (!localStorage.getItem("shc_users")) {
    localStorage.setItem("shc_users", JSON.stringify(DEFAULT_USERS));
  } else {
    try {
      const existing = JSON.parse(localStorage.getItem("shc_users")) || [];
      DEFAULT_USERS.forEach(defU => {
        if (!existing.some(u => u.email.toLowerCase() === defU.email.toLowerCase())) {
          existing.push(defU);
        }
      });
      localStorage.setItem("shc_users", JSON.stringify(existing));
    } catch (e) {}
  }
  if (!localStorage.getItem("shc_admin_credential")) {
    localStorage.setItem("shc_admin_credential", JSON.stringify(DEFAULT_ADMIN));
  }
  if (!localStorage.getItem("shc_bookings")) {
    localStorage.setItem("shc_bookings", JSON.stringify(DEFAULT_BOOKINGS));
  }
  if (!localStorage.getItem("shc_messages")) {
    localStorage.setItem("shc_messages", JSON.stringify(DEFAULT_MESSAGES));
  }
}

// Retrieve dynamic services
function getAppServices() {
  initAppStorage();
  try {
    return JSON.parse(localStorage.getItem("shc_services")) || DEFAULT_SERVICES;
  } catch (e) {
    return DEFAULT_SERVICES;
  }
}

// Synchronize live services and prices from MySQL backend
async function syncLiveServices() {
  try {
    const res = await fetch("http://localhost:8080/api/services");
    if (res.ok) {
      const data = await res.json();
      if (data && data.data && data.data.length > 0) {
        const services = data.data.map(s => {
          const existing = (typeof DEFAULT_SERVICES !== "undefined" ? DEFAULT_SERVICES : []).find(d => d.name.toLowerCase() === s.name.toLowerCase()) || {};
          return {
            id: s.id,
            code: existing.code || s.name.toUpperCase().replace(/\s+/g, "_"),
            name: s.name,
            description: s.description || existing.description || "",
            image: s.imageUrl || existing.image || "images/services/full-home.jpg",
            price: Math.round(s.basePrice != null ? s.basePrice : (existing.price || 999)),
            features: existing.features || ["Quality Sanitization", "Eco-friendly Cleaners", "Expert Equipment", "100% Satisfaction"]
          };
        });
        localStorage.setItem("shc_services", JSON.stringify(services));
        return services;
      }
    }
  } catch (e) {
    // Backend offline; use local storage fallback
  }
  return getAppServices();
}

// Retrieve current logged in user (or null)
function getCurrentUser() {
  try {
    const userStr = localStorage.getItem("shc_current_user") || sessionStorage.getItem("shc_current_user");
    return userStr ? JSON.parse(userStr) : null;
  } catch (e) {
    return null;
  }
}

// Retrieve current admin session
function getAdminSession() {
  try {
    const adminStr = sessionStorage.getItem("shc_admin_session") || localStorage.getItem("shc_admin_session");
    if (!adminStr) return null;
    const admin = JSON.parse(adminStr);
    if (admin && admin.role === "ROLE_ADMIN") {
      return admin;
    }
    return null;
  } catch (e) {
    return null;
  }
}

// ==========================================
// 3. Global Navbar & Authentication Rendering
// ==========================================
function renderNavbarAuth() {
  const navActions = document.getElementById("navAuthActions");
  if (!navActions) return;

  const currentUser = getCurrentUser();
  const currentAdmin = getAdminSession();

  if (currentUser) {
    navActions.innerHTML = `
      <div class="user-badge" title="${currentUser.email}">
        <i class="fas fa-user-circle"></i>
        <span>Hi, ${currentUser.fullName.split(" ")[0]}</span>
      </div>
      <button class="btn btn-outline btn-sm" onclick="handleUserLogout()">
        <i class="fas fa-sign-out-alt"></i> Logout
      </button>
    `;
  } else if (currentAdmin) {
    navActions.innerHTML = `
      <a href="admin-dashboard.html" class="user-badge" style="background:#fee2e2;color:#b91c1c;">
        <i class="fas fa-shield-alt"></i>
        <span>Admin Panel</span>
      </a>
      <button class="btn btn-outline btn-sm" onclick="handleAdminLogout()">
        <i class="fas fa-sign-out-alt"></i> Logout
      </button>
    `;
  } else {
    navActions.innerHTML = `
      <a href="login.html" class="btn btn-outline-primary btn-sm">
        <i class="fas fa-user"></i> Login / Register
      </a>
      <a href="booking.html" class="btn btn-primary btn-sm">
        <i class="fas fa-calendar-check"></i> Book Now
      </a>
    `;
  }
}

function handleUserLogout() {
  localStorage.removeItem("shc_current_user");
  sessionStorage.removeItem("shc_current_user");
  showToast("Logged out successfully.", "info");
  setTimeout(() => {
    window.location.href = "index.html";
  }, 500);
}

function handleAdminLogout() {
  sessionStorage.removeItem("shc_admin_session");
  localStorage.removeItem("shc_admin_session");
  showToast("Admin signed out.", "info");
  setTimeout(() => {
    window.location.href = "admin-login.html";
  }, 500);
}

// Setup active link highlighting based on current pathname
function highlightActiveNav() {
  const currentPath = window.location.pathname.split("/").pop() || "index.html";
  const navLinks = document.querySelectorAll(".nav-menu .nav-link");
  navLinks.forEach(link => {
    const href = link.getAttribute("href");
    if (href === currentPath || (currentPath === "" && href === "index.html")) {
      link.classList.add("active");
    } else {
      link.classList.remove("active");
    }
  });
}

// Mobile toggle handler
function setupMobileMenu() {
  const mobileToggle = document.getElementById("mobileToggle");
  const navMenu = document.getElementById("navMenu");
  if (mobileToggle && navMenu) {
    mobileToggle.addEventListener("click", () => {
      navMenu.classList.toggle("open");
      const icon = mobileToggle.querySelector("i");
      if (icon) {
        icon.classList.toggle("fa-bars");
        icon.classList.toggle("fa-times");
      }
    });
  }
}

// ==========================================
// 4. Toast Notification System
// ==========================================
function showToast(message, type = "success") {
  let container = document.getElementById("toastContainer");
  if (!container) {
    container = document.createElement("div");
    container.id = "toastContainer";
    container.className = "toast-container";
    document.body.appendChild(container);
  }

  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;
  toast.style.background = "#ffffff";
  toast.style.color = "#0f172a";
  
  let iconClass = "fa-check-circle";
  let iconColor = "#10b981";
  if (type === "error") { iconClass = "fa-exclamation-triangle"; iconColor = "#ef4444"; }
  if (type === "warning") { iconClass = "fa-exclamation-circle"; iconColor = "#f59e0b"; }
  if (type === "info") { iconClass = "fa-info-circle"; iconColor = "#0284c7"; }

  toast.innerHTML = `
    <i class="fas ${iconClass}" style="font-size: 1.25rem; color: ${iconColor};"></i>
    <div style="flex:1; font-weight: 600; font-size: 0.92rem; color: #0f172a !important;">${message}</div>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transform = "translateX(50px)";
    toast.style.transition = "all 0.3s ease";
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

// Format Currency in INR (₹)
function formatINR(amount) {
  return "₹" + Number(amount).toLocaleString("en-IN");
}

// ==========================================
// 5. DOM Ready Execution
// ==========================================
document.addEventListener("DOMContentLoaded", () => {
  initAppStorage();
  syncLiveServices();
  if (typeof validateUserSession === "function") {
    validateUserSession();
  }
  renderNavbarAuth();
  highlightActiveNav();
  setupMobileMenu();
});
