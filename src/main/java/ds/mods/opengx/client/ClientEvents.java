package ds.mods.opengx.client;

import static ds.mods.opengx.client.Keybindings.buttonMap;
import static ds.mods.opengx.client.Keybindings.buttonPressStart;
import static ds.mods.opengx.client.Keybindings.buttonSet;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Type;
import ds.mods.opengx.Glasses;
import ds.mods.opengx.OpenGX;
import ds.mods.opengx.client.gx.GXFramebuffer;
import ds.mods.opengx.component.ComponentMonitor;
import ds.mods.opengx.items.ItemGlasses;
import ds.mods.opengx.network.GlassesButtonEventMessage;
import ds.mods.opengx.network.GlassesButtonEventMessage.Button;

public class ClientEvents {
	public static ArrayList<WeakReference<ComponentMonitor>> monitors = new ArrayList<WeakReference<ComponentMonitor>>();

	@SubscribeEvent
	public void renderFramebuffersOverlay(RenderGameOverlayEvent.Post event)
	{
		if (event.type != ElementType.ALL) return;
		//cleanse monitor list
		Iterator<WeakReference<ComponentMonitor>> iter = monitors.iterator();
		while (iter.hasNext())
		{
			WeakReference<ComponentMonitor> w = iter.next();
			if (w.get() == null)
			{
				iter.remove();
			}
			else
			{
				ComponentMonitor ex = (ComponentMonitor) w.get();
				if (ex.fb != null && ex.owner != null)
				{
					ex.fb.bind();
					ex.owner.gx.render(ex.width, ex.height);
					ex.fb.unbind();
				}
			}
		}
	}
	
	@SubscribeEvent
	public void renderGlassesOverlay(RenderGameOverlayEvent.Pre event)
	{
		if (event.type != ElementType.CROSSHAIRS) return;
		ItemStack armor = Minecraft.getMinecraft().thePlayer.getCurrentArmor(3);
		if (armor != null && armor.getItem() == OpenGX.iGlasses)
		{
			//render the framebuffer!
			Glasses g = Glasses.get(new UUID(armor.stackTagCompound.getLong("msb"),armor.stackTagCompound.getLong("lsb")), Minecraft.getMinecraft().theWorld);
			if (g == null)
			{
				return;
			}
			if (!g.screenOn) return;
			event.setCanceled(true);
			ComponentMonitor mon = g.monitor;
			if (mon.width <= 0 || mon.height <= 0)
			{
				System.out.println("Invalid monitor dimensions! Setting to something sane.");
				mon.width = 128;
				mon.height = 96;
			}
			
			int width = event.resolution.getScaledWidth();
			int height = event.resolution.getScaledHeight();
			float scale = width/(float)Display.getWidth();
			float gscale = 1F/scale;
			float monwidth = mon.width*(mon.width > width || mon.height > height ? scale : 1F);
			float monheight = mon.height*(mon.width > width || mon.height > height ? scale : 1F);
			float x = (width/2F)-(monwidth/2F);
			float y = (height/2F)-(monheight/2F);
			
			if (mon.fb == null)
			{
				mon.fb = new GXFramebuffer(mon.width, mon.height);
				System.out.println("newfb");
			}
			if (mon.fb.width != mon.width || mon.fb.height != mon.height)
			{
				mon.fb = new GXFramebuffer(mon.width, mon.height);
				System.out.println("newfb");
			}
			
			if (mon.owner != null && mon.owner.gx != null)
			{
				mon.fb.bind();
				if (mon.owner.gx.needsRender())
					mon.owner.gx.render(mon.width, mon.height);
				mon.fb.unbind();
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				mon.fb.bindTexture();
				GL11.glPushMatrix();
				//GL11.glScalef(gscale, gscale, gscale);
				RenderUtils.texturedRectangle(x, y, monwidth, monheight, 0F, 1F, 1F, 0F);
				GL11.glPopMatrix();
				mon.fb.unbindTexture();
			}
		}
	}
	
	@SubscribeEvent
	public void guiOpened(GuiOpenEvent ev)
	{
		//reset all keys
		Glasses g = null;
		if (Minecraft.getMinecraft().thePlayer != null)
		{
			ItemStack armor = Minecraft.getMinecraft().thePlayer.getCurrentArmor(3);
			if (armor != null && armor.getItem() == OpenGX.iGlasses)
			{
				g = ItemGlasses.getGlasses(Minecraft.getMinecraft().thePlayer, armor);
			}
		}
		
		for (Button b : Button.values())
		{
			if (buttonMap.get(b) == null) continue;
			if (buttonSet.contains(b))
			{
				buttonSet.remove(b);
				int duration = (int) (System.currentTimeMillis()-buttonPressStart.get(b));
				
				GlassesButtonEventMessage gbem = new GlassesButtonEventMessage();
				gbem.button = b;
				gbem.released = true;
				gbem.duration = duration;
				if (g != null) g.key(gbem);
				OpenGX.network.sendToServer(gbem);
			}
		}
	}
	
	@SubscribeEvent
	public void clientWorldTick(TickEvent.ClientTickEvent ev)
	{
		if (!Minecraft.getMinecraft().isGamePaused())
			Glasses.updateAll();
	}
	
	@SubscribeEvent
	public void clientTick(TickEvent.RenderTickEvent ev)
	{
		if (Minecraft.getMinecraft().inGameHasFocus)
		{
			Glasses g = null;
			if (Minecraft.getMinecraft().thePlayer != null)
			{
				ItemStack armor = Minecraft.getMinecraft().thePlayer.getCurrentArmor(3);
				if (armor != null && armor.getItem() == OpenGX.iGlasses)
				{
					g = ItemGlasses.getGlasses(Minecraft.getMinecraft().thePlayer, armor);
				}
			}
			
			for (Button b : Button.values())
			{
				if (buttonMap.get(b) == null) continue;
				if (buttonSet.contains(b))
				{
					//check if released
					if (!Keyboard.isKeyDown(buttonMap.get(b).getKeyCode()))
					{
						buttonSet.remove(b);
						int duration = (int) (System.currentTimeMillis()-buttonPressStart.get(b));
						
						GlassesButtonEventMessage gbem = new GlassesButtonEventMessage();
						gbem.button = b;
						gbem.released = true;
						gbem.duration = duration;
						if (g != null) g.key(gbem);
						OpenGX.network.sendToServer(gbem);
					}
				}
				else
				{
					if (Keyboard.isKeyDown(buttonMap.get(b).getKeyCode()) && !buttonSet.contains(b))
					{
						buttonSet.add(b);
						buttonPressStart.put(b, System.currentTimeMillis());
						
						GlassesButtonEventMessage gbem = new GlassesButtonEventMessage();
						gbem.button = b;
						gbem.released = false;
						if (g != null) g.key(gbem);
						OpenGX.network.sendToServer(gbem);
					}
				}
			}
		}
	}
}
