package cn.bugstack.trigger.job;

import cn.bugstack.domain.seckill.adapter.repository.ISeckillRepository;
import cn.bugstack.domain.seckill.model.entity.SeckillActivityEntity;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @description 秒杀库存预热定时任务；将生效中的秒杀活动库存加载到 Redis
 * @create 2026-07-21
 */
@Slf4j
@Service
public class SeckillStockPreloadJob {

    @Resource
    private ISeckillRepository seckillRepository;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 每5分钟执行一次，预热秒杀库存到 Redis
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void exec() {
        RLock lock = redissonClient.getLock("seckill_stock_preload_job_exec");
        try {
            boolean isLocked = lock.tryLock(3, 0, TimeUnit.SECONDS);
            if (!isLocked) return;

            log.info("秒杀库存预热任务开始");

            // 查询所有生效中的秒杀活动
            List<SeckillActivityEntity> activityList = seckillRepository.queryOnlineSeckillActivityList();
            if (null == activityList || activityList.isEmpty()) {
                log.info("秒杀库存预热任务，无生效中的秒杀活动");
                return;
            }

            // 将库存预热到 Redis，过期时间为活动结束时间 + 1小时
            for (SeckillActivityEntity activity : activityList) {
                long expireSeconds = (activity.getSeckillEndTime().getTime() - System.currentTimeMillis()) / 1000 + 3600;
                if (expireSeconds <= 0) continue;
                seckillRepository.preloadSeckillStock(activity.getActivityId(), activity.getSeckillRemainStock(), expireSeconds);
                log.info("秒杀库存预热:{} stock:{}", activity.getActivityId(), activity.getSeckillRemainStock());
            }

            log.info("秒杀库存预热任务完成，共处理{}个活动", activityList.size());
        } catch (Exception e) {
            log.error("秒杀库存预热任务失败", e);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
