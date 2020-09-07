package com.centit.dde.datamoving.service;

import com.alibaba.fastjson.JSONObject;
import com.centit.dde.dao.DataPacketDao;
import com.centit.dde.dao.TaskDetailLogDao;
import com.centit.dde.dao.TaskLogDao;
import com.centit.dde.datamoving.dataopt.DatabaseBizOperation;
import com.centit.dde.datamoving.utils.BizOptFlowUtil;
import com.centit.dde.po.DataPacket;
import com.centit.dde.po.TaskDetailLog;
import com.centit.dde.po.TaskLog;
import com.centit.dde.services.DBPacketBizSupplier;
import com.centit.fileserver.common.FileStore;
import com.centit.framework.ip.service.IntegrationEnvironment;
import com.centit.product.dataopt.bizopt.JsMateObjectEventRuntime;
import com.centit.product.metadata.service.DatabaseRunTime;
import com.centit.product.metadata.service.MetaDataService;
import com.centit.product.metadata.service.MetaObjectService;
import com.centit.support.algorithm.UuidOpt;
import com.centit.support.common.ObjectException;
import com.centit.support.json.JSONOpt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author zhf
 */
@Service
public class TaskRun {
    private final TaskLogDao taskLogDao;
    private final TaskDetailLogDao taskDetailLogDao;
    private final DataPacketDao dataPacketDao;
    private final MetaDataService metaDataService;
    private final IntegrationEnvironment integrationEnvironment;
    private final DatabaseBizOperation databaseBizOperation;
    private FileStore fileStore;

    @Autowired(required = false)
    public void setFileStore(FileStore fileStore) {
        this.fileStore = fileStore;
    }


    private MetaObjectService metaObjectService;

    @Autowired(required = false)
    public void setMetaObjectService(MetaObjectService metaObjectService) {
        this.metaObjectService = metaObjectService;
    }

    private DatabaseRunTime databaseRunTime;

    @Autowired(required = false)
    public void setDatabaseRunTime(DatabaseRunTime databaseRunTime) {
        this.databaseRunTime = databaseRunTime;
    }


    private TaskLog taskLog;
    private Date beginTime;
    private TaskDetailLog detailLog;

    @Autowired
    public TaskRun(TaskLogDao taskLogDao, TaskDetailLogDao taskDetailLogDao, DataPacketDao dataPacketDao, MetaDataService metaDataService, IntegrationEnvironment integrationEnvironment, DatabaseBizOperation databaseBizOperation) {
        this.taskLogDao = taskLogDao;
        this.taskDetailLogDao = taskDetailLogDao;
        this.dataPacketDao = dataPacketDao;
        this.metaDataService = metaDataService;
        this.integrationEnvironment = integrationEnvironment;
        this.databaseBizOperation = databaseBizOperation;
        this.taskLog = new TaskLog();
        this.detailLog = new TaskDetailLog();
    }


    private int runStep(DataPacket dataPacket) {
        JSONObject bizOptJson = dataPacket.getDataOptDescJson();
        if (bizOptJson.isEmpty()) {
            return 0;
        }
        int iResult = 0;
        try {
            DBPacketBizSupplier dbPacketBizSupplier = new DBPacketBizSupplier(dataPacket);
            dbPacketBizSupplier.setIntegrationEnvironment(integrationEnvironment);
            dbPacketBizSupplier.setFileStore(fileStore);
            dbPacketBizSupplier.setBatchWise(dataPacket.getIsWhile());
            /*添加参数默认值传输*/
            dbPacketBizSupplier.setQueryParams(dataPacket.getPacketParamsValue());

            databaseBizOperation.setIntegrationEnvironment(integrationEnvironment);
            databaseBizOperation.setMetaDataService(metaDataService);
            databaseBizOperation.setBizOptJson(bizOptJson);
            JsMateObjectEventRuntime jsMateObjectEventRuntime =
                new JsMateObjectEventRuntime(metaObjectService, databaseRunTime);
            jsMateObjectEventRuntime.setParms(dataPacket.getPacketParamsValue());
            databaseBizOperation.setJsMateObjectEvent(jsMateObjectEventRuntime);
            iResult = BizOptFlowUtil.runDataExchange(dbPacketBizSupplier, databaseBizOperation);
            saveDetail( iResult, "ok");
        } catch (ObjectException e) {
            saveDetail( 0, e.getMessage());
        } catch (Exception e) {
            saveDetail(0, e.getMessage());
        }
        return iResult;
    }


    private void saveDetail(int iResult, String info) {
        detailLog.setRunBeginTime(beginTime);
        detailLog.setTaskId(taskLog.getTaskId());
        detailLog.setLogId(taskLog.getLogId());
        detailLog.setLogType(info);
        detailLog.setLogInfo(info);
        String successSign = "ok";
        if (successSign.equals(detailLog.getLogInfo())) {
            detailLog.setSuccessPieces((long) iResult);
            detailLog.setErrorPieces(0L);
        } else {
            detailLog.setSuccessPieces(0L);
            detailLog.setErrorPieces((long) iResult);
        }
        detailLog.setRunEndTime(new Date());
        detailLog.setLogDetailId(UuidOpt.getUuidAsString32());
        taskDetailLogDao.saveNewObject(detailLog);
    }

    public void runTask(String logId) {
        beginTime = new Date();
        taskLog = taskLogDao.getObjectById(logId);
        DataPacket dataPacket = dataPacketDao.getObjectWithReferences(taskLog.getTaskId());
        int i = runStep(dataPacket);
        taskLog.setRunEndTime(new Date());
        if (i > 0) {
            taskLog.setSuccessPieces("成功" + i + "批");
            taskLog.setErrorPieces("");
        } else {
            taskLog.setErrorPieces("失败");
            taskLog.setSuccessPieces("");
        }
        taskLogDao.updateObject(taskLog);
    }
}
