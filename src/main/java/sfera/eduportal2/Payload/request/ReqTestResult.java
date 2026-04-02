package sfera.eduportal2.Payload.request;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqTestResult {

    private Long userId;

    private Long testId;

    private Double score;

    private Date takenAt;


}
