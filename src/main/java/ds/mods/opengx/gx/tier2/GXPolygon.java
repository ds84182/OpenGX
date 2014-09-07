package ds.mods.opengx.gx.tier2;

import com.google.common.io.ByteArrayDataInput;

import ds.mods.opengx.gx.GXMatrix;

public class GXPolygon {
	public float[] x, y, u, v;
	public int color;
	public byte tex = -1;
	public String mctex;
	
	public GXPolygon(ByteArrayDataInput fifo, GXMatrix mtx)
	{
		update(fifo,mtx);
	}

	public void update(ByteArrayDataInput fifo, GXMatrix mtx) {
		tex = fifo.readByte();
		if (tex == -2)
		{
			//mctex breh
			byte len = fifo.readByte();
			byte[] data = new byte[len];
			fifo.readFully(data);
			mctex = new String(data);
			if (mctex.startsWith("minecraft:"))
			{
				mctex = mctex.substring(10); //so people won't have that stupid ass namespace error shit.
				//TODO: reference from block names completely
			}
		}
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
			if (tex == -2)
			{
				u[i] = Math.min(Math.abs(u[i]), 1.0F);//clamp values so we can't
				v[i] = Math.min(Math.abs(v[i]), 1.0F);//do evil things
			}
		}
	}
}
