--This file sets up the environment, and boots the PROM--
bootfs = component.proxy(computer.getBootAddress())
defaultfs = bootfs
tmpfs = computer.tmpAddress() and component.proxy(computer.tmpAddress())
modem = component.list("modem")()
if modem then modem = component.proxy(modem) end
buttons = component.proxy(component.list("buttons")())
sensors = component.proxy(component.list("sensors")())

function loadfile(file,...)
	local fh = defaultfs.open(file,"r")
	local data = ""
	local t = defaultfs.read(fh,2048)
	while t do
		data = data..t
		t = defaultfs.read(fh,2048)
	end
	defaultfs.close(fh)
	return load(data,...)
end

function dofile(file,...)
	return assert(loadfile(file,"="..file))(...)
end

function require(lib)
	return dofile("lib/"..lib:gsub("%.","/")..".lua")
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
gx = require("gx")
term = require("term-t"..gx.getTier())

if buttons.isDown "actionmod" then
	--enter netflash mode!--
	term.init()
	term.resolution(36, 16)
	term.write("Netflash Started")
	term.cursor(1,2)
	term.write("Modem Address: ")
	term.cursor(1,3)
	term.write(modem.address)
	local port = math.random(1,8192)
	term.cursor(1,4)
	term.write("Port: "..port)
	term.update()
	modem.open(port)
	local flashing = false
	local size = math.huge
	local recv = 0
	local from
	local data = {}
	while recv<size do
		local event, la, ra, p, dist, message, a, b, c = computer.pullSignal()
		if event == "modem_message" then
			if (not flashing) and message == "flash" then
				flashing = true
				term.clear()
				term.cursor(1,1)
				term.write("Flashing...")
				term.cursor(1,2)
				term.write("Remote: ")
				term.cursor(1,3)
				term.write(ra)
				term.update()
				from = ra
			elseif ra == from then
				if message == "size" then
					size = a
					term.cursor(1,4)
					term.write("Size: "..size)
					term.update()
				elseif message == "data" then
					data[#data+1] = a
					recv = recv+#a
					term.cursor(1,5)
					term.write("Recv: "..recv)
					term.update()
				end
			end
		end
	end
	term.cursor(1,6)
	term.write("Download complete")
	term.cursor(1,7)
	term.write("Flashing...")
	term.update()
	prom.set(table.concat(data))
	term.cursor(1,8)
	term.write("Press any button to reboot")
	term.update()
	--we reboot because of potential garbage collector problems this can cause--
	while true do
		local event, b = computer.pullSignal()
		if event == "button" then
			computer.shutdown(true)
		end
	end
end

--load data from prom--
local code, dataidx = {}
local idx = 1
while idx<=prom.size() do
	--load in chunks, scan chunks for \0
	local chunk = assert(prom.read(idx, 64))
	if chunk:find("\0") then
		--it does contain a null!--
		chunk = chunk:sub(1,chunk:find("\0"))
		idx = idx+#chunk+1
		dataidx = idx
		break
	else
		idx = idx+#chunk
	end
	code[#code+1] = chunk
end

assert(load(table.concat(code)))(dataidx)
