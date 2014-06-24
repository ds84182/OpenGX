package ds.mods.opengx.network;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class GXTextureUploadMessage implements IMessage {
	public int x, y, z;
	public byte id, fmt;
	public byte[] data;

	@Override
	public void fromBytes(ByteBuf buf) {
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		id = buf.readByte();
		fmt = buf.readByte();
		int len = buf.readInt();
		data = new byte[len];
		buf.readBytes(data);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeByte(id);
		buf.writeByte(fmt);
		buf.writeInt(data.length);
		buf.writeBytes(data);
	}

}
