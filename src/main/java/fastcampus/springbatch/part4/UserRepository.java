package fastcampus.springbatch.part4;

import java.time.LocalDate;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>{

	Collection<User> findAllByUpdatedDate(LocalDate updatedDate);

}
