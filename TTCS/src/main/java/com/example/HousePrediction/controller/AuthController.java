package com.example.HousePrediction.controller;

import com.example.HousePrediction.dto.request.LoginRequest;
import com.example.HousePrediction.dto.request.RegisterRequest;
import com.example.HousePrediction.dto.response.ResponseObject;
import com.example.HousePrediction.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    // API Đăng ký: POST http://localhost:8080/api/auth/register
    @PostMapping("/register")
    public ResponseEntity<ResponseObject> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        ResponseObject response = new ResponseObject("SUCCESS", "Đăng ký tài khoản thành công!", null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // API Đăng nhập: POST http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public ResponseEntity<ResponseObject> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.login(request);
        ResponseObject response = new ResponseObject("SUCCESS", "Đăng nhập thành công!", token);
        //                                                                                 ↑ token vào data
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}