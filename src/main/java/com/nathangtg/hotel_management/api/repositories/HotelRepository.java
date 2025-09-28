package com.nathangtg.hotel_management.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nathangtg.hotel_management.api.models.Hotel;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
}