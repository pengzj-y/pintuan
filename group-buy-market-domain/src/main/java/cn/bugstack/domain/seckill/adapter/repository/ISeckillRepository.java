package cn.bugstack.domain.seckill.adapter.repository;

import cn.bugstack.domain.seckill.model.entity.SeckillActivityEntity;
import cn.bugstack.domain.seckill.model.entity.SeckillOrderEntity;

import java.util.List;

/**
 * @description 秒杀仓储
 * @create 2026-07-21
 */
public interface ISeckillRepository {

    /**
     * 根据活动ID查询秒杀活动
     */
    SeckillActivityEntity querySeckillActivityByActivityId(Long activityId);

    /**
     * 查询生效中的秒杀活动列表
     */
    List<SeckillActivityEntity> queryOnlineSeckillActivityList();

    /**
     * 扣减秒杀库存（Redis 原子操作）
     *
     * @param activityId 活动ID
     * @return 1-成功 0-库存不足 -1-活动不存在或未开始
     */
    Long deductionStock(Long activityId);

    /**
     * 回补秒杀库存（Redis 原子操作）
     *
     * @param activityId 活动ID
     */
    void recoveryStock(Long activityId);

    /**
     * 预热秒杀库存到 Redis
     *
     * @param activityId 活动ID
     * @param stock      库存数量
     * @param expireSeconds 过期时间（秒）
     */
    void preloadSeckillStock(Long activityId, Integer stock, long expireSeconds);

    /**
     * 查询 Redis 中秒杀库存
     */
    Long getSeckillStockFromCache(Long activityId);

    /**
     * 写入秒杀订单
     */
    void writeSeckillOrder(SeckillOrderEntity seckillOrderEntity);

    /**
     * 根据外部交易单号查询秒杀订单
     */
    SeckillOrderEntity querySeckillOrderByOutTradeNo(String outTradeNo);

    /**
     * 根据用户ID和活动ID查询秒杀订单
     */
    SeckillOrderEntity querySeckillOrderByUserAndActivity(String userId, Long activityId);

    /**
     * 判断用户是否在人群标签范围内
     */
    boolean isTagCrowdRange(String tagId, String userId);

}
