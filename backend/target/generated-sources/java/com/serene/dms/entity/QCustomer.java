package com.serene.dms.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCustomer is a Querydsl query type for Customer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCustomer extends EntityPathBase<Customer> {

    private static final long serialVersionUID = 616147168L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCustomer customer = new QCustomer("customer");

    public final QBaseAuditEntity _super = new QBaseAuditEntity(this);

    public final StringPath address = createString("address");

    public final StringPath city = createString("city");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final DatePath<java.time.LocalDate> dateOfBirth = createDate("dateOfBirth", java.time.LocalDate.class);

    protected QDealer dealer;

    public final StringPath email = createString("email");

    public final StringPath firstName = createString("firstName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath lastName = createString("lastName");

    public final StringPath notes = createString("notes");

    public final StringPath phone = createString("phone");

    public final StringPath state = createString("state");

    public final EnumPath<Customer.CustomerStatus> status = createEnum("status", Customer.CustomerStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath updatedBy = _super.updatedBy;

    protected QUser user;

    public QCustomer(String variable) {
        this(Customer.class, forVariable(variable), INITS);
    }

    public QCustomer(Path<? extends Customer> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCustomer(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCustomer(PathMetadata metadata, PathInits inits) {
        this(Customer.class, metadata, inits);
    }

    public QCustomer(Class<? extends Customer> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.dealer = inits.isInitialized("dealer") ? new QDealer(forProperty("dealer"), inits.get("dealer")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

    public QDealer dealer() {
        if (dealer == null) {
            dealer = new QDealer(forProperty("dealer"));
        }
        return dealer;
    }

    public QUser user() {
        if (user == null) {
            user = new QUser(forProperty("user"));
        }
        return user;
    }

}

