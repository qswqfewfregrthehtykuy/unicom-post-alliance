package com.unicom.post.modules.outlet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unicom.post.modules.outlet.domain.entity.BizOutlet;
import com.unicom.post.modules.outlet.mapper.BizOutletMapper;
import com.unicom.post.modules.outlet.service.BizOutletService;
import org.springframework.stereotype.Service;

@Service
public class BizOutletServiceImpl extends ServiceImpl<BizOutletMapper, BizOutlet> implements BizOutletService {
    @Override
    public BizOutlet getById(Long id) {
        return super.getById(id);
    }
}