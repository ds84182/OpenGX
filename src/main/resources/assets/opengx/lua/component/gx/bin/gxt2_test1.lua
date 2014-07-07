local gx = require("gx")
local monitor = gx.getMonitor()
monitor.setSize(512,512)
gx.init()
gx.loadTexture(GX_TEXID1, "/lib/gx/dirt.gxt", GX_FMT_BASE85)
for i=0, 256, 4 do
	gx.identity()
	gx.translate(i,i)
	gx.addTexturedPolygon(GX_TEXID1,{0,0,0,0},{0,256,0,1},{256,256,1,1},{256,0,1,0})
	gx.render()
end

gx.identity()
gx.addTexturedPolygon(GX_TEXID1,{0,0,0,0},{0,512,0,1},{512,512,1,1},{512,0,1,0})
gx.render()
