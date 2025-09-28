package com.nathangtg.hotel_management.api.models;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "rooms")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Room extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", nullable = false, unique = true)
    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @Column(name = "room_type", nullable = false)
    @NotBlank(message = "Room type is required")
    private String roomType;

    @Column(name = "capacity", nullable = false)
    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be positive")
    private Integer capacity;

    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Price per night is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price per night must be greater than 0")
    private BigDecimal pricePerNight;

    @Column(name = "is_available", nullable = false)
    @NotNull(message = "Availability status is required")
    private Boolean isAvailable = true;

    @ManyToOne
    @NotNull(message = "Hotel is required for room")
    private Hotel hotel;

    @OneToMany(mappedBy = "room")
    private List<Booking> bookings;
}