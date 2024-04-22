
import { useRef,useState } from 'react';
import { SyncOutlined } from '@ant-design/icons';
import molecule from '@dtinsight/molecule';
import { Scrollbar } from '@dtinsight/molecule/esm/components';
import type { FormInstance } from 'antd';
import { Button, message,Spin, Steps } from 'antd';
import Base64 from 'base-64';

import API from '@/api';
import { ID_COLLECTIONS } from '@/constant';
import type { IUserDsProps } from '@/interface';
import { utf16to8 } from '@/utils';
// import InfoConfig from './InfoConfig';
// import SelectSource from './selectSource';
// import Version from './version';
import './add.scss';
import SelectSource from "@/pages/dataSource/selectSource";
import Version from "@/pages/dataSource/version";
import InfoConfig from "@/pages/dataSource/InfoConfig";
import {IDataSourceType} from "@/pages/dataSource/add";
import {connect} from "@dtinsight/molecule/esm/react";

// const { Step } = Steps;

const STEPS = ['选择数据源'];
//
// interface IAddProps {
// 	record?: IUserDsProps;
// 	onSubmit?: () => void;
// }
//
// interface IUser {
// 	selectedMenu?: string;
// 	currentUser?: number;
// }


const Add = connect(
	molecule.editor,
	()=>{


		return(
			<div>
				test
			</div>
		);
	}
)

// export default function Add() {
// 	// const isEdit = !!record;
// 	// const [current, setCurrent] = useState<number>(isEdit ? STEPS.length - 1 : 0);
// 	// const [user, setUser] = useState<IUser>({});
//
//
//
//
// 	return (
// 		// <Scrollbar>
// 			<div className="source">
// 				{/*
// 				<div className="content">
// 					<div className="top-steps">
// 						<Steps current={current} size="small">
// 							{STEPS.map((title) => (
// 								<Step title={title} key={title} disabled={isEdit && title !== '信息配置'} />
// 							))}
// 						</Steps>
// 					</div>
// 					<div className="step-info">{switchContent(current)}</div>
// 					<div className="footer-select">{switchFooter(current)}</div>
// 				</div>
// 				*/}
// 			</div>
// 		// </Scrollbar>
// 	);
// }


export default Add as () => JSX.Element;
