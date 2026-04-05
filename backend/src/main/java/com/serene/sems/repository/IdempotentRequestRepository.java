package com.serene.sems.repository;

import com.serene.sems.model.IdempotentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface IdempotentRequestRepository extends JpaRepository<IdempotentRequest, Long> {

    Optional<IdempotentRequest> findByIdempotencyKeyAndPrincipalNameAndPathFingerprint(
            String idempotencyKey, String principalName, String pathFingerprint);

    @Modifying
    @Query("DELETE FROM IdempotentRequest r WHERE r.recordedAt < :cutoff")
    int deleteByRecordedAtBefore(@Param("cutoff") Instant cutoff);
}
