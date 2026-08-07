package com.example.pricetag.features.address.controller;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.address.dto.requestDto.AddressDto;
import com.example.pricetag.features.address.service.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }


    @PostMapping
    private ResponseEntity<CommonResponseDto> createAddress(
            @RequestBody
            AddressDto addressDto) {
        return ResponseEntity.ok(addressService.createAddress(addressDto));
    }

    @GetMapping
    private ResponseEntity<CommonResponseDto> getAddress() {
        return ResponseEntity.ok(addressService.getAddress());
    }

}
