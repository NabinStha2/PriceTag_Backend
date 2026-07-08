package com.example.pricetag.features.user.service.impl;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.exceptions.ApplicationException;
import com.example.pricetag.features.auth.entity.Role;
import com.example.pricetag.features.user.dto.responseDto.UserResponseDto;
import com.example.pricetag.features.user.entity.User;
import com.example.pricetag.features.user.repository.UserRepo;
import com.example.pricetag.features.user.service.UserService;
import com.example.pricetag.utils.ColorLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserResponseDto getMyUser(String email) throws ApplicationException {
        User user = userRepo
                .findByEmail(email)
                .orElse(null);

        if (user == null) {
            throw new ApplicationException("404", "User not found", HttpStatus.NOT_FOUND);
        }
        //                .cartItems(user.getCartItems().stream().sorted(Comparator.comparing(CartItem::getCreatedAt).reversed()).toList())
        //                .appUserRole(user.getAppUserRole())

        return UserResponseDto
                .builder()
                .id(user.getId())
                .addresses(user.getAddresses())
//                .cartItems(user.getCartItems().stream().sorted(Comparator.comparing(CartItem::getCreatedAt).reversed()).toList())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .appUserRole(user
                                     .getRoles()
                                     .stream()
                                     .map(Role::getName)
                                     .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

    }

    @Override
    public UserResponseDto findUserById(int id) {
        User user = userRepo
                .findById(id)
                .orElse(null);
        if (user == null) {
            throw new ApplicationException("404", "User not found", HttpStatus.NOT_FOUND);
        }

        //                .appUserRole(user.getAppUserRole())
        return UserResponseDto
                .builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
//                .appUserRole(user.getAppUserRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

    }

    @Override
    public CommonResponseDto deleteUser(String email) {
        User user = userRepo
                .findByEmail(email)
                .orElse(null);

        if (user == null) {
            throw new ApplicationException("404", "User not found", HttpStatus.NOT_FOUND);
        }
        userRepo.delete(user);
        return CommonResponseDto
                .builder()
                .message("User has been deleted")
                .success(true)
                .build();

    }

    @Override
    public UserResponseDto updateUser(User user) {
        try {
            ColorLogger.logInfo("I am inside updateUser :: " + user.getEmail());

            UserDetails userDetails = (UserDetails) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();

            User existingUser = userRepo
                    .findByEmail(userDetails.getUsername())
                    .orElse(null);

            if (existingUser == null) {
                throw new ApplicationException("404", "User not found", HttpStatus.NOT_FOUND);
            }

            existingUser.setName(user.getName() == null ? existingUser.getName() : user.getName());
            existingUser
                    .setPhoneNumber(user.getPhoneNumber() == null ? existingUser.getPhoneNumber() :
                                    user.getPhoneNumber());

            userRepo.save(existingUser);
            //                    .appUserRole(existingUser.getAppUserRole())
            return UserResponseDto
                    .builder()
                    .id(existingUser.getId())
                    .email(existingUser.getEmail())
                    .name(existingUser.getName())
                    .phoneNumber(existingUser.getPhoneNumber())
//                    .appUserRole(existingUser.getAppUserRole())
                    .createdAt(existingUser.getCreatedAt())
                    .updatedAt(existingUser.getUpdatedAt())
                    .build();
        } catch (Exception e) {
            throw new ApplicationException("500", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
