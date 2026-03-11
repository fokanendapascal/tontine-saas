package com.techsolution.tontine_saas.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "contributions")
public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 4) // Précision financière standard
    private BigDecimal amount;

    private LocalDateTime dueDate;
    private LocalDateTime paymentDate;

    @Builder.Default // Indispensable pour que le Builder respecte la valeur par défaut
    @Column(nullable = false)
    private BigDecimal penalty = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContributionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_tontine_id", nullable = false)
    private MemberTontine memberTontine;

    // Aide à la cohérence : mise à jour auto du statut si payé
    public void markAsPaid(BigDecimal penaltyAmount) {
        this.paymentDate = LocalDateTime.now();
        this.penalty = penaltyAmount != null ? penaltyAmount : BigDecimal.ZERO;
        this.status = ContributionStatus.PAID;
    }
}