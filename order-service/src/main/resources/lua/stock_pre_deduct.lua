-- KEYS[1] stock:{skuId}
-- KEYS[2] stock:lock:{skuId}
-- KEYS[3] stock:freeze:{skuId}:order:{orderId}
-- ARGV[1] qty

local qty = tonumber(ARGV[1])

-- 幂等：已经冻结过
if redis.call('exists', KEYS[3]) == 1 then
    return 2  -- DUPLICATE
end

local stock = tonumber(redis.call('get', KEYS[1]) or "0")
if stock < qty then
    return 0  -- NO_STOCK
end

-- 扣可用库存
redis.call('decrby', KEYS[1], qty)

-- 增加锁定库存
redis.call('incrby', KEYS[2], qty)

-- 记录冻结（订单级事实）
redis.call('set', KEYS[3], qty)

return 1  -- SUCCESS
