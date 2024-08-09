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

import {useEffect, useRef, useState} from 'react';
import {Button, Divider, message, Modal, SelectProps, Space} from 'antd';
import type { ColumnsType } from 'antd/lib/table';
import moment from 'moment';
import { history } from 'umi';

import Api from '@/api';
import type { IActionRef } from '@/components/sketch';
import Sketch from '@/components/sketch';
import { DRAWER_MENU_ENUM } from '@/constant';
import './index.scss';
import TenantUserModal from "@/components/tenantUserModal";

interface ITenantUserProps {
    tenantId: number;
    gmtCreate: number;
    gmtModified: number;
    tenantName: string;
	tenantIdentity:string;
}

export default function TenantUserManage() {
    const actionRef = useRef<IActionRef>(null);
    const [modalVisible, setModalVisible] = useState(false);
    const [readonly, setReadonly] = useState(false);
    const [title, setTitle] = useState("新增角色");
    const [tenantId, setTenantId] = useState(-1);

    const getTenantList = (_: any, { current, pageSize }: { current: number; pageSize: number }) => {
        return Api.getTenantList().then((res) => {
            if (res.code === 1) {
                return {
                    total: res.data.length,
                    data: res.data,
                };
            }
        });
    };

    const handleDelete = (record: ITenantUserProps) => {
        Modal.confirm({
            title: `删除角色后不可恢复，确认删除角色 ${record.tenantId}?`,
            okText: '确认',
            onOk() {
                Api.deleteRole({
                    roleId: record.tenantId,
                }).then((res: any) => {
                    if (res.code === 1) {
                        message.success('角色删除成功');
                        actionRef.current?.submit();
                    }
                });
            },
        });
    };

    const onCancel = () => {
        setModalVisible(false);
    };

    const onSubmit = (params: { id:number,
		 userIdList:[] }) => {
        if(!readonly) {
			Api.updateTenantUser({ ...params }).then((res) => {
				if (res.code === 1) {
					onCancel();

					message.success('更新成功！');
					// actionRef.current?.submit();
				}
			});
		} else {
			onCancel()
		}
    };

    const viewTenant = (record: ITenantUserProps) => {
		setTenantId(record.tenantId)
		setTitle("查看租户")
		setModalVisible(true)
		setReadonly(true)
    };

	const updateRole = (record: ITenantUserProps) => {
		setTenantId(record.tenantId)
		setTitle("修改租户")
		setModalVisible(true)
		setReadonly(false)
	};


    const columns: ColumnsType<ITenantUserProps> = [
        {
            title: '租户名称',
            dataIndex: 'tenantName',
        },
		{
			title: '租户标识',
			dataIndex: 'tenantIdentity',
		},
		{
			title: '创建时间',
			dataIndex: 'gmtCreate',
			render(text) {
				return moment(text).format('YYYY-MM-DD HH:mm:ss');
			},
		},

        {
            title: '操作',
            dataIndex: 'deal',
            width: '170px',
            render: (_, record) => {
                return (
                    <Space split={<Divider type="vertical" />}>
                        <a onClick={() => viewTenant(record)}>查看</a>
                        <a onClick={() => updateRole(record)}>修改</a>
                    </Space>
                );
            },
        },
    ];
    return (
        <>
            <Sketch<ITenantUserProps, Record<string, never>>

                actionRef={actionRef}
                request={getTenantList}
                columns={columns}
                tableProps={{
                    rowSelection: undefined,
                }}
            />
			<TenantUserModal title={title} open={modalVisible} onCancel={onCancel}  okButtonProps={{disabled: readonly}}
						  onOk={onSubmit} tenantId={tenantId} readonly={readonly} />
        </>
    );
}
