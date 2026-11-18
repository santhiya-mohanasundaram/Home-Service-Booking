# SparkleHome - Professional Home Service Booking Platform

A modern, responsive, and accessible **Home Service Booking Web Application** built using **HTML5, CSS3, JavaScript (ES6+), and LocalStorage**, pre-architected for seamless integration with a future **Java Spring Boot + Spring Data JPA + MySQL** backend.

---

## 🌟 Key Application Features

1. **Modern Responsive Navigation**:
   * Desktop & mobile drawer navigation with active link detection and accessible toggle.
   * Dynamic user badge displaying customer name when signed in and Admin Panel badge for administrative accounts.

2. **Clean Homepage Layout**:
   * Hero banner featuring high-resolution cleaning photography and clear CTA triggers.
   * **Direct Cleaning Team Preference**: Select either **Male** or **Female** cleaning staff directly without intermediate complex categories.
   * Zero-whitespace alternating sections: *World-Class Standards & Comfort* and *Eco-Safe Non-Toxic Sanitization*.
   * Verified customer testimonials with star ratings and location tags.

3. **Dedicated Services Catalog (`services.html`)**:
   * Features all 7 cleaning services: *Kitchen Cleaning, Bedroom Cleaning, Bathroom Cleaning, Deep Cleaning, Corridor Cleaning, Hall Cleaning, Full Home Cleaning*.
   * **Strictly displays no prices on the Services page**.
   * Interactive multi-selection checkboxes with smooth scroll to in-depth sanitization protocol details.

4. **Transparent Indian Rupee (₹) Pricing (`prices.html`)**:
   * Service-wise pricing cards in INR (₹).
   * **Interactive Cost Estimator**: Live calculator computing total price based on selected services and customizable room counts (+/- buttons and quick 1 BHK, 2 BHK, 3 BHK, 4+ Villa presets).
   * Direct booking trigger with automated login gate.

5. **5-Step Strict Booking Flow (`booking.html` & `confirmation.html`)**:
   * **Step 1: User Login**: Enforces customer authentication before opening the booking configuration.
   * **Step 2: Booking Details**:
     * Cleaning team choice (Male or Female staff only).
     * Multiple services checkboxes.
     * Room layout selector (All Rooms counter + / -).
     * Date picker with past dates disabled (`min=today`).
     * Convenient time slot selector.
     * Customer details: Full Name, 10-digit Mobile Number, Service Address, Notes (no mandatory email required).
   * **Step 3: Summary Review Modal**: Interactive modal displaying complete booking breakdown before persistence.
   * **Step 4: Confirmation**: Generates a unique reference ID (e.g., `SHC-2026-89102`) and Confirmed status.
   * **Step 5: Download Booking**: Dedicated button (displayed strictly after confirmation) utilizing `html2pdf.js` to download an official formatted PDF receipt.

6. **Authentication & Security (`login.html`, `register.html`, `admin-login.html`)**:
   * Form validation with telephone regex checks and password matching.
   * Registered users are strictly granted `ROLE_USER`.
   * **Admin Panel Security Guard**: Accessing `admin-dashboard.html` without administrator credentials triggers an instant **HTTP 403 Forbidden Access Denied** wall.

7. **Admin Management Dashboard (`admin-dashboard.html`)**:
   * Real-time KPI stat cards: *Total, Pending, Confirmed, Completed, Cancelled*.
   * Searchable and filterable customer bookings data table.
   * Live booking status updater and deletion handler.
   * In-place service price editor updating catalog rates dynamically.
   * Customer inquiries management table.

---

## 🛠️ Technology Stack

* **Markup**: HTML5 (Semantic elements, ARIA landmarks)
* **Styling**: CSS3 (CSS Custom Properties, Flexbox, CSS Grid, Responsive Media Queries)
* **Scripting**: Vanilla JavaScript (ES6+, DOM API, modular design)
* **Client Storage**: LocalStorage & SessionStorage API
* **PDF Engine**: html2pdf.js (A4, high-DPI canvas capture)
* **Icons & Fonts**: Font Awesome 6.4.0, Google Fonts (*Outfit* and *Plus Jakarta Sans*)

---

## 🔌 Future Java Spring Boot REST API Mapping

| Frontend Function | HTTP Method | Spring Boot Endpoint | Payload / Description |
|---|---|---|---|
| `handleUserLogin()` | `POST` | `/api/auth/login` | `{ email, password }` -> JWT token |
| `handleUserRegistration()` | `POST` | `/api/auth/register` | User entity with `ROLE_USER` |
| `getAppServices()` | `GET` | `/api/services` | Returns all active cleaning services |
| `handleSaveServicePrice()` | `PUT` | `/api/admin/services/{id}/price` | `@PreAuthorize("hasRole('ADMIN')")` |
| `executeBookingConfirmation()` | `POST` | `/api/bookings` | Booking entity with customer & service details |
| `handleUpdateBookingStatus()` | `PUT` | `/api/admin/bookings/{id}/status` | Updates booking status |
| `handleDeleteBooking()` | `DELETE` | `/api/admin/bookings/{id}` | Deletes booking record |
| `handleContactSubmit()` | `POST` | `/api/contact` | Saves customer contact inquiry |

---

## 🚀 Getting Started

1. Clone or download the repository.
2. Open `Frontend/index.html` in any modern web browser or start a local server (e.g. VS Code Live Server or Python `http.server`).
3. Demo Credentials:
   * **Demo Customer**: `user@example.com` / `User@123`
   * **Demo Administrator**: `admin@homeservice.com` / `Admin@123`
