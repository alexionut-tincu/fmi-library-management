package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.NotBlank;

public record CreateBookRequest(
    @NotBlank(message = "Title is required")
    String title,

    @NotBlank(message = "Author is required")
    String author,

    @NotBlank(message = "ISBN is required")
    String isbn
) {}