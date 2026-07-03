package com.unicom.post.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unicom.post.common.exception.BusinessException;
import com.unicom.post.modules.system.domain.entity.SysOrg;
import com.unicom.post.modules.system.mapper.SysOrgMapper;
import com.unicom.post.modules.system.service.SysOrgService;
import com.unicom.post.modules.system.vo.OrgTreeNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysOrgServiceImpl extends ServiceImpl<SysOrgMapper, SysOrg> implements SysOrgService {

    @Override
    public OrgTreeNode getOrgTree(boolean includeOutlet) {
        LambdaQueryWrapper<SysOrg> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(SysOrg::getParentId)
                .eq(SysOrg::getIsDeleted, 0)
                .eq(SysOrg::getStatus, 1)
                .orderByAsc(SysOrg::getSortOrder);
        SysOrg root = this.getOne(wrapper);
        if (root == null) {
            return null;
        }
        OrgTreeNode rootNode = convertToNode(root);
        buildTree(rootNode, includeOutlet);
        return rootNode;
    }

    private void buildTree(OrgTreeNode parentNode, boolean includeOutlet) {
        List<SysOrg> children = baseMapper.selectChildrenByParentId(parentNode.getId());
        if (!includeOutlet) {
            children = children.stream()
                    .filter(org -> !"OUTLET".equals(org.getOrgType()))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(children)) {
            parentNode.setChildren(new ArrayList<>());
            return;
        }
        List<OrgTreeNode> childNodes = new ArrayList<>();
        for (SysOrg child : children) {
            OrgTreeNode node = convertToNode(child);
            childNodes.add(node);
            buildTree(node, includeOutlet);
        }
        parentNode.setChildren(childNodes);
    }

    private OrgTreeNode convertToNode(SysOrg org) {
        OrgTreeNode node = new OrgTreeNode();
        node.setId(org.getId());
        node.setOrgName(org.getOrgName());
        node.setOrgLevel(org.getOrgLevel());
        node.setOrgType(org.getOrgType());
        node.setLeaderName(org.getLeaderName());
        node.setAddress(org.getAddress());
        return node;
    }

    @Override
    public List<SysOrg> getChildren(Long parentId) {
        return baseMapper.selectChildrenByParentId(parentId);
    }

    @Override
    @Transactional
    public boolean createOrg(SysOrg org) {
        LambdaQueryWrapper<SysOrg> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOrg::getOrgCode, org.getOrgCode())
                .eq(SysOrg::getIsDeleted, 0);
        if (this.count(wrapper) > 0) {
            throw new BusinessException("组织编码已存在");
        }
        if (org.getParentId() != null) {
            SysOrg parent = this.getById(org.getParentId());
            if (parent == null) {
                throw new BusinessException("父组织不存在");
            }
            if ("CITY".equals(org.getOrgType())) {
                org.setProvinceId(parent.getId());
                org.setCityId(org.getId());
            } else if ("DISTRICT".equals(org.getOrgType())) {
                org.setProvinceId(parent.getProvinceId());
                org.setCityId(parent.getCityId());
            } else if ("OUTLET".equals(org.getOrgType())) {
                org.setProvinceId(parent.getProvinceId());
                org.setCityId(parent.getCityId());
            }
        } else {
            org.setProvinceId(null);
            org.setCityId(null);
        }
        return this.save(org);
    }

    @Override
    @Transactional
    public boolean updateOrg(Long id, SysOrg org) {
        SysOrg exist = this.getById(id);
        if (exist == null) {
            throw new BusinessException("组织不存在");
        }
        exist.setOrgName(org.getOrgName());
        exist.setLeaderName(org.getLeaderName());
        exist.setLeaderPhone(org.getLeaderPhone());
        exist.setAddress(org.getAddress());
        exist.setRemark(org.getRemark());
        return this.updateById(exist);
    }

    @Override
    @Transactional
    public boolean deleteOrg(Long id) {
        List<SysOrg> children = baseMapper.selectChildrenByParentId(id);
        if (!CollectionUtils.isEmpty(children)) {
            throw new BusinessException("该组织下还有下级，请先删除下级");
        }
        SysOrg org = this.getById(id);
        if (org == null) {
            throw new BusinessException("组织不存在");
        }
        org.setIsDeleted(1);
        org.setStatus(0);
        return this.updateById(org);
    }
}