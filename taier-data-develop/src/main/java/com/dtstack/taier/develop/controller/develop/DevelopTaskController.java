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

package com.dtstack.taier.develop.controller.develop;

import com.alibaba.fastjson.JSONObject;
import com.dtstack.taier.common.enums.EScheduleStatus;
import com.dtstack.taier.common.exception.ErrorCode;
import com.dtstack.taier.common.exception.TaierDefineException;
import com.dtstack.taier.common.lang.coc.APITemplate;
import com.dtstack.taier.common.lang.web.R;
import com.dtstack.taier.dao.domain.ScheduleTaskShade;
import com.dtstack.taier.dao.domain.TaskRecord;
import com.dtstack.taier.develop.dto.devlop.TaskRecordVO;
import com.dtstack.taier.develop.dto.devlop.TaskResourceParam;
import com.dtstack.taier.develop.dto.devlop.TaskVO;
import com.dtstack.taier.develop.mapstruct.vo.TaskMapstructTransfer;
import com.dtstack.taier.develop.service.develop.impl.DevelopTaskRecordService;
import com.dtstack.taier.develop.service.develop.impl.DevelopTaskService;
import com.dtstack.taier.develop.vo.develop.query.*;
import com.dtstack.taier.develop.vo.develop.result.*;
import com.dtstack.taier.scheduler.service.ScheduleTaskShadeService;
import com.google.common.base.Preconditions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Api(value = "任务管理", tags = {"任务管理"})
@RestController
@RequestMapping(value = "/task")
public class DevelopTaskController {

    @Autowired
    private DevelopTaskService developTaskService;

    @Autowired
    private ScheduleTaskShadeService scheduleTaskShadeService;

    @Autowired
    private DevelopTaskRecordService developTaskRecordService;

    @PostMapping(value = "getTaskById")
    @ApiOperation("数据开发-根据任务id，查询详情")
    public R<DevelopTaskGetTaskByIdResultVO> getTaskById(@RequestBody DevelopScheduleTaskVO developScheduleTaskVO) {
        return new APITemplate<DevelopTaskGetTaskByIdResultVO>() {
            @Override
            protected DevelopTaskGetTaskByIdResultVO process() {
                TaskVO taskById = developTaskService.getTaskById(TaskMapstructTransfer.INSTANCE.DevelopScheduleTaskVToTaskVO(developScheduleTaskVO));
                return TaskMapstructTransfer.INSTANCE.TaskVOToDevelopTaskGetTaskByIdResultVO(taskById);
            }
        }.execute();
    }

    @PostMapping(value = "checkIsLoop")
    @ApiOperation("检查task与依赖的task是否有构成有向环")
    public R<DevelopTaskResultVO> checkIsLoop(@RequestBody DevelopTaskCheckIsLoopVO infoVO) {
        return new APITemplate<DevelopTaskResultVO>() {
            @Override
            protected DevelopTaskResultVO process() {
                return TaskMapstructTransfer.INSTANCE.DevelopTaskToResultVO(developTaskService.checkIsLoop(infoVO.getTaskId(), infoVO.getDependencyTaskId()));
            }
        }.execute();
    }

    @PostMapping(value = "publishTask")
    @ApiOperation("任务发布")
    public R<DevelopTaskPublishTaskResultVO> publishTask(@RequestBody DevelopTaskPublishTaskVO detailVO) {
        return new APITemplate<DevelopTaskPublishTaskResultVO>() {
            @Override
            protected DevelopTaskPublishTaskResultVO process() {
                return TaskMapstructTransfer.INSTANCE.TaskCheckResultVOToDevelopTaskPublishTaskResultVO(developTaskService.publishTask(detailVO.getId(),
                        detailVO.getUserId()));
            }
        }.execute();
    }


