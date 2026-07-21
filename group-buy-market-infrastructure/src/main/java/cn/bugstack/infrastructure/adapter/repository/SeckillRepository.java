package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.seckill.adapter.repository.ISeckillRepository;
import cn.bugstack.domain.seckill.model.entity.SeckillActivityEntity;
import cn.bugstack.domain.seckill.model.entity.SeckillOrderEntity;
import cn.bugstack.infrastructure.dao.ISeckillActivityDao;
import cn.bugstack.infrastructure.dao.ISeckillOrderDao;
import cn.bugstack.infrastructure.dao.po.SeckillActivity;
import cn.bugstack.infrastructure.dao.po.SeckillOrder;
import cn.bugstack.infrastructure.redis.IRedisService;
import org.redisson.api.RBitSet;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @description 秒杀仓储实现
 * @create 2026-07-21
 */
@Repository
public class SeckillRepository implements ISeckillRepository {

    /**
     * Redis 秒杀库存 key 前缀
     */
    private static final String SECKILL_STOCK_KEY = "seckill_stock_";

    /**
     * Lua 脚本：原子扣减库存
     * 返回 1-成功 0-库存不足 -1-key不存在
     */
    private static final String DEDUCTION_STOCK_SCRIPT =
            "if (redis.call('exists', KEYS[1]) == 1) then" +
            "    local stock = tonumber(redis.call('get', KEYS[1]));" +
            "    if (stock == -1) then return 1 end;" +
            "    if (stock > 0) then" +
            "        redis.call('decr', KEYS[1]);" +
            "        return 1;" +
            "    end;" +
            "    return 0;" +
            "end;" +
            "return -1;";

    /**
     * Lua 脚本：原子回补库存
     */
    private static final String RECOVERY_STOCK_SCRIPT =
            "if (redis.call('exists', KEYS[1]) == 1) then" +
            "    local stock = tonumber(redis.call('get', KEYS[1]));" +
            "    local maxStock = tonumber(ARGV[1]);" +
            "    if (stock < maxStock) then" +
            "        redis.call('incr', KEYS[1]);" +
            "    end;" +
            "end;";

    @Resource
    private ISeckillActivityDao seckillActivityDao;
    @Resource
    private ISeckillOrderDao seckillOrderDao;
    @Resource
    private IRedisService redisService;

    @Override
    public SeckillActivityEntity querySeckillActivityByActivityId(Long activityId) {
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityByActivityId(activityId);
        if (null == seckillActivity) return null;
        return SeckillActivityEntity.builder()
                .activityId(seckillActivity.getActivityId())
                .goodsId(seckillActivity.getGoodsId())
                .seckillStock(seckillActivity.getSeckillStock())
                .seckillRemainStock(seckillActivity.getSeckillRemainStock())
                .seckillPrice(seckillActivity.getSeckillPrice())
                .seckillStartTime(seckillActivity.getSeckillStartTime())
                .seckillEndTime(seckillActivity.getSeckillEndTime())
                .status(seckillActivity.getStatus())
                .tagId(seckillActivity.getTagId())
                .build();
    }

    @Override
    public List<SeckillActivityEntity> queryOnlineSeckillActivityList() {
        List<SeckillActivity> seckillActivities = seckillActivityDao.queryOnlineSeckillActivityList();
        if (null == seckillActivities || seckillActivities.isEmpty()) {
            return new ArrayList<>();
        }
        List<SeckillActivityEntity> result = new ArrayList<>();
        for (SeckillActivity seckillActivity : seckillActivities) {
            result.add(SeckillActivityEntity.builder()
                    .activityId(seckillActivity.getActivityId())
                    .goodsId(seckillActivity.getGoodsId())
                    .seckillStock(seckillActivity.getSeckillStock())
                    .seckillRemainStock(seckillActivity.getSeckillRemainStock())
                    .seckillPrice(seckillActivity.getSeckillPrice())
                    .seckillStartTime(seckillActivity.getSeckillStartTime())
                    .seckillEndTime(seckillActivity.getSeckillEndTime())
                    .status(seckillActivity.getStatus())
                    .tagId(seckillActivity.getTagId())
                    .build());
        }
        return result;
    }

