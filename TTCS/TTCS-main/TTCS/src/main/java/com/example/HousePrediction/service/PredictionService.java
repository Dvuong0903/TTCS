package com.example.HousePrediction.service;

import com.example.HousePrediction.dto.request.PredictionRequest;
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

    public double getPredictionAndSave(PredictionRequest request, String username) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Gửi đủ 7 đặc trưng sang Python
            // city/district/houseType/legal là mã số dạng string (khớp với encoder)
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("area",      request.getArea());
            requestData.put("bedrooms",  request.getBedrooms() != null ? request.getBedrooms() : 0.0);
            requestData.put("floors",    request.getFloors()   != null ? request.getFloors()   : 1.0);
            requestData.put("city",      request.getCity()     != null ? request.getCity()     : "0");
            requestData.put("district",  request.getDistrict() != null ? request.getDistrict() : "0");
            requestData.put("houseType", request.getHouseType()!= null ? request.getHouseType(): "0");
            requestData.put("legal",     request.getLegal()    != null ? request.getLegal()    : "0");

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "http://localhost:5000/predict", requestData, Map.class);

            double predictedPrice = ((Number) response.getBody().get("gia_du_doan")).doubleValue();

            // Lưu lịch sử
            PredictionHistory history = new PredictionHistory();
            history.setArea(request.getArea());
            history.setPredictedPrice(predictedPrice);
            history.setUsername(username);
            repository.save(history);

            return predictedPrice;

        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public Page<PredictionHistory> getHistory(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return repository.findAll(pageable);
    }
}