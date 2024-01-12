import { useContext,useMemo, useRef, useState } from 'react';
import molecule from '@dtinsight/molecule';
import type { FormInstance } from 'antd';
import {Button, Checkbox, Divider, message, Space, Tabs} from 'antd';
import type { ColumnsType } from 'antd/lib/table/interface';
import moment from 'moment';
import { history } from 'umi';

import type { IActionRef } from '@/components/sketch';
import Sketch from '@/components/sketch';
import Api from "@/api";
import {DRAWER_MENU_ENUM} from "@/constant";
import context from "@/context";
import Publish, {CONTAINER_ID} from "@/components/task/publish";
import {createRoot} from "react-dom/client";



interface IRecordProps {
	taskId: number;
	version: number;
	gmtCreate: number;
	modifyUserName: string;
	sqlText: string;
}

interface IFormFieldProps {
	id?:string;
	currentPage: number;
	pageSize: number;
}

export default () => {
	const { supportJobTypes } = useContext(context);
	const [visibleSlidePane, setVisible] = useState(false);
	const [patchDataVisible, setPatchVisible] = useState(false);

	const actionRef = useRef<IActionRef>(null);

	const getRecordList = async (
		params: IFormFieldProps,
		{ current, pageSize }: { current: number; pageSize: number },
	) => {
		const taskid = history.location.query?.tid
		let num: number = taskid as unknown as number;
		return await Api.getTaskRecordList({
			taskId: num,
			currentPage: current,
			pageSize,
		}).then((res) => {
			if (res.code === 1) {
				// console.log(res.data)
				return {
					total: res.data.totalCount,
					data: res.data,
				};
			}
		});
	};

	const viewDetail = (record: IRecordProps) => {
		history.push({
			query: {
				drawer: DRAWER_MENU_ENUM.RECORD_DETAIL,
				rid: record.taskId.toString(),
				rv: record.version.toString(),
			},
		});
	};

	const columns: ColumnsType<IRecordProps> = [
		{
			title: '任务ID',
			dataIndex: 'taskId',
		},
		{
			title: '版本',
			dataIndex: 'version',
		},
		{
			title: '修改时间',
			dataIndex: 'gmtCreate',
			render(text) {
				return moment(text).format('YYYY-MM-DD HH:mm:ss');
			},
		},
		{
			title: '修改用户',
			dataIndex: 'modifyUserName',
			// render(text) {
			// 	return moment(text).format('YYYY-MM-DD HH:mm:ss');
			// },
		},
		{
			title: '操作',
			dataIndex: 'deal',
			width: '170px',
			render: (_, record) => {
				return (
					<Space split={<Divider type="vertical" />}>
						<a onClick={() => viewDetail(record)}>查看</a>
					</Space>
				);
			},
		},
	];

	return (
		<>
			<Sketch<IRecordProps, IFormFieldProps>
				actionRef={actionRef}
				request={getRecordList}
				columns={columns}
				tableProps={{
					rowSelection: undefined,
				}}
			/>
		</>
	)
}
