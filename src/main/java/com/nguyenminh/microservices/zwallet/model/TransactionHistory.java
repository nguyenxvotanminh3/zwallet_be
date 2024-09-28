package com.nguyenminh.microservices.zwallet.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(value = "transaction_history")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TransactionHistory {


    private String id;
    private String amountUsed;
    private String localDateTime;
    private String purpose;
    private String moneyLeft;

    @DBRef
    private UserModel user; // Use @DBRef to reference the user


}
