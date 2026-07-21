package com.readquest.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReadingPlanRequest {
    private String googleBookId;
    
    @NotBlank
    private String title;
    
    private String author;
    private String coverUrl;
    private String description;
    
    @NotNull
    @Min(1)
    private Integer totalPages;
    
    private String categories;
    private Double averageRating;
    private String language;
    private String isbn;
    private String publisher;
    private String publishedDate;
    
    @NotNull
    @Min(1)
    private Integer targetDays;
}
