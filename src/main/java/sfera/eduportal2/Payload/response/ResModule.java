package sfera.eduportal2.Payload.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResModule {
    private Long id;

    private String moduleName;

    private String categoryName;
}
