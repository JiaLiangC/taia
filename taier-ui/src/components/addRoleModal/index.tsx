

import type {ModalProps, SelectProps} from 'antd';
import {Form, Input, Modal, Select} from 'antd';

import { formItemLayout } from '@/constant';
import './index.scss';
import API from "@/api";
import {useEffect, useState} from "react";

const FormItem = Form.Item;

interface IEngineModalProps extends Omit<ModalProps, 'onOk'> {
    onOk?: (values: { name: string, remark:string,dataSourceIdList:[],userIdList:[],groupIdList:[] }) => void;
}

/**
 * 角色表单域组件
 */
export default ({ onOk,...restModalProps }: IEngineModalProps) => {
    const [form] = Form.useForm();
	const [dsOptions, setDsOptions] = useState([]);
	const [users, setUsers] = useState([]);
	const [groups, setGroups]= useState([]);
	useEffect(() => {
		console.log('组件挂载');
		// 仅在组件首次挂载时运行的代码
		const loadSourceList = () => {
			API.getAllDataSource({}).then((res) => {
				if (res.code === 1) {
					const options = [];
					for (let i = 0;i < res.data.length; i ++) {
						options.push({
							label: res.data[i].dataName,
							value: res.data[i].dataInfoId
						})
					}
					// @ts-ignore
					setDsOptions(options)
				}
			});

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

			API.listLdapGroup({}).then((res) => {
				if (res.code === 1) {
					const options = [];
					for (let i = 0;i < res.data.length; i ++) {
						options.push({
							label: res.data[i].name,
							value: res.data[i].id
						})
					}
					// @ts-ignore
					setGroups(options)
				}
			});
		};
		loadSourceList();

		return () => {
			console.log('组件卸载');
			// 组件卸载时运行的代码
		};
	}, []);



    const handleSubmit = () => {
        form.validateFields()
            .then((values) => {
                onOk?.({ name: values.name, remark:values.remark,
					dataSourceIdList:values.dataSources,
					userIdList:values.users,
					groupIdList:values.groups });
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
                    label="角色名称"
                    {...formItemLayout}
                    name="name"
                    rules={[
                        {
                            required: true,
                            message: '角色名称不可为空！',
                        }
                    ]}
                >
                    <Input placeholder="请输入角色名称" />
                </FormItem>
				<FormItem
					label="数据源"
					{...formItemLayout}
					name="dataSources"
					rules={[
						{
							required: true,
							message: '数据源不可为空！',
						}
					]}
				>
					<Select
						mode="multiple"
						allowClear
						style={{ width: '100%' }}
						placeholder="请选择"
						onChange={handleChange}
						options={dsOptions}
					/>
				</FormItem>
				<FormItem
					label="用户"
					{...formItemLayout}
					name="users"
				>
					<Select
						mode="multiple"
						allowClear
						style={{ width: '100%' }}
						placeholder="请选择"
						onChange={handleChange}
						options={users}
					/>
				</FormItem>
				<FormItem
					label="用户组"
					{...formItemLayout}
					name="groups"
				>
					<Select
						mode="multiple"
						allowClear
						style={{ width: '100%' }}
						placeholder="请选择"
						onChange={handleChange}
						options={groups}
					/>
				</FormItem>
				<FormItem
					label="备注"
					{...formItemLayout}
					name="remark"
				>
					<Input placeholder="请输入备注" />
				</FormItem>
            </Form>
        </Modal>
    );
};
