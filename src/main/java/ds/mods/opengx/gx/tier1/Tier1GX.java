package ds.mods.opengx.gx.tier1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import ds.mods.opengx.gx.GXServerTexture;
import ds.mods.opengx.gx.IGX;
import ds.mods.opengx.tileentity.TileEntityGX;

public class Tier1GX implements IGX {
	public static final int GX_SET_TEXTURE_SLOT = 1;
	public static final int GX_SET_TEXSLOT_VAR = 2;
	
	public static final int GX_UPLOAD_MAP = 3;
	public static final int GX_SET_MAP_VAR = 4;
	public static final int GX_CLEAR_MAP = 5;
	public static final int GX_PLOT_MAP = 6;
	public static final int GX_FIND_REPLACE_MAP = 7;
	
	public static final int GX_ADD_SPRITE = 8;
	public static final int GX_SET_SPRITE_VAR = 9;
	public static final int GX_REMOVE_SPRITE = 10;
	
	public static final int GX_DISABLE_CLEAR = 11;
	public static final int GX_SET_CLEAR_COLOR = 12;

	public static final int GX_TEXSLOT_VAR_TILESIZE = 0;
	
	public static final int GX_MAP_VAR_X = 0;
	public static final int GX_MAP_VAR_Y = 1;
	public static final int GX_MAP_VAR_XY = 2;
	public static final int GX_MAP_VAR_COLOR = 3;
	
	public static final int GX_SPRITE_VAR_X = 0;
	public static final int GX_SPRITE_VAR_Y = 1;
	public static final int GX_SPRITE_VAR_XY = 2;
	public static final int GX_SPRITE_VAR_W = 3;
	public static final int GX_SPRITE_VAR_H = 4;
	public static final int GX_SPRITE_VAR_WH = 5;
	public static final int GX_SPRITE_VAR_IX = 6;
	public static final int GX_SPRITE_VAR_IY = 7;
	public static final int GX_SPRITE_VAR_IXIY = 8;
	public static final int GX_SPRITE_VAR_IXIYWH = 9;
	public static final int GX_SPRITE_VAR_XYIXIYWH = 10;
	public static final int GX_SPRITE_VAR_COLOR = 11;
	public static final int GX_SPRITE_VAR_TEX = 12;

	public GXServerTexture[] serverTextures = new GXServerTexture[16];
	public GXTextureSlot[] textureSlots = new GXTextureSlot[4];
	public GXMap[] maps = new GXMap[4]; //0 = very back, 3 = front
	public GXSprite[] sprites = new GXSprite[128]; //sprites are drawn between map1 and map2

	public int error = 0;
	public static final int GX_ERROR_NONE = 0;
	public static final int GX_ERROR_TEXTURE_ID_OOR = -1;
	public static final int GX_ERROR_TEXSLOT_ID_OOR = -2;
	public static final int GX_ERROR_TEXSLOT_NOT_INIT = -3;
	public static final int GX_ERROR_TEXSLOT_VAR_UNKNOWN = -4;
	public static final int GX_ERROR_UNKNOWN_COMMAND = -5;
	public static final int GX_ERROR_MAP_ID_OOR = -6;
	public static final int GX_ERROR_MAP_NOT_INIT = -7;
	public static final int GX_ERROR_SPRITE_ID_OOR = -8;
	public static final int GX_ERROR_SPRITE_NOT_INIT = -9;
	
	public static final String errorUnknown = "An unknown error has occured";
	public static final String[] errorDescriptions = {
		"Texture ID out of range",
		"Texture Slot ID out of range",
		"Texture Slot not initialized",
		"Unknown Texture Slot Variable",
		"Unknown Command",
		"Map ID out of range",
		"Map not initialized",
		"Sprite ID out of range",
		"Sprite not initialized"
	};
	public String additionalInfo;
	
