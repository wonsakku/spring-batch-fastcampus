package fastcampus.springbatch.part4;

import java.time.LocalDate;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long>{

	Collection<User> findAllByUpdatedDate(LocalDate updatedDate);

	@Query(value = "SELECT MIN(u.id) FROM User u")
	long findMinId();

	@Query(value = "SELECT MAX(u.id) FROM User u")
	long findMaxId();

}
