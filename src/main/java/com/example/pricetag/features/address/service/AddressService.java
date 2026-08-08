package com.example.pricetag.features.address.service;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.address.dto.requestDto.AddressResponseDto;

import java.util.List;

public interface AddressService {

    CommonResponseDto<List<AddressResponseDto>> createAddress(
            AddressResponseDto addressResponseDto);

    CommonResponseDto<List<AddressResponseDto>> getAddress();

    CommonResponseDto<List<AddressResponseDto>> updateAddress(Long id,
                                                              AddressResponseDto addressResponseDto);

    CommonResponseDto<List<AddressResponseDto>> deleteAddress(Long id);

    CommonResponseDto<List<AddressResponseDto>> setDefaultAddress(Long id);
}
