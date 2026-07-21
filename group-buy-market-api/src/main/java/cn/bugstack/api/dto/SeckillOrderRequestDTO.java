package cn.bugstack.api.dto;

import lombok.Data;

/**
 * @description 秒杀下单请求对象
 * @create 2026-07-21
 */
@Data
public class SeckillOrderRequestDTO {

    // 用户ID
    private String userId;
    // 活动ID
    private Long activityId;
    // 渠道
    private String source;
    // 来源
    private String channel;
    // 外部交易单号
    private String outTradeNo;

}
