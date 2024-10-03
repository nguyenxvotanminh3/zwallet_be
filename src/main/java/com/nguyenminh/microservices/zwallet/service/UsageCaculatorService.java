package com.nguyenminh.microservices.zwallet.service;

import com.nguyenminh.microservices.zwallet.dto.TransactionHistoryResponse;
import com.nguyenminh.microservices.zwallet.model.PercentUsage;
import com.nguyenminh.microservices.zwallet.model.PercentUsageTotal;
import com.nguyenminh.microservices.zwallet.model.TransactionHistory;
import com.nguyenminh.microservices.zwallet.model.UserModel;
import com.nguyenminh.microservices.zwallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsageCaculatorService {
    private final UserRepository userRepository;
    private final TransactionHistoryService transactionHistoryService;
    public TransactionHistoryResponse mapToTransactionResponse(TransactionHistory transactionHistory) {
        return TransactionHistoryResponse.builder()
                .transactionId(transactionHistory.getId())
                .amountUsed(transactionHistory.getAmountUsed())
                .purpose(transactionHistory.getPurpose())
                .moneyLeft(transactionHistory.getMoneyLeft())
                .userId(transactionHistory.getUser().getId())
                .build();
    }

    public ResponseEntity<?> usageCaculate(String username){
        AtomicInteger food = new AtomicInteger();
        AtomicInteger bill = new AtomicInteger();
        AtomicInteger entertain = new AtomicInteger();
        AtomicInteger shopping = new AtomicInteger();
        AtomicInteger investment = new AtomicInteger();


        UserModel userModel = userRepository.findByUserName(username);

        if(userModel == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Can't find user with user name: "+ username);
        }
        List<TransactionHistory> transactionHistories = userModel.getTransactionHistory();

        List<TransactionHistoryResponse> transactionResponses = transactionHistories.stream()
                .map(this::mapToTransactionResponse) // Ánh xạ từng TransactionHistory thành TransactionResponse
                .toList();

        AtomicInteger totalTransaction = new AtomicInteger(userModel.getTransactionHistory().size());


        transactionResponses.forEach(
                transactionHistoryResponse -> {
               if(transactionHistoryResponse.getPurpose().equals("Food")){
                   food.getAndIncrement();

               }
               if(transactionHistoryResponse.getPurpose().equals("Bill")){
                   bill.getAndIncrement();
                    }
               if(transactionHistoryResponse.getPurpose().equals("Entertain")){
                   entertain.getAndIncrement();
                    }
               if(transactionHistoryResponse.getPurpose().equals("Shopping")){
                   shopping.getAndIncrement();
                    }
               if(transactionHistoryResponse.getPurpose().equals("Investment")){
                   investment.getAndIncrement();
                    }
               if(transactionHistoryResponse.getPurpose().equals("Add money to wallet")){
                        totalTransaction.getAndDecrement();
                    }
                });
        totalTransaction.getAndDecrement();
        log.info("The total: " + totalTransaction);
        log.info("Food " + food);
        log.info("Bill " + bill);
        log.info("Entertain " + entertain);
        log.info("Shopping " + shopping);
        log.info("Investment " + investment);
        PercentUsage percentUsage = new PercentUsage();
        percentUsage.setFood((float) ((food.get()) * 100L) / totalTransaction.get());
        percentUsage.setBill((float) ((bill.get()) * 100L / totalTransaction.get()));
        percentUsage.setEntertain((float) ((entertain.get()) * 100L / totalTransaction.get()));
        percentUsage.setShopping((float) ((shopping.get()) * 100L / totalTransaction.get()));
        percentUsage.setInvestment((float) ((investment.get()) * 100L / totalTransaction.get()));

        return ResponseEntity.ok(percentUsage);

    }

    public ResponseEntity<?> usageCaculateTotal(String username){
        AtomicInteger food = new AtomicInteger(0);
        AtomicInteger bill = new AtomicInteger(0);
        AtomicInteger entertain = new AtomicInteger(0);
        AtomicInteger shopping = new AtomicInteger(0);
        AtomicInteger investment = new AtomicInteger(0);


        UserModel userModel = userRepository.findByUserName(username);
        if(userModel == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Can't find user with user name: "+ username);
        }
        List<TransactionHistory> transactionHistories = userModel.getTransactionHistory();

        List<TransactionHistoryResponse> transactionResponses = transactionHistories.stream()
                .map(this::mapToTransactionResponse) // Ánh xạ từng TransactionHistory thành TransactionResponse
                .toList();

        transactionResponses.forEach(
                transactionHistoryResponse -> {
                    log.info("tag "+transactionHistoryResponse.getPurpose());
                    if(transactionHistoryResponse.getPurpose().equals("Food")){
                        food.getAndSet( food.get() +Integer.parseInt(transactionHistoryResponse.getAmountUsed()));

                        log.info("food " + food.get());
                    }
                    if(transactionHistoryResponse.getPurpose().equals("Bill")){
                        bill.getAndSet( (bill.get() +Integer.parseInt(transactionHistoryResponse.getAmountUsed())));
                        log.info("bill " + bill.get());
                    }
                    if(transactionHistoryResponse.getPurpose().equals("Entertain")){
                        entertain.getAndSet( (entertain.get() +Integer.parseInt(transactionHistoryResponse.getAmountUsed())));
                        log.info("entertain " + entertain.get());
                    }
                    if(transactionHistoryResponse.getPurpose().equals("Shopping")){
                        shopping.getAndSet( (shopping.get() +Integer.parseInt(transactionHistoryResponse.getAmountUsed())));
                        log.info("shopping " + shopping.get());
                    }
                    if(transactionHistoryResponse.getPurpose().equals("Investment")){
                        investment.getAndSet( (investment.get() +Integer.parseInt(transactionHistoryResponse.getAmountUsed())));
                        log.info("investment " + investment.get());
                    }
                });

        PercentUsageTotal percentUsage = new PercentUsageTotal();
        percentUsage.setFood(BigDecimal.valueOf((int) food.get()));
        percentUsage.setBill(BigDecimal.valueOf((int) bill.get()));
        percentUsage.setEntertain(BigDecimal.valueOf((int) entertain.get()));
        percentUsage.setShopping(BigDecimal.valueOf((int) shopping.get()));
        percentUsage.setInvestment(BigDecimal.valueOf((int) investment.get()));

        return ResponseEntity.ok(percentUsage);

    }

}
