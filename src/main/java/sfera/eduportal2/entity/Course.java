package sfera.eduportal2.entity;

import jakarta.persistence.ManyToOne;
import sfera.eduportal2.entity.Template.AbsEntity;

import java.util.Date;

public class Course extends AbsEntity {

    private String title;

    @ManyToOne
    private Category category;

    private String description;

    private Double price;

    private String level;

    private Date duration;

    private boolean active;
}
