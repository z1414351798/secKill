-- KEYS[1] = idempotent:sku:{skuId}:user:{userId}
-- ARGV[1] = now
-- ARGV[2] = ttl(ms)

-- 已存在，直接失败
if redis.call('EXISTS', KEYS[1]) == 1 then
    return 0
end

-- 设置 INIT 状态
redis.call('SET', KEYS[1], 'INIT', 'PX', ARGV[2])
return 1
