package com.dtstack.taier.dao.domain;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * @author haitaosong
 */
@TableName("role_user_group")
public class RoleUserGroup extends BaseEntity {

    private Integer ugId;
    private Long roleId;

    private String type;

    public Integer getUgId() {
        return ugId;
    }

    public void setUgId(Integer ugId) {
        this.ugId = ugId;
    }


    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
