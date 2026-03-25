package com.techsolution.tontine_saas.services;

import com.techsolution.tontine_saas.dtos.request.MemberTontineRequest;
import com.techsolution.tontine_saas.dtos.response.MemberTontineResponse;

import java.util.List;

public interface MemberTontineService {

    MemberTontineResponse addMemberToTontine(MemberTontineRequest request);

    void removeMemberFromTontine(Long id);

    List<MemberTontineResponse> getMembersByTontine(Long tontineId);

    List<MemberTontineResponse> getTontinesByUser(Long userId);

    MemberTontineResponse getMemberDetails(Long userId, Long tontineId);
}
