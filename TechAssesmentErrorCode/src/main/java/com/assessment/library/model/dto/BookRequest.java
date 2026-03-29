package com.assessment.library.model.dto;

import java.math.BigDecimal;

/**
 * Fault: Missing validation annotations (@NotBlank, @Min, etc.)
 */
public record BookRequest(
    String title,
    String isbn,
    BigDecimal price,
    Long authorId
) {}
