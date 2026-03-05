package com.techsolution.tontine_saas.services;

import com.techsolution.tontine_saas.dtos.request.TontineRequest;
import com.techsolution.tontine_saas.dtos.response.TontineResponse;

import java.util.List;

public interface TontineService {

    TontineResponse createTontine(TontineRequest request, Long adminId);

    TontineResponse getTontineById(Long tontineId);

    List<TontineResponse> getAssociationTontines(Long associationId, boolean onlyActive);

    TontineResponse updateTontineStatus(Long id, boolean active, Long adminId);

    void deleteTontine(Long id, Long adminId);

}
