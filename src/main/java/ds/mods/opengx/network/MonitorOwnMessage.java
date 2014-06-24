package ds.mods.opengx.network;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class MonitorOwnMessage implements IMessage {
	public int mx, my, mz;
	public int ox, oy, oz;
	public boolean hasOwner;

	@Override
	public void fromBytes(ByteBuf buf) {
		mx = buf.readInt();
		my = buf.readInt();
		mz = buf.readInt();
		hasOwner = buf.readBoolean();
		if (hasOwner)
		{
			ox = buf.readInt();
			oy = buf.readInt();
			oz = buf.readInt();
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
			buf.writeInt(ox);
			buf.writeInt(oy);
			buf.writeInt(oz);
		}
	}

}
