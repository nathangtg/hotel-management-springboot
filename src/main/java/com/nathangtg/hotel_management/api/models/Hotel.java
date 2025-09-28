package com.nathangtg.hotel_management.api.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "hotels")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Hotel extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    @NotBlank(message = "Hotel name is required")
    private String name;

    @Column(name = "address", nullable = false)
    @NotBlank(message = "Hotel address is required")
    private String address;

    @Column(name = "phone", nullable = false)
    @NotBlank(message = "Hotel phone is required")
    private String phone;

    @Column(name = "email", nullable = false)
    @Email(message = "Email should be valid")
    @NotBlank(message = "Hotel email is required")
    private String email;

    @OneToMany(mappedBy = "hotel")
    private List<Management> managements = new ArrayList<>();
}
