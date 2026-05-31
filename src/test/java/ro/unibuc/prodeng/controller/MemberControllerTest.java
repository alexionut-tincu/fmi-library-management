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
import ro.unibuc.prodeng.request.CreateMemberRequest;
import ro.unibuc.prodeng.response.MemberResponse;
import ro.unibuc.prodeng.service.MemberService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final MemberResponse member1 = new MemberResponse("1", "Alice Pop", "alice@example.com", Collections.emptyList());
    private final MemberResponse member2 = new MemberResponse("2", "Bob Ionescu", "bob@example.com", List.of("book-1"));

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberController).build();
    }

    // ── GET /api/members ─────────────────────────────────────────

    @Test
    void testGetAllMembers_withMembers_returnsList() throws Exception {
        when(memberService.getAllMembers()).thenReturn(Arrays.asList(member1, member2));

        mockMvc.perform(get("/api/members").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("1")))
                .andExpect(jsonPath("$[0].name", is("Alice Pop")))
                .andExpect(jsonPath("$[1].name", is("Bob Ionescu")));

        verify(memberService, times(1)).getAllMembers();
    }

    @Test
    void testGetAllMembers_noMembers_returnsEmptyList() throws Exception {
        when(memberService.getAllMembers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/members").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── GET /api/members/{id} ────────────────────────────────────

    @Test
    void testGetMemberById_existingMember_returnsMember() throws Exception {
        when(memberService.getMemberById("1")).thenReturn(member1);

        mockMvc.perform(get("/api/members/{id}", "1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.name", is("Alice Pop")))
                .andExpect(jsonPath("$.email", is("alice@example.com")));

        verify(memberService, times(1)).getMemberById("1");
    }

    @Test
    void testGetMemberById_nonExistingMember_returnsNotFound() throws Exception {
        when(memberService.getMemberById("non-existing")).thenThrow(new EntityNotFoundException("non-existing"));

        mockMvc.perform(get("/api/members/{id}", "non-existing").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/members ────────────────────────────────────────

    @Test
    void testCreateMember_validRequest_returnsCreated() throws Exception {
        CreateMemberRequest request = new CreateMemberRequest("Alice Pop", "alice@example.com");
        when(memberService.createMember(any(CreateMemberRequest.class))).thenReturn(member1);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.name", is("Alice Pop")))
                .andExpect(jsonPath("$.email", is("alice@example.com")));

        verify(memberService, times(1)).createMember(any(CreateMemberRequest.class));
    }

    @Test
    void testCreateMember_duplicateEmail_returnsBadRequest() throws Exception {
        CreateMemberRequest request = new CreateMemberRequest("Alice Pop", "alice@example.com");
        when(memberService.createMember(any(CreateMemberRequest.class)))
                .thenThrow(new IllegalArgumentException("Member with email alice@example.com already exists"));

        mockMvc = MockMvcBuilders.standaloneSetup(memberController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
        
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── DELETE /api/members/{id} ─────────────────────────────────

    @Test
    void testDeleteMember_existingMember_returnsNoContent() throws Exception {
        doNothing().when(memberService).deleteMember("1");

        mockMvc.perform(delete("/api/members/{id}", "1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(memberService, times(1)).deleteMember("1");
    }

    @Test
    void testDeleteMember_nonExistingMember_returnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException("non-existing")).when(memberService).deleteMember("non-existing");

        mockMvc.perform(delete("/api/members/{id}", "non-existing").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}