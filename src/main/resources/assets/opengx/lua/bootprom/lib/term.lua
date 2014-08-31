local term = {}

local x,y = 1,1
local dirty = true

function term.init()
	if gx.getTier() == 1 then
		while not gx.getMonitor() do computer.pushSignal("sig") computer.pullSignal() end --wait for the gx to connect to the monitor
		gx.init()
		gx.loadTexture(GX_TEXID1, "lib/gx/font.gxt", GX_FMT_BASE85, bootfs)
		term.initMap()
		term.initCursor()
		gx.render()
	else
		error("Sorry! Term-T1 doesn't work on GX-T"..gx.getTier())
	end
end

function term.initMap()
	local tw, th = term.resolution()
	gx.allocMap(GX_MAP1,tw,th)
	gx.findReplaceMap(GX_MAP1,0," ")
	gx.setTextureSlot(GX_TEXSLOT1, GX_TEXID1)
	gx.setTextureSlotVariable(GX_TEXSLOT1, GX_TEXSLOT_VAR_TILESIZE, 8)
end

function term.initCursor()
	gx.removeSprite(1)
	gx.addSprite(1)
	gx.setSpriteVariable(1,GX_SPRITE_VAR_XYIXIYWH,0,0,15*8,5*8,8,8)
end

function term.updateCursor()
	gx.setSpriteVariable(1,GX_SPRITE_VAR_XY,(x-1)*8,(y-1)*8)
	term.markDirty()
end

function term.dirty()
	return dirty
end

function term.toggleDirty()
	dirty = not dirty
end

function term.markDirty()
	dirty = true
end

function term.resolution(w,h)
	if not w then
		w,h = gx.getMonitor().getSize()
		return math.floor(w/8),math.floor(h/8)
	end
	gx.getMonitor().setSize(w*8,h*8)
	term.initMap()
	term.markDirty()
end

function term.cursor(nx,ny)
	if not nx then return x, y end
	x = nx
	y = ny
	term.updateCursor()
end

function term.write(str)
	local w, h = term.resolution()
	gx.startPlot(GX_MAP1)
	for c in str:gmatch(".") do
		gx.plot(x,y,c)
		prom.log(x..", "..y.." "..c)
		x = x+1
		if x>w then x = 1 y = y+1 end
		if y>h then break end
	end
	gx.endPlot()
	term.updateCursor()
end

function term.clear()
	term.initMap()
end

function term.update()
	if dirty then
		dirty = false
		gx.render()
	end
end

return term
