package sfera.eduportal2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sfera.eduportal2.entity.Options;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptionsRepository extends JpaRepository<Options, Long> {
    boolean existsByQuestionsIdAndCorrectTrue(Long questionsId);
    boolean existsByQuestionsIdAndCorrectTrueAndIdNot(Long questionsId, Long id);
    List<Options> findAllByQuestionsIdIn(List<Long> questionIds);

}
