package com.techsolution.tontine_saas.services;

import com.techsolution.tontine_saas.dtos.request.TontineRequest;
import com.techsolution.tontine_saas.dtos.response.TontineResponse;

import java.util.List;

public interface TontineService {

    TontineResponse createTontine(TontineRequest request);

    TontineResponse getTontineById(Long tontineId);

    List<TontineResponse> getAssociationTontines(boolean onlyActive);

    TontineResponse updateTontineStatus(Long id, boolean active);

    void deleteTontine(Long id );

}
