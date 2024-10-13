package com.nguyenminh.microservices.zwallet.service;

import com.nguyenminh.microservices.zwallet.configuration.AppConfiguration;
import com.nguyenminh.microservices.zwallet.model.PasswordResetToken;
import com.nguyenminh.microservices.zwallet.model.UserModel;
import com.nguyenminh.microservices.zwallet.repository.PasswordResetTokenRepository;
import com.nguyenminh.microservices.zwallet.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepository;
    private final AppConfiguration appConfiguration;
    private final Mapper mapper;
    private final TokenHandle tokenHandle;
    private final EncryptPasswordSerivce encryptPasswordSerivce;

    private final HttpServletRequest request;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    // change password
    public ResponseEntity<?> ChangePassword(String userName, String pass) {
        UserModel user = userRepository.findByUserName(userName);
        user.setPassword(encryptPasswordSerivce.encryptPassword(pass));
        userRepository.save(user);
        return ResponseEntity.ok("Password successfully change");
    }



    // forgotPassword
    public ResponseEntity<?> forgotPassword(String email) {

        Optional<UserModel> userOptional = userRepository.findByEmailAddress(email);

        if (userOptional.isEmpty()) {
            throw new IllegalStateException("Email not found. Please provide a valid email address.");
        }
        UserModel user = userOptional.get();
        String token = tokenHandle.createPasswordResetToken(user);
        return ResponseEntity.ok(mapper.mapToUserResponse(user));
    }











}
