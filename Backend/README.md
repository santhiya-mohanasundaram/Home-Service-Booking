# Home Service Booking Platform - Backend

Enterprise-grade Java Spring Boot REST API for SparkleHome Cleaning Services.

---

## 1. Technology Stack

- **Java Version:** 21 (LTS)
- **Framework:** Spring Boot 3.3.5
- **Modules:** Spring Web, Spring Security, Spring Data JPA, Spring Boot Validation
- **Database:** MySQL 8.0+
- **ORM / Persistence:** Hibernate
- **Security:** BCrypt Password Hashing, JJWT (HMAC-SHA256 Stateless Token Authentication)
- **Build Tool:** Apache Maven

---

## 2. Directory Structure

```text
Backend/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/homeservice/
│       │       ├── HomeServiceApplication.java
│       │       ├── config/
│       │       │   ├── CorsConfig.java
│       │       │   ├── DataInitializer.java
│       │       │   └── DotenvLoader.java
│       │       ├── controller/
│       │       │   ├── AdminController.java
│       │       │   ├── AuthController.java
│       │       │   ├── BookingController.java
│       │       │   ├── ContactController.java
│       │       │   ├── PriceController.java
│       │       │   ├── ServiceController.java
│       │       │   └── UserController.java
│       │       ├── dto/
│       │       │   ├── ApiResponse.java
│       │       │   ├── AuthResponse.java
│       │       │   ├── BookingRequest.java
│       │       │   ├── BookingResponse.java
│       │       │   ├── BookingStatusUpdateRequest.java
│       │       │   ├── ContactRequest.java
│       │       │   ├── DashboardStatsResponse.java
│       │       │   ├── LoginRequest.java
│       │       │   ├── PriceRequest.java
│       │       │   ├── RegisterRequest.java
│       │       │   ├── ServiceRequest.java
│       │       │   └── UserProfileResponse.java
│       │       ├── entity/
│       │       │   ├── Booking.java
│       │       │   ├── BookingStatus.java
│       │       │   ├── CleaningService.java
│       │       │   ├── ContactMessage.java
│       │       │   ├── Role.java
│       │       │   ├── ServicePrice.java
│       │       │   └── User.java
│       │       ├── exception/
│       │       │   ├── BadRequestException.java
│       │       │   ├── GlobalExceptionHandler.java
│       │       │   ├── ResourceNotFoundException.java
│       │       │   └── UnauthorizedException.java
│       │       ├── repository/
│       │       │   ├── BookingRepository.java
│       │       │   ├── CleaningServiceRepository.java
│       │       │   ├── ContactMessageRepository.java
│       │       │   ├── ServicePriceRepository.java
│       │       │   └── UserRepository.java
│       │       ├── security/
│       │       │   ├── CustomAccessDeniedHandler.java
│       │       │   ├── CustomUserDetailsService.java
│       │       │   ├── JwtAuthenticationFilter.java
│       │       │   ├── JwtUtils.java
│       │       │   ├── RestAuthenticationEntryPoint.java
│       │       │   ├── SecurityConfig.java
│       │       │   └── UserPrincipal.java
│       │       └── service/
│       │           ├── AuthService.java
│       │           ├── BookingService.java
│       │           ├── CleaningServiceService.java
│       │           ├── ContactService.java
│       │           ├── DashboardService.java
│       │           ├── PriceService.java
│       │           └── UserService.java
│       └── resources/
│           └── application.properties
├── .env                 (Local configuration with credentials - git-ignored)
├── .env.example         (Safe configuration template - committed)
├── .gitignore           (Protects .env, logs, and build artifacts)
├── README.md            (Full API documentation and setup guide)
└── pom.xml              (Maven build descriptor)
```

---

## 3. Database Setup Instructions

1. Ensure MySQL Server is running on port `3306`.
2. Connect to MySQL via command line or MySQL Workbench:
   ```bash
   mysql -u root -p
   ```
3. Create the database schema if it doesn't already exist:
   ```sql
   CREATE DATABASE IF NOT EXISTS home_service_booking;
   ```
4. Spring Data JPA with `spring.jpa.hibernate.ddl-auto=update` will automatically create the tables:
   - `users`
   - `services`
   - `prices`
   - `bookings`
   - `contact_messages`

---

## 4. Environment Configuration (`.env`)

Configure the `.env` file in the `Backend` directory:

