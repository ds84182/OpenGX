package ds.mods.opengx.client;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ds.mods.opengx.tileentity.TileEntityMonitor;

public class RenderToFramebufferOverlay {
	public static ArrayList<WeakReference<TileEntityMonitor>> monitors = new ArrayList<WeakReference<TileEntityMonitor>>();

	@SubscribeEvent
	public void renderOverlay(RenderGameOverlayEvent.Post event)
	{
		//cleanse monitor list
		Iterator<WeakReference<TileEntityMonitor>> iter = monitors.iterator();
		while (iter.hasNext())
		{
			WeakReference<TileEntityMonitor> w = iter.next();
			if (w.get() == null)
			{
				iter.remove();
			}
			else
			{
				TileEntityMonitor ex = (TileEntityMonitor) w.get();
				if (ex.fb != null && ex.owner != null)
				{
					ex.fb.bind();
					ex.owner.gx.render(ex.width, ex.height);
					ex.fb.unbind();
				}
			}
		}
	}
}
