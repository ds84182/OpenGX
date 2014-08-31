package ds.mods.opengx.client.gx.tier2;

import java.io.ByteArrayInputStream;
import java.nio.FloatBuffer;
import java.util.zip.GZIPInputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import com.google.common.io.ByteArrayDataInput;

import ds.mods.opengx.client.RenderUtils;
import ds.mods.opengx.client.gx.GXTexture;
import ds.mods.opengx.gx.tier2.GXPolygon;
import ds.mods.opengx.gx.tier2.Tier2GX;
import ds.mods.opengx.util.Ascii85InputStream;

public class ClientTier2GX extends Tier2GX {
	
	public GXTexture[] textures = new GXTexture[serverTextures.length];
	public int displayList = -1;
	
	private void removeDisplayList()
	{
		if (displayList >= 0)
		{
			GL11.glDeleteLists(displayList, 1);
		}
		displayList = -1;
	}
	
	private void addDisplayList()
	{
		displayList = GL11.glGenLists(1);
	}
	
	@Override
	public void uploadFIFO(ByteArrayDataInput fifo, byte[] fifoData) {
		removeDisplayList();
		super.uploadFIFO(fifo, fifoData);
	}

	@Override
	public void uploadTexture(short id, ByteArrayInputStream data, byte format) {
		removeDisplayList();
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
			removeDisplayList();
			//display error
			//fill render with fbwidth and fbheight
			RenderUtils.setColor(255, 0, 0);
			RenderUtils.rectangle(0, 0, fbwidth, fbheight);
			String textFirst = "Oh no! The GX-T2 runtime has encountered an error and needs to stop.";
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
			if (displayList < 0)
			{
				addDisplayList();
				GL11.glNewList(displayList, GL11.GL_COMPILE);
				Tessellator tess = Tessellator.instance;
				for (int i=0; i<nrpolygons; i++)
				{
					GXPolygon p = polygons[i];
					if (p != null)
					{
						byte tex = p.tex;
						int polylen = p.x.length;
						if (tex < 0 || tex >= textures.length)
						{
							GL11.glDisable(GL11.GL_TEXTURE_2D);
						}
						else
						{
							GL11.glEnable(GL11.GL_TEXTURE_2D);
							GXTexture t = textures[tex];
							if (t != null)
								GL11.glBindTexture(GL11.GL_TEXTURE_2D, t.getGlTextureId());
						}
						/*int ptyp = 0;
						if (polylen>=3)
						{
							ptyp = GL11.GL_TRIANGLE_FAN;
						}
						else if (polylen==2)
						{
							ptyp = GL11.GL_LINE;
						}
						else if (polylen==1)
						{
							ptyp = GL11.GL_POINT;
						}*/
						if (polylen >= 3)
						{
							tess.startDrawing(GL11.GL_TRIANGLE_FAN);
							int r = p.color >> 24 & 255;
							int j = p.color >> 16 & 255;
					        int k = p.color >> 8 & 255;
					        int l = p.color & 255;
							GL11.glColor4f(j/255F, k/255F, l/255F, 1.0F);
							for (int v=0; v<polylen; v++)
							{
								tess.addVertexWithUV(p.x[v], p.y[v], 0, p.u[v], p.v[v]);
							}
							tess.draw();
						}
					}
				}
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glEndList();
			}
			if (clear)
			{
				GL11.glClearColor(cR, cG, cB, 1.0f);
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // Clear Screen And Depth Buffer on the fbo to red
			}
			GL11.glCallList(displayList);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		removeDisplayList();
		super.finalize();
	}
	
	
}
