package com.unicom.post.modules.statistics.vo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class StatisticsRequest {
    @NotBlank(message = "维度不能为空")
    private String dimension;          // CITY|DISTRICT|OUTLET|DEVELOPER|DATE

    private Long cityId;
    private Long outletId;
    private String businessType;

    @NotNull(message = "开始日期不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}