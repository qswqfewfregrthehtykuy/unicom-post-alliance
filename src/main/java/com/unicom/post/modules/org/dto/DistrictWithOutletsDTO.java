package com.unicom.post.modules.org.dto;

import lombok.Data;
import java.util.List;

@Data
public class DistrictWithOutletsDTO {
    private Long id;
    private String name;
    private List<OutletSimpleDTO> outlets;
}