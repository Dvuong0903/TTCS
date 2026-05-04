package com.example.HousePrediction.service;

import com.example.HousePrediction.entity.PredictionHistory;
import com.example.HousePrediction.repository.PredictionHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.HashMap;
import java.util.Map;

@Service
public class PredictionService {

    @Autowired
    private PredictionHistoryRepository repository;

    public double getPredictionAndSave(double area) {
        try {
            // Gọi sang Python cổng 5000
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Double> requestData = new HashMap<>();
            requestData.put("dien_tich", area);

            ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:5000/predict", requestData, Map.class);
            double predictedPrice = (Double) response.getBody().get("gia_du_doan");

            // Lưu vào MySQL
            PredictionHistory history = new PredictionHistory();
            history.setArea(area);
            history.setPredictedPrice(predictedPrice);
            repository.save(history);

            return predictedPrice;
        } catch (Exception e) {
            return 0.0; // Nếu Python chưa bật thì tạm trả về 0
        }
    }
    public Page<PredictionHistory> getHistory(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return repository.findAll(pageable);
    }
}