package com.xxl.job.core.biz.model;

import java.util.ArrayList;
import java.util.List;

public class PagedJobInfoData{

    private int code;
    private String msg;

    private  int recordsTotal;

    private int recordsFiltered;

    private  List<JobInfo> data = new ArrayList<>();

    public PagedJobInfoData(){}
    public PagedJobInfoData(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public PagedJobInfoData(List<JobInfo> data) {
        this.code = ReturnT.SUCCESS_CODE;
        this.data = data;
        if(null != data) {
            this.recordsTotal = data.size();
            this.recordsFiltered = data.size();
        }
    }

    public static PagedJobInfoData fail(String msg){
        PagedJobInfoData retData = new PagedJobInfoData ();
        retData.setCode(ReturnT.FAIL_CODE);
        retData.setMsg(msg);
        return retData;
    }


    public void addData(JobInfo dt){
        data.add(dt);
    }

    public boolean isSuccess(){
        return code==ReturnT.SUCCESS_CODE;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(int recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    public int getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(int recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

    public List<JobInfo> getData() {
        return data;
    }

    public void setData(List<JobInfo> data) {
        this.data = data;
    }
}