	public static final String[] commandNames = {
		"GX_INIT",
		"GX_SET_TEXTURE_SLOT",
		"GX_SET_TEXSLOT_VAR",
		"GX_UPLOAD_MAP",
		"GX_SET_MAP_VAR",
		"GX_CLEAR_MAP",
		"GX_PLOT_MAP",
		"GX_FIND_REPLACE_MAP",
		"GX_ADD_SPRITE",
		"GX_SET_SPRITE_VAR",
		"GX_REMOVE_SPRITE"
	};
	
	public static final int GX_GET_TEXTURE_SLOT = 0;
	public static final int GX_GET_TEXSLOT_VAR = 1;
	public static final int GX_GET_MAP_TILE = 2;
	public static final int GX_GET_MAP_VAR = 3;
	public static final int GX_GET_SPRITE_VAR = 4;
	
	public boolean clear = true;
	public float cR, cG, cB;

	@Override
	public void uploadFIFO(ByteArrayDataInput fifo, byte[] fifoData) {
		byte lastCommand = -1;
		while (true)
		{
			byte b;
			try
			{
				b = fifo.readByte();
			}
			catch(Exception e)
			{
				break;
			}
			if (b == GX_INIT)
			{
				System.out.println("GX_INIT");
				error = 0;
				additionalInfo = null;
				reset();
			}
			else if(b == GX_SET_TEXTURE_SLOT)
			{
				byte slot = fifo.readByte();
				byte tex = fifo.readByte();
				if (slot < 0 || slot >= textureSlots.length)
				{
					error = GX_ERROR_TEXSLOT_ID_OOR;
					additionalInfo = "Happened during a GX_SET_TEXTURE_SLOT command";
					return;
				}
				if (tex < 0 || tex >= 16)
				{
					error = GX_ERROR_TEXTURE_ID_OOR;
					additionalInfo = "Happened during a GX_SET_TEXTURE_SLOT command";
					return;
				}
				textureSlots[slot] = new GXTextureSlot(tex);
			}
			else if(b == GX_SET_TEXSLOT_VAR)
			{
				byte slot = fifo.readByte();
				if (slot < 0 || slot >= textureSlots.length)
				{
					error = GX_ERROR_TEXSLOT_ID_OOR;
					additionalInfo = "Happened during a GX_SET_TEXSLOT_VAR command";
					return;
				}
				GXTextureSlot txs = textureSlots[slot];
				if (txs == null)
				{
					error = GX_ERROR_TEXSLOT_NOT_INIT;
					additionalInfo = "Happened during a GX_SET_TEXSLOT_VAR command";
					return;
				}
				switch (fifo.readByte())
				{
				case(GX_TEXSLOT_VAR_TILESIZE):
					txs.tilesize = fifo.readByte();
				break;
				default:
					error = GX_ERROR_TEXSLOT_VAR_UNKNOWN;
					additionalInfo = "Happened during a GX_SET_TEXSLOT_VAR command";
				return;
				}
			}
			else if (b == GX_UPLOAD_MAP)
			{
				byte mapid = fifo.readByte();
				short w = fifo.readShort(), h = fifo.readShort();
				if (mapid < 0 || mapid >= maps.length)
				{
					error = GX_ERROR_MAP_ID_OOR;
					additionalInfo = "Happened during a GX_UPLOAD_MAP command";
					return;
				}
				if (maps[mapid] == null || maps[mapid].finished)
				{
					System.out.println(w+","+h);
					maps[mapid] = new GXMap(w, h, fifo);
				}
				else
				{
					maps[mapid].feed(fifo);
				}
			}
			else if (b == GX_SET_MAP_VAR)
			{
				byte mapid = fifo.readByte();
				byte idx = fifo.readByte();
				if (mapid < 0 || mapid >= maps.length)
				{
					error = GX_ERROR_MAP_ID_OOR;
					additionalInfo = "Happened during a GX_SET_MAP_VAR command";
					return;
				}
				GXMap map = maps[mapid];
				if (map == null)
				{
					error = GX_ERROR_MAP_NOT_INIT;
					additionalInfo = "Happened during a GX_SET_MAP_VAR command";
					return;
				}
				switch(idx)
				{
				case(GX_MAP_VAR_X):
					map.x = fifo.readShort();
				break;
				case(GX_MAP_VAR_Y):
					map.y = fifo.readShort();
				break;
				case(GX_MAP_VAR_XY):
					map.x = fifo.readShort();
					map.y = fifo.readShort();
				break;
				case(GX_MAP_VAR_COLOR):
					map.color = fifo.readInt();
				break;
				}
			}
			else if (b == GX_CLEAR_MAP)
			{
				byte mapid = fifo.readByte();
				if (mapid < 0 || mapid >= maps.length)
				{
					error = GX_ERROR_MAP_ID_OOR;
					additionalInfo = "Happened during a GX_CLEAR_MAP command";
					return;
				}
				maps[mapid] = null;
			}
			else if (b == GX_PLOT_MAP)
			{
				byte mapid = fifo.readByte();
				if (mapid < 0 || mapid >= maps.length)
				{
					error = GX_ERROR_MAP_ID_OOR;
					additionalInfo = "Happened during a GX_PLOT_MAP command";
					return;
				}
				short plots = fifo.readShort();
				GXMap map = maps[mapid];
				if (map == null)
				{
					error = GX_ERROR_MAP_NOT_INIT;
					additionalInfo = "Happened during a GX_PLOT_MAP command";
					return;
				}
				for (int i=0; i<plots; i++)
				{
					short x = fifo.readShort();
					short y = fifo.readShort();
					short t = (short) (fifo.readByte() & 0xFF);
					map.setTile(x, y, t);
				}
			}
			else if (b == GX_FIND_REPLACE_MAP)
			{
				byte mapid = fifo.readByte();
				if (mapid < 0 || mapid >= maps.length)
				{
					error = GX_ERROR_MAP_ID_OOR;
					additionalInfo = "Happened during a GX_FIND_REPLACE_MAP command";
					return;
				}
				short freps = fifo.readShort();
				GXMap map = maps[mapid];
				if (map == null)
				{
					error = GX_ERROR_MAP_NOT_INIT;
					additionalInfo = "Happened during a GX_FIND_REPLACE_MAP command";
				}
				System.out.println(freps);
				for (int i=0; i<freps; i++)
				{
					short from = (short) (fifo.readByte() & 0xFF);
					short to = (short) (fifo.readByte() & 0xFF);
					for (int e=0; e<map.data.length; e++)
					{
						if (map.data[e] == from)
							map.data[e] = to;
					}
				}
			}
			else if (b == GX_ADD_SPRITE)
			{
				//sprite creation
				//this just makes the sprite
				byte spriteid = fifo.readByte();
				if (spriteid < 0 || spriteid >= sprites.length)
				{
					error = GX_ERROR_SPRITE_ID_OOR;
					additionalInfo = "Happened during a GX_ADD_SPRITE command";
					return;
				}
				sprites[spriteid] = new GXSprite();
			}
			else if (b == GX_SET_SPRITE_VAR)
			{
				//shows up at random and after GX_ADD_SPRITE
				byte spriteid = fifo.readByte();
				if (spriteid < 0 || spriteid >= sprites.length)
				{
					error = GX_ERROR_SPRITE_ID_OOR;
					additionalInfo = "Happened during a GX_SET_SPRITE_VAR command";
					return;
				}
				GXSprite sprite = sprites[spriteid];
				if (sprite == null)
				{
					error = GX_ERROR_SPRITE_NOT_INIT;
					additionalInfo = "Happened during a GX_SET_SPRITE_VAR command";
					return;
				}
				
				byte idx = fifo.readByte();
				if (idx == GX_SPRITE_VAR_X)
				{
					sprite.x = fifo.readFloat(); //yaas, floats
				}
				else if (idx == GX_SPRITE_VAR_Y)
				{
					sprite.y = fifo.readFloat();
				}
				else if (idx == GX_SPRITE_VAR_XY)
				{
					sprite.x = fifo.readFloat();
					sprite.y = fifo.readFloat();
				}
				else if (idx == GX_SPRITE_VAR_W)
				{
					sprite.w = fifo.readShort();
				}
				else if (idx == GX_SPRITE_VAR_W)
				{
					sprite.h = fifo.readShort();
				}
				else if (idx == GX_SPRITE_VAR_WH)
				{
					sprite.w = fifo.readShort();
					sprite.h = fifo.readShort();
				}
				else if (idx == GX_SPRITE_VAR_IX)
				{
					sprite.ix = fifo.readShort();
				}
				else if (idx == GX_SPRITE_VAR_IY)
				{
					sprite.iy = fifo.readShort();
				}
				else if (idx == GX_SPRITE_VAR_IXIY)
				{
					sprite.ix = fifo.readShort();
					sprite.iy = fifo.readShort();
				}
				else if (idx == GX_SPRITE_VAR_IXIYWH)
				{
					sprite.ix = fifo.readShort();
					sprite.iy = fifo.readShort();
					sprite.w = fifo.readShort();
					sprite.h = fifo.readShort();
				}
				else if (idx == GX_SPRITE_VAR_XYIXIYWH)
				{
					sprite.x = fifo.readFloat();
					sprite.y = fifo.readFloat();
					sprite.ix = fifo.readShort();
					sprite.iy = fifo.readShort();
					sprite.w = fifo.readShort();
					sprite.h = fifo.readShort();
				}
				else if (idx == GX_SPRITE_VAR_COLOR)
				{
					sprite.color = fifo.readInt();
				}
				else if (idx == GX_SPRITE_VAR_TEX)
				{
					byte tex = fifo.readByte();
					if (tex < 0 || tex >= 16)
					{
						error = GX_ERROR_TEXTURE_ID_OOR;
						additionalInfo = "Happened during a GX_SET_SPRITE_VAR command";
						return;
					}
					sprite.tex = tex;
				}
			}
			else if (b == GX_REMOVE_SPRITE)
			{
				byte spriteid = fifo.readByte();
				if (spriteid < 0 || spriteid >= sprites.length)
				{
					error = GX_ERROR_SPRITE_ID_OOR;
					additionalInfo = "Happened during a GX_REMOVE_SPRITE command";
					return;
				}
				sprites[spriteid] = null;
			}
			else if (b == GX_DISABLE_CLEAR)
			{
				clear = false;
			}
			else if (b == GX_SET_CLEAR_COLOR)
			{
				clear = true;
				cR = fifo.readFloat();
				cG = fifo.readFloat();
				cB = fifo.readFloat();
			}
			else
			{
				error = GX_ERROR_UNKNOWN_COMMAND;
				additionalInfo = (lastCommand == -1 ? "First GX command" : "After "+commandNames[lastCommand])+" ("+b+")";
				return;
			}
			lastCommand = b;
		}
	}

