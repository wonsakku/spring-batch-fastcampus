package fastcampus.springbatch.part3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ItemReaderConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final DataSource dataSource;
	private final EntityManagerFactory entityManagerFactory;

	@Bean
	public Job itemReaderJob() throws Exception{
		return jobBuilderFactory.get("itemReaderJob")
				.incrementer(new RunIdIncrementer())
				.start(this.customItemReaderStep())
				.next(this.csvFileStep())
				.next(this.jdbcStep())
				.next(this.jpaStep())
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
	
	@Bean
	public Step csvFileStep() throws Exception{
		return stepBuilderFactory.get("csvFileStep")
				.<Person, Person>chunk(5)
				.reader(this.csvFileItemReader())
				.writer(itemWriter())
				.build();
	}
	
	@Bean
	public Step jdbcStep() throws Exception {
		return stepBuilderFactory.get("jdbcStep")
				.<Person, Person>chunk(10)
				.reader(this.jdbcCursorItemReader())
				.writer(itemWriter())
				.build();
	}
	
	@Bean
	public Step jpaStep() throws Exception{
		return stepBuilderFactory.get("jpaStep")
				.<Person, Person>chunk(10)
				.reader(this.jpaCursorItemReader())
				.writer(this.itemWriter())
				.build();
	}
	
	
	
	private JpaCursorItemReader<Person> jpaCursorItemReader() throws Exception {
		
		JpaCursorItemReader<Person> itemReader = new JpaCursorItemReaderBuilder<Person>()
													.name("jpaCursorItemReader")
													.entityManagerFactory(entityManagerFactory)
													.queryString("SELECT p FROM Person p")
													.build();
		
		itemReader.afterPropertiesSet();
		return itemReader;
	}
	
	
	private JdbcCursorItemReader<Person> jdbcCursorItemReader() throws Exception{
		JdbcCursorItemReader<Person> itemReader = new JdbcCursorItemReaderBuilder<Person>()
															.name("jdbcCursorItemReader")
															.dataSource(dataSource)
															.sql("SELECT id, name, age, address FROM person")
															.rowMapper((rs, rowNum) -> 
																new Person(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)))
															.build();
		itemReader.afterPropertiesSet();
		return itemReader;
	}
	
	
	private FlatFileItemReader<Person> csvFileItemReader() throws Exception{
//		data를 읽을 수 있는 설정
//		csv파일을 1줄씩 읽을 수 있는 설정
		DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();
		
//		csv 파일을 Person 객체에 매핑하기 위해 Person 필드명을 설정하는 Tokenizer 객체 필요
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setNames("id", "name", "age", "address");
		
		lineMapper.setLineTokenizer(tokenizer);
		
		lineMapper.setFieldSetMapper(fieldSet -> {
			int id = fieldSet.readInt("id");
			String name = fieldSet.readString("name");
			String age = fieldSet.readString("age");
			String address = fieldSet.readString("address");
			
			return new Person(id, name, age, address);
		});
		
		
		FlatFileItemReader<Person> itemReader = new FlatFileItemReaderBuilder<Person>()
												.name("csvFileReader")
												.encoding("UTF-8")
												.resource(new ClassPathResource("test.csv"))
												.linesToSkip(1)
												.lineMapper(lineMapper)
												.build()
												;
		
		itemReader.afterPropertiesSet();
		
		return itemReader;
		
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














