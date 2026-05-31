package ro.unibuc.prodeng.response;

import java.util.List;

public record MemberResponse(
    String id,
    String name,
    String email,
    List<String> borrowedBookIds
) {}