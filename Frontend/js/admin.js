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
  const fillHomeserviceBtn = document.getElementById("fillDemoAdminBtn");
  const fillSparkleBtn = document.getElementById("fillSparkleAdminBtn");

  if (fillHomeserviceBtn) {
    fillHomeserviceBtn.addEventListener("click", () => {
      document.getElementById("adminEmail").value = "admin@homeservice.com";
      document.getElementById("adminPassword").value = "Admin@123";
      showToast("Credentials filled for admin@homeservice.com", "info");
    });
  }

  if (fillSparkleBtn) {
    fillSparkleBtn.addEventListener("click", () => {
      document.getElementById("adminEmail").value = "admin@sparklehome.com";
      document.getElementById("adminPassword").value = "Admin@Sparkle2026";
      showToast("Credentials filled for admin@sparklehome.com", "info");
    });
  }

  if (form) {
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      const email = document.getElementById("adminEmail").value.trim().toLowerCase();
      const password = document.getElementById("adminPassword").value;

      if (!email || !password) {
        showToast("Please enter your admin email and password.", "error");
        return;
      }

      // Valid official administrative credentials
      const validAdminAccounts = [
        { email: "admin@homeservice.com", password: "Admin@123" },
        { email: "admin@sparklehome.com", password: "Admin@Sparkle2026" }
      ];

      // Also check stored custom credential if any
      const storedAdmin = JSON.parse(localStorage.getItem("shc_admin_credential") || "null");
      if (storedAdmin && storedAdmin.email && storedAdmin.password) {
        validAdminAccounts.push({
          email: storedAdmin.email.toLowerCase(),
          password: storedAdmin.password
        });
      }

      const isMatch = validAdminAccounts.some(
        acc => acc.email.toLowerCase() === email && acc.password === password
      );

      let jwtToken = "jwt_mock_admin_token_" + Date.now();

      // Attempt live backend JWT authentication if backend is active
      try {
        const response = await fetch("http://localhost:8080/api/auth/login", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email: email, password: password })
        });
        if (response.ok) {
          const resData = await response.json();
          if (resData && resData.data && resData.data.token) {
            jwtToken = resData.data.token;
          }
        }
      } catch (backendErr) {
        // Backend offline; proceed with validated credential
      }

      if (isMatch || jwtToken.startsWith("ey")) {
        const adminSession = {
          email: email,
          role: "ROLE_ADMIN",
          token: jwtToken,
          loginTime: new Date().toISOString()
        };

        sessionStorage.setItem("shc_admin_session", JSON.stringify(adminSession));
        localStorage.setItem("shc_admin_session", JSON.stringify(adminSession));

        showToast("Admin authenticated successfully! Redirecting...", "success");
        setTimeout(() => {
          window.location.href = "admin-dashboard.html";
        }, 600);
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

// Helper to escape HTML characters
function escapeHtml(text) {
  if (!text) return "";
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  };
  return String(text).replace(/[&<>"']/g, m => map[m]);
}

// 1. Render Admin KPI Statistics Cards (Synced with Spring Boot / LocalStorage)
async function renderAdminStats() {
  const adminSession = getAdminSession();
  if (adminSession && adminSession.token) {
    try {
      const res = await fetch("http://localhost:8080/api/admin/dashboard", {
        headers: { "Authorization": `Bearer ${adminSession.token}` }
      });
      if (res.ok) {
        const resData = await res.json();
        if (resData && resData.data) {
          const d = resData.data;
          const statTotal = document.getElementById("statTotalBookings");
          const statPending = document.getElementById("statPendingBookings");
          const statConfirmed = document.getElementById("statConfirmedBookings");
          const statCompleted = document.getElementById("statCompletedBookings");
          const statCancelled = document.getElementById("statCancelledBookings");

          if (statTotal) statTotal.textContent = d.totalBookings || 0;
          if (statPending) statPending.textContent = d.pendingBookings || 0;
          if (statConfirmed) statConfirmed.textContent = d.confirmedBookings || 0;
          if (statCompleted) statCompleted.textContent = d.completedBookings || 0;
          if (statCancelled) statCancelled.textContent = d.cancelledBookings || 0;
          return;
        }
      }
    } catch (e) {
      console.warn("Backend admin dashboard fetch fallback:", e);
    }
  }

  const bookings = JSON.parse(localStorage.getItem("shc_bookings")) || [];

  const total = bookings.length;
  const pending = bookings.filter(b => (b.status || "").toUpperCase() === "PENDING").length;
  const confirmed = bookings.filter(b => (b.status || "").toUpperCase() === "CONFIRMED").length;
  const completed = bookings.filter(b => (b.status || "").toUpperCase() === "COMPLETED").length;
  const cancelled = bookings.filter(b => (b.status || "").toUpperCase() === "CANCELLED").length;

  const statTotal = document.getElementById("statTotalBookings");
  const statPending = document.getElementById("statPendingBookings");
  const statConfirmed = document.getElementById("statConfirmedBookings");
  const statCompleted = document.getElementById("statCompletedBookings");
  const statCancelled = document.getElementById("statCancelledBookings");

  if (statTotal) statTotal.textContent = total;
  if (statPending) statPending.textContent = pending;
  if (statConfirmed) statConfirmed.textContent = confirmed;
  if (statCompleted) statCompleted.textContent = completed;
  if (statCancelled) statCancelled.textContent = cancelled;
}

// 2. Render All Bookings in responsive Data Table (Synced with Backend /api/admin/bookings)
async function renderBookingsTable(filterStatus = "ALL", searchQuery = "") {
  const tbody = document.getElementById("adminBookingsTableBody");
  if (!tbody) return;

  const adminSession = getAdminSession();
  if (adminSession && adminSession.token) {
    try {
      const res = await fetch("http://localhost:8080/api/admin/bookings", {
        headers: { "Authorization": `Bearer ${adminSession.token}` }
      });
      if (res.ok) {
        const resData = await res.json();
        if (resData && resData.data && Array.isArray(resData.data)) {
          const backendBookings = resData.data.map(b => {
            const rawStatus = b.bookingStatus || "PENDING";
            const displayStatus = rawStatus.charAt(0).toUpperCase() + rawStatus.slice(1).toLowerCase();
            return {
              id: b.bookingId,
              backendId: b.id,
              userId: b.userId,
              customerName: b.customerName,
              customerMobile: b.mobileNumber,
              customerEmail: b.email,
              customerAddress: b.fullAddress,
              teamPreference: b.cleaningTeamPreference,
              selectedServices: b.servicesSummary ? b.servicesSummary.split(", ").map((s, idx) => ({ id: idx + 1, name: s, price: 999 })) : [],
              roomCount: b.roomCount,
              bookingDate: b.bookingDate,
              bookingTime: b.bookingTime,
              totalAmount: b.totalAmount,
              status: displayStatus,
              createdAt: b.createdAt
            };
          });
          localStorage.setItem("shc_bookings", JSON.stringify(backendBookings));
        }
      }
    } catch (e) {
      console.warn("Backend admin bookings fetch fallback:", e);
    }
  }

  let bookings = JSON.parse(localStorage.getItem("shc_bookings")) || [];

  // Filter by status (Case-insensitive check)
  if (filterStatus && filterStatus.toUpperCase() !== "ALL") {
    bookings = bookings.filter(b => (b.status || "").toUpperCase() === filterStatus.toUpperCase());
  }

  // Filter by search query (id, customer name, mobile, email)
  if (searchQuery) {
    const q = searchQuery.toLowerCase();
    bookings = bookings.filter(b =>
      (b.id || "").toLowerCase().includes(q) ||
      (b.customerName || "").toLowerCase().includes(q) ||
      (b.customerMobile || "").includes(q) ||
      (b.customerEmail || "").toLowerCase().includes(q)
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
    const normStatus = (b.status || "Pending").toUpperCase();
    let displayStatus = "Pending";
    if (normStatus === "CONFIRMED") displayStatus = "Confirmed";
    else if (normStatus === "COMPLETED") displayStatus = "Completed";
    else if (normStatus === "CANCELLED") displayStatus = "Cancelled";

    let selectStyle = "min-width:135px; width:100%; display:inline-block; font-weight:700; padding:0.45rem 0.85rem; border-radius:20px; cursor:pointer; outline:none; transition:all 0.2s; font-size:0.88rem; ";
    if (normStatus === "PENDING") selectStyle += "background:#fef3c7; color:#92400e; border:1.5px solid #fcd34d;";
    else if (normStatus === "CONFIRMED") selectStyle += "background:#e0f2fe; color:#0369a1; border:1.5px solid #bae6fd;";
    else if (normStatus === "COMPLETED") selectStyle += "background:#dcfce7; color:#15803d; border:1.5px solid #86efac;";
    else if (normStatus === "CANCELLED") selectStyle += "background:#fee2e2; color:#b91c1c; border:1.5px solid #fca5a5;";
    else selectStyle += "background:#ffffff; color:#334155; border:1.5px solid #cbd5e1;";

    const servicesNames = b.selectedServices ? b.selectedServices.map(s => s.name).join(", ") : "Standard Cleaning";

    return `
      <tr id="row-booking-${b.id}">
        <td>
          <strong style="color:var(--secondary); font-family:monospace; font-size:0.95rem;">${escapeHtml(b.id)}</strong>
          <div style="font-size:0.8rem; color:var(--text-muted);">${b.createdAt ? new Date(b.createdAt).toLocaleDateString() : 'Recent'}</div>
        </td>
        <td>
          <strong>${escapeHtml(b.customerName)}</strong>
          <div style="font-size:0.85rem; color:var(--text-muted); margin-top:2px;">
            <i class="fas fa-phone-alt" style="font-size:0.75rem; color:var(--primary);"></i> ${escapeHtml(b.customerMobile)}
          </div>
        </td>
        <td>
          <div style="font-size:0.88rem; font-weight:600; max-width:200px;">${escapeHtml(servicesNames)}</div>
          <div style="font-size:0.8rem; color:var(--primary); font-weight:700;">
            ${b.roomCount || 1} Room(s) &bull; ${escapeHtml(b.teamPreference || "Any")} Cleaner
          </div>
        </td>
        <td>
          <div><strong>${escapeHtml(b.bookingDate || "")}</strong></div>
          <div style="font-size:0.82rem; color:var(--text-muted);">${escapeHtml(b.bookingTime || "")}</div>
        </td>
        <td>
          <strong style="color:var(--primary); font-size:1.05rem;">${formatINR(b.totalAmount)}</strong>
        </td>
        <td style="min-width: 150px; width: 150px;">
          <select class="status-select" onchange="handleUpdateBookingStatus('${b.id}', this.value)" style="${selectStyle}">
            <option value="Pending" ${normStatus === "PENDING" ? "selected" : ""}>Pending</option>
            <option value="Confirmed" ${normStatus === "CONFIRMED" ? "selected" : ""}>Confirmed</option>
            <option value="Completed" ${normStatus === "COMPLETED" ? "selected" : ""}>Completed</option>
            <option value="Cancelled" ${normStatus === "CANCELLED" ? "selected" : ""}>Cancelled</option>
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

// Update Booking Status with Instant Counter Update
function handleUpdateBookingStatus(bookingId, newStatus) {
  const bookings = JSON.parse(localStorage.getItem("shc_bookings")) || [];
  const target = bookings.find(b => b.id === bookingId);
  if (target) {
    const formattedStatus = newStatus.charAt(0).toUpperCase() + newStatus.slice(1).toLowerCase();
    target.status = formattedStatus;
    localStorage.setItem("shc_bookings", JSON.stringify(bookings));

    // Also update backend
    try {
      const adminSession = getAdminSession();
      const targetRef = target.backendId || target.id || bookingId;
      if (adminSession && adminSession.token) {
        fetch(`http://localhost:8080/api/admin/bookings/${encodeURIComponent(targetRef)}/status`, {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${adminSession.token}`
          },
          body: JSON.stringify({ status: formattedStatus.toUpperCase() })
        }).catch(() => { });
      }
    } catch (e) { }

    showToast(`Booking ${bookingId} status updated to ${formattedStatus}.`, "success");
    // Immediate reactive count recalculation
    renderAdminStats();
    const filterEl = document.getElementById("adminStatusFilter");
    const searchEl = document.getElementById("adminSearchInput");
    renderBookingsTable(filterEl ? filterEl.value : "ALL", searchEl ? searchEl.value.trim() : "");
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
  const filterEl = document.getElementById("adminStatusFilter");
  const searchEl = document.getElementById("adminSearchInput");
  renderBookingsTable(filterEl ? filterEl.value : "ALL", searchEl ? searchEl.value.trim() : "");
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
async function renderServicesPriceManager() {
  const container = document.getElementById("adminServicesManagerList");
  if (!container) return;

  if (typeof syncLiveServices === "function") {
    await syncLiveServices();
  }
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
        <button class="btn btn-primary btn-sm" id="btn-save-price-${s.id}" onclick="handleSaveServicePrice(${s.id})">
          <i class="fas fa-save"></i> Save
        </button>
      </div>
    </div>
  `).join("");
}

// Save Updated Service Price Globally across entire website
async function handleSaveServicePrice(serviceId) {
  const input = document.getElementById(`price-input-${serviceId}`);
  if (!input) return;

  const newPrice = parseInt(input.value, 10);
  if (isNaN(newPrice) || newPrice <= 0) {
    showToast("Please enter a valid price amount.", "error");
    return;
  }

  const btn = document.getElementById(`btn-save-price-${serviceId}`) || (event && event.target ? event.target.closest("button") : null);
  const originalText = btn ? btn.innerHTML : '<i class="fas fa-save"></i> Save';
  if (btn) {
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';
    btn.disabled = true;
  }

  // Update backend MySQL database
  try {
    const adminSession = getAdminSession();
    const token = adminSession ? adminSession.token : null;
    const res = await fetch(`http://localhost:8080/api/admin/services/${serviceId}/price`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        ...(token ? { "Authorization": `Bearer ${token}` } : {})
      },
      body: JSON.stringify({ price: newPrice })
    });

    if (!res.ok) {
      console.warn("Backend price update returned HTTP " + res.status);
    }
  } catch (err) {
    console.warn("Backend price update network error:", err);
  } finally {
    if (btn) {
      btn.innerHTML = originalText;
      btn.disabled = false;
    }
  }

  // Update local storage services cache immediately
  const services = getAppServices();
  const target = services.find(s => s.id === serviceId);
  if (target) {
    target.price = newPrice;
    localStorage.setItem("shc_services", JSON.stringify(services));
    showToast(`Price for "${target.name}" updated to ${formatINR(newPrice)}!`, "success");
  }
}

