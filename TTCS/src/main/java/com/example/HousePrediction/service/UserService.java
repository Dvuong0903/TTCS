package com.example.HousePrediction.service;

import com.example.HousePrediction.config.JwtUtil;
import com.example.HousePrediction.dto.request.LoginRequest;
import com.example.HousePrediction.dto.request.RegisterRequest;
import com.example.HousePrediction.entity.User;
import com.example.HousePrediction.exception.AppException;
import com.example.HousePrediction.exception.ErrorCode;
import com.example.HousePrediction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public User register(RegisterRequest request) {
        // Kiểm tra xem user đã tồn tại chưa
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (!request.getPassword().equals(request.getConfirm())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        // Tạo user mới
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setFullname(request.getFullname());  
        newUser.setEmail(request.getEmail());  

        return userRepository.save(newUser);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean isMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!isMatch) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        return jwtUtil.generateToken(user.getUsername());
    }
}