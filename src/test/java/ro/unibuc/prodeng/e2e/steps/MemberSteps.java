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

public class MemberSteps {

    private static final String BASE_URL = "http://localhost:8080";

    private final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ResponseEntity<String> latestResponse;
    private final List<String> createdMemberIds = new ArrayList<>();
    private String lastCreatedMemberId;
    private Map<String, Object> lastRetrievedMember;

    @After
    public void cleanup() {
        for (String memberId : createdMemberIds) {
            try {
                restTemplate.delete(BASE_URL + "/api/members/" + memberId);
            } catch (Exception ignored) {}
        }
        createdMemberIds.clear();
    }

    @Given("a member named {string} with email {word} exists")
    public void createMemberForTest(String name, String email) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = objectMapper.writeValueAsString(Map.of("name", name, "email", email));
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL + "/api/members", entity, String.class);
        Map<String, Object> member = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
        lastCreatedMemberId = (String) member.get("id");
        createdMemberIds.add(lastCreatedMemberId);
    }

    @When("the client creates a member named {string} with email {word}")
    public void createMember(String name, String email) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = objectMapper.writeValueAsString(Map.of("name", name, "email", email));
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        latestResponse = restTemplate.postForEntity(BASE_URL + "/api/members", entity, String.class);
        Map<String, Object> member = objectMapper.readValue(latestResponse.getBody(), new TypeReference<>() {});
        lastCreatedMemberId = (String) member.get("id");
        createdMemberIds.add(lastCreatedMemberId);
    }

    @When("the client tries to create a member named {string} with email {word}")
    public void tryCreateMember(String name, String email) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = objectMapper.writeValueAsString(Map.of("name", name, "email", email));
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            latestResponse = restTemplate.postForEntity(BASE_URL + "/api/members", entity, String.class);
        } catch (HttpClientErrorException e) {
            latestResponse = ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    @When("the client retrieves all members")
    public void retrieveAllMembers() {
        latestResponse = restTemplate.getForEntity(BASE_URL + "/api/members", String.class);
    }

    @When("the client retrieves the member by id")
    public void retrieveMemberById() throws Exception {
        latestResponse = restTemplate.getForEntity(BASE_URL + "/api/members/" + lastCreatedMemberId, String.class);
        lastRetrievedMember = objectMapper.readValue(latestResponse.getBody(), new TypeReference<>() {});
    }

    @When("the client deletes the member")
    public void deleteMember() {
        latestResponse = restTemplate.exchange(BASE_URL + "/api/members/" + lastCreatedMemberId,
                HttpMethod.DELETE, null, String.class);
    }

    @When("the client tries to retrieve the deleted member by id")
    public void tryRetrieveDeletedMember() {
        try {
            latestResponse = restTemplate.getForEntity(BASE_URL + "/api/members/" + lastCreatedMemberId, String.class);
        } catch (HttpClientErrorException e) {
            latestResponse = ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    @Then("the member response status code is {int}")
    public void verifyMemberStatusCode(int statusCode) {
        assertThat(latestResponse.getStatusCode().value(), is(statusCode));
    }

    @Then("the client can see at least {int} members")
    public void verifyMemberCount(int minCount) throws Exception {
        List<Map<String, Object>> members = objectMapper.readValue(latestResponse.getBody(), new TypeReference<>() {});
        assertThat(members.size(), greaterThanOrEqualTo(minCount));
    }

    @Then("the member has name {string} and email {word}")
    public void verifyMemberDetails(String name, String email) {
        assertThat(lastRetrievedMember.get("name"), is(name));
        assertThat(lastRetrievedMember.get("email"), is(email));
    }
}