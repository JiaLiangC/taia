package com.dtstack.engine.master.server.builder.dependency;

import com.dtstack.engine.domain.ScheduleJobJob;
import com.dtstack.engine.domain.ScheduleTaskShade;
import com.dtstack.engine.master.server.builder.cron.ScheduleCorn;

import java.util.Date;
import java.util.List;

/**
 * @Auther: dazhi
 * @Date: 2022/1/4 3:55 PM
 * @Email: dazhi@dtstack.com
 * @Description: 依赖处理器
 */
public interface DependencyHandler extends Chain<DependencyHandler> {

    /**
     * 生成jobJob
     *
     * @param corn 生成周期
     * @param currentDate 当期执行时间
     * @param currentJobKey 当期实例key
     * @return currentJobKey的父实例
     */
    List<ScheduleJobJob> generationJobJobForTask(ScheduleCorn corn, Date currentDate,String currentJobKey);
}
