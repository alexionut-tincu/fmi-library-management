package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ro.unibuc.prodeng.IntegrationTestBase;
import ro.unibuc.prodeng.repository.BookRepository;
import ro.unibuc.prodeng.request.CreateBookRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("BookController Integration Tests")
class BookControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        bookRepository.deleteAll();
    }

    // Helper: creates a book and returns its ID
    private String createBook(String title, String author, String isbn) throws Exception {
        CreateBookRequest request = new CreateBookRequest(title, author, isbn);

        String response = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.author").value(author))
                .andExpect(jsonPath("$.isbn").value(isbn))
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void testCreateAndGetBook_validBook_retrievesBookSuccessfully() throws Exception {
        // Arrange
        String bookId = createBook("Clean Code", "Robert C. Martin", "978-0132350884");

        // Act & Assert
        mockMvc.perform(get("/api/books/" + bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Robert C. Martin"))
                .andExpect(jsonPath("$.isbn").value("978-0132350884"));
    }

    @Test
    void testGetAllBooks_multipleBooksExist_returnsAllBooks() throws Exception {
        // Arrange
        createBook("Clean Code", "Robert C. Martin", "978-0132350884");
        createBook("The Pragmatic Programmer", "Andrew Hunt", "978-0201616224");

        // Act & Assert
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testUpdateBook_validData_updatesBookSuccessfully() throws Exception {
        // Arrange
        String bookId = createBook("Clean Code", "Robert C. Martin", "978-0132350884");

        // Act & Assert
        mockMvc.perform(put("/api/books/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Clean Code (2nd Ed)\",\"author\":\"Robert C. Martin\",\"isbn\":\"978-0132350884\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code (2nd Ed)"));
    }

    @Test
    void testDeleteBook_existingBook_deletesSuccessfully() throws Exception {
        // Arrange
        String bookId = createBook("Clean Code", "Robert C. Martin", "978-0132350884");

        // Act & Assert
        mockMvc.perform(delete("/api/books/" + bookId))
                .andExpect(status().isNoContent());

        // Verify deletion persisted in DB
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetBook_nonExistentId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/books/nonexistentid123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateBook_duplicateIsbn_returnsBadRequest() throws Exception {
        // Arrange
        createBook("Clean Code", "Robert C. Martin", "978-0132350884");

        // Act & Assert — same ISBN should fail
        CreateBookRequest duplicate = new CreateBookRequest("Other Book", "Other Author", "978-0132350884");
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest());
    }
}