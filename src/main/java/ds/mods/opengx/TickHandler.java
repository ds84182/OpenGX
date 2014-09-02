package ds.mods.opengx;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Type;

public class TickHandler {
	@SubscribeEvent
	public void tick(TickEvent.ServerTickEvent ev)
	{
		Glasses.updateAll();
	}
}
