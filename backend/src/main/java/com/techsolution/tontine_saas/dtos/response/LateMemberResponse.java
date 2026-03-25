package com.techsolution.tontine_saas.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class LateMemberResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private int numberOfLateContributions;
    private BigDecimal totalDebt;
    private LocalDate lastDueDate;
}
