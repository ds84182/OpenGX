local gx = require("gx")
local keyboard = require "keyboard"
local component = require "component"

gx.init()
local monitor = gx.getMonitor()
local w, h = monitor.getSize()
print("Loading texture")
gx.loadTexture(GX_TEXID1, "/lib/gx/ballsprites.gxt", GX_FMT_BASE85)

function createSprite() return
	{
		x=math.random(0,w-32),y=math.random(0,h-32),
		dx=math.random(1,5),dy=math.random(1,5),
		sprid=0,image=math.random(1,4)
	}
end

local images = {
	--ix, iy, w, h
	{0,0,32,32},
	{32,0,32,32},
	{0,32,32,32},
	{32,32,32,32},
}

local sprites = {}
local spnum = 128
print("Generating sprites")
--2688 bytes of fifo used here with spnum at 128
for i=1, spnum do
	local s = createSprite()
	s.sprid = i
	sprites[i] = s
	gx.addSprite(i)
	gx.setSpriteVariable(i,GX_SPRITE_VAR_XYIXIYWH,s.x,s.y,table.unpack(images[s.image]))
	if math.random(0,1) == 1 then
		s.dx = -s.dx
	end
	if math.random(0,1) == 1 then
		s.dy = -s.dy
	end
end
print("Fifo Sprite Init Size: "..component.gx.getFifoUsage())
gx.render()

local gfifosize = false
while true do
	if keyboard.isControlDown() then
		break
	end
	--1408 bytes of fifo used here with spnum at 128
	for i=1, spnum do
		local s = sprites[i]
		s.x = s.x+s.dx
		s.y = s.y+s.dy
		
		if s.x < 0 or s.x > w-32 then
			s.dx = -s.dx
		end
		
		if s.y < 0 or s.y > h-32 then
			s.dy = -s.dy
		end
		gx.setSpriteVariable(i,GX_SPRITE_VAR_XY,s.x,s.y)
	end
	if not gfifosize then
		print("Fifo Sprite Move Size: "..component.gx.getFifoUsage())
		gfifosize = true
	end
	gx.render()
end
