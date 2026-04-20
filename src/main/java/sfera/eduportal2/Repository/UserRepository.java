package sfera.eduportal2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sfera.eduportal2.entity.Users;
import sfera.eduportal2.entity.enums.Role;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmail(String email);

    boolean existsByEmailAndRole_Role(String email, Role role);

    boolean existsByEmailAndRole_RoleAndIdNot(String email, Role role, Long id);

    List<Users> findByFullNameContainingIgnoreCaseAndEmailContainingIgnoreCaseAndPhoneNumberContaining(
            String name, String email, String phone);

    Optional<Users> findByCode(Long code);
}
