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

package com.dtstack.taier.develop.controller.console;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dtstack.taier.common.lang.web.R;
import com.dtstack.taier.dao.domain.Cluster;
import com.dtstack.taier.dao.domain.Role;
import com.dtstack.taier.dao.pager.PageResult;
import com.dtstack.taier.develop.mapstruct.console.ClusterTransfer;
import com.dtstack.taier.develop.service.console.ConsoleRoleService;
import com.dtstack.taier.develop.vo.console.ClusterEngineVO;
import com.dtstack.taier.develop.vo.console.ClusterInfoVO;
import com.dtstack.taier.develop.vo.console.ClusterVO;
import com.dtstack.taier.develop.vo.console.RoleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
@Api(value = "/role", tags = {"角色接口"})
public class RoleController {

    @Autowired
    private ConsoleRoleService consoleRoleService;

    @ApiOperation(value = "addRole", notes = "创建角色")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterName", value = "角色名称", required = true, dataType = "String")
    })
    @PostMapping(value = "/addRole")
    public R<Long> addRole(@RequestBody Role role) {
        return R.ok(consoleRoleService.addRole(role));
    }


    @ApiOperation(value = "pageQuery", notes = "角色列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "currentPage", value = "当前页", required = true, dataType = "int"),
            @ApiImplicitParam(name = "pageSize", value = "页面大小", required = true, dataType = "int")
    })
    @PostMapping(value = "/pageQuery")
    public R<PageResult<List<Role>>> pageQuery(@RequestParam("currentPage") int currentPage, @RequestParam("pageSize") int pageSize) {
        IPage<Role> roleIPage = consoleRoleService.pageQuery(currentPage, pageSize);
        List<Role> roles = roleIPage.getRecords();
        PageResult<List<Role>> pageResult = new PageResult<>(currentPage, pageSize, roleIPage.getTotal(), roles);
        return R.ok(pageResult);
    }

    @ApiOperation(value = "deleteRole", notes = "删除角色")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterId", value = "角色id", required = true, dataType = "long")
    })
    @PostMapping(value = "/deleteRole")
    public R<Boolean> deleteRole(@RequestParam("roleId") Long roleId) {
        return R.ok(consoleRoleService.deleteRole(roleId));
    }

    @ApiOperation(value = "getRole", notes = "获取角色包含用户和组")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterId", value = "角色id", required = true, dataType = "long")
    })
    @GetMapping(value = "/getRole")
    public R<RoleVO> getRole(@RequestParam("roleId") Long roleId) {
        return R.ok(consoleRoleService.getConsoleRoleInfo(roleId));
    }

    @ApiOperation(value = "getAllRole", notes = "获取所有角色名称")
    @GetMapping(value = "/getAllRole")
    public R<List<Role>> getAllRole() {
        List<Role> roles = consoleRoleService.getAllRole();
        return R.ok(roles);
    }
}
