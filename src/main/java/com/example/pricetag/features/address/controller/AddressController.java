package com.example.pricetag.features.address.controller;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.address.dto.requestDto.AddressResponseDto;
import com.example.pricetag.features.address.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    public ResponseEntity<CommonResponseDto<List<AddressResponseDto>>> createAddress(
            @Valid
            @RequestBody
            AddressResponseDto addressResponseDto) {
        return ResponseEntity.ok(addressService.createAddress(addressResponseDto));
    }

    @GetMapping
    public ResponseEntity<CommonResponseDto<List<AddressResponseDto>>> getAddress() {
        return ResponseEntity.ok(addressService.getAddress());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponseDto<List<AddressResponseDto>>> updateAddress(
            @PathVariable
            Long id,
            @Valid
            @RequestBody
            AddressResponseDto addressResponseDto) {
        return ResponseEntity.ok(addressService.updateAddress(id, addressResponseDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponseDto<List<AddressResponseDto>>> deleteAddress(
            @PathVariable
            Long id) {
        return ResponseEntity.ok(addressService.deleteAddress(id));
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<CommonResponseDto<List<AddressResponseDto>>> setDefaultAddress(
            @PathVariable
            Long id) {
        return ResponseEntity.ok(addressService.setDefaultAddress(id));
    }
}
