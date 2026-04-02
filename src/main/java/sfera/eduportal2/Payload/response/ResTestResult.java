package sfera.eduportal2.Payload.response;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResTestResult {

    private Long id;

    private String userName;

    private String testName;

    private Double score;

    private Date takenAt;



}
