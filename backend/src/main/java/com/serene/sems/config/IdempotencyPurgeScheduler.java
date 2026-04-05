package com.serene.sems.config;

import com.serene.sems.config.properties.IdempotencyProperties;
import com.serene.sems.service.IdempotentRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyPurgeScheduler {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyPurgeScheduler.class);

    private final IdempotentRequestService idempotentRequestService;
    private final IdempotencyProperties idempotencyProperties;

    public IdempotencyPurgeScheduler(
            IdempotentRequestService idempotentRequestService, IdempotencyProperties idempotencyProperties) {
        this.idempotentRequestService = idempotentRequestService;
        this.idempotencyProperties = idempotencyProperties;
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void purgeExpired() {
        if (!idempotencyProperties.isEnabled()) {
            return;
        }
        int removed =
                idempotentRequestService.deleteOlderThan(
                        idempotentRequestService.cutoffForTtl(idempotencyProperties.getTtlHours()));
        if (removed > 0) {
            log.debug("Purged {} idempotent request replay row(s)", removed);
        }
    }
}
