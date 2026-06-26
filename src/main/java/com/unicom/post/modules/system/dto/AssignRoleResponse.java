package com.unicom.post.modules.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignRoleResponse {
    private Long userId;
    private List<Long> roleIds;
}