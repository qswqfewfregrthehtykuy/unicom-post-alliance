package com.unicom.post.modules.system.vo;

import lombok.Data;
import java.util.List;

@Data
public class OrgTreeNode {
    private Long id;
    private String orgName;
    private Integer orgLevel;
    private String orgType;
    private String leaderName;
    private String address;
    private List<OrgTreeNode> children;
}