package com.techsolution.tontine_saas.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "savings_transactions")
public class SavingsTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    private String description;

    // 🔹 Nouveaux champs pour la traçabilité comptable
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal previousBalance;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfterTransaction;

    @Builder.Default
    private Boolean successful = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "savings_id", nullable = false)
    private Savings savings;

    @PrePersist
    public void prePersist() {
        if(this.createdAt == null){
            this.createdAt = LocalDateTime.now();
        }
        if (this.successful == null) {
            this.successful = true;
        }
    }
}