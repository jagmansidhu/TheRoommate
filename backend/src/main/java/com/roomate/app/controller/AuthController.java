package com.roomate.app.controller;

import com.roomate.app.dto.AuthDto;
import com.roomate.app.dto.LoginDto;
import com.roomate.app.dto.RegisterDto;
import com.roomate.app.dto.UserDTOS.UpdateProfileDto;
import com.roomate.app.entities.UserEntity;
import com.roomate.app.repository.UserRepository;
import com.roomate.app.service.JWTService;
import com.roomate.app.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class AuthController {
    private final UserService userService;
    private final JWTService jwtService;
    private final AuthenticationManager authManager;
    private final UserRepository userRepository;

    @GetMapping("/verify-status")
    public ResponseEntity<Map<String, Object>> getUserStatus(@AuthenticationPrincipal UserDetails user) {
        UserEntity usere = userRepository.getUserByEmail(user.getUsername());
        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", usere != null);
        response.put("verified", usere.isEnabled());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@AuthenticationPrincipal UserDetails user) {
        UserEntity usere = userRepository.getUserByEmail(user.getUsername());
        // System.out.println(usere.getEmail() + " " + user.getUsername() + " " +
        // usere.isEnabled());
        if (!user.isEnabled()) {
            return ResponseEntity.badRequest().body("User already verified or not logged in");
        }
        String token = userService.createToken(usere);
        userService.sendVerificationEmail(user.getUsername(), token);
        return ResponseEntity.ok("Verification email resent");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto req) {
        try {
            String token = userService.registerUser(req);
            return ResponseEntity.ok(new AuthDto(token));
        } catch (DuplicateKeyException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "User with this email already exists.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<Boolean> verify(@RequestParam("token") String token) {
        Boolean result = userService.verifyToken(token);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/updateProfile")
    public ResponseEntity<UserEntity> updateProfile(@Valid @RequestBody UpdateProfileDto req,
            HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        UserEntity updatedUser = userService.updateUserProfile(email, req);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto> login(@Valid @RequestBody LoginDto req, HttpServletRequest request,
            HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                Cookie cleared = new Cookie(cookie.getName(), "");
                cleared.setPath("/");
                cleared.setMaxAge(0);
                cleared.setHttpOnly(true);
                response.addCookie(cleared);
            }
        }

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        UserEntity user = userService.getUserEntityByEmail(req.getEmail());

        String token = jwtService.generateToken(user);

        boolean isSecure = request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));

        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .sameSite(isSecure ? "None" : "Lax")
                .maxAge(7 * 24 * 60 * 60)
                .build();

        response.setHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(new AuthDto(token));
    }

    @GetMapping("/status")
    public ResponseEntity<?> authStatus(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String token = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }


        if (token == null || token.isEmpty() || !token.contains(".")) {
            return ResponseEntity.status(401).body("Invalid or missing token");
        }

        try {
            if (jwtService.isTokenValid(token)) {
                String user = jwtService.extractUsername(token);
                return ResponseEntity.ok(Map.of("username", user));
            } else {
                return ResponseEntity.status(401).body("Invalid token");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body("Malformed token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        boolean isSecure = request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));

        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .sameSite(isSecure ? "None" : "Lax")
                .maxAge(0)
                .build();
        response.setHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok().build();
    }
}
