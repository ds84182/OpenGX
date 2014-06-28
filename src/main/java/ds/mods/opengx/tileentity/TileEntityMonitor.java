package ds.mods.opengx.tileentity;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Callback;
import li.cil.oc.api.network.Context;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.TileEntityEnvironment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import ds.mods.opengx.OpenGX;
import ds.mods.opengx.network.MonitorOwnMessage;
import ds.mods.opengx.network.MonitorSizeMessage;
import ds.mods.opengx.util.MonitorDiscovery;

public class TileEntityMonitor extends TileEntityEnvironment {
	public TileEntityGX owner;
	public int width = 128;
	public int height = 96;
	
	public int countdown = 100;
	
	public ForgeDirection facing = ForgeDirection.NORTH;
	
	public TileEntityMonitor()
	{
		node = Network.newNode(this, Visibility.Network).withComponent("gxmonitor").create();
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (countdown-- == 0)
		{
			countdown = 100;
			MonitorSizeMessage msm = new MonitorSizeMessage();
			msm.x = xCoord;
			msm.y = yCoord;
			msm.z = zCoord;
			msm.w = width;
			msm.h = height;
			OpenGX.network.sendToAllAround(msm, new TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 64D));
		}
	}

	@Override
	public void onConnect(Node node) {
		super.onConnect(node);
	}

	@Override
	public void onDisconnect(Node node) {
		System.out.println("Dis");
		if (owner != null && owner.node().address().equals(node.address()))
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
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		width = nbt.getInteger("width");
		height = nbt.getInteger("height");
		facing = ForgeDirection.VALID_DIRECTIONS[nbt.getByte("facing")];
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("width", width);
		nbt.setInteger("height", height);
		nbt.setByte("facing", (byte) facing.ordinal());
	}

	public Packet getDescriptionPacket()
	{
		setOwner(owner);
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		this.writeToNBT(nbttagcompound);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 3, nbttagcompound);
	}
	
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
        return owner != null && 
        		worldObj.getTileEntity(xCoord, yCoord, zCoord) == this &&
        		entityplayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
	}
	
	public void setOwner(TileEntityGX o)
	{
		owner = o;
		MonitorOwnMessage m = new MonitorOwnMessage();
		m.mx = this.xCoord;
		m.my = this.yCoord;
		m.mz = this.zCoord;
		
		if (o != null)
		{
			m.hasOwner = true;
			m.ox = o.xCoord;
			m.oy = o.yCoord;
			m.oz = o.zCoord;
		}
		
		OpenGX.network.sendToAllAround(m, new TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 64D));
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

	public void onChanged() {
		
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		if (worldObj.isRemote)
			readFromNBT(pkt.func_148857_g());
	}

}
