package com.centit.dde.core.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.centit.dde.bizopt.*;
import com.centit.dde.core.*;
import com.centit.dde.dao.DataPacketDao;
import com.centit.dde.dao.TaskDetailLogDao;
import com.centit.dde.po.DataPacket;
import com.centit.dde.po.TaskDetailLog;
import com.centit.dde.utils.BizModelJSONTransform;
import com.centit.fileserver.common.FileStore;
import com.centit.framework.ip.service.IntegrationEnvironment;
import com.centit.product.metadata.service.DatabaseRunTime;
import com.centit.product.metadata.service.MetaDataService;
import com.centit.product.metadata.service.MetaObjectService;
import com.centit.support.algorithm.BooleanBaseOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.common.ObjectException;
import com.centit.support.compiler.VariableFormula;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 业务流
 *
 * @author zhf
 */
@Service
public class BizOptFlowImpl implements BizOptFlow {
    @Value("${os.file.base.dir:./file_home/export}")
    private String path;

    @Autowired
    private IntegrationEnvironment integrationEnvironment;

    @Autowired
    private MetaDataService metaDataService;
    @Autowired
    private TaskDetailLogDao taskDetailLogDao;
    @Autowired
    private DataPacketDao dataPacketDao;
    @Autowired(required = false)
    private MetaObjectService metaObjectService;

    @Autowired(required = false)
    private DatabaseRunTime databaseRunTime;
    @Autowired(required = false)
    private FileStore fileStore;

    @Override
    public void initStep(int step) {
        this.step = step;
    }

    private int step;
    private Map<String, BizOperation> allOperations;

    public BizOptFlowImpl() {
        allOperations = new HashMap<>(50);
    }


    @PostConstruct
    public void init() {
        allOperations.put("start", BuiltInOperation::runStart);
        allOperations.put("scheduling", BuiltInOperation::runStart);
        allOperations.put("recuestbody",BuiltInOperation::runRequestBody);
        allOperations.put("map", BuiltInOperation::runMap);
        allOperations.put("filter", BuiltInOperation::runFilter);
        allOperations.put("append", BuiltInOperation::runAppend);
        allOperations.put("stat", BuiltInOperation::runStat);
        allOperations.put("analyse", BuiltInOperation::runAnalyse);
        allOperations.put("cross", BuiltInOperation::runCross);
        allOperations.put("compare", BuiltInOperation::runCompare);
        allOperations.put("sort", BuiltInOperation::runSort);
        allOperations.put("join", BuiltInOperation::runJoin);
        allOperations.put("union", BuiltInOperation::runUnion);
        allOperations.put("filterExt", BuiltInOperation::runFilterExt);
        allOperations.put("check", BuiltInOperation::runCheckData);
        allOperations.put("static", BuiltInOperation::runStaticData);
        allOperations.put("http", BuiltInOperation::runHttpData);
        allOperations.put("clear", BuiltInOperation::runClear);
        JSBizOperation jsBizOperation = new JSBizOperation(metaObjectService,
            databaseRunTime);
        allOperations.put("js", jsBizOperation);
        PersistenceBizOperation databaseOperation = new PersistenceBizOperation(
            path, integrationEnvironment, metaDataService);
        allOperations.put("persistence", databaseOperation);
        DBBizOperation dbBizOperation = new DBBizOperation(integrationEnvironment);
        allOperations.put("obtain-database", dbBizOperation);
        ExcelBizOperation excelBizOperation = new ExcelBizOperation(fileStore);
        allOperations.put("obtain-excel", excelBizOperation);
        CsvBizOperation csvBizOperation = new CsvBizOperation(fileStore);
        allOperations.put("obtain-csv", csvBizOperation);
        JsonBizOperation jsonBizOperation = new JsonBizOperation(fileStore);
        allOperations.put("obtain-json", jsonBizOperation);
        HttpBizOperation httpBizOperation = new HttpBizOperation();
        allOperations.put("obtain-http", httpBizOperation);
    }

    @Override
    public void registerOperation(String key, BizOperation opt) {
        allOperations.put(key, opt);
    }

    /**
     * 获取分支
     */
    private JSONObject getBatchStep(BizModel bizModel, DataOptDescJson dataOptDescJson, String stepId) {
        List<JSONObject> linksJson = dataOptDescJson.getNextLinks(stepId);
        for (JSONObject jsonObject : linksJson) {
            if (BooleanBaseOpt.castObjectToBoolean(
                VariableFormula.calculate(jsonObject.getString("rule"),
                    new BizModelJSONTransform(bizModel)), false)) {
                return jsonObject;
            }
        }
        return null;
    }

