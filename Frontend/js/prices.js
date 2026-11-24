/**
 * Home Service Booking - Prices Page Module (js/prices.js)
 * Displays service-wise pricing in INR (₹).
 * Includes Interactive Estimator & Login Gate for Book Now actions.
 */

let calcSelectedServices = new Set([1]); // default kitchen selected
let calcRoomCount = 1;

document.addEventListener("DOMContentLoaded", async () => {
  if (typeof syncLiveServices === "function") {
    await syncLiveServices();
  }
  await renderPricingCards();
  initPriceCalculator();
});

// Render Pricing cards with live price values from Spring Boot / LocalStorage
async function renderPricingCards() {
  const container = document.getElementById("pricingCardsGrid");
  if (!container) return;

  let services = getAppServices();
  try {
    const res = await fetch("http://localhost:8080/api/services");
    if (res.ok) {
      const data = await res.json();
      if (data && data.data && data.data.length > 0) {
        services = data.data.map(s => {
          const existing = (typeof DEFAULT_SERVICES !== "undefined" ? DEFAULT_SERVICES : []).find(d => d.name.toLowerCase() === s.name.toLowerCase()) || {};
          return {
            id: s.id,
            name: s.name,
            description: s.description || existing.description || "",
            image: s.imageUrl || existing.image || "images/services/full-home.jpg",
            price: Math.round(s.basePrice || existing.price || 999),
            features: existing.features || ["Quality Sanitization", "Eco-friendly Cleaners", "Expert Equipment", "100% Satisfaction"]
          };
        });
        localStorage.setItem("shc_services", JSON.stringify(services));
      }
    }
  } catch (e) {
    // offline fallback
  }

  container.innerHTML = services.map(service => `
    <div class="pricing-card ${service.id === 7 ? "featured" : ""}">
      <div class="pricing-img-thumb">
        <img src="${service.image}" alt="${service.name}" loading="lazy">
      </div>
      <h3>${service.name}</h3>
      <p style="font-size: 0.9rem; margin-bottom: 1rem;">${service.description}</p>
      
      <div class="pricing-card-price">
        <span class="pricing-amount">${formatINR(service.price)}</span>
        <span class="pricing-unit">/ standard room</span>
      </div>

      <ul class="service-features-list" style="margin-bottom: 1.8rem; flex: 1;">
        ${service.features.map(f => `<li><i class="fas fa-check" style="color:var(--primary);"></i> ${f}</li>`).join("")}
      </ul>

      <button class="btn btn-primary btn-block" onclick="handleBookServiceFromPrice(${service.id})">
        <i class="fas fa-calendar-plus"></i> Book Now
      </button>
    </div>
  `).join("");
}

// Handle Book Now button on price card
function handleBookServiceFromPrice(serviceId) {
  // Store preselected service
  sessionStorage.setItem("shc_preselected_services", JSON.stringify([serviceId]));

  // Strictly check if user is logged in
  const currentUser = getCurrentUser();
  if (!currentUser) {
    sessionStorage.setItem("shc_redirect_after_login", "booking.html");
    showToast("Please log in to continue with your booking.", "warning");
    setTimeout(() => {
      window.location.href = "login.html";
    }, 800);
  } else {
    window.location.href = "booking.html";
  }
}

// Initialize Interactive Price Estimator
function initPriceCalculator() {
  const calcServicesList = document.getElementById("calcServicesList");
  const countEl = document.getElementById("calcRoomCountVal");
  const decBtn = document.getElementById("calcDecRooms");
  const incBtn = document.getElementById("calcIncRooms");

  if (!calcServicesList) return;

  const services = getAppServices();

  // Render checkboxes
  calcServicesList.innerHTML = services.map(s => `
    <label style="display:flex; align-items:center; gap:0.6rem; padding:0.5rem 0.8rem; background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; cursor:pointer;">
      <input type="checkbox" value="${s.id}" ${calcSelectedServices.has(s.id) ? "checked" : ""} onchange="handleCalcServiceToggle(this, ${s.id})" style="accent-color:var(--primary); width:18px; height:18px;">
      <span style="font-size:0.92rem; font-weight:600;">${s.name}</span>
      <span style="margin-left:auto; color:var(--primary); font-weight:700;">${formatINR(s.price)}</span>
    </label>
  `).join("");

  if (decBtn && incBtn && countEl) {
    decBtn.addEventListener("click", () => {
      if (calcRoomCount > 1) {
        calcRoomCount--;
        countEl.textContent = calcRoomCount;
        updateCalculatorTotal();
      }
    });

    incBtn.addEventListener("click", () => {
      if (calcRoomCount < 20) {
        calcRoomCount++;
        countEl.textContent = calcRoomCount;
        updateCalculatorTotal();
      }
    });
  }

  updateCalculatorTotal();
}

function handleCalcServiceToggle(checkbox, serviceId) {
  if (checkbox.checked) {
    calcSelectedServices.add(serviceId);
  } else {
    calcSelectedServices.delete(serviceId);
  }
  updateCalculatorTotal();
}

function setCalcRoomPreset(count) {
  calcRoomCount = count;
  const countEl = document.getElementById("calcRoomCountVal");
  if (countEl) countEl.textContent = count;
  updateCalculatorTotal();
}

function updateCalculatorTotal() {
  const totalEl = document.getElementById("calcTotalDisplay");
  if (!totalEl) return;

  const services = getAppServices();
  let baseSum = 0;
  services.forEach(s => {
    if (calcSelectedServices.has(s.id)) {
      baseSum += s.price;
    }
  });

  const grandTotal = baseSum * calcRoomCount;
  totalEl.textContent = formatINR(grandTotal);
}

// Action: Book with custom estimator configuration
function bookFromCalculator() {
  if (calcSelectedServices.size === 0) {
    showToast("Please select at least one service to calculate.", "warning");
    return;
  }

  sessionStorage.setItem("shc_preselected_services", JSON.stringify(Array.from(calcSelectedServices)));
  sessionStorage.setItem("shc_calc_room_count", calcRoomCount.toString());

  const currentUser = getCurrentUser();
  if (!currentUser) {
    sessionStorage.setItem("shc_redirect_after_login", "booking.html");
    showToast("Please log in to continue booking.", "info");
    setTimeout(() => {
      window.location.href = "login.html";
    }, 800);
  } else {
    window.location.href = "booking.html";
  }
}
