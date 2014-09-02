package ds.mods.opengx.component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.WeakHashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import li.cil.oc.api.Network;
import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Callback;
import li.cil.oc.api.network.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;

public class ComponentPROM extends Component implements ManagedEnvironment {

	public static final WeakHashMap<World,HashMap<UUID,ComponentPROM>> serverCGX = new WeakHashMap<World,HashMap<UUID,ComponentPROM>>();
	public static final WeakHashMap<World,HashMap<UUID,ComponentPROM>> clientCGX = new WeakHashMap<World,HashMap<UUID,ComponentPROM>>();
	
	public static ComponentPROM get(UUID uuid, World w, int tier)
	{
		WeakHashMap<World,HashMap<UUID,ComponentPROM>> cgxm = w.isRemote ? clientCGX : serverCGX;
		if (!cgxm.containsKey(w))
		{
			cgxm.put(w, new HashMap<UUID,ComponentPROM>());
		}
		HashMap<UUID,ComponentPROM> m = cgxm.get(w);
		if (!m.containsKey(uuid))
		{
			m.put(uuid, new ComponentPROM(uuid, w, tier));
		}
		return m.get(uuid);
	}
	
	Node node = Network.newNode(this, Visibility.Neighbors).withComponent("prom").create();
	byte[] prom = new byte[0];
	
	public ComponentPROM(UUID uui, World world, int t) {
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
		prom = nbt.getByteArray("data");
		System.err.println("loaded "+prom.length+" bytes from nbt "+prom);
	}

	@Override
	public void save(NBTTagCompound nbt) {
		node.save(nbt);
		nbt.setByteArray("data", prom);
		System.err.println("saved "+prom.length+" bytes to nbt");
		if (nbt != saveUpper && saveUpper != null)
		{
			save(saveUpper);
		}
	}

	@Override
	public boolean canUpdate() {
		return false;
	}

	@Override
	public void update() {

	}
	
	@Callback(direct=true)
	public Object[] get(Context context, Arguments arguments)
	{
		return new Object[]{prom};
	}
	
	@Callback(direct=true)
	public Object[] read(Context context, Arguments arguments)
	{
		int index = arguments.checkInteger(0);
		if (index<1) throw new RuntimeException("Index < 1");
		if (index>prom.length) throw new RuntimeException("Index > "+prom.length);
		index--;
		int n = arguments.checkInteger(1);
		if (n<1) throw new RuntimeException("N < 1");
		if (index+n>prom.length) n = prom.length-index;
		return new Object[]{Arrays.copyOfRange(prom, index, index+n)};
	}
	
	@Callback(direct=true)
	public Object[] size(Context context, Arguments arguments)
	{
		return new Object[]{prom.length};
	}
	
	@Callback(direct=true)
	public Object[] set(Context context, Arguments arguments)
	{
		byte[] nprom = arguments.checkByteArray(0);
		if (nprom.length > 16384) throw new RuntimeException("attempt to write "+nprom.length+" bytes (limit 16384)");
		prom = nprom;
		return null;
	}
	
	@Callback(direct=true)
	public Object[] log(Context context, Arguments arguments)
	{
		System.err.println(arguments.checkString(0));
		return null;
	}

}
