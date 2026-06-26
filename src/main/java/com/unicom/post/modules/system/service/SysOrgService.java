package com.unicom.post.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.unicom.post.modules.system.domain.entity.SysOrg;
import com.unicom.post.modules.system.vo.OrgTreeNode;
import java.util.List;

public interface SysOrgService extends IService<SysOrg> {
    OrgTreeNode getOrgTree(boolean includeOutlet);
    List<SysOrg> getChildren(Long parentId);
    boolean createOrg(SysOrg org);
    boolean updateOrg(Long id, SysOrg org);
    boolean deleteOrg(Long id);
}