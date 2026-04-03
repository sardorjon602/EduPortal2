package sfera.eduportal2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sfera.eduportal2.entity.Questions;

import java.util.Optional;

@Repository
public interface QuestionsRepository extends JpaRepository<Questions, Long> {

  Optional<Questions> findById(Long id);
}
