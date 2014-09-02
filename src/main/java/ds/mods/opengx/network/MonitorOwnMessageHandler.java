package ds.mods.opengx.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ds.mods.opengx.OpenGX;
import ds.mods.opengx.component.ComponentGX;
import ds.mods.opengx.component.ComponentMonitor;

public class MonitorOwnMessageHandler implements IMessageHandler<MonitorOwnMessage, IMessage> {

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MonitorOwnMessage message, MessageContext ctx) {
		World w = OpenGX.proxy.getClientWorld();
		ComponentMonitor mon = ComponentMonitor.get(message.muuid, w, 1);
		if (message.hasOwner)
		{
			mon.owner = ComponentGX.get(message.uuid, w, message.tier);
		}
		else
		{
			mon.owner = null;
		}
		mon.onChanged();
		//mon.getWorldObj().markBlockForUpdate(mon.xCoord, mon.yCoord, mon.zCoord);
		return null;
	}

}
