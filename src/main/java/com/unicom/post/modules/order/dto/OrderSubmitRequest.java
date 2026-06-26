package com.unicom.post.modules.order.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class OrderSubmitRequest {
    @NotBlank
    private String businessType;
    @NotBlank
    private String developSource;
    @NotBlank
    private String customerName;
    @NotBlank
    private String customerPhone;
    private String customerIdCard;
    private String customerAddress;
    private String remark;
}