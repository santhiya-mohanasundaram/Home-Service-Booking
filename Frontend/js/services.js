/**
 * Home Service Booking - Services Page Module (js/services.js)
 * Displays 7 cleaning services WITHOUT prices.
 * Supports multi-selection, smooth scroll to details, and proceed-to-booking.
 */

let selectedServiceIds = new Set();

document.addEventListener("DOMContentLoaded", () => {
  renderServicesCatalog();
  setupSelectionActions();
});

// Render the 7 services WITHOUT ANY PRICES (Connected to Backend /api/services)
async function renderServicesCatalog() {
  const container = document.getElementById("servicesListGrid");
  if (!container) return;

  let services = getAppServices();
  if (typeof syncServicesWithBackend === "function") {
    services = await syncServicesWithBackend();
  }

  container.innerHTML = services.map(service => `
    <div class="service-card" id="service-card-${service.id}" data-id="${service.id}" onclick="toggleServiceSelection(${service.id})">
      <div class="service-img-wrapper">
        <img src="${service.image}" alt="${service.name}" loading="lazy">
        <div class="select-indicator" id="indicator-${service.id}">
          <i class="fas fa-plus"></i>
        </div>
      </div>
      <div class="service-card-body">
        <h3 class="service-card-title">${service.name}</h3>
        <p class="service-card-desc">${service.description}</p>
        <ul class="service-features-list">
          ${service.features.map(f => `<li><i class="fas fa-check-circle"></i> ${f}</li>`).join("")}
        </ul>
        <div class="service-card-footer">
          <button class="btn btn-outline-primary btn-sm" onclick="event.stopPropagation(); inspectServiceDetails(${service.id})">
            <i class="fas fa-info-circle"></i> View Details
          </button>
          <button class="btn btn-primary btn-sm" onclick="event.stopPropagation(); toggleServiceSelection(${service.id})">
            <span class="select-btn-text" id="btn-text-${service.id}">Select</span>
          </button>
        </div>
      </div>
    </div>
  `).join("");
}

// Toggle multi-selection of services
function toggleServiceSelection(serviceId) {
  const card = document.getElementById(`service-card-${serviceId}`);
  const indicator = document.getElementById(`indicator-${serviceId}`);
  const btnText = document.getElementById(`btn-text-${serviceId}`);

  if (selectedServiceIds.has(serviceId)) {
    selectedServiceIds.delete(serviceId);
    if (card) card.classList.remove("selected");
    if (indicator) {
      indicator.innerHTML = '<i class="fas fa-plus"></i>';
      indicator.style.background = "rgba(255, 255, 255, 0.9)";
      indicator.style.color = "var(--text-muted)";
    }
    if (btnText) btnText.textContent = "Select";
  } else {
    selectedServiceIds.add(serviceId);
    if (card) card.classList.add("selected");
    if (indicator) {
      indicator.innerHTML = '<i class="fas fa-check"></i>';
      indicator.style.background = "var(--primary)";
      indicator.style.color = "#ffffff";
    }
    if (btnText) btnText.textContent = "Selected";
  }

  updateSelectionBar();
}

// Update the sticky bottom selection bar
function updateSelectionBar() {
  const bar = document.getElementById("servicesSelectionBar");
  const countBadge = document.getElementById("selectedServicesCount");
  const namesEl = document.getElementById("selectedServicesNames");

  if (!bar) return;

  const count = selectedServiceIds.size;
  if (count > 0) {
    bar.style.display = "block";
    if (countBadge) countBadge.textContent = `${count} Service${count > 1 ? "s" : ""} Selected`;

    const services = getAppServices();
    const names = services
      .filter(s => selectedServiceIds.has(s.id))
      .map(s => s.name)
      .join(", ");
    if (namesEl) namesEl.textContent = names;
  } else {
    bar.style.display = "none";
  }
}

// Smooth scroll to details / inspection section
function inspectServiceDetails(serviceId) {
  const services = getAppServices();
  const service = services.find(s => s.id === serviceId);
  if (!service) return;

  const detailsSection = document.getElementById("serviceDetailsSection");
  const detailsTitle = document.getElementById("detailTitle");
  const detailsImage = document.getElementById("detailImage");
  const detailsDesc = document.getElementById("detailDesc");
  const detailsFeatures = document.getElementById("detailFeatures");

  if (detailsSection && detailsTitle && detailsImage && detailsDesc && detailsFeatures) {
    detailsTitle.textContent = service.name;
    detailsImage.src = service.image;
    detailsDesc.textContent = service.description;
    detailsFeatures.innerHTML = service.features.map(f => `
      <div style="display:flex; align-items:center; gap:0.6rem; padding:0.5rem 0;">
        <i class="fas fa-sparkles" style="color:var(--primary);"></i>
        <strong>${f}</strong>
      </div>
    `).join("");

    detailsSection.style.display = "block";
    detailsSection.scrollIntoView({ behavior: "smooth" });
  }
}

// Action: Proceed with selected services to Booking page
function setupSelectionActions() {
  const proceedBtn = document.getElementById("proceedToBookingBtn");
  if (proceedBtn) {
    proceedBtn.addEventListener("click", () => {
      if (selectedServiceIds.size === 0) {
        showToast("Please select at least one service.", "warning");
        return;
      }

      // Save preselected service IDs into sessionStorage
      sessionStorage.setItem("shc_preselected_services", JSON.stringify(Array.from(selectedServiceIds)));

      // Check if user is logged in
      const currentUser = getCurrentUser();
      if (!currentUser) {
        sessionStorage.setItem("shc_redirect_after_login", "booking.html");
        showToast("Please log in to continue booking your selected services.", "info");
        setTimeout(() => {
          window.location.href = "login.html";
        }, 800);
      } else {
        window.location.href = "booking.html";
      }
    });
  }
}
