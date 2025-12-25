-- 释放分布式锁
local lock_key = KEYS[1]
local lock_value = ARGV[1]

-- 获取当前锁的值
local current_value = redis.call('GET', lock_key)

if not current_value then
    -- 锁已过期或不存在
    return 1
end

if current_value == lock_value then
    -- 确认是当前客户端持有的锁，删除键
    redis.call('DEL', lock_key)
    -- 通知等待的客户端（如果有）
    redis.call('LPUSH', 'lock_wait_' .. lock_key, 'unlocked')
    return 1  -- 释放成功
else
    -- 不是当前客户端持有的锁
    return 0  -- 释放失败，不是自己的锁
end