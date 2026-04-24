package sfera.eduportal2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.AuthRegister;
import sfera.eduportal2.Payload.request.ReqUser;
import sfera.eduportal2.Payload.response.ResUser;
import sfera.eduportal2.Payload.response.Token;
import sfera.eduportal2.Repository.RoleRepository;
import sfera.eduportal2.Repository.UserRepository;
import sfera.eduportal2.Security.JwtProvider;
import sfera.eduportal2.entity.Users;
import sfera.eduportal2.entity.enums.Role;
import sfera.eduportal2.mapper.UserMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtProvider jwtProvider;
    private final JavaMailSender javaMailSender;

    public ApiResponse getMe(Users user) {
        return ApiResponse.builder()
                .message("Success")
                .success(true)
                .status(HttpStatus.OK)
                .body(userMapper.resUser(user))
                .build();
    }

    public ApiResponse teacherSave(AuthRegister authRegister) {
        boolean exists = userRepository.existsByEmailAndRole_Role(
                authRegister.getEmail(), Role.ROLE_TEACHER);
        if (exists) {
            return ApiResponse.builder()
                    .message("Teacher already exists")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
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
                .level(authRegister.getLevel())
                .build();
        userRepository.save(user);

        return ApiResponse.builder()
                .message("Teacher successfully saved")
                .success(true)
                .status(HttpStatus.OK)
                .build();
    }

    // ================================================================
    // 2. users/all — filtr qo'shildi
    // ================================================================
    public ApiResponse getAllUsers(String name, String email, String phone) {
        List<Users> users;

        boolean hasName = name != null && !name.isEmpty();
        boolean hasEmail = email != null && !email.isEmpty();
        boolean hasPhone = phone != null && !phone.isEmpty();

        if (hasName || hasEmail || hasPhone) {
            users = userRepository
                    .findByFullNameContainingIgnoreCaseAndEmailContainingIgnoreCaseAndPhoneNumberContaining(
                            hasName ? name : "",
                            hasEmail ? email : "",
                            hasPhone ? phone : ""
                    );
        } else {
            users = userRepository.findAll();
        }

        List<ResUser> resUsers = users.stream()
                .map(userMapper::resUser)
                .toList();

        return ApiResponse.builder()
                .message("Success")
                .success(true)
                .status(HttpStatus.OK)
                .body(resUsers)
                .build();
    }
    // ================================================================
    // 3. Admin userni update qilishi
    // ================================================================
    public ApiResponse updateUser(Users currentUser, ReqUser reqUser) {
        if (currentUser.getRole().getRole().equals(Role.ROLE_ADMIN)) {
            if (reqUser.getId() == null) {
                // Admin o'zini update qilmoqda
                boolean exists = userRepository.existsByEmailAndRole_RoleAndIdNot(
                        reqUser.getEmail(), Role.ROLE_ADMIN, currentUser.getId());
                if (exists) {
                    return ApiResponse.builder()
                            .message("This email already exists.")
                            .success(false)
                            .status(HttpStatus.BAD_REQUEST)
                            .build();
                }

                currentUser.setFullName(reqUser.getFullName());
                currentUser.setPhoneNumber(reqUser.getPhoneNumber());

                if (!reqUser.getEmail().equals(currentUser.getEmail())) {
                    currentUser.setEmail(reqUser.getEmail());
                    Users saved = userRepository.save(currentUser);
                    String token = jwtProvider.generateToken(saved.getEmail());
                    return ApiResponse.builder()
                            .message("Successfully updated")
                            .success(true)
                            .status(HttpStatus.OK)
                            .body(Token.builder()
                                    .token(token)
                                    .role(Role.ROLE_ADMIN.name())
                                    .build())
                            .build();
                }

                userRepository.save(currentUser);
                return ApiResponse.builder()
                        .message("Successfully updated")
                        .success(true)
                        .status(HttpStatus.OK)
                        .build();

            } else {
                // Admin boshqa userni update qilmoqda
                Users targetUser = userRepository.findById(reqUser.getId()).orElse(null);
                if (targetUser == null) {
                    return ApiResponse.builder()
                            .message("User not found.")
                            .success(false)
                            .status(HttpStatus.NOT_FOUND)
                            .build();
                }

                targetUser.setFullName(reqUser.getFullName());
                targetUser.setPhoneNumber(reqUser.getPhoneNumber());
                targetUser.setEmail(reqUser.getEmail());
                userRepository.save(targetUser);

                return ApiResponse.builder()
                        .message("User successfully updated")
                        .success(true)
                        .status(HttpStatus.OK)
                        .build();
            }
        } else {
            // Oddiy user o'zini update qilmoqda
            currentUser.setFullName(reqUser.getFullName());
            currentUser.setPhoneNumber(reqUser.getPhoneNumber());

            if (!reqUser.getEmail().equals(currentUser.getEmail())) {
                currentUser.setEmail(reqUser.getEmail());
                Users saved = userRepository.save(currentUser);
                String token = jwtProvider.generateToken(saved.getEmail());
                return ApiResponse.builder()
                        .message("Successfully updated")
                        .success(true)
                        .status(HttpStatus.OK)
                        .body(Token.builder()
                                .token(token)
                                .role(currentUser.getRole().getRole().name())
                                .build())
                        .build();
            }

            userRepository.save(currentUser);
            return ApiResponse.builder()
                    .message("Successfully updated")
                    .success(true)
                    .status(HttpStatus.OK)
                    .build();
        }
    }

    public ApiResponse deleteUser(Long id) {
        Users users = userRepository.findById(id).orElse(null);
        if (users == null) {
            return ApiResponse.builder()
                    .message("User not found")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
        userRepository.delete(users);
        return ApiResponse.builder()
                .message("Successfully deleted")
                .success(true)
                .status(HttpStatus.OK)
                .build();
    }

    // ================================================================
    // 4. Forgot password
    // ================================================================
//    public ApiResponse forgotPassword(String email) {
//        Users user = userRepository.findByEmail(email).orElse(null);
//        if (user == null) {
//            return ApiResponse.builder()
//                    .message("Bu email tizimda topilmadi")
//                    .success(false)
//                    .status(HttpStatus.NOT_FOUND)
//                    .build();
//        }
//
//        long code = Math.round(Math.random() * 1000000);
//        user.setCode(code);
//        userRepository.save(user);
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(email);
//        message.setSubject("Parolni tiklash kodi");
//        message.setText("Parolni tiklash uchun kod: " + code);
//        javaMailSender.send(message);
//
//        return ApiResponse.builder()
//                .message("Emailingizga kod yuborildi")
//                .success(true)
//                .status(HttpStatus.OK)
//                .build();
//    }


    // ================================================================
    // 4. Forgot password
    // ================================================================
    public ApiResponse forgotPassword(String email) {
        Users user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ApiResponse.builder()
                    .message("Bu email tizimda topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        long code = Math.round(Math.random() * 1000000);
        user.setCode(code);
        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();

        // MANA SHU QATOR QO'SHILDI:
        message.setFrom("haqqulovsardor51@gmail.com");

        message.setTo(email);
        message.setSubject("Parolni tiklash kodi");
        message.setText("Parolni tiklash uchun kod: " + code);
        javaMailSender.send(message);

        return ApiResponse.builder()
                .message("Emailingizga kod yuborildi")
                .success(true)
                .status(HttpStatus.OK)
                .build();
    }



    public ApiResponse resetPassword(Long code, String newPassword) {
        Users user = userRepository.findByCode(code).orElse(null);
        if (user == null) {
            return ApiResponse.builder()
                    .message("Kod noto'g'ri yoki muddati o'tgan")
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setCode(null);
        userRepository.save(user);

        return ApiResponse.builder()
                .message("Parol muvaffaqiyatli yangilandi")
                .success(true)
                .status(HttpStatus.OK)
                .build();
    }

}