package com.techsolution.tontine_saas.services;

import com.techsolution.tontine_saas.dtos.request.SavingsTransactionRequest;
import com.techsolution.tontine_saas.dtos.response.SavingsTransactionResponse;

import java.util.List;

public interface SavingsTransactionService {

    SavingsTransactionResponse processTransaction(SavingsTransactionRequest request, Long adminId);

    List<SavingsTransactionResponse> getTransactionHistory(Long savingsId);

    SavingsTransactionResponse getTransactionById(Long id);
}
