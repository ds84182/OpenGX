package ds.mods.opengx.network;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;

import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class GlassesComponentUUIDMessage implements IMessage {
	public UUID uuid;
	public HashMap<Integer,Pair<UUID,Integer>> uuids = new HashMap<Integer,Pair<UUID,Integer>>();

	@Override
	public void fromBytes(ByteBuf buf) {
		uuid = new UUID(buf.readLong(),buf.readLong());
		int num = buf.readInt();
		while (num-->0)
		{
			uuids.put(buf.readInt(), Pair.of(new UUID(buf.readLong(),buf.readLong()), buf.readInt()));
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(uuid.getMostSignificantBits());
		buf.writeLong(uuid.getLeastSignificantBits());
		int num = uuids.size();
		buf.writeInt(num);
		for (Entry<Integer,Pair<UUID,Integer>> e : uuids.entrySet())
		{
			buf.writeInt(e.getKey());
			buf.writeLong(e.getValue().getLeft().getMostSignificantBits());
			buf.writeLong(e.getValue().getLeft().getLeastSignificantBits());
			buf.writeInt(e.getValue().getRight());
		}
	}

}
