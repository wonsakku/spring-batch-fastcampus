package fastcampus.springbatch.part3;

import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class SavePersonConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory entityManagerFactory;
	
	@Bean
	public Job savePersonJob() throws Exception {
		return jobBuilderFactory.get("savePersonJob")
				.incrementer(new RunIdIncrementer())
				.start(savePersonStep(null))
				.listener(new SavePersonListener.SavePersonJobExecutionListener())
				.listener(new SavePersonListener.SavePersonAnnotationExecution())
				.build();
	}

	@Bean
	@JobScope
	public Step savePersonStep(@Value("#{jobParameters[allow_duplicate]}") String allowDuplicate) throws Exception {
		return stepBuilderFactory.get("savePersonStep")
				.<Person, Person>chunk(10)
				.reader(this.itemReader())
//				.processor(new DuplicateValidationProcessor<>(Person::getName, Boolean.parseBoolean(allowDuplicate)))
				.processor(this.itemProcessor(allowDuplicate))
				.writer(this.itemWriter())
				.listener(new SavePersonListener.SavePersonStepExecutionListener())
				.faultTolerant()
				.skip(NotFoundNameException.class)
				.skipLimit(1)
				.build();
	}

	private ItemProcessor<? super Person, ? extends Person> itemProcessor(String allowDuplicate) throws Exception{
		
		DuplicateValidationProcessor<Person> duplicateValidationProcessor = 
				new DuplicateValidationProcessor<>(Person::getName, Boolean.parseBoolean(allowDuplicate));
		
		ItemProcessor<Person, Person> validationProcessor = item -> {
			if(item.isNotEmptyName()) {
				return item;
			}
			throw new NotFoundNameException();
		};
		
		CompositeItemProcessor<Person, Person> itemProcessor = new CompositeItemProcessorBuilder()
				.delegates(new PersonValidationRetryProcessor(), validationProcessor, duplicateValidationProcessor)
				.build();
				
		
		itemProcessor.afterPropertiesSet();
		
		return itemProcessor;
	}

	
	private ItemWriter<? super Person> itemWriter() throws Exception {

		JpaItemWriter<Person> jpaItemWriter = new JpaItemWriterBuilder<Person>()
				.entityManagerFactory(entityManagerFactory)
				.build();
		
		
		ItemWriter<Person> logItemWriter = items -> log.info("person.size : {}", items.size());
		
		
		CompositeItemWriter<Person> itemWriter = new CompositeItemWriterBuilder<Person>()
				.delegates(jpaItemWriter, logItemWriter)
				.build();

		itemWriter.afterPropertiesSet();
		
		return itemWriter;
//		return items -> items.forEach(x -> log.info("저는 {}입니다.", x.getName()));
	}

	private ItemReader<? extends Person> itemReader() throws Exception {
		
		DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();
		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		
		lineTokenizer.setNames("name", "age", "address");
		lineMapper.setLineTokenizer(lineTokenizer);
		lineMapper.setFieldSetMapper(fieldSet -> {
			return Person.builder()
					.name(fieldSet.readString(0))
					.age(fieldSet.readString(1))
					.address(fieldSet.readString(2))
					.build();
		});
		
		
		FlatFileItemReader<Person> itemReader = new FlatFileItemReaderBuilder<Person>()
				.name("savePersonItemReader")
				.encoding("UTF-8")
				.linesToSkip(1)
				.resource(new ClassPathResource("person.csv"))
				.lineMapper(lineMapper)
				.build();

		itemReader.afterPropertiesSet();
		
		return itemReader;
	}
	

}





















