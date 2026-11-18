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

// Step 4: Final Booking Confirmation & Persistence
function executeBookingConfirmation() {
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
    customerEmail: bookingState.customerEmail,
    customerAddress: bookingState.customerAddress,
    specialInstructions: bookingState.specialInstructions,
    teamPreference: bookingState.teamPreference, // Male or Female ONLY
    selectedServices: pricing.selectedServices.map(s => ({ id: s.id, name: s.name, price: s.price })),
    roomSelectionType: bookingState.roomType,
    roomCount: bookingState.roomCount,
    selectedRooms: [bookingState.roomType],
    bookingDate: bookingState.bookingDate,
    bookingTime: bookingState.bookingTime,
    totalAmount: pricing.totalAmount,
    status: "Confirmed",
    createdAt: new Date().toISOString()
  };

  // Save to LocalStorage
  const bookings = JSON.parse(localStorage.getItem("shc_bookings")) || [];
  bookings.unshift(newBooking);
  localStorage.setItem("shc_bookings", JSON.stringify(bookings));

  // Save active confirmation into sessionStorage
  sessionStorage.setItem("shc_latest_confirmed_booking", JSON.stringify(newBooking));

  // Clean preselected session cache
  sessionStorage.removeItem("shc_preselected_services");
  sessionStorage.removeItem("shc_calc_room_count");

  // Close modal and redirect to confirmation.html
  closeReviewModal();
  showToast("Booking Confirmed Successfully!", "success");

  setTimeout(() => {
    window.location.href = `confirmation.html?id=${bookingId}`;
  }, 1000);
}
