package ro.unibuc.prodeng.service;

import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.BookEntity;
import ro.unibuc.prodeng.repository.BookRepository;
import ro.unibuc.prodeng.request.CreateBookRequest;
import ro.unibuc.prodeng.request.UpdateBookRequest;
import ro.unibuc.prodeng.response.BookResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BookResponse getBookById(String id) {
        BookEntity book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
        return toResponse(book);
    }

    public BookResponse createBook(CreateBookRequest request) {
        if (bookRepository.findByIsbn(request.isbn()).isPresent()) {
            throw new IllegalArgumentException("Book with ISBN " + request.isbn() + " already exists");
        }
        BookEntity book = new BookEntity(request.title(), request.author(), request.isbn());
        return toResponse(bookRepository.save(book));
    }

    public BookResponse updateBook(String id, UpdateBookRequest request) {
        BookEntity existing = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
        BookEntity updated = new BookEntity(
                existing.id(),
                request.title(),
                request.author(),
                existing.isbn(),
                existing.available()
        );
        return toResponse(bookRepository.save(updated));
    }

    public void deleteBook(String id) {
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException(id);
        }
        bookRepository.deleteById(id);
    }

    public List<BookResponse> getAvailableBooks() {
        return bookRepository.findByAvailable(true).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private BookResponse toResponse(BookEntity book) {
        return new BookResponse(book.id(), book.title(), book.author(), book.isbn(), book.available());
    }
}