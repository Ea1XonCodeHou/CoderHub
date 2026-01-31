package com.eaxon.coderhubpojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教程用户行为实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorialUserAction implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String tutorialId;
    private String userId;
    private String actionType;
    private LocalDateTime createTime;
}
