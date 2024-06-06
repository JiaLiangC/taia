

import type { ModalProps } from 'antd';
import { Form, Input,Modal } from 'antd';

import { formItemLayout } from '@/constant';
import './index.scss';

const FormItem = Form.Item;

interface IEngineModalProps extends Omit<ModalProps, 'onOk'> {
    onOk?: (values: { name: string, remark:string }) => void;
}

/**
 * 角色表单域组件
 */
export default ({ onOk, ...restModalProps }: IEngineModalProps) => {
    const [form] = Form.useForm();

    const handleSubmit = () => {
        form.validateFields()
            .then((values) => {
                onOk?.({ name: values.name, remark:values.remark });
            })
            .catch(() => {});
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
