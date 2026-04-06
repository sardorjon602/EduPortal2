package sfera.eduportal2.Payload.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResRecommendation {

    private Long id;

    private Long userId;

    private String moduleName;

    private String reason;

}
