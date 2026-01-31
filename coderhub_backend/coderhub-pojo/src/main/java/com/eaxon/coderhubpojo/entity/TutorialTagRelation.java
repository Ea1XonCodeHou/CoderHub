package com.eaxon.coderhubpojo.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教程标签关系实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorialTagRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String tutorialId;
    private String tagId;
}
