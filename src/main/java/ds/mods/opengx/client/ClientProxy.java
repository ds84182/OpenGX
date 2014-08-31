package ds.mods.opengx.client;

import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import ds.mods.opengx.CommonProxy;
import ds.mods.opengx.client.render.TileEntityExternalMonitorRenderer;
import ds.mods.opengx.tileentity.TileEntityExternalMonitor;

public class ClientProxy extends CommonProxy {
	@Override
	public void registerRenderers()
	{
		//MinecraftForge.EVENT_BUS.register(new DebugOverlay());
		ClientEvents ce = new ClientEvents();
		MinecraftForge.EVENT_BUS.register(ce);
		FMLCommonHandler.instance().bus().register(ce);
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityExternalMonitor.class, new TileEntityExternalMonitorRenderer());
		Keybindings.init();
	}
}
