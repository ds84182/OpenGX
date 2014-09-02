package ds.mods.opengx.component;

import java.util.UUID;

import li.cil.oc.api.machine.Owner;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class Component {
	public int tier = 1;
	public World worldObj;
	public UUID uuid;
	public Owner own;
	public NBTTagCompound saveUpper;
	
	public Component(World world, int t)
	{
		worldObj = world;
		uuid = UUID.randomUUID();
		tier = t;
	}
	
	public Component(UUID uui, World world, int t)
	{
		worldObj = world;
		uuid = uui;
		tier = t;
	}
}
