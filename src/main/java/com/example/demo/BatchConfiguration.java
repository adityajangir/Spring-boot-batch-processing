package com.example.demo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.data.MongoPagingItemReader;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
import org.springframework.batch.item.data.builder.MongoPagingItemReaderBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.config.Task;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
public class BatchConfiguration {
    @Autowired
    MongoTemplate mongoTemplate;

    @Bean
    public MongoPagingItemReader<NoSqlDataModel> reader() {
        return new MongoPagingItemReaderBuilder<NoSqlDataModel>()
                .name("studentItemReader")
                .template(mongoTemplate)
                .jsonQuery("{}")
                .targetType(NoSqlDataModel.class)
                .sorts(new HashMap<>())
                .pageSize(10)
                .build();
    }

    @Bean
    public StudentItemProcessor processor() {
        return new StudentItemProcessor();
    }

    @Bean
    public Processorinitial processorinitial() {
        return new Processorinitial();
    }

    @Bean
    public JdbcBatchItemWriter<Student> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Student>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO student (first_name, last_name, course) VALUES (:firstName, :lastName, :course)")
                .dataSource(dataSource)
                .build();
    }



    @Bean
    public FlatFileItemReader<NoSqlDataModel> readerinitial () {
        return new FlatFileItemReaderBuilder<NoSqlDataModel>()
                .name("csv_reader")
                .resource(new ClassPathResource("student-data.csv"))
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {
                    {
                        setTargetType(NoSqlDataModel.class);
                    }
                })
                .delimited()
                .names("id", "firstName", "lastName", "course")
                .build();
    }

    @Bean
    public MongoItemWriter<NoSqlDataModel> writerinitial () {
        return new MongoItemWriterBuilder<NoSqlDataModel>()
                .collection("students")
                .template(mongoTemplate)
                .build();
    }

    @Bean
    public FlatFileItemWriter<NoSqlDataModel> stepthreewriter() {
        return new FlatFileItemWriterBuilder<NoSqlDataModel>()
                .name("noSqlDataModelItemWriter")
                .resource(new FileSystemResource("nosql_data.csv"))
                .delimited()
                .delimiter(",")
                .names(new String[]{"firstName", "lastName", "course"})  // Replace with your actual field names
                .build();
    }

    @Bean
    public Step step2(JobRepository jobRepository, PlatformTransactionManager transactionManager, MongoItemWriter<NoSqlDataModel> writerinitial,
                      FlatFileItemReader<NoSqlDataModel> readerinitial, Processorinitial processorinitial){
        return new StepBuilder("step2", jobRepository)
                .<NoSqlDataModel, NoSqlDataModel>chunk(1, transactionManager)
                .reader(readerinitial)
                .processor(processorinitial())
                .writer(writerinitial)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                      MongoPagingItemReader<NoSqlDataModel> reader, StudentItemProcessor processor,
                      JdbcBatchItemWriter<Student> writer) {
        return new StepBuilder("step1", jobRepository)
                .<NoSqlDataModel, Student>chunk(1, transactionManager)
                .reader(reader)
                .processor(processor())
                .writer(writer)
                .build();
    }
    @Bean
    public Step step3 (JobRepository jobRepository, PlatformTransactionManager transactionManager, FlatFileItemReader<NoSqlDataModel> readerinitial,
                       Processorinitial processorinitial, FlatFileItemWriter stepthreewriter){
        return new StepBuilder("step3", jobRepository)
                .<NoSqlDataModel, NoSqlDataModel>chunk(1, transactionManager)
                .reader(readerinitial)
                .processor(processorinitial())
                .writer(stepthreewriter)
                .build();
    }

    @Bean
    public Flow splitFlow(Flow flow1, Flow flow2) {
        return new FlowBuilder<SimpleFlow>("splitFlow")
                .split(taskExecutor())
                .add(flow1, flow2)
                .build();
    }


    @Bean
    public Job importStudentJob(JobRepository jobRepository, JobCompletionNotificationListener listener, Step step1, Step step2, Step step3, Flow flow1, Flow flow2) {
        return new JobBuilder("importStudentJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(splitFlow(flow1, flow2))
                .build()
                .build();
    }


    @Bean
    public Flow flow1(Step step1, Step step2) {
        return new FlowBuilder<SimpleFlow>("flow1")
                .start(step1)
                .next(step2)
                .build();
    }


    @Bean
    public Flow flow2(Step step3) {
        return new FlowBuilder<SimpleFlow>("flow2")
                .start(step3)
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("spring_batch");
    }
}
