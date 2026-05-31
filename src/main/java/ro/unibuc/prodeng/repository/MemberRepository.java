package ro.unibuc.prodeng.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.prodeng.model.MemberEntity;
import java.util.Optional;

@Repository
public interface MemberRepository extends MongoRepository<MemberEntity, String> {
    Optional<MemberEntity> findByEmail(String email);
}