package com.nguyenminh.microservices.zwallet.service;
import com.nguyenminh.microservices.zwallet.configuration.AppConfiguration;
import com.nguyenminh.microservices.zwallet.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserModelService userModelService;
    private final AppConfiguration appConfiguration;

    public UserDetailsServiceImpl(UserModelService userModelService , AppConfiguration appConfiguration) {
        this.userModelService = userModelService;
        this.appConfiguration = appConfiguration;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserResponse user = userModelService.getUserByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        log.info("Found user: {}", user);
        log.info("Password: {}", user.getPassword());

        return User.builder()
                .username(user.getUserName())
                .password(user.getPassword())
                .authorities("USER")
                .build();
    }

}