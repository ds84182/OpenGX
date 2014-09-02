package ds.mods.opengx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.WeakHashMap;

import li.cil.oc.api.FileSystem;
import li.cil.oc.api.Machine;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.Container;
import li.cil.oc.api.machine.Owner;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.server.component.WirelessNetworkCard;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.Pair;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import ds.mods.opengx.component.ComponentButton;
import ds.mods.opengx.component.ComponentGX;
import ds.mods.opengx.component.ComponentMonitor;
import ds.mods.opengx.component.ComponentPROM;
import ds.mods.opengx.network.GlassesButtonEventMessage;
import ds.mods.opengx.network.GlassesComponentUUIDMessage;
import ds.mods.opengx.network.GlassesErrorMessage;

public class Glasses implements Owner, Container {
	public static WeakHashMap<World, ArrayList<Glasses>> svmap = new WeakHashMap<World, ArrayList<Glasses>>();
	public static WeakHashMap<World, ArrayList<Glasses>> clmap = new WeakHashMap<World, ArrayList<Glasses>>();

	public static WeakHashMap<World, ArrayList<Glasses>> getMap()
	{
		return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT ? clmap : svmap;
	}

	public static Glasses get(UUID uuid, World w) {
		WeakHashMap<World, ArrayList<Glasses>> map = getMap();
		Glasses g = null;
		if (map.get(w) == null)
		{
			System.out.println("NO ARRAY LIST");
			return null;
		}
		for (Glasses _g : map.get(w))
		{
			if (_g.uuid.equals(uuid))
			{
				g = _g;
				break;
			}
		}
		return g;
	}

	public static void updateAll()
	{
		Iterator<Entry<World, ArrayList<Glasses>>> iter = getMap().entrySet().iterator();
		while (iter.hasNext())
		{
			Entry<World, ArrayList<Glasses>> e = iter.next();
			Iterator<Glasses> giter = e.getValue().iterator();
			while (giter.hasNext())
			{
				Glasses g = giter.next();
				if (g.hasntUpdatedIn++ > 200)
				{
					//turn off!
					g.turnOff();
					giter.remove();
				}
			}
		}
	}

	public int hasntUpdatedIn = 0;
	public li.cil.oc.api.machine.Machine machine = null;
	public EntityPlayer holder;
	public ItemStack stack;
	public UUID uuid = UUID.randomUUID();

	public ComponentButton buttons;
	public ComponentGX gx;
	public ComponentMonitor monitor;
	public ComponentPROM prom;
	public ManagedEnvironment prombooter;
	public WirelessNetworkCard networkCard;

	public int save = 0;
	public boolean handledError = false;
	public boolean screenOn = true;

	public Glasses(EntityPlayer h, ItemStack stack)
	{
		if (getMap().get(h.worldObj) == null)
			getMap().put(h.worldObj, new ArrayList<Glasses>());
		getMap().get(h.worldObj).add(this);

		this.holder = h;
		this.stack = stack;

		NBTTagCompound nbt = stack.getTagCompound();
		uuid = new UUID(nbt.getLong("msb"), nbt.getLong("lsb"));

		buttons = ComponentButton.get(UUID.randomUUID(), h.worldObj, 0);
		gx = ComponentGX.get(UUID.randomUUID(), h.worldObj, 1);
		monitor = ComponentMonitor.get(UUID.randomUUID(), h.worldObj, 0);
		prom = ComponentPROM.get(UUID.randomUUID(), h.worldObj, 0);
		prombooter = FileSystem.asManagedEnvironment(FileSystem.fromClass(OpenGX.class, "opengx", "lua/bootprom"), "prombooter");
		networkCard = new WirelessNetworkCard(this);
		monitor.onChanged();

		loadDataFromNBT();

		if (!h.worldObj.isRemote)
		{
			machine = Machine.create(this, Machine.LuaArchitecture);
			Network.joinNewNetwork(machine.node());

			machine.node().network().connect(machine.node(), buttons.node());
			machine.node().network().connect(machine.node(), gx.node());
			machine.node().network().connect(machine.node(), monitor.node());
			machine.node().network().connect(machine.node(), prom.node());
			machine.node().network().connect(machine.node(), prombooter.node());
			machine.node().network().connect(machine.node(), networkCard.node());

			machine.node().load(nbt.getCompoundTag("node"));
			machine.load(nbt.getCompoundTag("machine"));
			machine.start();
		}
	}

	public void loadDataFromNBT()
	{
		NBTTagCompound nbt = stack.getTagCompound();

		buttons.load(nbt.getCompoundTag("buttons"));
		gx.load(nbt.getCompoundTag("gx"));
		monitor.load(nbt.getCompoundTag("monitor"));
		prom.load(nbt.getCompoundTag("prom"));
		prombooter.load(nbt.getCompoundTag("prombooter"));
		networkCard.load(nbt.getCompoundTag("networkCard"));

		fixOwnership();
	}

	public void fixOwnership()
	{
		buttons.own = this;
		gx.own = this;
		monitor.own = this;
		prom.own = this;
	}

