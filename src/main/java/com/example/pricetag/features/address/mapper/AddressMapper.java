package com.example.pricetag.features.address.mapper;

import com.example.pricetag.features.address.dto.request.AddressRequestDto;
import com.example.pricetag.features.address.dto.response.AddressResponseDto;
import com.example.pricetag.features.address.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AddressMapper {

    Address mapAddressRequestDtoToAddress(AddressRequestDto addressRequestDto);

    AddressResponseDto mapAddressToAddressResponseDto(Address address);
}
