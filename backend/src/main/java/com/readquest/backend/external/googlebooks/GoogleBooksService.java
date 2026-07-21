package com.readquest.backend.external.googlebooks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readquest.backend.entity.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleBooksService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Book> searchBooks(String query) {
        List<Book> books = new ArrayList<>();
        try {
            String encodedQuery = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
            String url = "https://www.googleapis.com/books/v1/volumes?q=" + encodedQuery + "&maxResults=20";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.get("items");
            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    books.add(parseBookNode(item));
                }
            }
        } catch (Exception e) {
            log.error("Error searching books in Google Books API: {}", e.getMessage());
        }
        return books;
    }

    public Book getBookDetails(String googleBookId) {
        try {
            String url = "https://www.googleapis.com/books/v1/volumes/" + googleBookId;
            String response = restTemplate.getForObject(url, String.class);
            JsonNode item = objectMapper.readTree(response);
            return parseBookNode(item);
        } catch (Exception e) {
            log.error("Error getting book details in Google Books API: {}", e.getMessage());
            return null;
        }
    }

    private Book parseBookNode(JsonNode item) {
        String id = item.path("id").asText();
        JsonNode volInfo = item.path("volumeInfo");

        String title = volInfo.path("title").asText("Unknown Title");
        
        List<String> authorsList = new ArrayList<>();
        if (volInfo.has("authors")) {
            for (JsonNode author : volInfo.get("authors")) {
                authorsList.add(author.asText());
            }
        }
        String author = authorsList.isEmpty() ? "Unknown Author" : String.join(", ", authorsList);

        String coverUrl = volInfo.path("imageLinks").path("thumbnail").asText();
        if (coverUrl.isEmpty()) {
            coverUrl = volInfo.path("imageLinks").path("smallThumbnail").asText();
        }
        
        if (coverUrl.startsWith("http://")) {
            coverUrl = coverUrl.replace("http://", "https://");
        }

        String description = volInfo.path("description").asText("No description available.");
        int totalPages = volInfo.path("pageCount").asInt(200);

        List<String> catList = new ArrayList<>();
        if (volInfo.has("categories")) {
            for (JsonNode cat : volInfo.get("categories")) {
                catList.add(cat.asText());
            }
        }
        String categories = catList.isEmpty() ? "General" : String.join(", ", catList);

        double averageRating = volInfo.path("averageRating").asDouble(0.0);
        String language = volInfo.path("language").asText("en");

        String isbn = "";
        if (volInfo.has("industryIdentifiers")) {
            for (JsonNode identifier : volInfo.get("industryIdentifiers")) {
                String type = identifier.path("type").asText();
                if ("ISBN_13".equals(type) || "ISBN_10".equals(type)) {
                    isbn = identifier.path("identifier").asText();
                }
            }
        }

        String publisher = volInfo.path("publisher").asText("Unknown Publisher");
        String publishedDate = volInfo.path("publishedDate").asText("Unknown Date");

        return Book.builder()
                .googleBookId(id)
                .title(title)
                .author(author)
                .coverUrl(coverUrl)
                .description(description)
                .totalPages(totalPages)
                .categories(categories)
                .averageRating(averageRating)
                .language(language)
                .isbn(isbn)
                .publisher(publisher)
                .publishedDate(publishedDate)
                .build();
    }
}
