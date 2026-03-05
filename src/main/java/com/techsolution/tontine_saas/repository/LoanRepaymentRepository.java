package com.techsolution.tontine_saas.repository;

import com.techsolution.tontine_saas.entities.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Repository
public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, Long> {

    List<LoanRepayment> findByLoanId(Long loanId);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM LoanRepayment r WHERE r.loan.id = :loanId")
    BigDecimal sumAmountByLoanId(@Param("loanId") Long loanId);

    // Optionnel : Une méthode pour calculer le total payé (montant + pénalités)
    @Query("SELECT COALESCE(SUM(r.amount + r.penalty), 0) FROM LoanRepayment r WHERE r.loan.id = :loanId")
    BigDecimal totalRepaidByLoanId(@Param("loanId") Long loanId);

}
