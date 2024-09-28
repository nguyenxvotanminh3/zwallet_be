package com.nguyenminh.microservices.zwallet.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "user")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserModel {


    private String id;
    private String userName;
    private String company;
    private String password;
    private String emailAddress;
    private String fullName;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String aboutMe;
    private String quotes;
    private String tag;
    private String totalAmount;
    private String friend ;
    private String profileImage;

    @DBRef
    private List<TransactionHistory> transactionHistory;


}
