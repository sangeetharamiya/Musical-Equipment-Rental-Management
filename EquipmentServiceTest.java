package com.rental.music.service;

import com.rental.music.exception.BusinessRuleException;
import com.rental.music.model.Equipment;
import com.rental.music.repository.BookingRepository;
import com.rental.music.repository.EquipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock private EquipmentRepository equipmentRepository;
    @Mock private BookingRepository bookingRepository;

    @InjectMocks private EquipmentService equipmentService;

    @Test
    void deleteEquipment_WhenBookingsExist_ShouldThrowBusinessRuleException() {
        Equipment eq = new Equipment();
        eq.setId(10L);
        eq.setName("Roland Juno-106");

        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(eq));
        when(bookingRepository.existsByEquipmentId(10L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> equipmentService.delete(10L));
        assertTrue(ex.getMessage().contains("existing bookings"));
        verify(equipmentRepository, never()).delete(any());
    }

    @Test
    void deleteEquipment_WhenNoBookingsExist_ShouldDeleteSuccessfully() {
        Equipment eq = new Equipment();
        eq.setId(10L);
        eq.setName("Roland Juno-106");

        when(equipmentRepository.findById(10L)).thenReturn(Optional.of(eq));
        when(bookingRepository.existsByEquipmentId(10L)).thenReturn(false);

        equipmentService.delete(10L);

        verify(equipmentRepository).delete(eq);
    }
}