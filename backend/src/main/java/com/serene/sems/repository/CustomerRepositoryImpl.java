package com.serene.sems.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.serene.sems.model.Customer;
import com.serene.sems.model.QCustomer;
import com.serene.sems.model.QDealer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CustomerRepositoryImpl implements CustomerRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public CustomerRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<Customer> findCustomers(Long dealerId, String nameQuery, Pageable pageable) {
        QCustomer customer = QCustomer.customer;
        QDealer dealer = QDealer.dealer;

        BooleanBuilder where = new BooleanBuilder();
        if (dealerId != null) {
            where.and(customer.dealer.id.eq(dealerId));
        }
        if (nameQuery != null && !nameQuery.isBlank()) {
            where.and(customer.fullName.containsIgnoreCase(nameQuery.trim()));
        }

        JPAQuery<Customer> dataQuery = queryFactory
                .selectFrom(customer)
                .leftJoin(customer.dealer, dealer)
                .where(where)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        for (OrderSpecifier<?> spec : orderSpecifiers(customer, dealer, pageable.getSort())) {
            dataQuery.orderBy(spec);
        }

        List<Customer> content = dataQuery.fetch();

        Long total = queryFactory
                .select(customer.count())
                .from(customer)
                .where(where)
                .fetchOne();
        long totalCount = total == null ? 0L : total;

        return new PageImpl<>(content, pageable, totalCount);
    }

    private List<OrderSpecifier<?>> orderSpecifiers(QCustomer c, QDealer d, Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        if (sort == null || sort.isEmpty()) {
            orders.add(c.id.asc());
            return orders;
        }
        for (Sort.Order o : sort) {
            Order dir = o.isAscending() ? Order.ASC : Order.DESC;
            switch (o.getProperty()) {
                case "fullName" -> orders.add(new OrderSpecifier<>(dir, c.fullName));
                case "city" -> orders.add(new OrderSpecifier<>(dir, c.city));
                case "phone" -> orders.add(new OrderSpecifier<>(dir, c.phone));
                case "active" -> orders.add(new OrderSpecifier<>(dir, c.active));
                case "id" -> orders.add(new OrderSpecifier<>(dir, c.id));
                case "createdAt" -> orders.add(new OrderSpecifier<>(dir, c.createdAt));
                case "dealer.companyName" -> orders.add(new OrderSpecifier<>(dir, d.companyName));
                default -> orders.add(c.id.asc());
            }
        }
        return orders;
    }
}
