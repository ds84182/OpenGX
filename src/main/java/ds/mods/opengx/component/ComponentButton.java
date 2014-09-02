package ds.mods.opengx.component;

import java.util.EnumSet;
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
import net.minecraft.world.World;
import ds.mods.opengx.network.GlassesButtonEventMessage.Button;

public class ComponentButton extends Component implements ManagedEnvironment {
	
	public static final WeakHashMap<World,HashMap<UUID,ComponentButton>> serverCGX = new WeakHashMap<World,HashMap<UUID,ComponentButton>>();
	public static final WeakHashMap<World,HashMap<UUID,ComponentButton>> clientCGX = new WeakHashMap<World,HashMap<UUID,ComponentButton>>();
	
	public static ComponentButton get(UUID uuid, World w, int tier)
	{
		WeakHashMap<World,HashMap<UUID,ComponentButton>> cgxm = w.isRemote ? clientCGX : serverCGX;
		if (!cgxm.containsKey(w))
		{
			cgxm.put(w, new HashMap<UUID,ComponentButton>());
		}
		HashMap<UUID,ComponentButton> m = cgxm.get(w);
		if (!m.containsKey(uuid))
		{
			m.put(uuid, new ComponentButton(uuid, w, tier));
		}
		return m.get(uuid);
	}
	
	Node node = Network.newNode(this, Visibility.Neighbors).withComponent("buttons").create();
	public EnumSet<Button> downButtons = EnumSet.noneOf(Button.class);
	
	public ComponentButton(UUID uui, World world, int t) {
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
	
	@Callback(direct=true)
	public Object[] isDown(Context context, Arguments arguments)
	{
		Button b = null;
		String arg = arguments.checkString(0).toLowerCase();
		if (arg.equals("action1"))
		{
			b = Button.ACTION1;
		}
		else if (arg.equals("action2"))
		{
			b = Button.ACTION2;
		}
		else if (arg.equals("actionmod"))
		{
			b = Button.ACTIONM;
		}
		else
			throw new RuntimeException("invalid button id");
		System.out.println(downButtons.contains(b));
		return new Object[]{downButtons.contains(b)};
	}

}
