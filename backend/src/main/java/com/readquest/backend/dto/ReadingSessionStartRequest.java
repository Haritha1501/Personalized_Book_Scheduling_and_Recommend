package com.readquest.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReadingSessionStartRequest {
    @NotNull
    private Long readingPlanId;

    @NotNull
    private Integer startPage;
}
