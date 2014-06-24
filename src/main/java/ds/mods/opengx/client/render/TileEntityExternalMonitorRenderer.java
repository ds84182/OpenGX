package ds.mods.opengx.client.render;

import java.lang.ref.WeakReference;
import java.util.Iterator;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import ds.mods.opengx.client.RenderToFramebufferOverlay;
import ds.mods.opengx.client.RenderUtils;
import ds.mods.opengx.client.gx.GXFramebuffer;
import ds.mods.opengx.tileentity.TileEntityExternalMonitor;

public class TileEntityExternalMonitorRenderer extends
		TileEntitySpecialRenderer {

	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		TileEntityExternalMonitor tile = (TileEntityExternalMonitor)var1;
		if (tile.mX != 0 || tile.mY != 0)
		{
			tile.fb = null;
			if (tile.putInRenderList)
			{
				Iterator<WeakReference<TileEntityExternalMonitor>> iter = RenderToFramebufferOverlay.monitors.iterator();
				while (iter.hasNext())
				{
					WeakReference<TileEntityExternalMonitor> w = iter.next();
					if (w.get() == tile)
					{
						iter.remove();
						break;
					}
				}
			}
			return;
		}
		if (!tile.putInRenderList)
		{
			tile.putInRenderList = true;
			RenderToFramebufferOverlay.monitors.add(new WeakReference<TileEntityExternalMonitor>(tile));
		}
		
		GL11.glPushMatrix();
		GL11.glTranslated(var2, var4, var6);
		GL11.glScaled(tile.mWidth, tile.mHeight, 1D);
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		RenderUtils.disableLighting();
		GL11.glColor3f(0,0,0);
		
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex3d(0, 0, 0);
		GL11.glVertex3d(0, 1, 0);
		GL11.glVertex3d(1, 1, 0);
		GL11.glVertex3d(1, 0, 0);
		GL11.glEnd();
		if (tile.owner != null)
		{
			if (tile.fb == null)
			{
				tile.fb = new GXFramebuffer(tile.width, tile.height);
			}
			if (tile.fb.width != tile.width || tile.fb.height != tile.height)
			{
				tile.fb = new GXFramebuffer(tile.width, tile.height);
			}
			
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_ALPHA);
			
			tile.fb.bindTexture();
			GL11.glColor3f(1,1,1);
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2d(1D, 0D);
			GL11.glVertex3d(0, 0, 0);
			GL11.glTexCoord2d(1D, 1D);
			GL11.glVertex3d(0, 1, 0);
			GL11.glTexCoord2d(0D, 1D);
			GL11.glVertex3d(1, 1, 0);
			GL11.glTexCoord2d(0D, 0D);
			GL11.glVertex3d(1, 0, 0);
			GL11.glEnd();
			tile.fb.unbindTexture();
		}
		else
		{
			tile.fb = null;
		}
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		RenderUtils.enableLighting();
		
		GL11.glPopMatrix();
	}

}
