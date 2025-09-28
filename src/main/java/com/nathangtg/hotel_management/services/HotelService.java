package com.nathangtg.hotel_management.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nathangtg.hotel_management.api.models.Hotel;
import com.nathangtg.hotel_management.api.repositories.HotelRepository;

@Service
public class HotelService {

    @Autowired
    public HotelRepository hotelRepository;

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public Optional<Hotel> getHotelById(Long id) {
        return hotelRepository.findById(id);
    }

    public Hotel createHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    public Hotel updateHotel(Long id, Hotel hotel) {
        if (hotelRepository.existsById(id)) {
            hotel.setId(id);
            return hotelRepository.save(hotel);
        } else {
            throw new RuntimeException("Hotel not found with id: " + id);
        }
    }

    public void deleteHotel(Long id) {
        if (hotelRepository.existsById(id)) {
            hotelRepository.deleteById(id);
        } else {
            throw new RuntimeException("Hotel not found with id: " + id);
        }
    }
}