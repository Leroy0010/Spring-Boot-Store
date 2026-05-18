package com.leroy.store.repositories;

import com.leroy.store.entities.Product;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    @Query("select p from Product p where p.category.id = :categoryId")
    @EntityGraph(attributePaths = {"category"})
    List<Product> findAllByCategory_Id(@Param("categoryId") UUID categoryId);

    @EntityGraph(attributePaths = {"category"})
    List<Product> findAll();

    @EntityGraph(attributePaths = {"category"})
    Optional<Product> findById(@Param("id") @NonNull UUID id);
}
