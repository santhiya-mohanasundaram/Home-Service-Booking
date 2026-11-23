package com.homeservice.config;

import com.homeservice.entity.*;
import com.homeservice.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Enterprise database seeder providing default administrative accounts, demo users,
 * active cleaning service pricing catalogs, sample destinations, hotels, flights, and coupons.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CleaningServiceRepository serviceRepository;
    private final ServicePriceRepository priceRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final DestinationRepository destinationRepository;
    private final HotelRepository hotelRepository;
    private final FlightRepository flightRepository;
    private final TravelPackageRepository packageRepository;
    private final CouponRepository couponRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@sparklehome.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@Sparkle2026}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository,
                           CleaningServiceRepository serviceRepository,
                           ServicePriceRepository priceRepository,
                           ContactMessageRepository contactMessageRepository,
                           DestinationRepository destinationRepository,
                           HotelRepository hotelRepository,
                           FlightRepository flightRepository,
                           TravelPackageRepository packageRepository,
                           CouponRepository couponRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.priceRepository = priceRepository;
        this.contactMessageRepository = contactMessageRepository;
        this.destinationRepository = destinationRepository;
        this.hotelRepository = hotelRepository;
        this.flightRepository = flightRepository;
        this.packageRepository = packageRepository;
        this.couponRepository = couponRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Developed by Santhiya Mohanasundaram & Reviewed by Dhanusri D
        initAdmin();
        initDemoUsers();
        initServicesAndPrices();
        initContactMessages();
        initDestinations();
        initHotels();
        initFlights();
        initTravelPackages();
        initCoupons();
    }

    private void initAdmin() {
        initSingleAdmin(adminEmail, adminPassword, "System Administrator");
        initSingleAdmin("admin@homeservice.com", "Admin@123", "SparkleHome Operations Admin");
    }

    private void initDemoUsers() {
        initSingleUser("user@example.com", "User@123", "Rahul Sharma", "9876543210", "Flat 402, Green Meadows, Bengaluru");
        initSingleUser("priya@example.com", "Priya@123", "Priya Sundaram", "9812345678", "Villa 12, Sunrise Avenue, Bengaluru");
    }

    private void initSingleUser(String email, String rawPassword, String fullName, String phone, String address) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User(
                    fullName,
                    email,
                    passwordEncoder.encode(rawPassword),
                    phone,
                    address,
                    Role.ROLE_USER
            );
            userRepository.save(user);
        }
    }

    private void initSingleAdmin(String email, String rawPassword, String fullName) {
        if (!userRepository.existsByEmail(email)) {
            User admin = new User(
                    fullName,
                    email,
                    passwordEncoder.encode(rawPassword),
                    "9876543210",
                    "SparkleHome HQ, Service Command Center",
                    Role.ROLE_ADMIN
            );
            userRepository.save(admin);
        }
    }

    private void initContactMessages() {
        if (contactMessageRepository.count() == 0) {
            List<ContactMessage> messages = List.of(
                    new ContactMessage(
                            "Sunita Roy",
                            "sunita.roy@gmail.com",
                            "9823114455",
                            "FULL_HOME",
                            "Full Apartment Deep Cleaning Inquiry",
                            "Do you offer post-renovation paint stain removal for full apartments?"
                    ),
                    new ContactMessage(
                            "Rajesh Khanna",
                            "rajesh.k@gmail.com",
                            "9811223344",
                            "KITCHEN",
                            "Kitchen & Chimney Degreasing",
                            "Need weekend kitchen & bathroom deep cleaning in Bengaluru."
                    )
            );
            contactMessageRepository.saveAll(messages);
        }
    }

    private void initServicesAndPrices() {
        if (serviceRepository.count() == 0) {
            List<CleaningService> defaultServices = List.of(
                    new CleaningService("Kitchen Cleaning", "Thorough degreasing, countertop sanitization, appliance surface polishing, and sink scrubbing.", 800.0, 300.0, "2 Hours", true, "images/services/kitchen.jpg"),
                    new CleaningService("Bedroom Cleaning", "Mattress vacuuming, dust removal, wardrobe surface wipe-down, and floor disinfecting.", 600.0, 250.0, "1.5 Hours", true, "images/services/bedroom.jpg"),
                    new CleaningService("Bathroom Cleaning", "Deep tile stain removal, grout scrubbing, faucet descaling, and high-grade disinfection.", 500.0, 200.0, "1 Hour", true, "images/services/bathroom.jpg"),
                    new CleaningService("Deep Cleaning", "Comprehensive 360-degree intensive steam clean and hospital-grade sanitization for every corner.", 2500.0, 600.0, "4 Hours", true, "images/services/deep-cleaning.jpg"),
                    new CleaningService("Corridor Cleaning", "Staircase and corridor sweeping, skirting board dusting, and anti-slip floor polishing.", 400.0, 150.0, "1 Hour", true, "images/services/corridor.jpg"),
                    new CleaningService("Hall Cleaning", "Sofa vacuuming, carpet spot cleaning, fan and light fixture dusting, and living room refresh.", 700.0, 250.0, "2 Hours", true, "images/services/hall.jpg"),
                    new CleaningService("Full Home Cleaning", "All-inclusive whole property top-to-bottom transformation by dedicated specialized cleaning crews.", 3500.0, 800.0, "5 Hours", true, "images/services/full-home.jpg")
            );

            List<CleaningService> savedServices = serviceRepository.saveAll(defaultServices);

            for (CleaningService s : savedServices) {
                ServicePrice price = new ServicePrice(
                        s.getId(),
                        s.getName(),
                        s.getBasePrice(),
                        s.getPricePerUnit(),
                        0.05,
                        "Standard base package for " + s.getName() + " with 5% GST."
                );
                priceRepository.save(price);
            }
        }
    }

    private void initDestinations() {
        if (destinationRepository.count() == 0) {
            List<Destination> list = List.of(
                    new Destination("Goa", "India", "Scenic sandy beaches, vibrant nightlife, and Portuguese heritage architecture.", "https://images.unsplash.com/photo-1512343879784-a960bf40e7f2", "Beach", true),
                    new Destination("Kerala", "India", "Lush green backwaters, tea plantations, and tranquil Ayurvedic retreats.", "https://images.unsplash.com/photo-1602216056096-3b40cc0c9944", "Nature", true),
                    new Destination("Paris", "France", "The global capital of art, fashion, gastronomy and iconic landmarks.", "https://images.unsplash.com/photo-1502602898657-3e91760cbb34", "Cultural", true),
                    new Destination("Bali", "Indonesia", "Tropical paradise known for forested volcanic mountains and coral reefs.", "https://images.unsplash.com/photo-1537996194471-e657df975ab4", "Island", true),
                    new Destination("Dubai", "UAE", "Futuristic architecture, luxury shopping, and desert safari adventures.", "https://images.unsplash.com/photo-1512453979798-5ea266f8880c", "City", false)
            );
            destinationRepository.saveAll(list);
        }
    }

    private void initHotels() {
        if (hotelRepository.count() == 0) {
            List<Hotel> list = List.of(
                    new Hotel("Taj Exotica Resort & Spa", "Goa", "Calwaddo, Benaulim, Goa 403716", 8500.0, 4.8, 15, "Pool, Spa, Beachfront, Free Wi-Fi"),
                    new Hotel("Kumarakom Lake Resort", "Kerala", "Kumarakom North Post, Kottayam, Kerala 686563", 9200.0, 4.9, 12, "Backwater View, Ayurveda Center, Pool"),
                    new Hotel("Hôtel Plaza Athénée", "Paris", "25 Avenue Montaigne, 75008 Paris, France", 14500.0, 4.7, 8, "Eiffel View, Fine Dining, Concierge"),
                    new Hotel("Ayana Resort and Spa", "Bali", "Jl. Karang Mas Sejahtera, Jimbaran, Bali", 7800.0, 4.8, 20, "Rock Bar, Ocean Views, 12 Pools"),
                    new Hotel("Atlantis, The Palm", "Dubai", "Crescent Rd - The Palm Jumeirah, Dubai", 16000.0, 4.9, 10, "Waterpark, Aquarium, Luxury Suites")
            );
            hotelRepository.saveAll(list);
        }
    }

    private void initFlights() {
        if (flightRepository.count() == 0) {
            List<Flight> list = List.of(
                    new Flight("AI-202", "Air India", "Bengaluru", "Goa", "08:30 AM", "09:45 AM", 3500.0, 50),
                    new Flight("6E-541", "IndiGo", "Bengaluru", "Kochi", "10:15 AM", "11:25 AM", 2900.0, 65),
                    new Flight("AF-198", "Air France", "Delhi", "Paris", "01:20 AM", "07:45 AM", 42000.0, 80),
                    new Flight("GA-880", "Garuda Indonesia", "Mumbai", "Bali", "11:00 PM", "08:30 AM", 28000.0, 45),
                    new Flight("EK-501", "Emirates", "Mumbai", "Dubai", "04:30 AM", "06:15 AM", 18500.0, 90)
            );
            flightRepository.saveAll(list);
        }
    }

    private void initTravelPackages() {
        if (packageRepository.count() == 0) {
            List<TravelPackage> list = List.of(
                    new TravelPackage("Goa Beach Escape", "Goa", 4, 18999.0, "Complete 4-day tropical vacation package with 5-star beachfront resort, watersports, and transfers.", "Resort Stay, Airport Transfers, Breakfast, Sightseeing Tour"),
                    new TravelPackage("Kerala Backwater Serenity", "Kerala", 5, 24500.0, "Experience houseboats, spice plantations, and Ayurvedic therapies across Alleppey and Munnar.", "Houseboat Cruise, Luxury Resort, Guided Tours, Daily Meals"),
                    new TravelPackage("Romantic Paris Getaway", "Paris", 6, 78000.0, "Discover world-class museums, Seine river dinner cruise, and Versailles palace exploration.", "4-Star Hotel, River Cruise, Museum Passes, Daily Breakfast"),
                    new TravelPackage("Bali Tropical Discovery", "Bali", 5, 34000.0, "Temple tours, volcano sunrise trek, Ubud cultural markets, and beach club access.", "Villa Accommodation, Private Chauffeur, Airport Pickup, Breakfast")
            );
            packageRepository.saveAll(list);
        }
    }

    private void initCoupons() {
        if (couponRepository.count() == 0) {
            List<Coupon> list = List.of(
                    new Coupon("SPARKLE20", 20.0, 500.0, 1000.0, true, "2026-12-31"),
                    new Coupon("HOMECLEAN10", 10.0, 300.0, 500.0, true, "2026-12-31"),
                    new Coupon("WELCOME50", 50.0, 1000.0, 2000.0, true, "2026-12-31"),
                    new Coupon("TRAVEL500", 15.0, 1500.0, 5000.0, true, "2026-12-31")
            );
            couponRepository.saveAll(list);
        }
    }
}
