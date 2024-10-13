package com.nguyenminh.microservices.zwallet.service;

import com.nguyenminh.microservices.zwallet.dto.TransactionHistoryResponse;
import com.nguyenminh.microservices.zwallet.dto.UserResponse;
import com.nguyenminh.microservices.zwallet.model.TransactionHistory;
import com.nguyenminh.microservices.zwallet.model.UserModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class Mapper {

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

    public TransactionHistoryResponse mapToTransactionResponse(TransactionHistory transactionHistory) {
        // Check if transactionHistory is null and return a default response
        if (transactionHistory == null) {
            return TransactionHistoryResponse.builder()
                    .transactionId(null)          // Or some default value
                    .amountUsed("0")             // Or some default value
                    .purpose("N/A")              // Or some default value
                    .moneyLeft("0")              // Or some default value
                    .userId(null)                // Or some default value
                    .build();
        }

        // Safely map fields from TransactionHistory to TransactionHistoryResponse
        return TransactionHistoryResponse.builder()
                .transactionId(transactionHistory.getId())
                .amountUsed(transactionHistory.getAmountUsed())
                .category(transactionHistory.getCategory())
                .createdAt(transactionHistory.getCreatedAt())
                .purpose(transactionHistory.getPurpose())
                .moneyLeft(transactionHistory.getMoneyLeft())
                .userId(transactionHistory.getUser() != null ? transactionHistory.getUser().getId() : null) // Handle potential null user
                .build();
    }
}
