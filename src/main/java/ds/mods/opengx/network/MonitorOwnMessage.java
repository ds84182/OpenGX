package ds.mods.opengx.network;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class MonitorOwnMessage implements IMessage {
	public int mx, my, mz;
	public UUID uuid;
	public int tier;
	public boolean hasOwner;

	@Override
	public void fromBytes(ByteBuf buf) {
		mx = buf.readInt();
		my = buf.readInt();
		mz = buf.readInt();
		hasOwner = buf.readBoolean();
		if (hasOwner)
		{
			uuid = new UUID(buf.readLong(), buf.readLong());
			tier = buf.readInt();
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(mx);
		buf.writeInt(my);
		buf.writeInt(mz);
		buf.writeBoolean(hasOwner);
		if (hasOwner)
		{
			buf.writeLong(uuid.getMostSignificantBits());
			buf.writeLong(uuid.getLeastSignificantBits());
			buf.writeInt(tier);
		}
	}

}