    @PostMapping(value = "addOrUpdateTask")
    @ApiOperation("数据开发-新建/更新 任务")
    public R<TaskCatalogueResultVO> addOrUpdateTask(@RequestBody DevelopTaskResourceParamVO paramVO) {
        return new APITemplate<TaskCatalogueResultVO>() {
            @Override
            protected TaskCatalogueResultVO process() {
                TaskResourceParam taskResourceParam = TaskMapstructTransfer.INSTANCE.TaskResourceParamVOToTaskResourceParam(paramVO);
                return TaskMapstructTransfer.INSTANCE.TaskVOToResultVO(developTaskService.addOrUpdateTask(taskResourceParam));
            }
        }.execute();
    }

    @PostMapping(value = "guideToTemplate")
    @ApiOperation("向导模式转模版")
    public R<TaskCatalogueResultVO> guideToTemplate(@RequestBody DevelopTaskResourceParamVO paramVO) {
        return new APITemplate<TaskCatalogueResultVO>() {
            @Override
            protected TaskCatalogueResultVO process() {
                TaskResourceParam taskResourceParam = TaskMapstructTransfer.INSTANCE.TaskResourceParamVOToTaskResourceParam(paramVO);
                return TaskMapstructTransfer.INSTANCE.TaskCatalogueVOToResultVO(developTaskService.guideToTemplate(taskResourceParam));
            }
        }.execute();
    }

    @PostMapping(value = "getChildTasks")
    @ApiOperation("获取子任务")
    public R<List<DevelopGetChildTasksResultVO>> getChildTasks(@RequestBody DevelopTaskGetChildTasksVO tasksVO) {
        return new APITemplate<List<DevelopGetChildTasksResultVO>>() {
            @Override
            protected List<DevelopGetChildTasksResultVO> process() {
                return TaskMapstructTransfer.INSTANCE.notDeleteTaskVOsToDevelopGetChildTasksResultVOs(developTaskService.getChildTasks(tasksVO.getTaskId()));
            }
        }.execute();
    }

    @PostMapping(value = "deleteTask")
    @ApiOperation("删除任务")
    public R<Long> deleteTask(@RequestBody DevelopTaskDeleteTaskVO detailVO) {
        return new APITemplate<Long>() {
            @Override
            protected Long process() {
                return developTaskService.deleteTask(detailVO.getTaskId(), detailVO.getUserId());
            }
        }.execute();
    }

    @PostMapping(value = "getSysParams")
    @ApiOperation("获取所有系统参数")
    public R<Collection<DevelopSysParameterResultVO>> getSysParams() {
        return new APITemplate<Collection<DevelopSysParameterResultVO>>() {
            @Override
            protected Collection<DevelopSysParameterResultVO> process() {
                return TaskMapstructTransfer.INSTANCE.DevelopSysParameterCollectionToDevelopSysParameterResultVOCollection(developTaskService.getSysParams());
            }
        }.execute();
    }

    @PostMapping(value = "checkName")
    @ApiOperation("新增离线任务/脚本/资源/自定义脚本，校验名称")
    public R<Void> checkName(@RequestBody DevelopTaskCheckNameVO detailVO) {
        return new APITemplate<Void>() {
            @Override
            protected Void process() {
                developTaskService.checkName(detailVO.getName(), detailVO.getType(), detailVO.getPid(), detailVO.getIsFile(), detailVO.getTenantId());
                return null;
            }
        }.execute();
    }

    @PostMapping(value = "getByName")
    @ApiOperation("根据名称查询任务")
    public R<DevelopTaskResultVO> getByName(@RequestBody DevelopTaskGetByNameVO detailVO) {
        return new APITemplate<DevelopTaskResultVO>() {
            @Override
            protected DevelopTaskResultVO process() {
                return TaskMapstructTransfer.INSTANCE.DevelopTaskToResultVO(developTaskService.getByName(detailVO.getName(), detailVO.getTenantId()));
            }
        }.execute();
    }

    @PostMapping(value = "getComponentVersionByTaskType")
    @ApiOperation("获取组件版本号")
    public R<List<DevelopTaskGetComponentVersionResultVO>> getComponentVersionByTaskType(@RequestBody DevelopTaskGetComponentVersionVO getComponentVersionVO) {
        return new APITemplate<List<DevelopTaskGetComponentVersionResultVO>>() {
            @Override
            protected List<DevelopTaskGetComponentVersionResultVO> process() {
                return developTaskService.getComponentVersionByTaskType(getComponentVersionVO.getTenantId(), getComponentVersionVO.getTaskType());
            }
        }.execute();
    }

