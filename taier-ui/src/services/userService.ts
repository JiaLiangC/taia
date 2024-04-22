
import { Component } from '@dtinsight/molecule/esm/react';
import Base64 from 'base-64';

import api from '@/api';
import {IUserDsProps} from '@/interface';


export interface IUserState {
	user: IUserDsProps[];
}

interface IUserService {
	getUser: () => IUserDsProps[];
	reloadUser: () => void;
}

export default class UserService extends Component<IUserState> implements IUserService {
	protected state: IUserState = {
		user: [],
	};

	constructor() {
		super();
		this.queryUser();
	}

	private queryUser = () => {
		api.getAllUser({}).then((res) => {
			if (res.code === 1) {
				const nextData: IUserDsProps[] = ((res.data as IUserDsProps[]) || []).map((ele) => {
					const canConvertLinkJson =
						ele.linkJson && !ele.linkJson.includes('{') && !ele.linkJson.includes('}');

					return {
						...ele,
						linkJson: canConvertLinkJson ? Base64.decode(ele.linkJson!) : ele.linkJson,
					};
				});

				this.setState({
					user: nextData,
				});
			}
		});
	};

	getUser = () => {
		return this.state.user || [];
	};

	reloadUser = () => {
		this.queryUser();
	};
}

