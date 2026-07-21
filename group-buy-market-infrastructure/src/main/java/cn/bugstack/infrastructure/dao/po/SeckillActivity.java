package cn.bugstack.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description 秒杀活动
 * @create 2026-07-21
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeckillActivity {

    /** 自增ID */
    private Long id;
    /** 活动ID */
    private Long activityId;
    /** 商品ID */
    private String goodsId;
    /** 秒杀库存 */
    private Integer seckillStock;
    /** 秒杀剩余库存 */
    private Integer seckillRemainStock;
    /** 秒杀价格 */
    private BigDecimal seckillPrice;
    /** 秒杀开始时间 */
    private Date seckillStartTime;
    /** 秒杀结束时间 */
    private Date seckillEndTime;
    /** 状态（0创建、1生效、2过期、3废弃） */
    private Integer status;
    /** 人群标签规则标识（为空则不限制） */
    private String tagId;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

}
