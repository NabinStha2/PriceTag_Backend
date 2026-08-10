package com.example.pricetag.features.address.service.impl;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.address.dto.request.AddressRequestDto;
import com.example.pricetag.features.address.dto.response.AddressResponseDto;
import com.example.pricetag.features.address.entity.Address;
import com.example.pricetag.features.address.mapper.AddressMapper;
import com.example.pricetag.features.address.repository.AddressRepo;
import com.example.pricetag.features.address.service.AddressService;
import com.example.pricetag.features.auth.service.AuthService;
import com.example.pricetag.features.user.entity.User;
import com.example.pricetag.features.user.repository.UserRepo;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AuthService authService;
    private final UserRepo userRepo;
    private final AddressRepo addressRepo;
    private final AddressMapper addressMapper;

    public AddressServiceImpl(AuthService authService, UserRepo userRepo, AddressRepo addressRepo,
                              AddressMapper addressMapper) {
        this.authService = authService;
        this.userRepo = userRepo;
        this.addressRepo = addressRepo;
        this.addressMapper = addressMapper;
    }

    @Override
    public CommonResponseDto<List<AddressResponseDto>> createAddress(
            AddressRequestDto addressRequestDto) {
        User authenticatedUser = authService.getUser();
        User user = userRepo
                .findByIdForUpdate(authenticatedUser.getId())
                .orElseThrow(() -> new ApplicationException("404", "User not found",
                                                            HttpStatus.NOT_FOUND));

        try {
            Address address = addressMapper.mapAddressRequestDtoToAddress(addressRequestDto);
            address.setUser(user);

            List<Address> existingAddresses = user.getAddresses();
            if (existingAddresses == null) {
                existingAddresses = new ArrayList<>();
                user.setAddresses(existingAddresses);
            }

            boolean isFirstAddress = existingAddresses.isEmpty();
            boolean shouldBeDefault =
                    isFirstAddress || Boolean.TRUE.equals(addressRequestDto.getIsDefault());

            if (shouldBeDefault) {
                for (Address existing : existingAddresses) {
                    if (Boolean.TRUE.equals(existing.getIsDefault())) {
                        existing.setIsDefault(false);
                    }
                }
                address.setIsDefault(true);
            } else {
                address.setIsDefault(false);
            }

            existingAddresses.add(address);
            addressRepo.saveAndFlush(address);

            List<AddressResponseDto> addressResponseDtos = existingAddresses
                    .stream()
                    .map(addressMapper::mapAddressToAddressResponseDto)
                    .toList();

            return CommonResponseDto
                    .<List<AddressResponseDto>>builder()
                    .success(true)
                    .data(addressResponseDtos)
                    .message("Address created successfully")
                    .build();
        } catch (DataAccessException e) {
            throw new ApplicationException("500", "Address creation failed",
                                           HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CommonResponseDto<List<AddressResponseDto>> getAddress() {
        User user = authService.getUser();
        List<Address> addresses = user.getAddresses();

        if (addresses == null || addresses.isEmpty()) {
            throw new ApplicationException("404", "Addresses not found", HttpStatus.NOT_FOUND);
        }

        List<AddressResponseDto> addressResponseDtos = addresses
                .stream()
                .map(addressMapper::mapAddressToAddressResponseDto)
                .toList();

        return CommonResponseDto
                .<List<AddressResponseDto>>builder()
                .success(true)
                .data(addressResponseDtos)
                .message("Addresses fetched successfully")
                .build();
    }

    @Override
    public CommonResponseDto<List<AddressResponseDto>> updateAddress(Long id,
                                                                     AddressRequestDto addressRequestDto) {
        User user = authService.getUser();
        Address address = addressRepo
                .findById(id)
                .orElseThrow(() -> new ApplicationException("404", "Address not found",
                                                            HttpStatus.NOT_FOUND));

        if (!address
                .getUser()
                .getId()
                .equals(user.getId())) {
            throw new ApplicationException("403", "Unauthorized to update this address",
                                           HttpStatus.FORBIDDEN);
        }

        try {
            address.setUnitNumber(addressRequestDto.getUnitNumber());
            address.setStreetNumber(addressRequestDto.getStreetNumber());
            address.setStreet(addressRequestDto.getStreet());
            address.setCity(addressRequestDto.getCity());
            address.setState(addressRequestDto.getState());
            address.setCountry(addressRequestDto.getCountry());
            address.setPostalCode(addressRequestDto.getPostalCode());
            address.setLabel(addressRequestDto.getLabel());

            if (Boolean.TRUE.equals(addressRequestDto.getIsDefault()) &&
                !Boolean.TRUE.equals(address.getIsDefault())) {
                List<Address> existingAddresses = user.getAddresses();
                for (Address existing : existingAddresses) {
                    if (!existing
                            .getId()
                            .equals(address.getId()) &&
                        Boolean.TRUE.equals(existing.getIsDefault())) {
                        existing.setIsDefault(false);
                    }
                }
                address.setIsDefault(true);
            }

            addressRepo.save(address);

            List<AddressResponseDto> addressResponseDtos = user
                    .getAddresses()
                    .stream()
                    .map(addressMapper::mapAddressToAddressResponseDto)
                    .toList();

            return CommonResponseDto
                    .<List<AddressResponseDto>>builder()
                    .success(true)
                    .data(addressResponseDtos)
                    .message("Address updated successfully")
                    .build();
        } catch (DataAccessException e) {
            throw new ApplicationException("500", "Address update failed",
                                           HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public CommonResponseDto<List<AddressResponseDto>> deleteAddress(Long id) {
        User user = authService.getUser();
        Address address = addressRepo
                .findById(id)
                .orElseThrow(() -> new ApplicationException("404", "Address not found",
                                                            HttpStatus.NOT_FOUND));

        if (!address
                .getUser()
                .getId()
                .equals(user.getId())) {
            throw new ApplicationException("403", "Unauthorized to delete this address",
                                           HttpStatus.FORBIDDEN);
        }

        try {
            boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());

            user
                    .getAddresses()
                    .remove(address);
            address.setUser(null);
            userRepo.save(user);

            if (wasDefault && !user
                    .getAddresses()
                    .isEmpty()) {
                Address newDefault = user
                        .getAddresses()
                        .getFirst();
                newDefault.setIsDefault(true);
                addressRepo.save(newDefault);
            }

            List<AddressResponseDto> addressResponseDtos = user
                    .getAddresses()
                    .stream()
                    .map(addressMapper::mapAddressToAddressResponseDto)
                    .toList();

            return CommonResponseDto
                    .<List<AddressResponseDto>>builder()
                    .success(true)
                    .data(addressResponseDtos)
                    .message("Address deleted successfully")
                    .build();
        } catch (DataAccessException e) {
            throw new ApplicationException("500", "Address deletion failed",
                                           HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public CommonResponseDto<List<AddressResponseDto>> setDefaultAddress(Long id) {
        User user = authService.getUser();
        Address address = addressRepo
                .findById(id)
                .orElseThrow(() -> new ApplicationException("404", "Address not found",
                                                            HttpStatus.NOT_FOUND));

        if (!address
                .getUser()
                .getId()
                .equals(user.getId())) {
            throw new ApplicationException("403", "Unauthorized to modify this address",
                                           HttpStatus.FORBIDDEN);
        }

        try {
            List<Address> existingAddresses = user.getAddresses();
            for (Address existing : existingAddresses) {
                if (existing
                        .getId()
                        .equals(address.getId())) {
                    existing.setIsDefault(true);
                } else if (Boolean.TRUE.equals(existing.getIsDefault())) {
                    existing.setIsDefault(false);
                }
            }

            userRepo.save(user);

            List<AddressResponseDto> addressResponseDtos = existingAddresses
                    .stream()
                    .map(addressMapper::mapAddressToAddressResponseDto)
                    .toList();

            return CommonResponseDto
                    .<List<AddressResponseDto>>builder()
                    .success(true)
                    .data(addressResponseDtos)
                    .message("Default address set successfully")
                    .build();
        } catch (DataAccessException e) {
            throw new ApplicationException("500", "Failed to set default address",
                                           HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
