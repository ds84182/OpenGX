package ds.mods.opengx.network;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class GXFifoUploadMessage implements IMessage {
	public UUID uuid;
	public int tier;
	public byte[] data;

	@Override
	public void fromBytes(ByteBuf buf) {
		uuid = new UUID(buf.readLong(), buf.readLong());
		tier = buf.readInt();
		int size = buf.readInt();
		data = new byte[size];
		buf.readBytes(data);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(uuid.getMostSignificantBits());
		buf.writeLong(uuid.getLeastSignificantBits());
		buf.writeInt(tier);
		buf.writeInt(data.length);
		buf.writeBytes(data);
	}

}
