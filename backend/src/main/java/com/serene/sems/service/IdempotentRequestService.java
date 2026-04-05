package com.serene.sems.service;

import com.serene.sems.model.IdempotentRequest;
import com.serene.sems.repository.IdempotentRequestRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class IdempotentRequestService {

    private final IdempotentRequestRepository repository;

    public IdempotentRequestService(IdempotentRequestRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<IdempotentRequest> find(String idempotencyKey, String principalName, String pathFingerprint) {
        return repository.findByIdempotencyKeyAndPrincipalNameAndPathFingerprint(
                idempotencyKey, principalName, pathFingerprint);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void saveReplay(
            String idempotencyKey,
            String principalName,
            String pathFingerprint,
            String requestBodyHash,
            int httpStatus,
            String contentType,
            byte[] responseBody) {
        IdempotentRequest row = new IdempotentRequest();
        row.setIdempotencyKey(idempotencyKey);
        row.setPrincipalName(principalName);
        row.setPathFingerprint(pathFingerprint);
        row.setRequestBodyHash(requestBodyHash);
        row.setHttpStatus(httpStatus);
        row.setContentType(contentType != null ? contentType : "application/json");
        row.setResponseBody(responseBody);
        row.setRecordedAt(Instant.now());
        try {
            repository.save(row);
        } catch (DataIntegrityViolationException ignored) {
            // Concurrent duplicate POST with same key — another thread stored first.
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int deleteOlderThan(Instant cutoff) {
        return repository.deleteByRecordedAtBefore(cutoff);
    }

    public Instant cutoffForTtl(int ttlHours) {
        return Instant.now().minus(ttlHours, ChronoUnit.HOURS);
    }
}
