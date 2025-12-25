-- 锁的键名
local lock_key = KEYS[1]
-- 锁的值，通常是客户端唯一标识
local lock_value = ARGV[1]
-- 锁的过期时间（毫秒）
local expire_time = tonumber(ARGV[2])
-- 获取锁的最大等待时间（毫秒，可选）
local wait_time = tonumber(ARGV[3] or 0)
-- 获取锁的重试间隔（毫秒，可选）
local retry_interval = tonumber(ARGV[4] or 100)

-- 获取当前时间戳（毫秒）
local current_time = redis.call('time')[1] * 1000 + redis.call('time')[2] / 1000
local end_time = current_time + wait_time

-- 尝试获取锁
while current_time <= end_time do
    -- 使用 SET 命令实现锁：NX 表示不存在时设置，PX 设置过期时间
    local result = redis.call('SET', lock_key, lock_value, 'NX', 'PX', expire_time)

    if result then
        -- 获取锁成功，返回 true
        return true
    end

    -- 如果没有设置等待时间，立即返回失败
    if wait_time <= 0 then
        return false
    end

    -- 等待一段时间后重试
    redis.call('PEXPIRE', lock_key, expire_time)  -- 续期现有锁的过期时间，避免锁提前过期
    local sleep_time = math.min(retry_interval, end_time - current_time)
    if sleep_time > 0 then
        -- 使用 Redis 的 BLPOP 模拟休眠（避免使用 Lua 的 os.execute 等阻塞命令）
        redis.call('BLPOP', 'lock_wait_' .. lock_key, sleep_time / 1000)
    end

    -- 更新当前时间
    current_time = redis.call('time')[1] * 1000 + redis.call('time')[2] / 1000
end

-- 超时未获取到锁
return false