	@Override
	public void reset() {
		for (int i=0; i<serverTextures.length; i++)
		{
			serverTextures[i] = null;
		}
		
		for (int i=0; i<textureSlots.length; i++)
		{
			textureSlots[i] = null;
		}

		for (int i=0; i<maps.length; i++)
		{
			maps[i] = null;
		}
		
		for (int i=0; i<sprites.length; i++)
		{
			sprites[i] = null;
		}
		clear = true;
		cR = 0.0F;
		cG = 0.0F;
		cB = 0.0F;
	}

	@Override
	public void uploadTexture(short id, ByteArrayInputStream data, byte format) {
		byte[] arr = new byte[data.available()];
		try {
			data.read(arr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		serverTextures[id] = new GXServerTexture(format, arr);
	}

	@Override
	public void render(int fbwidth, int fbheight) {

	}

	@Override
	public String type() {
		return "gxt1";
	}

	@Override
	public int getError() {
		return error;
	}

	@Override
	public String getErrorString() {
		return (-error >= errorDescriptions.length ? errorUnknown : errorDescriptions[-error])+(additionalInfo != null ? " : "+additionalInfo : "");
	}

	@Override
	public Object getValue(int index, int subindex, int supersub, int suprasub) {
		if (index == GX_GET_TEXTURE_SLOT)
		{
			if (subindex < 0 || subindex >= textureSlots.length)
				return null;
			if (textureSlots[subindex] != null)
			{
				return textureSlots[subindex].texid;
			}
		}
		else if (index == GX_GET_TEXSLOT_VAR)
		{
			if (subindex < 0 || subindex >= textureSlots.length)
				return null;
			if (textureSlots[subindex] != null)
			{
				if (supersub == GX_TEXSLOT_VAR_TILESIZE)
				{
					return textureSlots[subindex].tilesize;
				}
			}
		}
		else if (index == GX_GET_MAP_TILE)
		{
			if (subindex < 0 || subindex >= maps.length)
				return null;
			if (maps[subindex] != null)
			{
				return maps[subindex].getTile((short)supersub, (short)suprasub);
			}
		}
		else if (index == GX_GET_MAP_VAR)
		{
			if (subindex < 0 || subindex >= maps.length)
				return null;
			if (maps[subindex] != null)
			{
				if (supersub == GX_MAP_VAR_X)
				{
					return maps[subindex].x;
				}
				else if (supersub == GX_MAP_VAR_Y)
				{
					return maps[subindex].y;
				}
				else if (supersub == GX_MAP_VAR_COLOR)
				{
					return maps[subindex].color;
				}
			}
		}
		else if (index == GX_GET_SPRITE_VAR)
		{
			if (subindex < 0 || subindex >= maps.length)
				return null;
			if (sprites[subindex] != null)
			{
				if (supersub == GX_SPRITE_VAR_COLOR)
				{
					return sprites[subindex].color;
				}
				else if (supersub == GX_SPRITE_VAR_H)
				{
					return sprites[subindex].h;
				}
				else if (supersub == GX_SPRITE_VAR_IX)
				{
					return sprites[subindex].ix;
				}
				else if (supersub == GX_SPRITE_VAR_W)
				{
					return sprites[subindex].w;
				}
				else if (supersub == GX_SPRITE_VAR_IY)
				{
					return sprites[subindex].iy;
				}
				else if (supersub == GX_SPRITE_VAR_X)
				{
					return sprites[subindex].x;
				}
				else if (supersub == GX_SPRITE_VAR_Y)
				{
					return sprites[subindex].y;
				}
				else if (supersub == GX_SPRITE_VAR_W)
				{
					return sprites[subindex].w;
				}
				else if (supersub == GX_SPRITE_VAR_TEX)
				{
					return sprites[subindex].tex;
				}
			}
		}
		return null;
	}

	@Override
	public ArrayList<Pair<DataType, byte[]>> createMegaUpdate() {
		//this is a multi stage method
		//first we make the INIT/TEXSLOT packet. This packet will initialize and send texslots
		//then we make packets for each texture
		//then we make the MAP packet. It contains all the map data
		//then we make then SPRITE packet. It contains all sprite data
		
		ArrayList<Pair<DataType, byte[]>> packets = new ArrayList<Pair<DataType,byte[]>>();
		
		//stage1: INIT and TEXSLOT
		{
			ByteArrayDataOutput bado = ByteStreams.newDataOutput();
			
			bado.writeByte(GX_INIT);
			for (int i=0; i<textureSlots.length; i++)
			{
				GXTextureSlot texslot = textureSlots[i];
				if (texslot != null)
				{
					bado.writeByte(GX_SET_TEXTURE_SLOT);
					bado.writeByte(i);
					bado.writeByte(texslot.texid);
					
					bado.writeByte(GX_SET_TEXSLOT_VAR);
					bado.writeByte(i);
					bado.writeByte(GX_TEXSLOT_VAR_TILESIZE);
					bado.writeByte(texslot.tilesize);
				}
			}
			packets.add(Pair.of(DataType.FIFO, bado.toByteArray()));
		}
		
		//stage2: TEXTURES
		for (int i=0; i<serverTextures.length; i++)
		{
			GXServerTexture tex = serverTextures[i];
			if (tex != null)
			{
				ByteArrayDataOutput bado = ByteStreams.newDataOutput();
				
				bado.writeShort(i);
				bado.writeByte(tex.format);
				bado.writeInt(tex.data.length);
				bado.write(tex.data);
				packets.add(Pair.of(DataType.TEXTURE, bado.toByteArray()));
			}
		}
		
		//stage3: MAP
		{
			ByteArrayDataOutput bado = ByteStreams.newDataOutput();
			
			for (int i=0; i<maps.length; i++)
			{
				GXMap map = maps[i];
				if (map != null && map.finished)
				{
					bado.writeByte(GX_UPLOAD_MAP);
					bado.writeByte(i);
					bado.writeShort(map.width);
					bado.writeShort(map.height);
					int datasize = map.width*map.height;
					if (datasize <= Short.MAX_VALUE)
					{
						bado.writeShort(map.width*map.height);
						for (int n=0; n<datasize; n++)
						{
							bado.writeByte(map.data[n]);
						}
					}
					else
					{
						bado.writeShort(Short.MAX_VALUE);
						for (int n=0; n<Short.MAX_VALUE; n++)
						{
							bado.writeByte(map.data[n]);
						}
						int idx = Short.MAX_VALUE;
						while (idx<datasize)
						{
							bado.writeByte(GX_UPLOAD_MAP);
							bado.writeByte(i);
							bado.writeShort(map.width);
							bado.writeShort(map.height);
							int upld = (idx+Short.MAX_VALUE) < datasize ? Short.MAX_VALUE : (datasize-idx);
							bado.writeShort(upld);
							for (int n=idx; n<idx+upld; n++)
							{
								bado.writeByte(map.data[n]);
							}
							idx+=upld;
						}
					}
				}
			}
			packets.add(Pair.of(DataType.FIFO, bado.toByteArray()));
		}
		
		//stage4: SPRITE
		{
			ByteArrayDataOutput bado = ByteStreams.newDataOutput();
			
			for (int i=0; i<sprites.length; i++)
			{
				GXSprite sprite = sprites[i];
				if (sprite != null)
				{
					bado.writeByte(GX_ADD_SPRITE);
					bado.writeByte(i);
					
					bado.writeByte(GX_SET_SPRITE_VAR);
					bado.writeByte(i);
					bado.writeByte(GX_SPRITE_VAR_XYIXIYWH);
					bado.writeFloat(sprite.x);
					bado.writeFloat(sprite.y);
					bado.writeShort(sprite.ix);
					bado.writeShort(sprite.iy);
					bado.writeShort(sprite.w);
					bado.writeShort(sprite.h);
					
					bado.writeByte(GX_SET_SPRITE_VAR);
					bado.writeByte(i);
					bado.writeByte(GX_SPRITE_VAR_COLOR);
					bado.writeInt(sprite.color);
					
					bado.writeByte(GX_SET_SPRITE_VAR);
					bado.writeByte(i);
					bado.writeByte(GX_SPRITE_VAR_TEX);
					bado.writeByte(sprite.tex);
				}
			}
			packets.add(Pair.of(DataType.FIFO, bado.toByteArray()));
		}
		return packets;
	}
}
