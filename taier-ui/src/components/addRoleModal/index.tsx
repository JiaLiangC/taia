

import type {ModalProps, SelectProps} from 'antd';
import {Form, Input, Modal, Select} from 'antd';

import { formItemLayout } from '@/constant';
import './index.scss';
import API from "@/api";
import {useEffect, useState} from "react";

const FormItem = Form.Item;

interface IEngineModalProps extends Omit<ModalProps, 'onOk'> {
    onOk?: (values: { name: string, remark:string }) => void;
}

/**
 * 角色表单域组件
 */
export default ({ onOk,...restModalProps }: IEngineModalProps) => {
    const [form] = Form.useForm();
	const [dsOptions, setDsOptions] = useState([]);
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
					console.log(options)
					// @ts-ignore
					setDsOptions(options)
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
                onOk?.({ name: values.name, remark:values.remark });
            })
            .catch(() => {});
    };

	const options: SelectProps['options'] = [];
	for (let i = 10; i < 36; i++) {
		options.push({
			label: i.toString(36) + i,
			value: i.toString(36) + i,
		});
	}

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
						options={options}
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
						options={options}
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
