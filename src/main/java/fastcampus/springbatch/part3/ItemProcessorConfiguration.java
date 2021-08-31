package fastcampus.springbatch.part3;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ItemProcessorConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job itemProcessorJob() {
		return jobBuilderFactory.get("itemProcessorJob")
				.incrementer(new RunIdIncrementer())
				.start(itemProcessorStep())
				.build();
	}

	@Bean
	public Step itemProcessorStep() {
		return stepBuilderFactory.get("itemProcessorStep")
				.<Person, Person>chunk(10)
				.reader(this.itemReader())
				.processor(this.itemProcessor())
				.writer(this.itemWriter())
				.build();
	}

	private ItemWriter<? super Person> itemWriter() {
		return items -> {
			items.forEach(x -> log.info("PERSON.ID : {}", x.getId()));
		};
	}

	private ItemProcessor<? super Person, ? extends Person> itemProcessor() {
		return item -> {
			if(item.getId() % 2 == 0) {
				return item;
			}
			return null;
		};
	}

	private ItemReader<? extends Person> itemReader() {
		return new CustomItemReader<Person>(getItems());
	}

	private List<Person> getItems() {
		
		List<Person> items = new ArrayList<>();
		
		for(int i = 0 ; i < 10 ; i++) {
			Person person = Person.builder()
					.id(i+1)
					.name("test_name_" + (i+1))
					.age("test_age_" + (i+1))
					.address("test_address_" + (i+1))
					.build();
			
			items.add(person);
		}
		
		return items;
	}

}



















