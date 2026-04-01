package sfera.eduportal2.component;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import sfera.eduportal2.Repository.RoleRepository;
import sfera.eduportal2.Repository.UserRepository;
import sfera.eduportal2.entity.Roles;
import sfera.eduportal2.entity.Users;
import sfera.eduportal2.entity.enums.Role;

@Component
@RequiredArgsConstructor

public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddl;



    @Override
    public void run(String... args) throws Exception {
        if (ddl.equals("create") || ddl.equals("create-drop")) {
            Roles admin = Roles.builder()
                    .role(Role.ROLE_ADMIN)
                    .build();
            roleRepository.save(admin);

            Roles teacher = Roles.builder()
                    .role(Role.ROLE_TEACHER)
                    .build();
            roleRepository.save(teacher);


            Roles user = Roles.builder()
                    .role(Role.ROLE_USER)
                    .build();
            roleRepository.save(user);


            Users users = Users.builder()
                    .fullName("Admin admin")
                    .email("admin@gmail.com")
                    .age(20)
                    .level("HARD")
                    .password(passwordEncoder.encode("admin123"))
                    .phoneNumber("+998914676507")
                    .enabled(true)
                    .role(admin)
                    .build();
            userRepository.save(users);
        }
    }
}
