package fastcampus.springbatch.part4;

import java.time.LocalDate;
import java.util.Collection;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LevelUpJobExecutionListener implements JobExecutionListener{

	private final UserRepository userRepository;
	
	@Override
	public void beforeJob(JobExecution jobExecution) {}

	@Override
	public void afterJob(JobExecution jobExecution) {
		
		Collection<User> users = userRepository.findAllByUpdatedDate(LocalDate.now());
		
		long time = jobExecution.getEndTime().getTime() - jobExecution.getStartTime().getTime();
		
		log.info("회원 등급 업데이트 배치 프로그램");
		log.info("-----------------------");
		log.info("총 데이터 처리 {}건, 처리 시간 {}millis", users.size(), time);
		
	}

}
