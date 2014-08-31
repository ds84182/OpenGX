package ds.mods.opengx.driver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import li.cil.oc.api.Machine;
import li.cil.oc.api.machine.Owner;
import li.cil.oc.api.network.Node;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class Glasses implements Owner {
	public static HashMap<UUID, Glasses> map = new HashMap<UUID, Glasses>();
	
	public long lastUpdate = Long.MAX_VALUE;
	public li.cil.oc.api.machine.Machine machine = Machine.create(this, Machine.LuaArchitecture);
	public EntityPlayer holder;
	
	public Glasses(EntityPlayer h)
	{
		this.holder = h;
	}
	
	public void update()
	{
		lastUpdate = System.currentTimeMillis();
	}
	
	public void turnOff()
	{
		
	}
	
	public static void updateAll()
	{
		Iterator<Entry<UUID, Glasses>> iter = map.entrySet().iterator();
		while (iter.hasNext())
		{
			Entry<UUID, Glasses> e = iter.next();
			if ((System.currentTimeMillis() - e.getValue().lastUpdate) > 300000)
			{
				//turn off!
				e.getValue().turnOff();
				iter.remove();
			}
		}
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
		return 128000;
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
}
