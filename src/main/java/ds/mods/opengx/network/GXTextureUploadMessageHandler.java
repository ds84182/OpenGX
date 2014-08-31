package ds.mods.opengx.network;

import java.io.ByteArrayInputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ds.mods.opengx.component.ComponentGX;
import ds.mods.opengx.tileentity.TileEntityGX;

public class GXTextureUploadMessageHandler implements
		IMessageHandler<GXTextureUploadMessage, IMessage> {

	@Override
	public IMessage onMessage(GXTextureUploadMessage message, MessageContext ctx) {
		World w = Minecraft.getMinecraft().theWorld;
		ComponentGX gx = ComponentGX.get(message.uuid, w, message.tier);
		try
		{
			gx.gx.uploadTexture(message.id, new ByteArrayInputStream(message.data), message.fmt);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
