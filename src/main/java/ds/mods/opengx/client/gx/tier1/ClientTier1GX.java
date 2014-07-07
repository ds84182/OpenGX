package ds.mods.opengx.client.gx.tier1;

import java.io.ByteArrayInputStream;
import java.util.zip.GZIPInputStream;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

import ds.mods.opengx.client.DebugOverlay;
import ds.mods.opengx.client.RenderUtils;
import ds.mods.opengx.client.gx.GXTexture;
import ds.mods.opengx.gx.tier1.GXMap;
import ds.mods.opengx.gx.tier1.GXSprite;
import ds.mods.opengx.gx.tier1.GXTextureSlot;
import ds.mods.opengx.gx.tier1.Tier1GX;
import ds.mods.opengx.util.Ascii85InputStream;

public class ClientTier1GX extends Tier1GX {
	public GXTexture[] textures = new GXTexture[16];
	
	@Override
	public void uploadTexture(short id, ByteArrayInputStream data, byte format) {
		if (format == GX_FMT_BASE85)
		{
			try
			{
				Ascii85InputStream asciiInput = new Ascii85InputStream(data);
				GZIPInputStream compressedInput = new GZIPInputStream(asciiInput);
				int width = (compressedInput.read()<<8)|compressedInput.read();
				int height = (compressedInput.read()<<8)|compressedInput.read();
				if (width > 256 || height > 256)
				{
					compressedInput.close();
					return;
				}
				int[] imd = new int[width*height];
				for (int i=0; i<width*height; i++)
				{
					imd[i] = (compressedInput.read()<<24) | (compressedInput.read()<<16) | (compressedInput.read()<<8) | compressedInput.read();
				}
				compressedInput.close();
				textures[id] = new GXTexture(width, height, imd);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	@Override
	public void render(int fbwidth, int fbheight) {
		if (error < 0)
		{
			//display error
			//fill render with fbwidth and fbheight
			GL11.glClearColor (1.0f, 0.0f, 0.0f, 1.0f);
			GL11.glClear (GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // Clear Screen And Depth Buffer on the fbo to red
			String textFirst = "Oh no! The GX-T1 runtime has encountered an error and needs to stop.";
			int th = Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(textFirst, fbwidth).size();
			Minecraft.getMinecraft().fontRenderer.drawSplitString(textFirst, 0, 0, fbwidth, 0xFFFFFFFF);
			String errorStr = "Error: "+(-error >= errorDescriptions.length ? errorUnknown : errorDescriptions[(-error)-1]);
			Minecraft.getMinecraft().fontRenderer.drawSplitString(errorStr, 0, th*8+2, fbwidth, 0xFFFFFFFF);
			th += Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(errorStr, fbwidth).size();
			if (additionalInfo != null)
			{
				Minecraft.getMinecraft().fontRenderer.drawSplitString(additionalInfo, 0, th*8+4, fbwidth, 0xFFFFFFFF);
			}
		}
		else
		{
			if (clear)
			{
				GL11.glClearColor(cR, cG, cB, 1.0f);
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // Clear Screen And Depth Buffer on the fbo to red
			}
			renderMap(fbwidth,fbheight,0);
			renderMap(fbwidth,fbheight,1);
			renderSprites();
			renderMap(fbwidth,fbheight,2);
			renderMap(fbwidth,fbheight,3);
		}
	}
	
	private void renderSprites()
	{
		GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.5F);
		for (int i=sprites.length-1; i>=0; i--)
		{
			GXSprite spr = sprites[i];
			if (spr == null) continue;
			GXTexture tex = textures[spr.tex];
			if (tex == null) continue;
			float ixu = spr.ix/((float)tex.width);
			float iyv = spr.iy/((float)tex.height);
			float upix = 1/(float)tex.width;
			float vpix = 1/(float)tex.height;
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex.getGlTextureId());
			RenderUtils.texturedRectangle(spr.x, spr.y, spr.w, spr.h, ixu, iyv, ixu+(upix*spr.w), iyv+(vpix*spr.h), spr.color);
		}
	}
	
	private void renderMap(int fbwidth, int fbheight, int mid)
	{
		if (maps[mid] != null && textureSlots[mid] != null)
		{
			GXMap map = maps[mid];
			GXTextureSlot texslot = textureSlots[mid];
			if (textures[texslot.texid] != null)
			{
				GXTexture tex = textures[texslot.texid];
				float tilesize = texslot.tilesize;
				int textilew = (int) Math.ceil(tex.width/tilesize);
				int textileh = (int) Math.ceil(tex.height/tilesize);
				float utile = tilesize/(float)tex.width;
				float vtile = tilesize/(float)tex.height;
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex.getGlTextureId());
				for (int i = 0; i < map.width*map.height; i++) {
					short x = (short) (Math.floor(i / map.height));
					short y = (short) (i  % map.height);
					short t = map.getTile(x, y);
					short tx = (short) (t % textileh);
					short ty = (short) (Math.floor(t / textileh));
					//System.out.printf("%d,%d,%d  %d,%d\n",t,tx,ty, textilew,textileh);

					//Render the quad at (x, y)
					RenderUtils.texturedRectangle(x * texslot.tilesize + map.x, y * texslot.tilesize + map.y, texslot.tilesize, texslot.tilesize, tx*utile, ty*vtile, tx*utile+utile, ty*vtile+vtile, map.color);
				}
			}
		}
	}
	
	@Override
	public void reset()
	{
		super.reset();
		for (int i=0; i<16; i++)
		{
			textures[i] = null;
		}
		DebugOverlay.gx = this;
	}
}
