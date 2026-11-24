package com.rental.music.service;

import com.rental.music.dto.BookingRequest;
import com.rental.music.exception.BusinessRuleException;
import com.rental.music.model.Booking;
import com.rental.music.model.Equipment;
import com.rental.music.repository.BookingRepository;
import com.rental.music.repository.EquipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private EquipmentRepository equipmentRepository;
    @Mock private LocationLookupService locationLookupService;

    @InjectMocks private BookingService bookingService;

    private Equipment sampleEquipment;

    @BeforeEach
    void setUp() {
        sampleEquipment = new Equipment();
        sampleEquipment.setId(1L);
        sampleEquipment.setName("Fender Stratocaster");
        sampleEquipment.setDailyRate(new BigDecimal("45.00"));
    }

    @Test
    void calculateRentalCost_ShouldCalculateInclusiveDaysCorrectly() {
        LocalDate start = LocalDate.of(2026, 11, 10);
        LocalDate end = LocalDate.of(2026, 11, 12); // 3 inclusive days

        BigDecimal total = bookingService.calculateRentalCost(new BigDecimal("45.00"), start, end);

        assertEquals(new BigDecimal("135.00"), total);
    }

    @Test
    void calculateRentalCost_SingleDayRental_ShouldChargeOneDay() {
        LocalDate sameDay = LocalDate.of(2026, 11, 10);

        BigDecimal total = bookingService.calculateRentalCost(new BigDecimal("45.00"), sameDay, sameDay);

        assertEquals(new BigDecimal("45.00"), total);
    }

    @Test
    void createBooking_WhenDatesOverlap_ShouldThrowBusinessRuleException() {
        LocalDate start = LocalDate.of(2026, 11, 15);
        LocalDate end = LocalDate.of(2026, 11, 18);
        BookingRequest request = new BookingRequest(1L, "Alex Rivera", "alex@studio.com", "90210", start, end);

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(sampleEquipment));
        when(bookingRepository.existsOverlappingBooking(1L, start, end, null)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> bookingService.create(request));
        assertTrue(ex.getMessage().contains("already booked"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_WhenDatesDoNotOverlap_ShouldSaveAndCalculateCost() {
        LocalDate start = LocalDate.of(2026, 11, 20);
        LocalDate end = LocalDate.of(2026, 11, 21); // 2 days = $90.00
        BookingRequest request = new BookingRequest(1L, "Alex Rivera", "alex@studio.com", "90210", start, end);

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(sampleEquipment));
        when(bookingRepository.existsOverlappingBooking(1L, start, end, null)).thenReturn(false);
        when(locationLookupService.resolveCityState("90210")).thenReturn("Beverly Hills, CA");
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking created = bookingService.create(request);

        assertEquals(new BigDecimal("90.00"), created.getTotalCost());
        assertEquals("Beverly Hills, CA", created.getResolvedLocation());
        verify(bookingRepository).save(any(Booking.class));
    }
}