package com.unicom.post.modules.org.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unicom.post.modules.org.dto.CityDTO;
import com.unicom.post.modules.org.dto.DistrictWithOutletsDTO;
import com.unicom.post.modules.org.dto.OutletSimpleDTO;
import com.unicom.post.modules.org.service.OrgService;
import com.unicom.post.modules.outlet.domain.entity.BizOutlet;
import com.unicom.post.modules.outlet.mapper.BizOutletMapper;
import com.unicom.post.modules.system.domain.entity.SysCity;
import com.unicom.post.modules.system.domain.entity.SysDistrict;
import com.unicom.post.modules.system.mapper.SysCityMapper;
import com.unicom.post.modules.system.mapper.SysDistrictMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrgServiceImpl implements OrgService {

    private final SysCityMapper cityMapper;
    private final SysDistrictMapper districtMapper;
    private final BizOutletMapper outletMapper;

    public OrgServiceImpl(SysCityMapper cityMapper,
                          SysDistrictMapper districtMapper,
                          BizOutletMapper outletMapper) {
        this.cityMapper = cityMapper;
        this.districtMapper = districtMapper;
        this.outletMapper = outletMapper;
    }

    @Override
    public List<CityDTO> listCities() {
        LambdaQueryWrapper<SysCity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysCity::getIsDeleted, 0)
                .orderByAsc(SysCity::getSortOrder);
        List<SysCity> cities = cityMapper.selectList(wrapper);
        return cities.stream()
                .map(c -> {
                    CityDTO dto = new CityDTO();
                    dto.setId(c.getId());
                    dto.setName(c.getCityName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DistrictWithOutletsDTO> listDistrictsWithOutlets(Long cityId) {
        // 1. 查询该地市下的所有区县
        LambdaQueryWrapper<SysDistrict> districtWrapper = new LambdaQueryWrapper<>();
        districtWrapper.eq(SysDistrict::getCityId, cityId)
                .eq(SysDistrict::getIsDeleted, 0)
                .orderByAsc(SysDistrict::getSortOrder);
        List<SysDistrict> districts = districtMapper.selectList(districtWrapper);

        if (districts.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 收集区县ID
        List<Long> districtIds = districts.stream()
                .map(SysDistrict::getId)
                .collect(Collectors.toList());

        // 3. 查询这些区县下的所有网点
        LambdaQueryWrapper<BizOutlet> outletWrapper = new LambdaQueryWrapper<>();
        outletWrapper.in(BizOutlet::getDistrictId, districtIds)
                .eq(BizOutlet::getIsDeleted, 0)
                .eq(BizOutlet::getStatus, 1)   // 只查启用的网点
                .orderByAsc(BizOutlet::getOutletName);
        List<BizOutlet> outlets = outletMapper.selectList(outletWrapper);

        // 4. 按区县ID分组网点
        Map<Long, List<BizOutlet>> outletMap = outlets.stream()
                .collect(Collectors.groupingBy(BizOutlet::getDistrictId));

        // 5. 组装结果
        return districts.stream()
                .map(d -> {
                    DistrictWithOutletsDTO dto = new DistrictWithOutletsDTO();
                    dto.setId(d.getId());
                    dto.setName(d.getDistrictName());
                    List<BizOutlet> outletList = outletMap.getOrDefault(d.getId(), new ArrayList<>());
                    List<OutletSimpleDTO> outletDTOs = outletList.stream()
                            .map(o -> {
                                OutletSimpleDTO od = new OutletSimpleDTO();
                                od.setId(o.getId());
                                od.setName(o.getOutletName());
                                return od;
                            })
                            .collect(Collectors.toList());
                    dto.setOutlets(outletDTOs);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}