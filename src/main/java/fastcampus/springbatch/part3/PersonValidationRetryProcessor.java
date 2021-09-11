package fastcampus.springbatch.part3;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PersonValidationRetryProcessor implements ItemProcessor<Person, Person>{

	private final RetryTemplate retryTemplate;

	
	public PersonValidationRetryProcessor() {
		this.retryTemplate = new RetryTemplateBuilder()
				.maxAttempts(3)
				.retryOn(NotFoundNameException.class)
				.withListener(new SavePersonRetryListener())
				.build();
	}



	@Override
	public Person process(Person item) throws Exception {
		return this.retryTemplate.execute(context -> {
			// RetryCallback
			
			if(item.isNotEmptyName()) {
				return item;
			}
			
			
			log.info("RetryCallback");
			throw new NotFoundNameException();
		}, context -> {
			// RecoveryCallback
			log.info("RetryCallback");
			return item.unknownName();
		});
	}
	
	//org.springframework.retry.RetryListener
	public static class SavePersonRetryListener implements RetryListener{

		@Override // retry를 시작하는 설정; return true여야 retry가 적용
		public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
			return true;
		}

		@Override // retry 종료 후에 호출. 
		public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback,
				Throwable throwable) {
			log.info("close");
		}

		@Override // retry Template에 정의한 exception(NotFoundNameException)이 발생했을 때 호출.
		public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
				Throwable throwable) {
			log.info("onError");
		}
		
	}
	
}
























