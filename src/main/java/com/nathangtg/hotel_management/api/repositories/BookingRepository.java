package com.nathangtg.hotel_management.api.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nathangtg.hotel_management.api.models.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser_Id(Long userId);
    List<Booking> findByRoom_Id(Long roomId);
    List<Booking> findByStatus(String status);
    List<Booking> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate);
    List<Booking> findByCheckOutDateBetween(LocalDate startDate, LocalDate endDate);
}