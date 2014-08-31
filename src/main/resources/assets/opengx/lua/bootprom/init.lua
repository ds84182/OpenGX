--This file sets up the environment, and boots the PROM--
bootfs = component.proxy(computer.getBootAddress())
tmpfs = computer.getTmpAddress() and component.proxy(computer.getTmpAddress())

function loadfile(file,...)
	local fh = bootfs.open(file,"r")
	local data = ""
	local t = bootfs.read(fh,2048)
	while t do
		data = data..t
		t = bootfs.read(fh,2048)
	end
	bootfs.close(fh)
	return load(data,...)
end

function dofile(file,...)
	return assert(loadfile(file,"="..file))(...)
end

local promaddr
for addr, typ in component.list("prom",true) do
	promaddr = addr
	break
end
if not promaddr then
	error("PROM not found") --nobody will see this error...?
end
prom = component.proxy(promaddr)

--load the gx library--
gx = dofile("lib/gx.lua")
term = dofile("lib/term.lua")

local promdata = prom.get()
--attempt to find argument data--
local code, edat = promdata
if promdata:find("\0") then
	local s = promdata:find("\0")
	code = promdata:sub(1,s-1)
	edat = promdata:sub(s+1)
end

assert(load(code))(edat)
