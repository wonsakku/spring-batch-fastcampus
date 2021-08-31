package fastcampus.springbatch.part3;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ItemWriterConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final DataSource dataSource;
	private final EntityManagerFactory entityManagerFactory;

	@Bean
	public Job itemWriterJob() throws Exception{
		return jobBuilderFactory
				.get("itemWriterJob")
				.incrementer(new RunIdIncrementer())
				.start(this.csvItemWriterStep())
//				.next(this.jdbcBatchItemWriterStep())
				.next(this.jpaItemWriterStep())
				.build();
	}
	
	@Bean
	public Step jpaItemWriterStep() throws Exception {
		return stepBuilderFactory.get("jpaItemWriterStep")
				.<Person, Person>chunk(10)
				.reader(this.itemReader())
				.writer(this.jpaItemWriter())
				.build();
	}
	
	
	private ItemWriter<? super Person> jpaItemWriter() throws Exception {
		
		JpaItemWriter<Person> itemWriter = new JpaItemWriterBuilder<Person>()
				.entityManagerFactory(entityManagerFactory)
				.usePersist(true)
				.build();

		itemWriter.afterPropertiesSet();
		
		return itemWriter;
	}

	@Bean
	public Step jdbcBatchItemWriterStep() {
		return stepBuilderFactory.get("jdbcBatchItemWriterStep")
				.<Person, Person>chunk(10)
				.reader(this.itemReader())
				.writer(this.jdbcBatchItemWriter())
				.build()
				;
	}

	
	private ItemWriter<? super Person> jdbcBatchItemWriter() {
		
		JdbcBatchItemWriter<Person> itemWriter = new JdbcBatchItemWriterBuilder<Person>()
				.dataSource(dataSource)
				.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
				.sql("INSERT INTO person(name, age, address) values(:name, :age, :address)")
				.build();

		itemWriter.afterPropertiesSet();
		
		return itemWriter;
	}

	@Bean
	public Step csvItemWriterStep() throws Exception{
		return stepBuilderFactory
				.get("csvItemWriterStep")
				.<Person, Person>chunk(10)
				.reader(this.itemReader())
//				.processor()
				.writer(this.csvFileItemWriter())
				.build();
	}

	
	private ItemWriter<? super Person> csvFileItemWriter() throws Exception {
//		FlatFileItemWriter는 csv파일에 작성할 데이터를 추출하기 위해 FieldExtractor 객체가 필요
		BeanWrapperFieldExtractor<Person> fieldExtractor =new BeanWrapperFieldExtractor<>();
		fieldExtractor.setNames(new String[] {"id", "name", "age", "address"});
		
//		각 필드의 데이터를 하나의 라인에 작성하기 위해 구분값을 작성해줘야 한다.(콤마(,)로 필드값 구분)
		DelimitedLineAggregator<Person> lineAggregator = new DelimitedLineAggregator<>();
		lineAggregator.setDelimiter(",");
		lineAggregator.setFieldExtractor(fieldExtractor);
		
		FlatFileItemWriter<Person> itemWriter = new FlatFileItemWriterBuilder<Person>()
													.name("csvFileItemWriter")
													.encoding("UTF-8")
													.resource(new FileSystemResource("output/test-output.csv"))
													.lineAggregator(lineAggregator)
													.headerCallback(writer -> writer.write("id,이름,나이,거주지"))
													.footerCallback(writer -> writer.write("---------------\n"))
													.append(true)
													.build();
		
		itemWriter.afterPropertiesSet();

		return itemWriter;
	}



	private ItemReader<Person> itemReader(){
		return new CustomItemReader<>(getItems());
	}



	private List<Person> getItems() {
		List<Person> items = new ArrayList<>();
		
		for(int i = 0 ; i < 100 ; i++) {
			
			Person person = Person.builder()
					.name("test_name_" + (i+1))
					.age("test_age_" + (i+1))
					.address("test_address_" + (i+1))
					.build();
			
			items.add(person);
		}
		return items;
	}
	
}




