```properties
DB_URL=jdbc:mysql://localhost:3306/home_service_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
DB_USERNAME=root
DB_PASSWORD=YOUR_LOCAL_MYSQL_PASSWORD
JWT_SECRET=your_long_random_jwt_secret_at_least_256_bits
JWT_EXPIRATION=86400000
SERVER_PORT=8080
ADMIN_EMAIL=admin@sparklehome.com
ADMIN_PASSWORD=Admin@Sparkle2026
CORS_ALLOWED_ORIGINS=http://localhost:5500,http://127.0.0.1:5500,http://localhost:3000,http://localhost:8080
```

> **Security Note:** `Backend/.env` is included in `Backend/.gitignore` and will never be tracked by Git.

---

## 5. How to Run

Navigate to the `Backend` directory and start the server:

```bash
cd Backend
mvn spring-boot:run
```

The application will start on port `8080`.

---

## 6. REST API Endpoints Specification

### Authentication Endpoints

#### `POST /api/auth/register` (Public)
Creates a new customer account (Always assigns `ROLE_USER`).
- **Request:**
  ```json
  {
    "name": "Sathish Kumar",
    "email": "sathish@example.com",
    "password": "Password@123",
    "phone": "9876543210",
    "address": "123 Green Street, Bengaluru"
  }
  ```
- **Response (`201 Created`):**
  ```json
  {
    "success": true,
    "message": "User registered successfully as ROLE_USER",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "tokenType": "Bearer",
      "id": 2,
      "name": "Sathish Kumar",
      "email": "sathish@example.com",
      "role": "ROLE_USER"
    }
  }
  ```

#### `POST /api/auth/login` (Public)
Authenticates a user or admin and returns a signed JWT token.
- **Request:**
  ```json
  {
    "email": "admin@sparklehome.com",
    "password": "Admin@Sparkle2026"
  }
  ```

---

### User Profile Endpoints

#### `GET /api/users/profile` (Authenticated)
Header: `Authorization: Bearer <TOKEN>`

---

### Service Endpoints

#### `GET /api/services` (Public)
Fetches all active cleaning services.

#### `GET /api/services/{id}` (Public)
Fetches a single service by ID.

#### `POST /api/admin/services` (`ROLE_ADMIN` Only)
#### `PUT /api/admin/services/{id}` (`ROLE_ADMIN` Only)
#### `DELETE /api/admin/services/{id}` (`ROLE_ADMIN` Only)

---

### Pricing Endpoints

#### `GET /api/prices` (Public)
#### `GET /api/prices/{serviceId}` (Public)
#### `POST /api/admin/prices` (`ROLE_ADMIN` Only)
#### `PUT /api/admin/prices/{id}` (`ROLE_ADMIN` Only)

---

### Booking Endpoints

#### `POST /api/bookings` (Authenticated / Customer)
Calculates trusted pricing on backend from database.
- **Request:**
  ```json
  {
    "cleaningTeamPreference": "FEMALE",
    "selectedServices": "Kitchen Cleaning, Bathroom Cleaning",
    "roomCount": 2,
    "bookingDate": "2026-11-25",
    "bookingTime": "09:00 AM - 11:00 AM",
    "customerName": "Sathish Kumar",
    "mobileNumber": "9876543210",
    "email": "sathish@example.com",
    "fullAddress": "Flat 402, Green Meadows, Bengaluru",
    "specialInstructions": "Please use eco-friendly cleaners."
  }
  ```
- **Response (`201 Created`):**
  ```json
  {
    "success": true,
    "message": "Booking created successfully with status PENDING",
    "data": {
      "id": 1,
      "bookingId": "SH-20261119-A1B2C3",
      "totalAmount": 1680.0,
      "bookingStatus": "PENDING"
    }
  }
  ```

#### `GET /api/bookings/my` (Authenticated)
Retrieves the logged-in customer's booking history.

#### `GET /api/bookings/{id}` (Owner or Admin)
#### `PUT /api/bookings/{id}/cancel` (Owner or Admin)

---

### Admin Dashboard & Management (`ROLE_ADMIN` Only)

#### `GET /api/admin/dashboard`
Returns operational metrics: total bookings, pending, confirmed, completed, cancelled, revenue, active services, and recent bookings.

#### `GET /api/admin/bookings`
Returns all customer bookings.

#### `PUT /api/admin/bookings/{id}/status`
Updates booking status (`PENDING`, `CONFIRMED`, `COMPLETED`, `CANCELLED`).
- **Request:**
  ```json
  {
    "status": "COMPLETED"
  }
  ```

#### `GET /api/admin/contact-messages`
#### `DELETE /api/admin/contact-messages/{id}`

---

### Contact Endpoints

#### `POST /api/contact` (Public)
Submits a customer inquiry.

---

### Additional Modules & Enterprise Endpoints

