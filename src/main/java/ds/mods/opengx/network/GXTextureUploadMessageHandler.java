package ds.mods.opengx.network;

import java.io.ByteArrayInputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ds.mods.opengx.tileentity.TileEntityGX;

public class GXTextureUploadMessageHandler implements
		IMessageHandler<GXTextureUploadMessage, IMessage> {

	@Override
	public IMessage onMessage(GXTextureUploadMessage message, MessageContext ctx) {
		World w = Minecraft.getMinecraft().theWorld;
		TileEntity te = w.getTileEntity(message.x, message.y, message.z);
		if (te != null && te instanceof TileEntityGX)
		{
			TileEntityGX teGX = (TileEntityGX) te;
			teGX.gx.uploadTexture(message.id, new ByteArrayInputStream(message.data), message.fmt);
		}
		return null;
	}

}
