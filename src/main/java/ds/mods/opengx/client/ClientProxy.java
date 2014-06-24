package ds.mods.opengx.client;

import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.registry.ClientRegistry;
import ds.mods.opengx.CommonProxy;
import ds.mods.opengx.client.render.TileEntityExternalMonitorRenderer;
import ds.mods.opengx.tileentity.TileEntityExternalMonitor;

public class ClientProxy extends CommonProxy {
	@Override
	public void registerRenderers()
	{
		//MinecraftForge.EVENT_BUS.register(new DebugOverlay());
		MinecraftForge.EVENT_BUS.register(new RenderToFramebufferOverlay());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityExternalMonitor.class, new TileEntityExternalMonitorRenderer());
	}
}
