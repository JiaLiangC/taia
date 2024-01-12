package com.dtstack.taier.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dtstack.taier.dao.domain.TaskRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DevelopTaskRecordMapper extends BaseMapper<TaskRecord> {

    List<TaskRecord> recordListByTaskid(@Param("taskId") Long taskId);

    List<TaskRecord> pageRecordListByTaskid(@Param("taskId") Long taskId, @Param("currentPage") int currentPage, @Param("pageSize") int pageSize);

    TaskRecord recordDetailByTaskVersion(@Param("taskId") Long taskId, @Param("version") int version);
}
