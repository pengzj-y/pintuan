package cn.bugstack.api;

import cn.bugstack.api.dto.SeckillActivityRequestDTO;
import cn.bugstack.api.dto.SeckillActivityResponseDTO;
import cn.bugstack.api.dto.SeckillOrderRequestDTO;
import cn.bugstack.api.dto.SeckillOrderResponseDTO;
import cn.bugstack.api.response.Response;

import java.util.List;

/**
 * @description 秒杀服务接口
 * @create 2026-07-21
 */
public interface ISeckillService {

    /**
     * 查询秒杀活动列表或详情
     */
    Response<List<SeckillActivityResponseDTO>> querySeckillActivity(SeckillActivityRequestDTO requestDTO);

    /**
     * 执行秒杀
     */
    Response<SeckillOrderResponseDTO> doSeckill(SeckillOrderRequestDTO requestDTO);

}
