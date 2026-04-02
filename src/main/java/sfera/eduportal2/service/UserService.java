package sfera.eduportal2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.AuthRegister;
import sfera.eduportal2.Payload.request.ReqUser;
import sfera.eduportal2.Payload.response.Token;
import sfera.eduportal2.Repository.RoleRepository;
import sfera.eduportal2.Repository.UserRepository;
import sfera.eduportal2.Security.JwtProvider;
import sfera.eduportal2.entity.Users;
import sfera.eduportal2.entity.enums.Role;
import sfera.eduportal2.mapper.UserMapper;

@Service
@RequiredArgsConstructor
public class UserService {


    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtProvider jwtProvider;

    public ApiResponse getMe(Users user) {

        return ApiResponse.builder()
                .message("Success")
                .success(true)
                .status(HttpStatus.OK)
                .body(userMapper.resUser(user))
                .build();
    }

    public ApiResponse teacherSave(AuthRegister authRegister) {
        boolean exists = userRepository.existsByEmailAndRole_Role(authRegister.getEmail(), Role.ROLE_TEACHER);
        if (exists) {
            return ApiResponse.builder()
                    .message("Teacher already exists")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .body(null)
                    .build();
        }

        Users user = Users.builder()
                .fullName(authRegister.getFullName())
                .age(authRegister.getAge())
                .email(authRegister.getEmail())
                .phoneNumber(authRegister.getPhoneNumber())
                .password(passwordEncoder.encode(authRegister.getPassword()))
                .role(roleRepository.findByRole(Role.ROLE_TEACHER))
                .enabled(true)
                .code(0L)
                .level(null)
                .build();
        userRepository.save(user);
        return ApiResponse.builder()
                .message("Teacher successfully saved")
                .success(true)
                .status(HttpStatus.OK)
                .body(null)
                .build();
    }

    public ApiResponse updateUser(Users user, ReqUser  reqUser) {
        if (user.getRole().getRole().equals(Role.ROLE_ADMIN)){
            if (reqUser.getId() == null){
                boolean exists = userRepository.existsByEmailAndRole_RoleAndIdNot(user.getEmail(), Role.ROLE_ADMIN, reqUser.getId());
                if (exists) {
                    return ApiResponse.builder()
                            .message("This email already exists.")
                            .success(false)
                            .status(HttpStatus.BAD_REQUEST)
                            .body(null)
                            .build();
                }
                user.setFullName(reqUser.getFullName());
                user.setPhoneNumber(reqUser.getPhoneNumber());

                if (!reqUser.getEmail().equals(user.getEmail())){
                    user.setEmail(reqUser.getEmail());
                    Users save = userRepository.save(user);
                    String token = jwtProvider.generateToken(save.getEmail());
                    Token token1 = Token.builder().token(token).role(Role.ROLE_ADMIN.name()).build();
                    return ApiResponse.builder()
                            .message("Successfully updated ")
                            .success(true)
                            .status(HttpStatus.OK)
                            .body(token1)
                            .build();
                }
                userRepository.save(user);
                return ApiResponse.builder()
                        .message("Successfully updated ")
                        .success(true)
                        .status(HttpStatus.OK)
                        .body(null)
                        .build();

            }else {
                user = userRepository.findById(reqUser.getId()).orElse(null);
                if (user == null) {
                    return ApiResponse.builder()
                            .message("User not found.")
                            .success(false)
                            .status(HttpStatus.BAD_REQUEST)
                            .body(null)
                            .build();
                }

                user.setFullName(reqUser.getFullName());
                user.setPhoneNumber(reqUser.getPhoneNumber());
                user.setEmail(reqUser.getEmail());

                userRepository.save(user);
                return ApiResponse.builder()
                        .message("Success ")
                        .success(true)
                        .status(HttpStatus.OK)
                        .body(null)
                        .build();
        }
    }else {
            user.setFullName(reqUser.getFullName());
            user.setPhoneNumber(reqUser.getPhoneNumber());
            if (!reqUser.getEmail().equals(user.getEmail()) ) {
                user.setEmail(reqUser.getEmail());
                Users save = userRepository.save(user);
                String token = jwtProvider.generateToken(save.getEmail());
                Token token1 = Token.builder().token(token).role(user.getRole().getRole().name()).build();
                return ApiResponse.builder()
                        .message("Success")
                        .success(true)
                        .status(HttpStatus.OK)
                        .body(null)
                        .build();
}else {
                userRepository.save(user);
                return ApiResponse.builder()
                        .message("Success")
                        .success(true)
                        .status(HttpStatus.OK)
                        .body(null)
                        .build();
            }
        }
    }


}