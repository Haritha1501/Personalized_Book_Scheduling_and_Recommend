package com.readquest.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SpeedTestRequest {
    @NotNull
    private Double wpm;

    @NotNull
    private Double accuracy;

    @NotBlank
    private String readingType;
}
