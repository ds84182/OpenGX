package ds.mods.opengx;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;
import ds.mods.opengx.client.gui.GuiMonitor;
import ds.mods.opengx.container.MonitorContainer;
import ds.mods.opengx.tileentity.TileEntityMonitor;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		if (ID == 0)
		{
			TileEntity tile = world.getTileEntity(x, y, z);
			if (tile instanceof TileEntityMonitor)
			{
				return new MonitorContainer((TileEntityMonitor) tile);
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		if (ID == 0)
		{
			TileEntity tile = world.getTileEntity(x, y, z);
			if (tile instanceof TileEntityMonitor)
			{
				return new GuiMonitor(((TileEntityMonitor) tile).mon);
			}
		}
		return null;
	}

}
