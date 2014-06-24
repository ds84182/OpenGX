package ds.mods.opengx.gx;

public class GXServerTexture
{
	public byte format;
	public byte[] data;
	
	public GXServerTexture(byte f, byte[] d)
	{
		format = f;
		data = d;
	}
}