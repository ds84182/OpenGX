package ds.mods.opengx;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler {

	public void registerRenderers() {}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);
		/*if (te != null && te instanceof PrinterTE)
		{
			PrinterTE icte = (PrinterTE) te;
			return new PrinterContainer(player.inventory, icte);
		}
		else
		{
			return null;
		}*/
		return null;
	}
}
