// DevelopmentStatisticsResult
package com.unicom.post.modules.statistics.vo;

import lombok.Data;
import java.util.List;

@Data
public class DevelopmentStatisticsResult {
    private String dimension;
    private Long total;            // 总记录数
    private List<DevelopmentStatisticsVO> list;
}

