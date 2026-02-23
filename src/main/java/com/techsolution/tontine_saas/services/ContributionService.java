package com.techsolution.tontine_saas.services;

import com.techsolution.tontine_saas.dtos.request.ContributionRequest;
import com.techsolution.tontine_saas.dtos.response.ContributionResponse;
import com.techsolution.tontine_saas.dtos.response.LateMemberResponse;

import java.util.List;

public interface ContributionService {

    ContributionResponse payContribution(ContributionRequest request, Long adminId);

    ContributionResponse getContributionById(Long id);

    List<ContributionResponse> getMemberHistory(Long memberTontineId);

    List<ContributionResponse> getTontineHistory(Long tontineId);

    void updateContributionStatus(Long id, String status, Long adminId);

    /**
     * Identifie les membres en retard pour une tontine spécifique
     * et retourne les informations nécessaires à la relance.
     */
    List<LateMemberResponse> getLateMembers(Long tontineId);
}
