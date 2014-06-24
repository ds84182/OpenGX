package ds.mods.opengx.tileentity;

import java.util.Random;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Visibility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ds.mods.opengx.client.gx.GXFramebuffer;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityExternalMonitor extends TileEntityMonitor {
	public float r, g, b;
	public int mWidth = 1, mHeight = 1;
	public int mX, mY;
	public static final int MAX_WIDTH = 8;
	public static final int MAX_HEIGHT = 6;
	public int tiltest = 5;
	public boolean canConnect = true;
	public boolean putInRenderList = false;
	
	@SideOnly(Side.CLIENT)
	public GXFramebuffer fb;
	
	public TileEntityExternalMonitor()
	{
		r = (new Random()).nextFloat();
		g = (new Random()).nextFloat();
		b = (new Random()).nextFloat();
		node = Network.newNode(this, Visibility.Neighbors).withComponent("gxmonitor").create();
	}
	
	public int getXFromOrient(int x, int y)
	{
		return xCoord+x;
	}
	
	public int getYFromOrient(int x, int y)
	{
		return yCoord+y;
	}
	
	public int getZFromOrient(int x, int y)
	{
		return zCoord;
	}
	
	public boolean canConnectToTile(int x, int y, int z)
	{
		if (worldObj.getBlock(x, y, z) != getBlockType())
			return false;
		TileEntityExternalMonitor tile = (TileEntityExternalMonitor) worldObj.getTileEntity(x, y, z);
		return tile.canConnect && (tile.mWidth == mWidth || tile.mHeight == mHeight);
	}
	
	public void updateVolume()
	{
		for (int x=0; x<mWidth; x++)
		{
			for (int y=0; y<mHeight; y++)
			{
				TileEntityExternalMonitor rtile = (TileEntityExternalMonitor) worldObj.getTileEntity(getXFromOrient(x-mX,y-mY), getYFromOrient(x-mX,y-mY), getZFromOrient(x-mX,y-mY));
				rtile.r = r;
				rtile.g = g;
				rtile.b = b;
				rtile.mX = x;
				rtile.mY = y;
				rtile.mWidth = mWidth;
				rtile.mHeight = mHeight;
				rtile.owner = owner;
			}
		}
	}
	
	public boolean mergeLeft()
	{
		int px = getXFromOrient(mWidth-mX,-mY);
		int py = getYFromOrient(mWidth-mX,-mY);
		int pz = getZFromOrient(mWidth-mX,-mY);
		if (canConnectToTile(px,py,pz))
		{
			TileEntityExternalMonitor tile = (TileEntityExternalMonitor) worldObj.getTileEntity(px, py, pz);
			if (tile.mX == 0 && tile.mY == 0)
			{
				if (tile.mHeight == mHeight && mWidth+tile.mWidth <= MAX_WIDTH)
				{
					//we can merge
					mWidth += tile.mWidth;
					
					updateVolume();
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean mergeRight()
	{
		int px = getXFromOrient(-mX-1,-mY);
		int py = getYFromOrient(-mX-1,-mY);
		int pz = getZFromOrient(-mX-1,-mY);
		if (canConnectToTile(px,py,pz))
		{
			TileEntityExternalMonitor tile = (TileEntityExternalMonitor) worldObj.getTileEntity(px, py, pz);
			if (tile.mX == 0 && tile.mY == 0)
			{
				if (tile.mHeight == mHeight && mWidth+tile.mWidth <= MAX_WIDTH)
				{
					//we can merge
					mWidth += tile.mWidth;
					mX += tile.mWidth;
					
					updateVolume();
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean mergeUp()
	{
		int px = getXFromOrient(-mX,mHeight-mY);
		int py = getYFromOrient(-mX,mHeight-mY);
		int pz = getZFromOrient(-mX,mHeight-mY);
		if (canConnectToTile(px,py,pz))
		{
			TileEntityExternalMonitor tile = (TileEntityExternalMonitor) worldObj.getTileEntity(px, py, pz);
			if (tile.mX == 0 && tile.mY == 0)
			{
				if (tile.mWidth == mWidth && mHeight+tile.mHeight <= MAX_HEIGHT)
				{
					//we can merge
					mHeight += tile.mHeight;
					
					updateVolume();
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean mergeDown()
	{
		int px = getXFromOrient(-mX,-mY-1);
		int py = getYFromOrient(-mX,-mY-1);
		int pz = getZFromOrient(-mX,-mY-1);
		if (canConnectToTile(px,py,pz))
		{
			TileEntityExternalMonitor tile = (TileEntityExternalMonitor) worldObj.getTileEntity(px, py, pz);
			if (tile.mX == 0 && tile.mY == 0)
			{
				if (tile.mWidth == mWidth && mHeight+tile.mHeight <= MAX_HEIGHT)
				{
					//we can merge
					mHeight += tile.mHeight;
					mY += tile.mHeight;
					
					updateVolume();
					return true;
				}
			}
		}
		return false;
	}
	
	public void testMerge()
	{
		if (mX != 0 || mY != 0)
			return;
		try
		{
			TileEntityExternalMonitor origin = (TileEntityExternalMonitor) worldObj.getTileEntity(getXFromOrient(-mX,-mY), getYFromOrient(-mX,-mY), getZFromOrient(-mX,-mY));
			while(origin.mergeUp() | origin.mergeDown() | origin.mergeLeft() | origin.mergeRight())
			{
				origin = (TileEntityExternalMonitor) worldObj.getTileEntity(getXFromOrient(-mX,-mY), getYFromOrient(-mX,-mY), getZFromOrient(-mX,-mY));
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (tiltest--<=0)
		{
			tiltest = 20;
			testMerge();
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (mWidth > 1 || mHeight > 1)
		{
			for (int x=0; x<mWidth; x++)
			{
				for (int y=0; y<mHeight; y++)
				{
					TileEntityExternalMonitor rtile = (TileEntityExternalMonitor) worldObj.getTileEntity(getXFromOrient(x-mX,y-mY), getYFromOrient(x-mX,y-mY), getZFromOrient(x-mX,y-mY));
					if (rtile == null) continue;
					rtile.r = (new Random()).nextFloat();
					rtile.g = (new Random()).nextFloat();
					rtile.b = (new Random()).nextFloat();
					rtile.mX = 0;
					rtile.mY = 0;
					rtile.mWidth = 1;
					rtile.mHeight = 1;
				}
			}
		}
		canConnect = false;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		r = nbt.getFloat("red");
		g = nbt.getFloat("green");
		b = nbt.getFloat("blue");
		mWidth = nbt.getInteger("mWidth");
		mHeight = nbt.getInteger("mHeight");
		mX = nbt.getInteger("mX");
		mY = nbt.getInteger("mY");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setFloat("red", r);
		nbt.setFloat("green", g);
		nbt.setFloat("blue", b);
		nbt.setInteger("mWidth", mWidth);
		nbt.setInteger("mHeight", mHeight);
		nbt.setInteger("mX", mX);
		nbt.setInteger("mY", mY);
	}

	@Override
	public void setOwner(TileEntityGX o) {
		super.setOwner(o);
		updateVolume();
	}
	
	@Override
	public void onChanged()
	{
		updateVolume();
	}
}
