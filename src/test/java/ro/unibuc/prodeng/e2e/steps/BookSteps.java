package ro.unibuc.prodeng.e2e.steps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BookSteps {

    private static final String BASE_URL = "http://localhost:8080";

    private final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ResponseEntity<String> latestResponse;
    private final List<String> createdBookIds = new ArrayList<>();
    private String lastCreatedBookId;
    private Map<String, Object> lastRetrievedBook;

    @After
    public void cleanup() {
        for (String bookId : createdBookIds) {
            try {
                restTemplate.delete(BASE_URL + "/api/books/" + bookId);
            } catch (Exception ignored) {}
        }
        createdBookIds.clear();
    }

    @Given("a book with title {string} and author {string} and isbn {word} exists")
    public void createBookForTest(String title, String author, String isbn) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = objectMapper.writeValueAsString(Map.of("title", title, "author", author, "isbn", isbn));
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL + "/api/books", entity, String.class);
        Map<String, Object> book = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
        lastCreatedBookId = (String) book.get("id");
        createdBookIds.add(lastCreatedBookId);
    }

    @When("the client creates a book with title {string} and author {string} and isbn {word}")
    public void createBook(String title, String author, String isbn) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = objectMapper.writeValueAsString(Map.of("title", title, "author", author, "isbn", isbn));
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        latestResponse = restTemplate.postForEntity(BASE_URL + "/api/books", entity, String.class);
        Map<String, Object> book = objectMapper.readValue(latestResponse.getBody(), new TypeReference<>() {});
        lastCreatedBookId = (String) book.get("id");
        createdBookIds.add(lastCreatedBookId);
    }

    @When("the client retrieves all books")
    public void retrieveAllBooks() {
        latestResponse = restTemplate.getForEntity(BASE_URL + "/api/books", String.class);
    }

    @When("the client retrieves the book by id")
    public void retrieveBookById() throws Exception {
        latestResponse = restTemplate.getForEntity(BASE_URL + "/api/books/" + lastCreatedBookId, String.class);
        lastRetrievedBook = objectMapper.readValue(latestResponse.getBody(), new TypeReference<>() {});
    }

    @When("the client deletes the book")
    public void deleteBook() {
        latestResponse = restTemplate.exchange(BASE_URL + "/api/books/" + lastCreatedBookId,
                HttpMethod.DELETE, null, String.class);
    }

    @When("the client tries to retrieve the deleted book by id")
    public void tryRetrieveDeletedBook() {
        try {
            latestResponse = restTemplate.getForEntity(BASE_URL + "/api/books/" + lastCreatedBookId, String.class);
        } catch (HttpClientErrorException e) {
            latestResponse = ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    @Then("the book response status code is {int}")
    public void verifyBookStatusCode(int statusCode) {
        assertThat(latestResponse.getStatusCode().value(), is(statusCode));
    }

    @Then("the client can see at least {int} books")
    public void verifyBookCount(int minCount) throws Exception {
        List<Map<String, Object>> books = objectMapper.readValue(latestResponse.getBody(), new TypeReference<>() {});
        assertThat(books.size(), greaterThanOrEqualTo(minCount));
    }

    @Then("the book has title {string} and author {string}")
    public void verifyBookDetails(String title, String author) {
        assertThat(lastRetrievedBook.get("title"), is(title));
        assertThat(lastRetrievedBook.get("author"), is(author));
    }
}