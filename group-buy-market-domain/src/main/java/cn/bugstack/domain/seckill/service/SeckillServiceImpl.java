package cn.bugstack.domain.seckill.service;

import cn.bugstack.domain.seckill.adapter.repository.ISeckillRepository;
import cn.bugstack.domain.seckill.model.entity.SeckillActivityEntity;
import cn.bugstack.domain.seckill.model.entity.SeckillOrderEntity;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description 秒杀领域服务实现
 * @create 2026-07-21
 */
@Slf4j
@Service
public class SeckillServiceImpl implements ISeckillDomainService {

    @Resource
    private ISeckillRepository seckillRepository;

    @Override
    public List<SeckillActivityEntity> queryOnlineSeckillActivityList() {
        log.info("查询生效中的秒杀活动列表");
        return seckillRepository.queryOnlineSeckillActivityList();
    }

    @Override
    public SeckillActivityEntity querySeckillActivity(Long activityId, String userId) {
        log.info("查询秒杀活动详情:{} userId:{}", activityId, userId);

        SeckillActivityEntity seckillActivity = seckillRepository.querySeckillActivityByActivityId(activityId);
        if (null == seckillActivity) {
            log.info("秒杀活动不存在或未生效:{}", activityId);
            return null;
        }

        // 人群标签过滤：如果配置了 tagId，则校验用户是否在人群范围内
        String tagId = seckillActivity.getTagId();
        if (StringUtils.isNotBlank(tagId)) {
            boolean isWithin = seckillRepository.isTagCrowdRange(tagId, userId);
            if (!isWithin) {
                log.info("用户不在秒杀活动人群范围内:{} userId:{} tagId:{}", activityId, userId, tagId);
                return null;
            }
        }

        return seckillActivity;
    }

    @Override
    public SeckillOrderEntity doSeckill(Long activityId, String userId, String source, String channel, String outTradeNo) {
        log.info("执行秒杀:{} userId:{}", activityId, userId);

        // 1. 查询秒杀活动
        SeckillActivityEntity seckillActivity = seckillRepository.querySeckillActivityByActivityId(activityId);
        if (null == seckillActivity) {
            throw new AppException(ResponseCode.E0009);
        }

        // 2. 人群标签过滤
        String tagId = seckillActivity.getTagId();
        if (StringUtils.isNotBlank(tagId)) {
            boolean isWithin = seckillRepository.isTagCrowdRange(tagId, userId);
            if (!isWithin) {
                log.info("用户不在秒杀活动人群范围内:{} userId:{} tagId:{}", activityId, userId, tagId);
                throw new AppException(ResponseCode.E0010);
            }
        }

        // 3. 幂等性检查：根据 outTradeNo 查询是否已有订单
        SeckillOrderEntity existOrder = seckillRepository.querySeckillOrderByOutTradeNo(outTradeNo);
        if (null != existOrder) {
            log.info("秒杀订单已存在:{} outTradeNo:{}", activityId, outTradeNo);
            return existOrder;
        }

        // 4. 检查用户是否已参与过该秒杀活动
        SeckillOrderEntity userOrder = seckillRepository.querySeckillOrderByUserAndActivity(userId, activityId);
        if (null != userOrder) {
            log.info("用户已参与过该秒杀活动:{} userId:{}", activityId, userId);
            throw new AppException(ResponseCode.E0011);
        }

        // 5. Redis 原子扣减库存（Lua 脚本保证原子性）
        Long stockResult = seckillRepository.deductionStock(activityId);
        if (null == stockResult || stockResult == 0) {
            log.info("秒杀库存不足:{}", activityId);
            throw new AppException(ResponseCode.E0012);
        }

        try {
            // 6. 生成订单ID，写入秒杀订单
            String orderId = RandomStringUtils.randomNumeric(12);
            SeckillOrderEntity seckillOrderEntity = SeckillOrderEntity.builder()
                    .userId(userId)
                    .activityId(activityId)
                    .goodsId(seckillActivity.getGoodsId())
                    .orderId(orderId)
                    .source(source)
                    .channel(channel)
                    .originalPrice(seckillActivity.getSeckillPrice())
                    .seckillPrice(seckillActivity.getSeckillPrice())
                    .status(0)
                    .outTradeNo(outTradeNo)
                    .build();

            seckillRepository.writeSeckillOrder(seckillOrderEntity);

            log.info("秒杀成功:{} userId:{} orderId:{}", activityId, userId, orderId);
            return seckillOrderEntity;
        } catch (Exception e) {
            // 写入订单失败，回补 Redis 库存
            seckillRepository.recoveryStock(activityId);
            log.error("秒杀订单写入失败，回补库存:{} userId:{}", activityId, userId, e);
            throw e;
        }
    }

}
