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

package com.dtstack.taier.develop.utils;


import com.dtstack.taier.common.constant.Cookies;

import javax.servlet.http.Cookie;

public class CookieUtil {

    public static String getToken(Cookie[] cookies) {
        Object value = getCookieValue(cookies, Cookies.TOKEN);
        return value == null ? "" : value.toString();
    }


    public static long getUserId(Cookie[] cookies) {
        Object value = getCookieValue(cookies, Cookies.USER_ID);
        return value == null ? -1 : Long.parseLong(value.toString());
    }

    public static long getGroupId(Cookie[] cookies) {
        Object value = getCookieValue(cookies, Cookies.GROUP_ID);
        return value == null ? -1 : Long.parseLong(value.toString());
    }

    public static Integer getIsAdmin(Cookie[] cookies) {
        Object value = getCookieValue(cookies, Cookies.IS_ADMIN);
        return value == null ? -1 : Integer.parseInt(value.toString());
    }



    private static Object getCookieValue(Cookie[] cookies, String key) {
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (key.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