#### Swagger / OpenAPI Documentation
- **Swagger UI Interactive Console:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI v3 JSON Specification:** `http://localhost:8080/v3/api-docs`

#### User Management Endpoints (`/api/users`)
- `GET /api/users/profile` - Get logged-in user profile (`getUserProfile`)
- `PUT /api/users/profile` - Update user profile (`updateUserProfile`)
- `POST /api/users/change-password` - Change password with old password verification (`changePassword`)
- `GET /api/users` - Admin list of all registered users (`getAllUsers`)
- `GET /api/users/{id}` - Admin fetch user by ID (`getUserById`)
- `DELETE /api/users/{id}` - Admin delete user (`deleteUser`)

#### Destination Management Endpoints (`/api/destinations`)
- `GET /api/destinations` - Retrieve all active destinations (`getAllDestinations`)
- `GET /api/destinations/{id}` - Retrieve destination by ID (`getDestinationById`)
- `GET /api/destinations/search?keyword=...` - Search destinations (`searchDestinations`)
- `POST /api/destinations` - Admin create destination (`addDestination`)
- `PUT /api/destinations/{id}` - Admin update destination (`updateDestination`)
- `DELETE /api/destinations/{id}` - Admin delete destination (`deleteDestination`)

#### Hotel Management Endpoints (`/api/hotels`)
- `GET /api/hotels` - Retrieve all hotels (`getAllHotels`)
- `GET /api/hotels/{id}` - Retrieve hotel details (`getHotelById`)
- `GET /api/hotels/search?destination=...` - Search hotels (`searchHotels`)
- `GET /api/hotels/{id}/availability?checkIn=...&checkOut=...` - Check hotel availability (`checkHotelAvailability`)
- `POST /api/hotels` - Admin create hotel (`addHotel`)
- `PUT /api/hotels/{id}` - Admin update hotel (`updateHotel`)
- `DELETE /api/hotels/{id}` - Admin delete hotel (`deleteHotel`)

#### Flight Management Endpoints (`/api/flights`)
- `GET /api/flights` - Retrieve all flights (`getAllFlights`)
- `GET /api/flights/{id}` - Retrieve flight by ID (`getFlightById`)
- `GET /api/flights/search?origin=...&destination=...` - Search flights (`searchFlights`)
- `GET /api/flights/{id}/availability?seats=...` - Check seat availability (`checkFlightAvailability`)
- `POST /api/flights` - Admin add flight (`addFlight`)
- `PUT /api/flights/{id}` - Admin update flight (`updateFlight`)
- `DELETE /api/flights/{id}` - Admin delete flight (`deleteFlight`)

#### Package Management Endpoints (`/api/packages`)
- `GET /api/packages` - Retrieve all packages (`getAllPackages`)
- `GET /api/packages/{id}` - Retrieve package details (`getPackageById`)
- `GET /api/packages/search?keyword=...` - Search travel packages (`searchPackages`)
- `POST /api/packages` - Admin add package (`addPackage`)
- `PUT /api/packages/{id}` - Admin update package (`updatePackage`)
- `DELETE /api/packages/{id}` - Admin delete package (`deletePackage`)

#### Payment Management Endpoints (`/api/payments`)
- `POST /api/payments/process` - Process booking payment via Card, UPI, Netbanking (`processPayment`)
- `GET /api/payments/verify/{transactionId}` - Verify payment status (`verifyPayment`)
- `GET /api/payments/booking/{bookingId}` - Get payment details for booking (`getPaymentDetails`)
- `POST /api/payments/{id}/refund` - Admin issue payment refund (`refundPayment`)
- `PUT /api/payments/{id}/status` - Admin update payment status (`updatePaymentStatus`)

#### Coupon & Promotion Endpoints (`/api/bookings`)
- `GET /api/bookings/coupon/validate?code=...&amount=...` - Validate promo code (`validateCoupon`)
- `POST /api/bookings/{ref}/coupon?code=...` - Apply coupon discount to booking (`applyCoupon`)

#### Extended Admin Analytics Endpoints (`/api/admin`)
- `GET /api/admin/total-users` - Count registered users (`getTotalUsers`)
- `GET /api/admin/total-bookings` - Count total bookings (`getTotalBookings`)
- `GET /api/admin/total-revenue` - Calculate total realized revenue (`getTotalRevenue`)
- `GET /api/admin/booking-statistics` - Aggregate stats by status (`getBookingStatistics`)

---

## 7. Development & Contributors
- **Backend Architecture & APIs:** Santhiya Mohanasundaram (`santhiyamohanasundaram29@gmail.com`)
- **Review & Code Quality:** Dhanusri D (`ddhanusrid@gmail.com`)

