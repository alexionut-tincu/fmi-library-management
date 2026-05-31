package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "books")
public record BookEntity(
    @Id
    String id,
    String title,
    String author,
    String isbn,
    boolean available
) {
    public BookEntity(String title, String author, String isbn) {
        this(null, title, author, isbn, true);
    }
}