package ds.mods.opengx.network;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ds.mods.opengx.OpenGX;
import ds.mods.opengx.component.ComponentMonitor;
import ds.mods.opengx.tileentity.TileEntityMonitor;

public class MonitorSizeMessageHandler implements IMessageHandler<MonitorSizeMessage, IMessage> {

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MonitorSizeMessage message, MessageContext ctx) {
		ComponentMonitor mon = ComponentMonitor.get(message.uuid, OpenGX.proxy.getClientWorld(), 1);
		mon.width = message.w;
		mon.height = message.h;
		mon.onChanged();
		return null;
	}

}
