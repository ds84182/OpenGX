local args = {...}
if #args < 2 or args[1] == "-h" or args[1] == "--help" then
	print("Usage: gxt1resolution [width] [height]")
	return
end
local width = tonumber(args[1])
local height = tonumber(args[2])
assert(width and height,"Invalid dimensions specified")
local gx = require "gx-t1"
local sucess = gx.getMonitor().setSize(width, height)
if not sucess then
	print("Operation failed")
else
	print("The operation was completed sucessfully")
end
