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

package com.dtstack.taier.develop.vo.console;

import io.swagger.annotations.ApiModelProperty;

import java.sql.Timestamp;

/**
 * @author haitaosong
 */
public class TenantVO {

    @ApiModelProperty(notes = "租户名称")
    private String tenantName;

    @ApiModelProperty(notes = "租户id")
    private Long tenantId;
    @ApiModelProperty(notes = "创建时间")
    private Timestamp gmtCreate;
    @ApiModelProperty(notes = "租户标识")
    private String tenantIdentity;

    public Timestamp getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Timestamp gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public String getTenantIdentity() {
        return tenantIdentity;
    }

    public void setTenantIdentity(String tenantIdentity) {
        this.tenantIdentity = tenantIdentity;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
