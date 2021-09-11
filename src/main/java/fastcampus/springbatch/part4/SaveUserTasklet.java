package fastcampus.springbatch.part4;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import fastcampus.springbatch.part5.Orders;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SaveUserTasklet implements Tasklet{

	private final int SIZE = 100;
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
		
		for(int i = 0 ; i < SIZE ; i++) {
			users.add(
						User.builder()
							.orders(Collections.singletonList(Orders.builder()
										.amount(1_000)
										.createdDate(LocalDate.of(2021, 8, 1))
										.itemName("item_" + i)
									.build()))
//							.totalAmount(1_000)
							.username("test_user_" + i)
							.build()
					);
		}
		for(int i = 0 ; i < SIZE ; i++) {
			users.add(
					User.builder()
					.orders(Collections.singletonList(Orders.builder()
							.amount(200_000)
							.createdDate(LocalDate.of(2021, 9, 2))
							.itemName("item_" + i)
						.build()))
//					.totalAmount(200_000)
					.username("test_user_" + i)
					.build()
					);
		}
		for(int i = 0 ; i < SIZE ; i++) {
			users.add(
					User.builder()
					.orders(Collections.singletonList(Orders.builder()
							.amount(300_000)
							.createdDate(LocalDate.of(2021, 8, 3))
							.itemName("item_" + i)
						.build()))
//					.totalAmount(300_000)
					.username("test_user_" + i)
					.build()
					);
		}
		for(int i = 0 ; i < SIZE ; i++) {
			users.add(
					User.builder()
					.orders(Collections.singletonList(Orders.builder()
							.amount(500_000)
							.createdDate(LocalDate.of(2021, 8, 4))
							.itemName("item_" + i)
						.build()))
//					.totalAmount(500_000)
					.username("test_user_" + i)
					.build()
					);
		}
		return users;
	}
	
}










































