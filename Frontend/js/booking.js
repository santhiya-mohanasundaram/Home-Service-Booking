/**
 * Home Service Booking - Booking Engine Module (js/booking.js)
 * STRICT BOOKING FLOW ENFORCEMENT:
 * Step 1: User Login Verification (redirects if not logged in)
 * Step 2: Form Inputs (Team: Male/Female only, Services, Rooms + counter, Date min=today, Slots, Details)
 * Step 3: Interactive Summary Review & Confirm
 * Step 4: Generate unique ID, Confirmed Status, Storage
 */

// State Object for Active Booking
const bookingState = {
  teamPreference: "Female", // "Male" or "Female" ONLY
  selectedServiceIds: new Set([1]), // default Kitchen
  roomType: "All Rooms",
  roomCount: 1,
  selectedSpecificRooms: [],
  bookingDate: "",
  bookingTime: "09:00 AM - 11:00 AM",
  customerName: "",
  customerMobile: "",
  customerEmail: "",
  customerAddress: "",
  specialInstructions: ""
};

document.addEventListener("DOMContentLoaded", () => {
  // Step 1: Enforce User Login
  verifyUserAuthentication();

  // Initialize Form Elements & Preloaded State
  initBookingForm();
});

// Step 1: Enforce Authentication Guard
function verifyUserAuthentication() {
  const currentUser = getCurrentUser();
  if (!currentUser) {
    sessionStorage.setItem("shc_redirect_after_login", "booking.html");
    showToast("Please log in to proceed with booking.", "warning");
    setTimeout(() => {
      window.location.href = "login.html";
    }, 600);
  }
}

// Initialize Booking Page UI
function initBookingForm() {
  const currentUser = getCurrentUser();
  if (!currentUser) return;

  // Autofill customer details from logged in user
  const nameInput = document.getElementById("custFullName");
  const mobileInput = document.getElementById("custMobile");
  const emailInput = document.getElementById("custEmail");

  if (nameInput && !nameInput.value) nameInput.value = currentUser.fullName || "";
  if (mobileInput && !mobileInput.value) mobileInput.value = currentUser.mobile || "";
  if (emailInput && !emailInput.value) emailInput.value = currentUser.email || "";
  bookingState.customerEmail = currentUser.email || "";

  // Set min date to today's date (Do not allow past dates)
  const dateInput = document.getElementById("bookingDatePicker");
  if (dateInput) {
    const today = new Date().toISOString().split("T")[0];
    dateInput.min = today;
    // Default to tomorrow or today
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowStr = tomorrow.toISOString().split("T")[0];
    dateInput.value = tomorrowStr;
    bookingState.bookingDate = tomorrowStr;

    dateInput.addEventListener("change", (e) => {
      const selectedVal = e.target.value;
      if (selectedVal < today) {
        showToast("Past dates cannot be selected for booking.", "warning");
        dateInput.value = today;
        bookingState.bookingDate = today;
      } else {
        bookingState.bookingDate = selectedVal;
      }
      renderPriceSummary();
    });
  }

  // Check pre-selected services from session
  try {
    const preselected = sessionStorage.getItem("shc_preselected_services");
    if (preselected) {
      const ids = JSON.parse(preselected);
      if (Array.isArray(ids) && ids.length > 0) {
        bookingState.selectedServiceIds = new Set(ids);
      }
    }
    const preCount = sessionStorage.getItem("shc_calc_room_count");
    if (preCount) {
      bookingState.roomCount = parseInt(preCount, 10) || 1;
    }
  } catch (e) {
    console.error(e);
  }

  // Render Service Checkboxes
  renderBookingServicesList();

  // Render Team Selector (MALE & FEMALE ONLY)
  setupTeamSelector();

  // Setup Room Options & Counter
  setupRoomSelector();

  // Setup Time Slot Selector
  setupTimeSlots();

  // Calculate & Render Live Summary
  renderPriceSummary();

  // Setup Summary Review Modal & Final Confirm Handlers
  const reviewBtn = document.getElementById("proceedToReviewBtn");
  if (reviewBtn) {
    reviewBtn.addEventListener("click", handleReviewBookingClick);
  }

  const finalConfirmBtn = document.getElementById("confirmBookingFinalBtn");
  if (finalConfirmBtn) {
    finalConfirmBtn.addEventListener("click", executeBookingConfirmation);
  }

  // Initialize User Bookings & Handle Hash Navigation
  loadUserBookingsList();
  if (window.location.hash === "#my-bookings") {
    switchBookingView("MY_BOOKINGS");
  }
}

