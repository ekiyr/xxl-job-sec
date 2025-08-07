package com.xxl.job.executor.mvc.controller;

import com.xxl.job.core.biz.client.AdminJobInfoClient;
import com.xxl.job.core.biz.model.JobInfo;
import com.xxl.job.core.biz.model.JobTriggerParam;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.xxl.job.core.biz.model.ReturnT.FAIL_CODE;

@RestController
public class IndexController {

    @RequestMapping("/")
    @ResponseBody
    String index() {
        return "xxl job executor running.";
    }
    @Autowired
    private AdminJobInfoClient adminJobInfoClient;
    @PostMapping("/job/new")
    ReturnT<String> newJobInfo( @RequestBody JobInfo jobInfo) {
        return adminJobInfoClient.add(jobInfo);
    }

    @PostMapping("/job/update")
    ReturnT<String> updateJobInfo(@RequestBody JobInfo jobInfo) {
        return adminJobInfoClient.add(jobInfo);
    }

    @RequestMapping("/job/{jobId}/{op}")
    ReturnT<String> jobOp(@PathVariable("jobId")int jobId, @PathVariable("op") String op) {
        if("start".equalsIgnoreCase(op)){
            return adminJobInfoClient.start(jobId);
        }else if("remove".equalsIgnoreCase(op)){
            return adminJobInfoClient.remove(jobId);
        }else if("stop".equalsIgnoreCase(op)){
            return adminJobInfoClient.stop(jobId);
        }
        return new ReturnT<>(FAIL_CODE, "无效操作:"+op);
    }

    @PostMapping("/job/trigger")
    ReturnT<String> trigger(@RequestBody JobTriggerParam jobTriggerParam) {
        if(jobTriggerParam.getId() <=0 ){
           return new ReturnT<>(FAIL_CODE, "无效JOB:"+jobTriggerParam.getId());
        }
        return adminJobInfoClient.trigger(jobTriggerParam);
    }

}