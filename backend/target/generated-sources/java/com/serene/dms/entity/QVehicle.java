package com.serene.dms.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QVehicle is a Querydsl query type for Vehicle
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QVehicle extends EntityPathBase<Vehicle> {

    private static final long serialVersionUID = 2004958698L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QVehicle vehicle = new QVehicle("vehicle");

    public final QBaseAuditEntity _super = new QBaseAuditEntity(this);

    public final StringPath color = createString("color");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    protected QDealer dealer;

    public final StringPath description = createString("description");

    public final StringPath fuelType = createString("fuelType");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath mileage = createString("mileage");

    public final StringPath model = createString("model");

    public final NumberPath<java.math.BigDecimal> price = createNumber("price", java.math.BigDecimal.class);

    public final EnumPath<Vehicle.VehicleStatus> status = createEnum("status", Vehicle.VehicleStatus.class);

    public final StringPath transmission = createString("transmission");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath updatedBy = _super.updatedBy;

    public final StringPath variant = createString("variant");

    public final StringPath vin = createString("vin");

    public final NumberPath<Integer> year = createNumber("year", Integer.class);

    public QVehicle(String variable) {
        this(Vehicle.class, forVariable(variable), INITS);
    }

    public QVehicle(Path<? extends Vehicle> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QVehicle(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QVehicle(PathMetadata metadata, PathInits inits) {
        this(Vehicle.class, metadata, inits);
    }

    public QVehicle(Class<? extends Vehicle> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.dealer = inits.isInitialized("dealer") ? new QDealer(forProperty("dealer"), inits.get("dealer")) : null;
    }

    public QDealer dealer() {
        if (dealer == null) {
            dealer = new QDealer(forProperty("dealer"));
        }
        return dealer;
    }

}

