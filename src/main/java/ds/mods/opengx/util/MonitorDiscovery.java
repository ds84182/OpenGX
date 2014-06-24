package ds.mods.opengx.util;

import java.util.ArrayList;
import java.util.Random;

import ds.mods.opengx.tileentity.TileEntityGX;
import ds.mods.opengx.tileentity.TileEntityMonitor;

public class MonitorDiscovery {
	public TileEntityGX gx;
	public long id;
	public ArrayList<TileEntityMonitor> foundMonitors = new ArrayList<TileEntityMonitor>();
	
	public MonitorDiscovery(TileEntityGX g)
	{
		gx = g;
		id = (new Random()).nextLong();
	}
}
