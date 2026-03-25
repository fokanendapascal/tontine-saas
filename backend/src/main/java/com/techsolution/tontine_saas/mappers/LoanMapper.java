package com.techsolution.tontine_saas.mappers;

import com.techsolution.tontine_saas.dtos.request.LoanRequest;
import com.techsolution.tontine_saas.dtos.response.LoanResponse;
import com.techsolution.tontine_saas.entities.Loan;
import com.techsolution.tontine_saas.entities.User;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanMapper {

    public static Loan toEntity(LoanRequest request, User user) {

        return Loan.builder()
                .amount(request.getAmount())
                .interestRate(request.getInterestRate() != null ? request.getInterestRate() : 0.0)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus())
                .user(user)
                .build();
    }

    public static LoanResponse toResponse(Loan loan, BigDecimal totalRepaid) {

        BigDecimal safeTotalRepaid = totalRepaid != null ? totalRepaid : BigDecimal.ZERO;

        BigDecimal remainingAmount = loan.getAmount() != null
                ? loan.getAmount().subtract(safeTotalRepaid)
                : BigDecimal.ZERO;

        boolean isOverdue =
                loan.getEndDate() != null &&
                        loan.getEndDate().isBefore(LocalDate.now()) &&
                        remainingAmount.compareTo(BigDecimal.ZERO) > 0;

        LoanResponse.LoanResponseBuilder builder = LoanResponse.builder()
                .id(loan.getId())
                .amount(loan.getAmount())
                .interestRate(loan.getInterestRate())
                .startDate(loan.getStartDate())
                .endDate(loan.getEndDate())
                .status(loan.getStatus())
                .userId(loan.getUser() != null ? loan.getUser().getId() : null)
                .totalRepaid(safeTotalRepaid)
                .remainingAmount(remainingAmount)
                .overdue(isOverdue);

        if (loan.getUser() != null) {
            builder.userFullName(
                    loan.getUser().getFirstName() + " " +
                            loan.getUser().getLastName()
            );
        }

        return builder.build();
    }
}