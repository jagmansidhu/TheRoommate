package com.roomate.app.controller;

import com.roomate.app.dto.UserDTOS.UserDto;
import com.roomate.app.entities.UserEntity;
import com.roomate.app.service.implementation.UserServiceImplementation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserServiceImplementation userService;

    public UserController(UserServiceImplementation userService) {
        this.userService = userService;
    }

    // EFFECTS : Determines if user information has been inputted and complete
    @GetMapping("/profile-status")
    public ResponseEntity<Map<String, Boolean>> getProfileCompletionStatus(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        boolean isComplete = userService.isProfileCompleteInDatabase(email);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isComplete", isComplete);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get-user")
    public ResponseEntity<UserDto> getUser(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();

        UserEntity user = userService.getUserEntityByEmail(email);

        UserDto response = new UserDto();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhone(user.getPhone());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}