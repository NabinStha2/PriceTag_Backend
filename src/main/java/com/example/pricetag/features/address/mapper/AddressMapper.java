package com.example.pricetag.features.address.mapper;

import com.example.pricetag.features.address.dto.request.AddressRequestDto;
import com.example.pricetag.features.address.dto.response.AddressResponseDto;
import com.example.pricetag.features.address.entity.Address;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING
//        , nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AddressMapper {

    Address mapAddressRequestDtoToAddress(AddressRequestDto addressRequestDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",
            ignore = true)
    @Mapping(target = "user",
            ignore = true)
    @Mapping(target = "isDefault",
            ignore = true)
    @Mapping(target = "createdAt",
            ignore = true)
    @Mapping(target = "updatedAt",
            ignore = true)
    void updateAddressFromRequestDto(AddressRequestDto addressRequestDto,
                                     @MappingTarget
                                     Address address);

    AddressResponseDto mapAddressToAddressResponseDto(Address address);
}
