package com.nguyenminh.microservices.zwallet.service;

import com.nguyenminh.microservices.zwallet.dto.TagAndQuotesRequest;
import com.nguyenminh.microservices.zwallet.dto.UserResponse;
import com.nguyenminh.microservices.zwallet.model.UserModel;
import com.nguyenminh.microservices.zwallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final UserRepository userRepository;
    private final Mapper mapper;

    public UserResponse updateQuotesAndTag(String userName, TagAndQuotesRequest tagAndQuotesRequest) {
        UserModel userModel = userRepository.findByUserName(userName);

        if (userModel != null) {
            userModel.setTag(tagAndQuotesRequest.getTag());
            userModel.setQuotes(tagAndQuotesRequest.getQuotes());
            userRepository.save(userModel);

        } else throw new RuntimeException("Cant find user " + userName);

        return mapper.mapToUserResponse(userModel);
    }

    // Change total field of User model
    public UserResponse updateUserTotal(String name, String totalAmount) {
        UserModel userModel = userRepository.findByUserName(name);
        if (userModel != null) {
            userModel.setTotalAmount(totalAmount);
            userRepository.save(userModel);
        } else {
            throw new RuntimeException("Cant find user");
        }
        return mapper.mapToUserResponse(userModel);
    }


}
