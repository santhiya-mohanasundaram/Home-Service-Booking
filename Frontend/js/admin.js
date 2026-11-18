/**
 * Home Service Booking - Admin Management Module (js/admin.js)
 * STRICT ADMIN ACCESS CONTROL, DASHBOARD ANALYTICS & CRUD OPERATIONS
 * 
 * Future Spring Boot integration note:
 * All operations will be guarded by Spring Security @PreAuthorize("hasRole('ADMIN')")
 * with signed JWT tokens validated on the backend.
 */

document.addEventListener("DOMContentLoaded", () => {
  initAppStorage();

  const isDashboard = document.getElementById("adminDashboardView");
  const isLoginPage = document.getElementById("adminLoginForm");

  if (isDashboard) {
    // 1. Strictly verify Admin authorization
    verifyAdminAccess();

    // 2. Initialize Dashboard components
    renderAdminStats();
    renderBookingsTable();
    renderServicesPriceManager();
    renderContactMessagesTable();

    // Setup Admin search & filters
    setupAdminFilters();
  }

  if (isLoginPage) {
    setupAdminLoginForm();
  }
});

// Admin Login Form Handler
function setupAdminLoginForm() {
  const form = document.getElementById("adminLoginForm");
  const fillBtn = document.getElementById("fillDemoAdminBtn");

  if (fillBtn) {
    fillBtn.addEventListener("click", () => {
      document.getElementById("adminEmail").value = "admin@homeservice.com";
      document.getElementById("adminPassword").value = "Admin@123";
      showToast("Admin credentials filled!", "info");
    });
  }

  if (form) {
    form.addEventListener("submit", (e) => {
      e.preventDefault();
      const email = document.getElementById("adminEmail").value.trim().toLowerCase();
      const password = document.getElementById("adminPassword").value;

      const adminCred = JSON.parse(localStorage.getItem("shc_admin_credential")) || {
        email: "admin@homeservice.com",
        password: "Admin@123",
        role: "ROLE_ADMIN"
      };

      if (email === adminCred.email.toLowerCase() && password === adminCred.password) {
        // Create secure mock admin session
        const adminSession = {
          email: adminCred.email,
          role: "ROLE_ADMIN",
          token: "jwt_mock_admin_token_" + Date.now(),
          loginTime: new Date().toISOString()
        };

        sessionStorage.setItem("shc_admin_session", JSON.stringify(adminSession));
        localStorage.setItem("shc_admin_session", JSON.stringify(adminSession));

        showToast("Admin authenticated successfully!", "success");
        setTimeout(() => {
          window.location.href = "admin-dashboard.html";
        }, 800);
      } else {
        showToast("Access Denied: Invalid admin credentials.", "error");
      }
    });
  }
}

// Security Check: Strictly guard Admin Dashboard
function verifyAdminAccess() {
  const adminSession = getAdminSession();
  const currentUser = getCurrentUser();

  // If normal user attempts to access or no admin session exists
  if (!adminSession || adminSession.role !== "ROLE_ADMIN") {
    // Show Access Denied UI immediately & redirect
    document.body.innerHTML = `
      <div style="min-height:100vh; display:flex; align-items:center; justify-content:center; background:#0f172a; font-family:'Plus Jakarta Sans',sans-serif; padding:1.5rem;">
        <div style="background:#1e293b; border:1px solid #ef4444; border-radius:18px; padding:3rem 2rem; max-width:520px; text-align:center; color:#fff; box-shadow:0 25px 50px -12px rgba(0,0,0,0.5);">
          <div style="width:72px; height:72px; background:rgba(239,68,68,0.15); color:#ef4444; border-radius:50%; display:flex; align-items:center; justify-content:center; font-size:2.2rem; margin:0 auto 1.5rem;">
            <i class="fas fa-lock"></i>
          </div>
          <h2 style="font-family:'Outfit',sans-serif; font-size:1.8rem; margin-bottom:0.75rem; color:#f87171;">Access Denied (403 Forbidden)</h2>
          <p style="color:#94a3b8; font-size:0.95rem; line-height:1.6; margin-bottom:1.8rem;">
            This area is strictly restricted to authorized administrators. Regular users and unauthenticated guests do not have permission to access the Admin Dashboard.
          </p>
          <div style="display:flex; gap:1rem; justify-content:center;">
            <a href="index.html" style="background:#334155; color:#fff; padding:0.75rem 1.4rem; border-radius:9999px; text-decoration:none; font-weight:600; font-size:0.9rem;">
              Back to Home
            </a>
            <a href="admin-login.html" style="background:#ef4444; color:#fff; padding:0.75rem 1.4rem; border-radius:9999px; text-decoration:none; font-weight:600; font-size:0.9rem;">
              Admin Login
            </a>
          </div>
        </div>
      </div>
    `;
    setTimeout(() => {
      window.location.href = "admin-login.html";
    }, 3000);
    throw new Error("Unauthorized admin access blocked.");
  }
}

