package sfera.eduportal2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.AuthLogin;
import sfera.eduportal2.Payload.request.AuthRegister;
import sfera.eduportal2.Payload.response.Token;
import sfera.eduportal2.Repository.RoleRepository;
import sfera.eduportal2.Repository.UserRepository;
import sfera.eduportal2.Security.JwtProvider;
import sfera.eduportal2.entity.Users;
import sfera.eduportal2.entity.enums.Role;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MailSender javaMailSender;
    private final JwtProvider jWTProvider;
    private final PasswordEncoder passwordEncoder;


    public ApiResponse register(AuthRegister authRegister) {
        boolean exists = userRepository.existsByEmailAndRole_Role(authRegister.getEmail(), Role.ROLE_USER);
        if (exists) {
            return new ApiResponse("This email has been used before.", HttpStatus.BAD_REQUEST, false,null);
        }
        long code = Math.round(Math.random() * 1000000);
        System.out.println(code);

        Users  user = Users.builder()
                .fullName(authRegister.getFullName())
                .age(authRegister.getAge())
                .email(authRegister.getEmail())
                .password(passwordEncoder.encode(authRegister.getPassword()))
                .phoneNumber(authRegister.getPhoneNumber())
                .level(authRegister.getLevel())
                .enabled(false)
                .role(roleRepository.findByRole(Role.ROLE_USER))
                .code(code)
                .build();
        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("email");
        message.setTo(authRegister.getEmail());
        message.setSubject("Verify your account");
        message.setText("Your code "+code+"\n Enter this code.");
        javaMailSender.send(message);

        return new ApiResponse("You have registered, now enter the code",HttpStatus.OK, true ,null);
    }

    public ApiResponse activateUser(Long code){
        Users users = userRepository.findByCode(code).orElse(null);
        if(users==null){
            return new ApiResponse("User not found",HttpStatus.NOT_FOUND, false,null);
        }
        users.setEnabled(true);
        users.setCode(null);
        userRepository.save(users);

        String token = jWTProvider.generateToken(users.getEmail());
        Token tokenObj = Token.builder()
                .token(token)
                .role(users.getRole().getRole().name())
                .build();
        return new ApiResponse("You have activated your account",HttpStatus.OK, true, tokenObj);
    }

    public ApiResponse login(AuthLogin authLogin){
        Users users = userRepository.findByEmail(authLogin.getEmail()).orElse(null);
        if(users==null){
            return new ApiResponse("User not found",HttpStatus.NOT_FOUND, false, null);
        }
        if (users.isEnabled()) {
            if (passwordEncoder.matches(authLogin.getPassword(), users.getPassword())) {

                String token = jWTProvider.generateToken(users.getEmail());
                Token tokenObj = Token.builder()
                        .token(token)
                        .role(users.getRole().getRole().name())
                        .build();
                return new ApiResponse("You have logged in",HttpStatus.OK, true, tokenObj);
            }
            return new ApiResponse("You have not logged in",HttpStatus.BAD_REQUEST, false, null);
        }
        return new ApiResponse("You are not active ",HttpStatus.BAD_REQUEST, false,     null);
    }
}
