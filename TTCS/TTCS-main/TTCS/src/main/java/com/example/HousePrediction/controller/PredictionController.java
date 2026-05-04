package com.example.HousePrediction.controller;

import com.example.HousePrediction.dto.request.PredictionRequest;
import com.example.HousePrediction.dto.response.ResponseObject;
import com.example.HousePrediction.entity.PredictionHistory;
import com.example.HousePrediction.repository.PredictionHistoryRepository;
import com.example.HousePrediction.service.PredictionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/predict")
@CrossOrigin(origins = "*")
public class PredictionController {

    @Autowired
    private PredictionHistoryRepository predictionHistoryRepository;

    @Autowired
    private PredictionService predictionService;

    @PostMapping
    public ResponseEntity<ResponseObject> predictHousePrice(@Valid @RequestBody PredictionRequest request) {
        double price = predictionService.getPredictionAndSave(request.getArea());

        if (price == 0.0) {
            throw new RuntimeException("Lỗi kết nối tới AI Python ở cổng 5000");
        }

        ResponseObject response = new ResponseObject("SUCCESS", "Dự đoán thành công!", price);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(Principal principal) {

        String username = principal.getName();

        List<PredictionHistory> historyList = predictionHistoryRepository.findByUsernameOrderByCreatedAtDesc(username);

        return ResponseEntity.ok(historyList);
    }
}