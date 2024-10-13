package com.nguyenminh.microservices.zwallet.service;

import com.nguyenminh.microservices.zwallet.dto.TransactionHistoryResponse;
import com.nguyenminh.microservices.zwallet.dto.UserResponse;
import com.nguyenminh.microservices.zwallet.model.PaginatedResponse;
import com.nguyenminh.microservices.zwallet.model.TransactionHistory;
import com.nguyenminh.microservices.zwallet.model.UserModel;
import com.nguyenminh.microservices.zwallet.repository.TransactionHistoryRepository;
import com.nguyenminh.microservices.zwallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TransactionHistoryService {

    private final TransactionHistoryRepository transactionHistoryRepository;
    private final UserTransactionService userTransactionService;
    private final PaginationService paginationService;

    private final Mapper mapper;

    public List<TransactionHistoryResponse> getAllTransactionHistory() {
        List<TransactionHistory> transactionHistory = transactionHistoryRepository.findAll();
        return transactionHistory.stream().map(mapper::mapToTransactionResponse).toList();
    }

    public UserResponse createTransactionHistory(String name, TransactionHistory transactionHistory1) {

       return mapper.mapToUserResponse(
               userTransactionService.updateTransactionHistory(name,transactionHistory1)
       );
    }


    public PaginatedResponse<TransactionHistoryResponse> getTransactionHistoryPagination(int page, int size, String sort, String userName) throws UnsupportedEncodingException {

        return paginationService.getTransactionHistoryPagination(page,size,sort,userName);


    }



}
