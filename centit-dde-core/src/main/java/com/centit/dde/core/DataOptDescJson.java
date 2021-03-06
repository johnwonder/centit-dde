package com.centit.dde.core;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhf
 */

public class DataOptDescJson {
    private Map<String, JSONObject> nodeMap=new HashMap<>(50);
    private Map<String, List<JSONObject>> linkMap=new HashMap<>(100);

    public DataOptDescJson(JSONObject dataOptJson) {
        mapData(dataOptJson);
    }

    private void mapData(JSONObject dataOptJson) {
        JSONArray nodes = dataOptJson.getJSONArray("nodeList");
        for (Object obj : nodes) {
            if (obj instanceof JSONObject) {
                JSONObject nodeJson = (JSONObject) obj;
                nodeMap.put(nodeJson.getString("id"), nodeJson);
            }
        }
        JSONArray links = dataOptJson.getJSONArray("linkList");
        for (Object obj : links) {
            if (obj instanceof JSONObject) {
                JSONObject linkJson = (JSONObject) obj;
                String sourceId = linkJson.getString("sourceId");
                List<JSONObject> nextNodes = linkMap.get(sourceId);
                if (nextNodes != null) {
                    nextNodes.add(linkJson);
                } else {
                    List<JSONObject> jsonObjects= new ArrayList<>();
                    jsonObjects.add(linkJson);
                    linkMap.put(linkJson.getString("sourceId"),
                        jsonObjects);
                }
            }
        }

    }

    public JSONObject getStartStep() {
        for (Map.Entry<String, JSONObject> m : nodeMap.entrySet()) {
            if ("start".equals(m.getValue().getString("type"))) {
                return getOptStep(m.getKey());
            }
        }
        return null;
    }

    public JSONObject getOptStep(String id) {
        return nodeMap.get(id);
    }

    public JSONObject getNextStep(String id) {
        List<JSONObject> links = getNextLinks(id);
        if (links == null||links.size()!=1) {
            return null;
            //throw new ObjectException("不是有且只有一个后续节点");
        }
        return getOptStep(links.get(0).getString("targetId"));
    }

    public List<JSONObject> getNextLinks(String id) {
        return linkMap.get(id);
    }
}

