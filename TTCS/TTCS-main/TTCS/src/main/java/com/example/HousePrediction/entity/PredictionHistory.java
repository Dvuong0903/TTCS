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
    @Column(name = "username")
    private String username;
    private LocalDateTime createdAt = LocalDateTime.now();
}