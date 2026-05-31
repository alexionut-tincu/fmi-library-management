package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.BookEntity;
import ro.unibuc.prodeng.repository.BookRepository;
import ro.unibuc.prodeng.request.CreateBookRequest;
import ro.unibuc.prodeng.request.UpdateBookRequest;
import ro.unibuc.prodeng.response.BookResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private final BookEntity book1 = new BookEntity("1", "Clean Code", "Robert C. Martin", "978-0132350884", true);
    private final BookEntity book2 = new BookEntity("2", "The Pragmatic Programmer", "David Thomas", "978-0201616224", false);

    // ── getAllBooks ──────────────────────────────────────────────

    @Test
    void testGetAllBooks_withMultipleBooks_returnsAllBooks() {
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        List<BookResponse> result = bookService.getAllBooks();

        assertEquals(2, result.size());
        assertEquals("Clean Code", result.get(0).title());
        assertEquals("The Pragmatic Programmer", result.get(1).title());
    }

    @Test
    void testGetAllBooks_withNoBooks_returnsEmptyList() {
        when(bookRepository.findAll()).thenReturn(Collections.emptyList());

        List<BookResponse> result = bookService.getAllBooks();

        assertTrue(result.isEmpty());
    }

    // ── getBookById ──────────────────────────────────────────────

    @Test
    void testGetBookById_existingBook_returnsBook() {
        when(bookRepository.findById("1")).thenReturn(Optional.of(book1));

        BookResponse result = bookService.getBookById("1");

        assertNotNull(result);
        assertEquals("1", result.id());
        assertEquals("Clean Code", result.title());
        assertEquals("Robert C. Martin", result.author());
        assertEquals("978-0132350884", result.isbn());
        assertTrue(result.available());
    }

    @Test
    void testGetBookById_nonExistingBook_throwsEntityNotFoundException() {
        when(bookRepository.findById("non-existing")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookService.getBookById("non-existing"));
    }

    // ── createBook ──────────────────────────────────────────────

    @Test
    void testCreateBook_validRequest_createsAndReturnsBook() {
        CreateBookRequest request = new CreateBookRequest("Clean Code", "Robert C. Martin", "978-0132350884");

        when(bookRepository.findByIsbn("978-0132350884")).thenReturn(Optional.empty());
        when(bookRepository.save(any(BookEntity.class))).thenAnswer(invocation -> {
            BookEntity b = invocation.getArgument(0);
            return new BookEntity("generated-id", b.title(), b.author(), b.isbn(), b.available());
        });

        BookResponse result = bookService.createBook(request);

        assertNotNull(result);
        assertEquals("Clean Code", result.title());
        assertEquals("Robert C. Martin", result.author());
        assertEquals("978-0132350884", result.isbn());
        assertTrue(result.available());
        verify(bookRepository, times(1)).save(any(BookEntity.class));
    }

    @Test
    void testCreateBook_duplicateIsbn_throwsIllegalArgumentException() {
        CreateBookRequest request = new CreateBookRequest("Clean Code", "Robert C. Martin", "978-0132350884");

        when(bookRepository.findByIsbn("978-0132350884")).thenReturn(Optional.of(book1));

        assertThrows(IllegalArgumentException.class, () -> bookService.createBook(request));
        verify(bookRepository, never()).save(any());
    }

    // ── updateBook ──────────────────────────────────────────────

    @Test
    void testUpdateBook_existingBook_updatesAndReturnsBook() {
        UpdateBookRequest request = new UpdateBookRequest("Clean Code (2nd Ed.)", "Robert C. Martin");

        when(bookRepository.findById("1")).thenReturn(Optional.of(book1));
        when(bookRepository.save(any(BookEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse result = bookService.updateBook("1", request);

        assertEquals("Clean Code (2nd Ed.)", result.title());
        assertEquals("Robert C. Martin", result.author());
        assertEquals("978-0132350884", result.isbn()); // ISBN preserved
        verify(bookRepository, times(1)).save(any(BookEntity.class));
    }

    @Test
    void testUpdateBook_nonExistingBook_throwsEntityNotFoundException() {
        UpdateBookRequest request = new UpdateBookRequest("New Title", "New Author");

        when(bookRepository.findById("non-existing")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookService.updateBook("non-existing", request));
        verify(bookRepository, never()).save(any());
    }

    // ── deleteBook ──────────────────────────────────────────────

    @Test
    void testDeleteBook_existingBook_deletesSuccessfully() {
        when(bookRepository.existsById("1")).thenReturn(true);
        doNothing().when(bookRepository).deleteById("1");

        bookService.deleteBook("1");

        verify(bookRepository, times(1)).deleteById("1");
    }

    @Test
    void testDeleteBook_nonExistingBook_throwsEntityNotFoundException() {
        when(bookRepository.existsById("non-existing")).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> bookService.deleteBook("non-existing"));
        verify(bookRepository, never()).deleteById(anyString());
    }

    // ── getAvailableBooks ────────────────────────────────────────

    @Test
    void testGetAvailableBooks_withAvailableBooks_returnsOnlyAvailable() {
        when(bookRepository.findByAvailable(true)).thenReturn(List.of(book1));

        List<BookResponse> result = bookService.getAvailableBooks();

        assertEquals(1, result.size());
        assertTrue(result.get(0).available());
        assertEquals("Clean Code", result.get(0).title());
    }

    @Test
    void testGetAvailableBooks_noAvailableBooks_returnsEmptyList() {
        when(bookRepository.findByAvailable(true)).thenReturn(Collections.emptyList());

        List<BookResponse> result = bookService.getAvailableBooks();

        assertTrue(result.isEmpty());
    }
}