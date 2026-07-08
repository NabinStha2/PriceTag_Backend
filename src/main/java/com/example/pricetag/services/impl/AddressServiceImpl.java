package com.example.pricetag.services.impl;

import com.example.pricetag.dto.AddressDto;
import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.entity.Address;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.auth.service.AuthService;
import com.example.pricetag.features.user.entity.User;
import com.example.pricetag.features.user.repository.UserRepo;
import com.example.pricetag.services.AddressService;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AddressServiceImpl implements AddressService {

    private final AuthService authService;
    private final UserRepo userRepo;

    public AddressServiceImpl(
            AuthService authService,
            UserRepo userRepo) {
        this.authService = authService;
        this.userRepo = userRepo;
    }

    @Override
    public CommonResponseDto createAddress(AddressDto addressDto) {
        User user = authService.getUser();

        try {
//            if (user.getAddresses() != null) {
//                user.getAddresses().setStreet(addressDto.getStreet());
//                user.getAddresses().setCity(addressDto.getCity());
//                user.getAddresses().setPostalCode(addressDto.getPostalCode());
//                userRepo.save(user);
//            } else {
            Address address = Address
                    .builder()
                    .street(addressDto.getStreet())
                    .city(addressDto.getCity())
                    .postalCode(addressDto.getPostalCode())
                    .build();
            user.setAddresses(List.of(address));
            userRepo.save(user);
//            }
            return CommonResponseDto
                    .builder()
                    .success(true)
                    .data(Map.of("results", user.getAddresses()))
                    .message("Address created successfully")
                    .build();
        } catch (DataAccessException e) {
            throw new ApplicationException("500", "Address creation failed",
                                           HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public CommonResponseDto getAddress() {
        User user = authService.getUser();

        if (user.getAddresses() == null) {
            throw new ApplicationException("404", "Address not found", HttpStatus.NOT_FOUND);
        }

        return CommonResponseDto
                .builder()
                .success(true)
                .data(Map.of("results", user.getAddresses()))
                .message("Address fetched successfully")
                .build();
    }


}
