package com.rental.music.service;

import com.rental.music.dto.BookingRequest;
import com.rental.music.exception.BusinessRuleException;
import com.rental.music.exception.ResourceNotFoundException;
import com.rental.music.model.Booking;
import com.rental.music.model.Equipment;
import com.rental.music.repository.BookingRepository;
import com.rental.music.repository.EquipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EquipmentRepository equipmentRepository;
    private final LocationLookupService locationLookupService;

    public BookingService(BookingRepository bookingRepository,
                          EquipmentRepository equipmentRepository,
                          LocationLookupService locationLookupService) {
        this.bookingRepository = bookingRepository;
        this.equipmentRepository = equipmentRepository;
        this.locationLookupService = locationLookupService;
    }

    @Transactional(readOnly = true)
    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Booking findById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));
    }

    @Transactional
    public Booking create(BookingRequest request) {
        return saveOrUpdateBooking(null, request);
    }

    @Transactional
    public Booking update(Long id, BookingRequest request) {
        findById(id); // Verify exists
        return saveOrUpdateBooking(id, request);
    }

    private Booking saveOrUpdateBooking(Long existingBookingId, BookingRequest request) {
        validateDateRange(request.startDate(), request.endDate());

        Equipment equipment = equipmentRepository.findById(request.equipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with ID: " + request.equipmentId()));

        boolean overlaps = bookingRepository.existsOverlappingBooking(
                equipment.getId(), request.startDate(), request.endDate(), existingBookingId
        );
        if (overlaps) {
            throw new BusinessRuleException(
                "Equipment '" + equipment.getName() + "' is already booked during the selected date range."
            );
        }

        Booking booking = (existingBookingId != null) ? findById(existingBookingId) : new Booking();
        booking.setEquipment(equipment);
        booking.setCustomerName(request.customerName());
        booking.setCustomerEmail(request.customerEmail());
        booking.setPostalCode(request.postalCode());
        booking.setResolvedLocation(locationLookupService.resolveCityState(request.postalCode()));
        booking.setStartDate(request.startDate());
        booking.setEndDate(request.endDate());
        booking.setTotalCost(calculateRentalCost(equipment.getDailyRate(), request.startDate(), request.endDate()));

        return bookingRepository.save(booking);
    }

    @Transactional
    public void delete(Long id) {
        Booking booking = findById(id);
        bookingRepository.delete(booking);
    }

    public void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessRuleException("End date cannot be before start date.");
        }
    }

    /**
     * Inclusive rental day calculation:
     * Example: Oct 10 to Oct 10 = 1 day; Oct 10 to Oct 12 = 3 days.
     */
    public BigDecimal calculateRentalCost(BigDecimal dailyRate, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        long rentalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return dailyRate.multiply(BigDecimal.valueOf(rentalDays));
    }
}