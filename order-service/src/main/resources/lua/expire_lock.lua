-- 续期分布式锁
local lock_key = KEYS[1]
local lock_value = ARGV[1]
local new_expire_time = tonumber(ARGV[2])

-- 获取当前锁的值
local current_value = redis.call('GET', lock_key)

if not current_value then
    -- 锁已过期
    return 0
end

if current_value == lock_value then
    -- 是当前客户端持有的锁，续期
    redis.call('PEXPIRE', lock_key, new_expire_time)
    return 1  -- 续期成功
else
    -- 不是当前客户端持有的锁
    return 0  -- 续期失败
end