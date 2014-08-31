package ds.mods.opengx.util;

import java.util.ArrayList;
import java.util.Random;

import li.cil.oc.api.network.ManagedEnvironment;
import ds.mods.opengx.component.ComponentMonitor;

public class MonitorDiscovery {
	public ManagedEnvironment gx;
	public long id;
	public ArrayList<ComponentMonitor> foundMonitors = new ArrayList<ComponentMonitor>();
	
	public MonitorDiscovery(ManagedEnvironment managedEnvironment)
	{
		gx = managedEnvironment;
		id = (new Random()).nextLong();
	}
}
