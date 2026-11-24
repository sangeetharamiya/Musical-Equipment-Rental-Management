package com.rental.music.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record BookingRequest(
    @NotNull(message = "Equipment ID is required") Long equipmentId,
    @NotBlank(message = "Customer name is required") String customerName,
    @NotBlank(message = "Customer email is required") @Email(message = "Invalid email") String customerEmail,
    @NotBlank(message = "Postal code is required") String postalCode,
    @NotNull(message = "Start date is required") LocalDate startDate,
    @NotNull(message = "End date is required") LocalDate endDate
)