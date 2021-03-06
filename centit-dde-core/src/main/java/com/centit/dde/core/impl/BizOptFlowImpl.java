package com.centit.dde.core.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.centit.dde.bizopt.*;
import com.centit.dde.core.*;
import com.centit.dde.dao.DataPacketDao;
import com.centit.dde.dao.TaskDetailLogDao;
import com.centit.dde.po.DataPacket;
import com.centit.dde.po.TaskDetailLog;
import com.centit.dde.utils.BizModelJSONTransform;
import com.centit.dde.utils.Constant;
import com.centit.fileserver.common.FileStore;
import com.centit.framework.common.ResponseData;
import com.centit.product.metadata.dao.DatabaseInfoDao;
import com.centit.product.metadata.service.DatabaseRunTime;
import com.centit.product.metadata.service.MetaDataService;
import com.centit.product.metadata.service.MetaObjectService;
import com.centit.support.algorithm.BooleanBaseOpt;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.common.ObjectException;
import com.centit.support.compiler.VariableFormula;
import com.centit.support.json.JSONTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
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
    private DatabaseInfoDao databaseInfoDao;

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
        allOperations.put("sche", BuiltInOperation::runStart);
        allOperations.put("resBody", BuiltInOperation::runRequestBody);
        allOperations.put("resFile", BuiltInOperation::runRequestFile);
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
        allOperations.put("htts", BuiltInOperation::runHttpData);
        allOperations.put("clear", BuiltInOperation::runClear);
        JSBizOperation jsBizOperation = new JSBizOperation(metaObjectService,
            databaseRunTime);
        allOperations.put("js", jsBizOperation);
        PersistenceBizOperation databaseOperation = new PersistenceBizOperation(
            path, databaseInfoDao, metaDataService);
        allOperations.put("persistence", databaseOperation);
        DBBizOperation dbBizOperation = new DBBizOperation(databaseInfoDao);
        allOperations.put("database", dbBizOperation);
        ExcelBizOperation excelBizOperation = new ExcelBizOperation(fileStore);
        allOperations.put("excel", excelBizOperation);
        CsvBizOperation csvBizOperation = new CsvBizOperation(fileStore);
        allOperations.put("csv", csvBizOperation);
        JsonBizOperation jsonBizOperation = new JsonBizOperation(fileStore);
        allOperations.put("json", jsonBizOperation);
        RunSqlSBizOperation runsqlsbizoperation = new RunSqlSBizOperation(databaseRunTime);
        allOperations.put("sqlS", runsqlsbizoperation);
        ReportBizOperation reportBizOperation=new ReportBizOperation(fileStore);
        allOperations.put("SSD",reportBizOperation);
    }

    @Override
    public void registerOperation(String key, BizOperation opt) {
        allOperations.put(key, opt);
    }

    /**
     * 获取分支
     */
    private JSONObject getBatchStep(BizModel bizModel, DataOptDescJson dataOptDescJson, String stepId, Object preResult) {
        List<JSONObject> linksJson = dataOptDescJson.getNextLinks(stepId);
        for (JSONObject jsonObject : linksJson) {
            if (BooleanBaseOpt.castObjectToBoolean(
                VariableFormula.calculate(jsonObject.getString("expression"),
                    new BizModelJSONTransform(bizModel)), false)) {
                return dataOptDescJson.getOptStep(jsonObject.getString("targetId"));
            }
        }
        for (JSONObject jsonObject : linksJson) {
            if (Constant.ELSE.equalsIgnoreCase(jsonObject.getString("expression"))) {
                return dataOptDescJson.getOptStep(jsonObject.getString("targetId"));
            }
        }
        return null;
    }

    private Object returnResult(BizModel bizModel, JSONObject stepJson) throws IOException {
        String type = BuiltInOperation.getJsonFieldString(stepJson, "resultOptions", "1");
        String path;
        switch (type) {
            case "2":
                return "ok";
            case "3":
                path = BuiltInOperation.getJsonFieldString(stepJson, "source", "D");
                return bizModel.fetchDataSetByName(path);
            case "4":
                path = BuiltInOperation.getJsonFieldString(stepJson, "textarea", "D");
                return JSONTransformer.transformer(
                    JSON.parse(path), new BizModelJSONTransform(bizModel));
            case "5":
                return allOperations.get("SSD").runOpt(bizModel,stepJson);
            default:
                return bizModel;
        }
    }

    private Object runStep(DataOptDescJson dataOptDescJson, String logId, SimpleBizModel bizModel, JSONObject stepJson, Object preResult) throws IOException{
        String stepId = stepJson.getString("id");
        String stepType = stepJson.getString("type");
        if ("results".equals(stepType)) {
            return returnResult(bizModel, stepJson);
        }

        if ("sche".equals(stepType)) {
            DataPacket dataPacket = dataPacketDao.getObjectWithReferences(stepJson.getString("packetName"));
            Map<String, Object> queryParams = CollectionsOpt.cloneHashMap(bizModel.getModelTag());
            queryParams.putAll(BuiltInOperation.jsonArrayToMap(stepJson.getJSONArray("config"), "paramName", "paramDefaultValue"));
            preResult = run(dataPacket.getDataOptDescJson(), logId, queryParams);
        }

        if ("branch".equals(stepType)) {
            stepJson = getBatchStep(bizModel, dataOptDescJson, stepId, preResult);
        } else /*if (stepJson != null)*/ {
            preResult = runOneStepOpt(bizModel, stepJson, logId);
            stepJson = dataOptDescJson.getNextStep(stepId);
        }

        if(stepJson!=null){
            return runStep(dataOptDescJson, logId, bizModel, stepJson, preResult);
        } else {
            return preResult;
        }
    }

    @Override
    public Object run(JSONObject bizOptJson, String logId, Map<String, Object> queryParams) throws IOException {
        DataOptDescJson dataOptDescJson = new DataOptDescJson(bizOptJson);
        JSONObject stepJson = dataOptDescJson.getStartStep();
        if (stepJson == null) {
            writeLog(logId, "error", "没有start节点");
            return null;
        }
        SimpleBizModel bizModel = new SimpleBizModel(logId);
        bizModel.setModelTag(queryParams);
        Object responseData = ResponseData.successResponse;
        return runStep(dataOptDescJson, logId, bizModel, stepJson, responseData);
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
        detailLog.setTaskId(StringBaseOpt.fillZeroForString(StringBaseOpt.castObjectToString(++step, "0"),6));
        taskDetailLogDao.saveNewObject(detailLog);
        return detailLog;
    }

    /**
     * 单步运行
     */
    private ResponseData runOneStepOpt(BizModel bizModel, JSONObject bizOptJson, String logId) {
        String sOptType = bizOptJson.getString("type");
        BizOperation opt = allOperations.get(sOptType);
        if (opt == null) {
            if (logId != null) {
                writeLog(logId, "error", "找不到对应的操作：" + sOptType);
            }
        }
        try {
            TaskDetailLog detailLog = new TaskDetailLog();
            if (logId != null) {
                String processName = bizOptJson.getString("processName");
                if (StringBaseOpt.isNvl(processName)) {
                    processName = bizOptJson.getString("nodeName");
                }
                detailLog = writeLog(logId, sOptType + ":" + processName, "");
            }
            ResponseData responseData = opt.runOpt(bizModel, bizOptJson);
            JSONObject jsonObject=JSONObject.parseObject(responseData.getData().toString());
            if (logId != null) {
                detailLog.setSuccessPieces(jsonObject.getIntValue("success"));
                detailLog.setErrorPieces(jsonObject.getIntValue("error"));
                detailLog.setLogInfo(BuiltInOperation.getJsonFieldString(jsonObject, "info", "ok"));
                detailLog.setRunEndTime(new Date());
                taskDetailLogDao.updateObject(detailLog);
            }
            return responseData;
        } catch (Exception e) {
            if (logId != null) {
                writeLog(logId, "error", ObjectException.extortExceptionMessage(e, 4));
            }
            return ResponseData.makeErrorMessage(e.getMessage());
        }
    }

    private void debugOneStepOpt(BizModel bizModel, JSONObject bizOptJson) {
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
                    debugOneStepOpt(bizModel, (JSONObject) step);
                }
            }
        }
        return bizModel;
    }
}
