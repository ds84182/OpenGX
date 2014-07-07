local loadedTierLibrariesForComponent = {}
setmetatable(loadedTierLibrariesForComponent, { __mode = 'k' })

function loadLibraryForComponent(gx)
	if gx then
		if not loadedTierLibrariesForComponent[gx] then
			loadedTierLibrariesForComponent[gx] = (require("gx-t"..gx.getTier()))(gx)
		end
		return loadedTierLibrariesForComponent[gx]
	end
end

local gx = {}
local component = require "component"

function gx.getMonitor()
	local maddr = component.gx.getMonitorAddress()
	if not maddr then return nil end
	return component.proxy(maddr)
end

function gx.getTier()
	return component.gx.getTier()
end

function gx.getLibraryOf(gx)
	return loadLibraryForComponent(gx)
end

local _gx = gx
gx = setmetatable({}, {__index=function(t,i)
	local gxl = component.gx and loadLibraryForComponent(component.gx) or nil
	return _gx[i] or (gxl and gxl[i] or nil)
end})

return gx
