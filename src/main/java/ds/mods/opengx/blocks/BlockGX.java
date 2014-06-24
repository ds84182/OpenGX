package ds.mods.opengx.blocks;

import ds.mods.opengx.tileentity.TileEntityGX;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockGX extends Block {
	IIcon gxside, gxtopbottom, gxt1top;

	public BlockGX(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		System.out.println("NewTE");
		return new TileEntityGX();
	}

	@Override
	public boolean hasTileEntity(int metadata) {
		return true;
	}

	@Override
	public void registerBlockIcons(IIconRegister ir) {
		gxside = ir.registerIcon("opengx:gxside");
		gxtopbottom = ir.registerIcon("opengx:gxtopbottom");
		gxt1top = ir.registerIcon("opengx:gxt1top");
	}

	@Override
	public IIcon getIcon(IBlockAccess p_149673_1_, int p_149673_2_,
			int p_149673_3_, int p_149673_4_, int p_149673_5_) {
		// TODO Auto-generated method stub
		return super.getIcon(p_149673_1_, p_149673_2_, p_149673_3_, p_149673_4_,
				p_149673_5_);
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		if (side == 1 && meta == 0)
			return gxt1top;
		return side < 2 ? gxtopbottom : gxside;
	}

}
