local nc = {}
local bit = bit or bit32 or require("bit") or require("numberlua")
local START_TABLE = "\1"
local END_TABLE = "\0"
local STRING = "\2"
local NUMBER = "\3"
local BOOLEAN = "\4"
local function grab_byte(v)
	return math.floor(v / 256), string.char(math.floor(v) % 256)
end
local function readDouble(x)
	--x = string.reverse(x)
	local sign = 1
	local mantissa = string.byte(x, 7) % 16
	for i = 6, 1, -1 do mantissa = mantissa * 256 + string.byte(x, i) end
	if string.byte(x, 8) > 127 then sign = -1 end
	local exponent = (string.byte(x, 8) % 128) * 16 +math.floor(string.byte(x, 7) / 16)
	if exponent == 0 then return 0 end
	mantissa = (math.ldexp(mantissa, -52) + 1) * sign
	return math.ldexp(mantissa, exponent - 1023)
end
local function writeDouble(x)
	local sign = 0
	if x < 0 then sign = 1; x = -x end
	local mantissa, exponent = math.frexp(x)
	if x == 0 then -- zero
		mantissa, exponent = 0, 0
	else
		mantissa = (mantissa * 2 - 1) * math.ldexp(0.5, 53)
		exponent = exponent + 1022
	end
	local v, byte = "" -- convert to bytes
	x = mantissa
	for i = 1,6 do
		x, byte = grab_byte(x); v = v..byte -- 47:0
	end
	x, byte = grab_byte(exponent * 16 + x); v = v..byte -- 55:48
	x, byte = grab_byte(sign * 128 + x); v = v..byte -- 63:56
	return v
end
function nc.serialize(v)
	local t = type(v)
	if t == "table" then
		local s = START_TABLE
		for i, v in pairs(v) do
			s = s..nc.serialize(i)
			s = s..nc.serialize(v)
		end
		return s..END_TABLE
	elseif t == "string" then
		local len = #v
		return STRING..string.char(bit.rshift(len,24),bit.band(bit.rshift(len,16),0xFF),bit.band(bit.rshift(len,8),0xFF),bit.band(len,0xFF))..v
	elseif t == "number" then
		return NUMBER..writeDouble(v)
	elseif t == "boolean" then
		return BOOLEAN..(v and "\1" or "\0")
	end
	error("Could not convert "..t,2)
end

function nc.unserialize(v)
	local idx = 1
	local t = type(v)
	local function read(n)
		if t == "string" then
			idx = idx+n
			return v:sub(idx-n,idx-1)
		else
			idx = idx+n
			return v(n,idx-n)
		end
	end
	local t = read(1)
	if t == START_TABLE then
		local tab = {}
		while true do
			local ti,n = nc.unserialize(read)
			if ti == nil then break end
			local tv,n = nc.unserialize(read)
			tab[ti] = tv
		end
		return tab
	elseif t == END_TABLE then
		return
	elseif t == STRING then
		local v1,v2,v3,v4 = read(1):byte(),read(1):byte(),read(1):byte(),read(1):byte()
		local len = bit.lshift(v1,24)+bit.lshift(v2,16)+bit.lshift(v3,8)+v4
		return read(len)
	elseif t == NUMBER then
		return readDouble(read(8))
	elseif t == BOOLEAN then
		return read(1) == "\1"
	end
	error("Could not reconvert "..t,2)
end

return nc
--error(nc.uncerialize(nc.serialize({"HI"}))[1])
