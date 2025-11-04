package com.eaxon.coderhubpojo.DTO;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class NewTutorialDTO implements Serializable {
    private String title;
    private String description;
    private String coverImage;
    private String categoryId;
    private Integer difficulty;
    private String instructorName;
    private String instructorAvatar;
    private String instructorDesc;
    private BigDecimal price;
}
