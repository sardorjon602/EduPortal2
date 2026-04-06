package sfera.eduportal2.Payload.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqRecommendation {

    private Long userId;

    private String moduleName;

    private String reason;


}
