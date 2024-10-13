package com.nguyenminh.microservices.zwallet.service;

import com.nguyenminh.microservices.zwallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidateUserService {
    private final UserRepository userRepository;
    // check if user exist
    public boolean checkUserName(String name) {
        return userRepository.findByUserName(name) != null;
    }

}
