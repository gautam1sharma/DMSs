package com.serene.dms.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDealer is a Querydsl query type for Dealer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDealer extends EntityPathBase<Dealer> {

    private static final long serialVersionUID = -1282138213L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDealer dealer = new QDealer("dealer");

    public final QBaseAuditEntity _super = new QBaseAuditEntity(this);

    public final StringPath address = createString("address");

    public final StringPath city = createString("city");

    public final StringPath code = createString("code");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath phone = createString("phone");

    public final StringPath state = createString("state");

    public final EnumPath<Dealer.DealerStatus> status = createEnum("status", Dealer.DealerStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath updatedBy = _super.updatedBy;

    protected QUser user;

    public QDealer(String variable) {
        this(Dealer.class, forVariable(variable), INITS);
    }

    public QDealer(Path<? extends Dealer> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDealer(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDealer(PathMetadata metadata, PathInits inits) {
        this(Dealer.class, metadata, inits);
    }

    public QDealer(Class<? extends Dealer> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

    public QUser user() {
        if (user == null) {
            user = new QUser(forProperty("user"));
        }
        return user;
    }

}

