package com.serene.sems.model;

public enum AuditAction {
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LOGOUT,
    REGISTER_DEALER,
    ORDER_CREATED,
    ORDER_STATUS_CHANGED,
    CUSTOMER_CREATED,
    CUSTOMER_UPDATED,
    CUSTOMER_DELETED,
    PRODUCT_CREATED,
    PRODUCT_UPDATED,
    PRODUCT_DELETED,
    DEALER_CREATED,
    DEALER_UPDATED,
    DEALER_DELETED,
    DEALER_PROFILE_UPDATED,
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED,
    USER_UNLOCKED,

    /** Admin REST call (method, path, status on the audit row). */
    ADMIN_API_REQUEST,

    /** Dealer REST call (method, path, status on the audit row). */
    DEALER_API_REQUEST
}
