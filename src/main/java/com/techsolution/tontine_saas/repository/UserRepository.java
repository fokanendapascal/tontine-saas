package com.techsolution.tontine_saas.repository;

import com.techsolution.tontine_saas.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // Optimisation : Charge l'utilisateur et son association en une seule jointure
    @Query("SELECT u FROM User u JOIN FETCH u.association WHERE u.email = :email")
    Optional<User> findByEmailWithAssociation(@Param("email") String email);

    boolean existsByEmail(String email);

    List<User> findByAssociationId(Long associationId);

    long countByAssociationId(Long associationId);

    long countByAssociationIdAndActiveTrue(Long associationId);

}



