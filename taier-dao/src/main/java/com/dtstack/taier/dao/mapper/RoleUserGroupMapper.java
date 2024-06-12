package com.dtstack.taier.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dtstack.taier.dao.domain.RoleUserGroup;

/**
 * @author haitaosong
 */
public interface RoleUserGroupMapper extends BaseMapper<RoleUserGroup> {

   String TYPE_USER = "1";
   String TYPE_GROUP = "2";
}
