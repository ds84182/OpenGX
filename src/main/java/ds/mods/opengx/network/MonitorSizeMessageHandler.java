package ds.mods.opengx.network;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ds.mods.opengx.tileentity.TileEntityMonitor;

public class MonitorSizeMessageHandler implements IMessageHandler<MonitorSizeMessage, IMessage> {

	@Override
	public IMessage onMessage(MonitorSizeMessage message, MessageContext ctx) {
		TileEntity tile = Minecraft.getMinecraft().theWorld.getTileEntity(message.x, message.y, message.z);
		if (tile instanceof TileEntityMonitor)
		{
			((TileEntityMonitor) tile).width = message.w;
			((TileEntityMonitor) tile).height = message.h;
			((TileEntityMonitor) tile).onChanged();
			System.out.println("Changed");
		}
		return null;
	}

}