// 1. Render Admin KPI Statistics Cards
function renderAdminStats() {
  const bookings = JSON.parse(localStorage.getItem("shc_bookings")) || [];

  const total = bookings.length;
  const pending = bookings.filter(b => b.status === "Pending").length;
  const confirmed = bookings.filter(b => b.status === "Confirmed").length;
  const completed = bookings.filter(b => b.status === "Completed").length;
  const cancelled = bookings.filter(b => b.status === "Cancelled").length;

  document.getElementById("statTotalBookings").textContent = total;
  document.getElementById("statPendingBookings").textContent = pending;
  document.getElementById("statConfirmedBookings").textContent = confirmed;
  document.getElementById("statCompletedBookings").textContent = completed;
  document.getElementById("statCancelledBookings").textContent = cancelled;
}

// 2. Render All Bookings in responsive Data Table
function renderBookingsTable(filterStatus = "ALL", searchQuery = "") {
  const tbody = document.getElementById("adminBookingsTableBody");
  if (!tbody) return;

  let bookings = JSON.parse(localStorage.getItem("shc_bookings")) || [];

  // Filter by status
  if (filterStatus !== "ALL") {
    bookings = bookings.filter(b => b.status === filterStatus);
  }

  // Filter by search query (id, customer name, mobile, email)
  if (searchQuery) {
    const q = searchQuery.toLowerCase();
    bookings = bookings.filter(b => 
      b.id.toLowerCase().includes(q) ||
      b.customerName.toLowerCase().includes(q) ||
      b.customerMobile.includes(q) ||
      b.customerEmail.toLowerCase().includes(q)
    );
  }

  if (bookings.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" style="text-align:center; padding:2.5rem; color:var(--text-muted);">
          <i class="fas fa-inbox" style="font-size:2rem; margin-bottom:0.5rem; display:block;"></i>
          No bookings found matching criteria.
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = bookings.map(b => {
    let badgeClass = "badge-primary";
    if (b.status === "Pending") badgeClass = "badge-warning";
    if (b.status === "Confirmed") badgeClass = "badge-success";
    if (b.status === "Completed") badgeClass = "badge-primary";
    if (b.status === "Cancelled") badgeClass = "badge-danger";

    const servicesNames = b.selectedServices.map(s => s.name).join(", ");

    return `
      <tr id="row-booking-${b.id}">
        <td>
          <strong style="color:var(--secondary); font-family:monospace; font-size:0.95rem;">${b.id}</strong>
          <div style="font-size:0.8rem; color:var(--text-muted);">${new Date(b.createdAt).toLocaleDateString()}</div>
        </td>
        <td>
          <strong>${b.customerName}</strong>
          <div style="font-size:0.85rem; color:var(--text-muted); margin-top:2px;">
            <i class="fas fa-phone-alt" style="font-size:0.75rem; color:var(--primary);"></i> ${b.customerMobile}
          </div>
        </td>
        <td>
          <div style="font-size:0.88rem; font-weight:600; max-width:200px;">${servicesNames}</div>
          <div style="font-size:0.8rem; color:var(--primary); font-weight:700;">
            ${b.roomCount} Room(s) &bull; ${b.teamPreference || "Any"} Cleaner
          </div>
        </td>
        <td>
          <div><strong>${b.bookingDate}</strong></div>
          <div style="font-size:0.82rem; color:var(--text-muted);">${b.bookingTime}</div>
        </td>
        <td>
          <strong style="color:var(--primary); font-size:1.05rem;">${formatINR(b.totalAmount)}</strong>
        </td>
        <td>
          <select class="status-select" onchange="handleUpdateBookingStatus('${b.id}', this.value)" style="font-weight:700;">
            <option value="Pending" ${b.status === "Pending" ? "selected" : ""}>Pending</option>
            <option value="Confirmed" ${b.status === "Confirmed" ? "selected" : ""}>Confirmed</option>
            <option value="Completed" ${b.status === "Completed" ? "selected" : ""}>Completed</option>
            <option value="Cancelled" ${b.status === "Cancelled" ? "selected" : ""}>Cancelled</option>
          </select>
        </td>
        <td>
          <div style="display:flex; gap:0.4rem;">
            <button class="btn btn-outline btn-sm" title="View Details" onclick="viewBookingModal('${b.id}')">
              <i class="fas fa-eye"></i>
            </button>
            <button class="btn btn-outline btn-sm" title="Delete Booking" style="color:var(--accent-red); border-color:#fee2e2;" onclick="handleDeleteBooking('${b.id}')">
              <i class="fas fa-trash-alt"></i>
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join("");
}

// Update Booking Status
function handleUpdateBookingStatus(bookingId, newStatus) {
  const bookings = JSON.parse(localStorage.getItem("shc_bookings")) || [];
  const target = bookings.find(b => b.id === bookingId);
  if (target) {
    target.status = newStatus;
    localStorage.setItem("shc_bookings", JSON.stringify(bookings));
    showToast(`Booking ${bookingId} status updated to ${newStatus}.`, "success");
    renderAdminStats();
  }
}

// Delete Booking
function handleDeleteBooking(bookingId) {
  if (!confirm(`Are you sure you want to permanently delete booking ${bookingId}?`)) {
    return;
  }

  let bookings = JSON.parse(localStorage.getItem("shc_bookings")) || [];
  bookings = bookings.filter(b => b.id !== bookingId);
  localStorage.setItem("shc_bookings", JSON.stringify(bookings));

  showToast(`Booking ${bookingId} deleted.`, "info");
  renderAdminStats();
  renderBookingsTable();
}

// View Full Booking Details in Modal
function viewBookingModal(bookingId) {
  const bookings = JSON.parse(localStorage.getItem("shc_bookings")) || [];
  const b = bookings.find(item => item.id === bookingId);
  if (!b) return;

  const modalBody = document.getElementById("adminBookingModalBody");
  if (modalBody) {
    modalBody.innerHTML = `
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1.25rem;">
        <div>
          <span style="font-size:0.85rem; color:var(--text-muted);">Booking ID:</span>
          <h3 style="font-family:monospace; color:var(--primary); font-size:1.3rem;">${b.id}</h3>
        </div>
        <span class="badge badge-success" style="font-size:0.9rem;">${b.status}</span>
      </div>

      <table class="receipt-table" style="margin:0;">
        <tr>
          <th>Customer Name</th>
          <td>${b.customerName}</td>
        </tr>
        <tr>
          <th>Mobile Number</th>
          <td><i class="fas fa-phone-alt" style="font-size:0.8rem; color:var(--primary);"></i> ${b.customerMobile}</td>
        </tr>
        <tr>
          <th>Full Address</th>
          <td>${b.customerAddress}</td>
        </tr>
        <tr>
          <th>Team Preference</th>
          <td><strong>${b.teamPreference} Cleaner Staff</strong></td>
        </tr>
        <tr>
          <th>Selected Services</th>
          <td>
            ${b.selectedServices.map(s => `<div>${s.name} - ${formatINR(s.price)}</div>`).join("")}
          </td>
        </tr>
        <tr>
          <th>Room Configuration</th>
          <td>${b.roomCount} Room(s) (${b.roomSelectionType})</td>
        </tr>
        <tr>
          <th>Date & Time Slot</th>
          <td>${b.bookingDate} at ${b.bookingTime}</td>
        </tr>
        <tr>
          <th>Special Instructions</th>
          <td>${b.specialInstructions || "None provided"}</td>
        </tr>
        <tr>
          <th>Total Amount</th>
          <td><strong style="color:var(--primary); font-size:1.2rem;">${formatINR(b.totalAmount)}</strong></td>
        </tr>
      </table>
    `;

    const modal = document.getElementById("adminBookingDetailModal");
    if (modal) modal.classList.add("active");
  }
}

function closeAdminBookingModal() {
  const modal = document.getElementById("adminBookingDetailModal");
  if (modal) modal.classList.remove("active");
}

// 3. Manage Services & Prices
function renderServicesPriceManager() {
  const container = document.getElementById("adminServicesManagerList");
  if (!container) return;

  const services = getAppServices();

  container.innerHTML = services.map(s => `
    <div style="display:flex; align-items:center; justify-content:space-between; padding:1rem 1.25rem; border-bottom:1px solid var(--border-color); flex-wrap:wrap; gap:1rem;">
      <div style="display:flex; align-items:center; gap:1rem;">
        <img src="${s.image}" style="width:50px; height:50px; border-radius:10px; object-fit:cover;">
        <div>
          <h4 style="font-size:1rem; margin-bottom:0.15rem;">${s.name}</h4>
          <span style="font-size:0.8rem; color:var(--text-muted);">${s.code}</span>
        </div>
      </div>
      <div style="display:flex; align-items:center; gap:0.75rem;">
        <div style="position:relative; width:130px;">
          <span style="position:absolute; left:10px; top:50%; transform:translateY(-50%); font-weight:700; color:var(--text-muted);">₹</span>
          <input type="number" id="price-input-${s.id}" value="${s.price}" style="padding-left:1.6rem; width:100%; border:1px solid var(--border-color); border-radius:8px; padding-top:0.4rem; padding-bottom:0.4rem; font-weight:700;">
        </div>
        <button class="btn btn-primary btn-sm" onclick="handleSaveServicePrice(${s.id})">
          <i class="fas fa-save"></i> Save
        </button>
      </div>
    </div>
  `).join("");
}

// Save Updated Service Price
function handleSaveServicePrice(serviceId) {
  const input = document.getElementById(`price-input-${serviceId}`);
  if (!input) return;

  const newPrice = parseInt(input.value, 10);
  if (isNaN(newPrice) || newPrice <= 0) {
    showToast("Please enter a valid price amount.", "error");
    return;
  }

  const services = getAppServices();
  const target = services.find(s => s.id === serviceId);
  if (target) {
    target.price = newPrice;
    localStorage.setItem("shc_services", JSON.stringify(services));
    showToast(`Price for "${target.name}" updated to ${formatINR(newPrice)}!`, "success");
  }
}

// 4. View Contact Messages
function renderContactMessagesTable() {
  const tbody = document.getElementById("adminMessagesTableBody");
  if (!tbody) return;

  const messages = JSON.parse(localStorage.getItem("shc_messages")) || [];

  if (messages.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="5" style="text-align:center; padding:2rem; color:var(--text-muted);">
          No messages received yet.
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = messages.map(m => `
    <tr>
      <td>${new Date(m.createdAt).toLocaleDateString()}</td>
      <td><strong>${m.name}</strong></td>
      <td>
        <div>${m.email}</div>
        <small style="color:var(--text-muted);">${m.phone}</small>
      </td>
      <td style="max-width:300px;">${m.message}</td>
      <td>
        <button class="btn btn-outline btn-sm" style="color:var(--accent-red);" onclick="handleDeleteMessage(${m.id})">
          <i class="fas fa-trash-alt"></i>
        </button>
      </td>
    </tr>
  `).join("");
}

function handleDeleteMessage(messageId) {
  let messages = JSON.parse(localStorage.getItem("shc_messages")) || [];
  messages = messages.filter(m => m.id !== messageId);
  localStorage.setItem("shc_messages", JSON.stringify(messages));
  showToast("Message deleted.", "info");
  renderContactMessagesTable();
}

// Setup Filters & Search Bar in Admin Dashboard
function setupAdminFilters() {
  const searchInput = document.getElementById("adminSearchInput");
  const statusFilter = document.getElementById("adminStatusFilter");

  if (searchInput) {
    searchInput.addEventListener("input", (e) => {
      const status = statusFilter ? statusFilter.value : "ALL";
      renderBookingsTable(status, e.target.value.trim());
    });
  }

  if (statusFilter) {
    statusFilter.addEventListener("change", (e) => {
      const q = searchInput ? searchInput.value.trim() : "";
      renderBookingsTable(e.target.value, q);
    });
  }
}
