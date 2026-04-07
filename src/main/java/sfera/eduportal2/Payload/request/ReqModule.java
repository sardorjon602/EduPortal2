package sfera.eduportal2.Payload.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqModule {
    private String title;
    private String content;
    private Long categoryId;

}
