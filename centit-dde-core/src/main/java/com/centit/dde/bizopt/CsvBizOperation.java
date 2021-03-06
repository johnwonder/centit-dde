package com.centit.dde.bizopt;

import com.alibaba.fastjson.JSONObject;
import com.centit.dde.core.BizModel;
import com.centit.dde.core.BizOperation;
import com.centit.dde.core.SimpleDataSet;
import com.centit.dde.dataset.CsvDataSet;
import com.centit.fileserver.common.FileStore;
import com.centit.framework.common.ResponseData;

import java.io.IOException;


/**
 * @author zhf
 */
public class CsvBizOperation implements BizOperation {
    private FileStore fileStore;

    public CsvBizOperation(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    @Override
    public ResponseData runOpt(BizModel bizModel, JSONObject bizOptJson) {
        String sourDsName = BuiltInOperation.getJsonFieldString(bizOptJson, "nodeName", bizModel.getModelName());
        String filePath = bizOptJson.getJSONArray("upjson").getJSONObject(0).getString("fileId");
        CsvDataSet csvDataSet = new CsvDataSet();
        try {
            csvDataSet.setFilePath(
                fileStore.getFile(filePath).getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        SimpleDataSet dataSet = csvDataSet.load(null);
        bizModel.putDataSet(sourDsName, dataSet);
        return BuiltInOperation.getResponseSuccessData(dataSet.size());
    }
}
