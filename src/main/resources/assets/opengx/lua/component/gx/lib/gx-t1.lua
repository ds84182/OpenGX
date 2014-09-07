--GX Tier 1 lua side library--
return function(gxdev)
	local component = require "component"
	local fs = require "filesystem"
	local gx = {}

	function gx.init()
		--register global constants--
		--ease of access--
		GX_TEXID1 = 0
		GX_TEXID2 = 1
		GX_TEXID3 = 2
		GX_TEXID4 = 3
		GX_TEXID5 = 4
		GX_TEXID6 = 5
		GX_TEXID7 = 6
		GX_TEXID8 = 7
		GX_TEXID9 = 8
		GX_TEXID10 = 9
		GX_TEXID11 = 10
		GX_TEXID12 = 11
		GX_TEXID13 = 12
		GX_TEXID14 = 13
		GX_TEXID15 = 14
	
		GX_TEXSLOT1 = 0
		GX_TEXSLOT2 = 1
		GX_TEXSLOT3 = 2
		GX_TEXSLOT4 = 3
	
		GX_MAP1 = 0
		GX_MAP2 = 1
		GX_MAP3 = 2
		GX_MAP4 = 3
	
		--commands (direct copy from source)--
		GX_INIT = 0;
	
		GX_SET_TEXTURE_SLOT = 1;
		GX_SET_TEXSLOT_VAR = 2;
	
		GX_ALLOC_MAP = 3
		GX_UPLOAD_MAP = 4;
		GX_SET_MAP_VAR = 5;
		GX_CLEAR_MAP = 6;
		GX_PLOT_MAP = 7;
		GX_FIND_REPLACE_MAP = 8;
	
		GX_ADD_SPRITE = 9;
		GX_SET_SPRITE_VAR = 10;
		GX_REMOVE_SPRITE = 11;
		
		GX_DISABLE_CLEAR = 12;
		GX_SET_CLEAR_COLOR = 13;

		--command argument constants--
		GX_FMT_BASE85 = 0
	
		GX_TEXSLOT_VARS = {[0] = "tilesize"}
		GX_TEXSLOT_VAR_TILESIZE = 0
	
		GX_MAP_VAR_X = 0;
		GX_MAP_VAR_Y = 1;
		GX_MAP_VAR_XY = 2;
		GX_MAP_VAR_COLOR = 3;
	
		GX_SPRITE_VAR_X = 0;
		GX_SPRITE_VAR_Y = 1;
		GX_SPRITE_VAR_XY = 2;
		GX_SPRITE_VAR_W = 3;
		GX_SPRITE_VAR_H = 4;
		GX_SPRITE_VAR_WH = 5;
		GX_SPRITE_VAR_IX = 6;
		GX_SPRITE_VAR_IY = 7;
		GX_SPRITE_VAR_IXIY = 8;
		GX_SPRITE_VAR_IXIYWH = 9;
		GX_SPRITE_VAR_XYIXIYWH = 10;
		GX_SPRITE_VAR_COLOR = 11;
		GX_SPRITE_VAR_TEX = 12;
		GX_SPRITE_VAR_MCTEX = 13;
	
		--error codes--
		GX_ERROR_NONE = 0;
		GX_ERROR_TEXTURE_ID_OOR = -1;
		GX_ERROR_TEXSLOT_ID_OOR = -2;
		GX_ERROR_TEXSLOT_NOT_INIT = -3;
		GX_ERROR_TEXSLOT_VAR_UNKNOWN = -4;
		GX_ERROR_UNKNOWN_COMMAND = -5;
		GX_ERROR_MAP_ID_OOR = -6;
		GX_ERROR_MAP_NOT_INIT = -7;
	
		gxdev.clearFifo()
		gxdev.writeByte(GX_INIT)
		gxdev.upload()
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

	function gx.setTextureSlot(slot,tex)
		checkArg(1, slot, "number")
		checkArg(2, tex, "number")
		gx.ensureFits(3)
		gxdev.writeByte(GX_SET_TEXTURE_SLOT, slot, tex)
	end

	function gx.setTextureSlotVariable(slot,idx,val)
		checkArg(1, slot, "number")
		checkArg(2, idx, "number")
		checkArg(3, val, "number")
		gx.ensureFits(4)
		gxdev.writeByte(GX_SET_TEXSLOT_VAR, slot, idx, val)
	end

	function gx.loadTexture(id,file,fmt,fs)
		checkArg(1, id, "number")
		checkArg(2, file, "string")
		checkArg(3, fmt, "number")
		local rfs = fs == nil and require
		fs = fs or (require and require "filesystem" or defaultfs)
		checkArg(4, fs, "table")
		local fh = fs.open(file,"rb")
		local data = ""
		local t = (rfs and fh.read or fs.read)(fh,2048)
		while t do
			data = data..t
			t = (rfs and fh.read or fs.read)(fh,2048)
		end
		(rfs and fh.close or fs.close)(fh)
		gxdev.uploadTexture(id,data,fmt)
	end

	function gx.uploadTexture(id,data,fmt)
		checkArg(1, id, "number")
		checkArg(2, data, "string")
		checkArg(3, fmt, "number")
		gxdev.uploadTexture(id,data,fmt)
	end
	
	function gx.allocMap(id,w,h)
		checkArg(1, id, "number")
		checkArg(2, w, "number")
		checkArg(3, h, "number")
		gx.ensureFits(6)
		gxdev.writeByte(GX_ALLOC_MAP,id)
		gxdev.writeShort(w,h)
	end

	function gx.uploadMap(id,w,h,data)
		checkArg(1, id, "number")
		checkArg(2, w, "number")
		checkArg(3, h, "number")
		checkArg(4, data, "string")
		--this method can upload data if the fifo is overfilled by the map--
		local idx = 1
		local fifoSize = gxdev.getFifoSize()
		local function process()
			--first, try to upload all the data--
			local ffu = gxdev.getFifoUsage()+6+(#data-idx+1)
			if ffu < fifoSize then
				--we can upload the rest here
				gxdev.writeByte(GX_UPLOAD_MAP, id)
				gxdev.writeShort(w)
				gxdev.writeShort(h)
				local d = data:sub(idx,#data)
				gxdev.writeShort(#d)
				gxdev.writeBytes(d)
				return false
			else
				--we won't fit!
				local canWrite = fifoSize-ffu
				if canWrite < 9 then
					--we can't put any map data in this fifo!
					--upload it
					gxdev.upload()
					return process()
				end
				local canWriteOfData = canWrite-8
				local d = data:sub(idx,idx+canWriteOfData)
				idx = idx+canWriteOfData
				gxdev.writeByte(GX_UPLOAD_MAP, id)
				gxdev.writeShort(w)
				gxdev.writeShort(h)
				gxdev.writeShort(#d)
				gxdev.writeBytes(d)
				gxdev.upload()
				return true
			end
		end
		while process() do end
	end

	function gx.setMapVariable(id,idx,a,b,c,d,e,f,g)
		checkArg(1, id, "number")
		checkArg(2, idx, "number")
		if idx == GX_MAP_VAR_X or idx == GX_MAP_VAR_Y then
			checkArg(3, a, "number")
			gx.ensureFits(5)
			gxdev.writeByte(GX_SET_MAP_VAR,id,idx)
			gxdev.writeShort(a)
		elseif idx == GX_MAP_VAR_XY then
			checkArg(3, a, "number")
			checkArg(4, b, "number")
			gx.ensureFits(9)
			gxdev.writeByte(GX_SET_MAP_VAR,id,idx)
			gxdev.writeShort(a,b)
		elseif idx == GX_MAP_VAR_COLOR then
			checkArg(3, a, "number")
			checkArg(4, b, "number")
			checkArg(5, c, "number")
			checkArg(6, d, "number", "nil")
			d = d or 255
			gx.ensureFits(7)
			gxdev.writeByte(GX_SET_MAP_VAR,id,idx)
			gxdev.writeByte(d,a,b,c)
		end
	end

	function gx.clearMap(id)
		checkArg(1, id, "number")
		gx.ensureFits(2)
		gxdev.writeByte(GX_CLEAR_MAP,id)
	end

	local function s2n(s)
		return type(s) == "string" and s:byte() or s
	end

	gx.plots = nil
	function gx.startPlot(mapid)
		checkArg(1, mapid, "number")
		gx.plots = {mapid=mapid}
	end
	function gx.plot(x,y,t)
		if not gx.plots then error("Plot not started!") end
		checkArg(1, x, "number")
		checkArg(2, y, "number")
		checkArg(3, t, "number", "string")
		gx.plots[#gx.plots+1] = {x-1,y-1,s2n(t)}
	end
	function gx.endPlot()
		--do it just like uploadMap :D
		local function process()
			gx.ensureFits(8)
			if gx.isAvailable(3+(#gx.plots*5)) then
				gxdev.writeByte(GX_PLOT_MAP,gx.plots.mapid)
				gxdev.writeShort(#gx.plots)
				for i=1, #gx.plots do
					local v = gx.plots[i]
					gxdev.writeShort(v[1],v[2])
					gxdev.writeByte(v[3])
				end
				return false
			else
				--stagger upload
				print("Stagger uploading map...")
				local avail = gxdev.getFifoSize()-gxdev.getFifoUsage()
				local canUpload = math.floor((avail-3)/5)
				gxdev.writeByte(GX_PLOT_MAP,gx.plots.mapid)
				gxdev.writeShort(canUpload)
				local written = 0
				while written < canUpload do
					local v = table.remove(gx.plots,1)
					gxdev.writeShort(v[1],v[2])
					gxdev.writeByte(v[3])
					written = written+1
				end
				gxdev.upload()
				return true
			end
		end
		while process() do end
		gx.plots = nil
	end

	function gx.findReplaceMap(id,...)
		checkArg(1, id, "number")
		local fr = {...}
		assert(#fr%2 == 0, "number of find replaces must be an even number")
		for i=1, #fr, 2 do
			checkArg(i+1, fr[i], "number", "string")
			checkArg(i+2, fr[i+1], "number", "string")
		end
		gx.ensureFits(4+#fr)
		gxdev.writeByte(GX_FIND_REPLACE_MAP,id)
		gxdev.writeShort(#fr/2)
		for i=1, #fr, 2 do
			gxdev.writeByte(s2n(fr[i]),s2n(fr[i+1]))
		end
	end

	--[[
	GX_SPRITE_VAR_X = 0;
	GX_SPRITE_VAR_Y = 1;
	GX_SPRITE_VAR_XY = 2;
	GX_SPRITE_VAR_W = 3;
	GX_SPRITE_VAR_H = 4;
	GX_SPRITE_VAR_WH = 5;
	GX_SPRITE_VAR_IX = 6;
	GX_SPRITE_VAR_IY = 7;
	GX_SPRITE_VAR_IXIY = 8;
	GX_SPRITE_VAR_IXIYWH = 9;
	GX_SPRITE_VAR_XYIXIYWH = 10;
	GX_SPRITE_VAR_COLOR = 11;
	GX_SPRITE_VAR_TEX = 12;
	]]
	function gx.addSprite(id)
		checkArg(1, id, "number")
		gx.ensureFits(2)
		gxdev.writeByte(GX_ADD_SPRITE, id-1)
	end

	function gx.setSpriteVariable(id,idx,a,b,c,d,e,f)
		checkArg(1, id, "number")
		checkArg(2, idx, "number")
		gx.ensureFits(19)
		if idx == GX_SPRITE_VAR_X or idx == GX_SPRITE_VAR_Y then
			checkArg(3, a, "number")
			gxdev.writeByte(GX_SET_SPRITE_VAR,id-1,idx)
			gxdev.writeFloat(a)
		elseif idx == GX_SPRITE_VAR_XY then
			checkArg(3, a, "number")
			checkArg(4, b, "number")
			gxdev.writeByte(GX_SET_SPRITE_VAR,id-1,idx)
			gxdev.writeFloat(a,b)
		elseif idx == GX_SPRITE_VAR_W or idx == GX_SPRITE_VAR_H or idx == GX_SPRITE_VAR_IX or idx == GX_SPRITE_VAR_IY then
			checkArg(3, a, "number")
			gxdev.writeByte(GX_SET_SPRITE_VAR,id-1,idx)
			gxdev.writeShort(a)
		elseif idx == GX_SPRITE_VAR_WH or idx == GX_SPRITE_VAR_IXIY then
			checkArg(3, a, "number")
			checkArg(4, b, "number")
			gxdev.writeByte(GX_SET_SPRITE_VAR,id-1,idx)
			gxdev.writeShort(a,b)
		elseif idx == GX_SPRITE_VAR_IXIYWH then
			checkArg(3, a, "number")
			checkArg(4, b, "number")
			checkArg(5, c, "number")
			checkArg(6, d, "number")
			gxdev.writeByte(GX_SET_SPRITE_VAR,id-1,idx)
			gxdev.writeShort(a,b,c,d)
		elseif idx == GX_SPRITE_VAR_XYIXIYWH then
			checkArg(3, a, "number")
			checkArg(4, b, "number")
			checkArg(5, c, "number")
			checkArg(6, d, "number")
			checkArg(7, e, "number")
			checkArg(8, f, "number")
			gxdev.writeByte(GX_SET_SPRITE_VAR,id-1,idx)
			gxdev.writeFloat(a,b)
			gxdev.writeShort(c,d,e,f)
		elseif idx == GX_SPRITE_VAR_COLOR then
			checkArg(3, a, "number")
			checkArg(4, b, "number")
			checkArg(5, c, "number")
			checkArg(6, d, "number")
			gxdev.writeByte(GX_SET_SPRITE_VAR,id-1,idx)
			gxdev.writeByte(a,b,c,d)
		elseif idx == GX_SPRITE_VAR_TEX then
			checkArg(3, a, "number")
			gxdev.writeByte(GX_SET_SPRITE_VAR,id-1,idx)
			gxdev.writeByte(a)
		elseif idx == GX_SPRITE_VAR_MCTEX then
			checkArg(3, a, "string")
			gxdev.writeByte(GX_SET_SPRITE_VAR,id-1,idx,#a)
			gxdev.writeBytes(a)
		end
	end

	function gx.removeSprite(id)
		checkArg(1, id, "number")
		gx.ensureFits(2)
		gxdev.writeByte(GX_REMOVE_SPRITE, id-1)
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
		gxdev.writeFloat(r/255,g/255,b/255,(a or 255)/255)
	end

	function gx.render()
		gxdev.upload()
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
