package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.request.AssignTodoRequest;
import ro.unibuc.prodeng.request.CreateTodoRequest;
import ro.unibuc.prodeng.request.EditTodoRequest;
import ro.unibuc.prodeng.response.TodoResponse;
import ro.unibuc.prodeng.service.TodoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
class TodoControllerTest {

   @Mock
   private TodoService todoService;

   @InjectMocks
   private TodoController todoController;

   private MockMvc mockMvc;

   private ObjectMapper objectMapper = new ObjectMapper();

   private TodoResponse testTodo1 = new TodoResponse("todo-1", "Complete project", false, "Alice", "alice@example.com");
   private TodoResponse testTodo2 = new TodoResponse("todo-2", "Write documentation", true, "Alice", "alice@example.com");
   private CreateTodoRequest createTodoRequest = new CreateTodoRequest("Complete project", "alice@example.com");
   private AssignTodoRequest assignTodoRequest = new AssignTodoRequest("bob@example.com");
   private EditTodoRequest editTodoRequest = new EditTodoRequest("Updated description");

   @BeforeEach
   void setUp() {
      mockMvc = MockMvcBuilders.standaloneSetup(todoController).build();
   }

   @Test
   void testGetTodosByUserEmail_withMultipleTodos_returnsList() throws Exception {
      // Arrange
      List<TodoResponse> todos = Arrays.asList(testTodo1, testTodo2);
      when(todoService.getTodosByUserEmail("alice@example.com")).thenReturn(todos);

      // Act & Assert
      mockMvc.perform(get("/api/todos")
                      .param("assigneeEmail", "alice@example.com")
                      .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$", hasSize(2)))
              .andExpect(jsonPath("$[0].id", is("todo-1")))
              .andExpect(jsonPath("$[0].description", is("Complete project")))
              .andExpect(jsonPath("$[0].done", is(false)))
              .andExpect(jsonPath("$[0].assigneeName", is("Alice")))
              .andExpect(jsonPath("$[0].assigneeEmail", is("alice@example.com")))
              .andExpect(jsonPath("$[1].id", is("todo-2")))
              .andExpect(jsonPath("$[1].description", is("Write documentation")))
              .andExpect(jsonPath("$[1].done", is(true)))
              .andExpect(jsonPath("$[1].assigneeName", is("Alice")))
              .andExpect(jsonPath("$[1].assigneeEmail", is("alice@example.com")));

      verify(todoService, times(1)).getTodosByUserEmail("alice@example.com");
   }

   @Test
   void testGetTodosByUserEmail_withNoTodos_returnsEmptyList() throws Exception {
      // Arrange
      when(todoService.getTodosByUserEmail("alice@example.com")).thenReturn(Arrays.asList());

      // Act & Assert
      mockMvc.perform(get("/api/todos")
                      .param("assigneeEmail", "alice@example.com")
                      .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$", hasSize(0)));

      verify(todoService, times(1)).getTodosByUserEmail("alice@example.com");
   }
   
}
   
   // Further tests go here