package com.example.pricetag.features.user.controller;

import com.example.pricetag.features.user.dto.responseDto.UserResponseDto;
import com.example.pricetag.features.user.entity.User;
import com.example.pricetag.features.user.service.UserService;
import com.example.pricetag.utils.ColorLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<UserResponseDto> getMyUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return ResponseEntity.ok(userService.getMyUser(userDetails.getUsername()));
    }

    @PatchMapping("/edit")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<UserResponseDto> updateUser(
            @RequestBody
            User user) {
        ColorLogger.logInfo("I am inside updateUser Controller " + user);
        return ResponseEntity.ok(userService.updateUser(user));
    }

}
