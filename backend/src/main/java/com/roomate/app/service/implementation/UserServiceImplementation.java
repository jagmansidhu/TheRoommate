package com.roomate.app.service.implementation;

import com.roomate.app.dto.RegisterDto;
import com.roomate.app.dto.UserDTOS.UpdateProfileDto;
import com.roomate.app.entities.UserEntity;
import com.roomate.app.entities.VerificationTokenEntity;
import com.roomate.app.repository.UserRepository;
import com.roomate.app.repository.VerificationTokenRepository;
import com.roomate.app.service.JWTService;
import com.roomate.app.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService, UserDetailsService {

    public static final String VERIFY_EMAIL_URL = "http://localhost:8085/user/verify?token=";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final JavaMailSender mailSender;

    // EFFECTS :Checks if User currently exists in db
    @Override
    public boolean userExists(String email) {
        return userRepository.existsByEmail(email.toLowerCase(Locale.ROOT));
    }

    @Override
    public String registerUser(RegisterDto req) throws DuplicateKeyException {
        UserEntity user = new UserEntity();
        String email = req.getEmail();

        if (userExists(email)) {
            throw new DuplicateKeyException(email);
        }

        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEnabled(true); // TODO: Set back to false to re-enable email verification
        UserEntity savedUser = userRepository.save(user);

        // TODO: Uncomment to re-enable email verification
        // String token = createToken(savedUser);

        // sendVerificationEmail(user.getEmail(), token);

        return jwtService.generateToken(user);
    }

    @Override
    public String createToken(UserEntity savedUser) {
        verificationTokenRepository.deleteByUser_Id(savedUser.getId());
        String token = UUID.randomUUID().toString();
        VerificationTokenEntity verificationToken = new VerificationTokenEntity(savedUser, token);
        verificationTokenRepository.save(verificationToken);
        return token;
    }

    @Override
    public void sendVerificationEmail(@NotNull String email, String token) {
        String subject = "Email Verification";
        String verificationUrl = VERIFY_EMAIL_URL + token;
        String body = "Click the link to verify your account: " + verificationUrl;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("DaRoommate Team <roomate@example.com>");
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    @Override
    public boolean verifyToken(String token) {
        Optional<VerificationTokenEntity> optional = verificationTokenRepository.findByToken(token);

        if (optional.isPresent()) {
            VerificationTokenEntity verificationToken = optional.get();
            if (!verificationToken.isExpired()) {
                UserEntity user = verificationToken.getUser();
                user.setEnabled(true);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    // EFFECTS : Updates user profile with new First name, Last name, and Phone num
    @Override
    public UserEntity updateUserProfile(String email, UpdateProfileDto updatedDetails) {
        UserEntity user = getUserEntityByEmail(email);

        if (updatedDetails.getEmail() != null && !updatedDetails.getEmail().isEmpty()) {
            user.setEmail(email.toLowerCase());
        }

        if (updatedDetails.getFirstName() != null && !updatedDetails.getFirstName().isEmpty()) {
            user.setFirstName(updatedDetails.getFirstName().toLowerCase());
        }
        if (updatedDetails.getLastName() != null && !updatedDetails.getLastName().isEmpty()) {
            user.setLastName(updatedDetails.getLastName().toLowerCase());
        }

        if (updatedDetails.getPhone() != null && !updatedDetails.getPhone().isEmpty()) {
            user.setPhone(updatedDetails.getPhone());
        }

        if (updatedDetails.getCurPassword() != null &&
                updatedDetails.getPassword() != null &&
                !updatedDetails.getPassword().isEmpty()) {

            if (!passwordEncoder.matches(updatedDetails.getCurPassword(), user.getPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");

            }

            user.setPassword(passwordEncoder.encode(updatedDetails.getPassword()));
        }

        return userRepository.save(user);
    }

    // EFFECTS : Determines if profile is complete in the database
    @Override
    public boolean isProfileCompleteInDatabase(String email) {
        UserEntity user = getUserEntityByEmail(email);

        boolean firstNameProvided = user.getFirstName() != null && !user.getFirstName().isEmpty();
        boolean lastNameProvided = user.getLastName() != null && !user.getLastName().isEmpty();
        boolean phoneProvided = user.getPhone() != null && !user.getPhone().isEmpty();

        return firstNameProvided & lastNameProvided & phoneProvided;
    }

    @Override
    public UserEntity getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(user -> new User(
                        user.getEmail(),
                        user.getPassword(),
                        getAuthorities(user)))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(UserEntity user) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRoles()));
    }
}