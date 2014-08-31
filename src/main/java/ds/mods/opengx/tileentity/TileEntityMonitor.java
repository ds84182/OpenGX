package ds.mods.opengx.tileentity;

import java.util.UUID;

import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Owner;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.TileEntityEnvironment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import ds.mods.opengx.component.ComponentMonitor;

public class TileEntityMonitor extends TileEntityEnvironment {
	public ComponentMonitor mon;

	public UUID uuid = UUID.randomUUID();
	public ForgeDirection facing = ForgeDirection.NORTH;

	public void makeINE()
	{
		if (mon != null)
			return;
		mon = ComponentMonitor.get(uuid, worldObj, 1);
		node = mon.node();
		mon.own = new Owner()
		{

			@Override
			public Node node() {
				return mon.node();
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

	@Override
	public void updateEntity() {
		if (mon == null)
		{
			makeINE();
		}
		super.updateEntity();
		mon.update();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		facing = ForgeDirection.VALID_DIRECTIONS[nbt.getByte("facing")];
		uuid = new UUID(nbt.getLong("msb"),nbt.getLong("lsb"));
		mon = null;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setByte("facing", (byte) facing.ordinal());
		nbt.setLong("lsb", uuid.getLeastSignificantBits());
		nbt.setLong("msb", uuid.getMostSignificantBits());
	}

	public Packet getDescriptionPacket()
	{
		if (mon != null) mon.setOwner(mon.owner);
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		this.writeToNBT(nbttagcompound);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 3, nbttagcompound);
	}

	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return mon.owner != null && 
				worldObj.getTileEntity(xCoord, yCoord, zCoord) == this &&
				entityplayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		if (worldObj.isRemote)
			readFromNBT(pkt.func_148857_g());
	}

}
