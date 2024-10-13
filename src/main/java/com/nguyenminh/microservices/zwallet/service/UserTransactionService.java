package com.nguyenminh.microservices.zwallet.service;

import com.nguyenminh.microservices.zwallet.dto.TransactionHistoryResponse;
import com.nguyenminh.microservices.zwallet.dto.UserResponse;
import com.nguyenminh.microservices.zwallet.model.TransactionHistory;
import com.nguyenminh.microservices.zwallet.model.UserModel;
import com.nguyenminh.microservices.zwallet.repository.TransactionHistoryRepository;
import com.nguyenminh.microservices.zwallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserTransactionService {

    private final UserModelService userModelService;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final UserRepository userRepository;

    public UserModel updateTransactionHistory(String name, TransactionHistory transactionHistory){

        UserModel userResponse = userRepository.findByUserName(name);

        if(userResponse == null){
            throw new RuntimeException("Can't find user");
        }
        List<TransactionHistory> transactionHistoryList = userResponse.getTransactionHistory();
        if (transactionHistoryList == null) {
            transactionHistoryList = new ArrayList<>();
        }
        transactionHistory.setUser(userResponse);
        transactionHistoryRepository.save(transactionHistory);
        transactionHistoryList.add(transactionHistory);
        return userRepository.save(userResponse);

    }
}
