package ds.mods.opengx.util;

import java.util.ArrayList;
import java.util.Random;

import li.cil.oc.api.network.ManagedEnvironment;
import ds.mods.opengx.tileentity.TileEntityGX;
import ds.mods.opengx.tileentity.TileEntityMonitor;

public class MonitorDiscovery {
	public ManagedEnvironment gx;
	public long id;
	public ArrayList<TileEntityMonitor> foundMonitors = new ArrayList<TileEntityMonitor>();
	
	public MonitorDiscovery(ManagedEnvironment managedEnvironment)
	{
		gx = managedEnvironment;
		id = (new Random()).nextLong();
	}
}