// ==========================================
// User Bookings & Tab Switching System
// ==========================================
function switchBookingView(view) {
  const newView = document.getElementById("newBookingView");
  const myView = document.getElementById("myBookingsView");
  const tabNewBtn = document.getElementById("tabNewBookingBtn");
  const tabMyBtn = document.getElementById("tabMyBookingsBtn");

  if (view === "MY_BOOKINGS") {
    if (newView) newView.style.display = "none";
    if (myView) myView.style.display = "block";
    if (tabNewBtn) tabNewBtn.className = "btn btn-outline";
    if (tabMyBtn) tabMyBtn.className = "btn btn-primary";
    window.location.hash = "my-bookings";
    loadUserBookingsList();
  } else {
    if (newView) newView.style.display = "grid";
    if (myView) myView.style.display = "none";
    if (tabNewBtn) tabNewBtn.className = "btn btn-primary";
    if (tabMyBtn) tabMyBtn.className = "btn btn-outline";
    if (window.location.hash === "#my-bookings") {
      history.replaceState(null, null, "booking.html");
    }
  }
}

async function loadUserBookingsList(forceToast = false) {
  const container = document.getElementById("userBookingsListContainer");
  const countBadge = document.getElementById("userBookingsBadgeCount");
  if (!container) return;

  const currentUser = getCurrentUser();

  // Fetch live bookings from Spring Boot backend if user is authenticated
  if (currentUser && currentUser.token) {
    try {
      const res = await fetch("http://localhost:8080/api/bookings/my", {
        headers: { "Authorization": `Bearer ${currentUser.token}` }
      });
      if (res.ok) {
        const respData = await res.json();
        if (respData && respData.data && Array.isArray(respData.data)) {
          const backendBookings = respData.data.map(b => {
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
              selectedServices: (b.selectedServices || b.servicesSummary) ? (b.selectedServices || b.servicesSummary).split(", ").map((s, idx) => ({ id: idx + 1, name: s })) : [],
              roomCount: b.roomCount,
              bookingDate: b.bookingDate,
              bookingTime: b.bookingTime,
              totalAmount: b.totalAmount,
              status: displayStatus,
              createdAt: b.createdAt
            };
          });

          // Sync with local storage
          let localBookings = JSON.parse(localStorage.getItem("shc_bookings")) || [];
          backendBookings.forEach(bb => {
            const idx = localBookings.findIndex(lb => lb.id === bb.id || (lb.backendId && lb.backendId === bb.backendId));
            if (idx >= 0) {
              localBookings[idx] = { ...localBookings[idx], ...bb, status: bb.status };
            } else {
              localBookings.unshift(bb);
            }
          });
          localStorage.setItem("shc_bookings", JSON.stringify(localBookings));
        }
      }
    } catch (e) {
      console.warn("Backend offline or unable to load /api/bookings/my:", e);
    }
  }

  let bookings = JSON.parse(localStorage.getItem("shc_bookings")) || [];

  // If user is logged in, filter their relevant bookings
  if (currentUser && currentUser.email) {
    const userBookings = bookings.filter(b =>
      (b.customerEmail && b.customerEmail.toLowerCase() === currentUser.email.toLowerCase()) ||
      (b.email && b.email.toLowerCase() === currentUser.email.toLowerCase()) ||
      (b.userId && currentUser.id && b.userId == currentUser.id)
    );
    if (userBookings.length > 0) {
      bookings = userBookings;
    }
  }

  if (countBadge) countBadge.textContent = bookings.length;

  if (bookings.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 3rem 1.5rem; color: var(--text-muted);">
        <i class="fas fa-calendar-times" style="font-size: 2.8rem; color: #cbd5e1; margin-bottom: 0.85rem; display: block;"></i>
        <h4 style="color: var(--secondary); margin-bottom: 0.35rem; font-size: 1.25rem;">No Bookings Recorded Yet</h4>
        <p style="font-size: 0.92rem; margin-bottom: 1.5rem;">You haven't scheduled any cleaning appointments under this account.</p>
        <button type="button" class="btn btn-primary btn-sm" onclick="switchBookingView('NEW')">
          <i class="fas fa-plus-circle"></i> Create Your First Booking
        </button>
      </div>
    `;
    return;
  }

  container.innerHTML = `
    <div style="overflow-x: auto;">
      <table class="receipt-table" style="width: 100%; border-collapse: collapse; min-width: 650px;">
        <thead>
          <tr style="background: var(--bg-subtle); border-bottom: 2px solid var(--border-color); text-align: left;">
            <th style="padding: 0.85rem 1rem;">Booking ID & Date</th>
            <th style="padding: 0.85rem 1rem;">Services & Assigned Team</th>
            <th style="padding: 0.85rem 1rem;">Schedule Slot</th>
            <th style="padding: 0.85rem 1rem;">Total (₹)</th>
            <th style="padding: 0.85rem 1rem;">Current Status</th>
            <th style="padding: 0.85rem 1rem; text-align: center;">Official Receipt</th>
          </tr>
        </thead>
        <tbody>
          ${bookings.map(b => {
    const status = b.status || "Pending";
    let statusBadge = '<span class="badge badge-warning" style="font-size: 0.85rem; padding: 0.35rem 0.75rem;"><i class="fas fa-clock"></i> Pending</span>';
    if (status.toLowerCase() === "completed") {
      statusBadge = '<span class="badge badge-success" style="font-size: 0.85rem; padding: 0.35rem 0.75rem; background: #dcfce7; color: #15803d; border: 1px solid #86efac;"><i class="fas fa-check-circle"></i> Completed</span>';
    } else if (status.toLowerCase() === "confirmed") {
      statusBadge = '<span class="badge badge-primary" style="font-size: 0.85rem; padding: 0.35rem 0.75rem;"><i class="fas fa-check"></i> Confirmed</span>';
    } else if (status.toLowerCase() === "cancelled") {
      statusBadge = '<span class="badge badge-danger" style="font-size: 0.85rem; padding: 0.35rem 0.75rem;"><i class="fas fa-times-circle"></i> Cancelled</span>';
    }

    const services = b.selectedServices ? b.selectedServices.map(s => s.name).join(", ") : "Standard Cleaning";

    return `
              <tr style="border-bottom: 1px solid var(--border-color);">
                <td style="padding: 0.85rem 1rem;">
                  <strong style="color: var(--secondary); font-family: monospace; font-size: 0.95rem;">${b.id}</strong>
                  <div style="font-size: 0.8rem; color: var(--text-muted);">${new Date(b.createdAt || Date.now()).toLocaleDateString()}</div>
                </td>
                <td style="padding: 0.85rem 1rem;">
                  <div style="font-weight: 600; font-size: 0.92rem; color: var(--text-main);">${services}</div>
                  <div style="font-size: 0.8rem; color: var(--primary); font-weight: 700; margin-top: 2px;">
                    ${b.roomCount || 1} Room(s) &bull; ${b.teamPreference || "Female"} Staff
                  </div>
                </td>
                <td style="padding: 0.85rem 1rem;">
                  <div style="font-weight: 600;">${b.bookingDate}</div>
                  <div style="font-size: 0.82rem; color: var(--text-muted);">${b.bookingTime}</div>
                </td>
                <td style="padding: 0.85rem 1rem;">
                  <strong style="color: var(--primary); font-size: 1.05rem;">${formatINR(b.totalAmount)}</strong>
                </td>
                <td style="padding: 0.85rem 1rem;">
                  ${statusBadge}
                </td>
                <td style="padding: 0.85rem 1rem; text-align: center;">
                  <a href="confirmation.html?id=${b.id}" class="btn btn-outline-primary btn-sm" style="white-space: nowrap;">
                    <i class="fas fa-file-invoice"></i> View Receipt
                  </a>
                </td>
              </tr>
            `;
  }).join("")}
        </tbody>
      </table>
    </div>
  `;

  if (forceToast) {
    showToast("Booking status refreshed successfully!", "success");
  }
}

// Team Selector: Male & Female ONLY
function setupTeamSelector() {
  const maleCard = document.getElementById("teamMaleCard");
  const femaleCard = document.getElementById("teamFemaleCard");

  const quickPref = sessionStorage.getItem("shc_quick_team_pref");
  if (quickPref === "Male") {
    bookingState.teamPreference = "Male";
    if (maleCard) maleCard.classList.add("selected");
    if (femaleCard) femaleCard.classList.remove("selected");
  } else if (quickPref === "Female") {
    bookingState.teamPreference = "Female";
    if (femaleCard) femaleCard.classList.add("selected");
    if (maleCard) maleCard.classList.remove("selected");
  }

  if (maleCard && femaleCard) {
    maleCard.addEventListener("click", () => {
      bookingState.teamPreference = "Male";
      maleCard.classList.add("selected");
      femaleCard.classList.remove("selected");
      renderPriceSummary();
    });

    femaleCard.addEventListener("click", () => {
      bookingState.teamPreference = "Female";
      femaleCard.classList.add("selected");
      maleCard.classList.remove("selected");
      renderPriceSummary();
    });
  }
}

// Render Services Multiple Selection
function renderBookingServicesList() {
  const container = document.getElementById("bookingServicesContainer");
  if (!container) return;

  const services = getAppServices();

  container.innerHTML = services.map(service => {
    const isChecked = bookingState.selectedServiceIds.has(service.id);
    return `
      <div class="booking-service-item ${isChecked ? "selected" : ""}" id="booking-srv-item-${service.id}" onclick="toggleBookingService(${service.id})">
        <input type="checkbox" id="srv-chk-${service.id}" ${isChecked ? "checked" : ""} onclick="event.stopPropagation(); toggleBookingService(${service.id})">
        <div class="booking-service-label">
          <span>${service.name}</span>
          <small>${formatINR(service.price)}</small>
        </div>
      </div>
    `;
  }).join("");
}

function toggleBookingService(serviceId) {
  const item = document.getElementById(`booking-srv-item-${serviceId}`);
  const chk = document.getElementById(`srv-chk-${serviceId}`);

  if (bookingState.selectedServiceIds.has(serviceId)) {
    if (bookingState.selectedServiceIds.size === 1) {
      showToast("At least one service must be selected.", "warning");
      return;
    }
    bookingState.selectedServiceIds.delete(serviceId);
    if (item) item.classList.remove("selected");
    if (chk) chk.checked = false;
  } else {
    bookingState.selectedServiceIds.add(serviceId);
    if (item) item.classList.add("selected");
    if (chk) chk.checked = true;
  }

  renderPriceSummary();
}

// Setup Room Selection & Counter
function setupRoomSelector() {
  const roomPills = document.querySelectorAll(".room-radio-box");
  const counterWrap = document.getElementById("allRoomsCounterWrapper");
  const roomCountVal = document.getElementById("roomCountVal");
  const decBtn = document.getElementById("btnDecRooms");
  const incBtn = document.getElementById("btnIncRooms");

  roomPills.forEach(pill => {
    pill.addEventListener("click", () => {
      roomPills.forEach(p => p.classList.remove("selected"));
      pill.classList.add("selected");
      const rType = pill.getAttribute("data-room");
      bookingState.roomType = rType;

      if (rType === "All Rooms") {
        if (counterWrap) counterWrap.style.display = "flex";
      } else {
        if (counterWrap) counterWrap.style.display = "none";
        bookingState.roomCount = 1;
        if (roomCountVal) roomCountVal.textContent = "1";
      }
      renderPriceSummary();
    });
  });

  if (roomCountVal) {
    roomCountVal.textContent = bookingState.roomCount;
  }

  if (decBtn && incBtn && roomCountVal) {
    decBtn.addEventListener("click", () => {
      if (bookingState.roomCount > 1) {
        bookingState.roomCount--;
        roomCountVal.textContent = bookingState.roomCount;
        renderPriceSummary();
      }
    });

    incBtn.addEventListener("click", () => {
      if (bookingState.roomCount < 25) {
        bookingState.roomCount++;
        roomCountVal.textContent = bookingState.roomCount;
        renderPriceSummary();
      }
    });
  }
}

// Time Slots Selection
function setupTimeSlots() {
  const slotPills = document.querySelectorAll(".slot-pill");
  slotPills.forEach(pill => {
    pill.addEventListener("click", () => {
      slotPills.forEach(p => p.classList.remove("selected"));
      pill.classList.add("selected");
      bookingState.bookingTime = pill.getAttribute("data-slot");
      renderPriceSummary();
    });
  });
}

// Dynamic Price Summary Calculation
function calculatePricing() {
  const services = getAppServices();
  const selected = services.filter(s => bookingState.selectedServiceIds.has(s.id));

  let baseSum = 0;
  selected.forEach(s => {
    baseSum += s.price;
  });

  const totalAmount = baseSum * bookingState.roomCount;

  return {
    selectedServices: selected,
    roomCount: bookingState.roomCount,
    baseSum: baseSum,
    totalAmount: totalAmount
  };
}

function renderPriceSummary() {
  const pricing = calculatePricing();

  // Update sticky summary panel
  const srvListEl = document.getElementById("summarySelectedServicesList");
  const roomCountEl = document.getElementById("summaryRoomCount");
  const totalAmountEl = document.getElementById("summaryTotalAmount");
  const teamEl = document.getElementById("summaryTeamPref");

  if (srvListEl) {
    srvListEl.innerHTML = pricing.selectedServices.map(s => `
      <div style="display:flex; justify-content:space-between; margin-bottom:0.4rem; font-size:0.92rem;">
        <span>${s.name}</span>
        <strong>${formatINR(s.price)}</strong>
      </div>
    `).join("");
  }

  if (roomCountEl) {
    roomCountEl.textContent = `${pricing.roomCount} (${bookingState.roomType})`;
  }

  if (totalAmountEl) {
    totalAmountEl.textContent = formatINR(pricing.totalAmount);
  }

  if (teamEl) {
    teamEl.textContent = `${bookingState.teamPreference} Cleaner`;
  }
}

// Step 3: Trigger Review Summary Modal before final confirmation
function handleReviewBookingClick() {
  const nameInput = document.getElementById("custFullName");
  const mobileInput = document.getElementById("custMobile");
  const emailInput = document.getElementById("custEmail");
  const addressInput = document.getElementById("custAddress");
  const notesInput = document.getElementById("custNotes");
  const dateInput = document.getElementById("bookingDatePicker");

  bookingState.customerName = nameInput ? nameInput.value.trim() : "";
  bookingState.customerMobile = mobileInput ? mobileInput.value.trim() : "";
  bookingState.customerEmail = emailInput ? emailInput.value.trim() : "";
  bookingState.customerAddress = addressInput ? addressInput.value.trim() : "";
  bookingState.specialInstructions = notesInput ? notesInput.value.trim() : "";
  if (dateInput) bookingState.bookingDate = dateInput.value;

  // Validation
  if (!bookingState.customerName) {
    showToast("Please enter your full name.", "error");
    if (nameInput) nameInput.focus();
    return;
  }

  const phoneRegex = /^[6-9]\d{9}$/;
  if (!phoneRegex.test(bookingState.customerMobile)) {
    showToast("Please enter a valid 10-digit mobile number.", "error");
    if (mobileInput) mobileInput.focus();
    return;
  }

  if (!bookingState.customerAddress || bookingState.customerAddress.length < 8) {
    showToast("Please provide your complete service address.", "error");
    if (addressInput) addressInput.focus();
    return;
  }

  if (!bookingState.bookingDate) {
    showToast("Please select a booking date.", "error");
    return;
  }

  // Populate Review Modal
  const pricing = calculatePricing();

  const nameEl = document.getElementById("modalReviewCustName");
  const phoneEl = document.getElementById("modalReviewCustPhone");
  const emailEl = document.getElementById("modalReviewCustEmail");
  const addrEl = document.getElementById("modalReviewCustAddress");

  if (nameEl) nameEl.textContent = bookingState.customerName;
  if (phoneEl) phoneEl.textContent = bookingState.customerMobile;
  if (emailEl) emailEl.textContent = bookingState.customerEmail || "N/A";
  if (addrEl) addrEl.textContent = bookingState.customerAddress;
  document.getElementById("modalReviewTeam").textContent = `${bookingState.teamPreference} Staff`;
  document.getElementById("modalReviewDate").textContent = `${bookingState.bookingDate} (${bookingState.bookingTime})`;
  document.getElementById("modalReviewRooms").textContent = `${bookingState.roomCount} Room(s) [${bookingState.roomType}]`;
  document.getElementById("modalReviewServices").textContent = pricing.selectedServices.map(s => s.name).join(", ");
  document.getElementById("modalReviewTotal").textContent = formatINR(pricing.totalAmount);

  // Show Modal
  const modal = document.getElementById("bookingReviewModal");
  if (modal) modal.classList.add("active");
}

function closeReviewModal() {
  const modal = document.getElementById("bookingReviewModal");
  if (modal) modal.classList.remove("active");
}

// Keyboard and backdrop listener for review modal
document.addEventListener("keydown", (e) => {
  if (e.key === "Escape") {
    closeReviewModal();
  }
});

// Step 4: Final Booking Confirmation & Persistence (Connected to Spring Boot API)
async function executeBookingConfirmation() {
  const currentUser = getCurrentUser();
  const pricing = calculatePricing();

  // Generate unique booking ID (e.g. SHC-2026-XXXXX)
  const randomSuffix = Math.floor(10000 + Math.random() * 90000);
  const bookingId = `SHC-2026-${randomSuffix}`;

  const newBooking = {
    id: bookingId,
    userId: currentUser ? currentUser.id : null,
    customerName: bookingState.customerName,
    customerMobile: bookingState.customerMobile,
    customerEmail: bookingState.customerEmail || (currentUser ? currentUser.email : ""),
    customerAddress: bookingState.customerAddress,
    specialInstructions: bookingState.specialInstructions,
    teamPreference: bookingState.teamPreference || "Female", // Male or Female ONLY
    selectedServices: pricing.selectedServices.map(s => ({ id: s.id, name: s.name, price: s.price })),
    roomSelectionType: bookingState.roomType || "All Rooms",
    roomCount: bookingState.roomCount || 1,
    selectedRooms: [bookingState.roomType || "All Rooms"],
    bookingDate: bookingState.bookingDate,
    bookingTime: bookingState.bookingTime,
    totalAmount: pricing.totalAmount,
    paymentMethod: "Pay on Completion (Cash / UPI / Card)",
    paymentStatus: "Pending on Arrival",
    status: "Pending", // Strict Requirement: Initial status must be Pending!
    createdAt: new Date().toISOString()
  };

  // Attempt backend persistence to Spring Boot & MySQL
  try {
    const backendPayload = {
      cleaningTeamPreference: (newBooking.teamPreference || "FEMALE").toUpperCase(),
      selectedServices: newBooking.selectedServices.map(s => s.name).join(", "),
      roomCount: newBooking.roomCount,
      bookingDate: newBooking.bookingDate,
      bookingTime: newBooking.bookingTime,
      customerName: newBooking.customerName,
      mobileNumber: newBooking.customerMobile,
      email: newBooking.customerEmail,
      fullAddress: newBooking.customerAddress,
      specialInstructions: newBooking.specialInstructions || "Standard cleaning"
    };

    const token = currentUser && currentUser.token ? currentUser.token : null;
    const headers = { "Content-Type": "application/json" };
    if (token) headers["Authorization"] = `Bearer ${token}`;

    const res = await fetch("http://localhost:8080/api/bookings", {
      method: "POST",
      headers: headers,
      body: JSON.stringify(backendPayload)
    });

    if (res.ok) {
      const respData = await res.json();
      if (respData && respData.data) {
        newBooking.backendId = respData.data.id;
        newBooking.id = respData.data.bookingId || newBooking.id;
        newBooking.status = "Pending";
      }
    }
  } catch (err) {
    console.warn("Backend booking API error, continuing with local persistence:", err);
  }

  // Save to LocalStorage
  const bookings = JSON.parse(localStorage.getItem("shc_bookings")) || [];
  bookings.unshift(newBooking);
  localStorage.setItem("shc_bookings", JSON.stringify(bookings));

  // Save active confirmation into sessionStorage
  sessionStorage.setItem("shc_latest_confirmed_booking", JSON.stringify(newBooking));

  // Clean preselected session cache
  sessionStorage.removeItem("shc_preselected_services");
  sessionStorage.removeItem("shc_calc_room_count");

  // Close modal and show proper success message
  closeReviewModal();
  showToast("Thank You! Your Booking Has Been Confirmed (Status: Pending)", "success");

  setTimeout(() => {
    window.location.href = `confirmation.html?id=${newBooking.id}`;
  }, 1000);
}
