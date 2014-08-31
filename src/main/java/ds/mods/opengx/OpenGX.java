package ds.mods.opengx;

import java.util.UUID;

import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.Container;
import li.cil.oc.api.driver.Slot;
import li.cil.oc.api.network.ManagedEnvironment;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
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
import ds.mods.opengx.items.ItemGlasses;
import ds.mods.opengx.network.GXFifoUploadMessage;
import ds.mods.opengx.network.GXFifoUploadMessageHandler;
import ds.mods.opengx.network.GXTextureUploadMessage;
import ds.mods.opengx.network.GXTextureUploadMessageHandler;
import ds.mods.opengx.network.GlassesButtonEventMessage;
import ds.mods.opengx.network.GlassesButtonEventMessageHandler;
import ds.mods.opengx.network.GlassesComponentUUIDMessage;
import ds.mods.opengx.network.GlassesComponentUUIDMessageHandler;
import ds.mods.opengx.network.GlassesErrorMessage;
import ds.mods.opengx.network.GlassesErrorMessageHandler;
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
	
	public static ItemGlasses iGlasses;
	
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
		
		iGlasses = new ItemGlasses();
		GameRegistry.registerItem(iGlasses, "Glasses");
    }
	
	@EventHandler
    public void init(FMLInitializationEvent event)
    {
		network = new SimpleNetworkWrapper("OpenGX");
		network.registerMessage(GXFifoUploadMessageHandler.class, GXFifoUploadMessage.class, 0, Side.CLIENT);
		network.registerMessage(GXTextureUploadMessageHandler.class, GXTextureUploadMessage.class, 1, Side.CLIENT);
		network.registerMessage(MonitorOwnMessageHandler.class, MonitorOwnMessage.class, 2, Side.CLIENT);
		network.registerMessage(MonitorSizeMessageHandler.class, MonitorSizeMessage.class, 3, Side.CLIENT);
		network.registerMessage(GlassesComponentUUIDMessageHandler.class, GlassesComponentUUIDMessage.class, 4, Side.CLIENT);
		network.registerMessage(GlassesErrorMessageHandler.class, GlassesErrorMessage.class, 5, Side.CLIENT);
		
		network.registerMessage(GlassesButtonEventMessageHandler.class, GlassesButtonEventMessage.class, 6, Side.SERVER);
		
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
		MinecraftForge.EVENT_BUS.register(new Events());
		
		proxy.registerRenderers();
		
		FMLCommonHandler.instance().bus().register(new TickHandler());
		
		Driver.add(new li.cil.oc.api.driver.Item() {
			
			@Override
			public Slot slot(ItemStack stack) {
				return Slot.Card;
			}
			
			@Override
			public ManagedEnvironment createEnvironment(ItemStack stack,
					Container container) {
				Glasses g = Glasses.get(new UUID(stack.stackTagCompound.getLong("msb"),stack.stackTagCompound.getLong("lsb")), container.world());
				if (g == null) return null;
				return g.prom;
			}

			@Override
			public boolean worksWith(ItemStack stack) {
				return stack.getItem() == iGlasses;
			}

			@Override
			public int tier(ItemStack stack) {
				return 0;
			}

			@Override
			public NBTTagCompound dataTag(ItemStack stack) {
				return stack.stackTagCompound.getCompoundTag("prom");
			}
		});
    }
}
