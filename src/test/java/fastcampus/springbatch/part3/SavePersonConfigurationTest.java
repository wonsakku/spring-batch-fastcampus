package fastcampus.springbatch.part3;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fastcampus.springbatch.TestConfiguration;

//@SpringBootTest
@SpringBatchTest
@ContextConfiguration(classes = {SavePersonConfiguration.class, TestConfiguration.class})
class SavePersonConfigurationTest {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;
	
	@Autowired
	private PersonRepository personRepository;
	
	@AfterEach
	public void tearDown() throws Exception{
		personRepository.deleteAll();
	}

	@Test
	@DisplayName("step만 테스트")
	public void test_step() {
		JobExecution jobExecution = jobLauncherTestUtils.launchStep("savePersonStep");
		
		assertThat(
				jobExecution
					.getStepExecutions()
					.stream()
					.mapToInt(StepExecution::getWriteCount)
					.sum()
				)
		.isEqualTo(personRepository.count())
		.isEqualTo(3)
		;		

	}
	
	@Test
	@DisplayName("allow_dupliate가 false일 경우 테스트")
	public void test_allow_duplicate() throws Exception {
		
		//given
		JobParameters jobParameters = new JobParametersBuilder()
				.addString("allow_duplicate", "false")
				.toJobParameters();
		
		//when
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		
		
		//then
		assertThat(
				jobExecution
					.getStepExecutions()
					.stream()
					.mapToInt(StepExecution::getWriteCount)
					.sum()
				)
		.isEqualTo(personRepository.count())
		.isEqualTo(3)
		;		
		
	}

	@Test
	@DisplayName("allow_dupliate가 true일 경우 테스트")
	public void test_not_allow_duplicate() throws Exception {
		
		//given
		JobParameters jobParameters = new JobParametersBuilder()
				.addString("allow_duplicate", "true")
				.toJobParameters();
		
		//when
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		
		
		//then
		assertThat(
				jobExecution
				.getStepExecutions()
				.stream()
				.mapToInt(StepExecution::getWriteCount)
				.sum()
				)
		.isEqualTo(personRepository.count())
		.isEqualTo(100)
		;		
		
	}
	
}






































