package com.techsolution.tontine_saas.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    // Ajout du montant restant à rembourser
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal remainingAmount;

    @Column(nullable = false)
    private Double interestRate;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Calcule le montant total initial (Capital + Intérêts calculés).
     * Utilise l'arrondi monétaire standard (HALF_UP).
     */
    public BigDecimal calculateInitialTotal() {
        if (this.amount == null) {
            return BigDecimal.ZERO;
        }

        if (this.interestRate == null || this.interestRate == 0) {
            return this.amount;
        }

        // Calcul : Montant * (Taux / 100)
        BigDecimal interest = this.amount.multiply(BigDecimal.valueOf(this.interestRate))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return this.amount.add(interest);
    }
}