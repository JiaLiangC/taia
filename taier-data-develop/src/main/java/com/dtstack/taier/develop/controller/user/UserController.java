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

package com.dtstack.taier.develop.controller.user;

import com.dtstack.taier.common.exception.ErrorCode;
import com.dtstack.taier.common.exception.TaierDefineException;
import com.dtstack.taier.common.lang.web.R;
import com.dtstack.taier.common.sftp.SFTPHandler;
import com.dtstack.taier.dao.domain.LdapGroup;
import com.dtstack.taier.dao.domain.Tenant;
import com.dtstack.taier.dao.domain.User;
import com.dtstack.taier.develop.bo.datasource.DsListParam;
import com.dtstack.taier.develop.dto.user.DTToken;
import com.dtstack.taier.develop.dto.user.DtUser;
import com.dtstack.taier.develop.mapstruct.user.UserTransfer;
import com.dtstack.taier.develop.service.console.TenantService;
import com.dtstack.taier.develop.service.user.CookieService;
import com.dtstack.taier.develop.service.user.LoginService;
import com.dtstack.taier.develop.service.user.TokenService;
import com.dtstack.taier.develop.service.user.UserService;
import com.dtstack.taier.develop.vo.user.UserListVO;
import com.dtstack.taier.develop.vo.user.UserVO;
import com.dtstack.taier.pluginapi.util.MD5Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static com.dtstack.taier.develop.service.user.UserService.IS_ADMIN;

/**
 * @author yuebai
 * @date 2021-08-02
 */
@RestController
@RequestMapping("/user")
@Api(value = "/user", tags = {"用户接口"})
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private LoginService loginService;

    @Autowired
    private CookieService cookieService;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TenantService tenantService;

    @PostMapping(value = "/login")
    public R<String> login(@RequestParam(value = "username") String userName, @RequestParam(value = "password") String password, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isBlank(userName)) {
            throw new TaierDefineException("userName can not null");
        }
        if (StringUtils.isBlank(password)) {
            throw new TaierDefineException("password can not null");
        }

        User user = userService.getByUserName(userName.trim());
        if (null == user) {
            throw new TaierDefineException(ErrorCode.USER_IS_NULL);
        }
        String md5Password = MD5Util.getMd5String(password);
        if (!md5Password.equalsIgnoreCase(user.getPassword())) {
            throw new TaierDefineException("password not correct");
        }
        DtUser dtUser = new DtUser();
        dtUser.setUserId(user.getId());
        dtUser.setUserName(user.getUserName());
        dtUser.setEmail(user.getEmail());
        dtUser.setPhone(user.getPhoneNumber());

        Long tentantId = tenantService.getTentantId(user.getId());
        Tenant tenant = tenantService.getTenantById(tentantId);

        if(tenant != null) {
            dtUser.setTenantName(tenant.getTenantName());
        } else {
            dtUser.setTenantName("taier");
        }
        if(IS_ADMIN == user.getIsAdmin()) {
            dtUser.setRootOnly(true);
        } else {
            dtUser.setRootOnly(false);
        }
        loginService.onAuthenticationSuccess(request, response, dtUser);
        return R.ok(dtUser.getUserName());
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/loginByLdap")
    public R<String> loginByToken(@RequestParam(value = "username") String userName, @RequestParam(value = "password") String password, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (StringUtils.isBlank(userName)) {
            throw new TaierDefineException("userName can not null");
        }
        if (StringUtils.isBlank(password)) {
            throw new TaierDefineException("password can not null");
        }

        User user = userService.getUserByLdap(userName, password);

        if (null == user) {
            throw new TaierDefineException(ErrorCode.USER_IS_NULL);
        }
        if(!user.getPwdValid()) {
            throw new TaierDefineException(ErrorCode.USER_PWD_ERR);
        }
        Long tentantId = tenantService.getTentantId(user.getId());
        Tenant tenant = tenantService.getTenantById(tentantId);
        // 校验通过
        DtUser dtUser = new DtUser();
        dtUser.setUserId(user.getId());
        dtUser.setUserName(user.getUserName());
        dtUser.setEmail(user.getEmail());
        dtUser.setPhone(user.getPhoneNumber());
        dtUser.setTenantId(tentantId);
        dtUser.setGroupId(user.getGroupId());
        if(tenant != null) {
            dtUser.setTenantName(tenant.getTenantName());
        } else {
            dtUser.setTenantName("taier");
        }

        if(IS_ADMIN == user.getIsAdmin()) {
            dtUser.setRootOnly(true);
        } else {
            dtUser.setRootOnly(false);
        }
        loginService.onAuthenticationSuccess(request, response, dtUser);

        return R.ok(dtUser.getUserName());

    }

    @RequestMapping(value = "/logout")
    public R<Boolean> logout(HttpServletRequest request, HttpServletResponse response) {
        cookieService.clean(request, response);
        return R.ok(true);
    }

    @GetMapping("/ldap/users")
    public R<List<User>> listLdapUsers() {
        try {
            List<User> ldapUsers = userService.getLdapUsers();
            return R.ok(ldapUsers);
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new TaierDefineException("获取用户异常");
        }
    }

    @GetMapping("/ldap/groups")
    public R<List<LdapGroup>> listLdapGroups() {
        try {
            List<LdapGroup> ldapGroups = userService.getLdapGroups();
            return R.ok(ldapGroups);
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new TaierDefineException("获取用户组异常");
        }
    }

    @PostMapping(value = "/switchTenant")
    public R<String> switchTenant(@RequestParam(value = "tenantId") Long tenantId, HttpServletRequest request, HttpServletResponse response) {
        String token = cookieService.token(request);
        if (StringUtils.isBlank(token)) {
            throw new TaierDefineException(ErrorCode.TOKEN_IS_NULL);
        }
        DTToken decryption = tokenService.decryption(token);
        Long userId = decryption.getUserId();
        User user = userService.getById(userId);
        if (null == user) {
            throw new TaierDefineException(ErrorCode.USER_IS_NULL);
        }
        Tenant tenant = tenantService.getTenantById(tenantId);
        if (null == tenant) {
            throw new TaierDefineException(ErrorCode.TENANT_IS_NULL);
        }
        DtUser dtUser = new DtUser();
        dtUser.setUserId(user.getId());
        dtUser.setUserName(user.getUserName());
        dtUser.setEmail(user.getEmail());
        dtUser.setPhone(user.getPhoneNumber());
        dtUser.setTenantId(tenantId);
        dtUser.setTenantName(tenant.getTenantName());
        loginService.onAuthenticationSuccess(request, response, dtUser);
        return R.ok(user.getUserName());
    }


    @RequestMapping(value = "/queryUser")
    public R<List<UserVO>> queryUser() {
        List<User> users = userService.listAll();
        List<UserVO> userVOS = UserTransfer.INSTANCE.toVo(users);
        return R.ok(userVOS);
    }

    @ApiOperation("获取用户列表")
    @RequestMapping(value = "/getAllUser")
    public R<List<UserListVO>> getAllUser(@RequestBody DsListParam dsListParam) {
        dsListParam.setTenantId(null);
        return R.ok(userService.total(dsListParam));
    }


}
