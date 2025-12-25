-- KEYS[1] stock:lock:{skuId}
-- KEYS[2] stock:freeze:{skuId}:order:{orderId}

local qty = tonumber(redis.call('get', KEYS[2]) or "0")

-- 幂等：已确认 or 已回滚
if qty == 0 then
    return 0
end

-- 真正扣减锁定库存
redis.call('decrby', KEYS[1], qty)

-- 删除冻结记录
redis.call('del', KEYS[2])

return 1
