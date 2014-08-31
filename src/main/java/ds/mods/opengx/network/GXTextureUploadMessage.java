package ds.mods.opengx.network;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class GXTextureUploadMessage implements IMessage {
	public UUID uuid;
	public int tier;
	public byte id, fmt;
	public byte[] data;

	@Override
	public void fromBytes(ByteBuf buf) {
		uuid = new UUID(buf.readLong(), buf.readLong());
		tier = buf.readInt();
		id = buf.readByte();
		fmt = buf.readByte();
		int len = buf.readInt();
		data = new byte[len];
		buf.readBytes(data);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(uuid.getMostSignificantBits());
		buf.writeLong(uuid.getLeastSignificantBits());
		buf.writeInt(tier);
		buf.writeByte(id);
		buf.writeByte(fmt);
		buf.writeInt(data.length);
		buf.writeBytes(data);
	}

}
