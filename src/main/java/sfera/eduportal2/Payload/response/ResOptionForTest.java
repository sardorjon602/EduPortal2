package sfera.eduportal2.Payload.response;

import lombok.*;

@Data @Builder
@AllArgsConstructor @NoArgsConstructor
public class ResOptionForTest {
    private Long id;
    private String text;
    // isCorrect YO'Q — frontend ko'rmasin!
}
