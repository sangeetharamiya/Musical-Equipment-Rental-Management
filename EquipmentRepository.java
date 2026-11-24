package com.rental.music.repository;

import com.rental.music.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    boolean existsBySerialNumber(String serialNumber);
}