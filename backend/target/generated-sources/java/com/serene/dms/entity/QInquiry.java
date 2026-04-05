package com.serene.dms.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInquiry is a Querydsl query type for Inquiry
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInquiry extends EntityPathBase<Inquiry> {

    private static final long serialVersionUID = -676317051L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInquiry inquiry = new QInquiry("inquiry");

    public final QBaseAuditEntity _super = new QBaseAuditEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    protected QCustomer customer;

    protected QDealer dealer;

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath message = createString("message");

    public final StringPath name = createString("name");

    public final StringPath phone = createString("phone");

    public final StringPath response = createString("response");

    public final EnumPath<Inquiry.InquiryStatus> status = createEnum("status", Inquiry.InquiryStatus.class);

    public final StringPath subject = createString("subject");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath updatedBy = _super.updatedBy;

    protected QVehicle vehicle;

    public QInquiry(String variable) {
        this(Inquiry.class, forVariable(variable), INITS);
    }

    public QInquiry(Path<? extends Inquiry> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInquiry(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInquiry(PathMetadata metadata, PathInits inits) {
        this(Inquiry.class, metadata, inits);
    }

    public QInquiry(Class<? extends Inquiry> type, PathMetadata metadata, PathInits inits) {
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

