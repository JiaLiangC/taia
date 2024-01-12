import { useState } from 'react';
import {Alert, Button, message, Modal} from 'antd';

import { getTenantId, getUserId } from '@/utils';
import ajax from '../../api';

export const AUDIT_ID = 'audit_wrapper';

export default ({ taskId }: { taskId: number }) => {
	const [visible, changeVisible] = useState(true);
	const [loading, changeLoading] = useState(false);

	const onPass=()=>{
		// console.log(taskId)

		changeLoading(true);
		ajax.auditPassTask({
			taskId: taskId,
			userId: getUserId(),
		})
			.then((res)=>{
				const {code}=res;
				if(code===1){
					if(res.data){
						message.success('审核已通过');
					}else{
						message.error('用户没有审核权限，请联系管理员进行任务审核');
					}
					changeVisible(false);
				}
			})
			.finally(()=>{
				changeLoading(false);
			});
	};

	const onUnPass=()=>{
		changeLoading(true);
		ajax.auditUnPassTask({
			taskId:taskId,
			userId: getUserId(),
		})
			.then((res)=>{
				const {code}=res;
				if(code===1){
					if(res.data){
						message.success('审核未通过');
					}else{
						message.error('用户没有审核权限，请联系管理员进行任务审核');
					}
					changeVisible(false);
				}
			})
			.finally(()=>{
				changeLoading(false);
			});
	};

	return (
		<Modal
			wrapClassName="vertical-center-modal"
			title="审核任务"
			getContainer={() => document.getElementById(AUDIT_ID)!}
			prefixCls="ant-modal"
			style={{ height: '600px', width: '600px' }}
			visible={visible}
			onCancel={() => changeVisible(false)}
			// onOk={() => checkPublishTask()}
			// okText="通过"
			confirmLoading={loading}
			// cancelText="关闭"
			footer={[
				<Button key="pass" onClick={onPass} style={{backgroundColor:'#30E580'}}>
					通过
				</Button>,
				<Button key="unpass" onClick={onUnPass}>
					不通过
				</Button>,
			]}
		>
			<Alert message="管理员请确认检查任务代码后，对任务进行审核" type="info" closable={false} />
		</Modal>
	);
}
