package fastcampus.springbatch.part4;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class User {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String username;
	
	@Enumerated(EnumType.STRING)
	private Level level = Level.NORMAL;
	
	private int totalAmount;
	
	private LocalDate updatedDate;

	@Builder
	private User(String username, int totalAmount) {
		this.username = username;
		this.totalAmount = totalAmount;
	}
	
	@AllArgsConstructor
	public enum Level{
		VIP(500_000, null), 
		GOLD(500_000, VIP), 
		SILVER(300_000, GOLD),
		NORMAL(200_000, SILVER);
		
		private int nextAmount;
		private Level nextLevel;
	}
	
}














 


