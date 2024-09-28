package com.nguyenminh.microservices.zwallet.controller;
import com.nguyenminh.microservices.zwallet.configuration.AppConfiguration;
import com.nguyenminh.microservices.zwallet.configuration.JwtResponse;
import com.nguyenminh.microservices.zwallet.configuration.JwtUtils;
import com.nguyenminh.microservices.zwallet.dto.LoginRequest;
import com.nguyenminh.microservices.zwallet.dto.UserResponse;
import com.nguyenminh.microservices.zwallet.model.PasswordResetToken;
import com.nguyenminh.microservices.zwallet.model.UserModel;
import com.nguyenminh.microservices.zwallet.repository.PasswordResetTokenRepository;
import com.nguyenminh.microservices.zwallet.repository.UserRepository;
import com.nguyenminh.microservices.zwallet.service.UserModelService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class AuthController {
    @Autowired // Field injection for JwtUtils
    private JwtUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private final AppConfiguration appConfiguration;

    @Autowired
    private UserModelService userModelService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @PostMapping("/login")
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
    public ResponseEntity<?> loginSuccess() {
        return ResponseEntity.ok("Login successful!");
    }

    @PostMapping("/logout")
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
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        log.info(request.get("email"));
        String email = request.get("email");
        String response = userModelService.forgotPassword(email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("t") String t, @RequestBody String pass) {
        // Kiểm tra token có tồn tại và còn hiệu lực không
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(t);
        log.info("token found : " + passwordResetToken);
        if (passwordResetToken == null || passwordResetToken.getExpirationTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token is invalid or expired");
        }

        // Tìm người dùng từ token
        UserModel user = userRepository.findById(passwordResetToken.getUserId()).orElse(null);
        log.info("found user : " + user);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        log.info("raw pass : " + pass);
        // Cập nhật mật khẩu mới và mã hóa mật khẩu
        user.setPassword(appConfiguration.passwordEncoder().encode(pass));
        log.info("change password " + user.getPassword());
        userRepository.save(user);

        // Xóa token sau khi sử dụng
        passwordResetTokenRepository.deleteAllById(Collections.singleton(user.getId()));

        return ResponseEntity.ok("Password successfully reset");
    }


}