package com.mhaq.collect.entity;




/**
 * @author xutao
 * @date 2021/4/6 6:50 上午
 * @description 通道信息
 */

public class   LabItemColumnConfig  {

    protected Long id;



    private String  labRoomGuid;

    private String  testFlag;

    private String  colName;

    private String  colCaption;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabRoomGuid() {
        return labRoomGuid;
    }

    public void setLabRoomGuid(String labRoomGuid) {
        this.labRoomGuid = labRoomGuid;
    }

    public String getTestFlag() {
        return testFlag;
    }

    public void setTestFlag(String testFlag) {
        this.testFlag = testFlag;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public String getColCaption() {
        return colCaption;
    }

    public void setColCaption(String colCaption) {
        this.colCaption = colCaption;
    }
}
