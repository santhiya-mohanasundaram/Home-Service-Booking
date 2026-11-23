import os
import json
import urllib.request
import urllib.parse
import urllib.error
import time

BASE_URL = "http://localhost:8080"
FRONTEND_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "Frontend"))

results = {
    "frontend": [],
    "admin_login": [],
    "backend": [],
    "contact_messages": [],
    "status_lifecycle": []
}

def log_test(category, name, passed, details=""):
    results[category].append({
        "name": name,
        "status": "PASS" if passed else "FAIL",
        "details": details
    })
    mark = "[PASS]" if passed else "[FAIL]"
    print(f"{mark} {category.upper()}: {name} - {details}")

def make_request(method, endpoint, data=None, token=None):
    url = f"{BASE_URL}{endpoint}"
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    
    encoded_data = json.dumps(data).encode("utf-8") if data is not None else None
    req = urllib.request.Request(url, data=encoded_data, headers=headers, method=method)
    
    try:
        with urllib.request.urlopen(req) as resp:
            code = resp.getcode()
            body = resp.read().decode("utf-8")
            return code, json.loads(body) if body else {}
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8")
        try:
            return e.code, json.loads(body)
        except Exception:
            return e.code, {"error": body}
    except Exception as ex:
        return 0, {"error": str(ex)}

def test_frontend_files():
    print("\n================== 1. TESTING FRONTEND PAGES & ASSETS ==================")
    pages = [
        ("index.html", ["SparkleHome", "hero-cleaner.jpg", "Book Now", "Kitchen Cleaning"]),
        ("services.html", ["Kitchen Cleaning", "Bedroom Cleaning", "Bathroom Cleaning", "Deep Cleaning", "Full Home Cleaning"]),
        ("prices.html", ["Service-Wise Pricing", "pricingCardsGrid", "Calculate Your Total Cleaning Cost", "calcRoomCount", "calcTotalDisplay"]),
        ("about.html", ["About Home Service Booking", "Our Mission", "Pure Living"]),
        ("contact.html", ["Send Us a Message", "contactForm", "handleContactSubmit", "contactName", "contactEmail"]),
        ("login.html", ["Customer Login", "userLoginForm", "loginEmail", "loginPassword"]),
        ("register.html", ["Create an Account", "userRegisterForm", "regFullName", "regEmail", "regMobile"]),
        ("booking.html", ["newBookingView", "myBookingsView", "tabNewBookingBtn", "tabMyBookingsBtn", "proceedToReviewBtn"]),
        ("confirmation.html", ["Thank You! Your Booking Has Been Confirmed", "printableConfirmationCard", "btnDownloadBooking", "downloadBookingPDF"]),
        ("admin-login.html", ["Staff & Admin Login", "adminLoginForm", "fillDemoAdminBtn", "fillSparkleAdminBtn"]),
        ("admin-dashboard.html", ["adminDashboardView", "statTotalBookings", "statPendingBookings", "statCompletedBookings", "statCancelledBookings", "adminMessagesTableBody"])
    ]
    
    for filename, required_strings in pages:
        filepath = os.path.join(FRONTEND_DIR, filename)
        if not os.path.exists(filepath):
            log_test("frontend", f"File Exists: {filename}", False, "File missing on disk")
            continue
        
        with open(filepath, "r", encoding="utf-8") as f:
            content = f.read()
        
        missing = [s for s in required_strings if s not in content]
        if missing:
            log_test("frontend", f"Structure Integrity: {filename}", False, f"Missing tokens: {missing}")
        else:
            log_test("frontend", f"Structure Integrity: {filename}", True, "All critical elements, IDs, and scripts found")

def test_admin_logins():
    print("\n================== 2. TESTING ADMIN AUTHENTICATION ==================")
    # 1. Admin 1: admin@homeservice.com / Admin@123
    code1, res1 = make_request("POST", "/api/auth/login", {
        "email": "admin@homeservice.com",
        "password": "Admin@123"
    })
    token1 = res1.get("data", {}).get("token")
    role1 = res1.get("data", {}).get("role")
    log_test("admin_login", "Admin Login (admin@homeservice.com)", code1 == 200 and role1 == "ROLE_ADMIN", f"HTTP {code1}, Role={role1}")

    # 2. Admin 2: admin@sparklehome.com / Admin@Sparkle2026
    code2, res2 = make_request("POST", "/api/auth/login", {
        "email": "admin@sparklehome.com",
        "password": "Admin@Sparkle2026"
    })
    token2 = res2.get("data", {}).get("token")
    role2 = res2.get("data", {}).get("role")
    log_test("admin_login", "Admin Login (admin@sparklehome.com)", code2 == 200 and role2 == "ROLE_ADMIN", f"HTTP {code2}, Role={role2}")

    return token1 or token2

