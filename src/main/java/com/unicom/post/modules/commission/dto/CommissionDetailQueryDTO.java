package com.unicom.post.modules.commission.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class CommissionDetailQueryDTO {
    private String payeeType;          // OUTLET|DEVELOPER|PLATFORM
    private Long payeeId;
    private String commissionPhase;    // LEAD|FORMAL
    private String status;             // PENDING|SETTLED|FROZEN
    private Long cityId;
    private Long outletId;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private Integer pageNo = 1;
    private Integer pageSize = 20;
}