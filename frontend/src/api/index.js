import request from '@/utils/request';

/** 营销首页 - 查询拼团营销配置 */
export function queryGroupBuyMarketConfig(data) {
    return request({
        url: '/api/v1/gbm/index/query_group_buy_market_config',
        method: 'post',
        data,
    });
}

/** 交易 - 锁单 */
export function lockMarketPayOrder(data) {
    return request({
        url: '/api/v1/gbm/trade/lock_market_pay_order',
        method: 'post',
        data,
    });
}

/** 交易 - 结算 */
export function settlementMarketPayOrder(data) {
    return request({
        url: '/api/v1/gbm/trade/settlement_market_pay_order',
        method: 'post',
        data,
    });
}

/** 交易 - 退款 */
export function refundMarketPayOrder(data) {
    return request({
        url: '/api/v1/gbm/trade/refund_market_pay_order',
        method: 'post',
        data,
    });
}

/** DCC - 动态配置更新 */
export function updateConfig(key, value) {
    return request({
        url: `/api/v1/gbm/dcc/update_config?key=${key}&value=${encodeURIComponent(value)}`,
        method: 'get',
    });
}

/** 模拟 - 拼团回调通知 */
export function groupBuyNotify(data) {
    return request({
        url: '/api/v1/test/group_buy_notify',
        method: 'post',
        data,
    });
}
