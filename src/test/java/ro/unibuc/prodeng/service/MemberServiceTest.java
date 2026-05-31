package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.MemberEntity;
import ro.unibuc.prodeng.repository.MemberRepository;
import ro.unibuc.prodeng.request.CreateMemberRequest;
import ro.unibuc.prodeng.response.MemberResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private final MemberEntity member1 = new MemberEntity("1", "Alice Pop", "alice@example.com", Collections.emptyList());
    private final MemberEntity member2 = new MemberEntity("2", "Bob Ionescu", "bob@example.com", List.of("book-1"));

    // ── getAllMembers ────────────────────────────────────────────

    @Test
    void testGetAllMembers_withMultipleMembers_returnsAllMembers() {
        when(memberRepository.findAll()).thenReturn(Arrays.asList(member1, member2));

        List<MemberResponse> result = memberService.getAllMembers();

        assertEquals(2, result.size());
        assertEquals("Alice Pop", result.get(0).name());
        assertEquals("Bob Ionescu", result.get(1).name());
    }

    @Test
    void testGetAllMembers_noMembers_returnsEmptyList() {
        when(memberRepository.findAll()).thenReturn(Collections.emptyList());

        assertTrue(memberService.getAllMembers().isEmpty());
    }

    // ── getMemberById ────────────────────────────────────────────

    @Test
    void testGetMemberById_existingMember_returnsMember() {
        when(memberRepository.findById("1")).thenReturn(Optional.of(member1));

        MemberResponse result = memberService.getMemberById("1");

        assertNotNull(result);
        assertEquals("1", result.id());
        assertEquals("Alice Pop", result.name());
        assertEquals("alice@example.com", result.email());
        assertTrue(result.borrowedBookIds().isEmpty());
    }

    @Test
    void testGetMemberById_nonExistingMember_throwsEntityNotFoundException() {
        when(memberRepository.findById("non-existing")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> memberService.getMemberById("non-existing"));
    }

    // ── createMember ─────────────────────────────────────────────

    @Test
    void testCreateMember_validRequest_createsAndReturnsMember() {
        CreateMemberRequest request = new CreateMemberRequest("Alice Pop", "alice@example.com");

        when(memberRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(memberRepository.save(any(MemberEntity.class))).thenAnswer(invocation -> {
            MemberEntity m = invocation.getArgument(0);
            return new MemberEntity("generated-id", m.name(), m.email(), m.borrowedBookIds());
        });

        MemberResponse result = memberService.createMember(request);

        assertNotNull(result);
        assertEquals("Alice Pop", result.name());
        assertEquals("alice@example.com", result.email());
        assertTrue(result.borrowedBookIds().isEmpty());
        verify(memberRepository, times(1)).save(any(MemberEntity.class));
    }

    @Test
    void testCreateMember_duplicateEmail_throwsIllegalArgumentException() {
        CreateMemberRequest request = new CreateMemberRequest("Alice Pop", "alice@example.com");
        when(memberRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(member1));

        assertThrows(IllegalArgumentException.class, () -> memberService.createMember(request));
        verify(memberRepository, never()).save(any());
    }

    // ── deleteMember ─────────────────────────────────────────────

    @Test
    void testDeleteMember_existingMember_deletesSuccessfully() {
        when(memberRepository.existsById("1")).thenReturn(true);
        doNothing().when(memberRepository).deleteById("1");

        memberService.deleteMember("1");

        verify(memberRepository, times(1)).deleteById("1");
    }

    @Test
    void testDeleteMember_nonExistingMember_throwsEntityNotFoundException() {
        when(memberRepository.existsById("non-existing")).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> memberService.deleteMember("non-existing"));
        verify(memberRepository, never()).deleteById(anyString());
    }
}