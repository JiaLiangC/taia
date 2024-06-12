package com.dtstack.taier.dao.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * @author junda
 * @Description: 用户对应数据源信息
 * @Date: 2024-03-19
 */
@TableName("datasource_role")
public class DsRole extends BaseEntity{
    /**
     * 数据源id
     */
    @TableField("datasource_id")
    private Integer dataSourceId;

    /**
     * 用户id
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 是否删除
     */
    @TableField("is_deleted")
    private Integer isDeleted;

    public Integer getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(Integer dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