    @PostMapping(value = "allProductGlobalSearch")
    @ApiOperation("所有产品的已提交任务查询")
    public R<List<DevelopAllProductGlobalReturnVO>> allProductGlobalSearch(@RequestBody AllProductGlobalSearchVO allProductGlobalSearchVO) {
        return new APITemplate<List<DevelopAllProductGlobalReturnVO>>() {
            @Override
            protected List<DevelopAllProductGlobalReturnVO> process() {
                return developTaskService.allProductGlobalSearch(allProductGlobalSearchVO);
            }
        }.execute();
    }

    @PostMapping(value = "frozenTask")
    @ApiOperation("所有产品的已提交任务查询")
    public R<Boolean> frozenTask(@RequestBody DevelopFrozenTaskVO vo) {
        return new APITemplate<Boolean>() {
            @Override
            protected void checkParams() throws IllegalArgumentException {
                EScheduleStatus targetStatus = EScheduleStatus.getStatus(vo.getScheduleStatus());
                if (Objects.isNull(targetStatus)) {
                    throw new TaierDefineException(ErrorCode.INVALID_PARAMETERS);
                }
                if (CollectionUtils.isEmpty(vo.getTaskIds())) {
                    throw new TaierDefineException(ErrorCode.CAN_NOT_FIND_TASK);
                }
            }

            @Override
            protected Boolean process() {
                EScheduleStatus targetStatus = EScheduleStatus.getStatus(vo.getScheduleStatus());
                developTaskService.frozenTask(vo.getTaskIds(), vo.getScheduleStatus(), vo.getUserId());
                return true;
            }
        }.execute();
    }

    @PostMapping(value = "getSupportJobTypes")
    @ApiOperation("根据支持的引擎类型返回")
    public R<List<DevelopTaskTypeVO>> getSupportJobTypes(@RequestBody(required = false) DevelopTaskGetSupportJobTypesVO detailVO) {
        return new APITemplate<List<DevelopTaskTypeVO>>() {
            @Override
            protected List<DevelopTaskTypeVO>  process() {
                return developTaskService.getSupportJobTypes(detailVO.getTenantId());
            }
        }.execute();
    }

    @PostMapping(value = "getIncreColumn")
    @ApiOperation(value = "获取可以作为增量标识的字段")
    public R<List<JSONObject>> getIncreColumn(@RequestBody(required = false) DevelopDataSourceIncreColumnVO vo) {
        return new APITemplate<List<JSONObject>>() {
            @Override
            protected List<JSONObject> process() {
                return developTaskService.getIncreColumn(vo.getSourceId(), vo.getTableName(), vo.getSchema());
            }
        }.execute();
    }

    @PostMapping(value = "editTask")
    @ApiOperation(value = "编辑任务")
    public R<Void> editTask(@RequestBody DevelopTaskEditVO vo) {
        return new APITemplate<Void>() {
            @Override
            protected void checkParams() throws IllegalArgumentException {
                Preconditions.checkNotNull(vo.getTaskId(), "parameters of taskId not be null.");
                Preconditions.checkNotNull(vo.getName(), "parameters of name not be null.");
                Preconditions.checkNotNull(vo.getCatalogueId(), "parameters of catalogueId not be null.");
            }

            @Override
            protected Void process() {
                developTaskService.editTask(vo.getTaskId(), vo.getName(), vo.getCatalogueId(), vo.getDesc(),
                        vo.getTenantId(), vo.getComponentVersion());
                return null;
            }
        }.execute();
    }

    @PostMapping(value = "checkTaskNameRepeat")
    @ApiOperation("数据开发-校验任务名称是否重复")
    public R<Boolean> checkTaskNameRepeat(@RequestBody DevelopTaskNameCheckVO vo) {
        return new APITemplate<Boolean>() {
            @Override
            protected Boolean process() {
                return developTaskService.checkTaskNameRepeat(vo.getTaskName(), vo.getTenantId());
            }
        }.execute();
    }

