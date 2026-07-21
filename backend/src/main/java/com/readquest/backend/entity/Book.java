package com.readquest.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_book_id", unique = true, length = 50)
    private String googleBookId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 255)
    private String author;

    @Column(name = "cover_url", columnDefinition = "TEXT")
    private String coverUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_pages", nullable = false)
    private Integer totalPages;

    @Column(length = 255)
    private String categories;

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(length = 50)
    private String language;

    @Column(length = 50)
    private String isbn;

    @Column(length = 100)
    private String publisher;

    @Column(name = "published_date", length = 50)
    private String publishedDate;
}
