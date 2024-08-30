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

import com.dtstack.taier.common.exception.TaierDefineException;
import com.dtstack.taier.common.lang.coc.APITemplate;
import com.dtstack.taier.common.lang.web.R;
import com.dtstack.taier.datasource.plugin.common.exception.TaierSQLException;
import com.dtstack.taier.datasource.plugin.common.operation.FetchOrientation;
import com.dtstack.taier.datasource.plugin.common.session.SessionHandle;
import com.dtstack.taier.develop.dto.devlop.ExecuteResultVO;
import com.dtstack.taier.develop.dto.devlop.OpActionRequest;
import com.dtstack.taier.datasource.plugin.common.operation.OperationHandle;
import com.dtstack.taier.develop.dto.devlop.SessionData;
import com.dtstack.taier.develop.mapstruct.vo.DevelopJobMapstructTransfer;
import com.dtstack.taier.develop.service.develop.impl.DevelopJobService;
import com.dtstack.taier.develop.utils.ApiUtils;
import com.dtstack.taier.develop.vo.develop.query.DevelopJobStartSqlVO;
import com.dtstack.taier.develop.vo.develop.query.DevelopJobStartSyncVO;
import com.dtstack.taier.develop.vo.develop.query.DevelopJobSyncTaskVO;
import com.dtstack.taier.develop.vo.develop.result.DevelopExecuteResultVO;
import com.dtstack.taier.develop.vo.develop.result.DevelopGetSyncTaskStatusInnerResultVO;
import com.dtstack.taier.develop.vo.develop.result.DevelopStartSyncResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Api(value = "任务实例管理", tags = {"任务实例管理"})
@RestController
@RequestMapping(value = "/batchJob")
public class DevelopJobController {

    @Autowired
    private DevelopJobService batchJobService;


    @ApiOperation(value = "运行同步任务")
    @PostMapping(value = "startSyncImmediately")
    public R<DevelopStartSyncResultVO> startSyncImmediately(@RequestBody DevelopJobStartSyncVO vo) {

        return new APITemplate<DevelopStartSyncResultVO>() {

            @Override
            protected DevelopStartSyncResultVO process() throws TaierDefineException {
                return batchJobService.startSyncImmediately(vo.getTaskId(), vo.getUserId(), vo.getIsRoot(), vo.getTenantId());
            }
        }.execute();
    }

    @ApiOperation(value = "获取同步任务运行状态")
    @PostMapping(value = "getSyncTaskStatus")
    public R<DevelopGetSyncTaskStatusInnerResultVO> getSyncTaskStatus(@RequestBody DevelopJobSyncTaskVO vo) {

        return new APITemplate<DevelopGetSyncTaskStatusInnerResultVO>() {
            @Override
            protected DevelopGetSyncTaskStatusInnerResultVO process() throws TaierDefineException {
                return batchJobService.getSyncTaskStatus(vo.getTenantId(), vo.getJobId());
            }
        }.execute();
    }

    @ApiOperation(value = "停止同步任务")
    @PostMapping(value = "stopSyncJob")
    public R<Void> stopSyncJob(@RequestBody DevelopJobSyncTaskVO vo) {

        return new APITemplate<Void>() {
            @Override
            protected Void process() throws TaierDefineException {
                batchJobService.stopSyncJob(vo.getJobId());
                return null;
            }
        }.execute();
    }

    @ApiOperation(value = "运行sql")
    @PostMapping(value = "startSqlImmediately")
    public R<DevelopExecuteResultVO> startSqlImmediately(@RequestBody DevelopJobStartSqlVO vo) {

        return new APITemplate<DevelopExecuteResultVO>() {
            @Override
            protected DevelopExecuteResultVO process() throws TaierDefineException {
                ExecuteResultVO executeResultVO = batchJobService.startSqlImmediately(vo.getUserId(), vo.getTenantId(), vo.getTaskId(), vo.getSql(), vo.getTaskVariables());
                return DevelopJobMapstructTransfer.INSTANCE.executeResultVOToDevelopExecuteResultVO(executeResultVO);
            }
        }.execute();
    }


