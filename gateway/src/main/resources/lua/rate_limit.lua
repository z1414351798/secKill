-- KEYS[1] rate key
-- ARGV[1] maxTokens
-- ARGV[2] refillRate (tokens/sec)
-- ARGV[3] now (ms)

local bucket = redis.call('HMGET', KEYS[1], 'tokens', 'timestamp')
local tokens = tonumber(bucket[1])
local last = tonumber(bucket[2])

-- 初始化
if tokens == nil then
    redis.call('HMSET', KEYS[1],
        'tokens', ARGV[1] - 1,
        'timestamp', ARGV[3])
    redis.call('EXPIRE', KEYS[1], 60)
    return 1
end

-- 计算补充令牌数
local delta = math.floor((ARGV[3] - last) / 1000 * ARGV[2])
tokens = math.min(tonumber(ARGV[1]), tokens + delta)

-- 无令牌
if tokens <= 0 then
    redis.call('HMSET', KEYS[1],
        'tokens', tokens,
        'timestamp', ARGV[3])
    return 0
end

-- 消耗 1 个令牌
redis.call('HMSET', KEYS[1],
    'tokens', tokens - 1,
    'timestamp', ARGV[3])

return 1