def test_backend_apis(admin_token):
    print("\n================== 3. TESTING CORE BACKEND REST APIS ==================")
    # 1. User Registration
    user_email = f"testuser_{int(time.time())}@example.com"
    code, res = make_request("POST", "/api/auth/register", {
        "name": "Test Customer",
        "email": user_email,
        "password": "User@12345",
        "phone": "9876500000",
        "address": "123 Test Street, Bengaluru"
    })
    log_test("backend", "User Registration API", code == 201, f"HTTP {code}")

    # 2. User Login
    code, res = make_request("POST", "/api/auth/login", {
        "email": user_email,
        "password": "User@12345"
    })
    user_token = res.get("data", {}).get("token")
    log_test("backend", "User Login API", code == 200 and user_token is not None, f"HTTP {code}")

    # 3. Services API
    code, res = make_request("GET", "/api/services")
    services = res.get("data", [])
    log_test("backend", "Get Services API", code == 200 and len(services) >= 7, f"HTTP {code}, Count={len(services)}")

    # 4. Prices API
    code, res = make_request("GET", "/api/prices")
    prices = res.get("data", [])
    log_test("backend", "Get Prices API", code == 200 and len(prices) >= 7, f"HTTP {code}, Count={len(prices)}")

    # 5. User Bookings API (Empty initially)
    code, res = make_request("GET", "/api/bookings/my", token=user_token)
    log_test("backend", "Get User Bookings API", code == 200, f"HTTP {code}")

    return user_token

def test_contact_messages(admin_token):
    print("\n================== 4. TESTING CONTACT MESSAGES PERSISTENCE ==================")
    # 1. Public contact form submission
    code, res = make_request("POST", "/api/contact", {
        "name": "Meera Krishnan",
        "email": "meera.k@gmail.com",
        "phone": "9845123456",
        "serviceType": "FULL_HOME",
        "subject": "Deep cleaning quotation",
        "message": "Interested in full 3BHK sanitization package for next Saturday."
    })
    log_test("contact_messages", "Submit Contact Inquiry API", code == 201, f"HTTP {code}")

    # 2. Admin retrieves contact messages
    code, res = make_request("GET", "/api/admin/contact-messages", token=admin_token)
    messages = res.get("data", [])
    found_meera = any(m.get("email") == "meera.k@gmail.com" for m in messages)
    log_test("contact_messages", "Admin View Contact Messages API", code == 200 and found_meera, f"HTTP {code}, Messages Count={len(messages)}, Meera Found={found_meera}")

