package com.homeservice.service;

import com.homeservice.dto.BookingResponse;
import com.homeservice.dto.DashboardStatsResponse;
import com.homeservice.entity.BookingStatus;
import com.homeservice.entity.Role;
import com.homeservice.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service providing administrative aggregate statistics, financial reporting, and ecosystem oversight.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final BookingRepository bookingRepository;
    private final CleaningServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final DestinationRepository destinationRepository;
    private final HotelRepository hotelRepository;
    private final FlightRepository flightRepository;
    private final TravelPackageRepository packageRepository;

    public DashboardService(BookingRepository bookingRepository,
                            CleaningServiceRepository serviceRepository,
                            UserRepository userRepository,
                            BookingService bookingService,
                            DestinationRepository destinationRepository,
                            HotelRepository hotelRepository,
                            FlightRepository flightRepository,
                            TravelPackageRepository packageRepository) {
        this.bookingRepository = bookingRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.bookingService = bookingService;
        this.destinationRepository = destinationRepository;
        this.hotelRepository = hotelRepository;
        this.flightRepository = flightRepository;
        this.packageRepository = packageRepository;
    }

    /**
     * Generates complete administrative dashboard KPIs and status metrics.
     *
     * @return DashboardStatsResponse
     */
    public DashboardStatsResponse getAdminDashboard() {
        // Developed by Santhiya Mohanasundaram - Computes dynamic metrics across services and bookings
        DashboardStatsResponse stats = new DashboardStatsResponse();
        stats.setTotalBookings(getTotalBookings());
        stats.setPendingBookings(bookingRepository.countByBookingStatus(BookingStatus.PENDING));
        stats.setConfirmedBookings(bookingRepository.countByBookingStatus(BookingStatus.CONFIRMED));
        stats.setCompletedBookings(bookingRepository.countByBookingStatus(BookingStatus.COMPLETED));
        stats.setCancelledBookings(bookingRepository.countByBookingStatus(BookingStatus.CANCELLED));

        stats.setTotalRevenue(getTotalRevenue());
        stats.setTotalServices(serviceRepository.count());
        stats.setTotalUsers(getTotalUsers());

        List<BookingResponse> recent = bookingRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .limit(5)
                .map(bookingService::mapToResponse)
                .collect(Collectors.toList());
        stats.setRecentBookings(recent);

        return stats;
    }

    /**
     * Alias matching original controller name.
     */
    public DashboardStatsResponse getDashboardStats() {
        return getAdminDashboard();
    }

    /**
     * Returns total count of registered customers.
     */
    public long getTotalUsers() {
        return userRepository.countByRole(Role.ROLE_USER);
    }

    /**
     * Returns total count of bookings created.
     */
    public long getTotalBookings() {
        return bookingRepository.count();
    }

    /**
     * Calculates total gross revenue in INR.
     */
    public double getTotalRevenue() {
        // Reviewed by Dhanusri D - Verified null-safe revenue aggregation
        Double revenue = bookingRepository.calculateTotalRevenue();
        return revenue != null ? Math.round(revenue * 100.0) / 100.0 : 0.0;
    }

    /**
     * Provides breakdown of booking statistics by status.
     *
     * @return map of status to count
     */
    public Map<String, Object> getBookingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", getTotalBookings());
        stats.put("pending", bookingRepository.countByBookingStatus(BookingStatus.PENDING));
        stats.put("confirmed", bookingRepository.countByBookingStatus(BookingStatus.CONFIRMED));
        stats.put("completed", bookingRepository.countByBookingStatus(BookingStatus.COMPLETED));
        stats.put("cancelled", bookingRepository.countByBookingStatus(BookingStatus.CANCELLED));
        stats.put("totalRevenue", getTotalRevenue());
        return stats;
    }

    /**
     * Returns ecosystem inventory summary for destinations, hotels, flights, and packages.
     */
    public Map<String, Object> getEcosystemSummary() {
        Map<String, Object> eco = new HashMap<>();
        eco.put("destinationsCount", destinationRepository.count());
        eco.put("hotelsCount", hotelRepository.count());
        eco.put("flightsCount", flightRepository.count());
        eco.put("packagesCount", packageRepository.count());
        return eco;
    }
}
