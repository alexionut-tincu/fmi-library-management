package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ro.unibuc.prodeng.IntegrationTestBase;
import ro.unibuc.prodeng.repository.MemberRepository;
import ro.unibuc.prodeng.request.CreateMemberRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("MemberController Integration Tests")
class MemberControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        memberRepository.deleteAll();
    }

    // Helper: creates a member and returns its ID
    private String createMember(String name, String email) throws Exception {
        CreateMemberRequest request = new CreateMemberRequest(name, email);

        String response = mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void testCreateAndGetMember_validMember_retrievesMemberSuccessfully() throws Exception {
        // Arrange
        String memberId = createMember("Alice", "alice@library.com");

        // Act & Assert
        mockMvc.perform(get("/api/members/" + memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@library.com"));
    }

    @Test
    void testGetAllMembers_multipleMembersExist_returnsAllMembers() throws Exception {
        // Arrange
        createMember("Alice", "alice@library.com");
        createMember("Bob", "bob@library.com");

        // Act & Assert
        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testDeleteMember_existingMember_deletesSuccessfully() throws Exception {
        // Arrange
        String memberId = createMember("Alice", "alice@library.com");

        // Act & Assert
        mockMvc.perform(delete("/api/members/" + memberId))
                .andExpect(status().isNoContent());

        // Verify DB state
        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetMember_nonExistentId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/members/nonexistentid456"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateMember_duplicateEmail_returnsBadRequest() throws Exception {
        // Arrange
        createMember("Alice", "alice@library.com");

        // Act & Assert — same email should fail
        CreateMemberRequest duplicate = new CreateMemberRequest("Alicia", "alice@library.com");
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest());
    }
}