package com.techsolution.tontine_saas.repository;

import com.techsolution.tontine_saas.entities.MemberTontine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberTontineRepository extends JpaRepository<MemberTontine, Long> {

    List<MemberTontine> findByTontineId(Long tontineId);

    List<MemberTontine> findByUserId(Long userId);

    // Utile pour récupérer l'adhésion précise d'un membre à une tontine
    Optional<MemberTontine> findByUserIdAndTontineId(Long userId, Long tontineId);

    boolean existsByUserIdAndTontineId(Long userId, Long tontineId);

    long countByTontineId(Long tontineId);
}
