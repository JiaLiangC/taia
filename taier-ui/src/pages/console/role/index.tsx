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
import AddRoleModal from '@/components/addRoleModal';
import type { IActionRef } from '@/components/sketch';
import Sketch from '@/components/sketch';
import { DRAWER_MENU_ENUM } from '@/constant';
import './index.scss';
import API from "@/api";

interface IRoleProps {
    id: number;
    gmtCreate: number;
    gmtModified: number;
    name: string;
    remark: string;
}

export default function RoleManage() {
    const actionRef = useRef<IActionRef>(null);
    const [modalVisible, setModalVisible] = useState(false);
    const [readonly, setReadonly] = useState(false);
    const [title, setTitle] = useState("新增角色");
    let [roleId, setRoleId] = useState(-1);

    const getResourceList = (_: any, { current, pageSize }: { current: number; pageSize: number }) => {
        return Api.getRoleList({
            currentPage: current,
            pageSize,
        }).then((res) => {
            if (res.code === 1) {
                return {
                    total: res.data.totalCount,
                    data: res.data.data,
                };
            }
        });
    };

    const handleDelete = (record: IRoleProps) => {
        Modal.confirm({
            title: `删除角色后不可恢复，确认删除角色 ${record.name}?`,
            okText: '确认',
            onOk() {
                Api.deleteRole({
                    roleId: record.id,
                }).then((res: any) => {
                    if (res.code === 1) {
                        message.success('角色删除成功');
                        actionRef.current?.submit();
                    }
                });
            },
        });
    };

    const newRole = () => {
		setRoleId(-1);
        setModalVisible(true);
    };

    const onCancel = () => {
        setModalVisible(false);
		setRoleId(-1);
    };

    const onSubmit = (params: { id:number, name: string,remark:string,
		dataSourceIdList:[],userIdList:[],groupIdList:[] }) => {
        if(!readonly) {
			Api.addRole({ ...params }).then((res) => {
				if (res.code === 1) {
					onCancel();
					history.push({
						query: {
							drawer: DRAWER_MENU_ENUM.ROLE,
							name: params.name,
							roleId: res.data.toString(),
						},
					});
					if(params.id == -1) {
						message.success('角色新增成功！');
					} else {
						message.success('角色修改成功！');
					}
					setRoleId(-1)
					actionRef.current?.submit();
				}
			});
		} else {
			onCancel()
		}
    };

    const viewRole = (record: IRoleProps) => {
		setRoleId(record.id)
		setTitle("查看角色")
		setModalVisible(true)
		setReadonly(true)
    };

	const updateRole = (record: IRoleProps) => {
		setRoleId(record.id)
		setTitle("修改角色")
		setModalVisible(true)
		setReadonly(false)
	};


    const columns: ColumnsType<IRoleProps> = [
        {
            title: '角色名称',
            dataIndex: 'name',
        },
		{
			title: '备注',
			dataIndex: 'remark',
		},
        {
            title: '修改时间',
            dataIndex: 'gmtModified',
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
                        <a onClick={() => viewRole(record)}>查看</a>
                        <a onClick={() => updateRole(record)}>修改</a>
                        <a onClick={() => handleDelete(record)}>删除</a>
                    </Space>
                );
            },
        },
    ];
    return (
        <>
            <Sketch<IRoleProps, Record<string, never>>
                extra={
                    <Button type="primary" onClick={() => newRole()}>
                        新增角色
                    </Button>
                }
                actionRef={actionRef}
                request={getResourceList}
                columns={columns}
                tableProps={{
                    rowSelection: undefined,
                }}
            />
			<AddRoleModal title={title} open={modalVisible} onCancel={onCancel}  okButtonProps={{disabled: readonly}}
						  onOk={onSubmit} roleId={roleId} readonly={readonly} />
        </>
    );
}
