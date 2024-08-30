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

package com.dtstack.taier.develop.service.develop.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dtstack.taier.common.enums.EScheduleJobType;
import com.dtstack.taier.common.enums.TempJobType;
import com.dtstack.taier.common.exception.TaierDefineException;
import com.dtstack.taier.common.util.JsonUtils;
import com.dtstack.taier.common.util.MathUtil;
import com.dtstack.taier.dao.domain.DevelopSelectSql;
import com.dtstack.taier.dao.domain.ScheduleJob;
import com.dtstack.taier.dao.domain.ScheduleTaskShade;
import com.dtstack.taier.dao.domain.Task;
import com.dtstack.taier.datasource.api.base.ClientCache;
import com.dtstack.taier.datasource.api.client.IClient;
import com.dtstack.taier.datasource.api.dto.source.ISourceDTO;
import com.dtstack.taier.datasource.api.dto.source.RdbmsSourceDTO;
import com.dtstack.taier.datasource.api.source.DataSourceType;
import com.dtstack.taier.datasource.api.utils.DBUtil;
import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import com.dtstack.taier.datasource.plugin.common.operation.FetchOrientation;
import com.dtstack.taier.datasource.plugin.common.operation.OperationHandle;
import com.dtstack.taier.datasource.plugin.common.session.Session;
import com.dtstack.taier.datasource.plugin.common.session.SessionHandle;
import com.dtstack.taier.develop.dto.devlop.ExecuteResultVO;
import com.dtstack.taier.develop.dto.devlop.SessionData;
import com.dtstack.taier.develop.service.develop.ITaskRunner;
import com.dtstack.taier.develop.service.develop.TaskConfiguration;
import com.dtstack.taier.develop.service.develop.runner.JdbcTaskRunner;
import com.dtstack.taier.develop.service.schedule.JobService;
import com.dtstack.taier.develop.service.session.JdbcBackendService;
import com.dtstack.taier.develop.utils.ApiUtils;
import com.dtstack.taier.develop.vo.develop.result.DevelopGetSyncTaskStatusInnerResultVO;
import com.dtstack.taier.develop.vo.develop.result.DevelopStartSyncResultVO;
import com.dtstack.taier.pluginapi.constrant.ConfigConstant;
import com.dtstack.taier.pluginapi.enums.TaskStatus;
import com.dtstack.taier.pluginapi.exception.ExceptionUtil;
import com.dtstack.taier.scheduler.impl.pojo.ParamActionExt;
import com.dtstack.taier.scheduler.impl.pojo.ParamTaskAction;
import com.dtstack.taier.scheduler.service.ScheduleActionService;
import com.dtstack.taier.scheduler.vo.action.ActionJobEntityVO;
import com.dtstack.taier.scheduler.vo.action.ActionLogVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Service
public class DevelopJobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevelopJobService.class);

    private static final String DOWNLOAD_URL = ConfigConstant.REQUEST_PREFIX + "/developDownload/downloadJobLog?jobId=%s&taskType=%s&tenantId=%s";

    @Autowired
    private DevelopTaskService developTaskService;

    @Autowired
    private DevelopServerLogService developServerLogService;

    @Autowired
    private DevelopSelectSqlService developSelectSqlService;

    @Autowired
    private ScheduleActionService actionService;

    @Autowired
    private JobService jobService;

    @Autowired
    private TaskConfiguration taskConfiguration;

    @Autowired
    private JobParamReplace jobParamReplace;


    @Autowired
    private JdbcBackendService jdbcBackendService;

    /**
     * 运行同步任务
     *
     * @return
     */
    public DevelopStartSyncResultVO startSyncImmediately(Long taskId, Long userId, Boolean isRoot, Long tenantId) {
        DevelopStartSyncResultVO developStartSyncResultVO = new DevelopStartSyncResultVO();
        developStartSyncResultVO.setMsg(null);
        developStartSyncResultVO.setJobId(null);
        developStartSyncResultVO.setStatus(TaskStatus.SUBMITTING.getStatus());
        Task task = developTaskService.getOneWithError(taskId);
        if (!EScheduleJobType.SYNC.getVal().equals(task.getTaskType())) {
            throw new TaierDefineException("只支持同步任务直接运行");
        }
        try {
            ITaskRunner taskRunner = taskConfiguration.get(EScheduleJobType.SYNC.getType());
            Map<String, Object> actionParam = taskRunner.readyForSyncImmediatelyJob(task, tenantId, isRoot);
            String extraInfo = JSON.toJSONString(actionParam);
            ParamTaskAction paramTaskAction = new ParamTaskAction();
            ScheduleTaskShade scheduleTaskShade = JSON.parseObject(extraInfo, ScheduleTaskShade.class);
            scheduleTaskShade.setExtraInfo(extraInfo);
            scheduleTaskShade.setTaskId(task.getId());
            scheduleTaskShade.setScheduleConf(task.getScheduleConf());
            scheduleTaskShade.setComponentVersion(task.getComponentVersion());
            scheduleTaskShade.setQueueName(task.getQueueName());
            paramTaskAction.setTask(scheduleTaskShade);
            ParamActionExt paramActionExt = actionService.paramActionExt(paramTaskAction.getTask(), paramTaskAction.getJobId(), paramTaskAction.getFlowJobId());
            String jobId = paramActionExt.getJobId();
            actionService.start(paramActionExt);
            String name = MathUtil.getString(actionParam.get("name"));
            String job = MathUtil.getString(actionParam.get("job"));
            developSelectSqlService.addSelectSql(jobId, name, TempJobType.SYNC_TASK.getType(), task.getTenantId(),
                    job, userId, EScheduleJobType.SYNC.getType());
            developStartSyncResultVO.setMsg(String.format("任务提交成功,名称为: %s", name));
            developStartSyncResultVO.setJobId(jobId);
            developStartSyncResultVO.setStatus(TaskStatus.SUBMITTING.getStatus());
        } catch (Exception e) {
            LOGGER.warn("startSyncImmediately-->", e);
            developStartSyncResultVO.setMsg(e.getMessage());
            developStartSyncResultVO.setStatus(TaskStatus.SUBMITFAILD.getStatus());
        }
        return developStartSyncResultVO;
    }

    /**
     * 获取同步任务运行状态
     */
    public DevelopGetSyncTaskStatusInnerResultVO getSyncTaskStatus(Long tenantId, String jobId) {
        return this.getSyncTaskStatusInner(tenantId, jobId, 0);
    }

    private DevelopGetSyncTaskStatusInnerResultVO getSyncTaskStatusInner(final Long tenantId, final String jobId, int retryTimes) {
        final DevelopGetSyncTaskStatusInnerResultVO resultVO = new DevelopGetSyncTaskStatusInnerResultVO();
        resultVO.setMsg(null);
        resultVO.setStatus(TaskStatus.RUNNING.getStatus());

        try {
            ScheduleJob job = jobService.getScheduleJob(jobId);
            if (Objects.isNull(job)) {
                resultVO.setMsg("can not found job");
                return resultVO;
            }

            Integer status = TaskStatus.getShowStatus(job.getStatus());
            resultVO.setStatus(status);
            if (TaskStatus.RUNNING.getStatus().equals(status)) {
                resultVO.setMsg("运行中");
            }

            ActionLogVO actionLogVO = actionService.log(jobId);
            String engineLogStr = actionLogVO.getEngineLog();
            String logInfoStr = actionLogVO.getLogInfo();
            if (StringUtils.isNotBlank(engineLogStr)) {
                //移除increConf 信息
                try {
                    JSONObject engineLogJson = JSON.parseObject(engineLogStr);
                    engineLogJson.remove("increConf");
                    engineLogStr = engineLogJson.toJSONString();
                } catch (Exception e) {
                    LOGGER.error("", e);
                    if (TaskStatus.FINISHED.getStatus().equals(status) || TaskStatus.CANCELED.getStatus().equals(status)
                            || TaskStatus.FAILED.getStatus().equals(status)) {
                        resultVO.setMsg(engineLogStr);
                        resultVO.setDownload(String.format(DevelopJobService.DOWNLOAD_URL, jobId, EScheduleJobType.SYNC.getVal(), tenantId));
                    }
                    return resultVO;
                }
            }

            if (StringUtils.isEmpty(engineLogStr) && StringUtils.isEmpty(logInfoStr)) {
                return resultVO;
            }

            try {
                final JSONObject engineLog = JSON.parseObject(engineLogStr);
                final JSONObject logIngo = JSON.parseObject(logInfoStr);
                final StringBuilder logBuild = new StringBuilder();
                List<ActionJobEntityVO> engineEntities = actionService.entitys(Collections.singletonList(jobId));

                String engineJobId = "";
                if (CollectionUtils.isNotEmpty(engineEntities)) {
                    engineJobId = engineEntities.get(0).getEngineJobId();
                }
                final long startTime = Objects.isNull(job.getExecStartTime()) ? System.currentTimeMillis() : job.getExecStartTime().getTime();
                final String perf = StringUtils.isBlank(engineJobId) ? null : this.developServerLogService.formatPerfLogInfo(engineJobId, jobId, startTime, System.currentTimeMillis(), tenantId);
                if (StringUtils.isNotBlank(perf)) {
                    logBuild.append(perf.replace("\n", "  "));
                }

                if (TaskStatus.FAILED.getStatus().equals(status)) {
                    // 失败的话打印失败日志
                    logBuild.append("\n");
                    logBuild.append("====================Flink日志====================\n");

                    if (engineLog != null) {
                        if (StringUtils.isEmpty(engineLog.getString("root-exception")) && retryTimes < 3) {
                            retryTimes++;
                            Thread.sleep(500);
                            return this.getSyncTaskStatusInner(tenantId, jobId, retryTimes);
                        } else {
                            if (engineLog.containsKey("engineLogErr")) {
                                // 有这个字段表示日志没有获取到，目前engine端只对flink任务做了这种处理，这里先提前加上
                                logBuild.append(engineLog.getString("engineLogErr"));
                            } else {
                                logBuild.append(engineLog.getString("root-exception"));
                            }
                            logBuild.append("\n");
                        }
                    }

                    if (logIngo != null) {
                        logBuild.append(logIngo.getString("msg_info"));
                        logBuild.append("\n");
                    }

                    final DevelopSelectSql developHiveSelectSql = this.developSelectSqlService.getByJobId(jobId, tenantId, 0);
                    if (developHiveSelectSql != null) {
                        logBuild.append("====================任务信息====================\n");
                        final String sqlLog = developHiveSelectSql.getCorrectSqlText().replaceAll("(\"password\"[^\"]+\")([^\"]+)(\")", "$1**$3");
                        logBuild.append(JsonUtils.formatJSON(sqlLog));
                        logBuild.append("\n");
                    }
                }
                if (TaskStatus.FINISHED.getStatus().equals(status) || TaskStatus.CANCELED.getStatus().equals(status)
                        || TaskStatus.FAILED.getStatus().equals(status)) {
                    resultVO.setDownload(String.format(DevelopJobService.DOWNLOAD_URL, jobId, EScheduleJobType.SYNC.getVal(), tenantId));
                }
                resultVO.setMsg(logBuild.toString());
            } catch (Exception e) {
                // 日志解析失败，可能是任务失败，日志信息为非json格式
                LOGGER.error("", e);
                resultVO.setMsg(StringUtils.isEmpty(engineLogStr) ? "job run fail" : engineLogStr);
            }
        } catch (Exception e) {
            LOGGER.error("get job {} status error", jobId, e);
        }

        return resultVO;
    }

    /**
     * 停止同步任务
     */
    public void stopSyncJob(String jobId) {
        actionService.stop(Collections.singletonList(jobId));
    }


    /**
     * 运行SQL任务
     *
     * @param userId
     * @param tenantId
     * @param taskId
     * @param sql
     * @param taskVariables
     * @return
     */
    public ExecuteResultVO startSqlImmediately(Long userId, Long tenantId, Long taskId, String sql, List<Map<String, Object>> taskVariables) {
        ExecuteResultVO result = new ExecuteResultVO();
        try {
            Task task = developTaskService.getOneWithError(taskId);
            sql = jobParamReplace.paramReplace(sql, taskVariables, DateTime.now().toString("yyyyMMddHHmmss"));
            task.setSqlText(sql);
            ITaskRunner taskRunner = taskConfiguration.get(task.getTaskType());

            result = taskRunner.startSqlImmediately(userId, tenantId, sql, task, taskVariables);
            result.setTaskType(task.getTaskType());
        } catch (Exception e) {
            LOGGER.warn("startSqlImmediately-->", e);
            result.setMsg(ExceptionUtil.getErrorMessage(e));
            result.setStatus(TaskStatus.FAILED.getStatus());
            result.setSqlText(sql);
            return result;
        }
        return result;
    }


    //open session
    //close session
    //execute statement
    //cancel statement
    //operation rowset


    /*
    * open session and return session handle
    * */
    public String openSession(Long userId, Long tenantId, Long taskId) {
        Task task = developTaskService.getOneWithError(taskId);
        JdbcTaskRunner taskRunner = (JdbcTaskRunner) taskConfiguration.get(task.getTaskType());
        SessionHandle sessionHandle = null;
        try {
            Connection connection = taskRunner.getCon(userId, tenantId, task);
             sessionHandle = jdbcBackendService.openSession(connection, "user", "password", "", new HashMap<>());
            LOGGER.info("openSession: ", sessionHandle.getIdentifier().toString());
        } catch (TaierSQLException e) {
            LOGGER.info("openSession-->", e);
        }
        return sessionHandle.getIdentifier().toString();
    }

    /*
     *
     * use task runner
     **/
    public String executeStatement(String sessionHandler, String sql) throws TaierSQLException {
        OperationHandle operationHandle = jdbcBackendService.executeStatement(SessionHandle.fromUUID(sessionHandler), sql, new HashMap<>(), false, 50000);
        return operationHandle.getIdentifier().toString();
    }


   /* public  ExecuteResultVO fetchResults(
            String operationHandle,
            FetchOrientation orientation,
            int maxRows,
            boolean fetchLog,Long taskId)  {
        ExecuteResultVO result = new ExecuteResultVO();
        Task task = developTaskService.getOneWithError(taskId);
        JdbcTaskRunner taskRunner = (JdbcTaskRunner) taskConfiguration.get(task.getTaskType());
        try {
            result =  taskRunner.fetchResults(OperationHandle.fromString(operationHandle), orientation, maxRows, fetchLog);
            return result;
        } catch (TaierSQLException e) {
            LOGGER.warn("fetchResults-->", e);
            result.setMsg(ExceptionUtil.getErrorMessage(e));
            result.setStatus(TaskStatus.FAILED.getStatus());
            return result;
        }
    }*/

    public  ExecuteResultVO fetchResults(
            String operationHandle,
            FetchOrientation orientation,
            int maxRows,
            boolean fetchLog) throws TaierSQLException {
        List<List<Object>> returnList = new ArrayList<>();

        ExecuteResultVO<List<Object>> executeResult = new ExecuteResultVO<>();
        List<Map<String, Object>> list = new ArrayList<>();
        list = jdbcBackendService.fetchResults(OperationHandle.fromString(operationHandle), orientation, maxRows, fetchLog);

        if (CollectionUtils.isNotEmpty(list)) {
            returnList.add(list.get(0).keySet().stream().collect(Collectors.toList()));
            for (Map<String, Object> result : list) {
                List<Object> value = new ArrayList<>(result.values());
                returnList.add(value);
            }
        }

        executeResult.setResult(returnList);
        executeResult.setStatus(TaskStatus.FINISHED.getStatus());
        return  executeResult;
    }

    public void closeSession(String sessionHandleStr) throws TaierSQLException {
        jdbcBackendService.closeSession(SessionHandle.fromUUID(sessionHandleStr));
    }

    public void cancelOperation(String operationHandle) throws TaierSQLException {
        jdbcBackendService.cancelOperation(OperationHandle.fromString(operationHandle));
    }

    public void closeOperation(String operationHandle) throws TaierSQLException {
        jdbcBackendService.closeOperation(OperationHandle.fromString(operationHandle));
    }


    public List<SessionData> allSessions() {
        List<SessionData> list = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(jdbcBackendService.getSessionManager().allSessions().iterator(), Spliterator.ORDERED),
                        false
                ).map(session -> ApiUtils.sessionData((Session) session))
                .collect(Collectors.toList());
        return list;
    }

    /*
    *
    * use task runner
    **/
/*    public String executeStatement(String sessionHandler, Long taskId, String sql)  {
        try {
            Task task = developTaskService.getOneWithError(taskId);
            task.setSqlText(sql);
            JdbcTaskRunner taskRunner = (JdbcTaskRunner) taskConfiguration.get(task.getTaskType());
            OperationHandle operationHandle = taskRunner.executeStatement(task, sessionHandler);
            return operationHandle.getIdentifier().toString();
        } catch (Exception e) {
            LOGGER.warn("executeStatement-->", e);
            return "";
        }
    }*/



    /**
     * 停止通过sql任务执行的sql查询语句
     */
    public void stopSqlImmediately(String jobId, Long tenantId) {
        if (StringUtils.isNotBlank(jobId)) {
            this.developSelectSqlService.stopSelectJob(jobId, tenantId);
        }
    }

}

