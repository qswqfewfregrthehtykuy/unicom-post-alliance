package com.unicom.post.modules.org.service;

import com.unicom.post.modules.org.dto.CityDTO;
import com.unicom.post.modules.org.dto.DistrictWithOutletsDTO;

import java.util.List;

public interface OrgService {
    List<CityDTO> listCities();
    List<DistrictWithOutletsDTO> listDistrictsWithOutlets(Long cityId);
}