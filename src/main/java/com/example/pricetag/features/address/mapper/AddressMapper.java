package com.example.pricetag.features.address.mapper;

import com.example.pricetag.features.address.dto.requestDto.AddressResponseDto;
import com.example.pricetag.features.address.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AddressMapper {

    Address toEntity(AddressResponseDto dto);

    AddressResponseDto toDto(Address entity);
}
