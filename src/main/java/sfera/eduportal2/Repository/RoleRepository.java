package sfera.eduportal2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sfera.eduportal2.entity.Roles;
import sfera.eduportal2.entity.enums.Role;

@Repository
public interface RoleRepository extends JpaRepository<Roles, Integer> {
    Roles findByRole(Role role);
}
