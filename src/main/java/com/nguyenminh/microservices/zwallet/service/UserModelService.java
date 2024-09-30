package com.nguyenminh.microservices.zwallet.service;

import com.nguyenminh.microservices.zwallet.configuration.AppConfiguration;
import com.nguyenminh.microservices.zwallet.dto.TagAndQuotesRequest;
import com.nguyenminh.microservices.zwallet.dto.TransactionHistoryResponse;
import com.nguyenminh.microservices.zwallet.dto.UserRegistrationDto;
import com.nguyenminh.microservices.zwallet.dto.UserResponse;
import com.nguyenminh.microservices.zwallet.model.PasswordResetToken;
import com.nguyenminh.microservices.zwallet.model.TransactionHistory;
import com.nguyenminh.microservices.zwallet.model.UserModel;
import com.nguyenminh.microservices.zwallet.repository.PasswordResetTokenRepository;
import com.nguyenminh.microservices.zwallet.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserModelService {
    private final Path uploadDir = Paths.get("uploads");
    private final UserRepository userRepository;
    private final AppConfiguration appConfiguration;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;
    private final HttpServletRequest request;
    private final CloudinaryService cloudinaryService;

    public List<UserResponse> getAllUser() {
        List<UserModel> userModels = userRepository.findAll();
        return userModels.stream().map(this::mapToUserResponse).toList();
    }
    public ResponseEntity<?> ChangePassword(String userName, String pass) {
        // Tìm người dùng từ token
        UserModel user = userRepository.findByUserName(userName);
        // Cập nhật mật khẩu mới và mã hóa mật khẩu
        user.setPassword(appConfiguration.passwordEncoder().encode(pass));
        log.info("change password " + user.getPassword());
        userRepository.save(user);
        return ResponseEntity.ok("Password successfully change");
    }
    public Optional<UserResponse> getUserById(String id) {
        Optional<UserModel> userModel = userRepository.findById(id);
        if (userModel.isPresent()) {
            return userModel.map(this::mapToUserResponse);
        } else {
            throw new RuntimeException("Cant find user with id: " + id);
        }
    }
    public UserResponse updateUserImage(String name, MultipartFile file) {
        String imageData = this.cloudinaryService.upload(file);
        UserModel userModel = userRepository.findByUserName(name);
        if (userModel != null) {
            userModel.setProfileImage(imageData);
            userRepository.save(userModel);
            UserResponse userResponse = mapToUserResponse(userModel);
            return userResponse;
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public UserResponse updateUserDetail(String name, UserModel userModel2) {
        UserModel userModel = userRepository.findByUserName(name);

        if (userModel != null) {
            userModel.setUserName(userModel2.getUserName());
            userModel.setCompany(userModel2.getCompany());
            userModel.setCity(userModel2.getCity());
            userModel.setAddress(userModel2.getAddress());
            userModel.setCountry(userModel2.getCountry());
            userModel.setTag(userModel2.getTag());
            userModel.setAboutMe(userModel2.getAboutMe());
            userModel.setQuotes(userModel2.getQuotes());
            userModel.setPostalCode(userModel2.getPostalCode());
            userModel.setEmailAddress(userModel2.getEmailAddress());
            userModel.setFullName(userModel2.getFullName());
            userRepository.save(userModel);
            return mapToUserResponse(userModel);
        } else {
            throw new RuntimeException("Cant find user");
        }
    }

    public UserModel createUser(UserRegistrationDto userRegistrationDto) {
        UserModel userModel = new UserModel();
        userModel.setFullName(userRegistrationDto.getFullName());
        userModel.setEmailAddress(userRegistrationDto.getEmailAddress());
        userModel.setUserName(userRegistrationDto.getUserName());
        userModel.setPassword(appConfiguration.passwordEncoder().encode(userRegistrationDto.getPassword()));
        userModel.setCity(null);
        userModel.setAddress(null);
        userModel.setCountry(null);
        userModel.setTag(null);
        userModel.setAboutMe(null);
        userModel.setQuotes("Click here to edit the quotes yourself !");
        userModel.setTag(null);
        userModel.setPostalCode(null);
        userModel.setCompany(null);
        userModel.setTotalAmount("0");
        if (userRepository.findByUserName(userRegistrationDto.getUserName()) != null) {
            throw new RuntimeException("This user name has been used!");
        } else {
            userRepository.save(userModel);
            return userModel;
        }
    }

    public TransactionHistoryResponse mapToTransactionResponse(TransactionHistory transactionHistory) {
        return TransactionHistoryResponse.builder()
                .transactionId(transactionHistory.getId())
                .amountUsed(transactionHistory.getAmountUsed())
                .localDateTime(transactionHistory.getLocalDateTime())
                .purpose(transactionHistory.getPurpose())
                .moneyLeft(transactionHistory.getMoneyLeft())
                .userId(transactionHistory.getUser().getId())
                .build();
    }

    public UserResponse mapToUserResponse(UserModel userModel) {
        List<TransactionHistoryResponse> transactionHistoryResponses = userModel.getTransactionHistory() != null
                ? userModel.getTransactionHistory().stream()
                .map(this::mapToTransactionResponse)
                .toList()
                : Collections.emptyList();

        return UserResponse.builder()
                .userId(userModel.getId())
                .company(userModel.getCompany())
                .password(userModel.getPassword())
                .userName(userModel.getUserName())
                .emailAddress(userModel.getEmailAddress())
                .fullName(userModel.getFullName())
                .address(userModel.getAddress())
                .city(userModel.getCity())
                .country(userModel.getCountry())
                .profileImage(userModel.getProfileImage())
                .postalCode(userModel.getPostalCode())
                .aboutMe(userModel.getAboutMe())
                .quotes(userModel.getQuotes())
                .tag(userModel.getTag())
                .totalAmount(userModel.getTotalAmount())
                .transactionHistoryResponses(transactionHistoryResponses)
                .build();
    }


    public boolean checkUserName(String name) {
        log.info("USER NAME : " + name);


        return userRepository.findByUserName(name) != null;
    }


    @Transactional
    public UserResponse getUserByUserName(String userName) {
        UserModel userModel = userRepository.findByUserName(userName);

        if (userModel != null) {
            // Handle the case where transactionHistories might be null
            List<TransactionHistory> transactionHistories = userModel.getTransactionHistory();
            List<TransactionHistoryResponse> transactionHistoryResponses = (transactionHistories != null)
                    ? transactionHistories.stream()
                    .map(this::mapToTransactionResponse)
                    .toList()
                    : Collections.emptyList();
            log.info("image " + userModel.getProfileImage());
            return UserResponse.builder()
                    .userId(userModel.getId())
                    .company(userModel.getCompany())
                    .password(userModel.getPassword())
                    .quotes(userModel.getQuotes())
                    .country(userModel.getCountry())
                    .postalCode(userModel.getPostalCode())
                    .userName(userModel.getUserName())
                    .city(userModel.getCity())
                    .address(userModel.getAddress())
                    .aboutMe(userModel.getAboutMe())
                    .tag(userModel.getTag())
                    .profileImage(userModel.getProfileImage())
                    .emailAddress(userModel.getEmailAddress())
                    .fullName(userModel.getFullName())
                    .totalAmount(userModel.getTotalAmount())
                    // .profileImage(userModel.getProfileImage()) // Uncomment if needed
                    .quotes(userModel.getQuotes())
                    .transactionHistoryResponses(transactionHistoryResponses)
                    .build();
        } else {
            return null;
        }
    }

    public String deleteUserByName(String userName) {
        UserModel userModel = userRepository.findByUserName(userName);
        if (userModel != null) {
            userRepository.deleteById(String.valueOf(Integer.valueOf(userModel.getId())));
            return "Deleted user: " + userName;
        } else return "Cant find user: " + userName;
    }
    public UserResponse updateQuotesAndTag(String userName, TagAndQuotesRequest tagAndQuotesRequest) {
        UserModel userModel = userRepository.findByUserName(userName);

        if (userModel != null) {
            log.info("found user " + userModel.getUserName() );
           userModel.setTag(tagAndQuotesRequest.getTag());
           userModel.setQuotes(tagAndQuotesRequest.getQuotes());
           userRepository.save(userModel);

        } else throw new RuntimeException("Cant find user " + userName);

        return mapToUserResponse(userModel);
    }


    public UserResponse updateUserTotal(String name, String totalAmount) {
        UserModel userModel = userRepository.findByUserName(name);
        if (userModel != null) {
            userModel.setTotalAmount(totalAmount);
            userRepository.save(userModel);
        } else {
            throw new RuntimeException("Cant find user");
        }
        return mapToUserResponse(userModel);
    }



    public String forgotPassword(String email) {

        Optional<UserModel> userOptional = userRepository.findByEmailAddress(email);

        if (userOptional.isEmpty()) {
            throw new IllegalStateException("Email không tồn tại");
        }

        UserModel user = userOptional.get();
        log.info("Found user" + user.getEmailAddress());

        String token = createPasswordResetToken(user);

        return "Đã gửi token reset password đến email của bạn";
    }
    public void sendEmail(String to, String token) {
        String baseUrl = request.getRequestURL().toString()
                .replace(request.getRequestURI(), "");
        log.info("called mail sender");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("worknguyenvotanminh@gmail.com");
        message.setTo(to);
        log.info("send to : " + to);
        message.setSubject("Reset Your Password");
//        message.setText("Click the link to reset your password: " +
//                baseUrl + "/auth/reset-password?token=" + token);
        message.setText("This is your token: " + token + "\n" +
                "Click the link below to change your password:\n" +
                "https://thezwallet.netlify.app/zwallet/pages/forgotpassword");

        mailSender.send(message);
    }

    public String createPasswordResetToken(UserModel user) {
        SecureRandom secureRandom = new SecureRandom();
        int randomNumber = 100000 + secureRandom.nextInt(900000);
        String token = String.valueOf(randomNumber);
//        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(
                token,
                user.getId(),
                LocalDateTime.now().plusMinutes(5)
        );
        passwordResetTokenRepository.save(passwordResetToken);
        log.info("user email : " + user.getEmailAddress());
        log.info("Created token :" + passwordResetToken);

        sendEmail(user.getEmailAddress().toString(), token);

        log.info("Send to email : " + user.getEmailAddress());

        return token;
    }
}
