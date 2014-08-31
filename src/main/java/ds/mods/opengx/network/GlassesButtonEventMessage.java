package ds.mods.opengx.network;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class GlassesButtonEventMessage implements IMessage {
	public Button button;
	public boolean released;
	public int duration;

	@Override
	public void fromBytes(ByteBuf buf) {
		button = Button.values()[buf.readInt()];
		released = buf.readBoolean();
		if (released)
		{
			duration = buf.readInt();
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(button.ordinal());
		buf.writeBoolean(released);
		if (released)
		{
			buf.writeInt(duration);
		}
	}

	public static enum Button {
		ONOFF,
		SCREENPOWER,
		ACTION1,
		ACTION2,
		ACTIONM
	}
}
