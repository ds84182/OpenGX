package ds.mods.opengx.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import ds.mods.opengx.OpenGX;
import ds.mods.opengx.tileentity.TileEntityExternalMonitor;

public class BlockExternalMonitor extends Block {

	public BlockExternalMonitor(Material mat) {
		super(mat);
		setCreativeTab(OpenGX.tab);
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
	public void onBlockPlacedBy(World world, int x,
			int y, int z, EntityLivingBase entity,
			ItemStack item) {
		TileEntityExternalMonitor mon = (TileEntityExternalMonitor) world.getTileEntity(x, y, z);
		mon.facing = ForgeDirection.VALID_DIRECTIONS[BlockPistonBase.determineOrientation(world, x, y, z, entity)];
	}

}
