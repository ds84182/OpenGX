package ds.mods.opengx.tileentity;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Iterator;

import li.cil.oc.api.FileSystem;
import li.cil.oc.api.Network;
import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Callback;
import li.cil.oc.api.network.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.TileEntityEnvironment;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import ds.mods.opengx.OpenGX;
import ds.mods.opengx.gx.IGX;
import ds.mods.opengx.network.GXFifoUploadMessage;
import ds.mods.opengx.network.GXTextureUploadMessage;
import ds.mods.opengx.util.MonitorDiscovery;

/**
 * every GX tier utilizes the same tile entity
 * @author ds84182
 *
 */
public class TileEntityGX extends TileEntityEnvironment {
	public IGX gx;
	public ByteArrayDataOutput fifo;
	public int fifoBytes;
	public int fifoSize;
	public int tier = 1;
	public static final String serverGXFormat = "ds.mods.opengx.gx.tier%d.Tier%dGX";
	public static final String clientGXFormat = "ds.mods.opengx.client.gx.tier%d.ClientTier%dGX";
	public ManagedEnvironment romGX;
	public boolean initd = false;
	
	public TileEntityMonitor monitor;
	public String monitorAddress;
	public int monitorDiscoveryCountDown = 10;
	public MonitorDiscovery currentDiscovery;
	public int discoverCountDown = 10;
	public int monitorAddressFailures = 0;
	
	public void init()
	{
		System.out.println("INIT "+tier);
		initd = true;
		//tier = worldObj.getBlockMetadata(xCoord, yCoord, zCoord)+1;
		gx = null;
		fifoSize = tier*4096;
		fifo = ByteStreams.newDataOutput(fifoSize);
		monitorAddressFailures = 0;
		node = Network.newNode(this, Visibility.Network).withComponent("gxt"+tier).create();
		romGX = FileSystem.asManagedEnvironment(FileSystem.fromClass(OpenGX.class, "opengx", "lua/component/"+"gxt"+tier), "gxt"+tier);
	}
	
