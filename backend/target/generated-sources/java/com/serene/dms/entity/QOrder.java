package com.serene.dms.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrder is a Querydsl query type for Order
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrder extends EntityPathBase<Order> {

    private static final long serialVersionUID = -169357940L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrder order = new QOrder("order1");

    public final QBaseAuditEntity _super = new QBaseAuditEntity(this);

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    protected QCustomer customer;

    protected QDealer dealer;

    public final NumberPath<java.math.BigDecimal> discount = createNumber("discount", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> finalAmount = createNumber("finalAmount", java.math.BigDecimal.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath notes = createString("notes");

    public final StringPath orderNumber = createString("orderNumber");

    public final EnumPath<Order.OrderStatus> status = createEnum("status", Order.OrderStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath updatedBy = _super.updatedBy;

    protected QVehicle vehicle;

    public QOrder(String variable) {
        this(Order.class, forVariable(variable), INITS);
    }

    public QOrder(Path<? extends Order> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrder(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrder(PathMetadata metadata, PathInits inits) {
        this(Order.class, metadata, inits);
    }

    public QOrder(Class<? extends Order> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new QCustomer(forProperty("customer"), inits.get("customer")) : null;
        this.dealer = inits.isInitialized("dealer") ? new QDealer(forProperty("dealer"), inits.get("dealer")) : null;
        this.vehicle = inits.isInitialized("vehicle") ? new QVehicle(forProperty("vehicle"), inits.get("vehicle")) : null;
    }

    public QCustomer customer() {
        if (customer == null) {
            customer = new QCustomer(forProperty("customer"));
        }
        return customer;
    }

    public QDealer dealer() {
        if (dealer == null) {
            dealer = new QDealer(forProperty("dealer"));
        }
        return dealer;
    }

    public QVehicle vehicle() {
        if (vehicle == null) {
            vehicle = new QVehicle(forProperty("vehicle"));
        }
        return vehicle;
    }

}

