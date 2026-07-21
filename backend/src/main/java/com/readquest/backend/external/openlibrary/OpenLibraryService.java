package com.readquest.backend.external.openlibrary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readquest.backend.entity.Book;
import com.readquest.backend.external.googlebooks.GoogleBooksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenLibraryService {
    private final GoogleBooksService googleBooksService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Book> getRecommendations(String genre, String author) {
        List<Book> books = new ArrayList<>();
        try {
            // Normalize genre for Open Library (e.g. Science Fiction -> science_fiction)
            String cleanGenre = (genre == null ? "fiction" : genre.trim().toLowerCase().replace(" ", "_"));
            String url = "https://openlibrary.org/subjects/" + URLEncoder.encode(cleanGenre, StandardCharsets.UTF_8) + ".json?limit=10";
            
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode works = root.get("works");
            
            if (works != null && works.isArray() && works.size() > 0) {
                for (JsonNode work : works) {
                    books.add(parseWorkNode(work));
                }
            } else {
                // Graceful fallback: query Google Books by author/genre
                log.info("Open Library returned no results for genre: {}. Falling back to Google Books.", cleanGenre);
                books = googleBooksService.searchBooks(author != null ? author : genre);
            }
        } catch (Exception e) {
            log.error("Error fetching recommendations from Open Library: {}. Falling back to Google Books.", e.getMessage());
            try {
                books = googleBooksService.searchBooks(genre);
            } catch (Exception ex) {
                log.error("Google Books fallback also failed: {}", ex.getMessage());
                // Secondary static fallback to prevent failure
                books = getStaticFallbackBooks();
            }
        }
        return books;
    }

    public String getCoverFallback(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return null;
        }
        return "https://covers.openlibrary.org/b/isbn/" + isbn.trim() + "-L.jpg";
    }

    private Book parseWorkNode(JsonNode work) {
        String key = work.path("key").asText().replace("/works/", "");
        String title = work.path("title").asText("Unknown Title");
        
        List<String> authorsList = new ArrayList<>();
        JsonNode authors = work.path("authors");
        if (authors.isArray()) {
            for (JsonNode author : authors) {
                authorsList.add(author.path("name").asText());
            }
        }
        String author = authorsList.isEmpty() ? "Unknown Author" : String.join(", ", authorsList);

        String coverUrl = "";
        long coverId = work.path("cover_id").asLong(0);
        if (coverId != 0) {
            coverUrl = "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg";
        }

        // Open Library does not always supply page numbers, rating, and description in the subjects api
        // We will default these values safely
        return Book.builder()
                .googleBookId("OL_" + key)
                .title(title)
                .author(author)
                .coverUrl(coverUrl)
                .description("A wonderful recommended book from Open Library's collection.")
                .totalPages(250)
                .categories("Recommended")
                .averageRating(4.0)
                .language("en")
                .isbn("")
                .publisher("Open Library")
                .publishedDate("N/A")
                .build();
    }

    private List<Book> getStaticFallbackBooks() {
        List<Book> fallback = new ArrayList<>();
        fallback.add(Book.builder()
                .googleBookId("classic_1")
                .title("The Great Gatsby")
                .author("F. Scott Fitzgerald")
                .coverUrl("https://covers.openlibrary.org/b/isbn/9780743273565-L.jpg")
                .description("The story of the mysteriously wealthy Jay Gatsby and his love for the beautiful Daisy Buchanan.")
                .totalPages(180)
                .categories("Fiction, Classic")
                .averageRating(4.3)
                .build());
        fallback.add(Book.builder()
                .googleBookId("classic_2")
                .title("To Kill a Mockingbird")
                .author("Harper Lee")
                .coverUrl("https://covers.openlibrary.org/b/isbn/9780446310789-L.jpg")
                .description("The story of Atticus Finch defending a black man charged with rape in Alabama.")
                .totalPages(281)
                .categories("Fiction, Classic")
                .averageRating(4.8)
                .build());
        fallback.add(Book.builder()
                .googleBookId("classic_3")
                .title("1984")
                .author("George Orwell")
                .coverUrl("https://covers.openlibrary.org/b/isbn/9780451524935-L.jpg")
                .description("Winston Smith, a member of the Outer Party, rebels against Big Brother.")
                .totalPages(328)
                .categories("Fiction, Dystopian")
                .averageRating(4.6)
                .build());
        return fallback;
    }

    public List<Book> searchBooks(String query) {
        List<Book> books = new ArrayList<>();
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://openlibrary.org/search.json?q=" + encodedQuery + "&limit=20";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode docs = root.get("docs");
            if (docs != null && docs.isArray()) {
                for (JsonNode doc : docs) {
                    books.add(parseDocNode(doc));
                }
            }
        } catch (Exception e) {
            log.error("Error searching books in Open Library API: {}", e.getMessage());
        }
        return books;
    }

    private Book parseDocNode(JsonNode doc) {
        String key = doc.path("key").asText().replace("/works/", "");
        String title = doc.path("title").asText("Unknown Title");
        
        List<String> authorsList = new ArrayList<>();
        JsonNode authorNames = doc.path("author_name");
        if (authorNames.isArray()) {
            for (JsonNode name : authorNames) {
                authorsList.add(name.asText());
            }
        }
        String author = authorsList.isEmpty() ? "Unknown Author" : String.join(", ", authorsList);

        String coverUrl = "";
        long coverI = doc.path("cover_i").asLong(0);
        if (coverI != 0) {
            coverUrl = "https://covers.openlibrary.org/b/id/" + coverI + "-L.jpg";
        } else {
            JsonNode isbns = doc.path("isbn");
            if (isbns.isArray() && isbns.size() > 0) {
                coverUrl = getCoverFallback(isbns.get(0).asText());
            }
        }

        int totalPages = doc.path("number_of_pages_median").asInt(200);
        if (totalPages == 200) {
            totalPages = doc.path("number_of_pages").asInt(200);
        }

        List<String> catList = new ArrayList<>();
        JsonNode subjects = doc.path("subject");
        if (subjects.isArray()) {
            int count = 0;
            for (JsonNode sub : subjects) {
                catList.add(sub.asText());
                if (++count >= 3) break;
            }
        }
        String categories = catList.isEmpty() ? "General" : String.join(", ", catList);

        String isbn = "";
        JsonNode isbns = doc.path("isbn");
        if (isbns.isArray() && isbns.size() > 0) {
            isbn = isbns.get(0).asText();
        }

        String publisher = "";
        JsonNode publishers = doc.path("publisher");
        if (publishers.isArray() && publishers.size() > 0) {
            publisher = publishers.get(0).asText();
        }

        String publishedDate = doc.path("first_publish_year").asText("N/A");

        return Book.builder()
                .googleBookId("OL_" + key)
                .title(title)
                .author(author)
                .coverUrl(coverUrl)
                .description("Search result from Open Library.")
                .totalPages(totalPages)
                .categories(categories)
                .averageRating(4.0)
                .language("en")
                .isbn(isbn)
                .publisher(publisher.isEmpty() ? "Unknown Publisher" : publisher)
                .publishedDate(publishedDate)
                .build();
    }

    public Book getBookDetails(String workKey) {
        try {
            String url = "https://openlibrary.org/works/" + workKey + ".json";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            
            String title = root.path("title").asText("Unknown Title");
            
            String description = "No description available.";
            JsonNode descNode = root.get("description");
            if (descNode != null) {
                if (descNode.isTextual()) {
                    description = descNode.asText();
                } else if (descNode.has("value")) {
                    description = descNode.get("value").asText();
                }
            }

            String coverUrl = "";
            JsonNode covers = root.get("covers");
            if (covers != null && covers.isArray() && covers.size() > 0) {
                long coverId = covers.get(0).asLong(0);
                if (coverId != 0) {
                    coverUrl = "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg";
                }
            }

            return Book.builder()
                    .googleBookId("OL_" + workKey)
                    .title(title)
                    .author("Open Library Author")
                    .coverUrl(coverUrl)
                    .description(description)
                    .totalPages(250)
                    .categories("Recommended")
                    .averageRating(4.0)
                    .language("en")
                    .isbn("")
                    .publisher("Open Library")
                    .publishedDate("N/A")
                    .build();
        } catch (Exception e) {
            log.error("Error getting work details from Open Library: {}", e.getMessage());
            return null;
        }
    }
}
