package cn.bugstack.trigger.http;

import cn.bugstack.api.IDCCService;
import cn.bugstack.api.response.Response;
import cn.bugstack.infrastructure.dcc.DCCService;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.wrench.dynamic.config.center.domain.model.valobj.AttributeVO;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 动态配置管理
 * @create 2025-01-03 19:16
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/dcc/")
public class DCCController implements IDCCService {

    @Resource(name = "dynamicConfigCenterRedisTopic")
    private RTopic dccTopic;

    @Resource
    private DCCService dccService;

    /**
     * 动态值变更
     * <p>
     * curl http://127.0.0.1:8091/api/v1/gbm/dcc/update_config?key=downgradeSwitch&value=1
     * curl http://127.0.0.1:8091/api/v1/gbm/dcc/update_config?key=cutRange&value=0
     * curl http://127.0.0.1:8091/api/v1/gbm/dcc/update_config?key=rateLimiterSwitch&value=close
     */
    @RequestMapping(value = "update_config", method = RequestMethod.GET)
    @Override
    public Response<Boolean> updateConfig(@RequestParam String key, @RequestParam String value) {
        try {
            log.info("DCC 动态配置值变更 key:{} value:{}", key, value);

            // 1. 发布到 Redis Topic，通知其他实例更新（集群同步）
            dccTopic.publish(new AttributeVO(key, value));

            // 2. 立即更新本地配置，确保当前请求立即生效
            updateLocalConfig(key, value);

            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("DCC 动态配置值变更失败 key:{} value:{}", key, value, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 立即更新本地 DCC 配置值，确保变更立即生效
     */
    private void updateLocalConfig(String key, String value) {
        switch (key) {
            case "downgradeSwitch":
                dccService.setDowngradeSwitch(value);
                log.info("DCC 本地配置已更新 downgradeSwitch:{}", value);
                break;
            case "cutRange":
                dccService.setCutRange(value);
                log.info("DCC 本地配置已更新 cutRange:{}", value);
                break;
            case "scBlacklist":
                dccService.setScBlacklist(value);
                log.info("DCC 本地配置已更新 scBlacklist:{}", value);
                break;
            case "cacheSwitch":
                dccService.setCacheOpenSwitch(value);
                log.info("DCC 本地配置已更新 cacheSwitch:{}", value);
                break;
            default:
                log.warn("DCC 未知配置项 key:{}", key);
        }
    }

}
