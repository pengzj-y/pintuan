package cn.bugstack.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @description 秒杀下单应答对象
 * @create 2026-07-21
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeckillOrderResponseDTO {

    // 订单ID
    private String orderId;
    // 活动ID
    private Long activityId;
    // 商品ID
    private String goodsId;
    // 原始价格
    private BigDecimal originalPrice;
    // 秒杀价格
    private BigDecimal seckillPrice;
    // 订单状态
    private Integer status;

}
