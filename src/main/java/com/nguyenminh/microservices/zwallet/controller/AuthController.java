package com.nguyenminh.microservices.zwallet.controller;
import com.nguyenminh.microservices.zwallet.configuration.AppConfiguration;
import com.nguyenminh.microservices.zwallet.configuration.JwtResponse;
import com.nguyenminh.microservices.zwallet.configuration.JwtUtils;
import com.nguyenminh.microservices.zwallet.dto.ChangePasswordRequest;
import com.nguyenminh.microservices.zwallet.dto.LoginRequest;
import com.nguyenminh.microservices.zwallet.dto.UserResponse;
import com.nguyenminh.microservices.zwallet.model.PasswordResetToken;
import com.nguyenminh.microservices.zwallet.model.UserModel;
import com.nguyenminh.microservices.zwallet.repository.PasswordResetTokenRepository;
import com.nguyenminh.microservices.zwallet.repository.UserRepository;
import com.nguyenminh.microservices.zwallet.service.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final AppConfiguration appConfiguration;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserModelService userModelService;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final SecurityService securityService;
    private final Mapper mapper;
    private final EncryptPasswordSerivce encryptPasswordSerivce;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUserName(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Lấy thông tin UserDetails từ authentication
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Tạo JWT
            String jwt = jwtUtils.generateJwtToken(userDetails);
            String username = userDetails.getUsername();
            String roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));
            UserModel userModel =  userRepository.findByUserName(username);
            userRepository.save(userModel);
            return ResponseEntity.ok(new JwtResponse(jwt, username, roles)); // Trả về JWT trong response
        } catch (AuthenticationException e) {
            log.error("Authentication failed: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }
    @PostMapping("/login/success")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> loginSuccess() {
        return ResponseEntity.ok("Login successful!");
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);



            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("Invalid or missing JWT token");
        }
    }
    @PostMapping("/forgot-password")
    @CrossOrigin("*")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        log.info(request.get("email"));
        String email = request.get("email");
        ResponseEntity<?> response = securityService.forgotPassword(email);
        return response;
    }

    @PostMapping("/reset-password")
    @CrossOrigin("*")

    public ResponseEntity<?> resetPassword(@RequestParam("t") String t, @RequestBody HashMap<String , String> pass) {
        // Kiểm tra token có tồn tại và còn hiệu lực không
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(t);
        log.info("token found : " + passwordResetToken);
        if (passwordResetToken == null || passwordResetToken.getExpirationTime().isBefore(LocalDateTime.now())) {
            log.info("token"+ t);
            return ResponseEntity.badRequest().body("Token is invalid or expired");
        }

        // Tìm người dùng từ token
        UserModel user = userRepository.findById(passwordResetToken.getUserId()).orElse(null);
        assert user != null;
        UserResponse userResponse = mapper.mapToUserResponse(user);
        log.info("found user : " + userResponse);
        log.info("raw pass : " + pass);
        user.setPassword(encryptPasswordSerivce.encryptPassword(pass.get("newpass")));
        log.info("change password " + user.getPassword());
        userRepository.save(user);

        // Xóa token sau khi sử dụng
        passwordResetTokenRepository.deleteAllById(Collections.singleton(user.getId()));

        return ResponseEntity.ok("Password successfully reset");
    }



    @PutMapping("/user/change-pass")
    public ResponseEntity<?> changPassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(changePasswordRequest.getUserName());
            if (passwordEncoder.matches(changePasswordRequest.getOldPass(), userDetails.getPassword())) {
                return securityService.ChangePassword(changePasswordRequest.getUserName(),changePasswordRequest.getNewPass());
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Password not match");
            }
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }


}