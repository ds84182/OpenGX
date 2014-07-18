package ds.mods.opengx.gx.tier2;

import com.google.common.io.ByteArrayDataInput;

import ds.mods.opengx.gx.GXMatrix;

public class GXPolygon {
	public float[] x, y, u, v;
	public int color;
	public byte tex = -1;
	
	public GXPolygon(ByteArrayDataInput fifo, GXMatrix mtx)
	{
		update(fifo,mtx);
	}

	public void update(ByteArrayDataInput fifo, GXMatrix mtx) {
		tex = fifo.readByte();
		color = fifo.readInt();
		int len = Math.min(16, fifo.readByte());
		x = new float[len];
		y = new float[len];
		u = new float[len];
		v = new float[len];
		for (int i=0; i<len; i++)
		{
			x[i] = fifo.readFloat();
			y[i] = fifo.readFloat();
			if (tex != -1)
			{
				u[i] = fifo.readFloat();
				v[i] = fifo.readFloat();
			}
		}
	}
}
