package sfera.eduportal2.mapper;

import org.springframework.stereotype.Component;
import sfera.eduportal2.entity.Users;

@Component

public class UserMapper {

    public ResUser resUser(Users user ){
        return ResUser.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .imageUrl(user.getImage())
                .role(user.getRole() != null ? user.getRole().getRole().name() : null)
                .build();
    }


}
