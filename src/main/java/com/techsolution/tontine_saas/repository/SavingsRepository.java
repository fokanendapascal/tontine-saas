package com.techsolution.tontine_saas.repository;

import com.techsolution.tontine_saas.entities.Savings;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SavingsRepository extends JpaRepository<Savings, Long> {

    Optional<Savings> findByUser_Id(Long userId);

    List<Savings> findByUser_Association_Id(Long associationId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Savings s
        SET s.balance = s.balance + :amount
        WHERE s.user.id = :userId
    """)
    int addToBalance(
            @Param("userId") Long userId,
            @Param("amount") BigDecimal amount
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Savings s WHERE s.user.id = :userId")
    Optional<Savings> findByUserIdWithLock(@Param("userId") Long userId);

    @Query("""
        SELECT COALESCE(SUM(s.balance),0)
        FROM Savings s
        WHERE s.user.association.id = :associationId
    """)
    BigDecimal sumBalanceByAssociationId(@Param("associationId") Long associationId);
}