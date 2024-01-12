package com.dtstack.taier.develop.dto.devlop;

import com.baomidou.mybatisplus.annotation.TableField;
import com.dtstack.taier.dao.domain.TaskRecord;

public class TaskRecordVO extends TaskRecord {
    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 'task版本'
     */
    private Integer version;

    private Integer reviewStatus;

    /**
     * 最后修改task的用户
     */
    @TableField("modify_user_id")
    private Long modifyUserId;

    private String modifyUserName;

    /**
     * 'sql 文本'
     */
    private String sqlText;

    /**
     * '任务参数'
     */
    private String taskParams;

    /**
     * 调度配置 json格式
     */
    private String scheduleConf;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(Integer reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public Long getModifyUserId() {
        return modifyUserId;
    }

    public void setModifyUserId(Long modifyUserId) {
        this.modifyUserId = modifyUserId;
    }

    public String getModifyUserName() {
        return modifyUserName;
    }

    public void setModifyUserName(String modifyUserName) {
        this.modifyUserName = modifyUserName;
    }

    public String getSqlText() {
        return sqlText;
    }

    public void setSqlText(String sqlText) {
        this.sqlText = sqlText;
    }

    public String getTaskParams() {
        return taskParams;
    }

    public void setTaskParams(String taskParams) {
        this.taskParams = taskParams;
    }

    public String getScheduleConf() {
        return scheduleConf;
    }

    public void setScheduleConf(String scheduleConf) {
        this.scheduleConf = scheduleConf;
    }
}
