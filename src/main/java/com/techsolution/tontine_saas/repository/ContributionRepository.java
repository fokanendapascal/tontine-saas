package com.techsolution.tontine_saas.repository;

import com.techsolution.tontine_saas.entities.Contribution;
import com.techsolution.tontine_saas.entities.ContributionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, Long> {

    List<Contribution> findByMemberTontine_Tontine_Id(Long tontineId);

    List<Contribution> findByMemberTontine_User_Id(Long userId);

    List<Contribution> findByMemberTontine_Tontine_IdAndStatus(Long tontineId, ContributionStatus status);

    long countByMemberTontine_Tontine_Id(Long tontineId);

    @Query("""
        SELECT COALESCE(SUM(c.amount),0)
        FROM Contribution c
        WHERE c.memberTontine.tontine.id = :tontineId
        AND c.status = :status
    """)
    BigDecimal sumPaidAmountByTontineId(
            @Param("tontineId") Long tontineId,
            @Param("status") ContributionStatus status
    );

    @Query("""
        SELECT COALESCE(SUM(c.penalty),0)
        FROM Contribution c
        WHERE c.memberTontine.tontine.id = :tontineId
    """)
    BigDecimal sumTotalPenaltiesByTontineId(@Param("tontineId") Long tontineId);

    int countByMemberTontine_Id(Long id);

    @Query("""
        SELECT COALESCE(SUM(c.amount),0)
        FROM Contribution c
        WHERE c.memberTontine.tontine.association.id = :assocId
        AND c.status = :status
    """)
    BigDecimal sumAmountByAssociationAndStatus(
            @Param("assocId") Long assocId,
            @Param("status") ContributionStatus status
    );

    List<Contribution> findByMemberTontine_IdOrderByDueDateDesc(Long memberTontineId);

    @Query("""
        SELECT COALESCE(SUM(c.amount),0)
        FROM Contribution c
        WHERE c.memberTontine.id = :memberTontineId
        AND c.status = :status
    """)
    BigDecimal sumByMemberTontineId(
            @Param("memberTontineId") Long memberTontineId,
            @Param("status") ContributionStatus status
    );
}