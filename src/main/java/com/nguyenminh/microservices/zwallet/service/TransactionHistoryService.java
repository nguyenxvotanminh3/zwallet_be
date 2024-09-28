package com.nguyenminh.microservices.zwallet.service;

import com.nguyenminh.microservices.zwallet.dto.TransactionHistoryResponse;
import com.nguyenminh.microservices.zwallet.dto.UserResponse;
import com.nguyenminh.microservices.zwallet.model.PaginatedResponse;
import com.nguyenminh.microservices.zwallet.model.TransactionHistory;
import com.nguyenminh.microservices.zwallet.model.UserModel;
import com.nguyenminh.microservices.zwallet.repository.TransactionHistoryRepository;
import com.nguyenminh.microservices.zwallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TransactionHistoryService {
    @Autowired
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final UserRepository userRepository;

    public List<TransactionHistoryResponse> getAllTransactionHistory() {
        List<TransactionHistory> transactionHistory = transactionHistoryRepository.findAll();
        return transactionHistory.stream().map(this::mapToTransactionResponse).toList();
    }

    public UserResponse createTransactionHistory(String name, TransactionHistory transactionHistory1) {
        UserModel userModel = userRepository.findByUserName(name);
        if (userModel != null) {
            transactionHistory1.setUser(userModel);
            transactionHistoryRepository.save(transactionHistory1);
            List<TransactionHistory> transactionHistories = userModel.getTransactionHistory();
            if (transactionHistories == null) {
                transactionHistories = new ArrayList<>();
            }
            transactionHistories.add(transactionHistory1);
            userModel.setTransactionHistory(transactionHistories);
            userRepository.save(userModel);
            return mapToUserResponse(userModel);
        } else {
            throw new RuntimeException("No user found");
        }
    }


    public UserResponse mapToUserResponse(UserModel userModel) {
        List<TransactionHistoryResponse> transactionHistoryResponses =
                userModel.getTransactionHistory() != null ?
                        userModel.getTransactionHistory().stream()
                                .map(this::mapToTransactionResponse)
                                .toList() :
                        new ArrayList<>();

        return UserResponse.builder()
                .userId(userModel.getId())
                .userName(userModel.getUserName())
                .emailAddress(userModel.getEmailAddress())
                .password(userModel.getPassword())
                .fullName(userModel.getFullName())
                .address(userModel.getAddress())
                .city(userModel.getCity())
                .country(userModel.getCountry())
                .postalCode(userModel.getPostalCode())
                .aboutMe(userModel.getAboutMe())
                .quotes(userModel.getQuotes())
                .tag(userModel.getTag())
                .totalAmount(userModel.getTotalAmount())
                .transactionHistoryResponses(transactionHistoryResponses)
                .build();
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


    public PaginatedResponse<TransactionHistoryResponse> getTransactionHistoryPagination(int page, int size, String userName) throws UnsupportedEncodingException {
        // Tạo PageRequest để phân trang (không cần thiết nếu bạn không sử dụng Spring Data Page)
        PageRequest pageRequest = PageRequest.of(page, size);

        // Tìm kiếm người dùng theo userName
        UserModel userModel = userRepository.findByUserName(userName);
        if (userModel == null) {
            throw new RuntimeException("Can't find user");
        }

        // Lấy danh sách giao dịch của người dùng
        List<TransactionHistory> transactionHistories = userModel.getTransactionHistory();

        // Tổng số giao dịch
        int totalElements = transactionHistories.size();

        // Tính toán số trang
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Tính toán vị trí bắt đầu và kết thúc cho trang yêu cầu
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);

        // Kiểm tra nếu trang yêu cầu vượt quá số lượng giao dịch
        if (startIndex >= totalElements) {
            return new PaginatedResponse<>(page, size, totalPages, totalElements, new ArrayList<>());
        }

        // Lấy danh sách giao dịch trong trang hiện tại
        List<TransactionHistory> paginatedTransactions = transactionHistories.subList(startIndex, endIndex);

        // Ánh xạ từ TransactionHistory sang TransactionHistoryResponse
        List<TransactionHistoryResponse> transactionResponses = paginatedTransactions.stream()
                .map(this::mapToTransactionResponse) // Ánh xạ từng TransactionHistory thành TransactionResponse
                .collect(Collectors.toList());

        // Trả về đối tượng PaginatedResponse
        return new PaginatedResponse<>(
                page,              // Trang hiện tại
                size,              // Kích thước trang
                totalPages,        // Tổng số trang
                totalElements,     // Tổng số giao dịch
                transactionResponses // Danh sách giao dịch trong trang hiện tại
        );
    }
}
