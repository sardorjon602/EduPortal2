package sfera.eduportal2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sfera.eduportal2.entity.Category;
import sfera.eduportal2.entity.Module;

import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module,Long> {

    boolean existsByModuleNameIgnoreCaseAndIdNot(String moduleName, Long id);

    boolean existsByModuleNameIgnoreCase(String name);
}
