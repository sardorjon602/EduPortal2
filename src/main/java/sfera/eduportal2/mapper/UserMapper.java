package sfera.eduportal2.mapper;

import org.springframework.stereotype.Component;
import sfera.eduportal2.Payload.response.ResUser;
import sfera.eduportal2.entity.Users;

@Component

public class    UserMapper {

    public ResUser resUser(Users user ){
        return ResUser.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .age(user.getAge())
                .level(user.getLevel())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole() != null ? user.getRole().getRole().name() : null)
                .build();
    }


}