def test_booking_status_lifecycle_and_counts(admin_token, user_token):
    print("\n================== 5. TESTING STATUS LIFECYCLE & EXACT COUNTS ==================")
    # Step 0: Get baseline stats from Admin Dashboard API
    code, res = make_request("GET", "/api/admin/dashboard", token=admin_token)
    base_stats = res.get("data", {})
    base_all = base_stats.get("totalBookings", 0)
    base_pending = base_stats.get("pendingBookings", 0)
    base_completed = base_stats.get("completedBookings", 0)
    base_cancelled = base_stats.get("cancelledBookings", 0)

    print(f"Baseline: All={base_all}, Pending={base_pending}, Completed={base_completed}, Cancelled={base_cancelled}")

    # Sequence Step 1: Create 1 new booking -> Verify Pending = +1, All = +1
    b1_payload = {
        "cleaningTeamPreference": "FEMALE",
        "selectedServices": "Kitchen Cleaning, Bathroom Cleaning",
        "roomCount": 2,
        "bookingDate": "2026-12-01",
        "bookingTime": "10:00 AM - 12:00 PM",
        "customerName": "Test Customer 1",
        "mobileNumber": "9876511111",
        "email": "customer1@example.com",
        "fullAddress": "Flat 101, Sunshine Heights, Bengaluru",
        "specialInstructions": "Handle tiles gently"
    }
    code, res = make_request("POST", "/api/bookings", b1_payload, token=user_token)
    b1_id = res.get("data", {}).get("id")
    b1_status = res.get("data", {}).get("bookingStatus")
    log_test("status_lifecycle", "1. Create Booking 1 -> Initial Status is PENDING", code == 201 and b1_status == "PENDING", f"ID={b1_id}, Status={b1_status}")

    # Check stats after Booking 1
    code, res = make_request("GET", "/api/admin/dashboard", token=admin_token)
    s1 = res.get("data", {})
    log_test("status_lifecycle", "1b. Stat Check: Pending +1, All +1",
             s1.get("pendingBookings") == base_pending + 1 and s1.get("totalBookings") == base_all + 1,
             f"Pending={s1.get('pendingBookings')} (expected {base_pending+1}), All={s1.get('totalBookings')} (expected {base_all+1})")

    # Sequence Step 2: Change Pending to Completed -> Verify Completed +1, Pending -1
    code, res = make_request("PUT", f"/api/admin/bookings/{b1_id}/status", {"status": "COMPLETED"}, token=admin_token)
    log_test("status_lifecycle", "2. Update Booking 1 to COMPLETED", code == 200 and res.get("data", {}).get("bookingStatus") == "COMPLETED", f"HTTP {code}")

    code, res = make_request("GET", "/api/admin/dashboard", token=admin_token)
    s2 = res.get("data", {})
    log_test("status_lifecycle", "2b. Stat Check: Completed +1, Pending -1",
             s2.get("completedBookings") == base_completed + 1 and s2.get("pendingBookings") == base_pending,
             f"Completed={s2.get('completedBookings')} (expected {base_completed+1}), Pending={s2.get('pendingBookings')} (expected {base_pending})")

    # Sequence Step 3: Create another booking -> Verify Pending = +1, All = +1
    b2_payload = {
        "cleaningTeamPreference": "MALE",
        "selectedServices": "Full Home Cleaning",
        "roomCount": 1,
        "bookingDate": "2026-12-05",
        "bookingTime": "02:00 PM - 04:00 PM",
        "customerName": "Test Customer 2",
        "mobileNumber": "9876522222",
        "email": "customer2@example.com",
        "fullAddress": "Villa 45, Green Palms, Bengaluru",
        "specialInstructions": "Call before arrival"
    }
    code, res = make_request("POST", "/api/bookings", b2_payload, token=user_token)
    b2_id = res.get("data", {}).get("id")
    b2_status = res.get("data", {}).get("bookingStatus")
    log_test("status_lifecycle", "3. Create Booking 2 -> Initial Status is PENDING", code == 201 and b2_status == "PENDING", f"ID={b2_id}, Status={b2_status}")

    code, res = make_request("GET", "/api/admin/dashboard", token=admin_token)
    s3 = res.get("data", {})
    log_test("status_lifecycle", "3b. Stat Check: Pending +1, All +1",
             s3.get("pendingBookings") == base_pending + 1 and s3.get("totalBookings") == base_all + 2,
             f"Pending={s3.get('pendingBookings')} (expected {base_pending+1}), All={s3.get('totalBookings')} (expected {base_all+2})")

    # Sequence Step 4: Cancel that booking -> Verify Cancelled +1, Pending -1
    code, res = make_request("PUT", f"/api/admin/bookings/{b2_id}/status", {"status": "CANCELLED"}, token=admin_token)
    log_test("status_lifecycle", "4. Update Booking 2 to CANCELLED", code == 200 and res.get("data", {}).get("bookingStatus") == "CANCELLED", f"HTTP {code}")

    code, res = make_request("GET", "/api/admin/dashboard", token=admin_token)
    s4 = res.get("data", {})
    log_test("status_lifecycle", "4b. Stat Check: Cancelled +1, Pending -1",
             s4.get("cancelledBookings") == base_cancelled + 1 and s4.get("pendingBookings") == base_pending,
             f"Cancelled={s4.get('cancelledBookings')} (expected {base_cancelled+1}), Pending={s4.get('pendingBookings')} (expected {base_pending})")

    # Step 5: Verify All displays total bookings without duplicate counting
    code, res = make_request("GET", "/api/admin/bookings", token=admin_token)
    all_bookings = res.get("data", [])
    unique_ids = set(b.get("id") for b in all_bookings)
    log_test("status_lifecycle", "5. All Bookings Non-Duplication Check",
             len(all_bookings) == len(unique_ids) and len(all_bookings) == s4.get("totalBookings"),
             f"Total={len(all_bookings)}, Unique={len(unique_ids)}")

if __name__ == "__main__":
    test_frontend_files()
    admin_token = test_admin_logins()
    user_token = test_backend_apis(admin_token)
    test_contact_messages(admin_token)
    test_booking_status_lifecycle_and_counts(admin_token, user_token)

    total_tests = sum(len(v) for v in results.values())
    passed_tests = sum(sum(1 for t in v if t["status"] == "PASS") for v in results.values())
    print(f"\n================== SUMMARY: {passed_tests}/{total_tests} TESTS PASSED ==================")
