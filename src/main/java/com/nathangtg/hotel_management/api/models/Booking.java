package com.nathangtg.hotel_management.api.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "bookings")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Booking extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "check_in_date", nullable = false)
    @NotNull(message = "Check-in date is required")
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutDate;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Total price is required")
    private BigDecimal totalPrice;

    @Column(name = "status", nullable = false)
    @NotNull(message = "Booking status is required")
    private String status = "PENDING"; // PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required for booking")
    private User user;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    @NotNull(message = "Room is required for booking")
    private Room room;
    
    // Convenience method to get user ID
    @Transient
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
    
    // Convenience method to set user by ID (for DTO conversion)
    @Transient
    public void setUserId(Long userId) {
        if (this.user == null) {
            this.user = new User();
        }
        this.user.setId(userId);
    }
    
    // Convenience method to get room ID
    @Transient
    public Long getRoomId() {
        return room != null ? room.getId() : null;
    }
    
    // Convenience method to set room by ID (for DTO conversion)
    @Transient
    public void setRoomId(Long roomId) {
        if (this.room == null) {
            this.room = new Room();
        }
        this.room.setId(roomId);
    }
}