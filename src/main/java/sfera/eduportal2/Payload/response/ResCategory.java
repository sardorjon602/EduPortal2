package sfera.eduportal2.Payload.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResCategory {
    private Long id;
    private String name;
    private Integer questionCount;
}