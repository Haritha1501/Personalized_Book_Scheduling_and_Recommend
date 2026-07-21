package com.readquest.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReadingSessionEndRequest {
    @NotNull
    private Long readingSessionId;

    @NotNull
    private Integer endPage;
}
