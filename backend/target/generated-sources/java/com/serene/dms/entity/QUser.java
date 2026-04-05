package com.serene.dms.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 1103095245L;

    public static final QUser user = new QUser("user");

    public final QBaseAuditEntity _super = new QBaseAuditEntity(this);

    public final DateTimePath<java.time.LocalDateTime> accountExpiresAt = createDateTime("accountExpiresAt", java.time.LocalDateTime.class);

    public final BooleanPath accountLocked = createBoolean("accountLocked");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final DateTimePath<java.time.LocalDateTime> credentialsExpiresAt = createDateTime("credentialsExpiresAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final BooleanPath enabled = createBoolean("enabled");

    public final NumberPath<Integer> failedAttempts = createNumber("failedAttempts", Integer.class);

    public final StringPath firstName = createString("firstName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath lastName = createString("lastName");

    public final StringPath passwordHash = createString("passwordHash");

    public final StringPath phone = createString("phone");

    public final SetPath<Role, QRole> roles = this.<Role, QRole>createSet("roles", Role.class, QRole.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath updatedBy = _super.updatedBy;

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

