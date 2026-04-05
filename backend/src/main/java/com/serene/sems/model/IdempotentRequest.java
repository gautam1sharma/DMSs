package com.serene.sems.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * Stores successful POST responses for {@code Idempotency-Key} replay (same key + principal + path).
 */
@Entity
@Table(
        name = "idempotent_requests",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_idem_principal_path",
                columnNames = {"idempotency_key", "principal_name", "path_fingerprint"}),
        indexes = @Index(name = "idx_idem_created", columnList = "recorded_at"))
public class IdempotentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;

    @Column(name = "principal_name", nullable = false, length = 128)
    private String principalName;

    @Column(name = "path_fingerprint", nullable = false, length = 64)
    private String pathFingerprint;

    @Column(name = "request_body_hash", nullable = false, length = 64)
    private String requestBodyHash;

    @Column(name = "http_status", nullable = false)
    private int httpStatus;

    @Column(name = "content_type", length = 255)
    private String contentType;

    @Lob
    @Column(name = "response_body", nullable = false)
    private byte[] responseBody;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt = Instant.now();

    public Long getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getPathFingerprint() {
        return pathFingerprint;
    }

    public void setPathFingerprint(String pathFingerprint) {
        this.pathFingerprint = pathFingerprint;
    }

    public String getRequestBodyHash() {
        return requestBodyHash;
    }

    public void setRequestBodyHash(String requestBodyHash) {
        this.requestBodyHash = requestBodyHash;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(byte[] responseBody) {
        this.responseBody = responseBody;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }
}
