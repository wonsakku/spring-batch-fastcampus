package fastcampus.springbatch.part4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SaveUserTasklet implements Tasklet{

	private final UserRepository userRepository;
	
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		
		List<User> users = createUsers();
		
		Collections.shuffle(users);
		
		userRepository.saveAll(users);
		
		return RepeatStatus.FINISHED;
	}
	
	private List<User> createUsers(){
		List<User> users = new ArrayList<>();
		
		for(int i = 0 ; i < 100 ; i++) {
			users.add(
						User.builder()
							.totalAmount(1_000)
							.username("test_user_" + i)
							.build()
					);
		}
		for(int i = 100 ; i < 200 ; i++) {
			users.add(
					User.builder()
					.totalAmount(200_000)
					.username("test_user_" + i)
					.build()
					);
		}
		for(int i = 200 ; i < 300 ; i++) {
			users.add(
					User.builder()
					.totalAmount(300_000)
					.username("test_user_" + i)
					.build()
					);
		}
		for(int i = 300 ; i < 400 ; i++) {
			users.add(
					User.builder()
					.totalAmount(500_000)
					.username("test_user_" + i)
					.build()
					);
		}
		return users;
	}
	
}










































