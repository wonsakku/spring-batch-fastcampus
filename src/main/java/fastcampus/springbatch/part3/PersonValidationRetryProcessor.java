package fastcampus.springbatch.part3;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;

public class PersonValidationRetryProcessor implements ItemProcessor<Person, Person>{

	private final RetryTemplate retryTemplate;

	
	public PersonValidationRetryProcessor() {
		this.retryTemplate = new RetryTemplateBuilder()
				.maxAttempts(3)
				.retryOn(NotFoundNameException.class)
				.build();
	}



	@Override
	public Person process(Person item) throws Exception {
		return this.retryTemplate.execute(context -> {
			// RetryCallback
			
			if(item.isNotEmptyName()) {
				return item;
			}
			
			throw new NotFoundNameException();
			
		}, context -> {
			// RecoveryCallback
			return item.unknownName();
		});
	}
}
























