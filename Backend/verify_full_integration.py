import json
import urllib.request
import urllib.parse
import urllib.error
import time

BASE_URL = "http://localhost:8080"

def make_req(method, endpoint, data=None, token=None):
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
        except:
            return e.code, {"error": body}

def run_integration_verification():
    print("=" * 70)
    print("STARTING END-TO-END FRONTEND-BACKEND INTEGRATION TEST")
    print("=" * 70)
    
    # STEP 1: Registration
    reg_email = f"customer_{int(time.time())}@example.com"
    print(f"\n[STEP 1] Register new user: {reg_email}")
    c, r = make_req("POST", "/api/auth/register", {
        "name": "Kavitha Raman",
        "email": reg_email,
        "password": "Password@123",
        "phone": "9845012345",
        "address": "42 Lake View Road, Bengaluru"
    })
    assert c == 201, f"Registration failed with code {c}: {r}"
    reg_token = r["data"]["token"]
    user_id = r["data"]["id"]
    print(f"--> Success! User ID: {user_id}, Token: {reg_token[:20]}...")

    # STEP 2: Login
    print(f"\n[STEP 2] Authenticate registered user via /api/auth/login")
    c, r = make_req("POST", "/api/auth/login", {
        "email": reg_email,
        "password": "Password@123"
    })
    assert c == 200, f"Login failed: {r}"
    user_token = r["data"]["token"]
    print(f"--> Success! User logged in. Role: {r['data']['role']}")

    # STEP 2b: Demo User Login (Rahul)
    print(f"\n[STEP 2b] Verify Demo User Login: user@example.com / User@123")
    c, r = make_req("POST", "/api/auth/login", {
        "email": "user@example.com",
        "password": "User@123"
    })
    assert c == 200, f"Demo user login failed: {r}"
    print(f"--> Success! Demo user authenticated: {r['data']['name']}")

    # STEP 3: View Services
    print(f"\n[STEP 3] Fetch live active services via GET /api/services")
    c, r = make_req("GET", "/api/services")
    assert c == 200, f"Get services failed: {r}"
    services = r["data"]
    print(f"--> Success! Loaded {len(services)} services:")
    for s in services[:3]:
        print(f"    - ID {s['id']}: {s['name']} (Base: Rs.{s['basePrice']})")

    # STEP 4: View Pricing
    print(f"\n[STEP 4] Fetch pricing details via GET /api/prices")
    c, r = make_req("GET", "/api/prices")
    assert c == 200, f"Get prices failed: {r}"
    prices = r["data"]
    print(f"--> Success! Loaded {len(prices)} price configurations.")

    # STEP 5: Book Service & Save to MySQL
    print(f"\n[STEP 5] Create new booking via POST /api/bookings")
    booking_payload = {
        "cleaningTeamPreference": "FEMALE",
        "selectedServices": "Kitchen Cleaning, Bathroom Cleaning",
        "roomCount": 2,
        "bookingDate": "2026-11-28",
        "bookingTime": "09:00 AM - 11:00 AM",
        "customerName": "Kavitha Raman",
        "mobileNumber": "9845012345",
        "email": reg_email,
        "fullAddress": "42 Lake View Road, Bengaluru",
        "specialInstructions": "Focus on kitchen tile grout"
    }
    c, r = make_req("POST", "/api/bookings", booking_payload, token=user_token)
    assert c == 201, f"Create booking failed: {r}"
    booking_id = r["data"]["id"]
    booking_ref = r["data"]["bookingId"]
    status_initial = r["data"]["bookingStatus"]
    total_amount = r["data"]["totalAmount"]
    print(f"--> Success! Saved to MySQL: DB ID={booking_id}, Ref={booking_ref}, Amount=Rs.{total_amount}, Status={status_initial}")
    assert status_initial == "PENDING", f"Expected PENDING, got {status_initial}"

    # STEP 6: View Booking History on User Side
    print(f"\n[STEP 6] User queries booking history via GET /api/bookings/my")
    c, r = make_req("GET", "/api/bookings/my", token=user_token)
    assert c == 200, f"Get my bookings failed: {r}"
    my_bookings = r["data"]
    print(f"--> Success! Retrieved {len(my_bookings)} booking(s) for user:")
    found = any(b["bookingId"] == booking_ref for b in my_bookings)
    assert found, f"Newly created booking {booking_ref} not found in my_bookings!"
    print(f"    - Confirmed {booking_ref} present with status: {my_bookings[0]['bookingStatus']}")

    # STEP 7: Admin Login
    print(f"\n[STEP 7] Admin logs in via /api/auth/login")
    c, r = make_req("POST", "/api/auth/login", {
        "email": "admin@homeservice.com",
        "password": "Admin@123"
    })
    assert c == 200, f"Admin login failed: {r}"
    admin_token = r["data"]["token"]
    print(f"--> Success! Admin token obtained.")

    # STEP 8: Admin Updates Status to COMPLETED
    print(f"\n[STEP 8] Admin updates booking status to COMPLETED via PUT /api/admin/bookings/{booking_ref}/status")
    c, r = make_req("PUT", f"/api/admin/bookings/{booking_ref}/status", {"status": "COMPLETED"}, token=admin_token)
    assert c == 200, f"Admin status update failed: {r}"
    updated_status = r["data"]["bookingStatus"]
    assert updated_status == "COMPLETED", f"Expected COMPLETED, got {updated_status}"
    print(f"--> Success! MySQL updated booking status to: {updated_status}")

    # STEP 9: User Views Updated Status
    print(f"\n[STEP 9] User refreshes booking history to view updated status")
    c, r = make_req("GET", "/api/bookings/my", token=user_token)
    assert c == 200
    user_view_status = r["data"][0]["bookingStatus"]
    print(f"--> Success! User side reflects updated status: {user_view_status}")
    assert user_view_status == "COMPLETED", f"Expected user to see COMPLETED, got {user_view_status}"

    # STEP 10: Admin Dashboard Statistics
    print(f"\n[STEP 10] Admin Dashboard stats verification via GET /api/admin/dashboard")
    c, r = make_req("GET", "/api/admin/dashboard", token=admin_token)
    assert c == 200
    stats = r["data"]
    print(f"--> Success! Dashboard Stats: Total={stats['totalBookings']}, Pending={stats['pendingBookings']}, Completed={stats['completedBookings']}, Cancelled={stats['cancelledBookings']}")

    print("\n" + "=" * 70)
    print("ALL 10 END-TO-END FLOW VERIFICATION STEPS PASSED WITH 100% SUCCESS!")
    print("=" * 70)

if __name__ == "__main__":
    run_integration_verification()
