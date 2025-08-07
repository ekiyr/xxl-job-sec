package com.xxl.job.core.biz.model;

import java.util.ArrayList;
import java.util.List;

public class PagedRetData <T>{

    private int code;
    private String msg;

    private  int recordsTotal;

    private int recordsFiltered;

    private  List<T> data = new ArrayList<>();

    public PagedRetData(){}
    public PagedRetData(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public PagedRetData(List<T> data) {
        this.code = ReturnT.SUCCESS_CODE;
        this.data = data;
        if(null != data) {
            this.recordsTotal = data.size();
            this.recordsFiltered = data.size();
        }
    }

    public static PagedRetData<?> fail(String msg){
        PagedRetData<?> retData = new PagedRetData<>();
        retData.setCode(ReturnT.FAIL_CODE);
        retData.setMsg(msg);
        return retData;
    }

    public static <R> PagedRetData<R> succ(String msg){
        PagedRetData<R> retData = new PagedRetData<>();
        retData.setCode(ReturnT.SUCCESS_CODE);
        retData.setMsg(msg);
        return retData;
    }
    public static <R> PagedRetData<R> succ(List<R> data){
        PagedRetData<R> retData = new PagedRetData<>();
        retData.setCode(ReturnT.SUCCESS_CODE);
        retData.setMsg("成功");
        if(null != data) {
            retData.recordsTotal = data.size();
            retData.recordsFiltered = data.size();
        }
        retData.setData(data);
        return retData;
    }

    public void addData(T dt){
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

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
