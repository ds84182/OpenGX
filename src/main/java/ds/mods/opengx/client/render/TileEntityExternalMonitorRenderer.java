package ds.mods.opengx.client.render;

import java.lang.ref.WeakReference;
import java.util.Iterator;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import ds.mods.opengx.client.RenderToFramebufferOverlay;
import ds.mods.opengx.client.RenderUtils;
import ds.mods.opengx.client.gx.GXFramebuffer;
import ds.mods.opengx.component.ComponentMonitor;
import ds.mods.opengx.tileentity.TileEntityExternalMonitor;
import ds.mods.opengx.tileentity.TileEntityMonitor;

public class TileEntityExternalMonitorRenderer extends
		TileEntitySpecialRenderer {

	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4,
			double var6, float var8) {
		TileEntityExternalMonitor tile = (TileEntityExternalMonitor)var1;
		ComponentMonitor mon = tile.mon;
		if (mon == null) return;
		if (tile.mX != 0 || tile.mY != 0)
		{
			mon.fb = null;
			if (mon.isInRenderList)
			{
				Iterator<WeakReference<ComponentMonitor>> iter = RenderToFramebufferOverlay.monitors.iterator();
				while (iter.hasNext())
				{
					WeakReference<ComponentMonitor> w = iter.next();
					if (w.get() == mon)
					{
						iter.remove();
						break;
					}
				}
			}
			return;
		}
		if (!mon.isInRenderList)
		{
			mon.isInRenderList = true;
			RenderToFramebufferOverlay.monitors.add(new WeakReference<ComponentMonitor>(mon));
		}
		
		GL11.glPushMatrix();
		GL11.glTranslated(var2, var4, var6);
		int[] dx = tile.facingToOrient[tile.facing.ordinal()][0];
		int[] dy = tile.facingToOrient[tile.facing.ordinal()][1];
		int[] dz = tile.facingToOrient[tile.facing.ordinal()][2];
		GL11.glScaled((tile.mWidth*dx[0])+(tile.mHeight*dx[1]), (tile.mWidth*dy[0])+(tile.mHeight*dy[1]), (tile.mWidth*dz[0])+(tile.mHeight*dz[1]));
		RenderUtils.rotateFromFacing(tile.facing);
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		RenderUtils.disableLighting();
		GL11.glColor3f(0,0,0);
		
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex3d(0, 0, 0);
		GL11.glVertex3d(0, 1, 0);
		GL11.glVertex3d(1, 1, 0);
		GL11.glVertex3d(1, 0, 0);
		GL11.glEnd();
		if (mon.owner != null)
		{
			if (mon.fb == null)
			{
				mon.fb = new GXFramebuffer(mon.width, mon.height);
			}
			if (mon.fb.width != mon.width || mon.fb.height != mon.height)
			{
				mon.fb = new GXFramebuffer(mon.width, mon.height);
			}
			
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_ALPHA);
			
			mon.fb.bindTexture();
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
			mon.fb.unbindTexture();
		}
		else
		{
			mon.fb = null;
		}
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		RenderUtils.enableLighting();
		
		GL11.glPopMatrix();
	}

}
