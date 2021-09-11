package fastcampus.springbatch.part4;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import fastcampus.springbatch.TestConfiguration;

@SpringBatchTest
@ContextConfiguration(classes = {UserConfiguration.class, TestConfiguration.class})
public class UserConfigurationTest {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;
	
	@Autowired
	private UserRepository userRepository;
	
	
	@DisplayName("DB에 잘 들어가고 등급 상향이 잘 됐는지 테스트")
	@Test
	public void test() throws Exception {
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		
		int size = userRepository.findAllByUpdatedDate(LocalDate.now()).size();
		
		assertThat(
				jobExecution.getStepExecutions().stream()
					.filter(x-> x.getStepName().equals("userLevelUpStep"))
					.mapToInt(StepExecution::getWriteCount)
					.sum()
				).isEqualTo(size)
				 .isEqualTo(300)
		;
		
		assertThat(userRepository.count())
			.isEqualTo(400)
		;
	}
	
}
 
