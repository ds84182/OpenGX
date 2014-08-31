package ds.mods.opengx.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ds.mods.opengx.Glasses;
import ds.mods.opengx.OpenGX;
import ds.mods.opengx.items.ItemGlasses;

public class GlassesButtonEventMessageHandler implements
		IMessageHandler<GlassesButtonEventMessage, IMessage> {

	@Override
	public IMessage onMessage(GlassesButtonEventMessage message,
			MessageContext ctx) {
		EntityPlayer player = ctx.getServerHandler().playerEntity;
		ItemStack armor = player.getCurrentArmor(3);
		if (armor != null && armor.getItem() == OpenGX.iGlasses)
		{
			Glasses g = ItemGlasses.getGlasses(player, armor);
			if (g != null)
			{
				g.key(message);
			}
		}
		return null;
	}

}
