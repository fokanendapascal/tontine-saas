package com.techsolution.tontine_saas.services;

import com.techsolution.tontine_saas.dtos.request.AssociationRequest;
import com.techsolution.tontine_saas.dtos.response.AssociationResponse;

import java.util.List;

public interface AssociationService {

    AssociationResponse createAssociation(AssociationRequest request);

    AssociationResponse updateAssociation(Long id, AssociationRequest request);

    AssociationResponse getAssociationById(Long id);

    List<AssociationResponse> getAllAssociations();

    void deactivateAssociation(Long id);

    void activateAssociation(Long id);

    void deleteAssociation(Long id);
}
