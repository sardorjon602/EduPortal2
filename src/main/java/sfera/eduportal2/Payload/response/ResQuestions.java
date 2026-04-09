package sfera.eduportal2.Payload.response;
import lombok.Builder;
import lombok.Data;
import sfera.eduportal2.entity.enums.Type;

@Data
@Builder
public class ResQuestions {
    private Long id;
    private String text;
    private Type type;
    private Long moduleId;
    private String moduleName;
    // Agar variantlar (Options) bo'lsa, shu yerga List<ResOptions> qo'shasiz
}