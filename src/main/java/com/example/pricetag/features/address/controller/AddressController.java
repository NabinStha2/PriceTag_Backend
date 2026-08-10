package com.example.pricetag.features.address.controller;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.address.dto.request.AddressRequestDto;
import com.example.pricetag.features.address.dto.response.AddressResponseDto;
import com.example.pricetag.features.address.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
            AddressRequestDto addressRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(addressService.createAddress(addressRequestDto));
    }

    @GetMapping
    public ResponseEntity<CommonResponseDto<List<AddressResponseDto>>> getAddress() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(addressService.getAddress());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponseDto<AddressResponseDto>> updateAddress(
            @PathVariable
            Long id,
            @RequestBody
            AddressRequestDto addressRequestDto) {
        return ResponseEntity.ok(addressService.updateAddress(id, addressRequestDto));
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
