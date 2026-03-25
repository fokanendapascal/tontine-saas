package com.techsolution.tontine_saas.repository;

import com.techsolution.tontine_saas.entities.SavingsTransaction;
import com.techsolution.tontine_saas.entities.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SavingsTransactionRepository extends JpaRepository<SavingsTransaction, Long> {

    List<SavingsTransaction> findBySavingsIdOrderByCreatedAtDesc(Long savingsId);

    /**
     * Calcule la balance réelle en fonction du type de transaction.
     * DEPOSIT : +
     * WITHDRAWAL : -
     * TRANSFER : (Généralement traité comme un débit dans le compte source,
     * mais cela dépend de votre logique métier. Ici, on le traite comme un débit.)
     */
    @Query("SELECT COALESCE(SUM(CASE " +
            "  WHEN t.type = 'DEPOSIT' THEN t.amount " +
            "  WHEN t.type = 'WITHDRAWAL' THEN -t.amount " +
            "  WHEN t.type = 'TRANSFER' THEN -t.amount " + // À adapter selon votre gestion des virements
            "  ELSE 0 END), 0) " +
            "FROM SavingsTransaction t WHERE t.savings.id = :savingsId")
    BigDecimal calculateBalance(@Param("savingsId") Long savingsId);

    // Pour filtrer par type (ex: voir uniquement les dépôts)
    List<SavingsTransaction> findBySavingsIdAndType(Long savingsId, TransactionType type);

    Long countByTypeAndSavingsId(TransactionType transactionType, Long id);
}

