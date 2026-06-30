package com.unicom.post.modules.statistics.vo;

import lombok.Data;
import java.util.List;


// RankingResult
@Data
public class RankingResult {
    private String rankType;
    private String period;          // "2026-06-01 ~ 2026-06-20"
    private List<RankingVO> list;
}