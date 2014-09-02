package ds.mods.opengx.network;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class MonitorSizeMessage implements IMessage {
	public UUID uuid;
	public int w, h;

	@Override
	public void fromBytes(ByteBuf buf) {
		uuid = new UUID(buf.readLong(), buf.readLong());
		w = buf.readInt();
		if (w<=0) w=128;
		h = buf.readInt();
		if (h<=0) h=96;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(uuid.getMostSignificantBits());
		buf.writeLong(uuid.getLeastSignificantBits());
		buf.writeInt(w);
		buf.writeInt(h);
	}

}
