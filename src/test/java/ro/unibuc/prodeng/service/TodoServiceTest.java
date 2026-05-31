package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ro.unibuc.prodeng.model.TodoEntity;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.repository.TodoRepository;
import ro.unibuc.prodeng.request.CreateTodoRequest;
import ro.unibuc.prodeng.response.TodoResponse;
import ro.unibuc.prodeng.exception.EntityNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class TodoServiceTest {

   @Mock
   private TodoRepository todoRepository;

   @Mock
   private UserService userService;

   @InjectMocks
   private TodoService todoService;

   @Test
   void testGetTodoById_existingTodoRequested_returnsTodo() throws EntityNotFoundException {
      // Arrange
      UserEntity user = new UserEntity("1", "Alice", "alice@example.com");
      TodoEntity todo = new TodoEntity("todo-1", "Complete project", false, "1");

      when(todoRepository.findById("todo-1")).thenReturn(Optional.of(todo));
      when(userService.getUserEntityById("1")).thenReturn(user);

      // Act
      TodoResponse result = todoService.getTodoById("todo-1");

      // Assert
      assertNotNull(result);
      assertEquals("todo-1", result.id());
      assertEquals("Complete project", result.description());
      assertFalse(result.done());
      assertEquals("Alice", result.assigneeName());
      assertEquals("alice@example.com", result.assigneeEmail());
   }

   @Test
   void testGetTodoById_nonExistingTodoRequested_throwsEntityNotFoundException() {
      // Arrange
      when(todoRepository.findById("non-existing")).thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(EntityNotFoundException.class, () -> todoService.getTodoById("non-existing"));
   }
}