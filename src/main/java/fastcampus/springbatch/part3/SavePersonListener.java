package fastcampus.springbatch.part3;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.batch.core.annotation.BeforeStep;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SavePersonListener {

	
	public static class SavePersonStepExecutionListener{
		
		@BeforeStep
		public void beforeStep(StepExecution stepExecution) {
			log.info("beforeStep");
		}
		
		@AfterStep
		public ExitStatus afterStep(StepExecution stepExecution) {
			log.info("afterStep : {}", stepExecution.getWriteCount());
			
			if(stepExecution.getWriteCount() == 0) {
				return ExitStatus.FAILED;
			}
			
			return stepExecution.getExitStatus();
		}
		
	}
	
	public static class SavePersonJobExecutionListener implements JobExecutionListener{

		@Override
		public void beforeJob(JobExecution jobExecution) {
			log.info("beforeJob");
		}

		@Override
		public void afterJob(JobExecution jobExecution) {
			int sum = jobExecution.getStepExecutions()
					.stream()
					.mapToInt(StepExecution::getWriteCount)
					.sum();
			
			log.info("afterJob : {}", sum);
		}
	}
	
	
	public static class SavePersonAnnotationExecution {
		
		@BeforeJob
		public void annotationBeforeJob(JobExecution jobExecution) {
			log.info("annotationBeforeJob");
		}

		@AfterJob
		public void annotationAfterJob(JobExecution jobExecution) {
			int sum = jobExecution.getStepExecutions()
					.stream()
					.mapToInt(StepExecution::getWriteCount)
					.sum();
			
			log.info("annotationAfterJob : {}", sum);
		}
	}
	
}


























