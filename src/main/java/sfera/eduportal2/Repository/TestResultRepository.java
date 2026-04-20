package sfera.eduportal2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sfera.eduportal2.entity.TestResult;
import sfera.eduportal2.entity.Users;
import java.util.List;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findByUsersOrderByCreatedAtDesc(Users users);

    List<TestResult> findTop5ByUsersOrderByCreatedAtDesc(Users user);
    @Query("SELECT t FROM TestResult t WHERE " +
            "(:username IS NULL OR LOWER(t.users.fullName) LIKE LOWER(CONCAT('%', :username, '%')))")
    List<TestResult> findAllByUsernameFilter(@Param("username") String username);
}