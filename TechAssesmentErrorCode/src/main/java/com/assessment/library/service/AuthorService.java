package com.assessment.library.service;

import com.assessment.library.model.dto.AuthorResponse;
import com.assessment.library.model.entity.Author;
import com.assessment.library.model.entity.Book;
import com.assessment.library.repository.AuthorRepository;
import com.assessment.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    /**
     * Fault: Potential Concurrency Issue - HashMap is not thread-safe in a singleton service.
     */
    private final Map<Long, Integer> authorAccessCount = new HashMap<>();

    /**
     * Fault: N+1 Query Problem - Fetching each author's books individually.
     */
    public List<AuthorResponse> getAllAuthors() {
        return authorRepository.findAll().stream()
                .map(author -> {
                    // Simulating manual work that triggers N+1
                    log.info("Processing author: {}", author.getName());
                    return new AuthorResponse(
                            author.getId(),
                            author.getName(),
                            author.getBiography(),
                            author.getBooks() // Triggering lazy load for each author in a loop
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Fault: Missing @Transactional - Multiple updates without a transaction.
     */
    public void updateAuthorAndFirstBookPrice(Long authorId, String newBio) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found"));
        
        author.setBiography(newBio);
        authorRepository.save(author);

        // Fault: If this fails, the author update above is not rolled back.
        if (!author.getBooks().isEmpty()) {
            Book firstBook = author.getBooks().get(0);
            if (firstBook.getPrice() == null) {
                throw new IllegalStateException("First book price is missing, cannot update.");
            }
            firstBook.setPrice(firstBook.getPrice().multiply(java.math.BigDecimal.valueOf(1.1)));
            bookRepository.save(firstBook);
        }

        // Updating thread-unsafe map
        authorAccessCount.merge(authorId, 1, Integer::sum);
    }

    /**
     * Fault: Hardcoded configuration/URL.
     */
    public String getAuthorLegacyData(Long id) {
        String legacyUrl = "http://legacy-system:8081/v1/authors/" + id;
        log.info("Fetching from: {}", legacyUrl);
        return "Legacy data for " + id;
    }
}
