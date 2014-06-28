package ds.mods.opengx;

import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkWatchEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ds.mods.opengx.tileentity.TileEntityGX;

public class Events {
	@SubscribeEvent
	public void onChunkWatchedByPlayer(ChunkWatchEvent event)
	{
		if (!event.player.worldObj.isRemote)
		{
			//get all the TileEntities in the chunk
			Chunk c = event.player.worldObj.getChunkFromChunkCoords(event.chunk.chunkXPos, event.chunk.chunkZPos);
			int nt = 0;
			for (Object t : c.chunkTileEntityMap.values())
			{
				if (t instanceof TileEntityGX)
				{
					nt++;
				}
			}
			if (nt>0)
			{
				System.out.println(event.player.getDisplayName()+" is watching chunk with "+nt+" TileEntityGX tile entities");
			}
		}
	}
}
