package sfera.eduportal2.Payload.request;

import lombok.Data;

@Data
public class ReqStartTest {
    private Long userId;
    private Long moduleId;
    private Long testId;
}