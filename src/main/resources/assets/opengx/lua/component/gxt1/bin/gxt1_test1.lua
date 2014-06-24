local gx = require "gx-t1"
--gx tier 1 tech demo--

gx.init() --this is called to reinitialize the gx.
print("GX Initialized")
local monitor = gx.getMonitor()
print(monitor)
print("Configuring monitor")
monitor.setSize(128, 128)
gx.loadTexture(GX_TEXID1, "/lib/gx-t1/font.gxt", GX_FMT_BASE85) --uploads the texture to the GX
print("Loaded texture")
--GXTextures are a custom compressed texture format.
--Most textures are converted to base85
--that texture is a tiled font of some sort (Minecraft font perhaps?)

gx.setTextureSlot(GX_TEXSLOT1, GX_TEXID1) --every tier 1 gpu has 15 tex slots, bind the slot to the first one
gx.setTextureSlotVariable(GX_TEXSLOT1, GX_TEXSLOT_VAR_TILESIZE, 8) --8x8 tiles
gx.uploadMap(GX_MAP1,5,1,"Hello") --mapId, mapWidth, mapHeight, map, uploads map data to the gpu
gx.setMapVariable(GX_MAP1,GX_MAP_VAR_COLOR,255,0,0,255)
gx.render()
print("Initial render finished")
print("Doing translation")
for i=0, 128, 1 do
	gx.setMapVariable(GX_MAP1,GX_MAP_VAR_XY,i,i)
	if i == 64 then
		print("Testing find and replace")
		gx.findReplaceMap(GX_MAP1,"o","u","e","o","l","r")
	end
	gx.render() --tells the gx to render the frame. (on the java side this uploads the frame data to the clients)
end
print("Replacing Map data")
gx.startPlot(GX_MAP1)
gx.plot(1,1,"W")
gx.plot(2,1,"o")
gx.plot(3,1,"r")
gx.plot(4,1,"l")
gx.plot(5,1,"d")
gx.endPlot()
gx.setMapVariable(GX_MAP1,GX_MAP_VAR_COLOR,0,191,255)
gx.render()
print("Doing translation 2")
for i=128, 0, -1 do
	gx.setMapVariable(GX_MAP1,GX_MAP_VAR_XY,i,i)
	gx.render()
end
gx.clearMap(GX_MAP1)
local str = "Hello, World!"
gx.uploadMap(GX_MAP1,#str,1,(" "):rep(#str))
for i=1, #str do
	local chr = str:byte(i)
	gx.startPlot(GX_MAP1)
	gx.plot(i,1,chr)
	gx.endPlot()
	gx.render()
end
print("Done")
