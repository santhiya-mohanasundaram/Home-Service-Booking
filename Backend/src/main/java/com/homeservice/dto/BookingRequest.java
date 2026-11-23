package com.homeservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class BookingRequest {

    private String cleaningTeamPreference = "MALE"; // MALE / FEMALE

    @NotBlank(message = "Selected services are required")
    private String selectedServices;

    @NotNull(message = "Room count is required")
    @Min(value = 1, message = "Room count must be at least 1")
    private Integer roomCount;

    @NotNull(message = "Booking date is required")
    private LocalDate bookingDate;

    @NotBlank(message = "Booking time slot is required")
    private String bookingTime;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    @NotBlank(message = "Full address is required")
    private String fullAddress;

    private String specialInstructions;

    public BookingRequest() {
    }

    public String getCleaningTeamPreference() {
        return cleaningTeamPreference;
    }

    public void setCleaningTeamPreference(String cleaningTeamPreference) {
        this.cleaningTeamPreference = cleaningTeamPreference;
    }

    public String getSelectedServices() {
        return selectedServices;
    }

    public void setSelectedServices(String selectedServices) {
        this.selectedServices = selectedServices;
    }

    public Integer getRoomCount() {
        return roomCount;
    }

    public void setRoomCount(Integer roomCount) {
        this.roomCount = roomCount;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(String bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }
}