// 4. View Contact Messages (Strict Persistence & Backend Sync)
async function renderContactMessagesTable() {
  const tbody = document.getElementById("adminMessagesTableBody");
  if (!tbody) return;

  // Try backend sync if admin session token exists
  try {
    const adminSession = getAdminSession();
    if (adminSession && adminSession.token) {
      const resp = await fetch("http://localhost:8080/api/admin/contact-messages", {
        headers: { "Authorization": `Bearer ${adminSession.token}` }
      });
      if (resp.ok) {
        const body = await resp.json();
        if (body && body.data && body.data.length > 0) {
          const synced = body.data.map(m => ({
            id: m.id,
            name: m.name,
            email: m.email,
            phone: m.phone || "N/A",
            message: m.message,
            createdAt: m.createdAt || new Date().toISOString()
          }));
          localStorage.setItem("shc_messages", JSON.stringify(synced));
        }
      }
    }
  } catch (err) {
    // Graceful offline fallback
  }

  let messages = JSON.parse(localStorage.getItem("shc_messages") || "[]");

  // Fallback seed if empty
  if (!messages || messages.length === 0) {
    messages = [
      {
        id: 1,
        name: "Sunita Roy",
        email: "sunita.roy@gmail.com",
        phone: "9823114455",
        message: "Do you offer post-renovation paint stain removal for full apartments?",
        createdAt: "2026-11-14T16:20:00Z"
      },
      {
        id: 2,
        name: "Rajesh Khanna",
        email: "rajesh.k@gmail.com",
        phone: "9811223344",
        message: "Need weekend kitchen & bathroom deep cleaning in Bengaluru.",
        createdAt: "2026-11-17T11:45:00Z"
      }
    ];
    localStorage.setItem("shc_messages", JSON.stringify(messages));
  }

  tbody.innerHTML = messages.map(m => `
    <tr>
      <td>${m.createdAt ? new Date(m.createdAt).toLocaleDateString() : "Recent"}</td>
      <td><strong>${escapeHtml(m.name)}</strong></td>
      <td>
        <div>${escapeHtml(m.email)}</div>
        <small style="color:var(--text-muted);">${escapeHtml(m.phone || "N/A")}</small>
      </td>
      <td style="max-width:300px;">${escapeHtml(m.message)}</td>
      <td style="text-align:center;">
        <button class="btn btn-outline btn-sm" style="color:var(--accent-red);" onclick="handleDeleteMessage(${m.id})" title="Delete inquiry">
          <i class="fas fa-trash-alt"></i>
        </button>
      </td>
    </tr>
  `).join("");
}

function handleDeleteMessage(messageId) {
  let messages = JSON.parse(localStorage.getItem("shc_messages") || "[]");
  messages = messages.filter(m => m.id !== messageId);
  localStorage.setItem("shc_messages", JSON.stringify(messages));
  showToast("Inquiry message deleted.", "info");
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
