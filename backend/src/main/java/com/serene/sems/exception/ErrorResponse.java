package com.serene.sems.exception;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class ErrorResponse {

    private Instant timestamp;
    private int status;
    private String message;
    private String path;
    private String traceId;
    private Map<String, String> fieldErrors;

    public ErrorResponse() {
    }

    public static ErrorResponse of(int status, String message, String path, Map<String, String> fieldErrors) {
        ErrorResponse e = new ErrorResponse();
        e.timestamp = Instant.now();
        e.status = status;
        e.message = message;
        e.path = path;
        e.traceId = UUID.randomUUID().toString();
        e.fieldErrors = fieldErrors;
        return e;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}
