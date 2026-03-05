package com.techsolution.tontine_saas.repository;

import com.techsolution.tontine_saas.entities.Association;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssociationRepository extends JpaRepository<Association, Long> {

    // Utilisation de IgnoreCase pour éviter "Ma Tontine" vs "ma tontine"
    Optional<Association> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    // Retourne le nombre d'associations actives
    long countByActiveTrue();

    // Utile pour les listes filtrées
    List<Association> findByActiveTrue();

}
