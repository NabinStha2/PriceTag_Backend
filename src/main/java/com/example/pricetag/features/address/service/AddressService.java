package com.example.pricetag.features.address.service;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.address.dto.request.AddressRequestDto;
import com.example.pricetag.features.address.dto.response.AddressResponseDto;

import java.util.List;

public interface AddressService {

    CommonResponseDto<List<AddressResponseDto>> createAddress(AddressRequestDto addressRequestDto);

    CommonResponseDto<List<AddressResponseDto>> getAddress();

    CommonResponseDto<AddressResponseDto> updateAddress(Long id,
                                                        AddressRequestDto addressRequestDto);

    CommonResponseDto<List<AddressResponseDto>> deleteAddress(Long id);

    CommonResponseDto<AddressResponseDto> setDefaultAddress(Long id);
}
