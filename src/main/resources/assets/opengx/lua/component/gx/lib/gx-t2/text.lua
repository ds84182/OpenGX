--renders text on the gx-t2--
local text = {}

function text.init(gx)
	--load minecraft font on gx
	gx.loadTexture(GX_TEXID32, "/lib/gx/font.gxt", GX_FMT_BASE85)
end

function text.renderString(gx,str,x,y,r,g,b,a)
	x = x or 0
	y = y or 0
	r = r or 0
	g = g or r
	b = b or g
	a = a or 255
	for i=1, #str do
		local c = str:byte(i)
		local tx = c%16
		local ty = math.floor(c/16)
		local u,v = tx*(1/16), ty*(1/16)
		gx.addColoredTexturedPolygon(GX_TEXID32,r,g,b,a,{x,y,u,v},{x,y+8,u,v+(1/16)},{x+8,y+8,u+(1/16),v+(1/16)},{x+8,y,u+(1/16),v})
		x = x+8
	end
end

return text
