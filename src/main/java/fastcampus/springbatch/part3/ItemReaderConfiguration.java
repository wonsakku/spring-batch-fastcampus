package fastcampus.springbatch.part3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ItemReaderConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job itemReaderJob() {
		return jobBuilderFactory.get("itemReaderJob")
				.incrementer(new RunIdIncrementer())
				.start(this.customItemReaderStep())
				.build();
	}

	@Bean
	public Step customItemReaderStep() {
		return stepBuilderFactory.get("customItemReaderStep")
				.<Person, Person>chunk(3)
				.reader(new CustomItemReader<>(getItems()))
//				.processor()
				.writer(itemWriter())
				.build();
	}

	private ItemWriter<? super Person> itemWriter() {
		return items -> log.info(items.stream().map(Person::getName)
					.collect(Collectors.joining(", "))
				);
	}

	private List<Person> getItems() {
		List<Person> items = new ArrayList<>();
		for(int i = 0 ; i < 20 ; i++) {
			items.add(new Person(i + 1, "test_name_" + (i+1), "test_age_" + (i+1), "test_address_" + (i+1)));
		}
			
		return items;
	}

}














