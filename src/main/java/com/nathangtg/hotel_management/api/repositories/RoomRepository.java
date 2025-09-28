package com.nathangtg.hotel_management.api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nathangtg.hotel_management.api.models.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotelId(Long hotelId);
    List<Room> findByIsAvailableTrue();
    List<Room> findByRoomType(String roomType);
}