package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.SeckillOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description 秒杀订单
 * @create 2026-07-21
 */
@Mapper
public interface ISeckillOrderDao {

    /**
     * 插入秒杀订单
     */
    void insert(SeckillOrder seckillOrder);

    /**
     * 根据外部交易单号查询秒杀订单
     */
    SeckillOrder querySeckillOrderByOutTradeNo(String outTradeNo);

    /**
     * 根据用户ID和活动ID查询秒杀订单（用于校验是否已参与）
     */
    SeckillOrder querySeckillOrderByUserAndActivity(String userId, Long activityId);

}
