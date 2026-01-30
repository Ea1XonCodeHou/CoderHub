package com.eaxon.coderhubpojo.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户项目列表展示VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProjectVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String projectName;
    private String shortDescription;
    private String coverImage;
    private String gitUrl;
    private String demoUrl;
    private Integer viewCount;
    private Integer isOpenSource;
    private LocalDateTime createTime;
    private String categoryName;
    private List<String> techStacks;
}

