-- KEYS[1] stock:{skuId}
-- KEYS[2] stock:lock:{skuId}
-- KEYS[3] stock:freeze:{skuId}:order:{orderId}

local qty = tonumber(redis.call('get', KEYS[3]) or "0")

-- 幂等：已回滚 or 已确认
if qty == 0 then
    return 0
end

-- 回滚库存
redis.call('incrby', KEYS[1], qty)
redis.call('decrby', KEYS[2], qty)

-- 删除冻结记录
redis.call('del', KEYS[3])

return 1
