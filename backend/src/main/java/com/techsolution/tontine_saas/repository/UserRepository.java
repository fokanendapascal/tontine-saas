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

    @Query("""
    SELECT u FROM User u
    LEFT JOIN FETCH u.roles
    LEFT JOIN FETCH u.association
    WHERE u.email = :email
    """)
    Optional<User> findFullUserByEmail(@Param("email") String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndAssociationId(String email, Long associationId);

    List<User> findByAssociationId(Long associationId);

    long countByAssociationId(Long associationId);

    long countByAssociationIdAndActiveTrue(Long associationId);

}



