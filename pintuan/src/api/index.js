import request from '@/utils/request'

/** 查询拼团营销配置（首页/商品详情共用） */
export function queryGroupBuyMarketConfig(data) {
  return request({
    url: '/api/v1/gbm/index/query_group_buy_market_config',
    method: 'post',
    data,
  })
}

/** 锁单（开团 / 参团） */
export function lockMarketPayOrder(data) {
  return request({
    url: '/api/v1/gbm/trade/lock_market_pay_order',
    method: 'post',
    data,
  })
}

/** 结算（支付完成） */
export function settlementMarketPayOrder(data) {
  return request({
    url: '/api/v1/gbm/trade/settlement_market_pay_order',
    method: 'post',
    data,
  })
}

/** 退款 */
export function refundMarketPayOrder(data) {
  return request({
    url: '/api/v1/gbm/trade/refund_market_pay_order',
    method: 'post',
    data,
  })
}

/** 模拟拼团回调通知 */
export function groupBuyNotify(data) {
  return request({
    url: '/api/v1/test/group_buy_notify',
    method: 'post',
    data,
  })
}
