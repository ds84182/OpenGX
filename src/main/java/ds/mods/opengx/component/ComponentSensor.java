package ds.mods.opengx.component;

import java.util.HashMap;
import java.util.UUID;
import java.util.WeakHashMap;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Callback;
import li.cil.oc.api.network.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import ds.mods.opengx.Glasses;

public class ComponentSensor extends Component implements ManagedEnvironment {
	
	public static final WeakHashMap<World,HashMap<UUID,ComponentSensor>> serverCGX = new WeakHashMap<World,HashMap<UUID,ComponentSensor>>();
	public static final WeakHashMap<World,HashMap<UUID,ComponentSensor>> clientCGX = new WeakHashMap<World,HashMap<UUID,ComponentSensor>>();
	
	public static ComponentSensor get(UUID uuid, World w, int tier)
	{
		WeakHashMap<World,HashMap<UUID,ComponentSensor>> cgxm = w.isRemote ? clientCGX : serverCGX;
		if (!cgxm.containsKey(w))
		{
			cgxm.put(w, new HashMap<UUID,ComponentSensor>());
		}
		HashMap<UUID,ComponentSensor> m = cgxm.get(w);
		if (!m.containsKey(uuid))
		{
			m.put(uuid, new ComponentSensor(uuid, w, tier));
		}
		return m.get(uuid);
	}
	
	Node node = Network.newNode(this, Visibility.Neighbors).withComponent("sensors").create();
	public Glasses own;
	
	public ComponentSensor(UUID uui, World world, int t) {
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
		
	}

	@Override
	public void onMessage(Message message) {
		
	}

	@Override
	public void load(NBTTagCompound nbt) {
		if (node != null)
			node.load(nbt);
	}

	@Override
	public void save(NBTTagCompound nbt) {
		if (node != null)
			node.save(nbt);
	}

	@Override
	public boolean canUpdate() {
		return false;
	}

	@Override
	public void update() {
		
	}
	
	@Callback(direct = true)
	public Object[] gyro(Context context, Arguments arguments)
	{
		return new Object[]{own.holder.rotationPitch, own.holder.rotationYawHead};
	}
	
	@Callback(direct = true)
	public Object[] lookVector(Context context, Arguments arguments)
	{
		Vec3 vec = own.holder.getLookVec();
		return new Object[]{vec.xCoord,vec.yCoord,vec.zCoord};
	}

	@Callback(direct = true)
	public Object[] motion(Context context, Arguments arguments)
	{
		return new Object[]{
				own.holder.posX-own.holder.lastTickPosX+own.holder.motionX,
				own.holder.posY-own.holder.lastTickPosY+own.holder.motionY,
				own.holder.posZ-own.holder.lastTickPosZ+own.holder.motionZ
			};
	}
}
