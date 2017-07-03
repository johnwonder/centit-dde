package com.centit.dde.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import com.centit.dde.po.MapInfoDetail;
import com.centit.dde.service.MapInfoDetailManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.centit.dde.dao.MapinfoDetailDao;
import com.centit.dde.po.MapinfoDetailId;
import com.centit.framework.hibernate.service.BaseEntityManagerImpl;
import com.centit.framework.staticsystem.po.DatabaseInfo;
@Service
public class MapInfoDetailManagerImpl extends BaseEntityManagerImpl<MapInfoDetail,MapinfoDetailId,MapinfoDetailDao>
        implements MapInfoDetailManager {
    public static final Log log = LogFactory.getLog(MapInfoDetailManager.class);

    //private static final SysOptLog sysOptLog = SysOptLogFactoryImpl.getSysOptLog();
    
    private MapinfoDetailDao mapinfoDetailDao;

    @Resource(name="mapinfoDetailDao")
    @NotNull
    public void setMapinfoDetailDao(MapinfoDetailDao baseDao) {
        this.mapinfoDetailDao = baseDao;
        setBaseDao(this.mapinfoDetailDao);
    }

    
    public List<MapInfoDetail> listByMapinfoId(Long mapinfoId) {
        return mapinfoDetailDao.listByMapinfoId(mapinfoId);
    }
    
    public List<Map<String, String>> getGoalTableStruct(DatabaseInfo DatabaseInfo, String tableName) {
        return mapinfoDetailDao.getGoalTableStruct(DatabaseInfo, tableName);
    }

    public List<Map<String, String>> getSourceTableStruct(DatabaseInfo DatabaseInfo, String tableName) {
        return mapinfoDetailDao.getSourceTableStruct(DatabaseInfo, tableName);
    }

    public List<String> getTables(DatabaseInfo databaseInfo) {
        return mapinfoDetailDao.getTables(databaseInfo);
    }

    public List<Object> getTable(DatabaseInfo databaseInfo) {
        return mapinfoDetailDao.getTable(databaseInfo);
    }

    public void deleteMapinfoDetails(Long mapinfoId) {
        mapinfoDetailDao.deleteMapinfoDetails(mapinfoId);
    }

    public void updateExchangeMapinfo(Long mapinfoId, String soueceTableName, String goalTableName, String createSql) {
        mapinfoDetailDao.updateExchangeMapinfo(mapinfoId, soueceTableName, goalTableName, createSql);
    }

    public List<Map<String, String>> getSourceTableStructFromDatabase(Long mapinfoId) {
        return mapinfoDetailDao.getSourceTableStructFromDatabase(mapinfoId);
    }

    public List<Map<String, String>> getGoalTableStructFromDatabase(Long mapinfoId) {
        return mapinfoDetailDao.getGoalTableStructFromDatabase(mapinfoId);
    }

    public void updateSourceColumnSentence(Map<String, Object> structs, String mapinfoId) {
        mapinfoDetailDao.updateSourceColumnSentence(structs, mapinfoId);
    }

    public Long getMapinfoId() {
        return mapinfoDetailDao.getMapinfoId();
    }

    public List<String> getGoalColumnStrut(Long mapinfoId) {
        return this.mapinfoDetailDao.getGoalColumnStrut(mapinfoId);
    }

    public void saveMapinfoDetails(MapInfoDetail mapInfoDetail) {
        this.mapinfoDetailDao.saveMapinfoDetails(mapInfoDetail);
    }

}
