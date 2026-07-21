package com.readquest.backend.controller;

import com.readquest.backend.entity.Book;
import com.readquest.backend.entity.Recommendation;
import com.readquest.backend.entity.User;
import com.readquest.backend.external.googlebooks.GoogleBooksService;
import com.readquest.backend.external.openlibrary.OpenLibraryService;
import com.readquest.backend.repository.RecommendationRepository;
import com.readquest.backend.repository.UserRepository;
import com.readquest.backend.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookController {

    private final GoogleBooksService googleBooksService;
    private final OpenLibraryService openLibraryService;
    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;

    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String query) {
        List<Book> books = googleBooksService.searchBooks(query);
        if (books.isEmpty()) {
            books = openLibraryService.searchBooks(query);
        }
        if (books.isEmpty()) {
            books = getStaticFallbackBooks();
        }
        return ResponseEntity.ok(books);
    }

    @GetMapping("/details/{googleBookId}")
    public ResponseEntity<Book> getBookDetails(@PathVariable String googleBookId) {
        Book book;
        if (googleBookId.startsWith("OL_")) {
            String workKey = googleBookId.replace("OL_", "");
            book = openLibraryService.getBookDetails(workKey);
        } else {
            book = googleBooksService.getBookDetails(googleBookId);
        }
        
        if (book == null) {
            // Check fallback
            List<Book> fallbacks = getStaticFallbackBooks();
            book = fallbacks.stream()
                    .filter(b -> b.getGoogleBookId().equals(googleBookId))
                    .findFirst()
                    .orElse(null);
        }

        if (book == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(book);
    }

    private List<Book> getStaticFallbackBooks() {
        List<Book> fallback = new java.util.ArrayList<>();
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

    @GetMapping("/recommendations")
    public ResponseEntity<List<Book>> getRecommendations() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Recommendation> recs = recommendationRepository.findByUserId(userDetails.getId());

        if (recs.isEmpty()) {
            // Lazy load recommendations if database list is empty, using classic genres
            User user = userRepository.findById(userDetails.getId()).orElseThrow();
            String genre = user.getReaderType() != null ? user.getReaderType().getName() : "fiction";
            List<Book> books = openLibraryService.getRecommendations(genre, "Classic");
            return ResponseEntity.ok(books);
        }

        List<Book> books = recs.stream().map(Recommendation::getBook).collect(Collectors.toList());
        return ResponseEntity.ok(books);
    }
}
