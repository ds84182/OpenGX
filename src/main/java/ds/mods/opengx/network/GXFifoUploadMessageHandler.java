package ds.mods.opengx.network;

import com.google.common.io.ByteStreams;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ds.mods.opengx.tileentity.TileEntityGX;

public class GXFifoUploadMessageHandler implements IMessageHandler<GXFifoUploadMessage, IMessage> {

	@Override
	public IMessage onMessage(GXFifoUploadMessage message,
			MessageContext ctx) {
		World w = Minecraft.getMinecraft().theWorld;
		TileEntity te = w.getTileEntity(message.x, message.y, message.z);
		if (te != null && te instanceof TileEntityGX)
		{
			try
			{
				TileEntityGX teGX = (TileEntityGX) te;
				teGX.gx.uploadFIFO(ByteStreams.newDataInput(message.data));
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return null;
	}

}
