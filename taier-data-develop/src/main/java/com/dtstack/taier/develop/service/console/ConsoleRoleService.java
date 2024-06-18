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

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dtstack.taier.common.enums.Deleted;
import com.dtstack.taier.common.exception.ErrorCode;
import com.dtstack.taier.common.exception.TaierDefineException;
import com.dtstack.taier.dao.domain.DsRole;
import com.dtstack.taier.dao.domain.Role;
import com.dtstack.taier.dao.domain.RoleUserGroup;
import com.dtstack.taier.dao.mapper.DsRoleMapper;
import com.dtstack.taier.dao.mapper.RoleMapper;
import com.dtstack.taier.dao.mapper.RoleUserGroupMapper;
import com.dtstack.taier.develop.vo.console.RoleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.dtstack.taier.dao.mapper.RoleUserGroupMapper.TYPE_GROUP;
import static com.dtstack.taier.dao.mapper.RoleUserGroupMapper.TYPE_USER;

@Component
public class ConsoleRoleService {

    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private DsRoleMapper dsRoleMapper;
    @Autowired
    private RoleUserGroupMapper roleUserGroupMapper;
    

    @Transactional
    public Long addRole(Role role) {
        Long roleId = role.getId();
        if(roleId == null || roleId == -1) {
            if (roleMapper.getByName(role.getName()) != null) {
                throw new TaierDefineException(ErrorCode.NAME_ALREADY_EXIST.getDescription());
            }
            role.setId(null);
            int insert = roleMapper.insert(role);
            if (insert > 0) {
               addRoleAssociation(role);
            }
        } else {
            // 更新操作
            roleMapper.updateById(role);
            deleteRoleAssociation(roleId);
            addRoleAssociation(role);
        }
        return role.getId();
    }

    private void addRoleAssociation(Role role) {
        List<Integer> dataSourceIdList = role.getDataSourceIdList();
        List<Integer> userIdList = role.getUserIdList();
        List<Integer> groupIdList = role.getGroupIdList();
        if (dataSourceIdList != null && dataSourceIdList.size() > 0) {
            for (Integer dsId : dataSourceIdList) {
                DsRole ds = new DsRole();
                ds.setRoleId(role.getId());
                ds.setDataSourceId(dsId);
                dsRoleMapper.insert(ds);
            }
        }

        saveRoleUserGroup(role, userIdList, TYPE_USER);
        saveRoleUserGroup(role, groupIdList, TYPE_GROUP);
    }

    /**
     *  删除角色关联的数据源， 用户， 用户组
     * @param roleId
     */
    private void deleteRoleAssociation(Long roleId) {
        QueryWrapper<DsRole> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id", roleId);
        dsRoleMapper.delete(wrapper);

        QueryWrapper<RoleUserGroup> ruWrapper = new QueryWrapper<>();
        ruWrapper.eq("role_id", roleId);
        roleUserGroupMapper.delete(ruWrapper);
    }

    public Role getRole(Long roleId) {
        Role role = roleMapper.getOne(roleId);
        if(role != null) {
            List<Integer> dsSourceIds = dsRoleMapper.queryDsIdListByRole(role.getId());
            role.setDataSourceIdList(dsSourceIds);

            QueryWrapper<RoleUserGroup> wrapper = new QueryWrapper<>();
            wrapper.eq("role_id", role.getId());
            List<RoleUserGroup> roleUserGroups = roleUserGroupMapper.selectList(wrapper);
            List<Integer> userIds = new ArrayList<>();
            List<Integer> groupIds = new ArrayList<>();
            for (RoleUserGroup userGroup : roleUserGroups) {
                if(TYPE_USER.equals(userGroup.getType())) {
                    userIds.add(userGroup.getUgId());
                } else if(TYPE_GROUP.equals(userGroup.getType())) {
                    groupIds.add(userGroup.getUgId());
                }
            }
            role.setUserIdList(userIds);
            role.setGroupIdList(groupIds);
        }

        return role;
    }

    private void saveRoleUserGroup(Role role, List<Integer> groupIdList, String typeGroup) {
        if(groupIdList != null && groupIdList.size() > 0) {
            for (Integer groupId : groupIdList) {
                RoleUserGroup rug = new RoleUserGroup();
                rug.setRoleId(role.getId());
                rug.setUgId(groupId);
                rug.setType(typeGroup);
                roleUserGroupMapper.insert(rug);
            }
        }
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
