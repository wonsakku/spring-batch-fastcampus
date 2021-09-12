package fastcampus.springbatch.part4;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import fastcampus.springbatch.part5.Orders;
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
	
//	private int totalAmount;
	
	@OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id")
	private List<Orders> orders;
	
	private LocalDate updatedDate;

	@Builder
	private User(String username, List<Orders> orders) {
		this.username = username;
		this.orders = orders;
	}

	public boolean availableLevelUp() {
		return Level.availableLevelUp(this.getLevel(), this.getTotalAmount());
	}

	private int getTotalAmount() {
		return this.orders.stream()
				.mapToInt(Orders::getAmount)
				.sum();
	}
	
	
	public Level levelUp() {
		Level nextLevel = Level.getNextlevel(this.getTotalAmount());
		this.level = nextLevel;
		this.updatedDate = LocalDate.now();
		
		return nextLevel;
	}
	
	
	@AllArgsConstructor
	public enum Level{
		VIP(500_000, null), 
		GOLD(500_000, VIP), 
		SILVER(300_000, GOLD),
		NORMAL(200_000, SILVER);
		
		private static boolean availableLevelUp(Level level, int totalAmount) {
			if(Objects.isNull(level)) {
				return false;
			}
			
			if(Objects.isNull(level.nextLevel)) {
				return false;
			}
			return totalAmount >= level.nextAmount;
		}
		
		private static Level getNextlevel(int totalAmount) {
			if(totalAmount >= Level.VIP.nextAmount) {
				return VIP;
			}
			if(totalAmount >= Level.GOLD.nextAmount) {
				return GOLD.nextLevel;
			}
			if(totalAmount >= Level.SILVER.nextAmount) {
				return SILVER.nextLevel;
			}
			if(totalAmount >= Level.NORMAL.nextAmount) {
				return NORMAL.nextLevel;
			}
			
			return NORMAL;
		}

		private int nextAmount;
		private Level nextLevel;
	}


	
}














 


