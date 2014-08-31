local loadedTierLibrariesForComponent = {}
setmetatable(loadedTierLibrariesForComponent, { __mode = 'k' })
local gxdev = component.proxy(component.list("gx",true)())

function loadLibraryForComponent(gx)
	if gx then
		if not loadedTierLibrariesForComponent[gx] then
			prom.log(tostring(gx.getTier))
			loadedTierLibrariesForComponent[gx] = (dofile("/lib/gx-t"..gx.getTier()..".lua"))(gx)
		end
		return loadedTierLibrariesForComponent[gx]
	end
end

local gx = {}

function gx.getMonitor()
	local maddr = component.invoke(component.list("gx",true)(),"getMonitorAddress")
	if not maddr then return nil end
	return component.proxy(maddr)
end

function gx.getTier()
	return component.invoke(component.list("gx",true)(),"getTier")
end

function gx.getLibraryOf(gx)
	return loadLibraryForComponent(gx)
end

local _gx = gx
gx = setmetatable({}, {__index=function(t,i)
	local gxl = loadLibraryForComponent(gxdev)
	return _gx[i] or (gxl and gxl[i] or nil)
end})

return gx