    @ApiOperation(value = "停止通过sql任务执行的sql查询语句")
    @PostMapping(value = "stopSqlImmediately")
    public R<Void> stopSqlImmediately(@RequestBody DevelopJobSyncTaskVO vo) {

        return new APITemplate<Void>() {
            @Override
            protected Void process() throws TaierDefineException {
                batchJobService.stopSqlImmediately(vo.getJobId(), vo.getTenantId());
                return null;
            }
        }.execute();
    }



    /***********************************************************************************************************************************************************************************/
    /*********************************************************************************************************************************************************************************/


    @ApiOperation(value = "Open a session")
    @PostMapping(value = "/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<SessionHandle> openSession(@RequestBody DevelopJobStartSqlVO vo) {
        return new APITemplate<SessionHandle>() {
            @Override
            protected SessionHandle process() throws TaierDefineException {
                 String sessionHandler = batchJobService.openSession(vo.getUserId(), vo.getTenantId(), vo.getTaskId());
                return  SessionHandle.fromUUID(sessionHandler);
            }
        }.execute();
    }


    @ApiOperation(value = "Close a session")
    @DeleteMapping("/sessions/{sessionHandle}")
    public ResponseEntity<Void> closeSession(@PathVariable("sessionHandle") String sessionHandle) {
        try {
            batchJobService.closeSession(sessionHandle);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }


    @ApiOperation(value = "Get the list of all live sessions")
    @GetMapping("/sessions")
    public List<SessionData> sessions() {
        return batchJobService.allSessions();
    }

    @ApiOperation(value = "Fetch result")
    @GetMapping("/operations/{operationHandle}/rowset")
    public R<DevelopExecuteResultVO> getNextRowSet(@PathVariable("operationHandle") String operationHandle) {


        return new APITemplate<DevelopExecuteResultVO>() {
            @Override
            protected DevelopExecuteResultVO process() throws TaierDefineException {
                try {
                    ExecuteResultVO executeResultVO = batchJobService.fetchResults(operationHandle, FetchOrientation.FETCH_NEXT,50000,false);
                    return DevelopJobMapstructTransfer.INSTANCE.executeResultVOToDevelopExecuteResultVO(executeResultVO);
                } catch (TaierSQLException e) {
                    throw new TaierDefineException(e);
                }
            }

        }.execute();
    }


    @ApiOperation(value = "Create an operation with EXECUTE_STATEMENT type")
    @PostMapping(value = "/operations/{sessionHandle}/statement", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<OperationHandle> executeStatement(@PathVariable("sessionHandle") String sessionHandle, @RequestBody DevelopJobStartSqlVO vo) {
        return new APITemplate<OperationHandle>() {
            @Override
            protected OperationHandle process() throws TaierDefineException {
                try {
                    String opHandler = batchJobService.executeStatement(sessionHandle, vo.getSql());
                    return OperationHandle.fromString(opHandler);
                } catch (TaierSQLException e) {
                    throw new TaierDefineException(e);
                }
            }
        }.execute();
    }


    @ApiOperation(value = "Close or Cancel a operation")
    @PutMapping("/operations/{operationHandle}/{action}")
    public ResponseEntity<Void> applyOpAction(
            @PathVariable("action") String action,
            @PathVariable("operationHandle") String operationHandle) throws TaierSQLException {
        try {
            switch (action.toLowerCase()) {
                case "cancel":
                    batchJobService.cancelOperation(operationHandle);
                    break;
                case "close":
                    batchJobService.closeOperation(operationHandle);
                    break;
                default:
                    throw new TaierSQLException("Invalid action " + action);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            String errorMsg = "Error applying " + action + " for operation handle " + operationHandle;
            throw new TaierSQLException(errorMsg, e);
        }
    }

}