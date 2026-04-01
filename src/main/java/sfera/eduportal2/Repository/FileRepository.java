package sfera.eduportal2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sfera.eduportal2.entity.File;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
}
