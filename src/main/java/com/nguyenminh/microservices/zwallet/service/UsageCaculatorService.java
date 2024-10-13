package com.nguyenminh.microservices.zwallet.service;

import com.nguyenminh.microservices.zwallet.dto.MoneyPredict;
import com.nguyenminh.microservices.zwallet.dto.TransactionHistoryResponse;
import com.nguyenminh.microservices.zwallet.dto.UserResponse;
import com.nguyenminh.microservices.zwallet.model.PercentUsage;
import com.nguyenminh.microservices.zwallet.model.PercentUsageTotal;
import com.nguyenminh.microservices.zwallet.model.TransactionHistory;
import com.nguyenminh.microservices.zwallet.model.UserModel;
import com.nguyenminh.microservices.zwallet.repository.MoneyPredictRepository;
import com.nguyenminh.microservices.zwallet.repository.UserRepository;
import jdk.jfr.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsageCaculatorService {

    private final UserRepository userRepository;
    private final MoneyPredictRepository moneyPredictRepository;
    private final Mapper mapper;

    public ResponseEntity<?> usageCaculate(String username){
        AtomicInteger food = new AtomicInteger(0);
        AtomicInteger bill = new AtomicInteger(0);
        AtomicInteger entertain = new AtomicInteger(0);
        AtomicInteger shopping = new AtomicInteger(0);
        AtomicInteger investment = new AtomicInteger(0);
        AtomicInteger medicine = new AtomicInteger(0);
        AtomicInteger education = new AtomicInteger(0);
        AtomicInteger travel = new AtomicInteger(0);
        AtomicInteger rent = new AtomicInteger(0);
        AtomicInteger transportation = new AtomicInteger(0);
        AtomicInteger utilities = new AtomicInteger(0);
        AtomicInteger savings = new AtomicInteger(0);
        AtomicInteger charity = new AtomicInteger(0);
        AtomicInteger insurance = new AtomicInteger(0);
        AtomicInteger gifts = new AtomicInteger(0);
        AtomicInteger others = new AtomicInteger(0);
        AtomicInteger recive = new AtomicInteger(0);
        AtomicInteger transfer = new AtomicInteger(0);

        UserModel userModel = userRepository.findByUserName(username);

        if(userModel == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Can't find user with user name: "+ username);
        }

        List<TransactionHistory> transactionHistories = userModel.getTransactionHistory();


        List<TransactionHistoryResponse> transactionResponses = transactionHistories.stream()
                .map(mapper::mapToTransactionResponse)
                .toList();


        AtomicInteger totalTransaction = new AtomicInteger(userModel.getTransactionHistory().size());

        transactionResponses.forEach(transactionHistoryResponse -> {
            String category = transactionHistoryResponse.getCategory();

            if (category != null) {
                log.info("Category: {}", category);
                switch (category) {
                    case "Food" -> food.getAndIncrement();
                    case "Bill" -> bill.getAndIncrement();
                    case "Entertain" -> entertain.getAndIncrement();
                    case "Shopping" -> shopping.getAndIncrement();
                    case "Investment" -> investment.getAndIncrement();
                    case "Medicine" -> medicine.getAndIncrement();
                    case "Education" -> education.getAndIncrement();
                    case "Travel" -> travel.getAndIncrement();
                    case "Rent" -> rent.getAndIncrement();
                    case "Transportation" -> transportation.getAndIncrement();
                    case "Utilities" -> utilities.getAndIncrement();
                    case "Savings" -> savings.getAndIncrement();
                    case "Charity" -> charity.getAndIncrement();
                    case "Insurance" -> insurance.getAndIncrement();
                    case "Gifts" -> gifts.getAndIncrement();
                    case "Others" -> others.getAndIncrement();
                    case "Recive money" -> recive.getAndIncrement();
                    case "Transfer money" -> transfer.getAndIncrement();
                    default -> totalTransaction.getAndDecrement(); // Invalid or empty category
                }

            }
        });

        log.info("The total: " + totalTransaction);
        log.info("Food: " + food);
        log.info("Bill: " + bill);
        log.info("Entertain: " + entertain);
        log.info("Shopping: " + shopping);
        log.info("Investment: " + investment);
        log.info("Medicine: " + medicine);
        log.info("Education: " + education);
        log.info("Travel: " + travel);
        log.info("Rent: " + rent);
        log.info("Transportation: " + transportation);
        log.info("Utilities: " + utilities);
        log.info("Savings: " + savings);
        log.info("Charity: " + charity);
        log.info("Insurance: " + insurance);
        log.info("Recive" + recive);
        log.info("Transfer" + transfer);
        log.info("Gifts: " + gifts);
        log.info("Others: " + others);

        PercentUsage percentUsage = new PercentUsage();
        percentUsage.setFood((float) (food.get() * 100L / totalTransaction.get()));
        percentUsage.setBill((float) (bill.get() * 100L / totalTransaction.get()));
        percentUsage.setEntertain((float) (entertain.get() * 100L / totalTransaction.get()));
        percentUsage.setShopping((float) (shopping.get() * 100L / totalTransaction.get()));
        percentUsage.setInvestment((float) (investment.get() * 100L / totalTransaction.get()));
        percentUsage.setMedicine((float) (medicine.get() * 100L / totalTransaction.get()));
        percentUsage.setEducation((float) (education.get() * 100L / totalTransaction.get()));
        percentUsage.setTravel((float) (travel.get() * 100L / totalTransaction.get()));
        percentUsage.setRent((float) (rent.get() * 100L / totalTransaction.get()));
        percentUsage.setTransportation((float) (transportation.get() * 100L / totalTransaction.get()));
        percentUsage.setUtilities((float) (utilities.get() * 100L / totalTransaction.get()));
        percentUsage.setSavings((float) (savings.get() * 100L / totalTransaction.get()));
        percentUsage.setCharity((float) (charity.get() * 100L / totalTransaction.get()));
        percentUsage.setInsurance((float) (insurance.get() * 100L / totalTransaction.get()));
        percentUsage.setRecive((float) (recive.get() * 100L / totalTransaction.get()));
        percentUsage.setTransfer((float) (transfer.get() * 100L / totalTransaction.get()));
        percentUsage.setGifts((float) (gifts.get() * 100L / totalTransaction.get()));
        percentUsage.setOthers((float) (others.get() * 100L / totalTransaction.get()));

        return ResponseEntity.ok(percentUsage);
    }


    public ResponseEntity<?> usageCaculateTotal(String username){
        // Initialize categories with default values
        AtomicInteger food = new AtomicInteger(0);
        AtomicInteger bill = new AtomicInteger(0);
        AtomicInteger entertain = new AtomicInteger(0);
        AtomicInteger shopping = new AtomicInteger(0);
        AtomicInteger investment = new AtomicInteger(0);
        AtomicInteger medicine = new AtomicInteger(0);
        AtomicInteger education = new AtomicInteger(0);
        AtomicInteger travel = new AtomicInteger(0);
        AtomicInteger rent = new AtomicInteger(0);
        AtomicInteger transportation = new AtomicInteger(0);
        AtomicInteger utilities = new AtomicInteger(0);
        AtomicInteger savings = new AtomicInteger(0);
        AtomicInteger charity = new AtomicInteger(0);
        AtomicInteger insurance = new AtomicInteger(0);
        AtomicInteger gifts = new AtomicInteger(0);
        AtomicInteger others = new AtomicInteger(0);
        AtomicInteger recive = new AtomicInteger(0);
        AtomicInteger transfer = new AtomicInteger(0);
        // Find user by username
        UserModel userModel = userRepository.findByUserName(username);
        if (userModel == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Can't find user with user name: " + username);
        }

        List<TransactionHistory> transactionHistories = userModel.getTransactionHistory();



        // Map transactions
        List<TransactionHistoryResponse> transactionResponses = transactionHistories.stream()
                .map(mapper::mapToTransactionResponse)
                .toList();

        log.info("Transaction" + transactionResponses);

        // Calculate totals for each category
        transactionResponses.forEach(transactionHistoryResponse -> {
            String category = transactionHistoryResponse.getCategory();
            int amount = Integer.parseInt(transactionHistoryResponse.getAmountUsed());

            switch (category) {
                case "Food":
                    food.getAndSet(Integer.parseInt(transactionHistoryResponse.getAmountUsed()) + food.get());
                    break;
                case "Bill":
                    bill.addAndGet(Integer.parseInt(transactionHistoryResponse.getAmountUsed()) + bill.get());
                    break;
                case "Entertain":
                    entertain.addAndGet(Integer.parseInt(transactionHistoryResponse.getAmountUsed()) + entertain.get());
                    break;
                case "Shopping":
                    shopping.addAndGet(Integer.parseInt(transactionHistoryResponse.getAmountUsed()) + shopping.get());
                    break;
                case "Investment":
                    investment.addAndGet(Integer.parseInt(transactionHistoryResponse.getAmountUsed()) + investment.get());
                    break;
                case "Medicine":
                    medicine.addAndGet(Integer.parseInt(transactionHistoryResponse.getAmountUsed()) + medicine.get());
                    break;
                case "Education":
                    education.addAndGet(Integer.parseInt(transactionHistoryResponse.getAmountUsed()) + education.get());
                    break;
                case "Travel":
                    travel.addAndGet(Integer.parseInt(transactionHistoryResponse.getAmountUsed()) + travel.get());
                    break;
                case "Rent":
                    rent.addAndGet(Integer.parseInt(transactionHistoryResponse.getAmountUsed()) + rent.get());
                    break;
                case "Transportation":
                    transportation.addAndGet(Integer.parseInt(transactionHistoryResponse.getAmountUsed()) + transportation.get());
                    break;
                case "Utilities":
                    utilities.addAndGet(Integer.parseInt(transactionHistoryResponse.getAmountUsed()) + utilities.get());
                    break;
                case "Savings":
                    savings.addAndGet(Integer.parseInt(transactionHistoryResponse.getAmountUsed()) + savings.get());
                    break;
                case "Charity":
                    charity.addAndGet(Integer.parseInt(transactionHistoryResponse.getAmountUsed()) + charity.get());
                    break;
                case "Insurance":
                    insurance.addAndGet(Integer.parseInt(transactionHistoryResponse.getAmountUsed()) + insurance.get());
                    break;
                case "Gifts":
                    gifts.addAndGet(Integer.parseInt(transactionHistoryResponse.getAmountUsed()) + gifts.get());
                    break;
                case "Recive money":
                    recive.addAndGet(Integer.parseInt(transactionHistoryResponse.getAmountUsed()) + recive.get());
                    break;
                case "Transfer money":
                    transfer.addAndGet(Integer.parseInt(transactionHistoryResponse.getAmountUsed()) + transfer.get());
                    break;
                case "Others":
                    others.addAndGet(Integer.parseInt(transactionHistoryResponse.getAmountUsed()) + others.get());
                    break;
            }
        });

        // Set up PercentUsageTotal object
        PercentUsageTotal percentUsage = new PercentUsageTotal();
        percentUsage.setFood(BigDecimal.valueOf(food.get()));
        percentUsage.setBill(BigDecimal.valueOf(bill.get()));
        percentUsage.setEntertain(BigDecimal.valueOf(entertain.get()));
        percentUsage.setShopping(BigDecimal.valueOf(shopping.get()));
        percentUsage.setInvestment(BigDecimal.valueOf(investment.get()));
        percentUsage.setMedicine(BigDecimal.valueOf(medicine.get()));
        percentUsage.setEducation(BigDecimal.valueOf(education.get()));
        percentUsage.setTravel(BigDecimal.valueOf(travel.get()));
        percentUsage.setRent(BigDecimal.valueOf(rent.get()));
        percentUsage.setTransportation(BigDecimal.valueOf(transportation.get()));
        percentUsage.setUtilities(BigDecimal.valueOf(utilities.get()));
        percentUsage.setSavings(BigDecimal.valueOf(savings.get()));
        percentUsage.setCharity(BigDecimal.valueOf(charity.get()));
        percentUsage.setInsurance(BigDecimal.valueOf(insurance.get()));
        percentUsage.setGifts(BigDecimal.valueOf(gifts.get()));
        percentUsage.setOthers(BigDecimal.valueOf(others.get()));
        percentUsage.setRecive(BigDecimal.valueOf(recive.get()));
        percentUsage.setTransfer(BigDecimal.valueOf(transfer.get()));

        return ResponseEntity.ok(percentUsage);
    }


    public ResponseEntity<?> getFutureFund(String userName,String incomes, boolean save) {
        log.info("userName{}", userName);
        UserModel userModel = userRepository.findByUserName(userName);

        if(userModel == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Can't find user");
        }
        float needs = (float) (Integer.parseInt(incomes) * 55) /100;
        float saving = (float) (Integer.parseInt(incomes) * 10) /100;
        float investment = (float) (Integer.parseInt(incomes) * 10) /100;
        float hobbies = (float) (Integer.parseInt(incomes) * 10) /100;
        float emergency = (float) (Integer.parseInt(incomes) * 10) /100;
        float charity = (float) (Integer.parseInt(incomes) * 5) /100;

        MoneyPredict moneyPredict = MoneyPredict.builder()
                .needs(BigDecimal.valueOf(needs))
                .savings(BigDecimal.valueOf(saving))
                .charity(BigDecimal.valueOf(charity))
                .investment(BigDecimal.valueOf(investment))
                .emergency(BigDecimal.valueOf(emergency))
                .hobbies(BigDecimal.valueOf(hobbies))
                .build();

        if(save){
            userModel.setMoneyPredict(moneyPredict);
            userRepository.save(userModel);
            moneyPredictRepository.save(moneyPredict);
        }

        return ResponseEntity.ok(moneyPredict);

    }
}
