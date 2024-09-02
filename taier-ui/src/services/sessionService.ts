import { singleton } from 'tsyringe';
import API from '@/api';
import { EventEmitter } from 'events';

@singleton()
export default class SessionService extends EventEmitter {
    private sessions: Map<number, string> = new Map();
    private sessionStatus: Map<number, boolean> = new Map();

    constructor() {
		super();
		this.startSessionMonitoring();
	}

	public async recreateSession(params: any): Promise<string> {
        await this.closeSession(params);
        return this.createSession(params);
    }


	public async createSession(params: any): Promise<string> {
        const response = await API.createSession(params);
        const sessionHandle = response.data.identifier;
        this.sessions.set(params.taskId, sessionHandle);
        this.sessionStatus.set(params.taskId, true);
        this.emit('sessionStatusChanged', params.taskId, true);
        console.warn("createSession finished",params.taskId, sessionHandle)
        return sessionHandle;
    }

	public async closeSession(taskId: number): Promise<void> {
		const sessionHandle = this.sessions.get(taskId);
		if (sessionHandle) {
			await API.closeSession(sessionHandle);
			this.sessions.delete(taskId);
			this.sessionStatus.delete(taskId);
			this.emit('sessionStatusChanged', taskId, false);
		}
	}


    public getSession(taskId: number): string | undefined {
        return this.sessions.get(taskId);
    }

private startSessionMonitoring() {
        setInterval(async () => {
            try {
                const response = await API.getAllSessions();
                if (response.code === 1 && Array.isArray(response.data)) {
                    const activeSessions = new Set(response.data.map(session => session.identifier));
                    for (const [taskId, sessionHandle] of this.sessions) {
                        await this.checkSessionStatus(taskId, sessionHandle, activeSessions);
                    }
                } else {
                    console.error('Failed to get sessions or invalid response format');
                }
            } catch (error) {
                console.error('Error in session monitoring:', error);
            }
        }, 30000); // Check every 3 seconds
    }

    private async checkSessionStatus(taskId: number, sessionHandle: string, activeSessions: Set<string>) {
        const isActive = activeSessions.has(sessionHandle);
        if (isActive !== this.sessionStatus.get(taskId)) {
            this.sessionStatus.set(taskId, isActive);
            this.emit('sessionStatusChanged', taskId, isActive);
            if (!isActive) {
                try {
                    await this.recreateSession(taskId);
                } catch (error) {
                    console.error(`Failed to recreate session for task ${taskId}:`, error);
                }
            }
        }
    }
}

