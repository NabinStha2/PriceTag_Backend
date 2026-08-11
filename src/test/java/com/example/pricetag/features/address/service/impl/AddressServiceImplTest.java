package com.example.pricetag.features.address.service.impl;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.address.dto.response.AddressResponseDto;
import com.example.pricetag.features.address.entity.Address;
import com.example.pricetag.features.address.mapper.AddressMapper;
import com.example.pricetag.features.address.repository.AddressRepo;
import com.example.pricetag.features.auth.service.AuthService;
import com.example.pricetag.features.user.entity.User;
import com.example.pricetag.features.user.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserRepo userRepo;

    @Mock
    private AddressRepo addressRepo;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    private User user;
    private Address currentDefaultAddress;
    private Address newDefaultAddress;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
        currentDefaultAddress = address(10L, user, true);
        newDefaultAddress = address(20L, user, false);
        user.setAddresses(new ArrayList<>(List.of(currentDefaultAddress, newDefaultAddress)));
    }

    @Test
    void setDefaultAddressLocksUserAndChangesTheDefault() {
        when(authService.getUser()).thenReturn(user);
        when(userRepo.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(addressRepo.findById(newDefaultAddress.getId()))
                .thenReturn(Optional.of(newDefaultAddress));
        when(addressMapper.mapAddressToAddressResponseDto(currentDefaultAddress))
                .thenAnswer(invocation -> responseFor(currentDefaultAddress));
        when(addressMapper.mapAddressToAddressResponseDto(newDefaultAddress))
                .thenAnswer(invocation -> responseFor(newDefaultAddress));

        CommonResponseDto<List<AddressResponseDto>> response =
                addressService.setDefaultAddress(newDefaultAddress.getId());

        assertThat(currentDefaultAddress.getIsDefault()).isFalse();
        assertThat(newDefaultAddress.getIsDefault()).isTrue();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData())
                .extracting(AddressResponseDto::getIsDefault)
                .containsExactly(false, true);
        verify(userRepo).findByIdForUpdate(user.getId());
        verify(userRepo).save(user);
    }

    @Test
    void setDefaultAddressRejectsAnAddressOwnedByAnotherUser() {
        User otherUser = User.builder().id(2L).build();
        Address otherAddress = address(30L, otherUser, false);
        when(authService.getUser()).thenReturn(user);
        when(userRepo.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(addressRepo.findById(otherAddress.getId())).thenReturn(Optional.of(otherAddress));

        assertThatThrownBy(() -> addressService.setDefaultAddress(otherAddress.getId()))
                .isInstanceOfSatisfying(ApplicationException.class, exception -> {
                    assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(exception.getErrorCode()).isEqualTo("403");
                });

        assertThat(currentDefaultAddress.getIsDefault()).isTrue();
        assertThat(newDefaultAddress.getIsDefault()).isFalse();
        verify(userRepo, never()).save(user);
    }

    @Test
    void setDefaultAddressFailsWhenAddressDoesNotExist() {
        when(authService.getUser()).thenReturn(user);
        when(userRepo.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(addressRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.setDefaultAddress(999L))
                .isInstanceOfSatisfying(ApplicationException.class, exception -> {
                    assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(exception.getErrorCode()).isEqualTo("404");
                });

        verify(userRepo, never()).save(user);
    }

    private Address address(Long id, User owner, boolean isDefault) {
        Address address = Address.builder()
                .user(owner)
                .isDefault(isDefault)
                .street("George Street")
                .city("Sydney")
                .state("NSW")
                .country("Australia")
                .postalCode("2000")
                .build();
        address.setId(id);
        return address;
    }

    private AddressResponseDto responseFor(Address address) {
        return AddressResponseDto.builder()
                .id(address.getId())
                .isDefault(address.getIsDefault())
                .build();
    }
}
