package ds.mods.opengx.component;

import java.util.HashMap;
import java.util.UUID;
import java.util.WeakHashMap;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ds.mods.opengx.OpenGX;
import ds.mods.opengx.client.gx.GXFramebuffer;
import ds.mods.opengx.network.MonitorOwnMessage;
import ds.mods.opengx.network.MonitorSizeMessage;
import ds.mods.opengx.util.MonitorDiscovery;
import li.cil.oc.api.Network;
import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Callback;
import li.cil.oc.api.network.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ComponentMonitor extends Component implements ManagedEnvironment {

	public static final WeakHashMap<World,HashMap<UUID,ComponentMonitor>> serverCGX = new WeakHashMap<World,HashMap<UUID,ComponentMonitor>>();
	public static final WeakHashMap<World,HashMap<UUID,ComponentMonitor>> clientCGX = new WeakHashMap<World,HashMap<UUID,ComponentMonitor>>();
	
	public static ComponentMonitor get(UUID uuid, World w, int tier)
	{
		WeakHashMap<World,HashMap<UUID,ComponentMonitor>> cgxm = w.isRemote ? clientCGX : serverCGX;
		if (!cgxm.containsKey(w))
		{
			cgxm.put(w, new HashMap<UUID,ComponentMonitor>());
		}
		HashMap<UUID,ComponentMonitor> m = cgxm.get(w);
		if (!m.containsKey(uuid))
		{
			m.put(uuid, new ComponentMonitor(uuid, w, tier));
		}
		return m.get(uuid);
	}
	
	Node node = Network.newNode(this, Visibility.Network).withComponent("gxmonitor").create();
	
	@SideOnly(Side.CLIENT)
	public GXFramebuffer fb;
	public boolean isInRenderList = false;
	
	public ComponentGX owner;
	public int width = 128;
	public int height = 96;
	
	public int countdown = 100;
	
	public Runnable changed;
	
	public ComponentMonitor(UUID uui, World world, int t) {
		super(uui, world, t);
	}

	@Override
	public Node node() {
		return node;
	}

	@Override
	public void onConnect(Node node) {
		
	}

	@Override
	public void onDisconnect(Node node) {
		System.out.println("Dis");
		if (owner != null && owner.node() == node)
		{
			setOwner(null);
		}
	}

	@Override
	public void onMessage(Message message) {
		if (message.name().equals("monitor_discovery") && owner == null)
		{
			((MonitorDiscovery)message.data()[0]).foundMonitors.add(this);
		}
	}

	@Override
	public void load(NBTTagCompound nbt) {
		if (node != null)
			node.load(nbt);
		width = nbt.getInteger("width");
		height = nbt.getInteger("height");
	}

	@Override
	public void save(NBTTagCompound nbt) {
		node.save(nbt);
		nbt.setInteger("width", width);
		nbt.setInteger("height", height);
	}
	
	public void setOwner(ComponentGX gx)
	{
		if (owner != null)
			owner.monitor = null;
		owner = gx;
		MonitorOwnMessage m = new MonitorOwnMessage();
		m.muuid = uuid;
		
		if (gx != null)
		{
			m.hasOwner = true;
			m.uuid = gx.uuid;
			m.tier = gx.tier;
			gx.monitor = this;
			
			if (gx.gx != null)
				gx.gx.requestRerender();
		}
		
		System.out.println("set owner of "+uuid);
		OpenGX.network.sendToAllAround(m, new TargetPoint(worldObj.provider.dimensionId, own.x(), own.y(), own.z(), 64));
	}

	@Override
	public boolean canUpdate() {
		return true;
	}

	@Override
	public void update() {
		if (countdown-- == 0)
		{
			countdown = 100;
			MonitorSizeMessage msm = new MonitorSizeMessage();
			msm.uuid = uuid;
			msm.w = width;
			msm.h = height;
			OpenGX.network.sendToAllAround(msm, new TargetPoint(worldObj.provider.dimensionId, own.x(), own.y(), own.z(), 64));
		}
	}
	
	public void onChanged()
	{
		if (changed != null)
			changed.run();
	}
	
	@Callback(direct=true)
	public Object[] getSize(Context context, Arguments arguments)
	{
		return new Object[]{width, height};
	}
	
	@Callback(direct=true,limit=1)
	public Object[] setSize(Context context, Arguments arguments)
	{
		int w = arguments.checkInteger(0), h = arguments.checkInteger(1);
		if (w<1 || h<1 || w>512 || h>512)
			return new Object[]{false, "Size out of bounds (<0 or >512)"};
		width = w;
		height = h;
		onChanged();
		return new Object[]{true};
	}
}
