package ds.mods.opengx.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import ds.mods.opengx.tileentity.TileEntityMonitor;

public class MonitorContainer extends Container {
	TileEntityMonitor tile;
	
	public MonitorContainer(TileEntityMonitor t)
	{
		super();
		tile = t;
	}

	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		return tile.isUseableByPlayer(var1);
	}

}
