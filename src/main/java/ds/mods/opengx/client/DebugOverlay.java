package ds.mods.opengx.client;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ds.mods.opengx.client.gx.GXFramebuffer;
import ds.mods.opengx.client.gx.tier1.ClientTier1GX;
import ds.mods.opengx.gx.IGX;

public class DebugOverlay {
	GXFramebuffer overlayFramebuffer;
	public static IGX gx;

	@SubscribeEvent
	public void renderOverlay(RenderGameOverlayEvent.Post event)
	{
		if (event.type == ElementType.ALL)
		{
			if (overlayFramebuffer == null)
			{
				overlayFramebuffer = new GXFramebuffer(128, 128);
			}
			if (gx == null)
			{
				return;
			}
			overlayFramebuffer.bind();
	        gx.render((int)(128), (int)(128));
	        overlayFramebuffer.unbind();
	        GL11.glEnable(GL11.GL_TEXTURE_2D);
	        overlayFramebuffer.bindTexture();
	        RenderUtils.setColor(255, 255, 255);
	        RenderUtils.texturedRectangle(0, 0, 128, 128, 0F, 1F, 1F, 0F);
	        overlayFramebuffer.unbindTexture();
		}
	}

}
