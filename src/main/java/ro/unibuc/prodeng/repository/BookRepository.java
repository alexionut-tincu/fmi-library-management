package ro.unibuc.prodeng.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.prodeng.model.BookEntity;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends MongoRepository<BookEntity, String> {
    Optional<BookEntity> findByIsbn(String isbn);
    List<BookEntity> findByAuthor(String author);
    List<BookEntity> findByAvailable(boolean available);
}