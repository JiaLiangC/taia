package com.dtstack.taier.develop.vo.develop.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.sql.Timestamp;

@ApiModel("任务信息")
public class DevelopTaskRecordResultVO {

    @ApiModelProperty(value = "id", hidden = true)
    private Long id = 0L;

    @ApiModelProperty(value = "任务ID", example = "1", required = true)
    private Long taskId;

    @ApiModelProperty(value = "任务版本", example = "0", required = true)
    private Integer version;

    @ApiModelProperty(value = "创建时间", required = true)
    private Timestamp gmtCreate;

    @ApiModelProperty(value = "最后修改task的用户", example = "1")
    private String modifyUserName;

    @ApiModelProperty(value = "sql 文本", example = "show tables;", required = true)
    private String sqlText;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Timestamp getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Timestamp gmtCreate) {
        this.gmtCreate = gmtCreate;
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
}
