package com.techsolution.tontine_saas.repository;

import com.techsolution.tontine_saas.entities.Tontine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TontineRepository extends JpaRepository<Tontine, Long> {

    List<Tontine> findByAssociationId(Long associationId);

    // Récupérer uniquement les tontines en cours d'une association
    List<Tontine> findByAssociationIdAndActiveTrue(Long associationId);

    long countByAssociationId(Long associationId);
}
