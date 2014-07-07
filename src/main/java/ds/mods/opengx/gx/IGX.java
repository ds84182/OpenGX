package ds.mods.opengx.gx;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.util.Tuple;

import com.google.common.io.ByteArrayDataInput;


/**
 * Interface that contains common methods for the GX runtime
 * @author ds84182
 *
 */
public interface IGX {
	public static final int GX_INIT = 0;
	
	public static final int GX_FMT_BASE85 = 0;
	
	/**
	 * The implementing GX class reads and processes fifo commands from this
	 * @param fifo
	 */
	public void uploadFIFO(ByteArrayDataInput fifo, byte[] fifoData);
	/**
	 * Tells the GX that it has a texture do process
	 * @param id
	 * @param data
	 * @param format
	 */
	public void uploadTexture(short id, ByteArrayInputStream data, byte format);
	/**
	 * Renders the screen. Usually a FBO is already binded and set up
	 */
	public void render(int fbwidth, int fbheight);
	/**
	 * Reset the GX
	 */
	public void reset();
	/**
	 * Returns the type of GX
	 * @return the type of GX
	 */
	public String type();
	/**
	 * Returns the error code or 0 if no error
	 * @return
	 */
	public int getError();
	/**
	 * Returns the error string or "no error" if no error
	 * @return
	 */
	public String getErrorString();
	/**
	 * Allows the CPU to index values inside the GX
	 * @param index
	 * @param subindex
	 * @return
	 */
	public Object getValue(int index, int subindex, int supersub, int suprasub);
	/**
	 * Creates a load of packets to send to a client in order to get them up to date
	 * @return a megafuck list of packets
	 */
	public ArrayList<Pair<DataType, byte[]>> createMegaUpdate();
	
	enum DataType {
		FIFO, TEXTURE
	}
}
