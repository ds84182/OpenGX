package ds.mods.opengx.blocks;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ds.mods.opengx.tileentity.TileEntityExternalMonitor;

public class BlockExternalMonitor extends Block {

	public BlockExternalMonitor(Material mat) {
		super(mat);
	}
	
	@Override
	public boolean hasTileEntity(int metadata) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return new TileEntityExternalMonitor();
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int getRenderType() {
		return -1;
	}

	@Override
	public boolean onBlockActivated(World world, int x,
			int y, int z, EntityPlayer player,
			int side, float cx, float cy,
			float cz) {
		TileEntityExternalMonitor extern = (TileEntityExternalMonitor) world.getTileEntity(x, y, z);
		System.out.println(FMLCommonHandler.instance().getEffectiveSide());
		extern.testMerge();
		return true;
	}

}
