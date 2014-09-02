package ds.mods.opengx.tileentity;

import java.util.UUID;

import li.cil.oc.api.machine.Owner;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.TileEntityEnvironment;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ds.mods.opengx.component.ComponentGX;

/**
 * every GX tier utilizes the same tile entity
 * @author ds84182
 *
 */
public class TileEntityGX extends TileEntityEnvironment {
	public boolean initd = false;
	
	public int tier;
	public int metadataUpdateCountdown = 0;
	public ComponentGX component;
	public UUID uuid = UUID.randomUUID();
	
	public void init()
	{
		System.out.println("INIT "+tier);
		initd = true;
		component = null;
		//tier = worldObj.getBlockMetadata(xCoord, yCoord, zCoord)+1;
	}
    
    private double distance(TileEntity te)
    {
    	double dx = xCoord+te.xCoord;
    	double dy = yCoord+te.yCoord;
    	double dz = zCoord+te.zCoord;
    	
    	return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }
	
	@Override
	public void updateEntity()
	{
		if (!initd)
			init();
		if (component == null)
		{
			component = ComponentGX.get(uuid, worldObj, tier);
			node = component.node();
			component.own = new Owner()
			{

				@Override
				public Node node() {
					return component.node();
				}

				@Override
				public boolean canInteract(String player) {
					return true;
				}

				@Override
				public boolean isRunning() {
					return false;
				}

				@Override
				public boolean isPaused() {
					return false;
				}

				@Override
				public boolean start() {
					return false;
				}

				@Override
				public boolean pause(double seconds) {
					return false;
				}

				@Override
				public boolean stop() {
					return false;
				}

				@Override
				public boolean signal(String name, Object... args) {
					return false;
				}

				@Override
				public int x() {
					return xCoord;
				}

				@Override
				public int y() {
					return yCoord;
				}

				@Override
				public int z() {
					return zCoord;
				}

				@Override
				public World world() {
					return worldObj;
				}

				@Override
				public int installedMemory() {
					return 0;
				}

				@Override
				public int maxComponents() {
					return 0;
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
				
			};
		}
		super.updateEntity();
		component.update();
		if (metadataUpdateCountdown-- == 0)
		{
			metadataUpdateCountdown = 100;
			tier = this.getBlockMetadata()+1;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		uuid = new UUID(nbt.getLong("msb"),nbt.getLong("lsb"));
		init();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if (!initd)
			init();
		nbt.setLong("lsb", uuid.getLeastSignificantBits());
		nbt.setLong("msb", uuid.getMostSignificantBits());
	}
	
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		this.writeToNBT(nbttagcompound);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 3, nbttagcompound);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		if (worldObj.isRemote)
			readFromNBT(pkt.func_148857_g());
	}
}
