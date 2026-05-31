package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import ro.unibuc.prodeng.IntegrationTestBase;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.CreateUserRequest;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("UserController Integration Tests")
class UserControllerIntegrationTest extends IntegrationTestBase {

   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private UserRepository userRepository;

   @Autowired
   private ObjectMapper objectMapper;

   // clean database before each test
   @BeforeEach
   void cleanUp() {
      userRepository.deleteAll();
   }
   
   // tests go here
    private String createUser(String name, String email) throws Exception {
        CreateUserRequest request = new CreateUserRequest(name, email);

        String response = mockMvc.perform(post("/api/users")
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
    void testCreateAndGetUser_validUserCreation_retrievesUserSuccessfully() throws Exception {
        // Arrange
        String userId = createUser("Alice", "alice@example.com");

        // Act & Assert
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void testUpdateUserWithPut_validUserData_updatesUserSuccessfully() throws Exception {
        // Arrange
        String userId = createUser("Alice", "alice@example.com");

        // Act & Assert
        mockMvc.perform(put("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alicia\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alicia"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }
}