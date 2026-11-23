package com.homeservice.service;

import com.homeservice.dto.BookingRequest;
import com.homeservice.dto.BookingResponse;
import com.homeservice.dto.CouponValidationResponse;
import com.homeservice.entity.Booking;
import com.homeservice.entity.BookingStatus;
import com.homeservice.entity.Coupon;
import com.homeservice.entity.Role;
import com.homeservice.entity.User;
import com.homeservice.exception.BadRequestException;
import com.homeservice.exception.InvalidRequestException;
import com.homeservice.exception.ResourceNotFoundException;
import com.homeservice.exception.UnauthorizedException;
import com.homeservice.repository.BookingRepository;
import com.homeservice.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service managing customer booking life cycles, coupon validations, and price recalculations.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PriceService priceService;
    private final CouponRepository couponRepository;

    public BookingService(BookingRepository bookingRepository, PriceService priceService, CouponRepository couponRepository) {
        this.bookingRepository = bookingRepository;
        this.priceService = priceService;
        this.couponRepository = couponRepository;
    }

    /**
     * Creates a new booking with price calculation from database rates.
     *
     * @param request booking input details
     * @param currentUser authenticated user or null for guest
     * @return BookingResponse
     */
    public BookingResponse createBooking(BookingRequest request, User currentUser) {
        // Developed by Santhiya Mohanasundaram - Handles date validity and price summation
        if (request.getBookingDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Booking date cannot be in the past: " + request.getBookingDate());
        }

        double calculatedTotal = calculateBookingPrice(request.getSelectedServices(), request.getRoomCount());
        if (calculatedTotal <= 0) {
            calculatedTotal = 500.0;
        }

        String uniqueBookingId = generateBookingId();

        Booking booking = new Booking();
        booking.setBookingId(uniqueBookingId);
        if (currentUser != null) {
            booking.setUserId(currentUser.getId());
        }
        booking.setCleaningTeamPreference(request.getCleaningTeamPreference() != null ? request.getCleaningTeamPreference().toUpperCase() : "MALE");
        booking.setSelectedServices(request.getSelectedServices());
        booking.setRoomCount(request.getRoomCount());
        booking.setBookingDate(request.getBookingDate());
        booking.setBookingTime(request.getBookingTime());
        booking.setCustomerName(request.getCustomerName());
        booking.setMobileNumber(request.getMobileNumber());
        booking.setEmail(request.getEmail());
        booking.setFullAddress(request.getFullAddress());
        booking.setSpecialInstructions(request.getSpecialInstructions());
        booking.setTotalAmount(calculatedTotal);
        booking.setBookingStatus(BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }

    /**
     * Retrieves personal bookings of the logged-in customer.
     *
     * @param currentUser authenticated user
     * @return list of bookings
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(User currentUser) {
        // Reviewed by Dhanusri D - Guarantees security separation between customer histories
        if (currentUser == null) {
            throw new UnauthorizedException("User must be logged in to view personal bookings");
        }
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        if (bookings.isEmpty()) {
            bookings = bookingRepository.findByEmailOrderByCreatedAtDesc(currentUser.getEmail());
        }
        return bookings.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Alias for getUserBookings representing booking history.
     *
     * @param currentUser authenticated user
     * @return list of historical bookings
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingHistory(User currentUser) {
        return getUserBookings(currentUser);
    }

    /**
     * Finds a single booking by ID with authorization checks.
     *
     * @param id booking ID
     * @param currentUser active user
     * @return BookingResponse
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id, User currentUser) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));

        if (currentUser != null && currentUser.getRole() != Role.ROLE_ADMIN) {
            boolean isOwner = (booking.getUserId() != null && booking.getUserId().equals(currentUser.getId())) ||
                    booking.getEmail().equalsIgnoreCase(currentUser.getEmail());
            if (!isOwner) {
                throw new UnauthorizedException("You are not authorized to view this booking");
            }
        }

        return mapToResponse(booking);
    }

    /**
     * Updates an existing booking's date, time, rooms, or address.
     *
     * @param id booking ID
     * @param request updated values
     * @param currentUser requesting user
     * @return updated BookingResponse
     */
    public BookingResponse updateBooking(Long id, BookingRequest request, User currentUser) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));

        if (currentUser != null && currentUser.getRole() != Role.ROLE_ADMIN) {
            boolean isOwner = (booking.getUserId() != null && booking.getUserId().equals(currentUser.getId())) ||
                    booking.getEmail().equalsIgnoreCase(currentUser.getEmail());
            if (!isOwner) {
                throw new UnauthorizedException("You are not authorized to modify this booking");
            }
        }

        if (booking.getBookingStatus() == BookingStatus.COMPLETED || booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Completed or cancelled bookings cannot be updated.");
        }

        if (request.getBookingDate() != null) {
            if (request.getBookingDate().isBefore(LocalDate.now())) {
                throw new BadRequestException("New booking date cannot be in the past.");
            }
            booking.setBookingDate(request.getBookingDate());
        }
        if (request.getBookingTime() != null) booking.setBookingTime(request.getBookingTime());
        if (request.getFullAddress() != null) booking.setFullAddress(request.getFullAddress());
        if (request.getRoomCount() != null && request.getRoomCount() > 0) {
            booking.setRoomCount(request.getRoomCount());
            double newTotal = calculateBookingPrice(booking.getSelectedServices(), booking.getRoomCount());
            booking.setTotalAmount(newTotal);
        }

        return mapToResponse(bookingRepository.save(booking));
    }

    /**
     * Cancels an eligible booking.
     *
     * @param id booking ID
     * @param currentUser user requesting cancellation
     * @return updated BookingResponse
     */
    public BookingResponse cancelBooking(Long id, User currentUser) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));

        if (currentUser != null && currentUser.getRole() != Role.ROLE_ADMIN) {
            boolean isOwner = (booking.getUserId() != null && booking.getUserId().equals(currentUser.getId())) ||
                    booking.getEmail().equalsIgnoreCase(currentUser.getEmail());
            if (!isOwner) {
                throw new UnauthorizedException("You are not authorized to cancel this booking");
            }
        }

        if (booking.getBookingStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Completed bookings cannot be cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        Booking updatedBooking = bookingRepository.save(booking);
        return mapToResponse(updatedBooking);
    }

    /**
     * Retrieves all bookings across the platform for administrators.
     *
     * @return list of all bookings
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates booking status by numeric database ID.
     */
    public BookingResponse updateBookingStatus(Long id, BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));
        booking.setBookingStatus(status);
        return mapToResponse(bookingRepository.save(booking));
    }

    /**
     * Updates booking status by either numeric database ID or string reference ID.
     */
    public BookingResponse updateBookingStatusByRef(String ref, BookingStatus status) {
        Booking booking = null;
        try {
            Long numId = Long.parseLong(ref);
            booking = bookingRepository.findById(numId).orElse(null);
        } catch (NumberFormatException ignored) {}

        if (booking == null) {
            booking = bookingRepository.findByBookingId(ref)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + ref));
        }

        booking.setBookingStatus(status);
        return mapToResponse(bookingRepository.save(booking));
    }

    /**
     * Calculates price for given services and room count.
     *
     * @param services list of service names
     * @param roomCount number of rooms
     * @return calculated total price in INR
     */
    @Transactional(readOnly = true)
    public double calculateBookingPrice(List<String> services, int roomCount) {
        String servicesStr = services != null ? String.join(", ", services) : "";
        return priceService.calculateBookingTotal(servicesStr, roomCount);
    }

    /**
     * Calculates price for given comma-separated services and room count.
     *
     * @param servicesStr comma-separated services
     * @param roomCount number of rooms
     * @return calculated total price in INR
     */
    @Transactional(readOnly = true)
    public double calculateBookingPrice(String servicesStr, int roomCount) {
        return priceService.calculateBookingTotal(servicesStr, roomCount);
    }

    /**
     * Generates a unique, human-readable booking tracking ID.
     *
     * @return generated ID (e.g. SH-20261123-A1B2C3)
     */
    public String generateBookingId() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "SH-" + datePart + "-" + randomPart;
    }

    /**
     * Validates a promotional discount coupon.
     *
     * @param couponCode coupon code string
     * @param amount order total
     * @return CouponValidationResponse with discount calculations
     */
    @Transactional(readOnly = true)
    public CouponValidationResponse validateCoupon(String couponCode, Double amount) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            throw new InvalidRequestException("Coupon code cannot be empty.");
        }
        Coupon coupon = couponRepository.findByCodeIgnoreCaseAndActiveTrue(couponCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or inactive coupon code: " + couponCode));

        if (amount != null && coupon.getMinAmount() != null && amount < coupon.getMinAmount()) {
            return new CouponValidationResponse(false, coupon.getCode(), coupon.getDiscountPercent(), 0.0, amount,
                    "Minimum order amount of Rs." + coupon.getMinAmount() + " required for this coupon.");
        }

        double discount = (amount != null ? amount : 1000.0) * (coupon.getDiscountPercent() / 100.0);
        if (coupon.getMaxDiscount() != null && discount > coupon.getMaxDiscount()) {
            discount = coupon.getMaxDiscount();
        }
        double finalPrice = (amount != null ? amount : 1000.0) - discount;

        return new CouponValidationResponse(true, coupon.getCode(), coupon.getDiscountPercent(), discount, finalPrice,
                "Coupon applied successfully! Saved Rs." + discount);
    }

    /**
     * Applies a valid coupon to an existing booking.
     *
     * @param bookingRef booking reference ID
     * @param couponCode coupon code
     * @return updated BookingResponse
     */
    public BookingResponse applyCoupon(String bookingRef, String couponCode) {
        Booking booking = bookingRepository.findByBookingId(bookingRef)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingRef));

        CouponValidationResponse val = validateCoupon(couponCode, booking.getTotalAmount());
        if (!val.isValid()) {
            throw new InvalidRequestException(val.getMessage());
        }

        booking.setTotalAmount(val.getFinalAmount());
        return mapToResponse(bookingRepository.save(booking));
    }

    public BookingResponse mapToResponse(Booking b) {
        BookingResponse r = new BookingResponse();
        r.setId(b.getId());
        r.setBookingId(b.getBookingId());
        r.setUserId(b.getUserId());
        r.setCleaningTeamPreference(b.getCleaningTeamPreference());
        r.setSelectedServices(b.getSelectedServices());
        r.setRoomCount(b.getRoomCount());
        r.setBookingDate(b.getBookingDate());
        r.setBookingTime(b.getBookingTime());
        r.setCustomerName(b.getCustomerName());
        r.setMobileNumber(b.getMobileNumber());
        r.setEmail(b.getEmail());
        r.setFullAddress(b.getFullAddress());
        r.setSpecialInstructions(b.getSpecialInstructions());
        r.setTotalAmount(b.getTotalAmount());
        r.setBookingStatus(b.getBookingStatus());
        r.setCreatedAt(b.getCreatedAt());
        return r;
    }
}
