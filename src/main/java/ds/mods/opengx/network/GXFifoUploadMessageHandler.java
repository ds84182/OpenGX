package ds.mods.opengx.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ds.mods.opengx.OpenGX;
import ds.mods.opengx.component.ComponentGX;

public class GXFifoUploadMessageHandler implements IMessageHandler<GXFifoUploadMessage, IMessage> {

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(GXFifoUploadMessage message,
			MessageContext ctx) {
		World w = OpenGX.proxy.getClientWorld();
		ComponentGX gx = ComponentGX.get(message.uuid, w, message.tier);
		try
		{
			gx.gx.uploadFIFO(ByteStreams.newDataInput(message.data),message.data);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
