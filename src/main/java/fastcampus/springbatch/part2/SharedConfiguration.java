package fastcampus.springbatch.part2;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public
class SharedConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	
	@Bean
	public Job shareJob() {
		return jobBuilderFactory.get("shareJob")
				.incrementer(new RunIdIncrementer())
				.start(this.shareStep())
				.next(this.shareStep2())
				.build();
	}


	@Bean
	public Step shareStep() {
		return stepBuilderFactory.get("shareStep")
				.tasklet((contribution, chunckContext) -> {
					
					StepExecution stepExecution = contribution.getStepExecution();
					ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
					stepExecutionContext.putString("stepKey", "step execution context");
					
					JobExecution jobExecution = stepExecution.getJobExecution();
					JobInstance jobInstance = jobExecution.getJobInstance();
					ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();
					jobExecutionContext.putString("jobKey", "job execution context");
					JobParameters jobParameters = jobExecution.getJobParameters();
					
					log.info("jobName : {}, stepName : {}, parameter : {}",
							jobInstance.getJobName(),
							stepExecution.getStepName(),
							jobParameters.getLong("run.id")
							);
					
					
					return RepeatStatus.FINISHED;
				}).build()
				;
	}
	

	@Bean
	public Step shareStep2() {
		return stepBuilderFactory.get("shareStep2")
				.tasklet((contribution, chunkContext)->{
					
					StepExecution stepExecution = contribution.getStepExecution();
					ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
					
					JobExecution jobExecution = stepExecution.getJobExecution();
					ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();
					
					log.info("jobKey : {}, stepKey : {}",
							jobExecutionContext.getString("jobKey", "emptyJobKey"),
							stepExecutionContext.getString("stepKey", "emptyStepKey")
							);
					
					return RepeatStatus.FINISHED;
				}).build()
				;
	}

}








































