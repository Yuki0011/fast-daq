package com.mhaq.collect.entity;



import java.util.Date;

/**
 * @author xutao
 * @date 2021/4/4 8:32 下午
 * @description 实验项目表
 */

public class  LabItem {

    private Long  id;

    private String  labRoomGuid;


    private String  testFlag;

    private String  status;


    private String startTime;

    private String  endTime;

    private String  testNum;

    private String  item;

    private String  sampleNum;

    private String  sample;

    private String  sampleSpec;

    private String  developer;

    private String  remark;

    private String  purpose;


    private Date  testTime;

    private Long  point;

    public Long getPoint() {
        return point;
    }

    public void setPoint(Long point) {
        this.point = point;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getTestNum() {
        return testNum;
    }

    public void setTestNum(String testNum) {
        this.testNum = testNum;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getSampleNum() {
        return sampleNum;
    }

    public void setSampleNum(String sampleNum) {
        this.sampleNum = sampleNum;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public String getSampleSpec() {
        return sampleSpec;
    }

    public void setSampleSpec(String sampleSpec) {
        this.sampleSpec = sampleSpec;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Date getTestTime() {
        return testTime;
    }

    public void setTestTime(Date testTime) {
        this.testTime = testTime;
    }
}
