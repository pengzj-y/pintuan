package cn.bugstack.trigger.http;

import cn.bugstack.api.ISeckillService;
import cn.bugstack.api.dto.SeckillActivityRequestDTO;
import cn.bugstack.api.dto.SeckillActivityResponseDTO;
import cn.bugstack.api.dto.SeckillOrderRequestDTO;
import cn.bugstack.api.dto.SeckillOrderResponseDTO;
import cn.bugstack.api.response.Response;
import cn.bugstack.domain.activity.adapter.repository.IActivityRepository;
import cn.bugstack.domain.activity.model.valobj.SkuVO;
import cn.bugstack.domain.seckill.model.entity.SeckillActivityEntity;
import cn.bugstack.domain.seckill.model.entity.SeckillOrderEntity;
import cn.bugstack.domain.seckill.service.ISeckillDomainService;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @description 秒杀服务
 * @create 2026-07-21
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/seckill/")
public class SeckillController implements ISeckillService {

    @Resource
    private ISeckillDomainService seckillDomainService;
    @Resource
    private IActivityRepository activityRepository;

    @RequestMapping(value = "query_seckill_activity", method = RequestMethod.POST)
    @Override
    public Response<List<SeckillActivityResponseDTO>> querySeckillActivity(@RequestBody SeckillActivityRequestDTO requestDTO) {
        try {
            log.info("查询秒杀活动:{}", JSON.toJSONString(requestDTO));

            if (StringUtils.isBlank(requestDTO.getUserId())) {
                return Response.<List<SeckillActivityResponseDTO>>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            List<SeckillActivityEntity> activityEntities;

            // 如果指定了 activityId，查询单个活动详情
            if (null != requestDTO.getActivityId()) {
                SeckillActivityEntity activity = seckillDomainService.querySeckillActivity(requestDTO.getActivityId(), requestDTO.getUserId());
                activityEntities = new ArrayList<>();
                if (null != activity) {
                    activityEntities.add(activity);
                }
            } else {
                // 查询所有生效中的秒杀活动，并过滤掉用户不在人群范围内的
                List<SeckillActivityEntity> allActivities = seckillDomainService.queryOnlineSeckillActivityList();
                activityEntities = new ArrayList<>();
                for (SeckillActivityEntity activity : allActivities) {
                    SeckillActivityEntity filtered = seckillDomainService.querySeckillActivity(activity.getActivityId(), requestDTO.getUserId());
                    if (null != filtered) {
                        activityEntities.add(filtered);
                    }
                }
            }

            // 转换为响应 DTO
            List<SeckillActivityResponseDTO> responseList = new ArrayList<>();
            for (SeckillActivityEntity activity : activityEntities) {
                // 查询商品信息
                SkuVO skuVO = activityRepository.querySkuByGoodsId(activity.getGoodsId());
                String goodsName = null != skuVO ? skuVO.getGoodsName() : "未知商品";
                BigDecimal originalPrice = null != skuVO ? skuVO.getOriginalPrice() : activity.getSeckillPrice();

                // 计算倒计时（秒）
                long countdown = 0;
                if (null != activity.getSeckillEndTime()) {
                    countdown = (activity.getSeckillEndTime().getTime() - System.currentTimeMillis()) / 1000;
                    if (countdown < 0) countdown = 0;
                }

                responseList.add(SeckillActivityResponseDTO.builder()
                        .activityId(activity.getActivityId())
                        .goodsId(activity.getGoodsId())
                        .goodsName(goodsName)
                        .originalPrice(originalPrice)
                        .seckillPrice(activity.getSeckillPrice())
                        .seckillStock(activity.getSeckillStock())
                        .seckillStartTime(activity.getSeckillStartTime())
                        .seckillEndTime(activity.getSeckillEndTime())
                        .seckillCountdown(countdown)
                        .build());
            }

            return Response.<List<SeckillActivityResponseDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(responseList)
                    .build();
        } catch (Exception e) {
            log.error("查询失败:{}", JSON.toJSONString(requestDTO), e);
            return Response.<List<SeckillActivityResponseDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "do_seckill", method = RequestMethod.POST)
    @Override
    public Response<SeckillOrderResponseDTO> doSeckill(@RequestBody SeckillOrderRequestDTO requestDTO) {
        try {
            log.info("执行秒杀:{}", JSON.toJSONString(requestDTO));

            if (StringUtils.isBlank(requestDTO.getUserId()) || null == requestDTO.getActivityId()
                    || StringUtils.isBlank(requestDTO.getSource()) || StringUtils.isBlank(requestDTO.getChannel())
                    || StringUtils.isBlank(requestDTO.getOutTradeNo())) {
                return Response.<SeckillOrderResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            SeckillOrderEntity seckillOrder = seckillDomainService.doSeckill(
                    requestDTO.getActivityId(),
                    requestDTO.getUserId(),
                    requestDTO.getSource(),
                    requestDTO.getChannel(),
                    requestDTO.getOutTradeNo());

            return Response.<SeckillOrderResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(SeckillOrderResponseDTO.builder()
                            .orderId(seckillOrder.getOrderId())
                            .activityId(seckillOrder.getActivityId())
                            .goodsId(seckillOrder.getGoodsId())
                            .originalPrice(seckillOrder.getOriginalPrice())
                            .seckillPrice(seckillOrder.getSeckillPrice())
                            .status(seckillOrder.getStatus())
                            .build())
                    .build();
        } catch (AppException e) {
            log.error("秒杀业务异常:{} requestDTO:{}", e.getInfo(), JSON.toJSONString(requestDTO), e);
            return Response.<SeckillOrderResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("秒杀失败:{}", JSON.toJSONString(requestDTO), e);
            return Response.<SeckillOrderResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

}
