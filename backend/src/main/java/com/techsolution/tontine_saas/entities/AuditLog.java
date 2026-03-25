package com.techsolution.tontine_saas.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String action; // ex: "CREATE_LOAN", "UPDATE_SAVINGS"

    @Column(nullable = false, updatable = false)
    private String entityName; // ex: "Loan", "User"

    @Column(nullable = false, updatable = false)
    private Long entityId;

    @Column(nullable = false, updatable = false)
    private Long performedBy; // ID de l'utilisateur ayant fait l'action

    @Column(nullable = false, updatable = false)
    private LocalDateTime performedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "association_id", nullable = false, updatable = false)
    private Association association;

    @PrePersist
    public void prePersist() {
        if(this.performedAt == null) {
            this.performedAt = LocalDateTime.now();
        }
    }
}
