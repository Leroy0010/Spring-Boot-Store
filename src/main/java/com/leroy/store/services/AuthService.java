package com.leroy.store.services;

import com.leroy.store.entities.User;
import com.leroy.store.mappers.UserMapper;
import com.leroy.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public User getAuthenticatedUser() {
        var authentication =  SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;

        var userId = UUID.fromString(Objects.requireNonNull(authentication.getPrincipal()).toString());
        var user = userRepository.findById(userId).orElse(null);
        if(user == null){
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }
}
