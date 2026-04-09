package sfera.eduportal2.Payload.request;
import lombok.Data;

@Data
public class ReqStartTest {
    private Long moduleId; 
    private Long userId; // Agar @CurrentUser ishlatsangiz, buni olib tashlashingiz ham mumkin
}