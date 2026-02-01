package com.eaxon.coderhubpojo.DTO;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能操作推荐DTO
 * AI根据用户行为推荐的智能辅助操作
 */
@Data
@AllArgsConstructor
public class SmartActionRecommendation {
    
    /** 文章ID */
    private String articleId;
    
    /** 推荐的操作列表 */
    private List<Action> actions;
    
    /** 推荐原因 */
    private String reason;
    
    public SmartActionRecommendation() {
        this.actions = new ArrayList<>();
    }
    
    /**
     * 添加推荐操作
     */
    public void addAction(Action action) {
        if (this.actions == null) {
            this.actions = new ArrayList<>();
        }
        this.actions.add(action);
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Action {
        /** 操作ID */
        private String actionId;
        
        /** 操作标签 */
        private String label;
        
        /** 操作描述 */
        private String description;
        
        /** 操作图标 */
        private String icon;
        
        /** 优先级（1-5，5最高） */
        private Integer priority;
        
        public Action(String actionId, String label, String description) {
            this.actionId = actionId;
            this.label = label;
            this.description = description;
            this.priority = 3; // 默认优先级
        }
    }
}