    @Override
    public Long deductionStock(Long activityId) {
        String stockKey = SECKILL_STOCK_KEY + activityId;
        return redisService.evalScript(DEDUCTION_STOCK_SCRIPT, Arrays.asList(stockKey));
    }

    @Override
    public void recoveryStock(Long activityId) {
        String stockKey = SECKILL_STOCK_KEY + activityId;
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityByActivityId(activityId);
        if (null != seckillActivity) {
            redisService.evalScript(RECOVERY_STOCK_SCRIPT, Arrays.asList(stockKey), seckillActivity.getSeckillStock());
        }
    }

    @Override
    public void preloadSeckillStock(Long activityId, Integer stock, long expireSeconds) {
        String stockKey = SECKILL_STOCK_KEY + activityId;
        redisService.setAtomicLong(stockKey, stock);
    }

    @Override
    public Long getSeckillStockFromCache(Long activityId) {
        String stockKey = SECKILL_STOCK_KEY + activityId;
        return redisService.getAtomicLong(stockKey);
    }

    @Override
    public void writeSeckillOrder(SeckillOrderEntity seckillOrderEntity) {
        SeckillOrder seckillOrder = SeckillOrder.builder()
                .userId(seckillOrderEntity.getUserId())
                .activityId(seckillOrderEntity.getActivityId())
                .goodsId(seckillOrderEntity.getGoodsId())
                .orderId(seckillOrderEntity.getOrderId())
                .source(seckillOrderEntity.getSource())
                .channel(seckillOrderEntity.getChannel())
                .originalPrice(seckillOrderEntity.getOriginalPrice())
                .seckillPrice(seckillOrderEntity.getSeckillPrice())
                .status(seckillOrderEntity.getStatus())
                .outTradeNo(seckillOrderEntity.getOutTradeNo())
                .build();
        seckillOrderDao.insert(seckillOrder);
    }

    @Override
    public SeckillOrderEntity querySeckillOrderByOutTradeNo(String outTradeNo) {
        SeckillOrder seckillOrder = seckillOrderDao.querySeckillOrderByOutTradeNo(outTradeNo);
        if (null == seckillOrder) return null;
        return SeckillOrderEntity.builder()
                .userId(seckillOrder.getUserId())
                .activityId(seckillOrder.getActivityId())
                .goodsId(seckillOrder.getGoodsId())
                .orderId(seckillOrder.getOrderId())
                .source(seckillOrder.getSource())
                .channel(seckillOrder.getChannel())
                .originalPrice(seckillOrder.getOriginalPrice())
                .seckillPrice(seckillOrder.getSeckillPrice())
                .status(seckillOrder.getStatus())
                .outTradeNo(seckillOrder.getOutTradeNo())
                .build();
    }

    @Override
    public SeckillOrderEntity querySeckillOrderByUserAndActivity(String userId, Long activityId) {
        SeckillOrder seckillOrder = seckillOrderDao.querySeckillOrderByUserAndActivity(userId, activityId);
        if (null == seckillOrder) return null;
        return SeckillOrderEntity.builder()
                .userId(seckillOrder.getUserId())
                .activityId(seckillOrder.getActivityId())
                .goodsId(seckillOrder.getGoodsId())
                .orderId(seckillOrder.getOrderId())
                .source(seckillOrder.getSource())
                .channel(seckillOrder.getChannel())
                .originalPrice(seckillOrder.getOriginalPrice())
                .seckillPrice(seckillOrder.getSeckillPrice())
                .status(seckillOrder.getStatus())
                .outTradeNo(seckillOrder.getOutTradeNo())
                .build();
    }

    @Override
    public boolean isTagCrowdRange(String tagId, String userId) {
        RBitSet bitSet = redisService.getBitSet(tagId);
        if (!bitSet.isExists()) return true;
        return bitSet.get(redisService.getIndexFromUserId(userId));
    }

}
