package fastcampus.springbatch.part3;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ChunkProcessingConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	
	@Bean
	public Job chunckProcessiongJob() {
		return jobBuilderFactory.get("chunkProcessingJob")
				.incrementer(new RunIdIncrementer())
				.start(this.taskBaseStep())
//				.next(this.chunkBaseStep())
				.next(this.chunkBaseStep(null))
				.build();
	}

	@Bean
	public Step taskBaseStep() {
		return stepBuilderFactory.get("taskBaseStep")
				.tasklet(this.tasklet(null))
				.build()
				;
	}
	
	

	@Bean
	@JobScope
//	@Scope(value = "job", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public Step chunkBaseStep(@Value("#{jobParameters[chunkSize]}") String chunkSize) {
		return stepBuilderFactory.get("chunkBaseStep")
//				.<String, String>chunk(24)
				.<String, String>chunk(StringUtils.isNotEmpty(chunkSize) ? Integer.parseInt(chunkSize) : 10) 
				.reader(itemReader())
				.processor(itemProcessor())
				.writer(itemWriter())
				.build()
				;
	}
	
	
	private ItemWriter<? super String> itemWriter() {
		return items -> log.info("chunck item size : {}", items.size());
//		return items -> items.forEach(log::info);
	}

	private ItemProcessor<? super String, ? extends String> itemProcessor() {
		return item -> item + ", Spring Batch";
	}

	private ItemReader<? extends String> itemReader() {
		return new ListItemReader<>(getItems());
	}


	@Bean
	@StepScope
//	public Tasklet tasklet() {
	public Tasklet tasklet(@Value("#{jobParameters[chunkSize]}") String value) {
		List<String> items = getItems();
		
		return (contribution, chunkContext) -> {
			
			StepExecution stepExecution = contribution.getStepExecution();
//			JobParameters jobParameters = stepExecution.getJobParameters();
			
			
//			String value = jobParameters.getString("chunkSize", "10");
			int chunkSize = StringUtils.isNotEmpty(value) ? Integer.parseInt(value) : 10;
			int fromIndex = stepExecution.getReadCount();
			int toIndex = fromIndex + chunkSize;
			
			
			if(fromIndex >= items.size()) {
				return RepeatStatus.FINISHED;
			}
			
			if(toIndex > items.size()) {
				toIndex = items.size();
			}
			
			
			List<String> subList = items.subList(fromIndex, toIndex);
			log.info("task item size : {}", subList.size());
			
			stepExecution.setReadCount(toIndex);
			
			return RepeatStatus.CONTINUABLE;
		};
	}

	private List<String> getItems() {
		List<String> items = new ArrayList<>();
		for(int i = 0 ; i < 100 ; i++) {
			items.add(i + "_Hello");
		}
		return items;
	}
}











































