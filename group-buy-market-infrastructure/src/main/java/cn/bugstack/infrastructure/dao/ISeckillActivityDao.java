package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.SeckillActivity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @description 秒杀活动
 * @create 2026-07-21
 */
@Mapper
public interface ISeckillActivityDao {

    /**
     * 根据活动ID查询秒杀活动
     */
    SeckillActivity querySeckillActivityByActivityId(Long activityId);

    /**
     * 查询生效中的秒杀活动列表
     */
    List<SeckillActivity> queryOnlineSeckillActivityList();

    /**
     * 扣减秒杀库存
     *
     * @param activityId 活动ID
     * @return 影响行数
     */
    int deductionStock(Long activityId);

    /**
     * 回补秒杀库存
     *
     * @param activityId 活动ID
     * @return 影响行数
     */
    int recoveryStock(Long activityId);

}
