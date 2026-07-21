package cn.bugstack.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description 秒杀活动查询应答对象
 * @create 2026-07-21
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeckillActivityResponseDTO {

    // 活动ID
    private Long activityId;
    // 商品ID
    private String goodsId;
    // 商品名称
    private String goodsName;
    // 原始价格
    private BigDecimal originalPrice;
    // 秒杀价格
    private BigDecimal seckillPrice;
    // 秒杀库存
    private Integer seckillStock;
    // 秒杀开始时间
    private Date seckillStartTime;
    // 秒杀结束时间
    private Date seckillEndTime;
    // 秒杀倒计时（秒）
    private Long seckillCountdown;

}
