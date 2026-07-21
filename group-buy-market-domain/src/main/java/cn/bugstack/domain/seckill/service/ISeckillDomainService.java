package cn.bugstack.domain.seckill.service;

import cn.bugstack.domain.seckill.model.entity.SeckillActivityEntity;
import cn.bugstack.domain.seckill.model.entity.SeckillOrderEntity;

import java.util.List;

/**
 * @description 秒杀领域服务接口
 * @create 2026-07-21
 */
public interface ISeckillDomainService {

    /**
     * 查询生效中的秒杀活动列表
     */
    List<SeckillActivityEntity> queryOnlineSeckillActivityList();

    /**
     * 查询秒杀活动详情
     *
     * @param activityId 活动ID
     * @param userId     用户ID（用于人群标签过滤）
     * @return 秒杀活动信息，如果用户不在人群范围内返回 null
     */
    SeckillActivityEntity querySeckillActivity(Long activityId, String userId);

    /**
     * 执行秒杀
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     * @param source     渠道
     * @param channel    来源
     * @param outTradeNo 外部交易单号
     * @return 秒杀订单
     */
    SeckillOrderEntity doSeckill(Long activityId, String userId, String source, String channel, String outTradeNo);

}
