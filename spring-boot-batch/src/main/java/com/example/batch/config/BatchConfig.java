package com.example.batch.config;

import com.example.batch.model.EmployeeSyncItem;
import com.example.batch.processor.EmployeeItemProcessor;
import com.example.batch.reader.EmployeeJdbcReader;
import com.example.batch.writer.OpenSearchBulkWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
@EnableScheduling
public class BatchConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Value("${batch.chunk.size:100}")
    private int chunkSize;

    @Bean
    public Job employeeSyncJob(Step employeeSyncStep) {
        return jobBuilderFactory.get("employeeSyncJob")
                .incrementer(new RunIdIncrementer())
                .flow(employeeSyncStep)
                .end()
                .build();
    }

    @Bean
    public Step employeeSyncStep(ItemReader<EmployeeSyncItem> reader,
                                  ItemProcessor<EmployeeSyncItem, EmployeeSyncItem> processor,
                                  ItemWriter<EmployeeSyncItem> writer) {
        return stepBuilderFactory.get("employeeSyncStep")
                .<EmployeeSyncItem, EmployeeSyncItem>chunk(chunkSize)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .retry(Exception.class)
                .backoff(new Backoff(1000L, 2.0, 10000L))
                .build();
    }
}
