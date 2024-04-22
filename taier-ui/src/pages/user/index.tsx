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

import { useMemo, useState } from 'react';
import { ExclamationCircleOutlined } from '@ant-design/icons';
import molecule from '@dtinsight/molecule';
import { getEventPosition } from '@dtinsight/molecule/esm/common/dom';
import { ActionBar, Menu, useContextViewEle } from '@dtinsight/molecule/esm/components';
import { connect } from '@dtinsight/molecule/esm/react';
import { Content, Header } from '@dtinsight/molecule/esm/workbench/sidebar';
import { Empty, message, Modal } from 'antd';
import classNames from 'classnames';

import API from '@/api';
import { DetailInfoModal } from '@/components/detailInfo';
import {CATALOGUE_TYPE, ID_COLLECTIONS} from '@/constant';
import type {IUserDsProps} from '@/interface';
import {catalogueService, userService} from '@/services';
import {IUserState} from "@/services/userService";
import Add from './add';
// import Search from "@/pages/dataSource/search";
import './index.scss';
import {localize} from "@dtinsight/molecule/esm/i18n/localize";
// import Create from "@/components/task/create";
import EditFolder from "@/components/task/editFolder";

const { confirm } = Modal;

interface IOther {
    search: string;
}

const UserView = ({ user }: IUserState) => {
    const [other, setOther] = useState<IOther>({
        search: '',
    });

    const [visible, setVisible] = useState<boolean>(false);
    const [detailView, setView] = useState<IUserDsProps | undefined>(undefined);

    const contextView = useContextViewEle();

	const handleSearch = (value: Record<string, any>) => {
		const data = { ...other, ...value };
		setOther(data);
	};

	const tabData = {
		id: ID_COLLECTIONS.EDIT_USER_PREFIX,
		// modified: false,
		name: '编辑用户',
		icon: 'edit',
		breadcrumb: [
			{
				id: 'root',
				name: '用户中心',
			},
			{
				id: ID_COLLECTIONS.EDIT_DATASOURCE_PREFIX,
				name: '编辑用户',
			},
		],
		// data: {
		// 	// always create task under the root node or create task from context menu
		// 	nodePid: id || folderTree.data?.[0].id,
		// },
		renderPane: () => {
			return <Add />;
		},
	};

    const handleMenuClick = (menu: { id: string; name: string }, record: IUserDsProps) => {
        contextView?.hide();
        switch (menu.id) {
            case 'edit':
                if (molecule.editor.isOpened(ID_COLLECTIONS.EDIT_USER_PREFIX)) {
                    message.warning('请先保存或关闭编辑的用户');
                    const groupId = molecule.editor.getGroupIdByTab(ID_COLLECTIONS.EDIT_USER_PREFIX)!;
                    molecule.editor.setActive(groupId, ID_COLLECTIONS.EDIT_USER_PREFIX);
                } else {
                    molecule.editor.open(tabData);
                }
                break;
            default:
                break;
        }
    };

    const handleContextmenu = (e: React.MouseEvent<HTMLLIElement, MouseEvent>, record: IUserDsProps) => {
        e.preventDefault();
        e.currentTarget.focus();
        contextView?.show(getEventPosition(e), () => (
            <Menu
                role="menu"
                onClick={(_: any, item: any) => handleMenuClick(item, record)}
                data={[
                    {
                        id: 'edit',
                        name: '编辑',
                    },
                ]}
            />
        ));
    };

    const renderFilterUser = (item: IUserDsProps) => {
        if (other.search) {
            return item.userName.includes(other.search);
        }

        return true;
    };

    const filterUser = useMemo(() => user.filter(renderFilterUser), [user, other]);

    return (
        <div className="datasource-container">
			<Header
				title="用户管理中心"
				toolbar={
					<ActionBar
						data={[
							{
								id: 'add',
								title: '',
								icon: '',
								// contextMenu: [],
							},
						]}
					/>
				}
			/>

            <Content>
				{/*<Search onSearch={handleSearch} />*/}
				{filterUser.length ? (
					<div tabIndex={0} className="datasource-content">
						<ul className="datasource-list">
							{filterUser.map((item) => (
								<li
									key={item.userId}
									tabIndex={-1}
									className="datasource-record"
									// onClick={() => handleOpenDetail(item)}
									onContextMenu={(e) => handleContextmenu(e, item)}
								>
									<div className="datasource-title">
                                        <span className="title" title={item.userName}>
                                            {item.userName}
											{/*({item.dataType}*/}
											{/*{item.dataVersion || ''})*/}
                                        </span>
										{/*<span className={classNames('desc')}>{item.dataDesc || '--'}</span>*/}
									</div>
								</li>
							))}
						</ul>
					</div>
				) : (
					<Empty image={Empty.PRESENTED_IMAGE_SIMPLE} />
				)}
            </Content>
        </div>
    );
};

export default connect(userService, UserView);
