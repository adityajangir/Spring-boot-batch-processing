//package com.example.demo;
//
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobParametersBuilder;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.Date;
//
//@Service
//public class JobLauncherService {
//
//    @Autowired
//    private JobLauncher jobLauncher;
//
//    @Autowired
//    private Job importStudentJob;
//
//    public void runJob() throws Exception {
//        jobLauncher.run(importStudentJob, new JobParametersBuilder()
//                .addDate("launchDate", new Date())
//                .toJobParameters());
//    }
//}
