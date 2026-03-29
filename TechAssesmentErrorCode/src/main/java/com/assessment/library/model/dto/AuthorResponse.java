package com.assessment.library.model.dto;

import com.assessment.library.model.entity.Book;
import java.util.List;

/**
 * Fault: Leaking Book Entities instead of using a BookDTO.
 */
public record AuthorResponse(
    Long id,
    String name,
    String biography,
    List<Book> books
) {}