    @PostMapping(value = "getFlowWorkSubTasks")
    @ApiOperation("数据开发-获取任务流下的所有子任务")
    public R<List<ScheduleTaskShade>> getFlowWorkSubTasks(@RequestBody DevelopScheduleTaskVO vo) {
        return new APITemplate<List<ScheduleTaskShade>>() {
            @Override
            protected List<ScheduleTaskShade> process() {
                return scheduleTaskShadeService.getFlowWorkSubTasks(vo.getTaskId());
            }
        }.execute();
    }


    @PostMapping(value = "getSyncProperties")
    @ApiOperation("数据开发-获取数据同步字段映射")
    public R<JSONObject> getSyncProperties() {
        return R.ok(developTaskService.getSyncProperties());
    }

    @PostMapping(value = "/parsing_ftp_columns")
    @ApiOperation("数据开发-解析ftp任务字段列表")
    public R<ParsingFTPFileVO> parsingFtpTaskFile(@RequestBody DevelopTaskParsingFTPFileParamVO payload) throws IOException {
        return R.ok(developTaskService.parsingFtpTaskFile(payload));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "currentPage", value = "当前页", required = true, dataType = "int"),
            @ApiImplicitParam(name = "pageSize", value = "页面大小", required = true, dataType = "int")
    })
    @PostMapping(value="getTaskRecordList")
    @ApiOperation("获取当前任务的历史修改纪录")
    public R<List<DevelopTaskRecordResultVO>> getTaskRecordList(@RequestBody DevelopTaskRecordGetByTaskIdVO developTaskRecordGetByTaskIdVO, @RequestParam("currentPage") int currentPage, @RequestParam("pageSize") int pageSize){
        List<DevelopTaskRecordResultVO> taskRecordList = developTaskRecordService.pageGetTaskRecordList(developTaskRecordGetByTaskIdVO.getTaskId(), currentPage, pageSize);
        return R.ok(taskRecordList);
//        return new APITemplate<List<DevelopTaskRecordResultVO>>() {
//            @Override
//            protected List<DevelopTaskRecordResultVO> process() {return developTaskRecordService.getTaskRecordList(developTaskRecordGetByTaskIdVO.getTaskId());
//            }
//        }.execute();
    }

    @PostMapping(value="getTaskRecordDetail")
    @ApiOperation("获取当前任务记录对比上版本")
    public R<DevelopTaskRecordResultVO> getTaskRecordDetail(@RequestParam("recordId") Long recordId, @RequestParam("recordVersion") int recordVersion){
        DevelopTaskRecordResultVO taskRecordDetail = developTaskRecordService.getTaskRecordDetail(recordId, recordVersion);
        return R.ok(taskRecordDetail);
    }

    @PostMapping(value = "auditPassTask")
    @ApiOperation("审核任务-通过")
    public R<Boolean> auditPassTask(@RequestBody DevelopTaskAuditTaskVO detailVO) {
        return new APITemplate<Boolean>() {
            @Override
            protected Boolean process() {
                return developTaskService.auditPassTask(detailVO.getTaskId(), detailVO.getUserId());
            }
        }.execute();
    }

    @PostMapping(value = "auditUnPassTask")
    @ApiOperation("审核任务-不通过")
    public R<Boolean> auditUnPassTask(@RequestBody DevelopTaskAuditTaskVO detailVO) {
        return new APITemplate<Boolean>() {
            @Override
            protected Boolean process() {
                return developTaskService.auditUnPassTask(detailVO.getTaskId(), detailVO.getUserId());
            }
        }.execute();
    }

    @PostMapping(value = "getTaskAuditStatus")
    @ApiOperation("获取任务的审核状态")
    public R<JSONObject> getTaskAuditStatus(@RequestBody DevelopScheduleTaskVO developScheduleTaskVO) {
        TaskVO taskVO = TaskMapstructTransfer.INSTANCE.DevelopScheduleTaskVToTaskVO(developScheduleTaskVO);
        return R.ok(developTaskService.getTaskAuditStatus(taskVO));
    }
}
