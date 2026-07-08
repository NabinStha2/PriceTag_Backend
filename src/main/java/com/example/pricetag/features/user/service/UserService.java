package com.example.pricetag.features.user.service;

import com.example.pricetag.dto.CommonResponseDto;
import com.example.pricetag.features.user.dto.responseDto.UserResponseDto;
import com.example.pricetag.features.user.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    UserResponseDto getMyUser(String email);

    UserResponseDto findUserById(int id);

    CommonResponseDto deleteUser(String email);

    UserResponseDto updateUser(User user);

}
