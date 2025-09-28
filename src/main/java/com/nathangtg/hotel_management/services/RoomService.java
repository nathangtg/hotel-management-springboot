package com.nathangtg.hotel_management.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nathangtg.hotel_management.api.models.Room;
import com.nathangtg.hotel_management.api.repositories.RoomRepository;

@Service
public class RoomService {

    @Autowired
    public RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    public List<Room> getRoomsByHotelId(Long hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }

    public List<Room> getAvailableRooms() {
        return roomRepository.findByIsAvailableTrue();
    }

    public List<Room> getRoomsByType(String roomType) {
        return roomRepository.findByRoomType(roomType);
    }

    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    public Room updateRoom(Long id, Room room) {
        if (roomRepository.existsById(id)) {
            room.setId(id);
            return roomRepository.save(room);
        } else {
            throw new RuntimeException("Room not found with id: " + id);
        }
    }

    public void deleteRoom(Long id) {
        if (roomRepository.existsById(id)) {
            roomRepository.deleteById(id);
        } else {
            throw new RuntimeException("Room not found with id: " + id);
        }
    }
}