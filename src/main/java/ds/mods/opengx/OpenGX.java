package ds.mods.opengx;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import ds.mods.opengx.blocks.BlockExternalMonitor;
import ds.mods.opengx.blocks.BlockGX;
import ds.mods.opengx.blocks.BlockMonitor;
import ds.mods.opengx.network.GXFifoUploadMessage;
import ds.mods.opengx.network.GXFifoUploadMessageHandler;
import ds.mods.opengx.network.GXTextureUploadMessage;
import ds.mods.opengx.network.GXTextureUploadMessageHandler;
import ds.mods.opengx.network.MonitorOwnMessage;
import ds.mods.opengx.network.MonitorOwnMessageHandler;
import ds.mods.opengx.network.MonitorSizeMessage;
import ds.mods.opengx.network.MonitorSizeMessageHandler;
import ds.mods.opengx.tileentity.TileEntityExternalMonitor;
import ds.mods.opengx.tileentity.TileEntityGX;
import ds.mods.opengx.tileentity.TileEntityMonitor;

@Mod(modid = OpenGX.MODID, version = OpenGX.VERSION, dependencies = "after:OpenComputers")
public class OpenGX {
	public static final String MODID = "OpenGX";
	public static final String VERSION = "indev";
	
	@Instance(value = MODID)
    public static OpenGX instance;
	
	@SidedProxy(clientSide="ds.mods.opengx.client.ClientProxy", serverSide="ds.mods.opengx.CommonProxy")
    public static CommonProxy proxy;
	
	public static SimpleNetworkWrapper network;
	
	public static BlockGX bGX;
	public static BlockMonitor bMonitor;
	public static BlockExternalMonitor bExMonitor;
	
	public static CreativeTabs tab = new CreativeTabs("OpenGX") {
		
		@Override
		public Item getTabIconItem() {
			return Item.getItemFromBlock(bGX);
		}
	};
	
	@EventHandler
    public void preinit(FMLPreInitializationEvent event)
    {
		bGX = new BlockGX(Material.iron);
		GameRegistry.registerBlock(bGX, "GX");
		GameRegistry.registerTileEntity(TileEntityGX.class, "GX");
		
		bMonitor = new BlockMonitor(Material.iron);
		GameRegistry.registerBlock(bMonitor, "Monitor");
		GameRegistry.registerTileEntity(TileEntityMonitor.class, "Monitor");
		
		bExMonitor = new BlockExternalMonitor(Material.iron);
		GameRegistry.registerBlock(bExMonitor, "ExMonitor");
		GameRegistry.registerTileEntity(TileEntityExternalMonitor.class, "ExMonitor");
    }
	
	@EventHandler
    public void init(FMLInitializationEvent event)
    {
		network = new SimpleNetworkWrapper("OpenGX");
		network.registerMessage(GXFifoUploadMessageHandler.class, GXFifoUploadMessage.class, 0, Side.CLIENT);
		network.registerMessage(GXTextureUploadMessageHandler.class, GXTextureUploadMessage.class, 1, Side.CLIENT);
		network.registerMessage(MonitorOwnMessageHandler.class, MonitorOwnMessage.class, 2, Side.CLIENT);
		network.registerMessage(MonitorSizeMessageHandler.class, MonitorSizeMessage.class, 3, Side.CLIENT);
		
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
		MinecraftForge.EVENT_BUS.register(new Events());
		
		proxy.registerRenderers();
    }
}
