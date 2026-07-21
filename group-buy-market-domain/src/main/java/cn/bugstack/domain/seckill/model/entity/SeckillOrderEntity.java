package cn.bugstack.domain.seckill.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @description 秒杀订单实体
 * @create 2026-07-21
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeckillOrderEntity {

    /** 用户ID */
    private String userId;
    /** 活动ID */
    private Long activityId;
    /** 商品ID */
    private String goodsId;
    /** 订单ID */
    private String orderId;
    /** 渠道 */
    private String source;
    /** 来源 */
    private String channel;
    /** 原始价格 */
    private BigDecimal originalPrice;
    /** 秒杀价格 */
    private BigDecimal seckillPrice;
    /** 状态（0初始锁定、1消费完成、2用户退单） */
    private Integer status;
    /** 外部交易单号-确保外部调用唯一幂等 */
    private String outTradeNo;

}
