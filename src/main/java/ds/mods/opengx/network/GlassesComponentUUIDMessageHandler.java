package ds.mods.opengx.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ds.mods.opengx.Glasses;
import ds.mods.opengx.OpenGX;
import ds.mods.opengx.component.ComponentGX;
import ds.mods.opengx.component.ComponentMonitor;
import ds.mods.opengx.component.ComponentPROM;

public class GlassesComponentUUIDMessageHandler implements
		IMessageHandler<GlassesComponentUUIDMessage, IMessage> {

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(GlassesComponentUUIDMessage message,
			MessageContext ctx) {
		World w = OpenGX.proxy.getClientWorld();
		Glasses g = Glasses.get(message.uuid, w);
		if (g == null)
		{
			return null;
		}
		//update all component uuids
		g.gx = ComponentGX.get(message.uuids.get(0).getLeft(), w, message.uuids.get(0).getRight());
		g.monitor = ComponentMonitor.get(message.uuids.get(1).getLeft(), w, message.uuids.get(1).getRight());
		g.prom = ComponentPROM.get(message.uuids.get(2).getLeft(), w, message.uuids.get(2).getRight());
		//reload component inside state from nbt
		g.fixOwnership();
		return null;
	}

}
