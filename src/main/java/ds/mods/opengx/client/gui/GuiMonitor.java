package ds.mods.opengx.client.gui;

import li.cil.oc.api.network.Node;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import ds.mods.opengx.client.RenderUtils;
import ds.mods.opengx.client.gx.GXFramebuffer;
import ds.mods.opengx.tileentity.TileEntityMonitor;

public class GuiMonitor extends GuiScreen {
	GXFramebuffer fb;
	TileEntityMonitor mon;

	public GuiMonitor(TileEntityMonitor m)
	{
		super();
		mon = m;
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		super.drawScreen(par1, par2, par3);
		this.drawDefaultBackground();
		float scale = this.width/(float)Display.getWidth();
		float gscale = 1F/scale;
		float monwidth = mon.width*(mon.width > width || mon.height > height ? scale : 1F);
		float monheight = mon.height*(mon.width > width || mon.height > height ? scale : 1F);
		float x = (this.width/2F)-(monwidth/2F);
		float y = (this.height/2F)-(monheight/2F);
		if (mon.width != fb.width || mon.height != fb.height)
		{
			fb = new GXFramebuffer(mon.width, mon.height);
		}
		if (mon.owner != null && mon.owner.gx != null)
		{
			fb.bind();
			mon.owner.gx.render(mon.width, mon.height);
			fb.unbind();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			fb.bindTexture();
			GL11.glPushMatrix();
			//GL11.glScalef(gscale, gscale, gscale);
			RenderUtils.setColor(0,0,0);
			RenderUtils.rectangle(x, y, monwidth, monheight);
			RenderUtils.texturedRectangle(x, y, monwidth, monheight, 0F, 1F, 1F, 0F);
			GL11.glPopMatrix();
			fb.unbindTexture();
		}
	}

	@Override
	public void initGui() {
		fb = new GXFramebuffer(mon.width, mon.height);
		System.out.printf("%d %d\n", mon.width, mon.height);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

}
