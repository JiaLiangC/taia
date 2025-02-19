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

package com.dtstack.taier.develop.interceptor;

import com.dtstack.taier.common.constant.CommonConstant;
import com.dtstack.taier.common.exception.ErrorCode;
import com.dtstack.taier.common.exception.TaierDefineException;
import com.dtstack.taier.develop.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.dtstack.taier.develop.service.user.UserService.IS_ADMIN;

public class AdminPermissionCheckInterceptor extends HandlerInterceptorAdapter {

    private static Logger LOGGER = LoggerFactory.getLogger(AdminPermissionCheckInterceptor.class);

    private static String[] adminURIs = new String[]{"/taier/api/role/", "/taier/api/tenant/"};

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        Integer isAdmin = CookieUtil.getIsAdmin(request.getCookies());
        for (String uri : adminURIs) {
            if(requestURI.startsWith(uri)) {
                if(IS_ADMIN != isAdmin) {
                    throw new TaierDefineException(ErrorCode.PERMISSION_LIMIT);
                }
            }
        }

        return true;
    }
}
