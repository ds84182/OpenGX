package ds.mods.opengx.client;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ds.mods.opengx.tileentity.TileEntityExternalMonitor;

public class RenderToFramebufferOverlay {
	public static ArrayList<WeakReference<TileEntityExternalMonitor>> monitors = new ArrayList<WeakReference<TileEntityExternalMonitor>>();

	@SubscribeEvent
	public void renderOverlay(RenderGameOverlayEvent.Post event)
	{
		//cleanse monitor list
		Iterator<WeakReference<TileEntityExternalMonitor>> iter = monitors.iterator();
		while (iter.hasNext())
		{
			WeakReference<TileEntityExternalMonitor> w = iter.next();
			if (w.get() == null)
			{
				iter.remove();
			}
			else
			{
				TileEntityExternalMonitor ex = (TileEntityExternalMonitor) w.get();
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
