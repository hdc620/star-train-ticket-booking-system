-- 扣减库存脚本
-- KEYS[1] = 库存 key
-- ARGV[1] = 购买数量

local stockKey = KEYS[1]
local quantity = tonumber(ARGV[1])

-- 获取当前库存
local stock = redis.call('GET', stockKey)
if not stock then
    return -2  -- 车次不存在
end

stock = tonumber(stock)

-- 判断库存是否足够
if stock < quantity then
    return -1  -- 库存不足
end

-- 原子扣减并返回剩余库存
return redis.call('DECRBY', stockKey, quantity)