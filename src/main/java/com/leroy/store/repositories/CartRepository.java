package com.leroy.store.repositories;

import com.leroy.store.entities.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    @EntityGraph(attributePaths = {"items.product"})
    @Query("SELECT c FROM Cart c WHERE c.id = :id")
    Optional<Cart> findCartWithItems(@Param("id") UUID id);
}