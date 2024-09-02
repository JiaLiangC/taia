import molecule from '@dtinsight/molecule';
import { Component } from '@dtinsight/molecule/esm/react';
import { cloneDeep } from 'lodash';
import { container, singleton } from 'tsyringe';

import editorActions from '@/components/scaffolds/editorActions';
import { ID_COLLECTIONS } from '@/constant';
import type { CatalogueDataProps, IOfflineTaskProps } from '@/interface';
import type { IExecuteService } from './executeService';
import ExecuteService from './executeService';
import { taskRenderService, sessionService } from '.';
import { Badge, Switch } from 'antd';


const { RUNNING_TASK, RUN_TASK, STOP_TASK, SESSION_MODE_SWITCH, SESSION_STATUS } = editorActions;

interface IEditorActionBarState {
    runningTab: Set<number>;
    sessionMode: Map<number, boolean>;
    sessionStatus: Map<number, boolean>;
}

interface IEditorActionBarService {
    performSyncTaskActions: () => void;
    toggleSessionMode: (taskId: number) => void;
}

@singleton()
export default class EditorActionBarService
    extends Component<IEditorActionBarState>
    implements IEditorActionBarService
{
    protected state: IEditorActionBarState;
    private executeService: IExecuteService;

    constructor() {
        super();
        this.state = {
            runningTab: new Set(),
            sessionMode: new Map(),
            sessionStatus: new Map(),
        };

        this.executeService = container.resolve(ExecuteService);

        this.executeService.onStartRun(this.handleStartRun);
        this.executeService.onEndRun(this.handleEndRun);
        this.executeService.onStopTab(this.handleStopTab);

        sessionService.on('sessionStatusChanged', this.handleSessionStatusChanged);
    }

    componentWillUnmount() {
        this.executeService.off('onStartRun', this.handleStartRun);
        this.executeService.off('onEndRun', this.handleEndRun);
        this.executeService.off('onStopTab', this.handleStopTab);
        sessionService.off('sessionStatusChanged', this.handleSessionStatusChanged);
    }

    private handleStartRun = (currentTabId: number) => {
        const { current } = molecule.editor.getState();
        this.setState({
            runningTab: this.state.runningTab.add(currentTabId),
        });
        if (current?.activeTab === currentTabId.toString()) {
            molecule.editor.updateActions([
                RUNNING_TASK,
                {
                    id: ID_COLLECTIONS.TASK_STOP_ID,
                    disabled: false,
                },
            ]);
        }
    };

    private handleEndRun = (currentTabId: number) => {
        const { current } = molecule.editor.getState();
        this.state.runningTab.delete(currentTabId);
        this.setState({
            runningTab: this.state.runningTab,
        });
        if (current?.activeTab === currentTabId.toString()) {
            molecule.editor.updateActions([RUN_TASK, STOP_TASK]);
        }
    };

    private handleStopTab = (currentTabId: number) => {
        const { current } = molecule.editor.getState();
        this.state.runningTab.delete(currentTabId);
        this.setState({
            runningTab: this.state.runningTab,
        });
        if (current?.activeTab === currentTabId.toString()) {
            molecule.editor.updateActions([RUN_TASK, STOP_TASK]);
        }
    };

    private handleSessionStatusChanged = (taskId: number, status: boolean) => {
		console.warn("handleSessionStatusChanged taskId:",taskId,status);
		const newSessionState = new Map();
		newSessionState.set(taskId, false); // Initialize to false if null
		this.setState({
			sessionStatus: newSessionState
		});
        this.updateSessionStatusIcon(taskId);
    };

    public performSyncTaskActions = () => {
        const { current } = molecule.editor.getState();
        if (current?.tab?.data) {
            const currentTabData: CatalogueDataProps & IOfflineTaskProps = current?.tab?.data;
            const taskToolbar = cloneDeep(
                taskRenderService.renderEditorActions(currentTabData.taskType, currentTabData)
            );

            taskToolbar.push(SESSION_MODE_SWITCH);
			// if (this.state.sessionMode.get(currentTabData.id)) {
				taskToolbar.push(SESSION_STATUS);
			// }

            molecule.editor.updateGroup(current.id, {
                actions: [...taskToolbar, ...molecule.editor.getDefaultActions()],
            });
            if (this.state.runningTab.has(currentTabData.id)) {
                molecule.editor.updateActions([
                    RUNNING_TASK,
                    {
                        id: ID_COLLECTIONS.TASK_STOP_ID,
                        disabled: false,
                    },
                ]);
            }
            this.updateSessionModeIcon(currentTabData.id);
            this.updateSessionStatusIcon(currentTabData.id);
        } else if (current) {
            molecule.editor.updateGroup(current.id, {
                actions: [...molecule.editor.getDefaultActions()],
            });
        }
    };

	public toggleSessionMode = (taskId: number) => {
		console.warn("toggleSessionMode taskId:",taskId);
        let currentMode = this.state.sessionMode.get(taskId);
		if (currentMode === null || currentMode === undefined) {
			const newSessionMode = new Map();
			newSessionMode.set(taskId, false); // Initialize to false if null
			this.setState({
				sessionMode: newSessionMode
			});
		}
		console.warn("after initialize ",this.state.sessionMode);
		currentMode = this.state.sessionMode.get(taskId);
		const newSessionMode =  new Map(this.state.sessionMode).set(taskId, !currentMode)
		this.setState({
			sessionMode: newSessionMode
		}, () => {
		  this.updateSessionModeIcon(taskId);
		  console.warn("toggleSessionMode finished", this.state.sessionMode, this.state.sessionStatus);
		});
		console.warn("toggleSessionMode finished", this.state.sessionMode, this.state.sessionStatus);
		this.updateSessionStatusIcon(taskId);
		this.emit('sessionModeChanged', taskId);
    };


	private updateSessionModeIcon(taskId: number) {
        const isSessionMode = this.state.sessionMode.get(taskId) || false;
        molecule.editor.updateActions([
            {
                id: ID_COLLECTIONS.SESSION_MODE_SWITCH,
                title: isSessionMode ? 'Session 模式 (开启)' : 'Session 模式 (关闭)',
            },
        ]);
    }

  	private updateSessionStatusIcon(taskId: number) {
		console.warn("updateSessionStatusIcon taskId:",taskId,this.state.sessionMode.get(taskId));
        const isSessionMode = this.state.sessionMode.get(taskId) || false;
        const isConnected = this.state.sessionStatus.get(taskId) || false;

		if (isSessionMode) {
		 molecule.editor.updateActions([
			 {
				 id: ID_COLLECTIONS.SESSION_STATUS,
				 icon: isConnected ? <Badge status="success" text="" /> : <Badge status="default" text="" />,
				 title: isConnected ? 'Session 连接正常' : 'Session 连接断开',
			 },
		 ]);
		}else {
			molecule.editor.updateActions([
				{
					id: ID_COLLECTIONS.SESSION_STATUS,
					icon: isConnected ? <Badge status="success" text="" /> : <Badge status="default" text="" />,
					title: isConnected ? 'Session 连接正常' : 'Session 连接断开',
				},
			]);
		}
     }


    public getSessionMode(taskId: number): boolean {
        return this.state.sessionMode.get(taskId) || false;
    }
}

