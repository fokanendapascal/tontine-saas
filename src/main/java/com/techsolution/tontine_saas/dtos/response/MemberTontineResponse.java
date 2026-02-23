package com.techsolution.tontine_saas.dtos.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberTontineResponse {

    private Long id;

    private Integer orderPosition;

    private LocalDateTime joinedAt;

    private Long userId;

    private Long tontineId;

    // 🔥 Extensions intelligentes
    private String userFullName;
    private String tontineName;
    private Boolean isActive;
    private Long contributionCount;
    private BigDecimal totalContributed;
}