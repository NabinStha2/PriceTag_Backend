package com.example.pricetag.features.address.service;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.address.dto.requestDto.AddressDto;
import org.springframework.stereotype.Service;

@Service
public interface AddressService {

    CommonResponseDto createAddress(AddressDto addressDto);

    CommonResponseDto getAddress();

}
