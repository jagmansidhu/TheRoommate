package com.roomate.app.controller;

import com.roomate.app.dto.UserDTOS.UpdateProfileDto;
import com.roomate.app.dto.UserDTOS.UserDto;
import com.roomate.app.entities.UserEntity;
import com.roomate.app.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/profile/")
public class ProfileController {
    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/update_profile")
    public ResponseEntity<UserDto> updateUser(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UpdateProfileDto updatedDetails) {
        String email = userDetails.getUsername();
        UserEntity updateUser = userService.updateUserProfile(email, updatedDetails);
        UserDto response = new UserDto();
        response.setId(updateUser.getId());
        response.setEmail(updateUser.getEmail());
        response.setFirstName(updateUser.getFirstName());
        response.setLastName(updateUser.getLastName());
        response.setPhone(updateUser.getPhone());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
