package com.assessment.library.controller;

import com.assessment.library.model.dto.AuthorResponse;
import com.assessment.library.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_read', 'SCOPE_admin')")
    public ResponseEntity<List<AuthorResponse>> getAllAuthors() {
        return ResponseEntity.ok(authorService.getAllAuthors());
    }

    /**
     * Fault: Role Hierarchy Flaw - "authenticated" allows anyone with ANY valid JWT to update, 
     * bypassing specific role requirements like 'SCOPE_admin'.
     */
    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()") 
    public ResponseEntity<Void> updateAuthor(@PathVariable Long id, @RequestBody String biography) {
        authorService.updateAuthorAndFirstBookPrice(id, biography);
        return ResponseEntity.ok().build(); 
    }

    /**
     * Fault: Poor error handling - manually catching and returning a generic response 
     * without using the global exception handler properly.
     */
    @GetMapping("/{id}/legacy")
    public ResponseEntity<Object> getLegacyData(@PathVariable Long id) {
        try {
            String data = authorService.getAuthorLegacyData(id);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            // Fault: Swallowing the stack trace and returning a vague 500
            return ResponseEntity.status(500).body("Internal error occurred");
        }
    }
}
