package ds.mods.opengx.gx.tier2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.io.ByteArrayDataInput;

import ds.mods.opengx.gx.GXMatrix;
import ds.mods.opengx.gx.GXServerTexture;
import ds.mods.opengx.gx.IGX;

public class Tier2GX implements IGX {
	public GXServerTexture[] serverTextures = new GXServerTexture[64];
	public GXPolygon[] polygons = new GXPolygon[512]; //supports up to 512 polygons rendering at the same time
	public int nrpolygons = 0; //tells the renderer that we are rendering that many polygons.
	public GXMatrix matrix = new GXMatrix();
	
	public static final int GX_ADD_POLYGON = 1;
	public static final int GX_ADD_POLYGONS = 2;
	public static final int GX_CLEAR_POLYGONS = 3;
	public static final int GX_DISABLE_CLEAR = 4;
	public static final int GX_SET_CLEAR_COLOR = 5;
	public static final int GX_ENABLE_SELECTIVE_RENDER = 6;
	public static final int GX_DO_RENDER = 7;
	public static final int GX_LOAD_MATRIX = 8;
	public static final int GX_MULTIPLY_MATRIX = 9;
	public static final int GX_LOAD_IDENTITY_MATRIX = 10;
	
	public int error = 0;
	public static final int GX_ERROR_NONE = 0;
	
	public static final String errorUnknown = "An unknown error has occured";
	public static final String[] errorDescriptions = {
	};
	public String additionalInfo;
	
	public boolean clear = true;
	public float cR, cG, cB;
	
	public boolean selectiveRender = false;
	public boolean doRender = false;
	
	private void addPolygon(ByteArrayDataInput fifo)
	{
		if (polygons[nrpolygons] == null)
			polygons[nrpolygons++] = new GXPolygon(fifo,matrix);
		else
			polygons[nrpolygons++].update(fifo,matrix);
	}

	@Override
	public void uploadFIFO(ByteArrayDataInput fifo, byte[] fifoData) {
		byte lastCommand = -1;
		while (true)
		{
			byte b;
			try
			{
				b = fifo.readByte();
			}
			catch(Exception e)
			{
				break;
			}
			if (b == GX_INIT)
			{
				System.out.println("GX_INIT");
				error = 0;
				additionalInfo = null;
				reset();
			}
			else if (b == GX_ADD_POLYGON)
			{
				addPolygon(fifo);
			}
			else if (b == GX_ADD_POLYGONS)
			{
				int np = fifo.readInt();
				for (int i=0; i<np; i++)
					addPolygon(fifo);
			}
			else if (b == GX_CLEAR_POLYGONS)
			{
				nrpolygons = 0;
			}
			else if (b == GX_DISABLE_CLEAR)
			{
				clear = false;
			}
			else if (b == GX_SET_CLEAR_COLOR)
			{
				clear = true;
				cR = fifo.readFloat();
				cG = fifo.readFloat();
				cB = fifo.readFloat();
			}
			else if (b == GX_ENABLE_SELECTIVE_RENDER)
			{
				selectiveRender = true;
			}
			else if (b == GX_DO_RENDER)
			{
				doRender = true;
			}
			else if (b == GX_LOAD_MATRIX)
			{
				matrix = new GXMatrix(fifo);
			}
			else if (b == GX_MULTIPLY_MATRIX)
			{
				GXMatrix.mul(matrix, new GXMatrix(fifo), matrix);
			}
			else if (b == GX_LOAD_IDENTITY_MATRIX)
			{
				matrix = new GXMatrix();
			}
		}
	}

	@Override
	public void reset() {
		for (int i=0; i<serverTextures.length; i++)
		{
			serverTextures[i] = null;
		}
		nrpolygons = 0;
		clear = true;
		cR = 0.0F;
		cG = 0.0F;
		cB = 0.0F;
		selectiveRender = false;
		doRender = false;
		matrix = new GXMatrix();
	}
	
	@Override
	public void uploadTexture(short id, ByteArrayInputStream data, byte format) {
		byte[] arr = new byte[data.available()];
		try {
			data.read(arr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		serverTextures[id] = new GXServerTexture(format, arr);
	}

	@Override
	public String type() {
		return "gxt2";
	}

	@Override
	public int getError() {
		return error;
	}

	@Override
	public String getErrorString() {
		return (-error >= errorDescriptions.length ? errorUnknown : errorDescriptions[-error])+(additionalInfo != null ? " : "+additionalInfo : "");
	}

	@Override
	public Object getValue(int index, int subindex, int supersub, int suprasub) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Pair<DataType, byte[]>> createMegaUpdate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void render(int fbwidth, int fbheight) {
		// TODO Auto-generated method stub
		
	}
	
}
