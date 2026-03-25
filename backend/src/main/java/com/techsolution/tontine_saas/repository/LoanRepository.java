package com.techsolution.tontine_saas.repository;

import com.techsolution.tontine_saas.entities.Loan;
import com.techsolution.tontine_saas.entities.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUser_Id(Long userId);

    List<Loan> findByUser_Association_Id(Long associationId);

    long countByUser_Id(Long userId);

    @Query("""
        SELECT COALESCE(SUM(l.remainingAmount),0)
        FROM Loan l
        WHERE l.user.association.id = :associationId
        AND l.status IN :statuses
    """)
    BigDecimal sumRemainingAmountByAssociationId(
            @Param("associationId") Long associationId,
            @Param("statuses") List<LoanStatus> statuses
    );

    Long countByUser_Association_IdAndStatus(Long associationId, LoanStatus status);
}