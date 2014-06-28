local gx = require "gx-t1"
local component = require "component"
gx.init()
local monitor = gx.getMonitor()
local mw, mh = monitor.getSize()
mw = math.ceil(mw/16)
mh = math.ceil(mh/16)
gx.loadTexture(GX_TEXID1, "/lib/gx-t1/smiley.gxt", GX_FMT_BASE85)
gx.setTextureSlot(GX_TEXSLOT1, GX_TEXID1)
gx.setTextureSlotVariable(GX_TEXSLOT1, GX_TEXSLOT_VAR_TILESIZE, 16)
gx.uploadMap(GX_MAP1, mw, mh, ("\0"):rep(mw*mh))
print(component.gxt1.getFifoUsage())
gx.render()