	public void update()
	{
		hasntUpdatedIn = 0;

		buttons.update();
		gx.update();
		monitor.update();
		prom.update();
		prombooter.update();
		networkCard.update();

		if (!holder.worldObj.isRemote)
		{
			if (machine.lastError() != null && !handledError)
			{
				handledError = true;
				GlassesErrorMessage gem = new GlassesErrorMessage();
				gem.uuid = uuid;
				gem.error = machine.lastError();
				OpenGX.network.sendToAllAround(gem, new TargetPoint(holder.worldObj.provider.dimensionId, holder.posX, holder.posY, holder.posZ, 64));
			}

			machine.update();
			if (--save <= 0)
			{
				handledError = false; //so it can be resent next tick
				save = 100;

				{
					NBTTagCompound nbt = new NBTTagCompound();
					machine.save(nbt);
					stack.getTagCompound().setTag("machine", nbt);
				}

				{
					NBTTagCompound nbt = new NBTTagCompound();
					machine.node().save(nbt);
					stack.getTagCompound().setTag("node", nbt);
				}

				{
					NBTTagCompound nbt = new NBTTagCompound();
					buttons.save(nbt);
					stack.getTagCompound().setTag("buttons", nbt);
				}

				{
					NBTTagCompound nbt = new NBTTagCompound();
					gx.save(nbt);
					stack.getTagCompound().setTag("gx", nbt);
				}

				{
					NBTTagCompound nbt = new NBTTagCompound();
					monitor.save(nbt);
					stack.getTagCompound().setTag("monitor", nbt);
				}

				{
					NBTTagCompound nbt = new NBTTagCompound();
					prom.save(nbt);
					stack.getTagCompound().setTag("prom", nbt);
				}

				{
					NBTTagCompound nbt = new NBTTagCompound();
					prombooter.save(nbt);
					stack.getTagCompound().setTag("prombooter", nbt);
				}

				{
					NBTTagCompound nbt = new NBTTagCompound();
					networkCard.save(nbt);
					stack.getTagCompound().setTag("networkCard", nbt);
				}

				stack.getTagCompound().setBoolean("valid", true);

				//upload the uuids to the client
				GlassesComponentUUIDMessage gcum = new GlassesComponentUUIDMessage();
				gcum.uuid = uuid;
				gcum.uuids.put(0, Pair.of(gx.uuid, gx.tier));
				gcum.uuids.put(1, Pair.of(monitor.uuid, monitor.tier));
				gcum.uuids.put(2, Pair.of(prom.uuid, prom.tier));
				OpenGX.network.sendToAllAround(gcum, new TargetPoint(holder.worldObj.provider.dimensionId, holder.posX, holder.posY, holder.posZ, 64));
			}
		}
	}

	public void turnOff()
	{
		save = 0;
		update();
	}

	@Override
	public Node node() {
		return machine.node();
	}

	@Override
	public boolean canInteract(String player) {
		return machine.canInteract(player);
	}

	@Override
	public boolean isRunning() {
		return machine.isRunning();
	}

	@Override
	public boolean isPaused() {
		return machine.isPaused();
	}

	@Override
	public boolean start() {
		return machine.start();
	}

	@Override
	public boolean pause(double seconds) {
		return machine.pause(seconds);
	}

	@Override
	public boolean stop() {
		return machine.stop();
	}

	@Override
	public boolean signal(String name, Object... args) {
		return machine.signal(name, args);
	}

	@Override
	public int x() {
		return (int) holder.posX;
	}

	@Override
	public int y() {
		return (int) (holder.posY+1);
	}

	@Override
	public int z() {
		return (int) holder.posZ;
	}

	@Override
	public World world() {
		return holder.worldObj;
	}

	@Override
	public int installedMemory() {
		return 196000;
	}

	@Override
	public int maxComponents() {
		return 8;
	}

	@Override
	public void markAsChanged() {

	}

	@Override
	public void onMachineConnect(Node node) {

	}

	@Override
	public void onMachineDisconnect(Node node) {

	}

	public void key(GlassesButtonEventMessage message) {
		//key pressed
		if (!message.released)
			buttons.downButtons.add(message.button);
		else
			buttons.downButtons.remove(message.button);
		switch (message.button)
		{
		case ACTION1:
			if (!world().isRemote)
			{
				machine.signal("button", "action1", !message.released);
			}
			break;
		case ACTION2:
			if (!world().isRemote)
			{
				machine.signal("button", "action2", !message.released);
			}
			break;
		case ACTIONM:
			if (!world().isRemote)
			{
				machine.signal("button", "actionmod", !message.released);
			}
			break;
		case ONOFF:
			//quick click to turn it on, long click to turn off
			if (message.released && message.duration > 1000)
			{
				gx.gx.reset();
				if (!world().isRemote)
				{
					if (machine.isRunning())
						machine.stop();
				}
			}
			else if (message.released)
			{
				gx.gx.reset();
				if ((!world().isRemote))
				{
					if (machine.isRunning())
						machine.stop();
					machine.start();
					handledError = false;
				}
			}
			break;
		case SCREENPOWER:
			if (!message.released)
				screenOn = !screenOn;
			break;
		default:
			break;
		}
	}

	@Override
	public double xPosition() {
		return holder.posX;
	}

	@Override
	public double yPosition() {
		return holder.posY;
	}

	@Override
	public double zPosition() {
		return holder.posZ;
	}

	@Override
	public void markChanged() {
		save = 0;
	}
}
