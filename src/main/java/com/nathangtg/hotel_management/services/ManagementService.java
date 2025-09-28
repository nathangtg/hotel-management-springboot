package com.nathangtg.hotel_management.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nathangtg.hotel_management.api.models.Management;
import com.nathangtg.hotel_management.api.repositories.ManagementRepository;

@Service
public class ManagementService {

    @Autowired
    public ManagementRepository managementRepository;

    public List<Management> getAllManagements() {
        return managementRepository.findAll();
    }

    public Optional<Management> getManagementById(Long id) {
        return managementRepository.findById(id);
    }

    public Management createManagement(Management management) {
        return managementRepository.save(management);
    }

    public Management updateManagement(Long id, Management management) {
        if (managementRepository.existsById(id)) {
            management.setId(id);
            return managementRepository.save(management);
        } else {
            throw new RuntimeException("Management not found with id: " + id);
        }
    }

    public void deleteManagement(Long id) {
        if (managementRepository.existsById(id)) {
            managementRepository.deleteById(id);
        } else {
            throw new RuntimeException("Management not found with id: " + id);
        }
    }
}