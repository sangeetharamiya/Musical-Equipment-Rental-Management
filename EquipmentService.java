package com.rental.music.service;

import com.rental.music.exception.BusinessRuleException;
import com.rental.music.exception.ResourceNotFoundException;
import com.rental.music.model.Equipment;
import com.rental.music.repository.BookingRepository;
import com.rental.music.repository.EquipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final BookingRepository bookingRepository;

    public EquipmentService(EquipmentRepository equipmentRepository, BookingRepository bookingRepository) {
        this.equipmentRepository = equipmentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<Equipment> findAll() {
        return equipmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Equipment findById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with ID: " + id));
    }

    @Transactional
    public Equipment create(Equipment equipment) {
        if (equipmentRepository.existsBySerialNumber(equipment.getSerialNumber())) {
            throw new BusinessRuleException("Serial number already exists: " + equipment.getSerialNumber());
        }
        return equipmentRepository.save(equipment);
    }

    @Transactional
    public Equipment update(Long id, Equipment updated) {
        Equipment existing = findById(id);
        if (!existing.getSerialNumber().equals(updated.getSerialNumber())
                && equipmentRepository.existsBySerialNumber(updated.getSerialNumber())) {
            throw new BusinessRuleException("Serial number already exists: " + updated.getSerialNumber());
        }
        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setSerialNumber(updated.getSerialNumber());
        existing.setDailyRate(updated.getDailyRate());
        existing.setConditionStatus(updated.getConditionStatus());
        return equipmentRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Equipment existing = findById(id);
        if (bookingRepository.existsByEquipmentId(id)) {
            throw new BusinessRuleException(
                "Cannot delete equipment '" + existing.getName() + "' because it has existing bookings."
            );
        }
        equipmentRepository.delete(existing);
    }
}