package com.techsolution.tontine_saas.repository;

import com.techsolution.tontine_saas.entities.Tontine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface TontineRepository extends JpaRepository<Tontine, Long> {

    List<Tontine> findByAssociationId(Long associationId);

    // Récupérer uniquement les tontines en cours d'une association
    List<Tontine> findByAssociationIdAndActiveTrue(Long associationId);

    long countByAssociationId(Long associationId);


    Optional<Tontine> findByIdAndAssociationId(Long id, Long associationId);

    boolean existsByIdAndAssociationId(Long tontineId, Long associationId);
}
