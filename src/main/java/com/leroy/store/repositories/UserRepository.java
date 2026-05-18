package com.leroy.store.repositories;

import com.leroy.store.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @EntityGraph(attributePaths = {"addresses", "wishlist", "profile"})
    List<User> findAll();

    boolean existsByEmail(String email);

    @Query("select u from User u where upper(u.email) = upper(:email)")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);
}
