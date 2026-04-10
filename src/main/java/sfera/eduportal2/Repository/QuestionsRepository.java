package sfera.eduportal2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sfera.eduportal2.entity.Questions;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionsRepository extends JpaRepository<Questions, Long> {

    Optional<Questions> findById(Long id);

    @Query("SELECT q FROM Questions q WHERE q.module.category.id = :categoryId")
    List<Questions> findByModuleCategoryId(@Param("categoryId") Long categoryId);


}
