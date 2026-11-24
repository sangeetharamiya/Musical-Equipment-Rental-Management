package com.rental.music.controller;

import com.rental.music.dto.BookingRequest;
import com.rental.music.model.Booking;
import com.rental.music.service.BookingService;
import com.rental.music.service.LocationLookupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final LocationLookupService locationLookupService;

    public BookingController(BookingService bookingService, LocationLookupService locationLookupService) {
        this.bookingService = bookingService;
        this.locationLookupService = locationLookupService;
    }

    @GetMapping
    public List<Booking> getAll() {
        return bookingService.findAll();
    }

    @GetMapping("/{id}")
    public Booking getById(@PathVariable Long id) {
        return bookingService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking create(@Valid @RequestBody BookingRequest request) {
        return bookingService.create(request);
    }

    @PutMapping("/{id}")
    public Booking update(@PathVariable Long id, @Valid @RequestBody BookingRequest request) {
        return bookingService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bookingService.delete(id);
    }

    @GetMapping("/lookup-postal/{postalCode}")
    public Map<String, String> lookupPostal(@PathVariable String postalCode) {
        return Map.of("location", locationLookupService.resolveCityState(postalCode));
    }
}