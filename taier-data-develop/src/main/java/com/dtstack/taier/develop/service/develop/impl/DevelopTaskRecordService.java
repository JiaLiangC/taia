package com.dtstack.taier.develop.service.develop.impl;

import com.dtstack.taier.dao.domain.ScheduleTaskShade;
import com.dtstack.taier.dao.domain.TaskRecord;
import com.dtstack.taier.dao.mapper.DevelopTaskMapper;
import com.dtstack.taier.dao.mapper.DevelopTaskRecordMapper;
import com.dtstack.taier.develop.dto.devlop.TaskGetNotDeleteVO;
import com.dtstack.taier.develop.dto.devlop.TaskRecordVO;
import com.dtstack.taier.develop.vo.develop.result.DevelopTaskGetComponentVersionResultVO;
import com.dtstack.taier.develop.vo.develop.result.DevelopTaskRecordResultVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DevelopTaskRecordService {
    public static Logger LOGGER = LoggerFactory.getLogger(DevelopTaskService.class);

    private static final ObjectMapper objMapper = new ObjectMapper();

    @Autowired
    private DevelopTaskRecordMapper developTaskRecordMapper;

    /**
     * 分页获取当前任务的修改记录
     *
     * @param taskId
     * @return
     */
    public List<DevelopTaskRecordResultVO> getTaskRecordList(Long taskId) {
        List<TaskRecord> taskRecords = developTaskRecordMapper.recordListByTaskid(taskId);
        List<DevelopTaskRecordResultVO> developTaskRecordResultVOs = Lists.newArrayList();
        for (TaskRecord taskRecord:taskRecords){
            DevelopTaskRecordResultVO resultVO = new DevelopTaskRecordResultVO();
            resultVO.setId(taskRecord.getId());
            resultVO.setTaskId(taskRecord.getTaskId());
            resultVO.setVersion(taskRecord.getVersion());
            resultVO.setGmtCreate(taskRecord.getGmtCreate());
            resultVO.setModifyUserName(taskRecord.getModifyUserName());
            resultVO.setSqlText(taskRecord.getSqlText());
            developTaskRecordResultVOs.add(resultVO);
        }
        return developTaskRecordResultVOs;
    }

    public List<DevelopTaskRecordResultVO> pageGetTaskRecordList(Long taskId,int currentPage, int pageSize) {
        int offset = (currentPage-1)*pageSize;
        List<TaskRecord> taskRecords = developTaskRecordMapper.pageRecordListByTaskid(taskId,offset,pageSize);
        List<DevelopTaskRecordResultVO> developTaskRecordResultVOs = Lists.newArrayList();
        for (TaskRecord taskRecord:taskRecords){
            DevelopTaskRecordResultVO resultVO = new DevelopTaskRecordResultVO();
            resultVO.setId(taskRecord.getId());
            resultVO.setTaskId(taskRecord.getTaskId());
            resultVO.setVersion(taskRecord.getVersion());
            resultVO.setGmtCreate(taskRecord.getGmtCreate());
            resultVO.setModifyUserName(taskRecord.getModifyUserName());
            resultVO.setSqlText(taskRecord.getSqlText());
            developTaskRecordResultVOs.add(resultVO);
        }
        return developTaskRecordResultVOs;
    }

    public DevelopTaskRecordResultVO getTaskRecordDetail(Long taskId, Integer version) {
        TaskRecord taskRecord = developTaskRecordMapper.recordDetailByTaskVersion(taskId, version);
        DevelopTaskRecordResultVO resultVO = new DevelopTaskRecordResultVO();
        resultVO.setSqlText(taskRecord.getSqlText());
        return resultVO;
    }
}
