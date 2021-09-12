package fastcampus.springbatch.part6;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;

import fastcampus.springbatch.part4.LevelUpJobExecutionListener;
import fastcampus.springbatch.part4.SaveUserTasklet;
import fastcampus.springbatch.part4.User;
import fastcampus.springbatch.part4.UserRepository;
import fastcampus.springbatch.part5.JobParameterDecide;
import fastcampus.springbatch.part5.OrderStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
 
@Configuration
@Slf4j
@RequiredArgsConstructor
public class ParallelUserConfiguration {
 
	private final String JOB_NAME = "parallelUserJob";
	private final int CHUNK = 1000;
	
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final UserRepository userRepository;
	private final EntityManagerFactory entityManagerFactory;
	private final DataSource dataSource;
	private final TaskExecutor taskExecutor; 
	
	
	@Bean(JOB_NAME)
	public Job userJob() throws Exception {
		return this.jobBuilderFactory.get(JOB_NAME)
				.incrementer(new RunIdIncrementer())
				.listener(new LevelUpJobExecutionListener(userRepository))
				.start(this.saveUserFlow())
				.next(this.splitFlow(null))
				.build()
				.build();
	}
	
	
	@Bean(JOB_NAME + "_saveUserFlow")
	public Flow saveUserFlow() {
		
		TaskletStep saveUserStep =  this.stepBuilderFactory.get(JOB_NAME + "_saveUserStep")
				.tasklet(new SaveUserTasklet(userRepository))
				.build()
				;
		
		return new FlowBuilder<SimpleFlow>(JOB_NAME + "_saveUserFlow")
				.start(saveUserStep)
				.build();
		
	}
	
	@Bean(JOB_NAME + "_splitFlow")
	@JobScope
	public Flow splitFlow(@Value("#{jobParameters[date]}") String date) throws Exception {
		
		Flow userLevelUpFlow = new FlowBuilder<SimpleFlow>(JOB_NAME + "_userLevelUpFlow")
				.start(this.userLevelUpStep())
				.build();
		
		
		return new FlowBuilder<SimpleFlow>(JOB_NAME + "_splitFlow")
				.split(this.taskExecutor)
				.add(userLevelUpFlow, orderStatisticsFlow(date))
				.build();
	}
	
	 
	 
	private Flow orderStatisticsFlow(String date) throws Exception {
		return new FlowBuilder<SimpleFlow>(JOB_NAME + "_orderStatisticsFlow")
				.start(new JobParameterDecide("date"))
				.on(JobParameterDecide.CONTINUE.getName())
				.to(this.orderStatisticsStep(date))
				.build();
	}
	
	 
//	@Bean(JOB_NAME + "_orderStatisticsStep")
//	@JobScope
	private Step orderStatisticsStep(@Value("#{jobParameters[date]}") String date) throws Exception{
		return this.stepBuilderFactory.get(JOB_NAME + "_orderStatisticsStep")
				.<OrderStatistics, OrderStatistics>chunk(CHUNK)
				.reader(orderStatisticsItemReader(date))
				.writer(orderStatisticsItemWriter(date))
				.build();
	}
	 
	
	private ItemWriter<? super OrderStatistics> orderStatisticsItemWriter(String date) throws Exception {
		
		YearMonth yearMonth = YearMonth.parse(date);
		String fileName = yearMonth.getYear() + "년_" + yearMonth.getMonthValue() + "월_일별_주문_금액.csv";
		
		BeanWrapperFieldExtractor<OrderStatistics> fieldExtractor = new BeanWrapperFieldExtractor<>();
		fieldExtractor.setNames(new String[] {"amount", "date"});
		
		DelimitedLineAggregator<OrderStatistics> lineAggregator = new DelimitedLineAggregator<>();
		lineAggregator.setDelimiter(",");
		lineAggregator.setFieldExtractor(fieldExtractor);
		
		FlatFileItemWriter<OrderStatistics> itemWriter = new FlatFileItemWriterBuilder<OrderStatistics>()
				.resource(new FileSystemResource("output/" + fileName))
				.lineAggregator(lineAggregator)
				.name(JOB_NAME + "_orderStatisticsItemWriter")
				.encoding("UTF-8")
				.headerCallback(writer -> writer.write("total_amount, date"))
				.build();
		
		itemWriter.afterPropertiesSet();
		
		return itemWriter;
	}
     
	private ItemReader<? extends OrderStatistics> orderStatisticsItemReader(String date) throws Exception {
		// jdbcPagingItemReader를 이용해서 orders의 일별 총 합산된 금액을 조회해서
		// OrderStatistics 클래스로 담아서 ItemWriter로 넘길 것이다.
		// 그래서 String date를 쿼리를 날릴 수 있도록 YearMonth 객체로 파싱해준다.
		YearMonth yearMonth = YearMonth.parse(date);
		
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("startDate", yearMonth.atDay(1)); 
		parameters.put("endDate", yearMonth.atEndOfMonth());
		
		Map<String, Order> sortKey = new HashMap<>();
		sortKey.put("created_date", Order.ASCENDING);
		 
		JdbcPagingItemReader<OrderStatistics> itemReader = new JdbcPagingItemReaderBuilder<OrderStatistics>()
				.dataSource(dataSource)
				.rowMapper(
							(resultSet, i) -> OrderStatistics.builder()
								.amount(resultSet.getString(1))
								.date(LocalDate.parse(resultSet.getString(2), DateTimeFormatter.ISO_DATE))
								.build()
						)
				.pageSize(CHUNK)
				.name(JOB_NAME + "_orderStatisticsItemReader")
				.selectClause("sum(amount), created_date")
				.fromClause("orders")
				.whereClause("created_date >= :startDate and created_date <= :endDate")
				.groupClause("created_date")
				.parameterValues(parameters)
				.sortKeys(sortKey)
				.build();
		
		itemReader.afterPropertiesSet();
			
		return itemReader;
	}

//	@Bean(JOB_NAME + "_saveUserStep")
//	public Step saveUserStep() {
//		return this.stepBuilderFactory.get(JOB_NAME + "_saveUserStep")
//				.tasklet(new SaveUserTasklet(userRepository))
//				.build()
//				;
//	}
	
	@Bean(JOB_NAME + "_userLevelUpStep")
	public Step userLevelUpStep() throws Exception {
		return this.stepBuilderFactory.get(JOB_NAME + "_userLevelUpStep")
				.<User, User>chunk(CHUNK)
				.reader(itemReader())
				.processor(itemProcessor())
				.writer(itemWriter())
				.build();
	}
	
	private ItemWriter<? super User> itemWriter() {
		return users -> {
			users.forEach(x -> {
				x.levelUp();
				userRepository.save(x);
			});
		};
	}

	private ItemProcessor<? super User, ? extends User> itemProcessor() {
		return user -> {
			if(user.availableLevelUp()) {
				return user;
			}
			
			return null;
		};
	}

	private ItemReader<? extends User> itemReader() throws Exception {
		JpaPagingItemReader<User> itemReader = new JpaPagingItemReaderBuilder<User>()
				.queryString("SELECT u FROM User u")
				.entityManagerFactory(entityManagerFactory)
				.pageSize(CHUNK) //pageSize는 보통 chunk 사이즈와 동일하게 설정
				.name(JOB_NAME + "_userItemReader")
				.build();

		itemReader.afterPropertiesSet();
		
		return itemReader;
	}
}



 
 























