package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateBookRequest(
    @NotBlank(message = "Title is required")
    String title,

    @NotBlank(message = "Author is required")
    String author
) {}