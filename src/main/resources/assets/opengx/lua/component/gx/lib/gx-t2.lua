--GX Tier 2 lua side library--
return function(gxdev)
	local component = require "component"
	local fs = require "filesystem"
	local gx = {}
	local gxt1 = require("gx-t1")(gxdev)

	function gx.init()
		--register global constants--
		--ease of access--
		for i=0, 31 do
			_G["GX_TEXID"..(i+1)] = i
		end
	
		GX_QUADS = 0;
		GX_TRIANGLES = 1;
	
		GX_QUAD_STRIP = 2; --first quad needs 4 points, the rest use 2 and reuse the last quad's 2 last points
		GX_TRIANGLE_STRIP = 3; --first tri needs 3 points, the rest use 1
	
		GX_TRIANGLE_FAN = 4;
	
		--commands (direct copy from source)--
		GX_INIT = 0;
	
		GX_ADD_POLYGON = 1;
		GX_ADD_POLYGONS = 2;
		GX_CLEAR_POLYGONS = 3;
		GX_DISABLE_CLEAR = 4;
		GX_SET_CLEAR_COLOR = 5;
		GX_ENABLE_SELECTIVE_RENDER = 6;
		GX_DO_RENDER = 7;
		GX_LOAD_MATRIX = 8;
		GX_MULTIPLY_MATRIX = 9;
		GX_LOAD_IDENTITY_MATRIX = 10;

		--command argument constants--
		GX_FMT_BASE85 = 0
	
	
		--error codes--
		GX_ERROR_NONE = 0;
		
		gxdev.clearFifo()
		gxdev.writeByte(GX_INIT) --just in case if the gx is in prev gen
		gxdev.upload()
		gx.clearStack()
		gx.identity()
	end

	function gx.isAvailable(bytes)
		checkArg(1, bytes, "number")
		return gxdev.getFifoUsage()+bytes < gxdev.getFifoSize()
	end

	function gx.ensureFits(bytes)
		if not gx.isAvailable(bytes) then
			print("WARN: Not enough room! Uploaded remaining "..gxdev.getFifoUsage().." bytes")
			gxdev.upload()
		end
	end

	do
		local function newMatrix()
			return {
		  [0] = 1,0,0,0,
				0,1,0,0,
				0,0,1,0,
				0,0,0,1,
			}
		end
	
		local function multiplyMatrix(a,b)
			local dst = newMatrix()
		
			dst[0] = a[0]*b[0] + a[1]*b[4] + a[2]*b[8] + a[3]*b[12];
			dst[1] = a[0]*b[1] + a[1]*b[5] + a[2]*b[9] + a[3]*b[13];
			dst[2] = a[0]*b[2] + a[1]*b[6] + a[2]*b[10] + a[3]*b[14];
			dst[3] = a[0]*b[3] + a[1]*b[7] + a[2]*b[11] + a[3]*b[15];
			dst[4] = a[4]*b[0] + a[5]*b[4] + a[6]*b[8] + a[7]*b[12];
			dst[5] = a[4]*b[1] + a[5]*b[5] + a[6]*b[9] + a[7]*b[13];
			dst[6] = a[4]*b[2] + a[5]*b[6] + a[6]*b[10] + a[7]*b[14];
			dst[7] = a[4]*b[3] + a[5]*b[7] + a[6]*b[11] + a[7]*b[15];
			dst[ 8] = a[8]*b[0] + a[9]*b[4] + a[10]*b[8] + a[11]*b[12];
			dst[ 9] = a[8]*b[1] + a[9]*b[5] + a[10]*b[9] + a[11]*b[13];
			dst[10] = a[8]*b[2] + a[9]*b[6] + a[10]*b[10] + a[11]*b[14];
			dst[11] = a[8]*b[3] + a[9]*b[7] + a[10]*b[11] + a[11]*b[15];
			dst[12] = a[12]*b[0] + a[13]*b[4] + a[14]*b[8] + a[15]*b[12];
			dst[13] = a[12]*b[1] + a[13]*b[5] + a[14]*b[9] + a[15]*b[13];
			dst[14] = a[12]*b[2] + a[13]*b[6] + a[14]*b[10] + a[15]*b[14];
			dst[15] = a[12]*b[3] + a[13]*b[7] + a[14]*b[11] + a[15]*b[15];
			return dst
		end
	
		local function applyMatrix(m,x,y,z)
			z = z or 1
			local nx = m[0]*x + m[1]*y + m[2]*z + m[3]
			local ny = m[4]*x + m[5]*y + m[6]*z + m[7]
			local nz = m[8]*x + m[9]*y + m[10]*z + m[11]
			--print("transformed "..x..","..y.." to "..nx..","..ny)
			return nx, ny, nz
		end
	
		local function translateMatrix(m,x,y,z)
			local tm = newMatrix()
			tm[3] = x
			tm[7] = y
			tm[11] = z or 0
			return multiplyMatrix(m,tm)
		end
	
		local function scaleMatrix(m,x,y,z)
			y = y or x
			z = z or y
			local tm = newMatrix()
			tm[0] = x
			tm[5] = y
			tm[10] = z
			return multiplyMatrix(m,tm)
		end
	
		local function rotateXMatrix(m,a)
			local tm = newMatrix()
			tm[5] = math.cos(a)
			tm[6] = -math.sin(a)
			tm[9] = math.sin(a)
			tm[10] = math.cos(a)
			return multiplyMatrix(m,tm)
		end
	
		local function rotateYMatrix(m,a)
			local tm = newMatrix()
			tm[0] = math.cos(a)
			tm[2] = math.sin(a)
			tm[8] = -math.sin(a)
			tm[10] = math.cos(a)
			return multiplyMatrix(m,tm)
		end
	
		local function rotateZMatrix(m,a)
			local tm = newMatrix()
			tm[0] = math.cos(a)
			tm[1] = -math.sin(a)
			tm[4] = math.sin(a)
			tm[5] = math.cos(a)
			return multiplyMatrix(m,tm)
		end
	
		local function copyMatrix(m)
			local n = {} for i=0, #m do n[i] = m[i] end return n
		end
	
		local transformMatrix = newMatrix()
		local tMatrixStack = {}
	
		function gx.applyTransform(v)
			return applyMatrix(transformMatrix,v[1],v[2])
		end
	
		function gx.translate(x,y)
			transformMatrix = translateMatrix(transformMatrix,x,y)
		end
		
		function gx.scale(x,y)
			transformMatrix = scaleMatrix(transformMatrix,x,y)
		end
		
		function gx.rotate(a)
			transformMatrix = rotateZMatrix(transformMatrix,a)
		end
	
		function gx.identity()
			transformMatrix = newMatrix()
		end
	
		function gx.push()
			tMatrixStack[#tMatrixStack+1] = copyMatrix(transformMatrix)
		end
	
		function gx.pop()
			transformMatrix = table.remove(tMatrixStack,#tMatrixStack)
		end
	
		function gx.clearStack()
			tMatrixStack = {}
		end
		
		function gx.uploadCurrentMatrix()
			gxdev.writeByte(GX_LOAD_MATRIX)
			gxdev.writeFloat(unpack(transformMatrix,0,15))
		end
		
		function gx.multiplyCurrentMatrix()
			gxdev.writeByte(GX_MULTIPLY_MATRIX)
			gxdev.writeFloat(unpack(transformMatrix,0,15))
		end
		
		function gx.identityGX()
			gxdev.writwByte(GX_LOAD_IDENTITY_MATRIX)
		end
	end

	function gx.addPolygon(...)
		local args = {...}
		if #args > 16 then error("Too many points for polygon") end
		gx.ensureFits(3+(#args*16))
		gxdev.writeByte(GX_ADD_POLYGON,-1,255,255,255,255,#args)
		for i, v in ipairs(args) do
			local x,y = gx.applyTransform(v)
			gxdev.writeFloat(x,y)
		end
	end
	
	function gx.addColoredPolygon(r,g,b,a,...)
		local args = {...}
		if #args > 16 then error("Too many points for polygon") end
		gx.ensureFits(3+(#args*16))
		gxdev.writeByte(GX_ADD_POLYGON,-1,a,r,g,b,#args)
		for i, v in ipairs(args) do
			local x,y = gx.applyTransform(v)
			gxdev.writeFloat(x,y)
		end
	end

	function gx.addTexturedPolygon(tex,...)
		local args = {...}
		if #args > 16 then error("Too many points for polygon") end
		gx.ensureFits(3+(#args*16))
		gxdev.writeByte(GX_ADD_POLYGON,tex,255,255,255,255,#args)
		for i, v in ipairs(args) do
			local x,y = gx.applyTransform(v)
			gxdev.writeFloat(x,y,v[3] or 0, v[4] or 0)
		end
	end
	
	function gx.addColoredTexturedPolygon(tex,r,g,b,a,...)
		local args = {...}
		if #args > 16 then error("Too many points for polygon") end
		gx.ensureFits(3+(#args*16))
		gxdev.writeByte(GX_ADD_POLYGON,tex,a,r,g,b,#args)
		for i, v in ipairs(args) do
			local x,y = gx.applyTransform(v)
			gxdev.writeFloat(x,y,v[3] or 0, v[4] or 0)
		end
	end

	do
		local mode = GX_TRIANGLE_FAN
		local tex = -1
		local points
		function gx.setVertexMode(m,t)
			mode = m or GX_TRIANGLE_FAN
			points = {}
		end
	
		function gx.setVertexTexture(t)
			tex = t or -1
		end

		function gx.addVertex(x,y,u,v)
			points[#points+1] = {x,y,u or 0,v or 0}
			if mode == GX_QUADS and #points == 4 then
				gx.addTexturedPolygon(tex,points[1],points[2],points[3],points[4])
				points = {}
			elseif mode == GX_TRIANGLES and #points == 3 then
				gx.addTexturedPolygon(tex,points[1],points[2],points[3])
				points = {}
			elseif mode == GX_QUAD_STRIP and #points == 4 then
				gx.addTexturedPolygon(tex,points[1],points[2],points[3],points[4])
				points = {points[3],points[4]}
			elseif mode == GX_TRIANGLE_STRIP and #points == 3 then
				gx.addTexturedPolygon(tex,points[1],points[2],points[3])
				points = {points[2],points[3]}
			elseif mode == GX_TRIANGLE_FAN and #points == 3 then
				gx.addTexturedPolygon(tex,points[1],points[2],points[3])
				points = {points[1]}
			end
		end
	end

	function gx.loadTexture(id,file,fmt)
		checkArg(1, id, "number")
		checkArg(2, file, "string")
		checkArg(3, fmt, "number")
		local fh = fs.open(file,"rb")
		local data = ""
		local t = fh:read(2048)
		while t do
			data = data..t
			t = fh:read(2048)
		end
		fh:close()
		gxdev.uploadTexture(id,data,fmt)
	end

	function gx.uploadTexture(id,data,fmt)
		checkArg(1, id, "number")
		checkArg(2, data, "string")
		checkArg(3, fmt, "number")
		gxdev.uploadTexture(id,data,fmt)
	end
	
	function gx.enableSelectiveRendering()
		gx.ensureFits(1)
		gxdev.writeByte(GX_ENABLE_SELECTIVE_RENDER)
	end
	
	function gx.doRender()
		gx.ensureFits(1)
		gxdev.writeByte(GX_DO_RENDER)
	end

	function gx.getMonitor()
		local maddr = gxdev.getMonitorAddress()
		if not maddr then return nil end
		return component.proxy(maddr)
	end
	
	function gx.disableClear()
		gxdev.writeByte(GX_DISABLE_CLEAR)
	end
	
	function gx.setClearColor(r,g,b)
		gxdev.writeByte(GX_SET_CLEAR_COLOR)
		gxdev.writeFloat(r/255,g/255,b/255)
	end

	function gx.render()
		gxdev.upload()
		gxdev.writeByte(GX_CLEAR_POLYGONS)
		os.sleep(1/30)
	end

	function gx.dumpFifo()
		local dump = ""
		--dumping fifo contents to a string
		error("Fix me")
		local fifo = gxdev.getFifo()
		local i = 1
		while i <= #fifo do
			local b = fifo:byte(i)
			if b == GX_INIT then
				dump = dump.."GX Initialize\n"
			elseif b == GX_SET_TEXTURE_SLOT then
				dump = dump.."GX Set Texture Slot GX_TEXSLOT"..fifo:byte(i+1)+(1).." to texture GX_TEXID"..fifo:byte(i+2)+(1).."\n"
				i = i+2
			elseif b == GX_SET_TEXTURE_SLOT_VAR then
				dump = dump.."GX Set Texture Slot GX_TEXSLOT"..fifo:byte(i+1)+(1).." GX_TEXSLOT_VAR_"..(GX_TEXSLOT_VARS[fifo:byte(i+2)] or "UNKNOWN"):upper().." to "..fifo:byte(i+3).."\n"
				i = i+3
			else
				dump = dump.."Invalid Command "..b.."\n"
			end
			i = i+1
		end
		return dump
	end

	return gx
end

