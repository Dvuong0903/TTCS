package com.example.HousePrediction.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "prediction_history")
@Getter
@Setter
public class PredictionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double area;
    private double predictedPrice;
    
    private String city;
    private String district;
    private String houseType;
    private String legal;
    private Double floors;
    private Double bedrooms;

    @Column(name = "username")
    private String username;
    private LocalDateTime createdAt = LocalDateTime.now();
}