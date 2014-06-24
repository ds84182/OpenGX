package ds.mods.opengx.network;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ds.mods.opengx.tileentity.TileEntityGX;
import ds.mods.opengx.tileentity.TileEntityMonitor;

public class MonitorOwnMessageHandler implements IMessageHandler<MonitorOwnMessage, IMessage> {

	@Override
	public IMessage onMessage(MonitorOwnMessage message, MessageContext ctx) {
		TileEntity tmp;
		tmp = Minecraft.getMinecraft().theWorld.getTileEntity(message.mx, message.my, message.mz);
		if (tmp instanceof TileEntityMonitor)
		{
			TileEntityMonitor mon = (TileEntityMonitor) tmp;
			if (message.hasOwner)
			{
				tmp = Minecraft.getMinecraft().theWorld.getTileEntity(message.ox, message.oy, message.oz);
				if (tmp instanceof TileEntityGX)
				{
					mon.owner = (TileEntityGX) tmp;
				}
			}
			else
			{
				mon.owner = null;
			}
			mon.onChanged();
			mon.getWorldObj().markBlockForUpdate(mon.xCoord, mon.yCoord, mon.zCoord);
		}
		return null;
	}

}
