package com.unicom.post.modules.org.controller;

import com.unicom.post.common.result.Result;
import com.unicom.post.modules.org.dto.CityDTO;
import com.unicom.post.modules.org.dto.DistrictWithOutletsDTO;
import com.unicom.post.modules.org.service.OrgService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/org")
public class OrgQueryController {

    private final OrgService orgService;

    public OrgQueryController(OrgService orgService) {
        this.orgService = orgService;
    }

    /**
     * 获取所有地市列表
     */
    @GetMapping("/cities")
    public Result<List<CityDTO>> listCities() {
        return Result.success(orgService.listCities());
    }

    /**
     * 根据地市ID获取区县及下属网点（树形结构）
     */
    @GetMapping("/districts/outlets")
    public Result<List<DistrictWithOutletsDTO>> listDistrictsWithOutlets(
            @RequestParam Long cityId) {
        return Result.success(orgService.listDistrictsWithOutlets(cityId));
    }
}