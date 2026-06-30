package com.unicom.post.modules.statistics.vo;

import lombok.Data;
import java.util.List;


// TrendResult
@Data
public class TrendResult {
    private String granularity;
    private String metric;
    private List<TrendVO> list;
}