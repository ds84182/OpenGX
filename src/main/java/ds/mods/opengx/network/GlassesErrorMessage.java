package ds.mods.opengx.network;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class GlassesErrorMessage implements IMessage {
	public UUID uuid;
	public String error;

	@Override
	public void fromBytes(ByteBuf buf) {
		uuid = new UUID(buf.readLong(),buf.readLong());
		int len = buf.readInt();
		byte[] er = new byte[len];
		buf.readBytes(er);
		error = new String(er);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(uuid.getMostSignificantBits());
		buf.writeLong(uuid.getLeastSignificantBits());
		buf.writeInt(error.length());
		buf.writeBytes(error.getBytes());
	}

}
