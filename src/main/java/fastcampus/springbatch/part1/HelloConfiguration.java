package fastcampus.springbatch.part1;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class HelloConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	
	@Bean
	public Job helloJob() {
		return jobBuilderFactory.get("helloJob")
					.incrementer(new RunIdIncrementer())
					.start(this.helloStep())
					.build();
	}
	
	@Bean
	public Step helloStep() {
		return stepBuilderFactory.get("helloJob")
				.tasklet((contribution, chunckContext) -> {
					log.info("hello spring batch");
					return RepeatStatus.FINISHED;
				}).build();
	}
}






































