package ds.mods.opengx.gx.tier1;

import com.google.common.io.ByteArrayDataInput;

public class GXMap
{
	public short width;
	public short height;
	public short x;
	public short y;
	public short tileOffset;
	public int color;
	public short[] data;
	public boolean finished = false;
	public int dataidx = 0;

	public GXMap(short w, short h, ByteArrayDataInput fifo)
	{
		width = w;
		height = h;
		data = new short[w*h];
		color = 0xFFFFFFFF;
		feed(fifo);
	}

	public void feed(ByteArrayDataInput fifo) {
		short damnt = fifo.readShort();
		for (int i=0; i<damnt; i++)
		{
			data[dataidx++] = (short) (fifo.readByte() & 0xFF); //make it unsigned
		}
		finished = dataidx<data.length;
	}

	public short getTile(short x, short y)
	{
		return (short) (data[(y*height)+x]+tileOffset);
	}
	
	public void setTile(short x, short y, short t)
	{
		data[(y*height)+x] = t;
	}
}