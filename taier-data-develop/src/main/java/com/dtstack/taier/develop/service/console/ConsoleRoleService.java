/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtstack.taier.develop.service.console;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dtstack.taier.common.enums.Deleted;
import com.dtstack.taier.common.exception.ErrorCode;
import com.dtstack.taier.common.exception.TaierDefineException;
import com.dtstack.taier.dao.domain.Role;
import com.dtstack.taier.dao.mapper.RoleMapper;
import com.dtstack.taier.develop.vo.console.RoleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ConsoleRoleService {

    @Autowired
    private RoleMapper roleMapper;
    

    public Long addRole(Role role) {
        if (roleMapper.getByName(role.getName()) != null) {
            throw new TaierDefineException(ErrorCode.NAME_ALREADY_EXIST.getDescription());
        }
        roleMapper.insert(role);
        return role.getId();
    }

    public IPage<Role> pageQuery(int currentPage, int pageSize) {
        Page<Role> page = new Page<>(currentPage, pageSize);
        return roleMapper.selectPage(page, Wrappers.lambdaQuery(Role.class).eq(
                Role::getIsDeleted, Deleted.NORMAL.getStatus())
        );
    }

    /**
     * 删除集群
     * 判断该集群下是否有租户
     *
     * @param roleId
     */
    public boolean deleteRole(Long roleId) {
        if (null == roleId) {
            throw new TaierDefineException("RoleId cannot be empty");
        }
        Role role = roleMapper.getOne(roleId);
        if (null == role) {
            throw new TaierDefineException("Role does not exist");
        }

        return roleMapper.deleteById(roleId) > 0;
    }

    /**
     * 获取集群信息详情 需要根据组件分组
     *
     * @param clusterId
     * @return
     */
    public RoleVO getConsoleRoleInfo(Long clusterId) {
        Role role = roleMapper.getOne(clusterId);
        if (null == role) {
            return new RoleVO();
        }

        return new RoleVO();
    }

    public List<Role> getAllRole() {
        return roleMapper.selectList(Wrappers.lambdaQuery(Role.class)
                .eq(Role::getIsDeleted, Deleted.NORMAL.getStatus()));
    }

}
