package ro.unibuc.prodeng.service;

import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.MemberEntity;
import ro.unibuc.prodeng.repository.MemberRepository;
import ro.unibuc.prodeng.request.CreateMemberRequest;
import ro.unibuc.prodeng.response.MemberResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<MemberResponse> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MemberResponse getMemberById(String id) {
        MemberEntity member = memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
        return toResponse(member);
    }

    public MemberResponse createMember(CreateMemberRequest request) {
        if (memberRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Member with email " + request.email() + " already exists");
        }
        MemberEntity member = new MemberEntity(request.name(), request.email());
        return toResponse(memberRepository.save(member));
    }

    public void deleteMember(String id) {
        if (!memberRepository.existsById(id)) {
            throw new EntityNotFoundException(id);
        }
        memberRepository.deleteById(id);
    }

    private MemberResponse toResponse(MemberEntity member) {
        return new MemberResponse(member.id(), member.name(), member.email(), member.borrowedBookIds());
    }
}