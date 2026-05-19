package com.leroy.store.repositories;

import com.leroy.store.entities.Order;
import com.leroy.store.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = {"items.product"})
    @Query("SELECT o FROM Order o WHERE o.customer = :customer")
    List<Order> getAllWithItemsByCustomer(@Param("customer") User customer);

    @EntityGraph(attributePaths = {"items.product"})
    @Query("SELECT o FROM Order o WHERE o.id = :orderId")
    Optional<Order> getOrderWithItems(UUID orderId);
}