	@Override
    public void readFromNBT(final NBTTagCompound nbt) {
		tier = nbt.getInteger("tier")+1;
		monitorAddress = nbt.getString("monitor");
		if (monitorAddress.length() == 0)
			monitorAddress = null;
		init();
        super.readFromNBT(nbt);
        if (romGX != null) {
        	romGX.load(nbt.getCompoundTag("oc:romnode"));
        }
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("tier", tier-1);
        if (monitorAddress != null)
        	nbt.setString("monitor",monitorAddress);
        if (romGX != null) {
            final NBTTagCompound nodeNbt = new NBTTagCompound();
            romGX.save(nodeNbt);
            nbt.setTag("oc:romnode",nodeNbt);
        }
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
		if (gx == null)
		{
			try {
				gx = (IGX)Class.forName(String.format(worldObj.isRemote ? clientGXFormat : serverGXFormat, tier, tier)).newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		super.updateEntity();
		if (!worldObj.isRemote)
			if (monitor == null && node != null)
			{
				if (monitorAddress == null)
				{
					if (currentDiscovery == null)
					{
						monitorDiscoveryCountDown--;
						if (monitorDiscoveryCountDown <= 0)
						{
							monitorDiscoveryCountDown = 10;
							discoverCountDown = 10;
							currentDiscovery = new MonitorDiscovery(this);
							node.sendToVisible("monitor_discovery", currentDiscovery);
							System.out.println("DISCOVER");
						}
					}
					else
					{
						discoverCountDown--;
						if (discoverCountDown <= 0)
						{
							TileEntityMonitor closestMonitor = null;
							double dist = Double.MAX_VALUE;
							for (TileEntityMonitor te : currentDiscovery.foundMonitors)
							{
								if (distance(te) < dist)
								{
									dist = distance(te);
									closestMonitor = te;
								}
							}
							System.out.println(currentDiscovery.foundMonitors.size());
							if (closestMonitor != null)
							{
								monitor = closestMonitor;
								monitor.setOwner(this);
								for (Node noe : node().reachableNodes())
								{
									if (noe.host() instanceof Context)
									{
										System.out.println("CTX");
										noe.connect(monitor.node());
									}
								}
							}
							currentDiscovery = null;
						}
					}
				}
				else
				{
					//find it in reachable nodes
					for (Node n : node.reachableNodes())
					{
						if (n.address().equals(monitorAddress))
						{
							monitor = (TileEntityMonitor) n.host();
							monitor.setOwner(this);
							for (Node noe : node().reachableNodes())
							{
								if (noe.host() instanceof Context)
								{
									System.out.println("CTX");
									noe.connect(monitor.node());
								}
							}
							break;
						}
					}
					if (monitor == null)
						monitorAddressFailures++;
					if (monitorAddressFailures > 5)
					{
						monitorAddress = null;
					}
				}
			}
			else
			{
				
			}
	}

	@Callback(direct=true)
	public Object[] writeInt(Context context, Arguments arguments)
	{
		if (fifoBytes+(4*arguments.count()) > fifoSize)
		{
			return new Object[]{false};
		}
		fifoBytes += (4*arguments.count());
		for (int i = 0; i < arguments.count(); i++)
			fifo.writeInt(arguments.checkInteger(i));
		return new Object[]{true};
	}
	
	@Callback(direct=true)
	public Object[] writeFloat(Context context, Arguments arguments)
	{
		if (fifoBytes+(4*arguments.count()) > fifoSize)
		{
			return new Object[]{false};
		}
		fifoBytes += (4*arguments.count());
		for (int i = 0; i < arguments.count(); i++)
			fifo.writeFloat((float) arguments.checkDouble(i));
		return new Object[]{true};
	}
	
	@Callback(direct=true)
	public Object[] writeShort(Context context, Arguments arguments)
	{
		if (fifoBytes+(arguments.count()*2) > fifoSize)
		{
			return new Object[]{false};
		}
		fifoBytes+=(arguments.count()*2);
		for (int i = 0; i < arguments.count(); i++)
			fifo.writeShort(arguments.checkInteger(i));
		return new Object[]{true};
	}
	
	@Callback(direct=true)
	public Object[] writeByte(Context context, Arguments arguments)
	{
		if (fifoBytes+arguments.count() > fifoSize)
		{
			return new Object[]{false};
		}
		fifoBytes+=arguments.count();
		for (int i = 0; i < arguments.count(); i++)
			fifo.writeByte(arguments.checkInteger(i));
		return new Object[]{true};
	}
	
	@Callback(direct=true)
	public Object[] writeBytes(Context context, Arguments arguments)
	{
		byte[] arr = arguments.checkByteArray(0);
		if (fifoBytes+arr.length > fifoSize)
		{
			return new Object[]{false};
		}
		fifoBytes+=arr.length;
		fifo.write(arr);
		return new Object[]{true};
	}
	
	@Callback(direct=true)
	public Object[] upload(Context context, Arguments arguments)
	{
		//convert fifo to byte array
		byte[] data = fifo.toByteArray();
		try
		{
			gx.uploadFIFO(ByteStreams.newDataInput(Arrays.copyOf(data, data.length)));
			GXFifoUploadMessage msg = new GXFifoUploadMessage();
			msg.x = this.xCoord;
			msg.y = this.yCoord;
			msg.z = this.zCoord;
			msg.data = data;
			OpenGX.network.sendToAllAround(msg, new TargetPoint(worldObj.provider.dimensionId, this.xCoord, this.yCoord, this.zCoord, 64));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		double waitTime = (((double)fifoBytes)/((double)fifoSize))*(1D/15D);
		//System.out.println(waitTime);
		//context.pause(waitTime);
		fifoBytes = 0;
		fifo = ByteStreams.newDataOutput(fifoSize);
		return null;
	}
	
	@Callback()
	public Object[] uploadTexture(Context context, Arguments arguments)
	{
		byte id = (byte) arguments.checkInteger(0);
		byte[] data = arguments.checkByteArray(1);
		byte fmt = (byte) arguments.checkInteger(2);
		
		GXTextureUploadMessage msg = new GXTextureUploadMessage();
		msg.x = this.xCoord;
		msg.y = this.yCoord;
		msg.z = this.zCoord;
		msg.id = id;
		msg.fmt = fmt;
		msg.data = data;
		OpenGX.network.sendToAllAround(msg, new TargetPoint(worldObj.provider.dimensionId, this.xCoord, this.yCoord, this.zCoord, 64));
		gx.uploadTexture(id, new ByteArrayInputStream(data), fmt);
		//technically, the fifo would have to be copied into memory in order for a texture to upload
		context.pause((data.length/1024D)*(1/5D));
		
		return null;
	}
	
	@Callback(direct=true)
	public Object[] getFifo(Context context, Arguments arguments)
	{
		return new Object[]{fifo.toByteArray()};
	}
	
	@Callback(direct=true)
	public Object[] getFifoUsage(Context context, Arguments arguments)
	{
		return new Object[]{fifoBytes};
	}
	
	@Callback(direct=true)
	public Object[] getFifoSize(Context context, Arguments arguments)
	{
		return new Object[]{fifoSize};
	}
	
	@Callback(direct=true)
	public Object[] clearFifo(Context context, Arguments arguments)
	{
		fifoBytes = 0;
		fifo = ByteStreams.newDataOutput(fifoSize);
		return null;
	}
	
	@Callback(direct=true)
	public Object[] getError(Context context, Arguments arguments)
	{
		return new Object[]{gx.getError(), gx.getErrorString()};
	}
	
	@Callback(direct=true)
	public Object[] get(Context context, Arguments arguments)
	{
		return new Object[]{gx.getValue(arguments.checkInteger(0), arguments.checkInteger(1), arguments.checkInteger(2), arguments.checkInteger(3))};
	}
	
	@Callback(direct=true)
	public Object[] getMonitorAddress(Context context, Arguments arguments)
	{
		if (monitor != null)
		{
			context.node().connect(monitor.node());
			return new Object[]{monitor.node().address()};
		}
		return null;
	}
	
	@Override
	public void onConnect(Node node)
	{
		if (!initd)
			init();
		if (node.host() instanceof Context)
		{
			node.connect(romGX.node());
		}
	}
	
	@Override
	public void onDisconnect(Node node)
	{
		System.out.println("Discon");
		if (node.host() instanceof Context)
		{
			node.disconnect(romGX.node());
			if (monitor != null) node.disconnect(monitor.node());
		}
		else if (monitor != null && monitor.node().address().equals(node.address()))
		{
			monitor = null;
			for (Node n : node().reachableNodes())
			{
				if (n.host() instanceof Context)
				{
					n.disconnect(node);
				}
			}
		}
		else if (node == node())
		{
			for (Node n : romGX.node().reachableNodes())
			{
				if (n.host() instanceof Context)
				{
					n.disconnect(romGX.node());
				}
			}
		}
	}

	@Override
	public void onMessage(Message message) {
		
	}

	@Override
	public void invalidate() {
		if (monitor != null)
		{
			for (Node n : node().reachableNodes())
			{
				if (n.host() instanceof Context)
				{
					n.disconnect(monitor.node());
				}
			}
		}
		super.invalidate();
	}
}
