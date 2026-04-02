package sfera.eduportal2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sfera.eduportal2.entity.TestResult;

@Repository

public interface TestResultRepository extends JpaRepository<TestResult, Long> {


}