    @Override
    public BizModel run(JSONObject bizOptJson, String logId, Map<String, Object> queryParams) {
        DataOptDescJson dataOptDescJson = new DataOptDescJson(bizOptJson);
        JSONObject stepJson = dataOptDescJson.getStartStep();
        if (stepJson == null) {
            writeLog(logId, "error", "没有start节点");
            return null;
        }
        SimpleBizModel bizModel = BizModel.EMPTY_BIZ_MODEL;
        bizModel.setModelTag(queryParams);
        while (stepJson != null) {
            String stepId = stepJson.getString("id");
            String stepType = stepJson.getString("type");
            if ("return".equals(stepType)) {
                return bizModel;
            }
            if ("branch".equals(stepType)) {
                stepJson = getBatchStep(bizModel, dataOptDescJson, stepId);
            }
            if (stepJson != null) {
                runOneStep(bizModel, stepJson, logId);
            }
            if("scheduling".equals(stepType)){
                DataPacket dataPacket = dataPacketDao.getObjectWithReferences(stepJson.getString("packetName"));
                run(dataPacket.getDataOptDescJson(),logId,queryParams);
            }
            stepJson = dataOptDescJson.getNextStep(stepId);
        }
        if (bizModel.isEmpty()) {
            writeLog(logId, "emptyBizModel", "ok");
        }
        return bizModel;
    }

    private TaskDetailLog writeLog(String logId, String logType, String logInfo) {
        TaskDetailLog detailLog = new TaskDetailLog();
        detailLog.setRunBeginTime(new Date());
        detailLog.setLogId(logId);
        detailLog.setSuccessPieces(0);
        detailLog.setErrorPieces(0);
        detailLog.setLogType(logType);
        detailLog.setLogInfo(logInfo);
        detailLog.setRunEndTime(new Date());
        detailLog.setTaskId(StringBaseOpt.castObjectToString(++step, "0"));
        taskDetailLogDao.saveNewObject(detailLog);
        return detailLog;
    }

    /**
     * 单步运行
     */
    private void runOneStep(BizModel bizModel, JSONObject bizOptJson, String logId) {
        String sOptType = bizOptJson.getString("type");
        BizOperation opt = allOperations.get(sOptType);
        if (opt == null) {
            if (logId != null) {
                writeLog(logId, "error", "找不到对应的操作：" + sOptType);
            }
            throw new ObjectException(bizOptJson, "找不到对应的操作：" + sOptType);
        }
        try {
            TaskDetailLog detailLog = new TaskDetailLog();
            if (logId != null) {
                String processName = bizOptJson.getString("processName");
                if(StringBaseOpt.isNvl(processName)){
                    processName = bizOptJson.getString("nodeName");
                }
                detailLog = writeLog(logId, sOptType + ":" + processName, "");
            }
            JSONObject jsonObject = opt.runOpt(bizModel, bizOptJson);
            if (logId != null) {
                detailLog.setSuccessPieces(jsonObject.getIntValue("success"));
                detailLog.setErrorPieces(jsonObject.getIntValue("error"));
                detailLog.setLogInfo(BuiltInOperation.getJsonFieldString(jsonObject, "info", "ok"));
                detailLog.setRunEndTime(new Date());
                taskDetailLogDao.updateObject(detailLog);
            }
        } catch (Exception e) {
            if (logId != null) {
                writeLog(logId, "error", ObjectException.extortExceptionMessage(e, 4));
            }
            throw new ObjectException(bizOptJson, ObjectException.extortExceptionMessage(e, 4));
        }
    }

    protected void debugOneStep(BizModel bizModel, JSONObject bizOptJson) {
        String sOptType = bizOptJson.getString("operation");
        BizOperation opt = allOperations.get(sOptType);
        if (opt != null) {
            //TODO 记录运行前日志
            opt.debugOpt(bizModel, bizOptJson);
            //TODO 记录运行后日志
        } else {
            //TODO 记录运行错误日志
        }
    }

    @Override
    public BizModel debug(JSONObject bizOptJson) {
        BizModel bizModel = null;
        JSONArray optSteps = bizOptJson.getJSONArray("steps");
        if (optSteps != null) {
            for (Object step : optSteps) {
                if (step instanceof JSONObject) {
                    /*result =*/
                    debugOneStep(bizModel, (JSONObject) step);
                }
            }
        }
        return bizModel;
    }
}
