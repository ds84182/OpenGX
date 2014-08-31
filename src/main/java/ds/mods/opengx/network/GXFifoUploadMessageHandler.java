package ds.mods.opengx.network;

import com.google.common.io.ByteStreams;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ds.mods.opengx.component.ComponentGX;
import ds.mods.opengx.tileentity.TileEntityGX;

public class GXFifoUploadMessageHandler implements IMessageHandler<GXFifoUploadMessage, IMessage> {

	@Override
	public IMessage onMessage(GXFifoUploadMessage message,
			MessageContext ctx) {
		World w = Minecraft.getMinecraft().theWorld;
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
