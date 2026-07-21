package cn.bugstack.api.dto;

import lombok.Data;

/**
 * @description 秒杀活动查询请求对象
 * @create 2026-07-21
 */
@Data
public class SeckillActivityRequestDTO {

    // 用户ID
    private String userId;
    // 活动ID（可选，为空则查询列表）
    private Long activityId;
    // 渠道
    private String source;
    // 来源
    private String channel;

}
