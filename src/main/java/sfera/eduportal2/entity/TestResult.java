package sfera.eduportal2.entity;

import jakarta.persistence.*;
import lombok.*;
import sfera.eduportal2.entity.Template.AbsEntity;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class TestResult extends AbsEntity {

    @ManyToOne
    private Users users;

    @ManyToOne
    private TestSession testSession;

    private Date taskData;

    private LocalDateTime takenAt;

    private int correctCount;
    private int totalCount;
    private double scorePercent;
    private Double score;

    @Column(columnDefinition = "TEXT")
    private String aiRecommendation;  // AI tavsiyasi

    private String recommendedModule; // AI tavsiya qilgan modul
}