package sfera.eduportal2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sfera.eduportal2.entity.Test;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {

    boolean existsByTitleIgnoreCase(String title);

    boolean existsByTitleIgnoreCaseAndIdNot(String title, Long id);
}
