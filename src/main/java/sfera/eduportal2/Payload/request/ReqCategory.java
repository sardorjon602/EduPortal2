package sfera.eduportal2.Payload.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqCategory {
    private String name;
    private Integer questionCount;
}