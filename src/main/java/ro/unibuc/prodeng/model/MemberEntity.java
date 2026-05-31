package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "members")
public record MemberEntity(
    @Id
    String id,
    String name,
    String email,
    List<String> borrowedBookIds
) {
    public MemberEntity(String name, String email) {
        this(null, name, email, new java.util.ArrayList<>());
    }
}