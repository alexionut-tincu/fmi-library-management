package ro.unibuc.prodeng.response;

public record BookResponse(
    String id,
    String title,
    String author,
    String isbn,
    boolean available
) {}