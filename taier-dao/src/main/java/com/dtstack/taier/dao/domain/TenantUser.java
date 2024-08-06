package com.dtstack.taier.dao.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * @author junda
 * @Description: 用户对应数据源信息
 * @Date: 2024-03-19
 */
@TableName("tenant_user")
public class TenantUser extends BaseEntity{
    /**
     * 数据源id
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Integer userId;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * 是否删除
     */
    @TableField("is_deleted")
    private Integer isDeleted;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}
