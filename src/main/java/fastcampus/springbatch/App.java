package fastcampus.springbatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableBatchProcessing
@SpringBootApplication
public class App {

	public static void main(String[] args) {
		
//		SpringApplication.run(App.class, args);
		
		System.exit(SpringApplication.exit(SpringApplication.run(App.class, args)));
	}
	
	
	
	
	@Bean
	@Primary
	TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		
		taskExecutor.setCorePoolSize(10);
		taskExecutor.setMaxPoolSize(20);
		taskExecutor.setThreadNamePrefix("batch-thread-");
		taskExecutor.initialize();
		
		return taskExecutor;
	}

}
