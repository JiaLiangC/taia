

import type {ModalProps, SelectProps} from 'antd';
import {Form, Input, Modal, Select} from 'antd';

import { formItemLayout } from '@/constant';
import './index.scss';
import API from "@/api";
import {useEffect, useState} from "react";
import api from "@/api";

const FormItem = Form.Item;

interface IEngineModalProps extends Omit<ModalProps, 'onOk'> {
    onOk?: (values: { id:number,userIdList:[] }) => void;
	tenantId:number;
	readonly:boolean
}

/**
 * 租户用户绑定表单域组件
 */
export default ({ onOk,tenantId, readonly,...restModalProps }: IEngineModalProps) => {
    const [form] = Form.useForm();
	const [users, setUsers] = useState([]);

	useEffect(() => {
		// 仅在组件首次挂载时运行的代码
		const loadUserList = () => {
			API.listLdapUser({}).then((res) => {
				if (res.code === 1) {
					const options = [];
					for (let i = 0;i < res.data.length; i ++) {
						options.push({
							label: res.data[i].userName,
							value: res.data[i].id
						})
					}
					// @ts-ignore
					setUsers(options)
				}
			});
		};

		loadUserList();

		return () => {
			console.log('组件卸载');
			// 组件卸载时运行的代码
		};
	}, []);


	useEffect(() => {
		console.log('组件挂载', tenantId);
		if(tenantId !== -1) {
			api.queryTenantInfo({ tenantId: tenantId}).then((res) => {
				if (res.code === 1) {
					let tenant = res.data;
					form.setFieldsValue({
						tenantName: tenant.tenantName,
						tenantIdentity: tenant.tenantIdentity,
						users:tenant.userIdList
					})
				}
			});
		}

	}, [tenantId])



    const handleSubmit = () => {
        form.validateFields()
            .then((values) => {
                onOk?.({ id:tenantId,userIdList:values.users});
            })
            .catch(() => {});
    };

	const handleChange = (value: string[]) => {
		console.log(`selected ${value}`);
	};

    return (
        <Modal onOk={handleSubmit} className="c-roleManage__modal" {...restModalProps}>
            <Form form={form} autoComplete="off">
                <FormItem
                    label="租户名称"
                    {...formItemLayout}
                    name="tenantName"
                >
                    <Input disabled={true}/>
                </FormItem>
				<FormItem
					label="租户标识"
					{...formItemLayout}
					name="tenantIdentity"
				>
					<Input disabled={true}/>
				</FormItem>

				<FormItem
					label="用户"
					{...formItemLayout}
					name="users"
				>
					<Select
						showSearch
						optionFilterProp="label"
						mode="multiple"
						allowClear
						disabled={readonly}
						style={{ width: '100%' }}
						placeholder="请选择"
						onChange={handleChange}
						options={users}
					/>
				</FormItem>
            </Form>
        </Modal>
    );
};
