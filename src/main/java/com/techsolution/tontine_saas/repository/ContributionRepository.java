package com.techsolution.tontine_saas.repository;

import com.techsolution.tontine_saas.entities.Contribution;
import com.techsolution.tontine_saas.entities.ContributionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public interface ContributionRepository extends JpaRepository<Contribution, Long> {

    // Standard et fonctionnel
    List<Contribution> findByMemberTontine_Tontine_Id(Long tontineId);

    List<Contribution> findByMemberTontineIdOrderByDueDateDesc(Long memberTontineId);

    List<Contribution> findByMemberTontineUserId(Long userId);

    // Filtrer les impayés (très utile pour une tontine)
    List<Contribution> findByMemberTontineTontineIdAndStatus(Long tontineId, ContributionStatus status);

    long countByMemberTontine_Tontine_Id(Long tontineId);

    // Correction de la requête : COALESCE est parfait ici pour éviter les NullPointerException
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Contribution c WHERE c.memberTontine.tontine.id = :tontineId AND c.status = 'PAID'")
    BigDecimal sumPaidAmountByTontineId(@Param("tontineId") Long tontineId);

    @Query("SELECT COALESCE(SUM(c.penalty), 0) FROM Contribution c WHERE c.memberTontine.tontine.id = :tontineId")
    BigDecimal sumTotalPenaltiesByTontineId(@Param("tontineId") Long tontineId);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Contribution c WHERE c.memberTontine.id = :memberId AND c.status = 'PAID'")
    BigDecimal sumByMemberTontineId(@Param("memberId") Long memberId);

}
