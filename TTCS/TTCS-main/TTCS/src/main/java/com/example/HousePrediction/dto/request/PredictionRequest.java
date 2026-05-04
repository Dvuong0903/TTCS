package com.example.HousePrediction.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PredictionRequest {

    @NotNull(message = "Diện tích không được để trống!")
    @Min(value = 10, message = "Diện tích nhà phải từ 10m2 trở lên!")
    private Double area;
}