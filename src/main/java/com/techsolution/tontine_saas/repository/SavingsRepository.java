package com.techsolution.tontine_saas.repository;

import com.techsolution.tontine_saas.entities.Savings;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SavingsRepository extends JpaRepository<Savings, Long> {

    Optional<Savings> findByUserId(Long userId);

    List<Savings> findByUser_Association_Id(Long associationId);

    // Utile pour mettre à jour le solde de manière atomique
    @Modifying
    @Query("UPDATE Savings s SET s.balance = s.balance + :amount WHERE s.user.id = :userId")
    int addToBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Savings s WHERE s.user.id = :userId")
    Optional<Savings> findByUserIdWithLock(@Param("userId") Long userId);
}
