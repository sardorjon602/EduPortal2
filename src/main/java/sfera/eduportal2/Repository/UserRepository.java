package sfera.eduportal2.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sfera.eduportal2.entity.Users;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users,Long> {

    Optional<Users> findByEmail(String email);
}
