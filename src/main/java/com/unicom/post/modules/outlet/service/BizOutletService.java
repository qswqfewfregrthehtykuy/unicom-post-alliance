package com.unicom.post.modules.outlet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.unicom.post.modules.outlet.domain.entity.BizOutlet;

public interface BizOutletService extends IService<BizOutlet> {
    BizOutlet getById(Long id);
}