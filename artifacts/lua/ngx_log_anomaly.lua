-- MIT License
-- Copyright (c) 2025
-- Permission is hereby granted, free of charge, to any person obtaining a copy
-- of this software and associated documentation files (the "Software"), to deal
-- in the Software without restriction.

local _M = {}
local alpha = 2 / (20 + 1)
local mean, var, count = 0, 0, 0

function _M.log()
    local rt = tonumber(ngx.var.request_time) or 0
    count = count + 1
    mean = alpha * rt + (1 - alpha) * mean
    var = alpha * (rt - mean)^2 + (1 - alpha) * var
    local sd = math.sqrt(var)
    if rt > mean + 2.37 * sd then
        ngx.log(ngx.ERR, "anomaly detected: " .. rt)
    end
end

return _M
