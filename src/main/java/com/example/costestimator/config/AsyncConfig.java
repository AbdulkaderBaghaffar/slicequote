package com.example.costestimator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean
  public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
    ex.setCorePoolSize(4); // threads ready to work
    ex.setMaxPoolSize(8); // ceiling
    ex.setQueueCapacity(25); // waiting jobs
    ex.setThreadNamePrefix("slicer-"); // shows in logs
    ex.initialize();
    return ex;
  }
}
