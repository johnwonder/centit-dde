package com.centit.dde.controller;

import com.centit.dde.po.TaskDetailLog;
import com.centit.dde.service.TaskDetailLogManager;
import com.centit.framework.core.controller.WrapUpResponseBody;
import com.centit.framework.core.dao.PageQueryResult;
import com.centit.support.database.utils.PageDesc;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;


/**
 * @ClassName TaskLogController
 * @Date 2019/3/20 17:06
 * @Version 1.0
 */
@RestController
@RequestMapping(value = "taskDetailLog")
@Api(value = "任务明细日志", tags = "任务明细日志")
public class TaskDetailLogController {
    private static final Log log = LogFactory.getLog(TaskLogController.class);

    @Autowired
    private TaskDetailLogManager taskDetailLogManager;

    @PostMapping
    @ApiOperation(value = "新增明细日志")
    @WrapUpResponseBody
    public void createTaskDetailLog(TaskDetailLog detailLog){
        taskDetailLogManager.createTaskDetailLog(detailLog);
    }

    @PutMapping(value = "/{logDetailId}")
    @ApiOperation(value = "编辑明细日志")
    @ApiImplicitParam(name = "logDetailId", value = "明细日志编号")
    @WrapUpResponseBody
    public void updateTaskDetailLog(@PathVariable String logDetailId, TaskDetailLog detailLog){
        detailLog.setLogId(logDetailId);
        taskDetailLogManager.updateTaskDetailLog(detailLog);
    }

    @DeleteMapping(value = "/{logDetailId}")
    @ApiOperation(value = "删除明细日志")
    @ApiImplicitParam(name = "logDetailId", value = "明细日志编号")
    @WrapUpResponseBody
    public void delTaskDetailLog(@PathVariable String logDetailId){
        taskDetailLogManager.delTaskDetailLog(logDetailId);
    }

    @GetMapping
    @ApiOperation(value = "查询所有明细日志")
    @WrapUpResponseBody
    public PageQueryResult<TaskDetailLog> listTaskDetailLog(PageDesc pageDesc){
        List<TaskDetailLog> taskDetailLogs = taskDetailLogManager.listTaskDetailLog(new HashMap<String, Object>(), pageDesc);
        return PageQueryResult.createResult(taskDetailLogs,pageDesc);
    }

    @GetMapping(value = "/{logDetailId}")
    @ApiOperation(value = "查询单个明细日志")
    @ApiImplicitParam(name = "logDetailId", value = "明细日志编号")
    @WrapUpResponseBody
    public TaskDetailLog getTaskDetailLog(@PathVariable String logDetailId){
        TaskDetailLog taskDetailLog = taskDetailLogManager.getTaskDetailLog(logDetailId);
        return taskDetailLog;
    }
}
