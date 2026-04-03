package sfera.eduportal2.Payload.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestModule {
    private String title;
    private String content;
    private Long categoryId;

}
