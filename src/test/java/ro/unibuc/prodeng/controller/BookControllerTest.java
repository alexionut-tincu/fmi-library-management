package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.GlobalExceptionHandler;
import ro.unibuc.prodeng.request.CreateBookRequest;
import ro.unibuc.prodeng.request.UpdateBookRequest;
import ro.unibuc.prodeng.response.BookResponse;
import ro.unibuc.prodeng.service.BookService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final BookResponse book1 = new BookResponse("1", "Clean Code", "Robert C. Martin", "978-0132350884", true);
    private final BookResponse book2 = new BookResponse("2", "The Pragmatic Programmer", "David Thomas", "978-0201616224", false);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController).build();
    }

    // ── GET /api/books ───────────────────────────────────────────

    @Test
    void testGetAllBooks_withBooks_returnsList() throws Exception {
        when(bookService.getAllBooks()).thenReturn(Arrays.asList(book1, book2));

        mockMvc.perform(get("/api/books").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("1")))
                .andExpect(jsonPath("$[0].title", is("Clean Code")))
                .andExpect(jsonPath("$[0].available", is(true)))
                .andExpect(jsonPath("$[1].id", is("2")))
                .andExpect(jsonPath("$[1].title", is("The Pragmatic Programmer")));

        verify(bookService, times(1)).getAllBooks();
    }

    @Test
    void testGetAllBooks_noBooks_returnsEmptyList() throws Exception {
        when(bookService.getAllBooks()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/books").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── GET /api/books/{id} ──────────────────────────────────────

    @Test
    void testGetBookById_existingBook_returnsBook() throws Exception {
        when(bookService.getBookById("1")).thenReturn(book1);

        mockMvc.perform(get("/api/books/{id}", "1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.title", is("Clean Code")))
                .andExpect(jsonPath("$.author", is("Robert C. Martin")))
                .andExpect(jsonPath("$.isbn", is("978-0132350884")))
                .andExpect(jsonPath("$.available", is(true)));

        verify(bookService, times(1)).getBookById("1");
    }

    @Test
    void testGetBookById_nonExistingBook_returnsNotFound() throws Exception {
        when(bookService.getBookById("non-existing")).thenThrow(new EntityNotFoundException("non-existing"));

        mockMvc.perform(get("/api/books/{id}", "non-existing").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/books/available ─────────────────────────────────

    @Test
    void testGetAvailableBooks_returnsOnlyAvailable() throws Exception {
        when(bookService.getAvailableBooks()).thenReturn(List.of(book1));

        mockMvc.perform(get("/api/books/available").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].available", is(true)));
    }

    // ── POST /api/books ──────────────────────────────────────────

    @Test
    void testCreateBook_validRequest_returnsCreated() throws Exception {
        CreateBookRequest request = new CreateBookRequest("Clean Code", "Robert C. Martin", "978-0132350884");
        when(bookService.createBook(any(CreateBookRequest.class))).thenReturn(book1);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.title", is("Clean Code")));

        verify(bookService, times(1)).createBook(any(CreateBookRequest.class));
    }

    @Test
    void testCreateBook_duplicateIsbn_returnsBadRequest() throws Exception {
        CreateBookRequest request = new CreateBookRequest("Clean Code", "Robert C. Martin", "978-0132350884");
        when(bookService.createBook(any(CreateBookRequest.class)))
                .thenThrow(new IllegalArgumentException("Book with ISBN 978-0132350884 already exists"));

        mockMvc = MockMvcBuilders.standaloneSetup(bookController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/books/{id} ──────────────────────────────────────

    @Test
    void testUpdateBook_existingBook_returnsUpdatedBook() throws Exception {
        UpdateBookRequest request = new UpdateBookRequest("Clean Code (2nd Ed.)", "Robert C. Martin");
        BookResponse updated = new BookResponse("1", "Clean Code (2nd Ed.)", "Robert C. Martin", "978-0132350884", true);
        when(bookService.updateBook(eq("1"), any(UpdateBookRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/books/{id}", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Clean Code (2nd Ed.)")));

        verify(bookService, times(1)).updateBook(eq("1"), any(UpdateBookRequest.class));
    }

    @Test
    void testUpdateBook_nonExistingBook_returnsNotFound() throws Exception {
        UpdateBookRequest request = new UpdateBookRequest("New Title", "New Author");
        when(bookService.updateBook(eq("non-existing"), any(UpdateBookRequest.class)))
                .thenThrow(new EntityNotFoundException("non-existing"));

        mockMvc.perform(put("/api/books/{id}", "non-existing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/books/{id} ───────────────────────────────────

    @Test
    void testDeleteBook_existingBook_returnsNoContent() throws Exception {
        doNothing().when(bookService).deleteBook("1");

        mockMvc.perform(delete("/api/books/{id}", "1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteBook("1");
    }

    @Test
    void testDeleteBook_nonExistingBook_returnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException("non-existing")).when(bookService).deleteBook("non-existing");

        mockMvc.perform(delete("/api/books/{id}", "non-existing").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}