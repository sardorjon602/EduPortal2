package sfera.eduportal2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sfera.eduportal2.entity.Options;
import java.util.List;

@Repository
public interface OptionsRepository extends JpaRepository<Options, Long> {
    boolean existsByQuestionsIdAndIsCorrectTrue(Long questionsId);
    boolean existsByQuestionsIdAndIsCorrectTrueAndIdNot(Long questionsId, Long id);
    List<Options> findAllByQuestionsIdIn(List<Long> questionIds);
}