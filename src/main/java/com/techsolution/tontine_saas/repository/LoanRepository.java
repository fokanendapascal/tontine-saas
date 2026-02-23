package com.techsolution.tontine_saas.repository;

import com.techsolution.tontine_saas.entities.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    // Spring Data comprend parfaitement "UserId" sans le underscore si la propriété est "user"
    List<Loan> findByUserId(Long userId);

    // Pour les relations imbriquées (User -> Association), le underscore est une bonne pratique
    List<Loan> findByUser_Association_Id(Long associationId);

    long countByUserId(Long userId);

}
