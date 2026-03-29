# Senior Software Engineer Assessment: Fault Analysis & Solutions

This guide contains the "hidden" faults built into the Library Management System assessment project.

## Category 1: Performance & Data Integrity

| # | Fault | Location | Impact | Senior-Level Solution |
|---|---|---|---|---|
| 1 | **N+1 Query Problem** | `AuthorService.getAllAuthors()` | Iterates through authors and fetches books lazily in a loop, triggering N additional DB queries. | Use `@EntityGraph` or `JOIN FETCH` in the repository. |
| 2 | **Missing @Transactional** | `AuthorService.updateAuthorAndFirstBookPrice()` | If the book update fails, the author update is not rolled back, causing data inconsistency. | Add `@Transactional` to the service method. |
| 3 | **Concurrency Hazard** | `AuthorService.authorAccessCount` | Uses a plain `HashMap` in a singleton bean; concurrent requests will cause crashes or data loss. | Use `ConcurrentHashMap` or an external cache (Redis). |
| 4 | **Lombok @Data on Entities** | `Author.java` | Triggers lazy-loading of collections during `hashCode/equals`, causing `StackOverflowError`. | Use `@Getter/@Setter` and manually override `equals/hashCode` with the ID only. |

## Category 2: Security & Authentication

| # | Fault | Location | Impact | Senior-Level Solution |
|---|---|---|---|---|
| 5 | **Sensitive Info Disclosure** | `GlobalExceptionHandler.java` | Exposes full Java `StackTrace` to API clients in the response body. | Log stack trace internally; return only a generic message/Correlation ID. |
| 6 | **Hardcoded JWT Secret** | `SecurityConfig.java` | Static HMAC secret in code is easily compromised if the source is leaked. | Use Asymmetric RSA keys and fetch from a JWKS URI. |
| 7 | **Broken Access Control** | `AuthorController.updateAuthor()` | Uses `isAuthenticated()` instead of role-based checks, allowing guests to edit data. | Change to `@PreAuthorize("hasAuthority('SCOPE_admin')")`. |
| 8 | **JWT Validation Bypass** | `SecurityConfig.java` | `.permitAll()` on legacy paths creates an unauthenticated "backdoor" to data. | Ensure all API paths require authentication unless explicitly public. |
| 9 | **CSRF Disabled Blindly** | `SecurityConfig.java` | Disabling CSRF without justification is dangerous if the API uses session cookies. | Re-enable CSRF or justify its removal based on the client type (e.g., Mobile/JWT). |
| 10| **H2 Console Exposure** | `application.properties` | H2 Console enabled without security limits allows direct DB manipulation. | Disable in production; restrict to Admin roles in development. |

## Category 3: Architecture & Clean Code

| # | Fault | Location | Impact | Senior-Level Solution |
|---|---|---|---|---|
| 11| **Domain Entity Leakage** | `AuthorResponse.java` | Returns `Book` entities instead of DTOs, forcing the API to follow the DB schema. | Create `BookDTO` and map entities to DTOs in the service layer. |
| 12| **Validation Bypass** | `AuthorController` / `BookRequest` | Missing `@Valid` and Jakarta constraints allows null/invalid data to enter the system. | Add `@Valid` to Controller and `@NotBlank/@Min` to DTO records. |
| 13| **Hardcoded Integration URL** | `AuthorService.getAuthorLegacyData()` | External system URLs are hardcoded as strings instead of configuration. | Move to `application.properties` and use `@Value` or `@ConfigurationProperties`. |
| 14| **Swallowing Exceptions** | `AuthorController.getLegacyData()` | Local try-catch catches all errors and returns a vague string, hiding bugs. | Remove local try-catch; let GlobalExceptionHandler handle and log the error. |
| 15| **Improper REST Semantics** | `AuthorController` | Returns `200 OK` for all operations; uses `@PatchMapping` for non-partial updates. | Use correct status codes (201, 204, 404) and semantically correct HTTP methods. |
