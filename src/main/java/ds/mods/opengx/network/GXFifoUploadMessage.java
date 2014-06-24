package ds.mods.opengx.network;

import io.netty.buffer.ByteBuf;

import java.io.ByteArrayOutputStream;

import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class GXFifoUploadMessage implements IMessage {
	public int x, y, z;
	public byte[] data;

	@Override
	public void fromBytes(ByteBuf buf) {
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		int size = buf.readInt();
		data = new byte[size];
		buf.readBytes(data);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeInt(data.length);
		buf.writeBytes(data);
	}

}
