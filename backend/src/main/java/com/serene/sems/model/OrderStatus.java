package com.serene.sems.model;

import java.util.Map;
import java.util.Set;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            PENDING, Set.of(CONFIRMED, CANCELLED),
            CONFIRMED, Set.of(SHIPPED, CANCELLED),
            SHIPPED, Set.of(DELIVERED),
            DELIVERED, Set.of(),
            CANCELLED, Set.of()
    );

    /**
     * Returns the set of statuses that are reachable from {@code this} status.
     */
    public Set<OrderStatus> allowedTransitions() {
        return TRANSITIONS.getOrDefault(this, Set.of());
    }

    /**
     * Validates that moving from {@code this} to {@code target} is a legal transition.
     *
     * @throws IllegalArgumentException if the transition is not allowed
     */
    public void validateTransitionTo(OrderStatus target) {
        if (!allowedTransitions().contains(target)) {
            throw new IllegalArgumentException(
                    "Cannot transition order from " + this + " to " + target
                            + ". Allowed transitions: " + allowedTransitions());
        }
    }
}
